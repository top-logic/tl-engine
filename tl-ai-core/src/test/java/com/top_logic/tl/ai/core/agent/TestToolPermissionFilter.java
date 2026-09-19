/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;


import junit.framework.TestCase;

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
