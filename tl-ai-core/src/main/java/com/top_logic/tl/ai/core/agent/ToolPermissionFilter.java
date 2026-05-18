/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.function.Predicate;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Determines whether a tool is available to the current user in the current execution mode.
 *
 * <p>Used both for filtering tool schemas sent to the LLM (so it never sees
 * tools it cannot use) and for gating execution (defense in depth).</p>
 *
 * @see AgentLoop
 * @see SecurityContext
 */
public class ToolPermissionFilter implements Predicate<Tool> {

	private final SecurityContext _security;

	/**
	 * Creates a filter for the given security context.
	 *
	 * @param security the security context to evaluate against
	 */
	public ToolPermissionFilter(SecurityContext security) {
		_security = security;
	}

	@Override
	public boolean test(Tool tool) {
		if (!_security.hasToolPermission(tool)) {
			return false;
		}
		if (tool.mode() == ToolMode.STRUCTURAL
				&& _security.executionMode() == ExecutionMode.PRODUCTION) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a human-readable denial reason, or {@code null} if the tool is allowed.
	 *
	 * <p>Used by the execution gate to produce informative error messages fed back to the LLM.</p>
	 *
	 * @param tool the tool to check
	 * @return denial reason string, or {@code null} if the tool passes all gates
	 */
	public String denialReason(Tool tool) {
		if (!_security.hasToolPermission(tool)) {
			return "Permission denied: you do not have access to tool '" + tool.name() + "'.";
		}
		if (tool.mode() == ToolMode.STRUCTURAL
				&& _security.executionMode() == ExecutionMode.PRODUCTION) {
			return "Tool '" + tool.name() + "' cannot execute: it modifies shared "
				+ "application state, which requires maintenance or development mode. "
				+ "The system is currently in production mode.";
		}
		return null;
	}
}
