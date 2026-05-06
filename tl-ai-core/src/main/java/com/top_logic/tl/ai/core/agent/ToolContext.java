/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;
import java.util.Set;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.core.tools.ToolExecutor;

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
public class ToolContext implements ToolExecutionContext {

	/** Context with no security restrictions, auto-approval, and no-op scope. For tests only. */
	public static final ToolContext EMPTY =
		new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, CancellationToken.UNCANCELLABLE);

	/**
	 * Creates a new unrestricted context with a live transaction scope.
	 *
	 * <p>Use this for programmatic agent invocations (TL-Script, background jobs) where
	 * tools need locking and transaction support. Each call creates a fresh scope.</p>
	 */
	public static ToolContext createUnrestricted() {
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope());
	}

	private final SecurityContext _security;

	private final ApprovalCallback _approval;

	private final TransactionScope _transaction;

	private final Set<String> _sessionApprovedTools;

	private final int _delegationDepth;

	private final String _toolCallId;

	private final CancellationToken _cancellationToken;

	private final ProgressReporter _progressReporter;

	/**
	 * Creates a context with the given security, approval callback, and transaction scope.
	 *
	 * @param security    the security context for the current user
	 * @param approval    the callback for requesting user approval
	 * @param transaction the transaction scope for the current agent run
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval, TransactionScope transaction) {
		this(security, approval, transaction, Set.of(), 0, null, new CancellationToken(), ProgressReporter.NOOP);
	}

	/**
	 * Creates a context with the given security, approval callback, transaction scope,
	 * and pre-approved tools from a previous run in the same session.
	 *
	 * @param security             the security context for the current user
	 * @param approval             the callback for requesting user approval
	 * @param transaction          the transaction scope for the current agent run
	 * @param sessionApprovedTools tool names already approved for the session
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval,
			TransactionScope transaction, Set<String> sessionApprovedTools) {
		this(security, approval, transaction, sessionApprovedTools, 0, null, new CancellationToken(), ProgressReporter.NOOP);
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
	 * Creates a context with the given security, approval callback, transaction scope,
	 * and cancellation token.
	 *
	 * @param security          the security context for the current user
	 * @param approval          the callback for requesting user approval
	 * @param transaction       the transaction scope for the current agent run
	 * @param cancellationToken the cancellation token for cooperative cancellation
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval,
			TransactionScope transaction, CancellationToken cancellationToken) {
		this(security, approval, transaction, Set.of(), 0, null, cancellationToken, ProgressReporter.NOOP);
	}

	private ToolContext(SecurityContext security, ApprovalCallback approval,
			TransactionScope transaction, Set<String> sessionApprovedTools,
			int delegationDepth, String toolCallId, CancellationToken cancellationToken,
			ProgressReporter progressReporter) {
		_security = Objects.requireNonNull(security, "Security context must not be null.");
		_approval = Objects.requireNonNull(approval, "Approval callback must not be null.");
		_transaction = Objects.requireNonNull(transaction, "Transaction scope must not be null.");
		_sessionApprovedTools = sessionApprovedTools != null ? sessionApprovedTools : Set.of();
		_delegationDepth = delegationDepth;
		_toolCallId = toolCallId;
		_cancellationToken = Objects.requireNonNull(cancellationToken, "Cancellation token must not be null.");
		_progressReporter = Objects.requireNonNull(progressReporter, "Progress reporter must not be null.");
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

	/**
	 * Tool names already approved for the current session.
	 *
	 * <p>Carried across runs so the {@link AgentLoop} can skip approval for tools the user
	 * already approved with "Allow for Session" in an earlier run.</p>
	 */
	public Set<String> sessionApprovedTools() {
		return _sessionApprovedTools;
	}

	/**
	 * The current delegation depth.
	 *
	 * <p>Zero for the top-level agent, incremented by one for each level of sub-agent delegation.</p>
	 */
	public int delegationDepth() {
		return _delegationDepth;
	}

	/**
	 * The tool call ID currently being executed, or {@code null} if not set.
	 *
	 * <p>
	 * Set by the agent loop before executing each tool. Used by {@code DelegationTool} to identify
	 * its own tool call ID so child events can be routed to the correct delegation in the UI.
	 * </p>
	 */
	public String toolCallId() {
		return _toolCallId;
	}

	/**
	 * The cancellation token for cooperative cancellation of long-running operations.
	 *
	 * <p>Tools can check {@link CancellationToken#isCancelled()} periodically or call
	 * {@link CancellationToken#checkCancelled()} to throw if the run was cancelled.</p>
	 */
	@Override
	public CancellationToken cancellationToken() {
		return _cancellationToken;
	}

	/**
	 * The progress reporter for signaling tool execution status to the UI.
	 *
	 * <p>Tools call {@link ProgressReporter#progress(String)} to report intermediate status
	 * during long-running operations.</p>
	 */
	@Override
	public ProgressReporter progressReporter() {
		return _progressReporter;
	}

	/**
	 * Returns a copy of this context with the given tool call ID.
	 */
	public ToolContext withToolCallId(String toolCallId) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, toolCallId, _cancellationToken, _progressReporter);
	}

	/**
	 * Returns a copy of this context with the given progress reporter.
	 */
	public ToolContext withProgressReporter(ProgressReporter progressReporter) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, _toolCallId, _cancellationToken, progressReporter);
	}

	/**
	 * Creates a copy of this context for a delegated sub-agent run.
	 *
	 * <p>The returned context has the delegation depth incremented by one and uses the given
	 * child transaction scope. All other fields are inherited from this context.</p>
	 *
	 * <p>When the parent's scope is passed as {@code childTransaction}, locks acquired by
	 * child tools accumulate on the parent scope and are only released on parent
	 * commit/rollback. This is intentional — it ensures the parent holds all locks
	 * until the entire turn is committed.</p>
	 *
	 * @param childTransaction the transaction scope for the sub-agent run
	 */
	public ToolContext forDelegation(TransactionScope childTransaction) {
		return new ToolContext(_security, _approval, childTransaction, _sessionApprovedTools,
			_delegationDepth + 1, null, _cancellationToken, ProgressReporter.NOOP);
	}
}
