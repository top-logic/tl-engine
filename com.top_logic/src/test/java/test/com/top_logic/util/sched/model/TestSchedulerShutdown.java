/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.RunningModuleSystemSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.util.sched.TestingScheduler;
import test.com.top_logic.util.sched.TestingScheduler.TestingSchedulerConfig;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.Scheduler.SchedulerConfig;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.log.TaskLogWrapper;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.schedule.OnStartup;

/**
 * Test for the shutdown of the {@link Scheduler} while a cluster-wide {@link Task} is running, e.g.
 * an {@link OnStartup} task of an application that is stopped right after its start.
 */
@SuppressWarnings("javadoc")
public class TestSchedulerShutdown extends BasicTestCase {

	private static final String TASK_NAME = TestSchedulerShutdown.class.getSimpleName() + "Task";

	/** Maximum time in milliseconds to wait for the {@link Scheduler} and the task. */
	private static final long TIMEOUT = 30_000;

	/** Polling interval of a {@link Scheduler} whose regular rounds do not happen during a test. */
	private static final long LONG_POLLING_INTERVAL = TimeUnit.HOURS.toMillis(1);

	/**
	 * {@link SchedulerConfig#getShutdownJoinTime()} of the tests, much shorter than the run of the
	 * task.
	 */
	private static final int SHUTDOWN_JOIN_TIME = 100;

	/**
	 * How long the task keeps running after the shutdown has started. Longer than
	 * {@link #SHUTDOWN_JOIN_TIME}.
	 */
	private static final long TASK_RUN_AFTER_SHUTDOWN = 1000;

	/**
	 * When set, a {@link BlockingTask} waits for this latch after its start by the {@link Scheduler}
	 * and before it writes the start of its run to its log.
	 */
	static final AtomicReference<CountDownLatch> BEFORE_RESULT_PROTOCOL = new AtomicReference<>();

	/** Prefix of the error logged for a task that is unknown to the scheduler of the application. */
	private static final String UNKNOWN_TASK_ERROR = "Could not find task";

	/** Prefix of the error logged for a task that is still running after the time to kill. */
	private static final String TASK_STILL_RUNNING_ERROR = "Tasks still running after the time to kill";

	/**
	 * A task that is still running, when the shutdown of the {@link Scheduler} starts, finishes its
	 * run and writes its result before the {@link Scheduler} has shut down, as long as it ends
	 * within the {@link SchedulerConfig#getTimeToKill() time to kill}.
	 */
	public void testShutdownWaitsForRunningTask() throws Exception {
		String name = TASK_NAME + "WithinTimeToKill";
		TestingScheduler scheduler = startScheduler(name, TIMEOUT);
		BlockingTask task = (BlockingTask) scheduler.getTaskByName(name);
		task.awaitRunStarted();

		CollectingLogListener errors = new CollectingLogListener(AssertNoErrorLogListener.ERROR_AND_ABOVE, true);
		try {
			shutDownWhileRunning(scheduler, task);

			assertTrue("The scheduler must not shut down before the task has ended its run.",
				task.hasRunEnded());
		} finally {
			errors.deactivate();
		}
		assertNoErrors(errors.getLogEntries(), Collections.emptyList());
		task.assertRunWithoutFailure();
		assertResultWritten(task);
	}

	/**
	 * A task that is still running, when the {@link Scheduler} has given up waiting for it after
	 * the {@link SchedulerConfig#getTimeToKill() time to kill}, ends its run without failure and
	 * writes its result.
	 */
	public void testTaskEndsAfterShutdown() throws Exception {
		String name = TASK_NAME + "AfterTimeToKill";
		TestingScheduler scheduler = startScheduler(name, SHUTDOWN_JOIN_TIME);
		BlockingTask task = (BlockingTask) scheduler.getTaskByName(name);
		task.awaitRunStarted();

		CollectingLogListener errors = new CollectingLogListener(AssertNoErrorLogListener.ERROR_AND_ABOVE, true);
		try {
			shutDownWhileRunning(scheduler, task);
			task.awaitRunEnded();
		} finally {
			errors.deactivate();
		}
		assertNoErrors(errors.getLogEntries(), Collections.singletonList(TASK_STILL_RUNNING_ERROR));
		task.assertRunWithoutFailure();
		assertResultWritten(task);
	}

	/**
	 * Asserts that the given errors contain no other errors than the expected ones.
	 * 
	 * <p>
	 * The log of a task notifies the {@link Scheduler#getSchedulerInstance() scheduler of the
	 * application} about state changes, which does not know the task of a test scheduler. The
	 * resulting errors are ignored.
	 * </p>
	 * 
	 * @param expectedPrefixes
	 *        Prefixes of the messages of the errors that are expected.
	 */
	private static void assertNoErrors(List<LogEntry> errors, List<String> expectedPrefixes) {
		for (LogEntry error : errors) {
			String message = error.getMessage();
			if (message.startsWith(UNKNOWN_TASK_ERROR)) {
				continue;
			}
			if (expectedPrefixes.stream().anyMatch(message::startsWith)) {
				continue;
			}
			fail("Unexpected error during the shutdown: " + message
				+ (error.getException().hasValue() ? " Cause: " + error.getException().get() : ""));
		}
	}

	private static void assertResultWritten(Task task) throws MergeConflictException {
		assertNotSame("The run must write its result.", ResultType.NOT_FINISHED,
			currentResult(task).getResultType());
	}

	/**
	 * A stop request for a cluster-wide task, that the {@link Scheduler} has started but whose
	 * thread has not yet created the result of the run, is answered right away and stops the run.
	 */
	public void testStopBeforeRunHasCreatedItsResult() throws Exception {
		String name = TASK_NAME + "BeforeResult";
		CountDownLatch gate = new CountDownLatch(1);
		BEFORE_RESULT_PROTOCOL.set(gate);
		TestingScheduler scheduler;
		BlockingTask task;
		try {
			scheduler = startScheduler(name, TIMEOUT);
			task = (BlockingTask) scheduler.getTaskByName(name);
			task.awaitRunEntered();
		} finally {
			BEFORE_RESULT_PROTOCOL.set(null);
		}

		CollectingLogListener errors = new CollectingLogListener(AssertNoErrorLogListener.ERROR_AND_ABOVE, true);
		try {
			long start = System.currentTimeMillis();
			inSystemContext(task::signalStop);
			long duration = System.currentTimeMillis() - start;
			assertTrue("Stop request must not be retried, took " + duration + " ms.", duration < 1000);
		} finally {
			errors.deactivate();
			gate.countDown();
			task.release();
		}
		assertNoErrors(errors.getLogEntries(), Collections.emptyList());
		task.awaitRunEnded();
		scheduler.stopAndJoin(TIMEOUT);
		task.assertRunWithoutFailure();
		assertEquals(ResultType.CANCELED, currentResult(task).getResultType());
	}

	/**
	 * Shuts down the given {@link Scheduler} and lets the given task end its run
	 * {@link #TASK_RUN_AFTER_SHUTDOWN} after the start of the shutdown.
	 */
	private static void shutDownWhileRunning(TestingScheduler scheduler, BlockingTask task)
			throws InterruptedException {
		Thread releaser = new Thread(() -> {
			try {
				Thread.sleep(TASK_RUN_AFTER_SHUTDOWN);
			} catch (InterruptedException ex) {
				// Release the task anyway.
			}
			task.release();
		}, TestSchedulerShutdown.class.getSimpleName() + "Releaser");
		releaser.start();
		try {
			scheduler.accessibleShutDown();
		} finally {
			releaser.join(TIMEOUT);
		}
	}

	private static TestingScheduler startScheduler(String taskName, long timeToKill)
			throws ConfigurationException {
		TestingSchedulerConfig config = TestingScheduler.createSchedulerConfiguration(
			"Scheduler[" + taskName + "]", LONG_POLLING_INTERVAL, TIMEOUT, timeToKill, 4);
		config.update(config.descriptor().getProperty("shutdown-join-time"), SHUTDOWN_JOIN_TIME);
		BlockingTask.Config<?> taskConfig = TypedConfiguration.newConfigItem(BlockingTask.Config.class);
		taskConfig.setName(taskName);
		taskConfig.getSchedules().add(TypedConfiguration.createConfigItemForImplementationClass(OnStartup.class));
		config.getTasks().put(taskName, taskConfig);

		TestingScheduler[] scheduler = new TestingScheduler[1];
		inSystemContext(() -> scheduler[0] = TestingScheduler.createAndStartScheduler(config));
		return scheduler[0];
	}

	private static TaskResult currentResult(Task task) throws MergeConflictException {
		// Make the result committed by the task thread visible.
		HistoryUtils.updateSessionRevision();
		TaskResult[] result = new TaskResult[1];
		inSystemContext(() -> result[0] = TaskLogWrapper.getLogForTask(task).getCurrentResult());
		assertNotNull("Task has no result.", result[0]);
		return result[0];
	}

	private static void inSystemContext(Runnable action) {
		ThreadContext.inSystemContext(TestSchedulerShutdown.class, action::run);
	}

	/**
	 * Cluster-wide task that does not want to be stopped, and whose run lasts until it is
	 * {@link #release() released}.
	 */
	public static class BlockingTask extends TaskImpl<BlockingTask.Config<?>> {

		/**
		 * Configuration options for {@link BlockingTask}.
		 */
		public interface Config<I extends BlockingTask> extends TaskImpl.Config<I> {
			@Override
			@ClassDefault(BlockingTask.class)
			Class<? extends I> getImplementationClass();
		}

		private final CountDownLatch _runEntered = new CountDownLatch(1);

		private final CountDownLatch _runStarted = new CountDownLatch(1);

		private final CountDownLatch _release = new CountDownLatch(1);

		private final CountDownLatch _runEnded = new CountDownLatch(1);

		private final AtomicReference<Throwable> _failure = new AtomicReference<>();

		/**
		 * Creates a {@link BlockingTask} from configuration.
		 */
		@CalledByReflection
		public BlockingTask(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public boolean isNodeLocal() {
			return false;
		}

		@Override
		protected boolean signalStopHook() {
			return false;
		}

		@Override
		public void run() {
			super.run();
			try {
				CountDownLatch gate = BEFORE_RESULT_PROTOCOL.get();
				if (gate != null) {
					_runEntered.countDown();
					awaitLatch(gate);
				}
				runWithResultProtocol(this::work);
			} catch (Throwable ex) {
				_failure.set(ex);
				throw ex;
			} finally {
				_runEnded.countDown();
			}
		}

		private void work() {
			_runStarted.countDown();
			awaitLatch(_release);
		}

		private static void awaitLatch(CountDownLatch latch) {
			try {
				if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
					throw new IllegalStateException("Task was not released.");
				}
			} catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		}

		/**
		 * Waits until the run has been entered but, as long as {@link #BEFORE_RESULT_PROTOCOL} is
		 * set, not yet written the start to its log.
		 */
		public void awaitRunEntered() throws InterruptedException {
			assertTrue("Task has not been entered.", _runEntered.await(TIMEOUT, TimeUnit.MILLISECONDS));
		}

		/** Lets the run end. */
		public void release() {
			_release.countDown();
		}

		/** Waits until the run has started. */
		public void awaitRunStarted() throws InterruptedException {
			assertTrue("Task has not started.", _runStarted.await(TIMEOUT, TimeUnit.MILLISECONDS));
		}

		/** Waits until the run has ended. */
		public void awaitRunEnded() throws InterruptedException {
			assertTrue("Task has not ended its run.", _runEnded.await(TIMEOUT, TimeUnit.MILLISECONDS));
		}

		/** Whether the run has ended. */
		public boolean hasRunEnded() {
			return _runEnded.getCount() == 0;
		}

		/** Asserts that the run ended without failure. */
		public void assertRunWithoutFailure() {
			Throwable failure = _failure.get();
			if (failure != null) {
				throw (AssertionError) new AssertionError("Run of the task failed: " + failure).initCause(failure);
			}
		}

	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(RunningModuleSystemSetup.setup(TestSchedulerShutdown.class),
				Scheduler.Module.INSTANCE));
	}

}
