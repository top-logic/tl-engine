/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
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
		return new Tool() {
			@Override
			public String name() { return name; }

			@Override
			public String description() { return ""; }

			@Override
			public ToolSchema schema() { return new ToolSchema(name, "", "{}"); }

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("");
			}

			@Override
			public ToolMode mode() { return mode; }
		};
	}
}
