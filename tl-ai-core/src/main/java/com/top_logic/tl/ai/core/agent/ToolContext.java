/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;
import java.util.Set;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.execution.ExecutionHandle;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.util.TLContext;
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

	/**
	 * Context with no security restrictions, auto-approval, and no-op scope.
	 *
	 * <p>
	 * <b>A test seam.</b> It carries {@link SecurityContext#UNRESTRICTED}, which has no principal and
	 * therefore permits everything. No production path builds a context this way - see
	 * {@link #forCurrentUser()} and {@code ChatProcessor}, both of which resolve the acting account
	 * and refuse without one.
	 * </p>
	 */
	public static final ToolContext EMPTY =
		new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, CancellationToken.UNCANCELLABLE);

	/**
	 * Creates a context acting as the user bound to the calling thread, with a live transaction scope.
	 *
	 * <p>
	 * For the TL-Script entry points, which run on the thread of whoever executed the script: their
	 * tool calls are authorized against that account's roles and their model access against that
	 * account's rights.
	 * </p>
	 *
	 * <p>
	 * Each call creates a fresh transaction scope that shares the context's cancellation token, so lock
	 * waits end promptly when the run is cancelled.
	 * </p>
	 *
	 * <p>
	 * Auto-approves: a script has no approval surface. The run is not treated as a background run,
	 * because the account that executed the script is present and watching it - unlike
	 * {@code aiAgentChatAsync}, which detaches.
	 * </p>
	 *
	 * @throws IllegalStateException
	 *         If no user is bound to the calling thread. A run that cannot name its principal cannot
	 *         be authorized, and there is no unchecked mode to fall back to.
	 */
	public static ToolContext forCurrentUser() {
		CancellationToken cancellationToken = new CancellationToken();
		return new ToolContext(
			TopLogicSecurityContext.forRun(TLContext.currentUser(), false),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellationToken), cancellationToken);
	}

	/**
	 * Creates a context for an unattended run that acts as the given account.
	 *
	 * <p>
	 * For an entry point that has an authenticated account but <b>no human on the other end</b> - the
	 * MCP endpoint is the case this was written for: a request arrives with a credential, so the run
	 * has a principal, but nobody is watching who could answer an approval prompt.
	 * </p>
	 *
	 * <p>
	 * The run is therefore a {@link SecurityContext#isBackgroundRun() background run}: its tool
	 * ceiling is the intersection of the account's grants and the policy's
	 * {@code <background-allowlist>}, which is the configured substitute for the prompt it cannot
	 * show. Such a run confirms its own tool calls - hence {@link ApprovalCallback#AUTO_APPROVE} -
	 * and the allowlist is what bounds it.
	 * </p>
	 *
	 * <p>
	 * Gets a live {@link TransactionScope}, so a write-classified tool the allowlist admits can
	 * actually be committed by whoever drives the call.
	 * </p>
	 *
	 * @param principal
	 *        The authenticated account the run acts as. Never <code>null</code>.
	 * @throws IllegalStateException
	 *         If {@code principal} is <code>null</code> - see
	 *         {@link TopLogicSecurityContext#forRun(Person, boolean)}.
	 */
	public static ToolContext forBackgroundRun(Person principal) {
		CancellationToken cancellationToken = new CancellationToken();
		return new ToolContext(
			TopLogicSecurityContext.forRun(principal, true),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellationToken), cancellationToken);
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
	 * Per-object model access checks for the run's principal.
	 *
	 * <p>
	 * The seam tools use to filter what they return ({@link ModelSecurity#filterReadable}) and to
	 * refuse changes the principal may not make ({@link ModelSecurity#requireWrite}). Reached through
	 * the context tools already receive, so no extra plumbing is needed at the call sites.
	 * </p>
	 *
	 * <p>
	 * Always the real thing for a production run: the engine's model access rights, evaluated for the
	 * run's principal.
	 * </p>
	 */
	public ModelSecurity modelSecurity() {
		return _security.modelSecurity();
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
	 * Creates a copy of this context for a synchronous delegated sub-agent run.
	 *
	 * <p>The returned context has the delegation depth incremented by one and uses the given
	 * {@code childTransaction}, which must be a fresh {@link TransactionScope}. The parent's
	 * transaction is intentionally not visible to the child: the child's begin/commit/rollback
	 * lifecycle is independent so it cannot interfere with the parent's turn.</p>
	 *
	 * <p>
	 * The run's classification is carried over unchanged, including whether it is a
	 * {@link SecurityContext#isBackgroundRun() background run}. A synchronous delegation completes
	 * <em>inside</em> the parent turn, so whoever was there to answer an approval prompt before it
	 * started is still there - the child has exactly the parent's reach, and the
	 * {@code <background-allowlist>} applies to it if and only if it applied to the parent. The
	 * detached counterpart is {@link #forDetachedSubagent(TransactionScope, CancellationToken)}.
	 * </p>
	 *
	 * @param childTransaction the transaction scope for the sub-agent run
	 */
	public ToolContext forDelegation(TransactionScope childTransaction) {
		return new ToolContext(_security, _approval, childTransaction, _sessionApprovedTools,
			_delegationDepth + 1, null, _cancellationToken, ProgressReporter.NOOP,
			new ExecutionHandle(), _model);
	}

	/**
	 * Creates a copy of this context for a detached (fire-and-forget) asynchronous subagent run.
	 *
	 * <p>Like {@link #forDelegation(TransactionScope)}, the delegation depth is incremented
	 * and a fresh {@code childTransaction} is used. Unlike {@code forDelegation}, the child
	 * gets its <b>own</b> {@code childToken} instead of sharing the parent's, so cancelling
	 * one background job (or the parent turn ending) does not cancel unrelated subagents.</p>
	 *
	 * <p>
	 * <b>The child is a background run.</b> {@code spawn_subagent} returns a handle immediately and
	 * the parent turn ends while the child keeps working on a pool thread; from then on there is no
	 * human on the other end to answer an approval prompt, which is the design's own definition of an
	 * unattended run. Its tool ceiling is therefore the intersection of the acting user's grants and
	 * the policy's {@code <background-allowlist>} - the same bound a scheduler job or an
	 * {@code aiAgentChatAsync} run gets. Without it a detached child ran with the acting user's full
	 * interactive grant set, and under
	 * {@link com.top_logic.tl.ai.core.agent.approval.ApprovalMode#AUTO_APPROVE} it self-approved every
	 * write tool after the parent turn had ended.
	 * </p>
	 *
	 * <p>
	 * The child keeps the parent's {@link #approval() approval callback} deliberately. It is not what
	 * bounds the run - {@code AgentLoop} authorizes before it asks for approval, so a tool off the
	 * allowlist is refused whatever the callback would have answered - and in an
	 * {@code ALWAYS_ASK} session an open browser window can still answer the child's prompts, which is
	 * worth keeping.
	 * </p>
	 *
	 * @param childTransaction the transaction scope for the subagent run
	 * @param childToken       the subagent's own cancellation token
	 */
	public ToolContext forDetachedSubagent(TransactionScope childTransaction, CancellationToken childToken) {
		return new ToolContext(_security.withBackgroundCeiling(), _approval, childTransaction,
			_sessionApprovedTools, _delegationDepth + 1, null, childToken, ProgressReporter.NOOP,
			new ExecutionHandle(), _model);
	}
}
