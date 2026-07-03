/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Security context for tool execution.
 *
 * <p>Combines three concerns:</p>
 * <ul>
 *   <li><b>User-level access</b> ({@link #hasToolPermission}) — can this user invoke this tool at all?
 *       Currently always returns {@code true}; will integrate with TopLogic's per-user security
 *       when ready.</li>
 *   <li><b>System execution mode</b> ({@link #executionMode}) — is the system in development,
 *       production, or maintenance mode? The agent loop uses this to gate STRUCTURAL tools.</li>
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
	boolean hasToolPermission(Tool tool);

	/**
	 * Whether the current user has permission to invoke a tool by name.
	 *
	 * <p>Used for tools whose metadata is not available (e.g., external/MCP tools).
	 * The default returns {@code true} — permissive for unknown tools. Override
	 * to integrate with a role-based permission system.</p>
	 *
	 * @param toolName the tool name
	 * @return {@code true} if the user has permission
	 */
	default boolean hasToolPermission(String toolName) {
		return true;
	}

	/**
	 * Whether the current user has read access to the given object.
	 *
	 * <p>Placeholder — always returns {@code true}. Override in the bridge module
	 * to integrate with TopLogic's {@code BoundChecker}/{@code AccessManager}.</p>
	 *
	 * @param ref the object to check (typically a TLObject in the bridge)
	 * @return {@code true} if the user may read the object
	 */
	default boolean hasReadAccess(Object ref) {
		return true;
	}

	/**
	 * Whether the current user has write access to the given object.
	 *
	 * <p>Placeholder — always returns {@code true}. Override in the bridge module
	 * to integrate with TopLogic's {@code BoundChecker}/{@code AccessManager}.</p>
	 *
	 * @param ref the object to check (typically a TLObject in the bridge)
	 * @return {@code true} if the user may write the object
	 */
	default boolean hasWriteAccess(Object ref) {
		return true;
	}

	/**
	 * The system's current execution mode.
	 *
	 * <p>The agent loop uses this to enforce the system mode gate: STRUCTURAL tools
	 * are denied in {@link ExecutionMode#PRODUCTION}.</p>
	 */
	ExecutionMode executionMode();

	/**
	 * Whether this tool call needs user approval before executing.
	 *
	 * <p>Only called after {@link #hasToolPermission} passes and the system mode gate allows
	 * the tool. The default implementation requires approval for all state-changing
	 * tools ({@link ToolMode#USER_SCOPED}, {@link ToolMode#DATA_WRITE}, and {@link ToolMode#STRUCTURAL}).</p>
	 *
	 * <p><b>Contract:</b> implementations must be side-effect-free and stable within a turn.
	 * The agent loop calls this method more than once for the same tool call — first to
	 * predict whether a call can join a concurrent batch, then again as the execution
	 * gate.</p>
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
			case DATA_WRITE -> new PermissionDecision.NeedsApproval(
				"Modifies shared application data.", toolCall);
			case STRUCTURAL -> new PermissionDecision.NeedsApproval(
				"Modifies application structure.", toolCall);
		};
	}

	/** Context with no restrictions — for tests and trusted internal use. */
	SecurityContext UNRESTRICTED = new SecurityContext() {

		@Override
		public boolean hasToolPermission(Tool tool) {
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
