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

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.job.JobBody;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;

/**
 * Tests the {@link JobState#result() result} of a job run by a {@link JobRunner} that was
 * cancelled: what its body returns after the cancel stays, a body that throws leaves none.
 */
public class TestJobRunnerCancelledResult extends TestCase {

	/** How long the test waits for a job before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	/** What the bodies of these tests got done before the cancel. */
	private static final String PARTIAL = "2 of 3 files";

	/**
	 * Tests that a cancelled body returning what it got done ends the job as cancelled with that
	 * result.
	 */
	public void testACancelledBodyReturningKeepsItsResult() throws Exception {
		JobState last = runCancelled((job, arguments) -> {
			try {
				job.checkCancelled();
				return "everything";
			} catch (AbortExecutionException ex) {
				return PARTIAL;
			}
		});

		assertEquals(JobStatus.CANCELLED, last.status());
		assertEquals(PARTIAL, last.result());
		assertNull(last.error());
	}

	/** Tests that a cancelled body that throws ends the job as cancelled without a result. */
	public void testACancelledBodyThrowingHasNoResult() throws Exception {
		JobState last = runCancelled((job, arguments) -> {
			job.checkCancelled();
			return "everything";
		});

		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull(last.result());
		assertNull(last.error());
	}

	/**
	 * Tests that a cancelled body failing on its way out ends the job as cancelled without a
	 * result.
	 */
	public void testACancelledBodyFailingHasNoResult() throws Exception {
		JobState last = runCancelled((job, arguments) -> {
			throw new IllegalStateException("broken");
		});

		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull(last.result());
		assertNull(last.error());
	}

	/**
	 * Runs the given body as a cancelable job, cancels it once it runs, lets the body go on and waits
	 * for the job to end.
	 */
	private static JobState runCancelled(JobBody body) throws InterruptedException {
		CountDownLatch running = new CountDownLatch(1);
		CountDownLatch cancelled = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);
		DefaultViewChannel channel = new DefaultViewChannel("job");
		JobRunner runner = new JobRunner(null, channel, List.of(), true, 0, state -> finished.countDown());
		runner.start((job, arguments) -> {
			running.countDown();
			AbstractJobTest.awaitQuietly(cancelled);
			return body.run(job, arguments);
		}, List.of());

		assertTrue(running.await(TIMEOUT, TimeUnit.MILLISECONDS));
		runner.cancel();
		cancelled.countDown();
		assertTrue("The job must finish within " + TIMEOUT + " ms.", finished.await(TIMEOUT, TimeUnit.MILLISECONDS));
		return (JobState) channel.get();
	}

	/**
	 * Test suite requiring the {@link TypeIndex}, the {@link SchedulerService} the job runs on, and
	 * the {@link ResourcesModule} the messages of a failure are resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestJobRunnerCancelledResult.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
