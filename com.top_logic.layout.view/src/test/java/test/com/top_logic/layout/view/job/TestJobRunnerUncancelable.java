/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.job.JobBody;
import com.top_logic.layout.view.job.JobMonitor;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;

/**
 * Tests the {@link JobMonitor#uncancelable() point of no return} of a job run by a
 * {@link JobRunner}: a cancel before it ends the job as cancelled there, a cancel after it is
 * refused and the job ends with what its body returns.
 */
public class TestJobRunnerUncancelable extends TestCase {

	/** How long the test waits for a job before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	/** What a body produces. */
	private static final String RESULT = "result";

	/** The channel the job reports on. */
	private DefaultViewChannel _channel;

	/** Every state the job published, in the order it published them. */
	private final List<JobState> _states = Collections.synchronizedList(new ArrayList<>());

	/** Counted down by the state that ends the job. */
	private CountDownLatch _finished;

	/** Counted down by each of the first two states the job published. */
	private CountDownLatch _twoStates;

	/** Whether the body got past its point of no return. */
	private final AtomicBoolean _passed = new AtomicBoolean();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_finished = new CountDownLatch(1);
		_twoStates = new CountDownLatch(2);
		_channel = new DefaultViewChannel("job");
		_channel.addListener((sender, oldValue, newValue) -> {
			_states.add((JobState) newValue);
			_twoStates.countDown();
		});
	}

	/**
	 * Tests that a job cancelled before its point of no return ends there as cancelled, without
	 * the body running past it.
	 */
	public void testACancelBeforeThePointOfNoReturnEndsTheJob() throws Exception {
		CountDownLatch running = new CountDownLatch(1);
		CountDownLatch cancelled = new CountDownLatch(1);
		JobRunner runner = runner(true);
		runner.start((job, arguments) -> {
			running.countDown();
			AbstractJobTest.awaitQuietly(cancelled);
			job.uncancelable();
			_passed.set(true);
			return RESULT;
		}, List.of());

		assertTrue(running.await(TIMEOUT, TimeUnit.MILLISECONDS));
		runner.cancel();
		cancelled.countDown();

		JobState last = awaitFinished();
		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull(last.result());
		assertFalse("The body must not run past its point of no return.", _passed.get());
	}

	/**
	 * Tests that a cancel after the point of no return is refused, the reader is no longer offered
	 * to stop the job, and the job ends with the result of its body.
	 */
	public void testACancelAfterThePointOfNoReturnIsRefused() throws Exception {
		CountDownLatch passed = new CountDownLatch(1);
		CountDownLatch proceed = new CountDownLatch(1);
		JobRunner runner = runner(true);
		assertTrue(runner.isCancelable());
		runner.start((job, arguments) -> {
			job.uncancelable();
			passed.countDown();
			AbstractJobTest.awaitQuietly(proceed);
			return RESULT;
		}, List.of());

		assertTrue(passed.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertFalse(runner.isCancelable());
		assertTrue("The point of no return must be published within " + TIMEOUT + " ms.",
			_twoStates.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertEquals("The point of no return publishes a snapshot of its own.", 2, _states.size());
		assertTrue(_states.get(0).isCancelable());
		assertTrue(_states.get(1).isRunning());
		assertFalse(_states.get(1).isCancelable());

		runner.cancel();
		proceed.countDown();

		JobState last = awaitFinished();
		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals(RESULT, last.result());
	}

	/**
	 * Tests that a cancel reaching the job right before its point of no return ends the job there.
	 */
	public void testACancelRightBeforeThePointOfNoReturnEndsTheJob() throws Exception {
		JobRunner runner = runner(true);
		runner.start((job, arguments) -> {
			runner.cancel();
			job.uncancelable();
			_passed.set(true);
			return RESULT;
		}, List.of());

		JobState last = awaitFinished();
		assertEquals(JobStatus.CANCELLED, last.status());
		assertFalse("The body must not run past its point of no return.", _passed.get());
	}

	/**
	 * Tests that a cancel reaching the job right after its point of no return is refused.
	 */
	public void testACancelRightAfterThePointOfNoReturnIsRefused() throws Exception {
		JobRunner runner = runner(true);
		runner.start((job, arguments) -> {
			job.uncancelable();
			runner.cancel();
			job.checkCancelled();
			job.message(null);
			return RESULT;
		}, List.of());

		JobState last = awaitFinished();
		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals(RESULT, last.result());
	}

	/** Tests that a body failing past its point of no return ends the job as failed. */
	public void testAFailurePastThePointOfNoReturnFailsTheJob() throws Exception {
		JobRunner runner = runner(true);
		runner.start((job, arguments) -> {
			job.uncancelable();
			runner.cancel();
			throw new IllegalStateException("broken");
		}, List.of());

		JobState last = awaitFinished();
		assertEquals(JobStatus.FAILED, last.status());
	}

	/**
	 * Tests that the point of no return can be declared again, and by a job that is not cancelable
	 * at all.
	 */
	public void testThePointOfNoReturnIsIdempotent() throws Exception {
		JobRunner runner = runner(false);
		runner.start(body((job, arguments) -> {
			job.uncancelable();
			job.uncancelable();
			return RESULT;
		}), List.of());

		JobState last = awaitFinished();
		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals(RESULT, last.result());
	}

	/** A runner reporting on the channel of this test. */
	private JobRunner runner(boolean cancelable) {
		return new JobRunner(null, _channel, List.of(), cancelable, 0, state -> _finished.countDown());
	}

	/** The given work, written where the compiler sees which interface it implements. */
	private static JobBody body(JobBody body) {
		return body;
	}

	/** Waits for the job to end and returns its last state. */
	private JobState awaitFinished() throws InterruptedException {
		assertTrue("The job must finish within " + TIMEOUT + " ms.", _finished.await(TIMEOUT, TimeUnit.MILLISECONDS));
		return (JobState) _channel.get();
	}

	/**
	 * Test suite requiring the {@link TypeIndex}, the {@link SchedulerService} the job runs on, and
	 * the {@link ResourcesModule} the messages of a failure are resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestJobRunnerUncancelable.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
