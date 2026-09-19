/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.sched;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.ChatProcessor.ChatRunOptions;
import com.top_logic.ai.chat.layout.ChatProcessor.SendResult;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.sched.task.impl.StateHandlingTask;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * A scheduler task that puts one prompt to a named agent and records the run as a chat session.
 *
 * <p>
 * The point of running the agent <em>through the chat</em> rather than headlessly is the transcript:
 * a scheduled run appears in the chat history of the account it ran as, next to that account's own
 * conversations, and can be opened, read and continued there. {@code AgentRunner} would execute the
 * same agent with less plumbing, but what it leaves behind is an execution row, not a sitting the
 * user can open. Everything else this task does - the tracker, the approval mode, the sub-session -
 * follows from taking that route.
 * </p>
 *
 * <p>
 * Configured in the {@code Scheduler}'s service configuration like any other task:
 * </p>
 *
 * <pre>
 * &lt;task class="com.top_logic.ai.chat.sched.AgentTask"
 *     name="DailyInvoiceSummary"
 *     agent="Albert"
 *     run-as="root"
 *     run-on-startup="false"
 * &gt;
 *     &lt;prompt&gt;Summarise the invoices recorded today.&lt;/prompt&gt;
 *     &lt;schedules&gt;
 *         &lt;daily time-of-day="18:00"/&gt;
 *     &lt;/schedules&gt;
 * &lt;/task&gt;
 * </pre>
 *
 * <p>
 * <b>Nobody is there.</b> A scheduled run has no browser window, so it cannot be asked anything. In
 * the default {@link ApprovalMode#AUTO_APPROVE} mode the task marks its tracker
 * {@link TaskProgressTracker#isUnattended() unattended}: the run is bounded by the policy's
 * {@code <background-allowlist>} instead of by an approval prompt, and {@code askUser} answers the
 * model at once rather than parking the run until the interaction timeout. See
 * {@code doc/scheduled-agent.md}.
 * </p>
 *
 * <p>
 * Not to be confused with {@code tl.ai.execution:AgentTask}, which is one entry of the plan an agent
 * writes while it works. This is a {@code com.top_logic.util.sched.task.Task} - a scheduled job.
 * </p>
 */
@InApp
public class AgentTask extends StateHandlingTask<AgentTask.Config> {

	/**
	 * Configuration options for {@link AgentTask}.
	 */
	public interface Config extends StateHandlingTask.Config<AgentTask> {

		/** @see #getAgent() */
		String AGENT = "agent";

		/** @see #getPrompt() */
		String PROMPT = "prompt";

		/** @see #getPromptExpression() */
		String PROMPT_EXPRESSION = "prompt-expression";

		/** @see #getRunAs() */
		String RUN_AS = "run-as";

		/** @see #getApprovalMode() */
		String APPROVAL_MODE = "approval-mode";

		/** @see #isRecordSession() */
		String RECORD_SESSION = "record-session";

		/** @see #getSessionTitle() */
		String SESSION_TITLE = "session-title";

		/**
		 * Name of the agent to run.
		 *
		 * <p>
		 * Leave empty to run the application's entry-point agent, which is what a chat message
		 * without an explicit choice runs. The account this task runs as must be allowed to run the
		 * named agent, exactly as in the chat.
		 * </p>
		 */
		@Name(AGENT)
		@Nullable
		String getAgent();

		/**
		 * The message to put to the agent, as plain text.
		 *
		 * <p>
		 * Write it as an element, so a prompt of several lines stays readable:
		 * {@code <prompt>Summarise the invoices recorded today.</prompt>}. Use
		 * {@link #getPromptExpression()} instead when the message has to name application data.
		 * </p>
		 */
		@Name(PROMPT)
		@Nullable
		String getPrompt();

		/**
		 * TL-Script computing the message to put to the agent, evaluated at the start of every run.
		 *
		 * <p>
		 * The alternative to {@link #getPrompt()}, for a task whose message depends on what the
		 * application holds at that moment - "these are the invoices already summarised, take the
		 * rest". The expression takes no arguments and its result is used as text.
		 * </p>
		 *
		 * <p>
		 * Configure either this or {@link #getPrompt()}, not both and not neither.
		 * </p>
		 */
		@Name(PROMPT_EXPRESSION)
		@Nullable
		Expr getPromptExpression();

		/**
		 * Login name of the account the run acts as.
		 *
		 * <p>
		 * The run's principal: its roles decide which tools the agent may use and which agents it
		 * may run, and its access rights decide which objects those tools may touch. The recorded
		 * chat session belongs to this account and appears in its chat history. Defaults to the
		 * {@code root} account.
		 * </p>
		 */
		@Name(RUN_AS)
		@Nullable
		String getRunAs();

		/**
		 * How the tool calls of the run are approved.
		 *
		 * <p>
		 * {@link ApprovalMode#AUTO_APPROVE}, the default, is the only mode that runs unattended: the
		 * agent confirms its own calls and is bounded by the tool-access policy's
		 * {@code <background-allowlist>} instead. {@link ApprovalMode#MANUAL} declares that a person
		 * will answer - any window of the {@code run-as} account can - and makes every
		 * approval-needing call wait for that answer, up to the interaction timeout of 30 minutes.
		 * </p>
		 */
		@Name(APPROVAL_MODE)
		@FormattedDefault("AUTO_APPROVE")
		ApprovalMode getApprovalMode();

		/**
		 * Whether the run is recorded as a chat session of the {@code run-as} account.
		 *
		 * <p>
		 * On by default, and the reason this task goes through the chat at all: the sitting appears
		 * in that account's chat history with the transcript of what the agent did. Switch it off
		 * for a task that is only wanted for its effects; the run then leaves no transcript behind,
		 * only what its tools wrote and what the log says.
		 * </p>
		 */
		@Name(RECORD_SESSION)
		@BooleanDefault(true)
		boolean isRecordSession();

		/**
		 * Title of the recorded chat session.
		 *
		 * <p>
		 * Defaults to the name of the task. Every run creates its own session, so the title is what
		 * tells the runs of one task apart from the rest of the account's chats, not one run from
		 * another - the session's timestamp does that.
		 * </p>
		 */
		@Name(SESSION_TITLE)
		@Nullable
		String getSessionTitle();
	}

	/**
	 * How long to wait between two looks at the running agent.
	 *
	 * <p>
	 * The task thread has nothing to do but notice that the run ended and that it was asked to stop.
	 * A second is short enough for both and cheap enough to spend on a run measured in minutes.
	 * </p>
	 */
	private static final long POLL_INTERVAL_MILLIS = 1000;

	/**
	 * Whether a run of this task is in flight.
	 *
	 * <p>
	 * The overlap guard: a run that takes longer than the interval must not have a second one
	 * started on top of it, writing into a second session with the same conversation's data. Per
	 * task instance, which is per configured task, so two differently configured tasks do not block
	 * each other.
	 * </p>
	 */
	private final AtomicBoolean _running = new AtomicBoolean(false);

	/** The compiled {@link Config#getPromptExpression()}, or <code>null</code> if none is configured. */
	private final QueryExecutor _promptExpression;

	/** Creates an {@link AgentTask} from configuration. */
	@CalledByReflection
	public AgentTask(InstantiationContext context, Config config) {
		super(context, config);

		boolean hasText = !isEmpty(config.getPrompt());
		boolean hasExpression = config.getPromptExpression() != null;
		if (hasText == hasExpression) {
			// Refused here rather than at the first run: a task whose message is either undecidable
			// or absent has nothing to do, and a scheduled task says so at boot or not at all.
			context.error("Task '" + config.getName() + "' must configure exactly one of '"
				+ Config.PROMPT + "' and '" + Config.PROMPT_EXPRESSION + "', not "
				+ (hasText ? "both" : "neither") + ".");
			_promptExpression = null;
		} else {
			_promptExpression = hasExpression ? QueryExecutor.compile(config.getPromptExpression()) : null;
		}
	}

	/**
	 * Runs on one cluster node only.
	 *
	 * <p>
	 * An agent run has effects - it writes, it sends, it costs money - so running it once per node
	 * would do all of that as often as the cluster is wide.
	 * </p>
	 */
	@Override
	public boolean isNodeLocal() {
		return false;
	}

	@Override
	protected void runHook() {
		if (!_running.compareAndSet(false, true)) {
			Logger.info("Task '" + getName() + "' skipped: its previous run is still working.",
				AgentTask.class);
			getLog().taskEnded(ResultType.WARNING, I18NConstants.RUN_SKIPPED_STILL_RUNNING);
			return;
		}
		try {
			Person account = account();
			if (account == null) {
				return;
			}
			// StateHandlingTask has already established a system interaction, and
			// TLContextManager.inPersonContext does nothing to an existing one - only its root
			// variant sets the person there. So the account is put onto this sub-session here and
			// taken off again afterwards, which is what that variant does one level down.
			TLSubSessionContext subSession = TLContextManager.getSubSession();
			Person previous = subSession.getPerson();
			subSession.setPerson(account);
			try {
				runSitting(account, subSession, newTracker(account));
			} finally {
				subSession.setPerson(previous);
			}
		} finally {
			_running.set(false);
		}
	}

	/**
	 * Puts this task's prompt to its agent and waits for the answer.
	 *
	 * <p>
	 * One sitting: everything the overlap guard brackets and everything the stop signal has to end.
	 * A subclass that wants a different shape of sitting - several prompts, a step before and after
	 * - replaces this and keeps the guard, the account binding and the scheduler's result handling.
	 * It is called with {@code account} already bound to {@code subSession}, and it reports the
	 * task's result itself.
	 * </p>
	 *
	 * @param account
	 *        The account the run acts as, already bound to {@code subSession}.
	 * @param subSession
	 *        The sub-session the agent's tools operate in - this task's own, since a scheduled run
	 *        has no application window.
	 * @param tracker
	 *        The chat state of the sitting, as {@link #newTracker(Person)} prepared it.
	 */
	protected void runSitting(Person account, TLSubSessionContext subSession,
			TaskProgressTracker tracker) {
		String prompt;
		try {
			prompt = prompt();
		} catch (RuntimeException ex) {
			Logger.error("Task '" + getName() + "': computing the prompt failed: " + ex.getMessage(), ex,
				AgentTask.class);
			getLog().taskEnded(ResultType.ERROR,
				I18NConstants.PROMPT_FAILED__MESSAGE.fill(message(ex)), ex);
			return;
		}
		if (isEmpty(prompt)) {
			Logger.warn("Task '" + getName() + "' has an empty prompt; nothing was sent.", AgentTask.class);
			getLog().taskEnded(ResultType.FAILURE, I18NConstants.PROMPT_EMPTY);
			return;
		}

		if (getConfig().isRecordSession() && !openSession(tracker, account)) {
			return;
		}

		SendResult sent = ChatProcessor.sendMessage(tracker, subSession, account.getName(), prompt, null,
			new ChatRunOptions(getConfig().getAgent(), null, false, List.of()));
		if (sent.isBusy()) {
			Logger.info("Task '" + getName() + "' skipped: its chat session is already processing.",
				AgentTask.class);
			getLog().taskEnded(ResultType.WARNING, I18NConstants.RUN_SKIPPED_SESSION_BUSY);
			return;
		}
		reportOutcome(tracker, awaitCompletion(tracker), sent.getTaskId());
	}

	/**
	 * The chat state of one sitting: what a browser window would have brought, made by the task
	 * because a scheduled run has none.
	 *
	 * <p>
	 * Only the approval mode and the account are settled here; the session is opened afterwards,
	 * because a task configured not to record one has no session at all.
	 * </p>
	 */
	protected TaskProgressTracker newTracker(Person account) {
		TaskProgressTracker tracker = new TaskProgressTracker();
		tracker.setUserName(account.getName());
		tracker.setApprovalMode(getConfig().getApprovalMode());
		// Only AUTO_APPROVE is truly unattended. MANUAL says the operator expects to answer, and
		// since #393 any window of this account can: the run is then an ordinary attended one that
		// happens to have been started by a clock.
		tracker.setUnattended(getConfig().getApprovalMode() == ApprovalMode.AUTO_APPROVE);
		return tracker;
	}

	/**
	 * Creates the chat session this run is recorded in and activates it on {@code tracker}.
	 *
	 * @return Whether the session is now the tracker's active one. A failure is reported as the
	 *         task's result, because an unrecorded run of a task configured to record one is not
	 *         the job that was asked for.
	 */
	private boolean openSession(TaskProgressTracker tracker, Person account) {
		TLObject session = ChatHistoryService.createSession(sessionTitle(), account);
		if (session == null) {
			getLog().taskEnded(ResultType.ERROR, I18NConstants.SESSION_NOT_CREATED);
			return false;
		}
		// The row was written in a transaction of its own, at a revision newer than this thread's
		// stable read view. Without this the session the tracker is about to activate cannot be
		// found, and the run would be recorded nowhere (Gitea #405 is the same revision boundary
		// seen from a tool).
		HistoryUtils.updateSessionRevision();
		String sessionId = session.tHandle().getObjectName().toExternalForm();
		if (!tracker.setActiveSessionId(sessionId, account)) {
			Logger.error("Task '" + getName() + "': the chat session just created for " + account.getName()
				+ " could not be activated.", AgentTask.class);
			getLog().taskEnded(ResultType.ERROR, I18NConstants.SESSION_NOT_CREATED);
			return false;
		}
		return true;
	}

	/**
	 * Waits for the agent to finish, or for this task to be stopped.
	 *
	 * <p>
	 * Package-visible for testing: what it does with a stopped task is worth pinning, and a test
	 * can hand it a tracker with a run in it without having a model to run.
	 * </p>
	 *
	 * @return Whether the agent finished by itself. {@code false} means the task was asked to stop
	 *         and the agent thread was interrupted.
	 */
	boolean awaitCompletion(TaskProgressTracker tracker) {
		while (ChatProcessor.isProcessing(tracker)) {
			if (getShouldStop()) {
				// The run holds this thread's sub-session, so it has to end before this method
				// returns and the sub-session is handed back. Interrupting is what /chat/cancel
				// does; the loop's cancellation token is not reachable from here without the run's
				// state, and the state is what the interrupt reaches anyway.
				interruptAgent(tracker);
				return false;
			}
			try {
				Thread.sleep(POLL_INTERVAL_MILLIS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				interruptAgent(tracker);
				return false;
			}
		}
		return true;
	}

	private void interruptAgent(TaskProgressTracker tracker) {
		AIProcessingState state = tracker.getAnyActiveProcessingState();
		Thread agent = state == null ? null : state.getAgentThread();
		if (agent != null) {
			Logger.info("Task '" + getName() + "' was stopped; interrupting its agent run.", AgentTask.class);
			agent.interrupt();
		}
	}

	/** Reports what the run did as this task's result. */
	private void reportOutcome(TaskProgressTracker tracker, boolean finished, String taskId) {
		if (!finished) {
			getLog().taskEnded(ResultType.CANCELED, I18NConstants.RUN_STOPPED);
			return;
		}
		AIProcessingState state = taskId == null ? null : tracker.getProcessingState(taskId);
		Throwable error = state == null ? null : state.getError();
		if (error != null) {
			Logger.error("Task '" + getName() + "': the agent run failed: " + error.getMessage(), error,
				AgentTask.class);
			getLog().taskEnded(ResultType.ERROR, I18NConstants.RUN_FAILED__MESSAGE.fill(message(error)), error);
			return;
		}
		Logger.info("Task '" + getName() + "' finished its agent run.", AgentTask.class);
	}

	/**
	 * The account the run acts as, or <code>null</code> if it cannot be resolved - in which case the
	 * task's result has been set already.
	 */
	private Person account() {
		String login = getConfig().getRunAs();
		if (isEmpty(login)) {
			return PersonManager.getManager().getRoot();
		}
		Person account = Person.byName(login.trim());
		if (account == null) {
			Logger.error("Task '" + getName() + "': there is no account '" + login + "' to run as.",
				AgentTask.class);
			getLog().taskEnded(ResultType.ERROR, I18NConstants.NO_SUCH_ACCOUNT__LOGIN.fill(login));
		}
		return account;
	}

	/**
	 * The message to put to the agent: the configured text, or the result of the configured
	 * expression evaluated now.
	 *
	 * <p>
	 * Package-visible so a test can ask what a configuration produces without running an agent.
	 * </p>
	 */
	String prompt() {
		if (_promptExpression == null) {
			return getConfig().getPrompt();
		}
		Object computed = _promptExpression.execute();
		return computed == null ? null : computed.toString();
	}

	/** The title the recorded session is created with. */
	private String sessionTitle() {
		String configured = getConfig().getSessionTitle();
		return isEmpty(configured) ? getName() : configured;
	}

	private static String message(Throwable ex) {
		String message = ex.getMessage();
		return message == null || message.isEmpty() ? ex.getClass().getSimpleName() : message;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isBlank();
	}
}
