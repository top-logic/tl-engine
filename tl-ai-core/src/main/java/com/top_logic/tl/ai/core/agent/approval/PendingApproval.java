/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A tool approval that is waiting for a user decision.
 *
 * <p>
 * The counterpart of {@link UserInteractionRequest} for approvals: it pairs the
 * {@link ApprovalRequest} the agent loop raised with the future its thread is parked on, so a UI
 * can discover the approval through {@link UserInteractionStore#getPendingApprovals()} and answer
 * it through {@link UserInteractionStore#resolve(String, String)} without having observed the
 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ApprovalRequired} event that announced it.
 * </p>
 *
 * <p>
 * An approval is kept in its own shape rather than as a {@link UserInteractionRequest} because an
 * approval UI needs what only {@link ApprovalRequest} carries: the tool name, its arguments and its
 * {@link com.top_logic.tl.ai.tool.api.ToolMode}.
 * </p>
 *
 * @param request   The approval the agent loop is waiting on.
 * @param createdAt Creation timestamp in milliseconds.
 * @param future    The future the agent thread blocks on. Completed with the user's response
 *                  string (see {@link ApprovalResult}).
 */
public record PendingApproval(ApprovalRequest request, long createdAt, CompletableFuture<String> future) {

	/** */
	public PendingApproval {
		Objects.requireNonNull(request);
		Objects.requireNonNull(future);
	}

	/**
	 * The ID to pass to {@link UserInteractionStore#resolve(String, String)} to answer this
	 * approval.
	 */
	public String id() {
		return request.approvalRequestId();
	}
}
