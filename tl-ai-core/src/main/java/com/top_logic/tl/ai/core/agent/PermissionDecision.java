/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.message.ToolCall;

/**
 * Result of a permission/approval check on a tool call.
 *
 * <p>Three outcomes:</p>
 * <ul>
 *   <li>{@link Allowed} — tool executes immediately</li>
 *   <li>{@link Denied} — tool is blocked; reason is fed to the LLM as a tool result</li>
 *   <li>{@link NeedsApproval} — agent loop pauses and asks the user via
 *       {@link ApprovalCallback}</li>
 * </ul>
 *
 * @see SecurityContext#checkApproval(com.top_logic.tl.ai.core.tool.Tool, ToolCall)
 */
public sealed interface PermissionDecision {

	/** Tool may execute without approval. */
	record Allowed() implements PermissionDecision {
	}

	/**
	 * Tool is denied by the system.
	 *
	 * @param reason message fed back to the LLM as the tool result
	 */
	record Denied(String reason) implements PermissionDecision {
	}

	/**
	 * Tool needs user approval before execution.
	 *
	 * @param displayReason human-readable reason shown in the approval dialog
	 * @param toolCall      the tool call awaiting approval
	 */
	record NeedsApproval(String displayReason, ToolCall toolCall) implements PermissionDecision {
	}
}
