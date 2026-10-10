/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentProgress;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.web.WebSearchHit;
import com.top_logic.tl.ai.core.tools.web.WebToolsService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.model.ModelService;

/**
 * Albert's {@code TL_deepResearch} skill with the shipped {@code TL_ResearchWorker}, run end to end
 * on a scripted model and the {@link FixtureWeb} (#723).
 *
 * <p>
 * What the model answers is scripted; everything those answers set in motion is the real thing: the
 * shipped agent definitions, the skill's tools, {@code spawn_subagent} starting one background run
 * per sub-question, the workers' web tools, the delivery of their results to the waiting lead, the
 * join of their reading lists into the lead's, and the citation check of the report. The shape this
 * pins is the one the skill prescribes: the lead loads the skill, starts its workers in one turn,
 * waits, never reads a page itself, and writes one report whose links point at pages the workers
 * read.
 * </p>
 *
 * <p>
 * Whether a real model follows the skill is not something a scripted model can say; that is what
 * the research eval with real models measures.
 * </p>
 */
public class TestDeepResearchSkill extends BasicTestCase {

	private static final String ALBERT = "TL_SingleAgent";

	private static final String WORKER = "TL_ResearchWorker";

	private static final String QUESTION = "Compare the heat pump subsidies of Germany, Austria and Denmark.";

	private static final String INVENTED = "https://invented.test/never-read";

	private static final List<Topic> TOPICS = List.of(
		new Topic("Germany", "heat pump subsidy Germany 2026", "https://bafa.de.test/heat-pumps",
			"Germany pays up to 70 percent of the cost of a heat pump."),
		new Topic("Austria", "heat pump subsidy Austria 2026", "https://kesseltausch.at.test/heat-pumps",
			"Austria pays up to 7500 euros for a heat pump."),
		new Topic("Denmark", "heat pump subsidy Denmark 2026", "https://ens.dk.test/heat-pumps",
			"Denmark pays a fixed subsidy for heat pumps that replace oil boilers."));

	/**
	 * One sub-question, the query its worker searches with, and the page it reads.
	 */
	private record Topic(String country, String query, String url, String fact) {

		String task() {
			return "Sub-question: What heat pump subsidy does " + country + " pay in 2026?\n"
				+ "Purpose: one section of the comparison.\nScope: 2026, " + country + ".\n"
				+ "Budget: at most 15 tool calls.";
		}

		String findings() {
			return "## Answer\n" + fact + "\n\n## Findings\n- " + fact + " - [" + country + " subsidy page]("
				+ url + " \"" + fact + "\")\n\n## Conflicts and gaps\n- None.";
		}
	}

	private String _registryKey;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		for (Topic topic : TOPICS) {
			FixtureWeb.search(topic.query(),
				new WebSearchHit(topic.country() + " subsidy page", topic.url(), topic.fact(), ""));
			FixtureWeb.page(topic.url(), topic.country() + " subsidy page", "# Subsidies\n\n" + topic.fact());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		FixtureWeb.clear();
		if (_registryKey != null) {
			// The registries belong to the process, and the next suite's fresh database hands out the
			// same session ids: a registry left here would be found by the next test's run.
			SubagentRegistries.remove(_registryKey);
		}
		super.tearDown();
	}

	/**
	 * The whole run: the lead plans and fans out, every worker searches and reads, the lead reads no
	 * page itself, and its one report cites exactly the pages the workers read.
	 */
	public void testTheLeadDelegatesTheReadingAndCitesWhatTheWorkersRead() {
		ScriptedProvider.install(TestDeepResearchSkill::reply);
		Person root = PersonManager.getManager().getRoot();
		AgentSession session = AgentExecutionStore.getInstance().createSession(SessionKind.SCRIPT, root,
			"TestDeepResearchSkill");

		_registryKey = SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session));
		Run run = run(root, session);

		assertEquals(StopReason.COMPLETE, run.stopReason());
		List<String> leadCalls = run.toolCalls();
		assertEquals("The lead loads the skill, starts one worker per sub-question in one turn and waits.",
			List.of("loadSkill", "spawn_subagent", "spawn_subagent", "spawn_subagent", "wait_for_subagent"),
			leadCalls);

		SubagentJobRegistry registry = SubagentRegistries.find(_registryKey);
		assertNotNull("The conversation's registry ran the workers.", registry);
		List<SubagentJob> jobs = registry.byStatus(SubagentJobStatus.values());
		assertEquals("Spawn answers: " + run.conversation().messages(), TOPICS.size(), jobs.size());
		for (SubagentJob job : jobs) {
			assertEquals(WORKER, job.targetAgentName());
			assertEquals("Worker failed: " + job.subagentId(), SubagentJobStatus.DONE, job.status());
			assertEquals("Every worker searches, then reads the page it found.",
				List.of("webSearch", "webFetch"), toolNames(job.progress()));
			for (SubagentProgress.Step step : job.progress().steps()) {
				assertEquals(step.toolName(), SubagentProgress.Outcome.OK, step.outcome());
			}
		}

		Set<String> cited = new TreeSet<>();
		for (Citation citation : run.citations()) {
			cited.add(citation.url());
		}
		Set<String> read = new TreeSet<>();
		for (Topic topic : TOPICS) {
			read.add(topic.url());
		}
		assertEquals("The report cites exactly the pages the workers read.", read, cited);
		assertEquals("The report is written once.", 1, run.reports());
		assertFalse("A link to a page nobody read keeps no address: " + run.response(),
			run.response().contains(INVENTED));
		assertTrue("The statement of the invented link stays as text: " + run.response(),
			run.response().contains("Sweden pays nothing"));
	}

	/**
	 * The lead loads the skill, plans and spawns, waits, writes; a worker searches, reads, reports.
	 */
	private static List<ContentPart> reply(ProviderRequest request, int turn) {
		String prompt = String.valueOf(request.systemPrompt());
		if (prompt.startsWith("You are a research worker.")) {
			return worker(request, turn);
		}
		switch (turn) {
			case 0:
				return List.of(new TextContent("I will research this."),
					call("loadSkill", "{\"skillId\": \"TL_deepResearch\"}"));
			case 1: {
				List<ContentPart> parts = new ArrayList<>();
				parts.add(new TextContent("Effort: standard. Plan:\n1. Germany\n2. Austria\n3. Denmark\n"
					+ "The workers start now."));
				for (Topic topic : TOPICS) {
					parts.add(call("spawn_subagent",
						"{\"agent\": \"" + WORKER + "\", \"task\": " + json(topic.task()) + "}"));
				}
				return parts;
			}
			case 2:
				return List.of(call("wait_for_subagent", "{\"timeoutMs\": 120000}"));
			default:
				StringBuilder report = new StringBuilder("# Heat pump subsidies compared\n\n");
				for (Topic topic : TOPICS) {
					report.append("## ").append(topic.country()).append("\n[").append(topic.fact())
						.append("](").append(topic.url()).append(")\n\n");
				}
				report.append("## Open points\n[Sweden pays nothing](").append(INVENTED).append(").\n");
				return List.of(new TextContent(report.toString()));
		}
	}

	private static List<ContentPart> worker(ProviderRequest request, int turn) {
		String brief = String.valueOf(request.messages().get(0));
		Topic topic = TOPICS.stream().filter(t -> brief.contains(t.task().substring(0, 50)))
			.findFirst().orElseThrow(() -> new IllegalStateException("Unknown worker task: " + brief));
		switch (turn) {
			case 0:
				return List.of(call("webSearch", "{\"query\": " + json(topic.query()) + "}"));
			case 1:
				return List.of(call("webFetch", "{\"url\": " + json(topic.url()) + "}"));
			default:
				return List.of(new TextContent(topic.findings()));
		}
	}

	private record Run(StopReason stopReason, Conversation conversation, String response, ConversationLog log) {

		List<String> toolCalls() {
			List<String> result = new ArrayList<>();
			for (Message message : conversation.messages()) {
				if (message instanceof ToolResultMessage toolResult) {
					result.add(toolResult.toolName());
				}
			}
			return result;
		}

		List<Citation> citations() {
			List<Citation> result = new ArrayList<>();
			for (Part.TextPart text : texts()) {
				result.addAll(text.citations());
			}
			return result;
		}

		int reports() {
			int result = 0;
			for (Part.TextPart text : texts()) {
				if (text.text() != null && text.text().startsWith("# ")) {
					result++;
				}
			}
			return result;
		}

		private List<Part.TextPart> texts() {
			List<Part.TextPart> result = new ArrayList<>();
			for (Turn turn : log.turns()) {
				for (Part part : turn.assistantParts()) {
					if (part instanceof Part.TextPart text) {
						result.add(text);
					}
				}
			}
			return result;
		}
	}

	private static Run run(Person root, AgentSession session) {
		CancellationToken cancellation = new CancellationToken();
		ToolContext context = new ToolContext(
			RunSecurity.contextFor(new RunPrincipal(root, RunEntryPoint.CHAT, false)),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellation), cancellation);
		Conversation conversation = new Conversation();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent(QUESTION))));
			ConversationLog log = env.logListener().log();
			log.startTurn().setUserParts(
				List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), QUESTION)));
			AgentRunner.RecordedRun run = AgentRunner.run(ALBERT, conversation, null, context, event -> {
				// Only the outcome matters here.
			}, null, log, session, null);
			return new Run(run.result().stopReason(), conversation, String.valueOf(run.response()), log);
		} finally {
			context.transaction().close();
			HistoryUtils.updateSessionRevision();
		}
	}

	private static List<String> toolNames(SubagentProgress progress) {
		return progress.steps().stream().map(SubagentProgress.Step::toolName).toList();
	}

	private static ToolCall call(String tool, String arguments) {
		return new ToolCall("call-" + UUID.randomUUID(), tool, arguments);
	}

	private static String json(String text) {
		return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}

	/**
	 * The suite, on the services a chat request passes through, and the web tools.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestDeepResearchSkill.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				WebToolsService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
