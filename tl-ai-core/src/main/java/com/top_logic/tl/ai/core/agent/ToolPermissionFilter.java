/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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

	/**
	 * Whether the tool is available at all.
	 *
	 * <p>
	 * {@link ToolMode#STRUCTURAL} is the only mode this gate refuses, and it refuses it because the
	 * tool would change <em>this</em> application's model on a live installation. A
	 * {@link ToolMode#EXTERNAL} tool passes: what it changes belongs to another system, which this
	 * application's production state says nothing about. The control on an external call is the
	 * approval prompt - see {@link SecurityContext#checkApproval} - not a mode a deployment is in.
	 * </p>
	 */
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
			// Prefer the specific reason: the model can act on "you lack the model.structure
			// capability" and an operator can trace it, where a bare "denied" leaves both guessing.
			String specific = _security.toolDenialReason(tool);
			return specific != null
				? specific
				: "Permission denied: you do not have access to tool '" + tool.name() + "'.";
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
