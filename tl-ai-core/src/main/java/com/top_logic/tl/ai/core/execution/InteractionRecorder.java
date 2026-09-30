/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.InteractionState;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.InteractionType;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;

/**
 * Best-effort persistence of human-in-the-loop requests as {@link InteractionRequest} rows.
 *
 * <p>
 * Used by the agent loop (tool approvals) and the ask-user tool (questions/choices). A pending
 * row is created when the interaction is presented and resolved with the user's decision or
 * answer. Rows are only written when the current run is recorded (an {@link AgentExecution}
 * exists) and the {@link AgentExecutionStore} is active; persistence failures are logged and
 * never disturb the interaction itself.
 * </p>
 */
public final class InteractionRecorder {

	private InteractionRecorder() {
		// Static utility
	}

	/**
	 * Records a pending interaction of the run behind {@code handle}, after writing down what the
	 * run has recorded so far (#598).
	 *
	 * <p>
	 * The run is about to park its thread on a person, and a restart during that wait must not take
	 * the turn with it - see {@link ExecutionHandle#checkpoint()}. The checkpoint is queued on the
	 * store's writer before the interaction row, so it is written by the time the wait begins.
	 * </p>
	 *
	 * @see #begin(AgentExecution, InteractionType, String, String, String, String)
	 */
	public static String begin(ExecutionHandle handle, InteractionType type,
			String requestId, String header, String prompt, String payload) {
		if (handle == null) {
			return null;
		}
		handle.checkpoint();
		return begin(handle.get(), type, requestId, header, prompt, payload);
	}

	/**
	 * Records a pending interaction and returns its {@code requestId} handle, or <code>null</code>
	 * when recording is off (or the row could not be persisted).
	 *
	 * <p>
	 * The returned value is the plain {@code requestId} string, never the persistent
	 * {@link InteractionRequest} wrapper: the caller runs on the agent/tool thread whose KB view
	 * predates the just-recorded row, so dereferencing the wrapper there would throw "created
	 * after access revision N". Pass the returned handle back to
	 * {@link #resolve(String, InteractionState, String)} to close the interaction.
	 * </p>
	 *
	 * @param execution
	 *        The recorded execution waiting for the response; <code>null</code> disables
	 *        recording.
	 * @param type
	 *        The interaction kind.
	 * @param requestId
	 *        Correlation id used by the presenting surface.
	 * @param header
	 *        Short label, e.g. the tool name for approvals.
	 * @param prompt
	 *        The question or approval reason presented to the user.
	 * @param payload
	 *        Structured request data — for approvals the tool call's actual argument JSON.
	 */
	public static String begin(AgentExecution execution, InteractionType type,
			String requestId, String header, String prompt, String payload) {
		if (execution == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return null;
		}
		try {
			// The row (and the responder resolved from execution.getSession()) is created on the
			// store's writer thread, which sees KB HEAD. We deliberately do not return the
			// persistent wrapper: dereferencing it on this agent thread — whose KB view predates
			// the just-recorded execution — would fail. The stable requestId string is enough to
			// resolve the row later.
			AgentExecutionStore.getInstance().createInteraction(execution, type, requestId,
				header, prompt, payload);
			return requestId;
		} catch (RuntimeException ex) {
			Logger.warn("Failed to record interaction request " + requestId + ": " + ex.getMessage(),
				ex, InteractionRecorder.class);
			return null;
		}
	}

	/**
	 * Resolves a previously recorded interaction, located by the {@code requestId} handle returned
	 * from {@link #begin}. No-op for a <code>null</code> handle.
	 *
	 * <p>
	 * Best-effort: the row is looked up and updated on the store's writer thread and any failure is
	 * logged, never rethrown, so a persistence hiccup cannot abort the interaction/tool. The handle
	 * is a plain string, so even the failure logging is revision-safe (it never dereferences a
	 * persistent wrapper on the stale agent thread).
	 * </p>
	 *
	 * @param requestId
	 *        The handle returned by {@link #begin}; <code>null</code> when recording was off.
	 * @param state
	 *        Terminal state (RESOLVED, TIMED_OUT or CANCELLED).
	 * @param resolution
	 *        The user's decision or answer; may be <code>null</code>.
	 */
	public static void resolve(String requestId, InteractionState state, String resolution) {
		if (requestId == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return;
		}
		try {
			AgentExecutionStore.getInstance().resolveInteraction(requestId, state, resolution);
		} catch (CancelledWriteException ex) {
			// Expected: cancelling a run unparks the thread waiting on the question or approval,
			// and that thread resolves the request here while still carrying the interrupt. The
			// write is queued and still commits; only the wait for it is given up.
			Logger.info("Run cancelled while interaction request " + requestId
				+ " was open; its resolution is written without waiting for it.",
				InteractionRecorder.class);
		} catch (RuntimeException ex) {
			Logger.warn("Failed to resolve interaction request " + requestId + ": "
				+ ex.getMessage(), ex, InteractionRecorder.class);
		}
	}
}
