/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.lang.Thread.State;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.RunningModuleSystemSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.util.sched.TestingScheduler;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.sched.ScheduledThread;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.Scheduler.SchedulerConfig;
import com.top_logic.util.sched.entry.SchedulerEntry;
import com.top_logic.util.sched.task.Task;

/**
 * Tests the API of the {@link Scheduler} for a running {@link Task}:
 * {@link Scheduler#takeOverMaintenanceMode(Task)} and
 * {@link Scheduler#runAfterTaskDone(Task, Runnable)}.
 *
 * <p>
 * The {@link TestingScheduler} waits on barriers in each dispatch round, so the test decides when
 * the {@link Scheduler} processes the end of a task.
 * </p>
 */
public class TestSchedulerMaintenanceMode extends BasicTestCase {

	/** Maximum time in milliseconds to wait for an asynchronous state change. */
	private static final long TIMEOUT = 10000;

	private TestingScheduler _scheduler;

	private CyclicBarrier _startWaitBarrier;

	private CyclicBarrier _endWaitBarrier;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_startWaitBarrier = new CyclicBarrier(2);
		_endWaitBarrier = new CyclicBarrier(2);
		SchedulerConfig config = TestingScheduler.createSchedulerConfiguration(
			/* thread name: */"Scheduler[" + getName() + "]",
			/* polling interval: */10,
			/* max task time: */5000,
			/* time before task kill: */1000,
			/* max tasks: */1000);
		_scheduler = (TestingScheduler) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		_scheduler.setStartWaitBarrier(_startWaitBarrier);
		_scheduler.setEndWaitBarrier(_endWaitBarrier);
		_scheduler.accessibleStartUp();
		// The scheduler is waiting at the beginning of its first dispatch round.
		TestScheduler.waitOnBarrier(_startWaitBarrier);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			stopScheduler();
		} finally {
			try {
				maintenanceWindow().leaveMaintenanceWindow();
			} finally {
				super.tearDown();
			}
		}
	}

	/**
	 * A task takes over the maintenance mode the {@link Scheduler} entered for it, and the
	 * {@link Scheduler} does not leave it when the task is done.
	 */
	public void testTakeOverMaintenanceMode() {
		CallbackTestTask task = createTask(true);
		AtomicReference<Boolean> takenOver = new AtomicReference<>();
		AtomicBoolean inMaintenanceModeDuringRun = new AtomicBoolean();
		task.setCallback(t -> {
			inMaintenanceModeDuringRun.set(isInMaintenanceMode());
			takenOver.set(Boolean.valueOf(_scheduler.takeOverMaintenanceMode(t)));
		});

		runOnce(task);

		assertTrue("Maintenance mode not active during the run.", inMaintenanceModeDuringRun.get());
		assertEquals("Scheduler entered the maintenance mode for the task.", Boolean.TRUE, takenOver.get());
		assertTrue("Scheduler must not leave a maintenance mode taken over by the task.", isInMaintenanceMode());
	}

	/**
	 * A maintenance mode entered before a forced run is not taken over, and the {@link Scheduler}
	 * does not leave it.
	 */
	public void testNoTakeOverOfForeignMaintenanceMode() {
		maintenanceWindow().enterMaintenanceWindow(0);
		waitUntil("Maintenance mode not entered.", TestSchedulerMaintenanceMode::isInMaintenanceMode);

		CallbackTestTask task = createTask(true);
		AtomicReference<Boolean> takenOver = new AtomicReference<>();
		task.setCallback(t -> takenOver.set(Boolean.valueOf(_scheduler.takeOverMaintenanceMode(t))));

		runOnce(task);

		assertEquals("Scheduler did not enter the maintenance mode for the task.", Boolean.FALSE, takenOver.get());
		assertTrue("Scheduler must not leave a maintenance mode it did not enter.", isInMaintenanceMode());
	}

	/**
	 * A task that needs no maintenance mode cannot take one over, and the {@link Scheduler} leaves
	 * none.
	 */
	public void testNoTakeOverWithoutMaintenanceMode() {
		CallbackTestTask task = createTask(false);
		AtomicReference<Boolean> takenOver = new AtomicReference<>();
		task.setCallback(t -> takenOver.set(Boolean.valueOf(_scheduler.takeOverMaintenanceMode(t))));

		runOnce(task);

		assertEquals(Boolean.FALSE, takenOver.get());
		assertFalse(isInMaintenanceMode());
	}

	/**
	 * Without a take-over, the {@link Scheduler} leaves the maintenance mode it entered for a task
	 * when the task is done.
	 */
	public void testLeaveMaintenanceModeWithoutTakeOver() {
		CallbackTestTask task = createTask(true);
		AtomicBoolean inMaintenanceModeDuringRun = new AtomicBoolean();
		task.setCallback(t -> inMaintenanceModeDuringRun.set(isInMaintenanceMode()));

		runOnce(task);

		assertTrue("Maintenance mode not active during the run.", inMaintenanceModeDuringRun.get());
		assertFalse("Scheduler must leave the maintenance mode it entered for the task.", isInMaintenanceMode());
	}

	/**
	 * An action registered by the running task runs after the {@link Scheduler} processed the end of
	 * the task, not already when the task thread ends.
	 */
	public void testRunAfterTaskDone() {
		CallbackTestTask task = createTask(false);
		AtomicBoolean actionRun = new AtomicBoolean();
		AtomicReference<SchedulerEntry.State> stateInAction = new AtomicReference<>();
		task.setCallback(t -> _scheduler.runAfterTaskDone(t, () -> {
			stateInAction.set(_scheduler.getEntries().get(t).get().getState());
			actionRun.set(true);
		}));

		startAndAwaitThreadEnd(task);
		assertFalse("Action must not run before the scheduler processed the end of the task.", actionRun.get());

		dispatchRound();
		assertTrue("Action not run after the scheduler processed the end of the task.", actionRun.get());
		assertEquals(SchedulerEntry.State.WAITING, stateInAction.get());

		// The action runs once.
		actionRun.set(false);
		task.setCallback(null);
		runOnce(task);
		assertFalse("Action must run only once.", actionRun.get());
	}

	/**
	 * A failing action is logged and does not prevent the further actions from running.
	 */
	public void testRunAfterTaskDoneWithFailingAction() {
		CallbackTestTask task = createTask(false);
		AtomicBoolean secondActionRun = new AtomicBoolean();
		task.setCallback(t -> {
			_scheduler.runAfterTaskDone(t, () -> {
				throw new RuntimeException("Expected failure of an after-done action.");
			});
			_scheduler.runAfterTaskDone(t, () -> secondActionRun.set(true));
		});

		CollectingLogListener errors = new CollectingLogListener(AssertNoErrorLogListener.ERROR_AND_ABOVE, true);
		try {
			runOnce(task);
		} finally {
			errors.deactivate();
		}
		assertTrue("Second action not run after a failing first action.", secondActionRun.get());
		assertTrue("Failing action not logged.", errors.getLogEntries().stream()
			.anyMatch(entry -> entry.getMessage().contains(task.getName())));
	}

	private CallbackTestTask createTask(boolean needsMaintenanceMode) {
		CallbackTestTask.Config<?> config = TypedConfiguration.newConfigItem(CallbackTestTask.Config.class);
		config.setName(getName());
		config.setNeedingMaintenanceMode(needsMaintenanceMode);
		config.setMaintenanceModeDelay(0);
		CallbackTestTask task = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		// No schedule: The task runs only when forced.
		_scheduler.addTask(task);
		return task;
	}

	/**
	 * Forces a run of the given task and waits until the {@link Scheduler} processed its end.
	 */
	private void runOnce(CallbackTestTask task) {
		startAndAwaitThreadEnd(task);
		dispatchRound();
		assertEquals("Scheduler did not process the end of the task.", 0, _scheduler.getNumRunningTasks());
	}

	/**
	 * Forces a run of the given task and waits until the thread of the task has terminated, while
	 * the {@link Scheduler} waits at the beginning of its next dispatch round.
	 */
	private void startAndAwaitThreadEnd(CallbackTestTask task) {
		_scheduler.startTask(task);
		dispatchRound();
		waitUntil("Task thread did not end.", () -> {
			SchedulerEntry entry = _scheduler.getEntries().get(task).get();
			ScheduledThread thread = entry.getThread();
			return thread != null && thread.getState() == State.TERMINATED;
		});
	}

	/**
	 * Lets the {@link Scheduler} perform one complete dispatch round.
	 */
	private void dispatchRound() {
		// Scheduler starts dispatching.
		TestScheduler.waitOnBarrier(_endWaitBarrier);
		// Scheduler finished dispatching.
		TestScheduler.waitOnBarrier(_startWaitBarrier);
	}

	private void stopScheduler() throws InterruptedException {
		if (_scheduler == null) {
			return;
		}
		_scheduler.signalStop();
		_scheduler.setStartWaitBarrier(null);
		_scheduler.setEndWaitBarrier(null);
		release(_startWaitBarrier);
		release(_endWaitBarrier);
		_scheduler.getSchedulerThread().join(TIMEOUT);
		_scheduler = null;
	}

	private static void release(CyclicBarrier barrier) {
		if (barrier.getNumberWaiting() == 1) {
			TestScheduler.waitOnBarrier(barrier);
		}
		barrier.reset();
	}

	private static void waitUntil(String message, BooleanSupplier condition) {
		long timeout = System.currentTimeMillis() + TIMEOUT;
		while (!condition.getAsBoolean()) {
			assertTrue(message, System.currentTimeMillis() < timeout);
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private static boolean isInMaintenanceMode() {
		return maintenanceWindow().getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE;
	}

	private static MaintenanceWindowManager maintenanceWindow() {
		return MaintenanceWindowManager.getInstance();
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test test = RunningModuleSystemSetup.setup(TestSchedulerMaintenanceMode.class);
		test = ServiceTestSetup.createSetup(test, MaintenanceWindowManager.Module.INSTANCE);
		String customConfig = CustomPropertiesDecorator.createFileName(TestSchedulerMaintenanceMode.class);
		test = TestUtils.doNotMerge(new CustomPropertiesSetup(test, customConfig, true));
		return KBSetup.getSingleKBTest(test);
	}

}
