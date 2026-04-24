/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.ToolSchema;
import com.top_logic.tl.ai.core.tools.Tool;
import com.top_logic.tl.ai.core.tools.ToolMode;
import com.top_logic.tl.ai.core.tools.ToolResult;

/**
 * Tests for {@link SecurityContext}.
 */
public class TestSecurityContext extends TestCase {

	/**
	 * UNRESTRICTED context allows all tools and returns DEVELOPMENT mode.
	 */
	public void testUnrestrictedAllowsAll() {
		SecurityContext ctx = SecurityContext.UNRESTRICTED;
		Tool readTool = stubTool("read", ToolMode.READ);
		Tool globalTool = stubTool("edit", ToolMode.GLOBAL);

		assertTrue(ctx.hasToolPermission(readTool));
		assertTrue(ctx.hasToolPermission(globalTool));
		assertEquals(ExecutionMode.DEVELOPMENT, ctx.executionMode());
	}

	/**
	 * UNRESTRICTED auto-approves all tool calls (no approval needed).
	 */
	public void testUnrestrictedAutoApproves() {
		SecurityContext ctx = SecurityContext.UNRESTRICTED;
		Tool globalTool = stubTool("edit", ToolMode.GLOBAL);
		ToolCall tc = new ToolCall("tc1", "edit", "{}");

		PermissionDecision decision = ctx.checkApproval(globalTool, tc);
		assertTrue("Expected Allowed but got " + decision.getClass().getSimpleName(),
			decision instanceof PermissionDecision.Allowed);
	}

	/**
	 * Default checkApproval: READ tools are auto-allowed.
	 */
	public void testDefaultApprovalReadAllowed() {
		SecurityContext ctx = contextWithMode(ExecutionMode.PRODUCTION);
		Tool readTool = stubTool("search", ToolMode.READ);
		ToolCall tc = new ToolCall("tc1", "search", "{}");

		PermissionDecision decision = ctx.checkApproval(readTool, tc);
		assertTrue("Expected Allowed but got " + decision.getClass().getSimpleName(),
			decision instanceof PermissionDecision.Allowed);
	}

	/**
	 * Default checkApproval: USER_SCOPED tools need approval.
	 */
	public void testDefaultApprovalUserScopedNeedsApproval() {
		SecurityContext ctx = contextWithMode(ExecutionMode.PRODUCTION);
		Tool userTool = stubTool("create_view", ToolMode.USER_SCOPED);
		ToolCall tc = new ToolCall("tc1", "create_view", "{}");

		PermissionDecision decision = ctx.checkApproval(userTool, tc);
		assertTrue("Expected NeedsApproval but got " + decision.getClass().getSimpleName(),
			decision instanceof PermissionDecision.NeedsApproval);
	}

	/**
	 * Default checkApproval: GLOBAL tools need approval.
	 */
	public void testDefaultApprovalGlobalNeedsApproval() {
		SecurityContext ctx = contextWithMode(ExecutionMode.DEVELOPMENT);
		Tool globalTool = stubTool("create_class", ToolMode.GLOBAL);
		ToolCall tc = new ToolCall("tc1", "create_class", "{}");

		PermissionDecision decision = ctx.checkApproval(globalTool, tc);
		assertTrue("Expected NeedsApproval but got " + decision.getClass().getSimpleName(),
			decision instanceof PermissionDecision.NeedsApproval);
	}

	private static SecurityContext contextWithMode(ExecutionMode mode) {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return mode;
			}
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
			public ToolResult execute(Map<String, Object> args, ToolContext context) { return ToolResult.of(""); }

			@Override
			public ToolMode mode() { return mode; }
		};
	}
}
