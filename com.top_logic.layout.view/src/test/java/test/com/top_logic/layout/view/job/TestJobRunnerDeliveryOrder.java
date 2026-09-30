/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

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
 * Tests that a {@link JobRunner} publishes its snapshots in the order it took them, so that the
 * snapshot ending a job is the last one the channel receives.
 *
 * <p>
 * The test holds the flush of a held-back report inside the channel write until the body has
 * returned, which is the moment a snapshot taken while the job was running competes with the one
 * ending it.
 * </p>
 */
public class TestJobRunnerDeliveryOrder extends TestCase {

	/** How long the test waits for any step before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	/** The shortest time between two published snapshots, long enough to hold a report back. */
	private static final long UPDATE_INTERVAL = 100;

	/** What the body of the job produces. */
	private static final String RESULT = "result";

	/**
	 * Tests that a flush that took its snapshot while the job was running does not overwrite the
	 * snapshot ending the job.
	 */
	public void testFlushDoesNotOverwriteTheTerminalSnapshot() throws Exception {
		BlockingChannel channel = new BlockingChannel();
		CountDownLatch finished = new CountDownLatch(1);
		JobRunner runner = new JobRunner(null, channel, List.of(), false, UPDATE_INTERVAL,
			state -> finished.countDown());

		runner.start((job, arguments) -> {
			channel._worker = Thread.currentThread();
			job.fraction(0.1);
			job.fraction(0.2);
			assertTrue("The flush of the held-back report must start writing the channel.",
				channel._flushWriting.await(TIMEOUT, TimeUnit.MILLISECONDS));
			channel._bodyReturning.countDown();
			return RESULT;
		}, List.of());

		assertTrue("The job must finish within " + TIMEOUT + " ms.",
			finished.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertTrue("The flush must leave the channel within " + TIMEOUT + " ms.",
			channel._flushLeft.await(TIMEOUT, TimeUnit.MILLISECONDS));

		JobState last = (JobState) channel.get();
		assertEquals("The channel keeps the snapshot that ended the job.", JobStatus.COMPLETED, last.status());
		assertEquals(RESULT, last.result());
	}

	/**
	 * {@link DefaultViewChannel} that holds the write of a running state from a thread other than
	 * the worker - the flush - until the body has returned and the job has either published its end
	 * or waits to publish it.
	 */
	private static class BlockingChannel extends DefaultViewChannel {

		/** The thread the body runs on, once it has started. */
		volatile Thread _worker;

		/** Counted down once the flush has entered {@link #set(Object)}. */
		final CountDownLatch _flushWriting = new CountDownLatch(1);

		/** Counted down by the body right before it returns. */
		final CountDownLatch _bodyReturning = new CountDownLatch(1);

		/** Counted down once the terminal snapshot has been written. */
		final CountDownLatch _terminalWritten = new CountDownLatch(1);

		/** Counted down once the flush has left {@link #set(Object)}. */
		final CountDownLatch _flushLeft = new CountDownLatch(1);

		BlockingChannel() {
			super("job");
		}

		@Override
		public boolean set(Object newValue) {
			Thread worker = _worker;
			JobState state = (JobState) newValue;
			boolean flush = worker != null && Thread.currentThread() != worker && state.isRunning();
			if (!flush) {
				try {
					return super.set(newValue);
				} finally {
					if (state.isFinished()) {
						_terminalWritten.countDown();
					}
				}
			}
			try {
				_flushWriting.countDown();
				awaitQuietly(_bodyReturning);
				awaitTerminalWrittenOrBlocked(worker);
				return super.set(newValue);
			} finally {
				_flushLeft.countDown();
			}
		}

		/**
		 * Waits until the worker has written the terminal snapshot or waits for the flush to finish
		 * its delivery.
		 */
		private void awaitTerminalWrittenOrBlocked(Thread worker) {
			long end = System.currentTimeMillis() + TIMEOUT;
			while (System.currentTimeMillis() < end) {
				if (worker.getState() == Thread.State.BLOCKED) {
					return;
				}
				try {
					if (_terminalWritten.await(5, TimeUnit.MILLISECONDS)) {
						return;
					}
				} catch (InterruptedException ex) {
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
