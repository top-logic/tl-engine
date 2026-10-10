/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInboxBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.binding.BindingSnapshot;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Shared async-spawn logic extracted from {@link DelegationTool}.
 *
 * <p>Both {@link DelegationTool} (spawn by pre-configured target) and
 * {@link SpawnSubagentTool} (spawn by caller-supplied agent name) delegate to this class
 * for the background-thread machinery so the lifecycle is implemented exactly once.</p>
 */
public class SubagentSpawner {

	/** Utility class — no instances. */
	private SubagentSpawner() {
	}

	/**
	 * Spawns an asynchronous subagent and returns a handle {@link ToolResult} immediately.
	 *
	 * <p>Captures the caller thread's {@link BindingSnapshot} and {@link SubSessionContext},
	 * registers a {@link SubagentJob}, emits {@code SubagentStarted}, and submits the worker
	 * to the registry's executor. The worker re-establishes the bindings (minus the
	 * {@link SubagentRegistryBinding} — subagents are depth-1), runs the subagent loop —
	 * recorded as an {@code AgentExecution} with {@code parentExecution} set when the parent
	 * run is recorded — emits {@code SubagentCompleted/Cancelled/Failed}, and stores the
	 * result in the job.</p>
	 *
	 * <p>
	 * The spawn is fire-and-forget, so the child is an <b>unattended</b> run: it keeps working after
	 * the parent turn that started it has ended, with nobody to answer an approval prompt. Its context
	 * is therefore built with
	 * {@link com.top_logic.tl.ai.core.agent.ToolContext#forDetachedSubagent}, which bounds it by the
	 * policy's {@code <background-allowlist>}, and its loop is built from that bounded context so the
	 * child's own prompt reports what it can actually call.
	 * </p>
	 *
	 * <p>
	 * That same bounded context decides whether the spawn happens at all: a child that can reach no tool
	 * this run may call is refused here rather than started, with a tool error the model can relay. See
	 * {@link #detachedSpawnRefusal}.
	 * </p>
	 *
	 * @param targetAgentName
	 *        Name of the agent to build (passed to {@link DelegationTool.AgentLoopFactory#buildSubagentLoop}).
	 * @param task
	 *        The task description (mandatory, non-blank).
	 * @param contextStr
	 *        Optional extra context to prepend to the prompt (may be {@code null}).
	 * @param toolContext
	 *        Execution context from the parent agent turn.
	 * @param loopFactory
	 *        Factory used to construct the subagent's {@link AgentLoop}.
	 * @param registry
	 *        The active {@link SubagentJobRegistry} for this session.
	 * @return A handle {@link ToolResult} containing the subagent id, or the tool error refusing a
	 *         spawn that could not have done anything - see {@link #detachedSpawnRefusal}.
	 */
	public static ToolResult spawnAsync(String targetAgentName, String task, String contextStr,
			ToolContext toolContext, DelegationTool.AgentLoopFactory loopFactory,
			SubagentJobRegistry registry) {

		// Before anything is registered or a thread is taken: a child that can reach no tool this run
		// may call would burn a provider round trip and come back with nothing.
		String refusal = detachedSpawnRefusal(targetAgentName, childCapabilities(toolContext));
		if (refusal != null) {
			return ToolResult.of(refusal);
		}

		String userPrompt = buildUserPrompt(task, contextStr);

		// Capture caller-thread context so the background thread can re-establish it.
		BindingSnapshot bindings = BindingSnapshot.capture();
		ConversationLogListener logListener = ConversationLogListenerBinding.getListener();
		// Guard: ThreadContextManager may not be started in test environments.
		final SubSessionContext userSession = getSubSessionSafe();

		// When the parent run is recorded, record the subagent as its own execution with
		// parentExecution set - same as the synchronous delegation path - so async runs
		// appear in the execution history and the parent+children token total stays a KB
		// query. Recording is best-effort and must never prevent the spawn.
		final AgentExecution parentExecution = toolContext.executionHandle().get();
		// The parent execution row is written by the AgentExecutionStore's writer thread, and this
		// agent thread's view of the database need not include it, so resolving
		// parentExecution.getSession() here can fail with "created after access revision N"
		// (Gitea #405). The store resolves it on the thread that can see it; this used to advance
		// the whole sub-session's revision instead, which moved every other read of this thread
		// along with it. Best-effort: without a session the child run is simply not recorded.
		final AgentSession recordingSession =
			parentExecution == null || !AgentExecutionStore.Module.INSTANCE.isActive() ? null
				: AgentExecutionStore.getInstance().sessionOf(parentExecution);

		CancellationToken childToken = new CancellationToken();
		SubagentJob job = registry.register(targetAgentName, task, childToken);
		job.setParentToolCallId(toolContext.toolCallId());

		// Lifecycle: SubagentStarted to the parent's listener (for the UI panel).
		if (logListener != null) {
			logListener.recordSubagentStarted(job.subagentId(), targetAgentName, task, toolContext.toolCallId());
		}

		registry.spawn(job, j -> {
			ComputationEx2<SubagentResult, RuntimeException, RuntimeException> work = () -> {
				try (
					BindingSnapshot.Established established = bindings.establish();
					// Deliberately clear the registry the snapshot just carried over: a
					// subagent must not see the session's SubagentJobRegistry. Its own
					// turn-start injection (AgentLoop#injectSubagentResults) would steal
					// the parent's undelivered sibling results, and subagents are
					// structurally depth-1 anyway (buildSubagentLoop omits the
					// spawn/wait/delegation tools from the child's catalog).
					SubagentRegistryBinding.Binding noRegistry = SubagentRegistryBinding.bind(null);
					// Replaces the parent turn's store the snapshot just carried over: that turn ends
					// while this child works on, and a question or approval parked in its store after
					// that is shown nowhere and answered by nobody. The job's store lives as long as
					// the job and names the child as the one asking.
					UserInteractionStoreBinding.Binding ownStore =
						UserInteractionStoreBinding.bind(j.interactionStore());
					// What the lead sends it, read at the start of each of its turns.
					SubagentInboxBinding.Binding inbox = SubagentInboxBinding.bind(j.inbox())) {

					Instant start = Instant.now();

					// The child's context first, the loop second, and the loop from the child's own
					// security context - not the parent's. This spawn is fire-and-forget: the parent
					// turn ends while the child keeps working here, so the child is an unattended run
					// and forDetachedSubagent bounds it by the <background-allowlist>. Building the
					// loop from the parent's context instead would leave the child's prompt claiming
					// capabilities its own gate then refuses.
					ToolContext childContext =
						toolContext.forDetachedSubagent(new TransactionScope(), j.cancellationToken());
					reportBackgroundCeiling(targetAgentName, j.subagentId(), childContext);

					AgentLoop targetLoop = loopFactory.buildSubagentLoop(targetAgentName, childContext.security());
					Conversation conversation = new Conversation();
					conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
						List.of(new TextContent(userPrompt))));
					int before = conversation.messages().size();

					AgentEventListener childListener = subagentListener(logListener, j.subagentId());

					// Capture the child run id from the RunStarted event as a plain string. Reading
					// it back from the recorded AgentExecution wrapper would touch the KnowledgeBase
					// on this long-running worker thread, whose stable read revision predates the row
					// the store's committer writes ("created after access revision N"). The event
					// carries the id without any KB access, on both the success and failure paths.
					AgentEventListener capturingListener = event -> {
						if (event instanceof AgentEvent.RunStarted started) {
							// At once, not after the run: the transcript of a running child is
							// what the user wants to open while it works.
							j.setChildRunId(started.runId());
						}
						j.progress().onEvent(event);
						childListener.onEvent(event);
					};

					// This worker thread's bound ConversationLogListener targets the PARENT's log,
					// so the child needs its own log to be recorded separately. Seed it with the
					// task brief as the user turn; the loop's events populate the rest. AgentRunner
					// then records the transcript and the child AgentExecution (parentExecution set,
					// mirroring DelegationTool#executeSync) through the same general mechanism used
					// for every other run - no bespoke serialization here.
					ConversationLog childLog = new ConversationLog();
					childLog.startTurn().setUserParts(
						List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), userPrompt)));
					j.setLiveLog(childLog);

					try {
						AgentRunner.RecordedRun run = AgentRunner.run(targetLoop, targetAgentName,
							conversation, childContext, capturingListener, childLog, recordingSession,
							parentExecution);
						AgentResult result = run.result();

						if (j.cancellationToken().isCancelled()) {
							if (logListener != null) {
								logListener.recordSubagentCancelled(j.subagentId(), j.childRunId());
							}
							throw new java.util.concurrent.CancellationException("Subagent cancelled.");
						}

						String finalText = AgentFactory.extractResponseText(conversation, before);
						long durationMs = Duration.between(start, Instant.now()).toMillis();
						int toolCalls = countToolCalls(conversation, before);
						SubagentResult sr = new SubagentResult(finalText, result.totalUsage(), durationMs,
							toolCalls, result.stopReason());
						if (logListener != null) {
							logListener.recordSubagentCompleted(j.subagentId(), sr, j.childRunId());
						}
						return sr;
					} catch (java.util.concurrent.CancellationException ce) {
						throw ce;
					} catch (Exception e) {
						// AgentRunner already finalized the child execution with the failure; the run
						// id was taken from the RunStarted event already, so only the event is left.
						if (logListener != null) {
							logListener.recordSubagentFailed(j.subagentId(), e.getMessage(), j.childRunId());
						}
						throw e instanceof RuntimeException re ? re : new RuntimeException(e);
					}
				}
			};
			if (userSession != null) {
				return ThreadContextManager.inContext(userSession, work);
			}
			return ThreadContextManager.inSystemInteraction(SubagentSpawner.class, work);
		});

		return ToolResult.of("Subagent " + targetAgentName + " started (id=" + job.subagentId()
			+ "). It runs in the background; its result will arrive in a later turn or via "
			+ "wait_for_subagent.");
	}

	/**
	 * The capability view of the run the detached child <em>will be</em>, over the deployed agent graph.
	 *
	 * <p>
	 * Deliberately not the parent turn's view. A detached subagent is an unattended run, so
	 * {@link ToolContext#forDetachedSubagent} bounds it by the policy's {@code <background-allowlist>},
	 * and an agent the parent turn reaches in full can be out of reach once that applies. Judging the
	 * spawn by the parent's context would let through exactly the useless child run
	 * {@link #detachedSpawnRefusal} exists to prevent.
	 * </p>
	 */
	private static AgentCapabilities childCapabilities(ToolContext toolContext) {
		return AgentCapabilities.forRun(toolContext.security().withBackgroundCeiling());
	}

	/**
	 * The tool error refusing to spawn the named agent, or <code>null</code> if it is worth
	 * spawning.
	 *
	 * <p>
	 * The spawn counterpart of the delegate filter: {@code list_agents} and the prompt's delegate
	 * catalog already leave out an agent that can reach no tool the run may call
	 * ({@link AgentCapabilities#unusableReason(com.top_logic.tl.ai.core.agent.AgentDefinition)}),
	 * and a spawn the model can still make would contradict them. What it saves is a child run, a
	 * provider round trip and a result that says nothing.
	 * </p>
	 *
	 * <p>
	 * An <b>ordinary tool error</b> rather than an exception, because the model has to be able to
	 * relay it: the answer is a grant, and only the user can go and ask for one. It never suggests
	 * the agent does not exist - it does, and saying otherwise would send the model looking for a
	 * different name for the same work. An agent that really is unknown is not refused here at all:
	 * that is not a denial, and the spawn fails where it always did.
	 * </p>
	 *
	 * <p>
	 * <b>Which grant, though, depends on what refused</b> (#517). The capability text below is
	 * composed only where the capability filter is the cause; an agent {@code <agent-access>}
	 * withholds gets the policy's own sentence and no capability list, because listing them would
	 * name exactly the grants that do not help - see
	 * {@link com.top_logic.tl.ai.core.agent.AgentCapabilities.Unusable} and
	 * {@link #withheldAgentRefusal}.
	 * </p>
	 *
	 * @param targetAgentName
	 *        Name of the agent the model asked for.
	 * @param childCapabilities
	 *        The capability view of the run the child will be - {@link #childCapabilities}, not the
	 *        parent turn's.
	 */
	public static String detachedSpawnRefusal(String targetAgentName,
			AgentCapabilities childCapabilities) {
		AgentDefinition target = childCapabilities.graph().agent(targetAgentName);
		AgentCapabilities.Unusable unusable = childCapabilities.unusableReason(target);
		if (unusable == null) {
			return null;
		}
		if (!unusable.aboutCapabilities()) {
			return withheldAgentRefusal(targetAgentName, unusable);
		}

		Set<String> required = childCapabilities.unreachableCapabilities(target);
		Set<String> unattended = childCapabilities.lostToBackgroundCeiling(required);
		Set<String> notHeld = new TreeSet<>(required);
		notHeld.removeAll(unattended);

		StringBuilder refusal = new StringBuilder();
		refusal.append("ERROR: Subagent '").append(targetAgentName).append("' was not started: not one"
			+ " of the tools it can reach is available to this run, so it could only have come back"
			+ " empty-handed.\n\n");
		if (!notHeld.isEmpty()) {
			refusal.append("This user does not hold the capabilities its tools require:\n");
			appendCapabilities(refusal, notHeld);
		}
		if (!unattended.isEmpty()) {
			// The subtle half: the user does hold these, and telling them otherwise would send them to
			// their administrator for a role grant they already have.
			refusal.append("A spawned subagent runs unattended - nobody is present to approve a tool"
				+ " call - so it is bounded by the deployment's background allowlist. It cannot reach"
				+ " the capabilities its tools require, even though this user holds them:\n");
			appendCapabilities(refusal, unattended);
		}
		refusal.append("Say which capability is missing and continue with what you can do; spawning it"
			+ " again will not help.");
		return refusal.toString();
	}

	/**
	 * The refusal for a spawn the capability filter did not refuse: the agent itself is out of reach.
	 *
	 * <p>
	 * Names no capability, on purpose (#517). {@code <agent-access>} withholds the agent, or the agent
	 * is infrastructure, or what it runs on is denied to this account - and every capability grant in
	 * the deployment leaves all three exactly where they are. The message the policy wrote already
	 * names the setting an administrator would change, so it is relayed rather than restated, and the
	 * closing line sends the user after that setting instead of after a capability.
	 * </p>
	 */
	private static String withheldAgentRefusal(String targetAgentName, AgentCapabilities.Unusable unusable) {
		StringBuilder refusal = new StringBuilder();
		refusal.append("ERROR: Subagent '").append(targetAgentName).append("' was not started. ")
			.append(unusable.message()).append("\n\n");
		if (unusable.cause() == AgentCapabilities.Unusable.Cause.AGENT_WITHHELD) {
			refusal.append("Only a grant of this agent to one of this account's roles lets the spawn"
				+ " through, and no capability grant does. ");
		}
		refusal.append("Say that and continue with what you can do; spawning it again will not help.");
		return refusal.toString();
	}

	/** One bullet per capability, each saying what it covers where the deployment describes it. */
	private static void appendCapabilities(StringBuilder refusal, Collection<String> capabilities) {
		for (String capability : capabilities) {
			refusal.append("- ").append(AgentCapabilities.describeCapability(capability)).append("\n");
		}
		refusal.append("\n");
	}

	/**
	 * Records what being unattended costs this subagent, so the loss is legible rather than silent.
	 *
	 * <p>
	 * Silent capability loss is the failure mode this classification must not have: an operator asked
	 * why a subagent "cannot do what the same agent does in the chat" has to find the answer here. Says
	 * nothing when nothing is withheld, which is every application that configures no
	 * {@code <tool-access>} policy.
	 * </p>
	 *
	 * @see com.top_logic.tl.ai.core.agent.ToolContext#forDetachedSubagent
	 */
	private static void reportBackgroundCeiling(String targetAgentName, String subagentId,
			ToolContext childContext) {
		Set<String> withheld = childContext.security().capabilitiesWithheldInBackground();
		if (withheld.isEmpty()) {
			return;
		}
		Logger.info("Subagent '" + targetAgentName + "' (id=" + subagentId + ") runs detached, so it"
			+ " has no user who could approve a tool call and the background allowlist applies -"
			+ " capabilities withheld from it that the acting user otherwise holds: " + withheld,
			SubagentSpawner.class);
	}

	/** Returns the current sub-session or {@code null} when the module is not started (e.g. in tests). */
	static SubSessionContext getSubSessionSafe() {
		try {
			return ThreadContextManager.getSubSession();
		} catch (IllegalStateException ex) {
			return null;
		}
	}

	/** Builds the user prompt string from task and optional context. */
	static String buildUserPrompt(String task, String contextStr) {
		StringBuilder userPrompt = new StringBuilder();
		if (contextStr != null && !contextStr.isBlank()) {
			userPrompt.append("## Context\n\n").append(contextStr).append("\n\n");
		}
		userPrompt.append("## Task\n\n").append(task);
		return userPrompt.toString();
	}

	private static AgentEventListener subagentListener(ConversationLogListener logListener, String subagentId) {
		if (logListener == null) {
			return event -> { /* no-op */ };
		}
		// Wrap child events as DelegatedEvent carrying the subagentId for UI routing.
		return logListener.subagentListener(subagentId);
	}

	private static int countToolCalls(Conversation conversation, int fromIndex) {
		int count = 0;
		List<com.top_logic.tl.ai.core.message.Message> messages = conversation.messages();
		for (int i = Math.max(0, fromIndex); i < messages.size(); i++) {
			if (messages.get(i) instanceof AssistantMessage assistant) {
				for (ContentPart part : assistant.content()) {
					if (part instanceof ToolCall) {
						count++;
					}
				}
			}
		}
		return count;
	}
}
