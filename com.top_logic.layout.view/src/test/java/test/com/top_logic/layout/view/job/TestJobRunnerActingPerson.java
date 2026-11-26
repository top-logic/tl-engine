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

import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.util.ActingPerson;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Tests that a job started by a {@link JobRunner} acts for the {@link ActingPerson} of the request
 * that started it: its body, and the work that follows the job.
 */
public class TestJobRunnerActingPerson extends BasicTestCase {

	/** How long the test waits for any step before it counts as hanging. */
	private static final long TIMEOUT = 60_000;

	private Person _person;

	private TLContext _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_person = TestPerson.createPerson("jobActingPersonUser");

		ThreadContextManager manager = TLContextManager.getManager();
		_window = (TLContext) manager.newSubSessionContext();
		_window.setSessionContext(manager.newSessionContext());
		_window.setPerson(_person);
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_person);
		_person = null;
		_window = null;
		super.tearDown();
	}

	/** A job that a present person starts acts for this person until it ends. */
	public void testJobActsForThePersonWhoStartedIt() throws Exception {
		Seen seen = runJob(_person);

		assertEquals(JobStatus.COMPLETED, seen.end().status());
		assertSame("The body acts for the person who started the job.", _person, seen.body());
		assertSame("The work after the job acts for the person who started the job.", _person, seen.finished());
	}

	/** A job started in a request without an acting person has none. */
	public void testJobWithoutActingPerson() throws Exception {
		Seen seen = runJob(null);

		assertEquals(JobStatus.COMPLETED, seen.end().status());
		assertNull(seen.body());
		assertNull(seen.finished());
	}

	/**
	 * What a job saw: the acting person of its body and of the work after it, and its last state.
	 */
	private record Seen(Person body, Person finished, JobState end) {
		// Plain data.
	}

	/**
	 * Starts a job from a request of {@link #_window} with the given acting person and waits for
	 * the job to end.
	 */
	private Seen runJob(Person acting) throws Exception {
		AtomicReference<Person> body = new AtomicReference<>();
		AtomicReference<Person> finished = new AtomicReference<>();
		CountDownLatch done = new CountDownLatch(1);
		DefaultViewChannel channel = new DefaultViewChannel("job");
		inNewThread(() -> ThreadContextManager.inContext(_window, () -> {
			try (ThreadLocalBinding<Person>.Handle binding = ActingPerson.bind(acting)) {
				new JobRunner(null, channel, List.of(), false, 0, state -> {
					finished.set(ActingPerson.handOver());
					done.countDown();
				}).start((job, arguments) -> {
					body.set(ActingPerson.handOver());
					return null;
				}, List.of());
			}
			return null;
		}));
		assertTrue("Timeout waiting for the job.", done.await(TIMEOUT, TimeUnit.MILLISECONDS));
		return new Seen(body.get(), finished.get(), (JobState) channel.get());
	}

	private static <T> T inNewThread(Callable<T> job) throws Exception {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestJobRunnerActingPerson.class.getSimpleName());
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

	/**
	 * Test suite requiring a knowledge base with persons and the {@link SchedulerService} the job
	 * runs on.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestJobRunnerActingPerson.class),
				SchedulerService.Module.INSTANCE));
	}

}
