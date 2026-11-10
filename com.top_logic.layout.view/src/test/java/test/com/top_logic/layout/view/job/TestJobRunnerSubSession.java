/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.util.ItemByNameCache;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.job.I18NConstants;
import com.top_logic.layout.view.job.JobBody;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the sub-session the body of a job started by a {@link JobRunner} runs in.
 *
 * <p>
 * The window the job is started from keeps serving its requests while the body runs, so the body
 * must not share the window's sub-session.
 * </p>
 */
public class TestJobRunnerSubSession extends BasicTestCase {

	/** How long the test waits for any step before it counts as hanging. */
	private static final long TIMEOUT = 60_000;

	private Person _person;

	private Person _committed;

	private TLContext _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_person = TestPerson.createPerson("jobSubSessionUser");

		ThreadContextManager manager = TLContextManager.getManager();
		_window = (TLContext) manager.newSubSessionContext();
		_window.setSessionContext(manager.newSessionContext());
		_window.setPerson(_person);
	}

	@Override
	protected void tearDown() throws Exception {
		if (_committed != null) {
			Person committed = _committed;
			_committed = null;
			// The session revision of the test thread does not yet contain the commit of the window:
			// delete in a new interaction.
			inNewThread(() -> ThreadContext.inSystemContext(TestJobRunnerSubSession.class, () -> {
				TestPerson.deletePersonAndUser(committed);
				return null;
			}));
		}
		TestPerson.deletePersonAndUser(_person);
		_person = null;
		_window = null;
		super.tearDown();
	}

	/**
	 * Tests that the body runs in a sub-session derived from the one of the starting request, with
	 * the user of that request.
	 */
	public void testBodyRunsInDerivedSubSession() throws Exception {
		AtomicReference<TLContext> bodyContext = new AtomicReference<>();
		JobState end = runJob((job, arguments) -> {
			bodyContext.set(TLContext.getContext());
			return null;
		});

		assertEquals(JobStatus.COMPLETED, end.status());
		TLContext context = bodyContext.get();
		assertNotNull(context);
		assertNotSame("The body must not share the sub-session of the window.", _window, context);
		assertSame("The body runs for the window it was started from.", _window, context.getOrigin());
		assertSame(_person, context.getPerson());
	}

	/**
	 * Tests that a failure of the body is reported with its messages from within the derived
	 * sub-session, where they are resolved for the log entry in the language of the user.
	 */
	public void testFailureIsReportedFromDerivedSubSession() throws Exception {
		JobState end = runJob((job, arguments) -> {
			throw new TopLogicException(com.top_logic.util.model.I18NConstants.CONSTRAINTS_VIOLATED,
				new TopLogicException(I18NConstants.ERROR_NO_JOB_TO_REPORT_TO)).initSeverity(ErrorSeverity.ERROR);
		});

		assertEquals(JobStatus.FAILED, end.status());
		assertEquals(com.top_logic.util.model.I18NConstants.CONSTRAINTS_VIOLATED, end.error());
		assertEquals(List.of(I18NConstants.ERROR_NO_JOB_TO_REPORT_TO), end.errorDetails());
	}

	/**
	 * Tests that a body still reads the knowledge base after the window has committed while the body
	 * was running.
	 */
	public void testCommitOfWindowWhileBodyRuns() throws Exception {
		DBKnowledgeBase kb = (DBKnowledgeBase) PersistencyLayer.getKnowledgeBase();
		CountDownLatch revisionPinned = new CountDownLatch(1);
		CountDownLatch commitDone = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);
		DefaultViewChannel channel = new DefaultViewChannel("job");

		inNewThread(() -> ThreadContextManager.inContext(_window, () -> {
			JobRunner runner = new JobRunner(null, channel, List.of(), false, 0, state -> finished.countDown());
			runner.start((job, arguments) -> {
				kb.getSessionRevision();
				revisionPinned.countDown();
				await(commitDone);

				ItemByNameCache<String> cache =
					new ItemByNameCache<>(kb, Person.OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, String.class);
				try {
					assertNotNull(cache.lookup(_person.getName()));
				} finally {
					cache.invalidate();
				}
				return null;
			}, List.of());
			return null;
		}));

		await(revisionPinned);
		inNewThread(() -> ThreadContextManager.inContext(_window, () -> {
			_committed = TestPerson.createPerson("jobSubSessionCommitted");
			return null;
		}));
		commitDone.countDown();

		await(finished);
		JobState end = (JobState) channel.get();
		assertEquals("The job must not fail: " + end.error(), JobStatus.COMPLETED, end.status());
	}

	/**
	 * Starts the given body from a request of {@link #_window} and waits for the job to end.
	 */
	private JobState runJob(JobBody body) throws Exception {
		CountDownLatch finished = new CountDownLatch(1);
		DefaultViewChannel channel = new DefaultViewChannel("job");
		inNewThread(() -> ThreadContextManager.inContext(_window, () -> {
			new JobRunner(null, channel, List.of(), false, 0, state -> finished.countDown())
				.start(body, List.of());
			return null;
		}));
		await(finished);
		return (JobState) channel.get();
	}

	private static <T> T inNewThread(Callable<T> job) throws Exception {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestJobRunnerSubSession.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		try {
			return task.get(TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof Error error) {
				throw error;
			}
			throw (Exception) cause;
		}
	}

	private static void await(CountDownLatch latch) {
		try {
			assertTrue("Timeout waiting for another thread.", latch.await(TIMEOUT, TimeUnit.MILLISECONDS));
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Test suite requiring a knowledge base with persons and the {@link SchedulerService} the job
	 * runs on.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestJobRunnerSubSession.class),
				SchedulerService.Module.INSTANCE));
	}

}
