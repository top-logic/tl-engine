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

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.job.I18NConstants;
import com.top_logic.layout.view.job.JobBody;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests how a {@link JobRunner} reports a failure of its body: the summary as the
 * {@link JobState#error() error}, the problems chained below it as the
 * {@link JobState#errorDetails() details}.
 *
 * <p>
 * A failure is also logged on the worker thread with its messages resolved; a job whose failure
 * could not be logged would never end, so every failing job that ends here shows that the log entry
 * was written.
 * </p>
 */
public class TestJobRunnerFailure extends TestCase {

	/** How long the test waits for a job before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	/** The summary of a failure with details. */
	private static final ResKey TITLE = com.top_logic.util.model.I18NConstants.CONSTRAINTS_VIOLATED;

	/** The first problem behind {@link #TITLE}. */
	private static final ResKey PROBLEM_1 = I18NConstants.ERROR_NO_JOB_TO_REPORT_TO;

	/** The second problem behind {@link #TITLE}. */
	private static final ResKey PROBLEM_2 = I18NConstants.ERROR_UNKNOWN_JOB_PHASE__NAME_PHASES.fill("x", "y");

	/** What a successful body produces. */
	private static final String RESULT = "result";

	/**
	 * Tests that the problems chained below the summary of a failure become the details of the
	 * error.
	 */
	public void testTheChainedProblemsAreTheDetails() throws Exception {
		JobState last = runJob((job, arguments) -> {
			throw failureWithProblems();
		});

		assertEquals(JobStatus.FAILED, last.status());
		assertEquals(TITLE, last.error());
		assertEquals(List.of(PROBLEM_1, PROBLEM_2), last.errorDetails());
	}

	/**
	 * Tests that a failure with a message for the user is found below a technical exception that
	 * wraps it.
	 */
	public void testAWrappedFailureIsReportedWithItsMessages() throws Exception {
		JobState last = runJob((job, arguments) -> {
			throw new RuntimeException("wrapper", failureWithProblems());
		});

		assertEquals(JobStatus.FAILED, last.status());
		assertEquals(TITLE, last.error());
		assertEquals(List.of(PROBLEM_1, PROBLEM_2), last.errorDetails());
	}

	/**
	 * Tests that a failure without a message for the user is reported generically, naming its
	 * technical message, and without details.
	 */
	public void testAnUnexpectedFailureIsReportedGenerically() throws Exception {
		JobState last = runJob((job, arguments) -> {
			throw new IllegalStateException("broken");
		});

		assertEquals(JobStatus.FAILED, last.status());
		assertEquals(I18NConstants.ERROR_JOB_FAILED__MESSAGE.fill("broken"), last.error());
		assertEquals(List.of(), last.errorDetails());
	}

	/** Tests that a completed job has no error details. */
	public void testACompletedJobHasNoDetails() throws Exception {
		JobState last = runJob((job, arguments) -> RESULT);

		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals(RESULT, last.result());
		assertNull(last.error());
		assertEquals(List.of(), last.errorDetails());
	}

	/** Tests that a cancelled job has no error, even when its body fails on its way out. */
	public void testACancelledJobHasNoDetails() throws Exception {
		CountDownLatch running = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);
		DefaultViewChannel channel = new DefaultViewChannel("job");
		JobRunner runner = new JobRunner(null, channel, List.of(), true, 0, state -> finished.countDown());
		runner.start((job, arguments) -> {
			running.countDown();
			try {
				Thread.sleep(TIMEOUT);
			} catch (InterruptedException ex) {
				// Woken by the cancellation.
			}
			throw failureWithProblems();
		}, List.of());

		assertTrue(running.await(TIMEOUT, TimeUnit.MILLISECONDS));
		runner.cancel();
		assertTrue("The job must finish within " + TIMEOUT + " ms.", finished.await(TIMEOUT, TimeUnit.MILLISECONDS));

		JobState last = (JobState) channel.get();
		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull(last.error());
		assertEquals(List.of(), last.errorDetails());
	}

	/**
	 * A failure with the summary {@link #TITLE} and the problems chained below it, a failure the
	 * user can act on, as a commit vetoed by the model constraints is.
	 */
	private static I18NRuntimeException failureWithProblems() {
		return new TopLogicException(TITLE, new TopLogicException(PROBLEM_1, new TopLogicException(PROBLEM_2)))
			.initSeverity(ErrorSeverity.ERROR);
	}

	/** Runs the given body as a job and waits for it to end. */
	private static JobState runJob(JobBody body) throws InterruptedException {
		CountDownLatch finished = new CountDownLatch(1);
		DefaultViewChannel channel = new DefaultViewChannel("job");
		new JobRunner(null, channel, List.of(), false, 0, state -> finished.countDown()).start(body, List.of());
		assertTrue("The job must finish within " + TIMEOUT + " ms.", finished.await(TIMEOUT, TimeUnit.MILLISECONDS));
		return (JobState) channel.get();
	}

	/**
	 * Test suite requiring the {@link TypeIndex}, the {@link SchedulerService} the job runs on, and
	 * the {@link ResourcesModule} the messages of a failure are resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestJobRunnerFailure.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
