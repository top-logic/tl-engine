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

	// TODO: Add ProgressReporter — Pillar 7 (Human-in-the-Loop). Long-running tools
	//   should report progress (e.g., "processing 50 of 200 records") so the UI can
	//   display status. Interface: progress(String message) or progress(int current, int total).
	// TODO: Add AgentContext — for sub-agent delegation. Carries parent agent identity,
	//   delegation depth, shared memory references.

	/** Context with no security restrictions, auto-approval, and no transaction. For tests and trusted internal use. */
	public static final ToolContext EMPTY =
		new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);

	private final SecurityContext _security;

	private final ApprovalCallback _approval;

	private final TransactionScope _transaction;

	/**
	 * Creates a context with the given security, approval callback, and transaction scope.
	 *
	 * @param security    the security context for the current user
	 * @param approval    the callback for requesting user approval
	 * @param transaction the transaction scope for the current agent run
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval, TransactionScope transaction) {
		_security = Objects.requireNonNull(security, "Security context must not be null.");
		_approval = Objects.requireNonNull(approval, "Approval callback must not be null.");
		_transaction = Objects.requireNonNull(transaction, "Transaction scope must not be null.");
	}

	/**
	 * Creates a context with the given security and approval callback.
	 * Uses {@link TransactionScope#NOOP} for the transaction scope.
	 *
	 * @param security the security context for the current user
	 * @param approval the callback for requesting user approval
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval) {
		this(security, approval, TransactionScope.NOOP);
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

	/**
	 * The transaction scope for the current agent run.
	 *
	 * <p>Tools do not call begin/commit/rollback — the agent loop manages the lifecycle.
	 * Tools can check {@link TransactionScope#isActive()} if needed.</p>
	 */
	public TransactionScope transaction() {
		return _transaction;
	}
}
