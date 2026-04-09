/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Callback for requesting user approval before executing a tool.
 *
 * <p>The agent loop calls this when a tool requires user approval (based on
 * {@link SecurityContext#checkApproval}). The implementation blocks until the user
 * responds — the agent loop thread is already blocking by design.</p>
 *
 * <p>Core defines this interface; integrators implement it against their UI
 * (SSE, WebSocket, CLI prompt, etc.).</p>
 *
 * @see ApprovalRequest
 * @see ApprovalResult
 */
@FunctionalInterface
public interface ApprovalCallback {

	/**
	 * Request user approval for a tool call.
	 *
	 * <p>Blocks until the user approves or rejects.</p>
	 *
	 * @param request details of the tool call needing approval
	 * @return the user's decision
	 */
	ApprovalResult requestApproval(ApprovalRequest request);

	/** Auto-approves all requests. For tests and auto-approve mode. */
	ApprovalCallback AUTO_APPROVE = request -> new ApprovalResult.Approved();
}
