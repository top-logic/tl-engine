/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.event.CompositeAgentEventListener;
import com.top_logic.tl.ai.core.citation.CitationValidator;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.memory.CompactionStrategies;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolRegistry;

/**
 * Runs an agent through {@link AgentFactory} with persistent execution recording.
 *
 * <p>
 * This is the preferred entry point for running agents: it wraps the caller's event listener in
 * an {@link ExecutionRecorder}, so every run leaves an {@link AgentExecution} row — created with
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
		// Ensure the run's turns land in the log that becomes the transcript. Callers that wire a
		// ConversationLogListener over this log themselves (e.g. the chat UI) already record into
		// it; entry points that don't (e.g. the TL-Script functions, which pass a no-op listener)
		// would otherwise serialize an empty log. This closes that gap for every caller.
		AgentEventListener downstream = withTranscriptRecording(listener, log);

		// Entry points may start the run's turn before invoking the loop (chat records the
		// user message first); an open current turn belongs to this run, not to history.
		int turnsBefore = log != null
			? log.turns().size() - (log.currentTurn() != null ? 1 : 0)
			: 0;

		AgentEventListener effectiveListener = downstream;
		ExecutionRecorder recorder = null;
		if (session != null) {
			recorder = new ExecutionRecorder(session, agentName, parentExecution,
				context.executionHandle(), downstream, checkpointOf(log, turnsBefore));
			effectiveListener = recorder;
		} else {
			Logger.info("Agent run of '" + agentName + "' not recorded: no session given.",
				AgentRunner.class);
		}

		// The numbering belongs to the conversation, not to this run: the model reads its own earlier
		// answers back, markers and all, so a run that restarted at 1 handed out numbers that already
		// meant another source - and the validator then deleted the marker (#495).
		context.citationMarkers().resumeFrom(log);
		AgentFactory.RunResult runResult;
		try {
			runResult = AgentFactory.getInstance().runAgent(agentName, conversation, memory,
				context, effectiveListener, sessionRegistry,
				overrides != null ? overrides : AgentFactory.RunOverrides.NONE);
		} catch (RuntimeException ex) {
			if (recorder != null) {
				recorder.complete(transcriptOf(log, turnsBefore), ex);
			}
			throw ex;
		}

		String response = runResult.response();
		if (log != null) {
			response = CitationValidator.apply(log, response).text();
		}
		if (recorder != null) {
			recorder.complete(transcriptOf(log, turnsBefore), response, null);
		}
		// The recorder's result carries the provider's own error message (it saw the ProviderError
		// event); the loop's own result is the fallback for an unrecorded run.
		return new RecordedRun(response, runResult.registry(),
			recorder != null && recorder.result() != null ? recorder.result() : runResult.result(),
			recorder != null ? recorder.execution() : null);
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
	 * depth-limited loop. They get identical transcript recording ({@link #withTranscriptRecording}
	 * over {@code log} plus an {@link ExecutionRecorder}) without duplicating that wiring.
	 * </p>
	 *
	 * <p>
	 * The caller owns the {@code conversation} and {@code log} (including any user turn seeded
	 * before the run); the returned {@link RecordedRun} carries no response text or tool registry
	 * (both {@code null}) - the caller extracts what it needs from the conversation it passed in.
	 * On an exception the recorder is finalized with the failure and the exception is rethrown.
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

		AgentEventListener downstream = withTranscriptRecording(listener, log);

		// Callers may seed the run's first turn (e.g. the subagent's user message) before the loop;
		// an open current turn belongs to this run, not to history.
		int turnsBefore = log != null
			? log.turns().size() - (log.currentTurn() != null ? 1 : 0)
			: 0;

		AgentEventListener effectiveListener = downstream;
		ExecutionRecorder recorder = null;
		if (session != null) {
			recorder = new ExecutionRecorder(session, agentName, parentExecution,
				context.executionHandle(), downstream, checkpointOf(log, turnsBefore));
			effectiveListener = recorder;
		} else {
			Logger.info("Agent run of '" + agentName + "' not recorded: no session given.",
				AgentRunner.class);
		}

		// The numbering belongs to the conversation, not to this run: the model reads its own earlier
		// answers back, markers and all, so a run that restarted at 1 handed out numbers that already
		// meant another source - and the validator then deleted the marker (#495).
		context.citationMarkers().resumeFrom(log);
		AgentResult result;
		try {
			// A pre-built loop serves a subagent, which brings no session memory: it gets one over its
			// own conversation, so that it compacts like a chat run instead of overflowing.
			result = loop.run(conversation, CompactionStrategies.forDefaultModel(), context, effectiveListener);
		} catch (RuntimeException ex) {
			if (recorder != null) {
				recorder.complete(transcriptOf(log, turnsBefore), ex);
			}
			throw ex;
		}

		if (log != null) {
			CitationValidator.apply(log, lastAssistantText(log));
		}
		if (recorder != null) {
			recorder.complete(transcriptOf(log, turnsBefore), null);
		}
		return new RecordedRun(null, null, result,
			recorder != null ? recorder.execution() : null);
	}

	/**
	 * Returns a listener that additionally records the run's events into {@code log} (the transcript
	 * source), unless {@code listener} already dispatches to a recorder for that log.
	 *
	 * <p>
	 * The recorder used is the {@link ConversationLogListener} bound to the current thread when it
	 * targets {@code log} (so tool-driven parts and loop events share one listener); otherwise a
	 * fresh {@link ConversationLogListener} over {@code log} is created. When {@code log} is
	 * <code>null</code> no transcript is kept and {@code listener} is returned unchanged.
	 * </p>
	 */
	private static AgentEventListener withTranscriptRecording(AgentEventListener listener,
			ConversationLog log) {
		if (log == null) {
			return listener;
		}
		ConversationLogListener bound = ConversationLogListener.current();
		ConversationLogListener transcriptRecorder =
			(bound != null && bound.log() == log) ? bound : new ConversationLogListener(log);
		if (listener != null && listener.dispatchesTo(transcriptRecorder)) {
			return listener;
		}
		return listener == null
			? transcriptRecorder
			: new CompositeAgentEventListener(transcriptRecorder, listener);
	}

	/**
	 * The run's transcript while it is still going, for the recorder to checkpoint (#598), or
	 * <code>null</code> when the run keeps no log.
	 *
	 * <p>
	 * The same slice of the log that {@link #transcriptOf(ConversationLog, int)} writes at the end,
	 * open turn included - which is the point: the open turn is the one a restart would otherwise
	 * lose.
	 * </p>
	 */
	private static Supplier<String> checkpointOf(ConversationLog log, int turnsBefore) {
		return log == null ? null : () -> transcriptOf(log, turnsBefore);
	}

	/**
	 * Serializes the turns the current run appended to the log, or <code>null</code> if the log
	 * is unavailable or serialization fails.
	 */
	private static String transcriptOf(ConversationLog log, int turnsBefore) {
		if (log == null) {
			return null;
		}
		try {
			List<Turn> turns = log.turns();
			List<Turn> runTurns = turns.subList(Math.min(turnsBefore, turns.size()), turns.size());
			return ConversationLogSerializer.toJson(runTurns);
		} catch (RuntimeException ex) {
			Logger.warn("Failed to serialize run transcript: " + ex.getMessage(), ex, AgentRunner.class);
			return null;
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
