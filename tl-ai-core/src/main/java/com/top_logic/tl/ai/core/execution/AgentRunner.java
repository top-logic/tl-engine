/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.session.ConversationLog;
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
	 *        The terminal {@link AgentResult}, or <code>null</code> if the loop died before
	 *        emitting a completion event.
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
		AgentEventListener effectiveListener = listener;
		ExecutionRecorder recorder = null;
		if (session != null) {
			recorder = new ExecutionRecorder(session, agentName, parentExecution,
				context.executionHandle(), listener);
			effectiveListener = recorder;
		}

		// Entry points may start the run's turn before invoking the loop (chat records the
		// user message first); an open current turn belongs to this run, not to history.
		int turnsBefore = log != null
			? log.turns().size() - (log.currentTurn() != null ? 1 : 0)
			: 0;

		AgentFactory.RunResult runResult;
		try {
			runResult = AgentFactory.getInstance().runAgent(agentName, conversation, memory,
				context, effectiveListener, sessionRegistry);
		} catch (RuntimeException ex) {
			if (recorder != null) {
				recorder.complete(transcriptOf(log, turnsBefore), ex);
			}
			throw ex;
		}

		if (recorder != null) {
			recorder.complete(transcriptOf(log, turnsBefore), null);
		}
		return new RecordedRun(runResult.response(), runResult.registry(),
			recorder != null ? recorder.result() : null,
			recorder != null ? recorder.execution() : null);
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
}
