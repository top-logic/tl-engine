/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.security.AiSecurityMode;

/**
 * Tests the decision {@link McpToolContexts} makes for an unauthenticated MCP caller.
 *
 * <p>
 * The mode is injected rather than configured, because what is under test is the rule and not the
 * service that resolves it: {@code /mcp/*} names no user, so a mode that
 * {@link AiSecurityMode#requiresUser() requires} one cannot execute a tool there.
 * </p>
 *
 * <p>
 * What these tests deliberately do <b>not</b> cover is the HTTP round trip - that no filter guards
 * {@code /mcp/*} is a property of the deployment descriptor, and asserting it would need a servlet
 * container. It is stated in the boot log instead ({@code MCPServerService}).
 * </p>
 */
public class TestMcpToolContexts extends TestCase {

	/**
	 * An application that has not opted into enforcement keeps its previous behaviour.
	 *
	 * <p>
	 * The compatibility half of the change: no existing MCP client stops working, because nothing was
	 * being enforced for it to start failing against.
	 * </p>
	 */
	public void testUnenforcedDeploymentMayExecute() {
		assertNull(McpToolContexts.refusalReason(AiSecurityMode.LEGACY));
	}

	/** A deployment that deliberately acts as the system may execute too - it named no user either. */
	public void testSystemModeMayExecute() {
		assertNull(McpToolContexts.refusalReason(AiSecurityMode.SYSTEM));
	}

	/**
	 * An enforcing deployment refuses, and says why in terms an administrator can act on.
	 *
	 * <p>
	 * The security half: this is the closed door that used to be
	 * {@code ToolExecutions.execute(tool, args, null)}.
	 * </p>
	 */
	public void testEnforcedDeploymentRefuses() {
		String reason = McpToolContexts.refusalReason(AiSecurityMode.ENFORCED);

		assertNotNull("An enforced deployment must not execute an unauthenticated tool call.", reason);
		assertTrue(reason, reason.contains("/mcp/*"));
		assertTrue("The message must name the cause, not just the refusal.",
			reason.contains("does not authenticate"));
		assertTrue("The message must name the policy that would be bypassed.",
			reason.contains("<tool-access>"));
		assertTrue("The message must point at the tracked follow-up.", reason.contains("#261"));
		assertTrue("The message must name an entry point that does work.",
			reason.contains("chat") || reason.contains("TL-Script"));
	}

	/** Every mode is decided one way or the other; none falls through. */
	public void testEveryModeIsDecided() {
		for (AiSecurityMode mode : AiSecurityMode.values()) {
			String reason = McpToolContexts.refusalReason(mode);
			assertEquals("Refusal must follow exactly from whether the mode requires a user: " + mode,
				mode.requiresUser(), reason != null);
		}
	}
}
