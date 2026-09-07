/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import junit.framework.TestCase;

/**
 * Tests that an MCP session stays with the account that opened it.
 *
 * <p>
 * The transport keys every request after {@code initialize} by the {@code mcp-session-id} header and
 * keeps no owner, so authenticating each request is not enough on its own: two accounts that both
 * hold a valid credential would otherwise be able to take turns on one session, and the second would
 * inherit the first one's conversation.
 * </p>
 */
public class TestMcpSessionPrincipals extends TestCase {

	/** The account that opened a session may use it. */
	public void testTheOwnerMayUseItsSession() {
		McpSessionPrincipals sessions = new McpSessionPrincipals();
		sessions.bind("session-1", "anna");

		assertTrue(sessions.mayUse("session-1", "anna"));
	}

	/** A second account may not attach to it. */
	public void testASecondAccountMayNot() {
		McpSessionPrincipals sessions = new McpSessionPrincipals();
		sessions.bind("session-1", "anna");

		assertFalse("A valid credential of its own must not let ben onto anna's session.",
			sessions.mayUse("session-1", "ben"));
	}

	/**
	 * An unknown session id is left to the transport.
	 *
	 * <p>
	 * Either the client is opening its session in this very request, or the transport does not know
	 * the id either and answers {@code 404}. Deciding it here would duplicate the transport's session
	 * lookup and could disagree with it.
	 * </p>
	 */
	public void testAnUnknownSessionIsNotThisClassesBusiness() {
		assertTrue(new McpSessionPrincipals().mayUse("never-seen", "anna"));
	}

	/**
	 * A deleted session is forgotten, and its id becomes claimable again.
	 *
	 * <p>
	 * Which is correct only because the transport forgets it at the same moment - the client's
	 * {@code DELETE} removes both entries.
	 * </p>
	 */
	public void testADeletedSessionIsReleased() {
		McpSessionPrincipals sessions = new McpSessionPrincipals();
		sessions.bind("session-1", "anna");
		assertEquals(1, sessions.size());

		sessions.release("session-1");

		assertEquals(0, sessions.size());
		assertTrue(sessions.mayUse("session-1", "ben"));
	}

	/** Re-binding an id replaces its owner, as a fresh initialize on a reused id would. */
	public void testRebindingReplacesTheOwner() {
		McpSessionPrincipals sessions = new McpSessionPrincipals();
		sessions.bind("session-1", "anna");
		sessions.bind("session-1", "ben");

		assertFalse(sessions.mayUse("session-1", "anna"));
		assertTrue(sessions.mayUse("session-1", "ben"));
	}
}
