/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

/**
 * The decision whether an MCP tool call may execute at all.
 *
 * <p>
 * <b>The MCP endpoint has no caller identity.</b> {@code /mcp/*} is served by
 * {@code MCPTransportServlet}, which is registered in this module's {@code web-fragment.xml} with no
 * filter and no {@code security-constraint}; the transport's own security validator is the SDK's
 * {@code NOOP}; and the tool handler runs on a Reactor worker thread with no ambient TopLogic
 * interaction, so {@code TLContext.currentUser()} is <code>null</code> there. Compare
 * {@code tl-chat}, whose {@code ChatSessionFilter} rejects unauthenticated callers on
 * {@code /chat/*} before any agent code runs. Real transport authentication for {@code /mcp/*} is
 * tracked as issue #261.
 * </p>
 *
 * <p>
 * Two consequences follow, and this class is where both are stated:
 * </p>
 * <ul>
 * <li>The run would have <b>no principal</b>. None is invented - not the account that deployed the
 * application, not a service user - because a fabricated identity would make the audit log lie about
 * who acted.</li>
 * <li>Agent tool execution is authorized against the acting principal's roles and per-object rights,
 * always. A run that cannot name its principal cannot be authorized.</li>
 * </ul>
 *
 * <p>
 * Those two facts are irreconcilable, so tool execution over MCP is <b>refused outright</b> - for
 * every deployment, not only for some. Resources and prompts are unaffected; only tool calls are.
 * </p>
 *
 * @see McpToolAdapter
 */
final class McpToolContexts {

	private McpToolContexts() {
		// Static utility.
	}

	/**
	 * Why this deployment must not execute tools over MCP.
	 *
	 * <p>
	 * Never <code>null</code>: there is no configuration under which an unauthenticated caller may
	 * execute an agent tool. The method survives as the one place the refusal is worded, and as the
	 * seam issue #261 will change once the transport can authenticate.
	 * </p>
	 *
	 * @return A message for the MCP client, never <code>null</code>.
	 */
	static String refusalReason() {
		return "Error: executing an agent tool requires an authenticated caller, because every tool"
			+ " call is authorized against the roles and the model access rights of the account it acts"
			+ " as - and the MCP endpoint /mcp/* does not authenticate its callers, so this call has no"
			+ " account to act as. No principal is invented for it, so the call is refused. Reach these"
			+ " tools through an authenticated entry point instead (the chat UI, or a TL-Script call,"
			+ " both of which act as the logged-in account). Transport authentication for /mcp/* is"
			+ " tracked as issue #261.";
	}
}
