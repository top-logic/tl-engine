/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import junit.framework.TestCase;

/**
 * Tests the decision {@link McpToolContexts} makes for an unauthenticated MCP caller.
 *
 * <p>
 * There is nothing to inject: {@code /mcp/*} names no user, agent tool execution is authorized
 * against the acting account for every run, and the two cannot be reconciled. The refusal is
 * therefore unconditional and decidable without a running application.
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
	 * Tool execution over MCP is refused, whatever the deployment configures.
	 *
	 * <p>
	 * The closed door that used to be {@code ToolExecutions.execute(tool, args, null)}, and that used
	 * to open again for a deployment which had configured no policy.
	 * </p>
	 */
	public void testExecutionIsAlwaysRefused() {
		assertNotNull("An unauthenticated tool call must never execute.",
			McpToolContexts.refusalReason());
	}

	/** The refusal says why in terms an administrator can act on. */
	public void testRefusalNamesTheCauseAndTheWayRound() {
		String reason = McpToolContexts.refusalReason();

		assertTrue(reason, reason.contains("/mcp/*"));
		assertTrue("The message must name the cause, not just the refusal.",
			reason.contains("does not authenticate"));
		assertTrue("The message must say what authorization needs and this call lacks.",
			reason.contains("authenticated caller"));
		assertTrue("The message must point at the tracked follow-up.", reason.contains("#261"));
		assertTrue("The message must name an entry point that does work.",
			reason.contains("chat") || reason.contains("TL-Script"));
	}

	/**
	 * The refusal no longer talks about security modes.
	 *
	 * <p>
	 * A message naming a mode would send an administrator looking for a setting to change, and there is
	 * none: the fix is transport authentication (#261), not configuration.
	 * </p>
	 */
	public void testRefusalDoesNotOfferAModeToChange() {
		String reason = McpToolContexts.refusalReason();

		assertFalse(reason, reason.contains("security mode"));
		assertFalse(reason, reason.contains("(mode "));
		assertFalse(reason, reason.contains("LEGACY"));
		assertFalse(reason, reason.contains("ENFORCED"));
	}
}
