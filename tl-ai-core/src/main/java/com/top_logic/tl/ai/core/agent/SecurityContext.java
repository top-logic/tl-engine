/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.tool.Tool;
import com.top_logic.tl.ai.core.tool.ToolMode;

/**
 * Security context for tool execution.
 *
 * <p>Combines three concerns:</p>
 * <ul>
 *   <li><b>User-level access</b> ({@link #canUseTool}) — can this user invoke this tool at all?
 *       Currently always returns {@code true}; will integrate with TopLogic's per-user security
 *       when ready.</li>
 *   <li><b>System execution mode</b> ({@link #executionMode}) — is the system in development,
 *       production, or maintenance mode? The agent loop uses this to gate GLOBAL tools.</li>
 *   <li><b>Approval policy</b> ({@link #checkApproval}) — does this tool call need user
 *       confirmation before executing?</li>
 * </ul>
 *
 * @see ToolContext
 * @see ExecutionMode
 * @see PermissionDecision
 */
public interface SecurityContext {

	/**
	 * Whether the current user is allowed to invoke the given tool.
	 *
	 * <p>This is a coarse check — it answers "can this user use this tool at all?"
	 * not "can this user use this tool with these specific arguments?" The latter
	 * is handled inside the tool itself.</p>
	 *
	 * @param tool the tool to check
	 * @return {@code true} if the user may invoke the tool
	 */
	boolean canUseTool(Tool tool);

	/**
	 * The system's current execution mode.
	 *
	 * <p>The agent loop uses this to enforce the system mode gate: GLOBAL tools
	 * are denied in {@link ExecutionMode#PRODUCTION}.</p>
	 */
	ExecutionMode executionMode();

	/**
	 * Whether this tool call needs user approval before executing.
	 *
	 * <p>Only called after {@link #canUseTool} passes and the system mode gate allows
	 * the tool. The default implementation requires approval for all state-changing
	 * tools ({@link ToolMode#USER_SCOPED} and {@link ToolMode#GLOBAL}).</p>
	 *
	 * @param tool     the tool to check
	 * @param toolCall the specific invocation
	 * @return the approval decision
	 */
	default PermissionDecision checkApproval(Tool tool, ToolCall toolCall) {
		return switch (tool.mode()) {
			case READ -> new PermissionDecision.Allowed();
			case USER_SCOPED -> new PermissionDecision.NeedsApproval(
				"Modifies your personal settings.", toolCall);
			case GLOBAL -> new PermissionDecision.NeedsApproval(
				"Modifies shared application state.", toolCall);
		};
	}

	/** Context with no restrictions — for tests and trusted internal use. */
	SecurityContext UNRESTRICTED = new SecurityContext() {

		@Override
		public boolean canUseTool(Tool tool) {
			return true;
		}

		@Override
		public ExecutionMode executionMode() {
			return ExecutionMode.DEVELOPMENT;
		}

		@Override
		public PermissionDecision checkApproval(Tool tool, ToolCall toolCall) {
			return new PermissionDecision.Allowed();
		}
	};
}
