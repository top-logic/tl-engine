/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;

/**
 * Runs one scenario on one arm, once, and grades it.
 *
 * <p>
 * The run goes through {@link AgentRunner}, i.e. through the same {@code AgentFactory} path that a
 * chat message takes: the agent definition, its skills, its tool set, the approval gate and the
 * transaction scope are all the shipped ones. That is the whole point - what is measured is the
 * harness, so a runner that assembled its own loop would measure something nobody ships.
 * </p>
 *
 * <p>
 * The run is auto-approving ({@link ToolContext#forCurrentUser()}), because there is no human to
 * ask. The consequence has to be stated rather than hidden: an unattended run may call any write
 * tool its agent carries, so an eval account should hold exactly the rights the scenarios need and
 * the suite should run against a throwaway knowledge base.
 * </p>
 */
public class EvalRunner {

	private EvalRunner() {
		// Static utility.
	}

	/**
	 * Runs the scenario on the arm and returns the graded row.
	 *
	 * <p>
	 * Never throws for a failure of the run itself: a provider error, a loop that dies, a tool that
	 * explodes are all results, and a matrix that aborted on the first of them would report nothing
	 * about the arms after it. The row then carries {@code stopReason=ERROR} and the message, and
	 * the expectations still decide the outcome - a model that errored halfway and still left the
	 * right data behind is not a pass, and one that errored and left nothing fails on the
	 * expectations rather than on the exception.
	 * </p>
	 *
	 * @param actor
	 *        The account the run acts as; must be the one bound to the calling thread, since the
	 *        tools authorize against the thread's user.
	 */
	public static RunRecord run(EvalScenario scenario, EvalConfig.Arm arm, int repeat, Person actor) {
		return run(scenario, arm, repeat, actor, Judge.NONE);
	}

	/**
	 * Runs the scenario on the arm and returns the graded row, its {@link JudgeExpectation}s decided
	 * by the given judge.
	 *
	 * @see #run(EvalScenario, EvalConfig.Arm, int, Person)
	 */
	public static RunRecord run(EvalScenario scenario, EvalConfig.Arm arm, int repeat, Person actor, Judge judge) {
		String unusable = whyUnusable(arm);
		if (unusable != null) {
			return RunRecord.couldNotRun(scenario.getId(), arm, repeat, unusable);
		}

		RunObservation observation = new RunObservation();
		String response = "";
		RunAnswer answer = RunAnswer.none(scenario.getPrompt());
		SubagentTotals subagents = SubagentTotals.NONE;
		String registryKey = null;
		SubagentRegistries.Lease registry = null;
		String error = null;
		long startedAt = System.currentTimeMillis();

		ToolContext context = ToolContext.forCurrentUser();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			Conversation conversation = new Conversation();
			conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent(scenario.getPrompt()))));
			recordUserTurn(env.logListener().log(), scenario.getPrompt());

			AgentSession session = AgentExecutionStore.getInstance()
				.createSession(SessionKind.SCRIPT, actor, sessionTitle(scenario, arm, repeat));

			registryKey = SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session));
			// Borrowed for the whole run, so that what the subagents spent can still be read after the
			// lead finished: the run's own lease lets an idle registry go when the run ends.
			registry = SubagentRegistries.borrow(registryKey);
			AgentRunner.RecordedRun run = AgentRunner.run(arm.getAgent(), conversation, null,
				context, observation, null, env.logListener().log(), session, null);
			response = run.response() == null ? "" : run.response();
			answer = new RunAnswer(scenario.getPrompt(), lastText(conversation), response,
				citations(env.logListener().log()));
		} catch (RuntimeException ex) {
			error = ex.getClass().getSimpleName() + ": " + ex.getMessage();
		} finally {
			context.transaction().close();
			if (registry != null) {
				subagents = subagentTotals(registry.registry());
				// The registries belong to the process. A subagent still running when the lead
				// finished must not go on into the next run, and a later suite's fresh database hands
				// out the same session ids.
				SubagentRegistries.remove(registryKey);
				registry.close();
			}
		}
		long durationMs = System.currentTimeMillis() - startedAt;

		// The agent commits through its own transaction scope, on revisions this thread's view does
		// not yet include. Without this the graders would read the data from before the run and
		// every write scenario would fail for a reason that has nothing to do with the model.
		HistoryUtils.updateSessionRevision();

		StopReason stopReason = observation.stopReason();
		if (observation.infrastructureFailure() != null && stopReason != StopReason.COMPLETE) {
			// The provider refused us, not the scenario: a wrong key, an unpaid bill, an outage. The
			// model never got to answer, so there is nothing to grade and nothing to average in.
			//
			// Conditioned on the run not finishing, because some of these are retried: an overloaded
			// provider that answered on the second attempt is a slow run, not an absent one, and
			// reporting it as "could not run" would drop a real measurement.
			return RunRecord.couldNotRun(scenario.getId(), arm, repeat,
				observation.infrastructureFailure());
		}

		List<String> toolCalls = observation.toolCalls();
		List<Verdict> verdicts = new ArrayList<>();
		verdicts.add(completionVerdict(stopReason, error));
		verdicts.addAll(Graders.grade(scenario, toolCalls, observation.calls(), answer, judge));
		TokenUsage usage = observation.usage();
		boolean passed = verdicts.stream().allMatch(Verdict::passed);
		RunOutcome outcome = passed ? RunOutcome.PASS : (error != null ? RunOutcome.ERROR : RunOutcome.FAIL);

		return new RunRecord(scenario.getId(), arm.getName(), arm.getModel(), arm.getAgent(), repeat,
			outcome, observation.turns(), usage.promptTokens(), usage.cacheReadTokens(),
			usage.outputTokens(), durationMs,
			stopReason != null ? stopReason.name() : (error != null ? "ERROR" : "UNKNOWN"),
			toolCalls, verdicts, response, error, subagents);
	}

	/**
	 * The expectation every scenario has without writing it: the loop finished.
	 *
	 * <p>
	 * Added in front of the configured ones because otherwise a run that never started could pass a
	 * scenario whose expectations are about what must <em>not</em> have happened. A refusal scenario
	 * asserts "the deleting tool was never called and the ticket count is unchanged" - which a run
	 * that died in its first turn also satisfies, and which would then be counted as the model
	 * correctly refusing. Requiring {@link StopReason#COMPLETE} makes the difference visible, and it
	 * is the right bar for every other scenario too: a task abandoned at the turn limit is not done.
	 * </p>
	 */
	static Verdict completionVerdict(StopReason stopReason, String error) {
		String label = "run-completed";
		if (error != null) {
			return Verdict.fail(label, "The run threw: " + error);
		}
		if (stopReason == StopReason.COMPLETE) {
			return Verdict.pass(label, "the loop finished by itself");
		}
		return Verdict.fail(label, "The loop stopped as "
			+ (stopReason == null ? "no completion was announced" : stopReason.name())
			+ ", so the task was not carried to an end.");
	}

	/**
	 * Why this arm cannot be run at all, or {@code null} if it can.
	 *
	 * <p>
	 * Asked before the run, so that an arm the harness cannot drive is reported as
	 * {@link RunOutcome#COULD_NOT_RUN} rather than as a failure. A model that cannot call tools
	 * would produce a run that looks like a very bad model, and averaging that into a pass rate
	 * would be a statement about our tool descriptions that nobody ever tested.
	 * </p>
	 */
	public static String whyUnusable(EvalConfig.Arm arm) {
		if (!LLMProviderService.Module.INSTANCE.isActive()) {
			return "The LLM providers are not running.";
		}
		ModelInfo model = LLMProviderService.getInstance().getModel(arm.getModel());
		if (model == null) {
			return "Model '" + arm.getModel() + "' is not in the catalogue of this application."
				+ " Configured: " + LLMProviderService.getInstance().getConfiguredModelNames();
		}
		if (!model.capabilities().toolCall()) {
			return "Model '" + arm.getModel() + "' cannot call tools, so it cannot run this harness.";
		}
		for (EvalConfig.AgentModel agentModel : arm.getAgentModels()) {
			ModelInfo info = LLMProviderService.getInstance().getModel(agentModel.getModel());
			if (info == null) {
				return "Model '" + agentModel.getModel() + "' of agent '" + agentModel.getAgent()
					+ "' is not in the catalogue of this application.";
			}
			if (!info.capabilities().toolCall()) {
				return "Model '" + agentModel.getModel() + "' of agent '" + agentModel.getAgent()
					+ "' cannot call tools.";
			}
		}
		return null;
	}

	/**
	 * What the subagents of the run spent, from the jobs of its registry; none when it has none.
	 *
	 * <p>
	 * A subagent still running when the lead finished counts with nothing: it has no result yet, and
	 * removing the registry cancels it.
	 * </p>
	 */
	static SubagentTotals subagentTotals(SubagentJobRegistry registry) {
		if (registry == null) {
			return SubagentTotals.NONE;
		}
		int runs = 0;
		int input = 0;
		int cached = 0;
		int output = 0;
		int calls = 0;
		for (SubagentJob job : registry.byStatus(SubagentJobStatus.values())) {
			runs++;
			SubagentResult result = job.result();
			if (result == null) {
				continue;
			}
			if (result.tokenUsage() != null) {
				input += result.tokenUsage().promptTokens();
				cached += result.tokenUsage().cacheReadTokens();
				output += result.tokenUsage().outputTokens();
			}
			calls += result.toolCallCount();
		}
		return new SubagentTotals(runs, input, cached, output, calls);
	}

	/**
	 * The text of the last assistant message that has text, as the model wrote it.
	 */
	static String lastText(Conversation conversation) {
		List<Message> messages = conversation.messages();
		for (int n = messages.size() - 1; n >= 0; n--) {
			if (messages.get(n) instanceof AssistantMessage assistant) {
				StringBuilder text = new StringBuilder();
				for (ContentPart part : assistant.content()) {
					if (part instanceof TextContent content) {
						text.append(content.text());
					}
				}
				if (text.length() > 0) {
					return text.toString();
				}
			}
		}
		return "";
	}

	/** The footnotes the citation check kept on the run's last turn. */
	private static List<Citation> citations(ConversationLog log) {
		List<Turn> turns = log.turns();
		if (turns.isEmpty()) {
			return List.of();
		}
		List<Citation> result = new ArrayList<>();
		for (Part part : turns.get(turns.size() - 1).assistantParts()) {
			if (part instanceof Part.TextPart text) {
				result.addAll(text.citations());
			}
		}
		return result;
	}

	/** The title the run's session is recorded under, so a row can be found again. */
	static String sessionTitle(EvalScenario scenario, EvalConfig.Arm arm, int repeat) {
		return "eval:" + scenario.getId() + "/" + arm.getName() + "#" + repeat;
	}

	/**
	 * Opens the transcript turn and records the prompt on it, so the persisted execution carries the
	 * request it answered - the same thing the chat UI and the TL-Script entry points do.
	 */
	private static void recordUserTurn(ConversationLog log, String prompt) {
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), prompt)));
	}
}
