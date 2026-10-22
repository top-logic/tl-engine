/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Data sent to the user when a tool call needs approval.
 *
 * <p>Contains everything the approval UI needs to display: tool name, arguments,
 * risk level (mode), a human-readable reason, and a timeout.</p>
 *
 * @param approvalRequestId unique ID for this approval request (used to correlate SSE events with
 *                          the UI response)
 * @param toolName          name of the tool requesting approval
 * @param toolCallId        unique ID of the tool call from the LLM
 * @param arguments         deserialized arguments the tool would receive
 * @param mode              the tool's {@link ToolMode} (indicates scope of change)
 * @param displayReason     human-readable reason shown in the approval dialog
 * @param timeout           maximum time to wait for user response
 *                          (see {@link InteractionTimeoutService#approvalTimeout()})
 *
 * @see ApprovalCallback
 */
public record ApprovalRequest(
		String approvalRequestId,
		String toolName,
		String toolCallId,
		Map<String, Object> arguments,
		ToolMode mode,
		String displayReason,
		Duration timeout) {

	/** */
	public ApprovalRequest {
		Objects.requireNonNull(approvalRequestId);
		Objects.requireNonNull(toolName);
		Objects.requireNonNull(toolCallId);
		Objects.requireNonNull(arguments);
		Objects.requireNonNull(mode);
		Objects.requireNonNull(displayReason);
		Objects.requireNonNull(timeout);
	}
}
