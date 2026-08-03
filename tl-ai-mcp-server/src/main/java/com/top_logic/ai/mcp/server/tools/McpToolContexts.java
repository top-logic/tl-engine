/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * The execution context an MCP tool call runs in.
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
 * <li>The run has <b>no principal</b>. No principal is invented - not the account that deployed the
 * application, not a service user - because a fabricated identity would make the audit log lie about
 * who acted.</li>
 * <li>The run is <b>unattended</b>: there is no human on the other end of the protocol to answer an
 * approval prompt, so the policy's {@code <background-allowlist>} applies as it does to a scheduled
 * run.</li>
 * </ul>
 *
 * <p>
 * Under {@link AiSecurityMode#ENFORCED} those two facts are irreconcilable - a run that cannot name
 * its principal cannot be authorized - so tool execution over MCP is refused outright rather than
 * running as the system. Under {@link AiSecurityMode#LEGACY}, the default and the state of every
 * application that has not written a {@code <tool-access>} policy, nothing is checked anyway and the
 * endpoint behaves exactly as it did before this class existed.
 * </p>
 *
 * @see McpToolAdapter
 */
final class McpToolContexts {

	private McpToolContexts() {
		// Static utility.
	}

	/**
	 * Why this deployment must not execute tools over MCP, or <code>null</code> if it may.
	 *
	 * @return A message for the MCP client, or <code>null</code> when the call may proceed.
	 */
	static String refusalReason() {
		return refusalReason(AiToolAccessService.ceiling());
	}

	/**
	 * Why a deployment in the given mode must not execute tools over MCP.
	 *
	 * <p>
	 * The mode is a parameter so that both outcomes are decidable without a running application.
	 * </p>
	 *
	 * @param mode
	 *        The deployment's effective security mode.
	 * @return A message for the MCP client, or <code>null</code> when the call may proceed.
	 */
	static String refusalReason(AiSecurityMode mode) {
		if (!mode.requiresUser()) {
			return null;
		}
		return "Error: this application enforces role-based agent tool access (mode " + mode
			+ "), which authorizes every tool call against the roles of a known user - and the MCP"
			+ " endpoint /mcp/* does not authenticate its callers, so this run has no user to"
			+ " authorize. Executing the tool anyway would bypass the <tool-access> policy, so it is"
			+ " refused. Reach these tools through an authenticated entry point instead (the chat UI,"
			+ " or a TL-Script call, both of which act as the logged-in account). Transport"
			+ " authentication for /mcp/* is tracked as issue #261.";
	}

	/**
	 * The context for one MCP tool call: no principal, unattended.
	 *
	 * <p>
	 * Only meaningful when {@link #refusalReason()} returned <code>null</code>; the caller checks
	 * that first, because under an enforcing mode building this context would fail by design.
	 * </p>
	 *
	 * @param transaction
	 *        The scope the caller opened for this call and will commit or roll back.
	 * @param cancellation
	 *        The token shared with {@code transaction}.
	 */
	static ToolContext forCall(TransactionScope transaction, CancellationToken cancellation) {
		SecurityContext security = TopLogicSecurityContext.forRun(null, true);
		// Auto-approve: the protocol has no approval round trip. That is precisely why the run counts
		// as unattended above, which is what subjects it to the background allowlist.
		return new ToolContext(security, ApprovalCallback.AUTO_APPROVE, transaction, cancellation);
	}
}
