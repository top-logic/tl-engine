/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;
import java.util.Set;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.execution.ExecutionHandle;
import com.top_logic.tl.ai.core.limits.BudgetAllowance;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.util.TLContext;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.tl.ai.tool.api.RunInfo;
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
	 *
	 * <p>
	 * It is a <em>shared</em> instance, and so is its {@link #executionHandle()}. Use it to call a
	 * tool, never to run an agent loop: a run publishes its identity onto that handle, and two runs
	 * sharing this constant would overwrite each other's. {@link #unrestricted()} is the same
	 * context with a handle of its own.
	 * </p>
	 */
	public static final ToolContext EMPTY =
		new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, CancellationToken.UNCANCELLABLE);

	/**
	 * A fresh context shaped like {@link #EMPTY} - no security restrictions, auto-approval, no-op
	 * transaction scope - but with an {@link #executionHandle()} of its own.
	 *
	 * <p>
	 * What the entry points that take no context from their caller run with. Each call gets its own
	 * instance, so concurrent runs do not report each other's run to their tools.
	 * </p>
	 */
	public static ToolContext unrestricted() {
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, CancellationToken.UNCANCELLABLE);
	}

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
			RunSecurity.contextFor(
				new RunPrincipal(TLContext.currentUser(), RunEntryPoint.SCRIPT, false)),
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
			RunSecurity.contextFor(new RunPrincipal(principal, RunEntryPoint.MCP, true)),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellationToken), cancellationToken)
				.unattended();
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

	private final ReadingList _readingList;

	private final String _model;

	private final boolean _attended;

	private final BudgetAllowance _budgetAllowance;

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
			cancellationToken, progressReporter, new ExecutionHandle(), new ReadingList(), null,
			true, BudgetAllowance.UNLIMITED);
	}

	private ToolContext(SecurityContext security, ApprovalCallback approval,
			TransactionScope transaction, Set<String> sessionApprovedTools,
			int delegationDepth, String toolCallId, CancellationToken cancellationToken,
			ProgressReporter progressReporter, ExecutionHandle executionHandle,
			ReadingList readingList, String model, boolean attended,
			BudgetAllowance budgetAllowance) {
		_security = Objects.requireNonNull(security, "Security context must not be null.");
		_approval = Objects.requireNonNull(approval, "Approval callback must not be null.");
		_transaction = Objects.requireNonNull(transaction, "Transaction scope must not be null.");
		_sessionApprovedTools = sessionApprovedTools != null ? sessionApprovedTools : Set.of();
		_delegationDepth = delegationDepth;
		_toolCallId = toolCallId;
		_cancellationToken = Objects.requireNonNull(cancellationToken, "Cancellation token must not be null.");
		_progressReporter = Objects.requireNonNull(progressReporter, "Progress reporter must not be null.");
		_executionHandle = Objects.requireNonNull(executionHandle, "Execution handle must not be null.");
		_readingList = Objects.requireNonNull(readingList, "Reading list must not be null.");
		_model = model;
		_attended = attended;
		_budgetAllowance = Objects.requireNonNull(budgetAllowance, "Budget allowance must not be null.");
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
	 * The object the given identifier names, read with the rights of this run's principal.
	 *
	 * <p>
	 * The same resolution a script tool's object-typed parameter gets, reached from the context a
	 * Java tool already holds, so both kinds of tool turn an identifier into an object in the one
	 * place that runs the read check.
	 * </p>
	 */
	@Override
	public TLObject resolveObject(String id) {
		return TidResolver.resolve(id, modelSecurity());
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
	 * Which agent run this tool call belongs to - run id, conversation, agent, model, and the
	 * delegating run for a sub-agent.
	 *
	 * <p>
	 * Plain data, captured as the run started, so reading it touches no {@code KnowledgeBase} and
	 * can never throw. This is the accessor a tool uses; {@link #executionHandle()}'s
	 * {@link ExecutionHandle#get() wrapper} is an opaque token for the execution store, not a
	 * readable object (Gitea #405).
	 * </p>
	 */
	@Override
	public RunInfo runInfo() {
		return _executionHandle.runInfo();
	}

	/**
	 * What this run read: the sources its answer may point at.
	 *
	 * <p>
	 * One list per run, shared by every copy of this context, so the read tools of a concurrent
	 * batch register into the same list. A {@link #forDelegation(TransactionScope) delegation} and a
	 * {@link #forDetachedSubagent(TransactionScope, CancellationToken) detached sub-agent} get a list
	 * of their own; it joins this one when their result is delivered here.
	 * </p>
	 */
	public ReadingList readingList() {
		return _readingList;
	}

	@Override
	public void readSource(ReadSource source) {
		_readingList.add(source);
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
	 * Whether a human is present in this run who could be asked a question.
	 *
	 * @see ToolExecutionContext#attended()
	 * @see #unattended()
	 */
	@Override
	public boolean attended() {
		return _attended;
	}

	/**
	 * A copy of this context for a run that has nobody to ask.
	 *
	 * <p>
	 * What the scheduler's {@code AgentTask} and the MCP endpoint build their context with. It says
	 * only that no answer can arrive; it authorizes nothing and takes nothing away. Whether such a
	 * run is additionally bounded by the policy's {@code <background-allowlist>} is the decision of
	 * its {@link #security() security context}, made where that context is built - the two are
	 * normally set together, and deliberately remain two statements rather than one, because a run
	 * can lose its human without any change to what it is allowed to do.
	 * </p>
	 */
	public ToolContext unattended() {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, _toolCallId, _cancellationToken, _progressReporter, _executionHandle,
			_readingList, _model, false, _budgetAllowance);
	}

	/**
	 * What is left of the principal's token and money budget for this run, and what the run has
	 * spent of it.
	 *
	 * <p>
	 * {@link BudgetAllowance#UNLIMITED} unless the installation configured a budget and the run's
	 * entry point resolved one - which is what {@code RunLimits.admit} does at every run start. The
	 * loop records each assistant turn into it and stops with
	 * {@link StopReason#BUDGET_EXHAUSTED} when it runs out.
	 * </p>
	 *
	 * <p>
	 * A {@link #forDelegation(TransactionScope) delegated} run shares this instance rather than
	 * getting one of its own: a delegate spends the budget of the run that delegated it.
	 * </p>
	 */
	public BudgetAllowance budgetAllowance() {
		return _budgetAllowance;
	}

	/**
	 * Returns a copy of this context carrying the given budget allowance.
	 *
	 * <p>
	 * Called once per run, where the run is admitted. The copy is what the loop runs with, so
	 * everything the run delegates carries the same allowance.
	 * </p>
	 */
	public ToolContext withBudgetAllowance(BudgetAllowance budgetAllowance) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, _toolCallId, _cancellationToken, _progressReporter, _executionHandle,
			_readingList, _model, _attended, budgetAllowance);
	}

	/**
	 * Returns a copy of this context bound to the given model.
	 */
	public ToolContext withModel(String model) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, _toolCallId, _cancellationToken, _progressReporter, _executionHandle,
			_readingList, model, _attended, _budgetAllowance);
	}

	/**
	 * Returns a copy of this context with the given tool call ID.
	 */
	public ToolContext withToolCallId(String toolCallId) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, toolCallId, _cancellationToken, _progressReporter, _executionHandle,
			_readingList, _model, _attended, _budgetAllowance);
	}

	/**
	 * Returns a copy of this context with the given progress reporter.
	 */
	public ToolContext withProgressReporter(ProgressReporter progressReporter) {
		return new ToolContext(_security, _approval, _transaction, _sessionApprovedTools,
			_delegationDepth, _toolCallId, _cancellationToken, progressReporter, _executionHandle,
			_readingList, _model, _attended, _budgetAllowance);
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
			waitedForExecutionHandle(), new ReadingList(), _model, _attended, _budgetAllowance);
	}

	/**
	 * A {@link #childExecutionHandle() child handle} for a delegation this run waits for, whose
	 * {@link ExecutionHandle#checkpoint() checkpoint} before an approval writes this run's transcript
	 * - the turn on the user's screen (#598).
	 */
	private ExecutionHandle waitedForExecutionHandle() {
		ExecutionHandle child = childExecutionHandle();
		child.checkpointThrough(_executionHandle);
		return child;
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
	 * <p>
	 * For the same reason the child inherits the parent's {@link #attended()} rather than being made
	 * unattended along with its security ceiling: the two answer different questions. The ceiling
	 * asks whether an approval prompt would be <em>shown in time</em> - the parent turn has ended,
	 * so it would not - while {@link #attended()} asks whether the account is there at all. A
	 * subagent of a chat run may therefore still ask its question, and any window of that account
	 * answers it; a subagent of a scheduled run may not, because there was never anybody.
	 * </p>
	 *
	 * @param childTransaction the transaction scope for the subagent run
	 * @param childToken       the subagent's own cancellation token
	 */
	public ToolContext forDetachedSubagent(TransactionScope childTransaction, CancellationToken childToken) {
		return new ToolContext(_security.withBackgroundCeiling(), _approval, childTransaction,
			_sessionApprovedTools, _delegationDepth + 1, null, childToken, ProgressReporter.NOOP,
			childExecutionHandle(), new ReadingList(), _model, _attended, _budgetAllowance);
	}

	/**
	 * A fresh {@link ExecutionHandle} for a run this one delegates: its own execution, with this
	 * run named as its {@link RunInfo#parent() parent}.
	 *
	 * <p>
	 * The parent's {@link RunInfo} is plain data, so a child tool can read who delegated it (and,
	 * through {@link RunInfo#root()}, the whole chain up to the top-level run) with the same
	 * revision safety as its own facts.
	 * </p>
	 */
	private ExecutionHandle childExecutionHandle() {
		return new ExecutionHandle(runInfo(), _delegationDepth + 1);
	}
}
