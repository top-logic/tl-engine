/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.security.DeclaredTypeAccess;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Determines whether a tool is available to the current user in the current execution mode.
 *
 * <p>Used both for filtering tool schemas sent to the LLM (so it never sees
 * tools it cannot use) and for gating execution (defense in depth).</p>
 *
 * <p>
 * Three independent questions, and a tool has to pass all three: may this user use this tool at all
 * ({@link SecurityContext#hasToolPermission(Tool)}), may <em>anyone</em> use it in the current
 * {@link ExecutionMode}, and - for a tool that declares the model types it works on - may this user
 * read any of them ({@link DeclaredTypeAccess}). Keeping them here rather than in three filters is
 * what makes the schema the model is sent and the gate that stops a call agree by construction.
 * </p>
 *
 * @see AgentLoop
 * @see SecurityContext
 * @see ToolMethod#types()
 */
public class ToolPermissionFilter implements Predicate<Tool> {

	private final SecurityContext _security;

	private final DeclaredTypeAccess _declaredTypes;

	/**
	 * Creates a filter for the given security context.
	 *
	 * @param security the security context to evaluate against
	 */
	public ToolPermissionFilter(SecurityContext security) {
		this(security, new DeclaredTypeAccess(security.modelSecurity()));
	}

	/**
	 * Creates a filter that asks the given declared-type check instead of the one the security
	 * context's model rights would produce.
	 *
	 * <p>
	 * Package-visible: the seam a test uses to pin that this filter consults the check in both of its
	 * halves without booting an application model. Production always goes through
	 * {@link #ToolPermissionFilter(SecurityContext)}.
	 * </p>
	 */
	ToolPermissionFilter(SecurityContext security, DeclaredTypeAccess declaredTypes) {
		_security = security;
		_declaredTypes = declaredTypes;
	}

	/**
	 * The tools a run is offered: the ones its principal may use that can also act in the run at all.
	 *
	 * <p>
	 * The second half is not a permission - see {@link Tool#unavailableReason(ToolExecutionContext)}:
	 * the {@code present} tool on a page that has no place to show anything is withheld from nobody,
	 * it simply has nothing to do there (#649). It is asked here, beside the permission, so that the
	 * schemas the model is sent and the list a call for an unknown name is answered with stay the same
	 * set.
	 * </p>
	 *
	 * @param context
	 *        The run.
	 * @return The test whether the run is offered a given tool.
	 */
	public static Predicate<Tool> offeredIn(ToolContext context) {
		ToolPermissionFilter permitted = new ToolPermissionFilter(context.security());
		return tool -> permitted.test(tool) && canAct(tool, context);
	}

	/**
	 * Whether the tool can act in the run; a tool that fails to say is not offered rather than failing
	 * the request it would have been offered in.
	 */
	private static boolean canAct(Tool tool, ToolContext context) {
		try {
			return tool.unavailableReason(context) == null;
		} catch (RuntimeException ex) {
			Logger.warn("Tool '" + tool.name() + "' failed to say whether it can act, so it is not offered.", ex,
				ToolPermissionFilter.class);
			return false;
		}
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
		// The quiet form: a catalog is enumerated once per request, and a tool nobody called must
		// not write a log line. denialReason asks the logging form, for a call really made.
		return _declaredTypes.permits(tool);
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
		return _declaredTypes.denialReason(tool);
	}
}
