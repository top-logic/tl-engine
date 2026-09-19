/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;


import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;

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
		Tool globalTool = stubTool("edit", ToolMode.STRUCTURAL);

		assertTrue(ctx.hasToolPermission(readTool));
		assertTrue(ctx.hasToolPermission(globalTool));
		assertEquals(ExecutionMode.DEVELOPMENT, ctx.executionMode());
	}

	/**
	 * UNRESTRICTED auto-approves all tool calls (no approval needed).
	 */
	public void testUnrestrictedAutoApproves() {
		SecurityContext ctx = SecurityContext.UNRESTRICTED;
		Tool globalTool = stubTool("edit", ToolMode.STRUCTURAL);
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
	 * Default checkApproval: DATA_WRITE tools need approval.
	 */
	public void testDefaultApprovalDataWriteNeedsApproval() {
		SecurityContext ctx = contextWithMode(ExecutionMode.DEVELOPMENT);
		Tool dataWriteTool = stubTool("execute_script", ToolMode.DATA_WRITE);
		ToolCall tc = new ToolCall("tc1", "execute_script", "{}");

		PermissionDecision decision = ctx.checkApproval(dataWriteTool, tc);
		assertTrue("Expected NeedsApproval but got " + decision.getClass().getSimpleName(),
			decision instanceof PermissionDecision.NeedsApproval);
	}

	/**
	 * Default checkApproval: STRUCTURAL tools need approval.
	 */
	public void testDefaultApprovalStructuralNeedsApproval() {
		SecurityContext ctx = contextWithMode(ExecutionMode.DEVELOPMENT);
		Tool structuralTool = stubTool("create_class", ToolMode.STRUCTURAL);
		ToolCall tc = new ToolCall("tc1", "create_class", "{}");

		PermissionDecision decision = ctx.checkApproval(structuralTool, tc);
		assertTrue("Expected NeedsApproval but got " + decision.getClass().getSimpleName(),
			decision instanceof PermissionDecision.NeedsApproval);
	}

	/**
	 * Default checkApproval: EXTERNAL tools need approval.
	 *
	 * <p>
	 * The one gate an outward call answers to. It is not asserted in {@code PRODUCTION} only: the
	 * prompt is owed in every execution mode, because what makes an external call irreversible is
	 * that another system committed it, not which mode this installation happens to run in.
	 * </p>
	 */
	public void testDefaultApprovalExternalNeedsApproval() {
		ToolCall tc = new ToolCall("tc1", "mcp__jira__createIssue", "{}");
		Tool externalTool = stubTool("mcp__jira__createIssue", ToolMode.EXTERNAL);

		for (ExecutionMode mode : ExecutionMode.values()) {
			PermissionDecision decision = contextWithMode(mode).checkApproval(externalTool, tc);
			assertTrue("Expected NeedsApproval in " + mode + " but got "
				+ decision.getClass().getSimpleName(),
				decision instanceof PermissionDecision.NeedsApproval);
		}
	}

	/**
	 * The default approval decision is defined for every {@link ToolMode}, and {@code READ} is the
	 * only one that is allowed outright.
	 *
	 * <p>
	 * Asserted over {@link ToolMode#values()} rather than constant by constant, so that a mode added
	 * later cannot slip in without a deliberate decision about its prompt. The switch in
	 * {@code checkApproval} is exhaustive and would not compile without a case, but a case that
	 * returns {@code Allowed} compiles fine - and that is the mistake worth failing on.
	 * </p>
	 */
	public void testEveryModeIsDecided() {
		SecurityContext ctx = contextWithMode(ExecutionMode.PRODUCTION);
		for (ToolMode mode : ToolMode.values()) {
			ToolCall tc = new ToolCall("tc1", "tool", "{}");
			PermissionDecision decision = ctx.checkApproval(stubTool("tool", mode), tc);
			if (mode == ToolMode.READ) {
				assertTrue("READ must be allowed without a prompt.",
					decision instanceof PermissionDecision.Allowed);
			} else {
				assertTrue(mode + " must need approval but got "
					+ decision.getClass().getSimpleName(),
					decision instanceof PermissionDecision.NeedsApproval);
			}
		}
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
		return ToolStubs.builder(name).description("").schema(new ToolSchema(name, "", "{}")).mode(mode).result("")
			.build();
	}
}
