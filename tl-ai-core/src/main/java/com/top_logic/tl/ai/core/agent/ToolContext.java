/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;

/**
 * Execution context passed to tools.
 *
 * <p>Constructed per-user, per-run. Carries the {@link SecurityContext} for permission
 * checks and the {@link ApprovalCallback} for human-in-the-loop approval.</p>
 *
 * @see ToolExecutor
 * @see SecurityContext
 * @see ApprovalCallback
 */
public class ToolContext {

	// TODO: Add TransactionScope — Pillar 3 (Transactional Editing). Tools that write
	//   shared state must do so inside a transaction that can be reviewed, committed,
	//   or rolled back. The scope would be created per-agent-run and passed here.
	// TODO: Add ProgressReporter — Pillar 7 (Human-in-the-Loop). Long-running tools
	//   should report progress (e.g., "processing 50 of 200 records") so the UI can
	//   display status. Interface: progress(String message) or progress(int current, int total).
	// TODO: Add AgentContext — for sub-agent delegation. Carries parent agent identity,
	//   delegation depth, shared memory references.

	/** Context with no security restrictions and auto-approval. For tests and trusted internal use. */
	public static final ToolContext EMPTY =
		new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE);

	private final SecurityContext _security;

	private final ApprovalCallback _approval;

	/**
	 * Creates a context with the given security and approval callback.
	 *
	 * @param security the security context for the current user
	 * @param approval the callback for requesting user approval
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval) {
		_security = Objects.requireNonNull(security, "Security context must not be null.");
		_approval = Objects.requireNonNull(approval, "Approval callback must not be null.");
	}

	/**
	 * The security context for the current user.
	 *
	 * <p>Tools use this to filter data based on the user's permissions.</p>
	 */
	public SecurityContext security() {
		return _security;
	}

	/**
	 * The approval callback for requesting user confirmation.
	 *
	 * <p>Used by the agent loop to pause for approval on state-changing tools.</p>
	 */
	public ApprovalCallback approval() {
		return _approval;
	}
}
