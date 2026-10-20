/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;


import junit.framework.TestCase;

import com.top_logic.tl.ai.core.security.DeclaredTypeAccess;
import com.top_logic.tl.ai.core.security.DeclaredTypeAccess.Verdict;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests for {@link ToolPermissionFilter}.
 */
public class TestToolPermissionFilter extends TestCase {

	/** READ tool in PRODUCTION mode: allowed. */
	public void testReadToolAllowedInProduction() {
		ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(ExecutionMode.PRODUCTION));
		Tool tool = stubTool("search", ToolMode.READ);

		assertTrue(filter.test(tool));
		assertNull(filter.denialReason(tool));
	}

	/** STRUCTURAL tool in PRODUCTION mode: denied. */
	public void testStructuralToolDeniedInProduction() {
		ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(ExecutionMode.PRODUCTION));
		Tool tool = stubTool("editModel", ToolMode.STRUCTURAL);

		assertFalse(filter.test(tool));
		String reason = filter.denialReason(tool);
		assertNotNull(reason);
		assertTrue(reason.contains("editModel"));
		assertTrue(reason.contains("production mode"));
	}

	/** STRUCTURAL tool in DEVELOPMENT mode: allowed. */
	public void testStructuralToolAllowedInDevelopment() {
		ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(ExecutionMode.DEVELOPMENT));
		Tool tool = stubTool("editModel", ToolMode.STRUCTURAL);

		assertTrue(filter.test(tool));
		assertNull(filter.denialReason(tool));
	}

	/** STRUCTURAL tool in MAINTENANCE mode: allowed. */
	public void testStructuralToolAllowedInMaintenance() {
		ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(ExecutionMode.MAINTENANCE));
		Tool tool = stubTool("editModel", ToolMode.STRUCTURAL);

		assertTrue(filter.test(tool));
		assertNull(filter.denialReason(tool));
	}

	/** DATA_WRITE tool in PRODUCTION mode: allowed (only STRUCTURAL is gated). */
	public void testDataWriteAllowedInProduction() {
		ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(ExecutionMode.PRODUCTION));
		Tool tool = stubTool("insertRecord", ToolMode.DATA_WRITE);

		assertTrue(filter.test(tool));
		assertNull(filter.denialReason(tool));
	}

	/**
	 * EXTERNAL tool in every execution mode: allowed.
	 *
	 * <p>
	 * The production ban exists because a {@code STRUCTURAL} tool would change this application's
	 * own model on a live installation. An external call changes another system's data, which this
	 * application's execution mode says nothing about - so denying it here would withhold a
	 * legitimately granted tool behind a reason that is not true of it. Its control is the approval
	 * prompt.
	 * </p>
	 */
	public void testExternalAllowedInEveryExecutionMode() {
		Tool tool = stubTool("mcp__jira__createIssue", ToolMode.EXTERNAL);

		for (ExecutionMode mode : ExecutionMode.values()) {
			ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(mode));
			assertTrue("EXTERNAL must pass the execution-mode gate in " + mode, filter.test(tool));
			assertNull("EXTERNAL must have no denial reason in " + mode, filter.denialReason(tool));
		}
	}

	/** Tool denied by user permission (hasToolPermission returns false). */
	public void testUserPermissionDenied() {
		SecurityContext ctx = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool t) { return false; }

			@Override
			public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
		};
		ToolPermissionFilter filter = new ToolPermissionFilter(ctx);
		Tool tool = stubTool("secret", ToolMode.READ);

		assertFalse(filter.test(tool));
		String reason = filter.denialReason(tool);
		assertNotNull(reason);
		assertTrue(reason.contains("secret"));
		assertTrue(reason.contains("Permission denied"));
	}

	/**
	 * A tool whose declared types are all unreadable is dropped from the schemas <em>and</em> refused
	 * at the gate, with the same reason (issue #258).
	 *
	 * <p>
	 * The two halves are asserted together because their agreeing is the property: a tool the model
	 * is offered and every call to which is refused would cost a turn to discover.
	 * </p>
	 */
	public void testDeclaredTypesWithoutReadAccessWithholdTheTool() {
		ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(ExecutionMode.DEVELOPMENT),
			new DeclaredTypeAccess(name -> Verdict.UNREADABLE));
		Tool tool = ToolStubs.onTypes("listCustomers", "myapp:Customer");

		assertFalse("A tool whose declared types are out of reach must not be offered.",
			filter.test(tool));
		String reason = filter.denialReason(tool);
		assertNotNull(reason);
		assertTrue("The reason must name the type rather than a capability: " + reason,
			reason.contains("myapp:Customer"));
	}

	/** The same tool for a principal who may read the declared type: offered and allowed. */
	public void testDeclaredTypesWithReadAccessKeepTheTool() {
		ToolPermissionFilter filter = new ToolPermissionFilter(contextWithMode(ExecutionMode.DEVELOPMENT),
			new DeclaredTypeAccess(name -> Verdict.READABLE));
		Tool tool = ToolStubs.onTypes("listCustomers", "myapp:Customer");

		assertTrue(filter.test(tool));
		assertNull(filter.denialReason(tool));
	}

	/**
	 * The capability gate decides before the declared types do.
	 *
	 * <p>
	 * A tool the user may not use at all must be explained by the capability it costs; naming a type
	 * instead would send them to the {@code <security-config>} while the {@code <tool-access>} policy
	 * is what refused them.
	 * </p>
	 */
	public void testTheCapabilityGateAnswersBeforeTheDeclaredTypes() {
		SecurityContext denied = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool t) {
				return false;
			}

			@Override
			public String toolDenialReason(Tool t) {
				return "Permission denied: tool 'listCustomers' needs the 'data.read' capability.";
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		ToolPermissionFilter filter = new ToolPermissionFilter(denied,
			new DeclaredTypeAccess(name -> Verdict.UNREADABLE));

		String reason = filter.denialReason(ToolStubs.onTypes("listCustomers", "myapp:Customer"));
		assertTrue("The capability must be named: " + reason, reason.contains("data.read"));
		assertFalse("The type must not be named where the capability decided: " + reason,
			reason.contains("myapp:Customer"));
	}

	private static SecurityContext contextWithMode(ExecutionMode mode) {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) { return true; }

			@Override
			public ExecutionMode executionMode() { return mode; }
		};
	}

	private static Tool stubTool(String name, ToolMode mode) {
		return ToolStubs.builder(name).description("").schema(new ToolSchema(name, "", "{}")).mode(mode).result("")
			.build();
	}
}
