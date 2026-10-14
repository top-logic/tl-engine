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

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;

/**
 * Tests that the snapshot ending a job run by a {@link JobRunner} is the last value its channel
 * holds, even when a report held back by the update interval competes with it.
 *
 * <p>
 * The test holds the delivery of the first report inside the channel write until the body has
 * returned and the job has ended. At that moment a second report is held back by the update
 * interval and the snapshot ending the job is waiting for the running delivery to complete.
 * </p>
 */
public class TestJobRunnerDeliveryOrder extends TestCase {

	/** How long the test waits for any step before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	/**
	 * The shortest time between two published snapshots, longer than the test waits: the end of the
	 * job does not wait for it.
	 */
	private static final long UPDATE_INTERVAL = 60_000;

	/** What the body of the job produces. */
	private static final String RESULT = "result";

	/**
	 * Tests that the snapshot ending the job is delivered without waiting for the update interval,
	 * replaces the report held back before it, and is the last value of the channel.
	 */
	public void testTerminalSnapshotIsTheLastValue() throws Exception {
		BlockingChannel channel = new BlockingChannel();
		CountDownLatch finished = new CountDownLatch(1);
		JobRunner runner = new JobRunner(null, channel, List.of(), false, UPDATE_INTERVAL,
			state -> finished.countDown());

		runner.start((job, arguments) -> {
			channel._worker = Thread.currentThread();
			job.fraction(0.1);
			assertTrue("The first report must start writing the channel.",
				channel._firstReportWriting.await(TIMEOUT, TimeUnit.MILLISECONDS));
			job.fraction(0.2);
			channel._bodyReturning.countDown();
			return RESULT;
		}, List.of());

		assertTrue("The job must finish within " + TIMEOUT + " ms.",
			finished.await(TIMEOUT, TimeUnit.MILLISECONDS));

		List<JobState> received = List.copyOf(channel._received);
		assertEquals("The initial snapshot, the first report, and the end: " + received, 3, received.size());
		assertEquals(Double.valueOf(0.1), received.get(1).fraction());
		JobState last = (JobState) channel.get();
		assertSame(received.get(2), last);
		assertEquals("The channel keeps the snapshot that ended the job.", JobStatus.COMPLETED, last.status());
		assertEquals(RESULT, last.result());
	}

	/**
	 * {@link DefaultViewChannel} that holds the write of the first report until the body has
	 * returned and the worker has ended the job.
	 */
	private static class BlockingChannel extends DefaultViewChannel {

		/** The thread the body runs on, once it has started. */
		volatile Thread _worker;

		/** Every value written, in the order written. */
		final List<JobState> _received = Collections.synchronizedList(new ArrayList<>());

		/** Counted down once the write of the first report has started. */
		final CountDownLatch _firstReportWriting = new CountDownLatch(1);

		/** Counted down by the body right before it returns. */
		final CountDownLatch _bodyReturning = new CountDownLatch(1);

		BlockingChannel() {
			super("job");
		}

		@Override
		public boolean set(Object newValue) {
			JobState state = (JobState) newValue;
			Thread worker = _worker;
			if (worker != null && state.isRunning() && _firstReportWriting.getCount() > 0) {
				_firstReportWriting.countDown();
				awaitQuietly(_bodyReturning);
				awaitJobEnded(worker);
			}
			_received.add(state);
			return super.set(newValue);
		}

		/**
		 * Waits until the worker has left the body and the end of the job, which it handed to the
		 * publisher without waiting for this delivery.
		 */
		private static void awaitJobEnded(Thread worker) {
			long end = System.currentTimeMillis() + TIMEOUT;
			while (System.currentTimeMillis() < end && worker.getState() == Thread.State.RUNNABLE) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}

		private static void awaitQuietly(CountDownLatch latch) {
			try {
				latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the {@link SchedulerService} the job runs on.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestJobRunnerDeliveryOrder.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}
}
