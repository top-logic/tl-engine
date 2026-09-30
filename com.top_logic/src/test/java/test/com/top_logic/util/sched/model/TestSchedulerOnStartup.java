/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.util.ApplicationStartedSetup;
import test.com.top_logic.util.sched.TestingScheduler;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.TaskCommon;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.log.TaskLogWrapper;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.schedule.OnStartup;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Test for the start of the {@link Scheduler} dispatch, for {@link OnStartup} tasks that run only
 * on one node in the cluster, and for the startup cleanup of their cluster locks.
 */
@SuppressWarnings("javadoc")
public class TestSchedulerOnStartup extends BasicTestCase {

	private static final String TASK_NAME = TestSchedulerOnStartup.class.getSimpleName() + "Task";

	/** Maximum time in milliseconds to wait for the {@link Scheduler}. */
	private static final long TIMEOUT = 30_000;

	/**
	 * A cluster-wide {@link OnStartup} task, whose task log contains a run from before the start of
	 * the {@link Scheduler}, runs again after the start. A run after the start of the
	 * {@link Scheduler} is not repeated.
	 */
	public void testClusterTaskRunsAgainAfterSchedulerRestart() throws Exception {
		ClusterTask first = addTask(scheduler());
		waitFor("First run of the task.", () -> first.getRuns() == 1 && scheduler().getNumRunningTasks() == 0);
		long firstRun = currentResult(first).getStartDate().getTime();

		ModuleUtil.INSTANCE.restart(Scheduler.Module.INSTANCE, null);
		Scheduler restarted = scheduler();
		assertTrue("Persisted run must be before the dispatch start of the restarted scheduler.",
			firstRun < restarted.getDispatchStart());

		ClusterTask second = addTask(restarted);
		waitFor("Run of the task after the scheduler restart.",
			() -> second.getRuns() == 1 && restarted.getNumRunningTasks() == 0);
		assertTrue(currentResult(second).getStartDate().getTime() >= restarted.getDispatchStart());

		// A further instance of the task, as another cluster node sees it: It has not run itself,
		// but the task log contains a run after the start of the scheduler.
		assertTrue(restarted.removeTask(second));
		ClusterTask third = addTask(restarted);
		waitFor("Scheduler has processed the task.",
			() -> third.getNextShed() == SchedulingAlgorithm.NO_SCHEDULE);
		assertEquals("Task has run after the scheduler start already.", 0, third.getRuns());
		assertEquals(1, second.getRuns());
		assertTrue(restarted.removeTask(third));
	}

	/**
	 * The {@link Scheduler} does not dispatch while the application has not started, but when the
	 * startup completes. A {@link Scheduler} shut down before does not start dispatching.
	 */
	public void testDispatchStartsWhenApplicationHasStarted() throws RestartException {
		Scheduler stopped;
		Scheduler waiting;
		ApplicationStartedSetup.resetStartup();
		try {
			ModuleUtil.INSTANCE.restart(Scheduler.Module.INSTANCE, null);
			stopped = scheduler();
			assertEquals(SchedulingAlgorithm.NO_SCHEDULE, stopped.getDispatchStart());
			assertNull(TestingScheduler.getThread(stopped));

			ModuleUtil.INSTANCE.restart(Scheduler.Module.INSTANCE, null);
			waiting = scheduler();
			assertNotSame(stopped, waiting);
			assertEquals(SchedulingAlgorithm.NO_SCHEDULE, waiting.getDispatchStart());
			assertNull(TestingScheduler.getThread(waiting));
		} finally {
			ApplicationStartedSetup.completeStartup();
		}
		assertTrue(waiting.getDispatchStart() != SchedulingAlgorithm.NO_SCHEDULE);
		assertTrue(TestingScheduler.getThread(waiting).isAlive());

		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, stopped.getDispatchStart());
		assertNull(TestingScheduler.getThread(stopped));
	}

	/**
	 * The startup cleanup of a cluster-wide task releases a cluster lock that an earlier start of
	 * this node has left behind, although the node id of that lock is outdated.
	 */
	public void testStartupReleasesLockOfEarlierNodeStart() throws Exception {
		ClusterTask task = createTask(TASK_NAME + "EarlierStart");
		TaskLogWrapper log = logWithLock(task, TaskCommon.getCurrentClusterNodeName());
		assertFalse("Lock of an earlier start is not the lock of the current start.", log.hasClusterLock());

		inSystemContext(() -> assertTrue(log.tryStartupNodeClean(task).isSuccess()));

		assertFalse("Lock of an earlier start of this node must be released.", log.isClusterLockSet());
		assertEquals(TaskState.INACTIVE, log.getState());
	}

	/**
	 * The startup cleanup of a cluster-wide task keeps the cluster lock of another node.
	 */
	public void testStartupKeepsLockOfOtherNode() throws Exception {
		ClusterTask task = createTask(TASK_NAME + "OtherNode");
		TaskLogWrapper log = logWithLock(task, TaskCommon.getCurrentClusterNodeName() + "-other");

		inSystemContext(() -> assertTrue(log.tryStartupNodeClean(task).isSuccess()));

		assertTrue("Lock of another node must be kept.", log.isClusterLockSet());
		assertEquals(TaskState.RUNNING, log.getState());
	}

	/**
	 * The log of the given task, marked as running and locked by the given node name with a node id
	 * other than the one of this node.
	 */
	private static TaskLogWrapper logWithLock(ClusterTask task, String nodeName) {
		TaskLogWrapper[] result = new TaskLogWrapper[1];
		inSystemContext(() -> {
			TaskLogWrapper log = TaskLogWrapper.getLogForTask(task);
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				log.tHandle().setAttributeValue(TaskLogWrapper.PROPERTY_CLUSTER_NAME, nodeName);
				log.tHandle().setAttributeValue(TaskLogWrapper.PROPERTY_CLUSTER_ID,
					TaskCommon.getCurrentClusterNodeId() + 1);
				log.markAsRunning();
				tx.commit();
			}
			result[0] = log;
		});
		return result[0];
	}

	private static void inSystemContext(Runnable action) {
		ThreadContext.inSystemContext(TestSchedulerOnStartup.class, action::run);
	}

	private static Scheduler scheduler() {
		return Scheduler.getSchedulerInstance();
	}

	private static TaskResult currentResult(ClusterTask task) throws MergeConflictException {
		// Make the result committed by the scheduler's task thread visible.
		HistoryUtils.updateSessionRevision();
		TaskResult[] result = new TaskResult[1];
		ThreadContext.inSystemContext(TestSchedulerOnStartup.class, () -> {
			result[0] = task.getLog().getCurrentResult();
		});
		assertNotNull("Task has no result.", result[0]);
		return result[0];
	}

	private static ClusterTask addTask(Scheduler scheduler) throws ConfigurationException {
		ClusterTask task = createTask(TASK_NAME);
		ThreadContext.inSystemContext(TestSchedulerOnStartup.class, () -> scheduler.addTask(task));
		return task;
	}

	private static ClusterTask createTask(String name) throws ConfigurationException {
		ClusterTask.Config<?> config = TypedConfiguration.newConfigItem(ClusterTask.Config.class);
		config.setName(name);
		config.getSchedules().add(TypedConfiguration.createConfigItemForImplementationClass(OnStartup.class));
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static void waitFor(String message, BooleanSupplier condition) {
		long timeout = System.currentTimeMillis() + TIMEOUT;
		while (!condition.getAsBoolean()) {
			if (System.currentTimeMillis() > timeout) {
				fail("Timeout: " + message);
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Task that runs only on one node in the cluster and counts its runs on this node.
	 */
	public static class ClusterTask extends TaskImpl<ClusterTask.Config<?>> {

		/**
		 * Configuration options for {@link ClusterTask}.
		 */
		public interface Config<I extends ClusterTask> extends TaskImpl.Config<I> {
			@Override
			@ClassDefault(ClusterTask.class)
			Class<? extends I> getImplementationClass();
		}

		private final AtomicInteger _runs = new AtomicInteger();

		/**
		 * Creates a {@link ClusterTask} from configuration.
		 */
		@CalledByReflection
		public ClusterTask(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public boolean isNodeLocal() {
			return false;
		}

		@Override
		public void run() {
			super.run();
			ThreadContext.inSystemContext(ClusterTask.class, () -> {
				getLog().taskStarted();
				getLog().taskEnded(ResultType.SUCCESS, ResultType.SUCCESS.getMessageI18N());
			});
			_runs.incrementAndGet();
		}

		/**
		 * The number of runs of this instance.
		 */
		public int getRuns() {
			return _runs.get();
		}

	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(ApplicationStartedSetup.setup(TestSchedulerOnStartup.class),
				Scheduler.Module.INSTANCE));
	}

}
