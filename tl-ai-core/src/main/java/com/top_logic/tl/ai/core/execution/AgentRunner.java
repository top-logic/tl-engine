/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.List;

import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.citation.CitationValidator;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.memory.CompactionStrategies;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.ToolRegistry;

/**
 * Runs an agent through {@link AgentFactory} with persistent execution recording.
 *
 * <p>
 * This is the preferred entry point for running agents: it records the run through a
 * {@link RunRecording} over the caller's event listener, so every run leaves an {@link AgentExecution} row — created with
 * status RUNNING before the first model call and finalized with stop reason, token usage, error
 * message and the serialized per-run transcript on any exit, including exceptional ones. The
 * execution is recorded against the {@link AgentSession} captured at call time, so a completed
 * run can never be attributed to a different session than the one it started under.
 * </p>
 *
 * <p>
 * When no session is given (or the {@link AgentExecutionStore} service is inactive), the run
 * proceeds unrecorded — identical to calling {@link AgentFactory#runAgent} directly.
 * </p>
 */
public final class AgentRunner {

	/**
	 * Result of a recorded run.
	 *
	 * @param response
	 *        The final assistant response text.
	 * @param registry
	 *        The tool registry used during the run.
	 * @param result
	 *        The terminal {@link AgentResult} - why the run stopped, and what the provider said if
	 *        it failed. <code>null</code> only if the loop died before emitting a completion
	 *        event.
	 * @param execution
	 *        The persisted execution row, or <code>null</code> when recording was off.
	 */
	public record RecordedRun(String response, ToolRegistry registry, AgentResult result,
			AgentExecution execution) {}

	private AgentRunner() {
		// Static utility
	}

	/**
	 * Runs the named agent, recording it as an {@link AgentExecution} of the given session.
	 *
	 * @param agentName
	 *        The agent name as declared in the configuration.
	 * @param conversation
	 *        The conversation to operate on (ignored when {@code memory} is given).
	 * @param memory
	 *        Optional conversation memory for context management, or <code>null</code>.
	 * @param context
	 *        Execution context with security policy, approval callback, and transaction scope.
	 * @param listener
	 *        Receives progress events during the run.
	 * @param sessionRegistry
	 *        Optional session-scoped tool registry for skill persistence, or <code>null</code>.
	 * @param log
	 *        The conversation log recording this run; its new turns become the execution's
	 *        transcript. May be <code>null</code> (no transcript).
	 * @param session
	 *        The session to record the execution under; <code>null</code> disables recording.
	 * @param parentExecution
	 *        The delegating execution for sub-agent runs; <code>null</code> for top-level runs.
	 */
	public static RecordedRun run(String agentName, Conversation conversation,
			ConversationMemory memory, ToolContext context, AgentEventListener listener,
			ToolRegistry sessionRegistry, ConversationLog log, AgentSession session,
			AgentExecution parentExecution) {
		return run(agentName, conversation, memory, context, listener, sessionRegistry, log, session,
			parentExecution, AgentFactory.RunOverrides.NONE);
	}

	/**
	 * Runs the named agent with optional per-run model and MCP overrides.
	 */
	public static RecordedRun run(String agentName, Conversation conversation,
			ConversationMemory memory, ToolContext context, AgentEventListener listener,
			ToolRegistry sessionRegistry, ConversationLog log, AgentSession session,
			AgentExecution parentExecution, AgentFactory.RunOverrides overrides) {
		RunRecording recording = RunRecording.open(agentName, session, parentExecution,
			context.executionHandle(), log, listener);

		AgentFactory.RunResult runResult;
		try (ConversationRegistry registry = bindConversationRegistry(session, parentExecution)) {
			runResult = AgentFactory.getInstance().runAgent(agentName, conversation, memory,
				context, recording, sessionRegistry,
				overrides != null ? overrides : AgentFactory.RunOverrides.NONE);
		} catch (RuntimeException ex) {
			recording.fail(ex);
			throw ex;
		}

		String response = runResult.response();
		if (log != null) {
			response = CitationValidator.apply(log, response, context.readingList()).text();
		}
		recording.finish(response);
		// The recording's result carries the provider's own error message (it saw the ProviderError
		// event); the loop's own result is the fallback for an unrecorded run.
		return new RecordedRun(response, runResult.registry(),
			recording.result() != null ? recording.result() : runResult.result(),
			recording.execution());
	}

	/**
	 * Runs a pre-built {@link AgentLoop}, recording it as an {@link AgentExecution} of the given
	 * session through the very same transcript path as the agent-name
	 * {@link #run(String, Conversation, ConversationMemory, ToolContext, AgentEventListener, ToolRegistry, ConversationLog, AgentSession, AgentExecution)
	 * overload}.
	 *
	 * <p>
	 * This entry point exists for callers that cannot build their loop from an agent name via
	 * {@link AgentFactory} - notably asynchronous subagents, which run a specially built,
	 * depth-limited loop. They get identical recording through {@link RunRecording} without
	 * duplicating that wiring.
	 * </p>
	 *
	 * <p>
	 * The caller owns the {@code conversation} and {@code log} (including any user turn seeded
	 * before the run); the returned {@link RecordedRun} carries no response text or tool registry
	 * (both {@code null}) - the caller extracts what it needs from the conversation it passed in.
	 * On an exception the recording is finalized with the failure and the exception is rethrown.
	 * </p>
	 *
	 * @param loop
	 *        The pre-built loop to run.
	 * @param agentName
	 *        The agent name, recorded on the execution row.
	 * @param conversation
	 *        The conversation to operate on. It is compacted when it outgrows the loop's model,
	 *        with {@link CompactionStrategies#forDefaultModel()}.
	 * @param context
	 *        Execution context; its {@link ToolContext#executionHandle() execution handle} receives
	 *        the created execution, so the caller can read the child run id back from it.
	 * @param listener
	 *        Receives progress events during the run.
	 * @param log
	 *        The conversation log recording this run; its new turns become the execution's
	 *        transcript. May be <code>null</code> (no transcript).
	 * @param session
	 *        The session to record the execution under; <code>null</code> disables recording.
	 * @param parentExecution
	 *        The delegating execution for sub-agent runs; <code>null</code> for top-level runs.
	 */
	public static RecordedRun run(AgentLoop loop, String agentName, Conversation conversation,
			ToolContext context, AgentEventListener listener, ConversationLog log,
			AgentSession session, AgentExecution parentExecution) {
		// A pre-built loop never passed through AgentFactory, so this is the only place that knows
		// which agent the run serves. Published as plain data for the run's tools to read.
		context.executionHandle().setAgentName(agentName);

		RunRecording recording = RunRecording.open(agentName, session, parentExecution,
			context.executionHandle(), log, listener);

		AgentResult result;
		try {
			// A pre-built loop serves a subagent, which brings no session memory: it gets one over its
			// own conversation, so that it compacts like a chat run instead of overflowing.
			result = loop.run(conversation, CompactionStrategies.forDefaultModel(), context, recording);
		} catch (RuntimeException ex) {
			recording.fail(ex);
			throw ex;
		}

		if (log != null) {
			CitationValidator.apply(log, lastAssistantText(log), context.readingList());
		}
		recording.finish(null);
		return new RecordedRun(null, null, result, recording.execution());
	}

	/**
	 * Binds the conversation's {@link SubagentJobRegistry} for a top-level run that has none bound, so
	 * that {@code spawn_subagent} works wherever a run is recorded under a conversation - a TL-Script
	 * run, the eval, a scheduled task - and not only in the chat, which binds a registry of its own.
	 *
	 * <p>
	 * A run without a conversation gets none: a subagent's result is delivered to the conversation
	 * that started it, and such a run has nothing to deliver it to. A delegated run gets none either,
	 * because it would take its siblings' results from the lead.
	 * </p>
	 *
	 * <p>
	 * The registry is borrowed for the run: when the run ends and the registry has no work left -
	 * every subagent ended and its result delivered - it is forgotten, so that a run per conversation
	 * leaves nothing behind. Subagents still at work keep it.
	 * </p>
	 */
	private static ConversationRegistry bindConversationRegistry(AgentSession session,
			AgentExecution parentExecution) {
		SubagentJobRegistry bound = SubagentRegistryBinding.getRegistry();
		if (bound != null || session == null || parentExecution != null) {
			return new ConversationRegistry(SubagentRegistryBinding.bind(bound), null);
		}
		String conversationId = ExecutionRecorder.conversationIdOf(session);
		if (conversationId == null) {
			return new ConversationRegistry(SubagentRegistryBinding.bind(null), null);
		}
		SubagentRegistries.Lease lease = SubagentRegistries.borrow(SubagentRegistries.conversationKey(conversationId));
		return new ConversationRegistry(SubagentRegistryBinding.bind(lease.registry()), lease);
	}

	/** The registry a run binds, and the lease it borrowed it under, if any. */
	private record ConversationRegistry(SubagentRegistryBinding.Binding binding, SubagentRegistries.Lease lease)
			implements AutoCloseable {

		@Override
		public void close() {
			binding.close();
			if (lease != null) {
				lease.close();
			}
		}
	}

	private static String lastAssistantText(ConversationLog log) {
		Turn turn = log.currentTurn();
		if (turn == null) {
			List<Turn> turns = log.turns();
			turn = turns.isEmpty() ? null : turns.get(turns.size() - 1);
		}
		if (turn == null) {
			return "";
		}
		Part.TextPart last = null;
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.TextPart text) {
				last = text;
			}
		}
		return last == null ? "" : last.text();
	}
}
