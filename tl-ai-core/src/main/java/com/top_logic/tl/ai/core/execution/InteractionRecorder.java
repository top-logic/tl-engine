/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
	 * Records a pending interaction, or returns <code>null</code> when recording is off.
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
	public static InteractionRequest begin(AgentExecution execution, InteractionType type,
			String requestId, String header, String prompt, String payload) {
		if (execution == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return null;
		}
		try {
			// The session owner is the person expected to respond (for chat sessions the
			// chatting user); sessions without an owner leave the request open to any user.
			return AgentExecutionStore.getInstance().createInteraction(execution, type, requestId,
				header, prompt, payload, execution.getSession().getOwner());
		} catch (RuntimeException ex) {
			Logger.warn("Failed to record interaction request " + requestId + ": " + ex.getMessage(),
				ex, InteractionRecorder.class);
			return null;
		}
	}

	/**
	 * Resolves a previously recorded interaction. No-op for <code>null</code> requests.
	 *
	 * @param state
	 *        Terminal state (RESOLVED, TIMED_OUT or CANCELLED).
	 * @param resolution
	 *        The user's decision or answer; may be <code>null</code>.
	 */
	public static void resolve(InteractionRequest request, InteractionState state, String resolution) {
		if (request == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return;
		}
		try {
			AgentExecutionStore.getInstance().resolveInteraction(request, state, resolution);
		} catch (RuntimeException ex) {
			Logger.warn("Failed to resolve interaction request " + request.getRequestId() + ": "
				+ ex.getMessage(), ex, InteractionRecorder.class);
		}
	}
}
