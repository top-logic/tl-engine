/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.sched;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatMessage;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.log.TransientTaskLog;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * What a scheduled {@link AgentTask} sitting is, apart from the model call in the middle of it
 * (issue #384).
 *
 * <p>
 * What the task contributes is everything <em>around</em> the model call: it resolves the account to
 * act as, binds it to the thread, creates and titles the chat session the sitting is recorded in,
 * declares the run unattended, sends the prompt and waits for the run to end. Each of those is
 * asserted here. What the model answers is not - that is the agent loop's business, and this test
 * has neither a key nor an agent definition to make it interesting.
 * </p>
 *
 * <p>
 * The chat state of a sitting is reached through {@link AgentTask#runSitting}, the task's own
 * extension point: a {@link ProbeTask} notes the tracker it is handed and lets the real sitting run.
 * </p>
 */
public class TestScheduledAgentRun extends BasicTestCase {

	private static Person account;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (account == null) {
			account = TestPerson.createPerson("scheduler-account");
		}
	}

	/** The sitting is a chat session of the {@code run-as} account, titled after the task. */
	public void testSittingIsRecordedAsATitledSession() throws Exception {
		int before = ChatHistoryService.getSessionsForUser(account).size();

		ProbeTask task = task("<task %s name='NightlySummary' run-as='" + account.getName() + "'>"
			+ "<prompt>Summarise today.</prompt></task>");
		task.drive();

		List<TLObject> sessions = ChatHistoryService.getSessionsForUser(account);
		assertEquals("The run gets a session of its own.", before + 1, sessions.size());
		assertEquals("NightlySummary", title(sessions.get(0)));
		assertEquals("The sitting is recorded against the account it ran as.",
			account, sessions.get(0).tValueByName("owner"));
	}

	/** A task that names its own title uses it instead of the task name. */
	public void testConfiguredSessionTitleWins() throws Exception {
		ProbeTask task = task("<task %s name='NightlySummary' run-as='" + account.getName() + "'"
			+ " session-title='Tageszusammenfassung'><prompt>Summarise today.</prompt></task>");
		task.drive();

		assertEquals("Tageszusammenfassung", title(ChatHistoryService.getSessionsForUser(account).get(0)));
	}

	/** The prompt reaches the conversation as the user message of the turn. */
	public void testThePromptIsWhatIsSent() throws Exception {
		ProbeTask task = task("<task %s name='Nightly' run-as='" + account.getName() + "'>"
			+ "<prompt>Summarise the invoices recorded today.</prompt></task>");
		task.drive();

		assertEquals("Summarise the invoices recorded today.", sentMessage(task));
	}

	/**
	 * A TL-Script prompt is evaluated when the task runs, not when it is configured.
	 *
	 * <p>
	 * The reason the property exists: a scheduled prompt that has to name what the application
	 * currently holds - "these are the ones already done, take the rest" - is only right if it is
	 * computed at that moment. Asserted by changing the data between two evaluations.
	 * </p>
	 */
	public void testPromptExpressionFollowsTheData() throws Exception {
		ProbeTask task = task("<task %s name='Nightly'><prompt-expression>"
			+ "\"Sessions: \" + all(`tl.ai.execution:AgentSession`).size()"
			+ "</prompt-expression></task>");

		String before = task.prompt();
		ChatHistoryService.createSession("one more", account);
		// The row was written in a transaction of its own; this thread's read view has to be moved
		// past it before the expression can see it - the same step the task itself takes after
		// creating the session it records into.
		HistoryUtils.updateSessionRevision();

		assertFalse("The expression must be evaluated per run, not compiled into a fixed text: "
			+ before, before.equals(task.prompt()));
	}

	/**
	 * A run started while the previous one is still working is skipped, and says so.
	 *
	 * <p>
	 * An agent sitting can take longer than the interval it is scheduled at. Without the guard the
	 * second run would open a second session and drive the same conversation state from two
	 * threads.
	 * </p>
	 */
	public void testOverlappingRunIsSkipped() throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		BlockingTask task = new BlockingTask(instantiationContext(),
			config("<task %s name='Nightly' run-as='" + account.getName() + "'>"
				+ "<prompt>Summarise today.</prompt></task>"),
			started, release);

		// The sitting resolves an account and reads the knowledge base, so the thread that drives it
		// needs a TopLogic context - the scheduler gives its task one.
		Thread first = new Thread(
			() -> ThreadContext.inSystemContext(TestScheduledAgentRun.class, task::drive),
			"TestScheduledAgentRun-first");
		first.setDaemon(true);
		first.start();
		try {
			assertTrue("The first run did not reach its sitting.", started.await(10, TimeUnit.SECONDS));

			task.drive();

			assertEquals("The second run must be skipped, not queued and not run.",
				ResultType.WARNING, resultType(task));
			assertEquals(1, task.sittings());
		} finally {
			release.countDown();
			first.join(TimeUnit.SECONDS.toMillis(10));
		}
	}

	/** In the default approval mode the run declares that it has nobody to ask. */
	public void testAutoApproveRunIsUnattended() throws Exception {
		ProbeTask task = task("<task %s name='Nightly' run-as='" + account.getName() + "'>"
			+ "<prompt>Summarise today.</prompt></task>");
		task.drive();

		TaskProgressTracker tracker = tracker(task);
		assertEquals(ApprovalMode.AUTO_APPROVE, tracker.getApprovalMode());
		assertTrue("Nothing can answer this run, so askUser must fail rather than park it.",
			tracker.isUnattended());
	}

	/**
	 * A task configured for manual approval is an attended run.
	 *
	 * <p>
	 * It declares that a person will answer, and since #393 any window of the {@code run-as}
	 * account can. Such a run keeps the approval prompt as its bound and is therefore not given the
	 * background ceiling, and {@code askUser} still works in it.
	 * </p>
	 */
	public void testManualApprovalRunIsAttended() throws Exception {
		ProbeTask task = task("<task %s name='Nightly' run-as='" + account.getName()
			+ "' approval-mode='MANUAL'><prompt>Summarise today.</prompt></task>");
		task.drive();

		TaskProgressTracker tracker = tracker(task);
		assertEquals(ApprovalMode.MANUAL, tracker.getApprovalMode());
		assertFalse(tracker.isUnattended());
	}

	/**
	 * A task with {@code record-session='false'} runs, and leaves no session behind.
	 *
	 * <p>
	 * The run still happens - it fails here only for want of a model, which is what the reported
	 * error is - but nothing of it is kept: the chat state of a tracker with no active session is
	 * transient, so there is no transcript to open afterwards. That is the whole of what switching
	 * the option off costs, and the reason it is on by default.
	 * </p>
	 */
	public void testUnrecordedRunCreatesNoSession() throws Exception {
		int before = ChatHistoryService.getSessionsForUser(account).size();

		ProbeTask task = task("<task %s name='Nightly' run-as='" + account.getName()
			+ "' record-session='false'><prompt>Summarise today.</prompt></task>");
		task.drive();

		assertEquals(before, ChatHistoryService.getSessionsForUser(account).size());
		assertNull("Nothing is recorded, so the tracker has no session either.",
			tracker(task).getActiveSessionId());
	}

	/** A task naming an account that does not exist fails, and names it. */
	public void testUnknownAccountFails() throws Exception {
		ProbeTask task = task("<task %s name='Nightly' run-as='nobody-by-that-name'>"
			+ "<prompt>Summarise today.</prompt></task>");
		task.drive();

		assertEquals(ResultType.ERROR, resultType(task));
		assertNull("Nothing may be started for an account that does not exist.", task.tracker());
	}

	/**
	 * Being stopped ends the run rather than waiting it out.
	 *
	 * <p>
	 * The scheduler asks a task to stop and then waits for it; this task has handed its work to
	 * another thread, so it has to pass the signal on. Driven with a run that is simply parked,
	 * because what the agent thread is doing when it is interrupted does not matter here.
	 * </p>
	 */
	public void testStopInterruptsTheAgentThread() throws Exception {
		ProbeTask task = task("<task %s name='Nightly'><prompt>Summarise today.</prompt></task>");
		task.stop();

		TaskProgressTracker tracker = new TaskProgressTracker();
		AIProcessingState run = new AIProcessingState();
		run.setCurrentTaskId(UUID.randomUUID().toString());
		run.startProcessing();
		tracker.addProcessingState(run.getCurrentTaskId(), run);

		CountDownLatch interrupted = new CountDownLatch(1);
		Thread agent = new Thread(() -> {
			try {
				Thread.sleep(TimeUnit.MINUTES.toMillis(5));
			} catch (InterruptedException ex) {
				interrupted.countDown();
			}
		}, "TestScheduledAgentRun-agent");
		agent.setDaemon(true);
		agent.start();
		run.setAgentThread(agent);
		try {
			assertFalse("A stopped task must report that the run did not finish.",
				task.awaitCompletion(tracker));
			assertTrue("The agent thread has to be interrupted; nothing else ends the run.",
				interrupted.await(10, TimeUnit.SECONDS));
		} finally {
			agent.interrupt();
		}
	}

	/** The message the sitting sent, read from the chat its tracker holds. */
	private static String sentMessage(ProbeTask task) {
		List<ChatMessage> messages = tracker(task).getChat().getMessages();
		assertFalse("Nothing was sent.", messages.isEmpty());
		return messages.get(0).getText();
	}

	/** The chat state of the sitting that just ran. */
	private static TaskProgressTracker tracker(ProbeTask task) {
		TaskProgressTracker tracker = task.tracker();
		assertNotNull("No sitting ran.", tracker);
		return tracker;
	}

	private static String title(TLObject session) {
		return (String) session.tValueByName("title");
	}

	private static ResultType resultType(ProbeTask task) {
		TaskResult result = task.getLog().getLastResult();
		assertNotNull("The task reported no result.", result);
		return result.getResultType();
	}

	private static ProbeTask task(String xml) throws ConfigurationException {
		return new ProbeTask(instantiationContext(), config(xml));
	}

	private static InstantiationContext instantiationContext() {
		return new DefaultInstantiationContext(TestScheduledAgentRun.class);
	}

	private static AgentTask.Config config(String xml) throws ConfigurationException {
		String attributes = "xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + AgentTask.Config.class.getName() + "'";
		return (AgentTask.Config) TypedConfiguration.parse(
			CharacterContents.newContent(String.format(xml, attributes)));
	}

	/**
	 * An {@link AgentTask} with the two things the scheduler would otherwise supply: a task log to
	 * report into, and an answer to "were you asked to stop?".
	 */
	static class ProbeTask extends AgentTask {

		private final TaskLog _log = new TransientTaskLog(this);

		private volatile boolean _stop;

		private volatile TaskProgressTracker _tracker;

		ProbeTask(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected void runSitting(Person runAs, TLSubSessionContext subSession,
				TaskProgressTracker tracker) {
			_tracker = tracker;
			super.runSitting(runAs, subSession, tracker);
		}

		/** The chat state of the last sitting, or <code>null</code> if none ran. */
		TaskProgressTracker tracker() {
			return _tracker;
		}

		@Override
		public TaskLog getLog() {
			return _log;
		}

		@Override
		protected synchronized boolean getShouldStop() {
			return _stop;
		}

		/** Asks this task to stop, as {@code Scheduler.stopTask} does. */
		void stop() {
			_stop = true;
		}

		/**
		 * Runs the task the way {@code StateHandlingTask} runs it: the log is opened before and,
		 * when the run reported no result of its own, closed as a success afterwards.
		 */
		void drive() {
			_log.taskStarted();
			runHook();
			TaskResult current = _log.getCurrentResult();
			if (current != null && current.getResultType() == ResultType.NOT_FINISHED) {
				_log.taskEnded(ResultType.SUCCESS, ResultType.SUCCESS.getMessageI18N());
			}
		}
	}

	/** A {@link ProbeTask} whose sitting stays in flight until it is released. */
	static class BlockingTask extends ProbeTask {

		private final CountDownLatch _started;

		private final CountDownLatch _release;

		private volatile int _sittings;

		BlockingTask(InstantiationContext context, Config config, CountDownLatch started,
				CountDownLatch release) {
			super(context, config);
			_started = started;
			_release = release;
		}

		@Override
		protected void runSitting(Person runAs, TLSubSessionContext subSession,
				TaskProgressTracker tracker) {
			_sittings++;
			_started.countDown();
			try {
				_release.await(30, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		/** How often the guarded sitting was entered. */
		int sittings() {
			return _sittings;
		}
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestScheduledAgentRun.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMService.Module.INSTANCE));
	}
}
