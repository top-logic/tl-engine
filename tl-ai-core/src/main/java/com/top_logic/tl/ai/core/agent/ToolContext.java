/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;
import java.util.Set;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.execution.ExecutionHandle;
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
	 * tools need locking and transaction support. Each call creates a fresh scope that
	 * shares the context's cancellation token, so lock waits end promptly when the run
	 * is cancelled.</p>
	 */
	public static ToolContext createUnrestricted() {
		CancellationToken cancellationToken = new CancellationToken();
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(cancellationToken), cancellationToken);
	}

	private final SecurityContext _security;

	private final ApprovalCallback _approval;

	private final TransactionScope _transaction;

	private final Set<String> _sessionApprovedTools;

	private final int _delegationDepth;

	private final String _toolCallId;

	private final CancellationToken _cancellationToken;

	private final ProgressReporter _progressReporter;

	private final ExecutionHandle _executionHandle;

	private final String _model;

	/**
	 * Creates a context with the given security, approval callback, and transaction scope.
	 *
	 * <p>Derives its cancellation token from {@code transaction}, so cancelling this
	 * context's token also ends the scope's lock-retry waits, and vice versa. Pass an
	 * explicit token (see the overloads below) only when the context must observe
	 * cancellation independently of the transaction scope.</p>
	 *
	 * @param security    the security context for the current user
	 * @param approval    the callback for requesting user approval
	 * @param transaction the transaction scope for the current agent run
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval, TransactionScope transaction) {
		this(security, approval, transaction, Set.of(), 0, null, transaction.cancellationToken(),
			ProgressReporter.NOOP);
	}

	/**
	 * Creates a context with the given security, approval callback, transaction scope,
	 * and pre-approved tools from a previous run in the same session.
	 *
	 * <p>Derives its cancellation token from {@code transaction} — see the note on the
	 * 3-argument overload.</p>
	 *
	 * @param security             the security context for the current user
	 * @param approval             the callback for requesting user approval
	 * @param transaction          the transaction scope for the current agent run
	 * @param sessionApprovedTools tool names already approved for the session
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval,
			TransactionScope transaction, Set<String> sessionApprovedTools) {
		this(security, approval, transaction, sessionApprovedTools, 0, null,
			transaction.cancellationToken(), ProgressReporter.NOOP);
	}

	/**
	 * Creates a context with the given security, approval callback, transaction scope,
	 * pre-approved tools, and cancellation token.
	 *
	 * <p>Pass the same token to the {@link TransactionScope} so lock-retry waits also end
	 * when the run is cancelled.</p>
	 *
	 * @param security             the security context for the current user
	 * @param approval             the callback for requesting user approval
	 * @param transaction          the transaction scope for the current agent run
	 * @param sessionApprovedTools tool names already approved for the session
	 * @param cancellationToken    the cancellation token for cooperative cancellation
	 */
	public ToolContext(SecurityContext security, ApprovalCallback approval,
			TransactionScope transaction, Set<String> sessionApprovedTools,
			CancellationToken cancellationToken) {
		this(security, approval, transaction, sessionApprovedTools, 0, null, cancellationToken, ProgressReporter.NOOP);
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
		this(security, approval, transaction, sessionApprovedTools, delegationDepth, toolCallId,
			cancellationToken, progressReporter, new ExecutionHandle(), null);
	}

	private ToolContext(SecurityContext security, ApprovalCallback approval,
			TransactionScope transaction, Set<String> sessionApprovedTools,
			int delegationDepth, String toolCallId, CancellationToken cancellationToken,
			ProgressReporter progressReporter, ExecutionHandle executionHandle, String model) {
		_security = Objects.requireNonNull(security, "Security context must not be null.");
		_approval = Objects.requireNonNull(approval, "Approval callback must not be null.");
		_transaction = Objects.requireNonNull(transaction, "Transaction scope must not be null.");
		_sessionApprovedTools = sessionApprovedTools != null ? sessionApprovedTools : Set.of();
		_delegationDepth = delegationDepth;
		_toolCallId = toolCallId;
		_cancellationToken = Objects.requireNonNull(cancellationToken, "Cancellation token must not be null.");
		_progressReporter = Objects.requireNonNull(progressReporter, "Progress reporter must not be null.");
		_executionHandle = Objects.requireNonNull(executionHandle, "Execution handle must not be null.");
		_model = model;
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
	 * The holder for the persisted execution of the current run.
	 *
	 * <p>
	 * Filled by the {@code ExecutionRecorder} when run recording is active; its value stays
	 * {@code null} for unrecorded runs. Copies of this context share the same holder; a
	 * {@link #forDelegation(TransactionScope) delegation context} gets a fresh one, since the
	 * child run is a separate execution.
	 * </p>
	 */
	public ExecutionHandle executionHandle() {
		return _executionHandle;
	}

	/**
	 * The model of the run this tool executes in, or {@code null} outside an agent loop.
	 *
	 * <p>
	 * Set by the {@link AgentLoop} from its configuration. A tool that produces content the model
	 * has to <em>perceive</em> - an image, a PDF - uses it to ask the model catalogue whether that
	 * model can, and to name the model in its explanation when it cannot.
	 * </p>
	 */
	public String model() {
		return _model;
	}

	/**
	 * Returns a copy of this context bound to the given model.
	 */
	public ToolContext withModel(String model) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, _toolCallId, _cancellationToken, _progressReporter, _executionHandle,
			model);
	}

	/**
	 * Returns a copy of this context with the given tool call ID.
	 */
	public ToolContext withToolCallId(String toolCallId) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, toolCallId, _cancellationToken, _progressReporter, _executionHandle,
			_model);
	}

	/**
	 * Returns a copy of this context with the given progress reporter.
	 */
	public ToolContext withProgressReporter(ProgressReporter progressReporter) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, _toolCallId, _cancellationToken, progressReporter, _executionHandle,
			_model);
	}

	/**
	 * Creates a copy of this context for a delegated sub-agent run.
	 *
	 * <p>The returned context has the delegation depth incremented by one and uses the given
	 * {@code childTransaction}, which must be a fresh {@link TransactionScope}. The parent's
	 * transaction is intentionally not visible to the child: the child's begin/commit/rollback
	 * lifecycle is independent so it cannot interfere with the parent's turn.</p>
	 *
	 * @param childTransaction the transaction scope for the sub-agent run
	 */
	public ToolContext forDelegation(TransactionScope childTransaction) {
		return new ToolContext(_security, _approval, childTransaction, _sessionApprovedTools,
			_delegationDepth + 1, null, _cancellationToken, ProgressReporter.NOOP,
			new ExecutionHandle(), _model);
	}

	/**
	 * Creates a copy of this context for a detached asynchronous subagent run.
	 *
	 * <p>Like {@link #forDelegation(TransactionScope)}, the delegation depth is incremented
	 * and a fresh {@code childTransaction} is used. Unlike {@code forDelegation}, the child
	 * gets its <b>own</b> {@code childToken} instead of sharing the parent's, so cancelling
	 * one background job (or the parent turn ending) does not cancel unrelated subagents.</p>
	 *
	 * @param childTransaction the transaction scope for the subagent run
	 * @param childToken       the subagent's own cancellation token
	 */
	public ToolContext forSubagent(TransactionScope childTransaction, CancellationToken childToken) {
		return new ToolContext(_security, _approval, childTransaction, _sessionApprovedTools,
			_delegationDepth + 1, null, childToken, ProgressReporter.NOOP,
			new ExecutionHandle(), _model);
	}
}
