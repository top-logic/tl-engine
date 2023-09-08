/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.lang.Thread.State;
import java.util.Calendar;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.util.sched.TestingScheduler;
import test.com.top_logic.util.sched.model.BarrierTestTask.BarrierTestTaskConfig;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.sched.ScheduledThread;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.Scheduler.SchedulerConfig;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;

/**
 * Tests for {@link com.top_logic.util.sched.Scheduler} class.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestScheduler extends BasicTestCase {

	private static final long INFINITLY = Long.MAX_VALUE;

	/** Timeout in milliseconds when using the {@link BarrierTestTask}. */
	private static final long BARRIER_TIMEOUT = 2000;

	// All times must be in ascending order so tests will execute correctly

    /** All times are multiplied with this factor */
	public static int TIME_ZOOM = 20;

    /** Time that the Scheduler should sleep between checks */
    public static long TASK_SLEEP      = TIME_ZOOM;
   
    /** Time that a short batch/task should run without Problems */
    public static long SHORT_SLEEP     = 3  * TIME_ZOOM;

	/** Tasks/Batches running longer than this time should be signaled to stop() */
    public static int TASK_TIMEOUT     = 6  * TIME_ZOOM;

    /** Time that the Testcase will Sleep to reach all desired results. */
    public static long TEST_SLEEP      = 9  * TIME_ZOOM;

	/** Time that a long batch/task will run an therefore be signaled to stop() */
    public static long LONG_SLEEP      = 12 * TIME_ZOOM;

	private static final long UNCERTAINTY = TASK_SLEEP;

	private TestingScheduler scheduler;

    /**
     * Constructor for TestScheduler.
     * 
     * @param name method name of the test to execute.
     */
    public TestScheduler(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
    	stopAndJoin();
    	
    	super.tearDown();
    }
    
    /**
     *  Test handling of Tasks.
     */
    public void testTasks() throws InterruptedException {
        
		Calendar now = CalendarUtil.createCalendar();
        
        int curHour   = now.get(Calendar.HOUR_OF_DAY);
        int curMinute = now.get(Calendar.MINUTE);
        
        // Create a fast acting Scheduler
		createScheduler("testTasks", TASK_SLEEP, TASK_TIMEOUT, 1);
		assertTrue(scheduler.getSchedulerThread().isDaemon()); // What the hell ;-)
        
        // add a infinitely runningTask
		ExampleTask deadTask = new ExampleTask("Dead", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, INFINITLY);
		scheduler.addTask(deadTask);
        
        // Suppress ERROR which is quite OK in this case 
        Logger.configureStdout("FATAL"); //***********

        Thread.sleep(TASK_SLEEP);
        for (int i=0; i < 48; i++) { // (adjust for one minute +/-)
            if (deadTask.getShouldStop())
                break;
            Thread.sleep(TASK_SLEEP);
        }
		Thread.sleep(UNCERTAINTY + TASK_TIMEOUT + TASK_SLEEP);
        Logger.configureStdout(); //***********

        assertTrue("DeadTask Task was not signaled to stop", deadTask.getShouldStop());
       
        // add a Slow Batch
		curHour = now.get(Calendar.HOUR_OF_DAY);
		curMinute = now.get(Calendar.MINUTE);
        ExampleTask slowTask = new ExampleTask("SlowTask", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, LONG_SLEEP);
		scheduler.addTask(slowTask);

        // Sleep until slowTask is submitted and executed 
        Thread.sleep(LONG_SLEEP);
        
        // add a Fast Task  overlapping
		curHour = now.get(Calendar.HOUR_OF_DAY);
		curMinute = now.get(Calendar.MINUTE);
        ExampleTask fastTask = new ExampleTask("FastTask", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, SHORT_SLEEP);
		scheduler.addTask(fastTask);
        
        // Suppress ERROR which is quite OK in this case 
        Logger.configureStdout("FATAL"); //***********

		assertTrue(scheduler.removeTask(slowTask));
		assertTrue(scheduler.removeTask(fastTask));

        Thread.sleep(2 * TASK_SLEEP);
        
        for (int i=0; i < 24; i++) { // (adjust for one minute +/-)
            if (slowTask.getShouldStop())
                break;
            Thread.sleep(TASK_SLEEP);
        }
        Logger.configureStdout(); 
                
        assertTrue("Slow Task was not signaled to stop", slowTask.getShouldStop());
        
		assertFalse(scheduler.removeTask(fastTask)); // cannot be removed as it was removed before
		scheduler.addTask(fastTask);
        
        // Sleep until Batch was submitted
        Thread.sleep(SHORT_SLEEP);
        
		// Sleep until the task is signaled to stop
        // fastTask.join(); // wont help
        
        // Batch just finished without signal ...
        assertTrue(!fastTask.getShouldStop());
        
		stopAndJoin();
        
        // Now do it in one rush ...

		createScheduler("testTasks", TASK_SLEEP, (long) TASK_TIMEOUT, 3);
        
        // add a infinitely runningTask
		curHour = now.get(Calendar.HOUR_OF_DAY);
		curMinute = now.get(Calendar.MINUTE);
		deadTask = new ExampleTask("Dead", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, INFINITLY);
        slowTask = new ExampleTask("SlowTask", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, LONG_SLEEP);
        fastTask = new ExampleTask("FastTask", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, SHORT_SLEEP);

		scheduler.addTask(deadTask);
		scheduler.addTask(slowTask);
		scheduler.addTask(fastTask);
        
        // Suppress ERROR which is quite OK in this case 
        Logger.configureStdout("FATAL"); //***********
        // Sleep until Tasks where submitted and handled
		Thread.sleep(TASK_SLEEP + LONG_SLEEP + 1000);
        for (int i=0; i < 36; i++) { // (adjust for one minute +/-)
            if (slowTask.getShouldStop())
                break;
            Thread.sleep(5000);
        }
        Logger.configureStdout(); //***********
        
		stopAndJoin();
        
        assertTrue(!fastTask.getShouldStop());
		assertTrue("Ticket #11857: ", slowTask.getShouldStop());
        assertTrue( deadTask.getShouldStop());
        
    }

    /**
     *  Test handling Tasks that do not want to die.
     */
    public void testZombies() throws InterruptedException {
        
		Calendar now = CalendarUtil.createCalendar();
        
        int curHour   = now.get(Calendar.HOUR_OF_DAY);
        int curMinute = now.get(Calendar.MINUTE);
        
        // Create a fast acting Scheduler
		createScheduler("testZombies", TASK_SLEEP, TASK_TIMEOUT, 1);
		assertTrue(scheduler.getSchedulerThread().isDaemon()); // What the hell ;-)
        
		ExampleTask zombieTask = new ExampleTask("Zombie", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, INFINITLY);
        zombieTask.setAllowStop(false);
		scheduler.addTask(zombieTask);
        
        // Sleep until Task was executed
        Thread.sleep(100);
        
        // Suppress ERRORs which are quite OK in this case 
        Logger.configureStdout("FATAL"); //***********
        Thread.sleep(3 * TASK_SLEEP + TASK_TIMEOUT);

        // Cannot reliable assert as Task is often in a transient Stack ..
		assertTrue("Scheduler forgot about Task.", scheduler.isKnown(zombieTask));
        assertTrue("Zombie Task was not signaled to stop", zombieTask.getShouldStop());
        zombieTask.setAllowStop(true); // Allow Zombie to stop
        
        Thread.sleep(3 * TASK_SLEEP + TASK_TIMEOUT);
        Logger.configureStdout(); //***********
		assertTrue("Scheduler forgot about Task.", scheduler.isKnown(zombieTask));
		stopAndJoin();
    }

    /** Test handling of shutdown with a mixed basket of Batches and Thread.
     */
    public void testShutdown() throws InterruptedException {
        
        // Create a fast acting Scheduler
		createScheduler("testShutdown", TASK_SLEEP, TASK_TIMEOUT, 3);
        
		assertTrue(scheduler.getSchedulerThread().isDaemon()); // What the hell ;-)
        
		Calendar now = CalendarUtil.createCalendar();
        
        int curHour   = now.get(Calendar.HOUR_OF_DAY);
        int curMinute = now.get(Calendar.MINUTE);

        // add a infinitely runningTask
		ExampleTask deadTask = new ExampleTask("Dead", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, INFINITLY);
        ExampleTask slowTask = new ExampleTask("SlowTask" , LegacySchedulesCommon.ONCE, 0, curHour, curMinute, LONG_SLEEP);
        ExampleTask fastTask = new ExampleTask("FastBatch", LegacySchedulesCommon.ONCE, 0, curHour, curMinute, SHORT_SLEEP);

		scheduler.addTask(deadTask);
		scheduler.addTask(slowTask);
		scheduler.addTask(fastTask);
        
        Thread.sleep(SHORT_SLEEP); // Sleep a bit to get things going
               
        // Suppress ERROR which is quite OK in this case 
        Logger.configureStdout("FATAL");    //***********
		stopAndJoin();
        Logger.configureStdout();           //***********
        
        assertTrue(deadTask.getShouldStop());
		assertTrue("Ticket #11857: ", slowTask.getShouldStop());
        // assertTrue(fastTask.getShouldStop()); // to fast, well
        
    }

    /** Test Stopping of Task when Scheduler is stopped.
     */
    public void testStopping() throws InterruptedException {

		Calendar now = CalendarUtil.createCalendar();
        
        int curHour   = now.get(Calendar.HOUR_OF_DAY);
        int curMinute = now.get(Calendar.MINUTE);
        
		scheduler =
			TestingScheduler.newStartedScheduler("TestScheduler", TASK_SLEEP, TASK_TIMEOUT, 1000*60, 2);

        ExampleTask  slowTask  = new ExampleTask ("SlowTask",  LegacySchedulesCommon.ONCE, 0, curHour, curMinute, LONG_SLEEP);
        ExampleTask  fastTask  = new ExampleTask ("FastTask",  LegacySchedulesCommon.ONCE, 0, curHour, curMinute, 10);

		scheduler.addTask(slowTask);
		scheduler.addTask(fastTask);
        
        Thread.sleep(3 * TASK_SLEEP + TASK_TIMEOUT); // give some time for Tasks/Batches to run
        
		stopAndJoin();

        assertTrue("Fast Task needs not stop" , !fastTask.getShouldStop());
        assertTrue("Slow Task should stop"    ,  slowTask.getShouldStop());
    }

    /** Test Executing a task more than once.
     * 
	 *  (There was a big design flaw here)
     */
    public void testPeriodically() throws InterruptedException {

         // Create a very fast acting Scheduler
		createScheduler("testPeriodically", TASK_SLEEP, TASK_TIMEOUT, 1);
        
		Calendar now = CalendarUtil.createCalendar();
        
        int curHour   = now.get(Calendar.HOUR_OF_DAY);
        int curMinute = now.get(Calendar.MINUTE);

        ExampleTask periodTask = new ExampleTask(
            "PeriodicalTask", 
            LegacySchedulesCommon.DAILY | LegacySchedulesCommon.PERIODICALLY, 0,
            curHour, curMinute, TASK_SLEEP << 2, curHour + 1, curMinute, 10);  
            // May fail when run around midnight, well
		scheduler.addTask(periodTask);
        
        Thread.sleep(TASK_SLEEP << 4); // give some time for periodTask to run
        
		stopAndJoin();
        int runTimes = periodTask.getRunTimes();
        assertTrue("Should run more than 2 times, but was " + runTimes,
                    runTimes > 2);
    }

	public void testTaskEndResultUpdate() {
		CyclicBarrier schedulerBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier(getName(), schedulerBarrier);
		try {
			AssertNoErrorLogListener loglistener = new AssertNoErrorLogListener();
			try {
				waitUntilThreadsWaiting(schedulerBarrier, 1);

				AlwaysBarrierTestTask task = createAlwaysBarrierTestTask(getName(), true);
				scheduler.addTask(task);

				// Scheduler starts Task
				waitOnBarrier(schedulerBarrier);
				// Scheduler waits for task started
				waitUntilThreadsWaiting(schedulerBarrier, 1);
				// Task committed start
				waitOnBarrier(task.getBarrier());
				// Scheduler updates revision to "task started"
				waitOnBarrier(schedulerBarrier);
				// Scheduler waits for task stopped
				waitUntilThreadsWaiting(schedulerBarrier, 1);
				// Task stops
				waitOnBarrier(task.getBarrier());
				// Task stopped
				waitForEnd(task);
				// Wait for scheduler found problem, task is done
				waitOnBarrier(schedulerBarrier);
				// Wait for scheduler has written problem
				waitUntilThreadsWaiting(schedulerBarrier, 1);

				waitOnBarrier(schedulerBarrier);
				loglistener.assertNoErrorLogged("Task has been successfully completed.");
			} finally {
				loglistener.deactivate();
			}
		} finally {
			stopBarrierScheduler();
		}
	}

	private void waitForEnd(TaskImpl<?> task) {
		ScheduledThread taskThread = scheduler.getEntries().get(task).get().getThread();
		if (taskThread != null) {
			while (taskThread.getState() != State.TERMINATED) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	private void waitUntilThreadsWaiting(CyclicBarrier barrier, int minNumberWaitingThreads) {
		while (barrier.getNumberWaiting() < minNumberWaitingThreads) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				// ignore
			}
		}
	}

	/**
	 * If a {@link Task} is {@link Scheduler#addTask(Task) added to the Scheduler}, it has to be
	 * enabled, initially.
	 */
	public void testAutoEnableOnAdd() {
		CyclicBarrier schedulerBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testAutoEnableOnAdd", schedulerBarrier);
		try {
			AlwaysBarrierTestTask task = createAlwaysBarrierTestTask("testAutoEnableOnAdd");
			scheduler.addTask(task);

			assertTrue(scheduler.isEnabled(task));
			// Scheduler entering dispatch:
			waitOnBarrier(schedulerBarrier);
			waitForTaskTerminated(task);
		} finally {
			stopBarrierScheduler();
		}
	}

	/**
	 * If a {@link Task} is {@link Scheduler#addDisabledTask(Task) added 'disabled' to the
	 * Scheduler}, it has to be disabled, initially.
	 */
	public void testAutoDisableOnAddDisabled() {
		CyclicBarrier schedulerBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testAutoDisableOnAddDisabled", schedulerBarrier);
		try {
			AlwaysBarrierTestTask task = createAlwaysBarrierTestTask("testAutoDisableOnAddDisabled");
			scheduler.addDisabledTask(task);

			assertFalse(scheduler.isEnabled(task));
			// Scheduler entering dispatch:
			waitOnBarrier(schedulerBarrier);
			// Scheduler ignores task, as it is disabled.
			// Scheduler entering dispatch, again:
			waitOnBarrier(schedulerBarrier);
			// One dispatch is guaranteed to have completed, check that nothing has been started:
			assertEquals(0, scheduler.getStartedTasks());
		} finally {
			stopBarrierScheduler();
		}
	}

	/**
	 * {@link Scheduler#disable(Task) Disable} a {@link Task} which initially
	 * {@link Scheduler#isEnabled(Task) is enabled}.
	 */
	public void testDisableAutoEnabled() {
		CyclicBarrier schedulerBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testDisableAutoEnabled", schedulerBarrier);
		try {
			AlwaysBarrierTestTask task = createAlwaysBarrierTestTask("testDisableAutoEnabled");
			scheduler.addTask(task);

			assertTrue(scheduler.isEnabled(task));
			scheduler.disable(task);
			assertFalse(scheduler.isEnabled(task));
			// Scheduler entering dispatch:
			waitOnBarrier(schedulerBarrier);
			// Scheduler ignores task, as it is disabled.
			// Scheduler entering dispatch, again:
			waitOnBarrier(schedulerBarrier);
			// One dispatch is guaranteed to have completed, check that nothing has been started:
			assertEquals(0, scheduler.getStartedTasks());
		} finally {
			stopBarrierScheduler();
		}
	}

	/**
	 * {@link Scheduler#enable(Task) Enable} a {@link Task} which initially
	 * {@link Scheduler#isEnabled(Task) is disabled}.
	 */
	public void testEnableAutoDisabled() {
		CyclicBarrier schedulerBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testEnableAutoDisabled", schedulerBarrier);
		try {
			AlwaysBarrierTestTask task = createAlwaysBarrierTestTask("testEnableAutoDisabled");
			scheduler.addDisabledTask(task);

			assertFalse(scheduler.isEnabled(task));
			scheduler.enable(task);
			assertTrue(scheduler.isEnabled(task));
			// Scheduler entering dispatch:
			waitOnBarrier(schedulerBarrier);
			waitForTaskTerminated(task);
		} finally {
			stopBarrierScheduler();
		}
	}

	/**
	 * Test that a 'enable-disable' round-trip works: {@link Scheduler#enable(Task) Enable} and
	 * {@link Scheduler#disable(Task) disable} a {@link Task} and {@link Scheduler#enable(Task)
	 * Enable} it again.
	 */
	public void testDisableEnableDisable() {
		CyclicBarrier schedulerBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testDisableEnableDisable", schedulerBarrier);
		try {
			AlwaysBarrierTestTask task = createAlwaysBarrierTestTask("testDisableEnableDisable");
			scheduler.addTask(task);

			assertTrue(scheduler.isEnabled(task));
			// Scheduler entering dispatch:
			waitOnBarrier(schedulerBarrier);
			waitForTaskTerminated(task);

			scheduler.disable(task);
			assertFalse(scheduler.isEnabled(task));
			// Scheduler entering dispatch:
			waitOnBarrier(schedulerBarrier);
			// Scheduler ignores task, as it is disabled.
			// Scheduler entering dispatch, again:
			waitOnBarrier(schedulerBarrier);
			// One dispatch is guaranteed to have completed, check that the task has been started again:
			assertEquals(1, scheduler.getStartedTasks());

			scheduler.enable(task);
			assertTrue(scheduler.isEnabled(task));
			// Scheduler entering dispatch:
			waitOnBarrier(schedulerBarrier);
			waitForTaskTerminated(task);
		} finally {
			stopBarrierScheduler();
		}
	}

	private void waitForTaskTerminated(BarrierTestTask task) {
		// Task starts:
		waitOnBarrier(task.getBarrier());
		// Task is finished:
		waitOnBarrier(task.getBarrier());
		// Wait for termination of task
		waitForEnd(task);
	}

	public void testForceRun() {
		CyclicBarrier startWaitBarrier = new CyclicBarrier(2);
		CyclicBarrier endWaitBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testForceRun", startWaitBarrier, endWaitBarrier);
		waitOnBarrier(startWaitBarrier);
		try {
			BarrierTestTask task = createNeverBarrierTestTask("testForceRun");
			scheduler.addTask(task);
			assertForceRun(task);
		} finally {
			stopBarrierScheduler();
		}
	}

	public void testForceRunWithDisabledTask() {
		CyclicBarrier startWaitBarrier = new CyclicBarrier(2);
		CyclicBarrier endWaitBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testForceRunWithDisabledTask", startWaitBarrier, endWaitBarrier);
		waitOnBarrier(startWaitBarrier);
		try {
			BarrierTestTask task = createNeverBarrierTestTask("testForceRunWithDisabledTask");
			scheduler.addDisabledTask(task);
			assertForceRun(task);
		} finally {
			stopBarrierScheduler();
		}
	}

	private void assertForceRun(BarrierTestTask task) {
		assertSchedulerStartsNothing();

		scheduler.startTask(task);
		assertSchedulerStartsTask(task);
		assertSchedulerStartsNothing();

		scheduler.startTask(task);
		assertSchedulerStartsTask(task);
		assertSchedulerStartsNothing();
	}

	public void testForceRunWithBlockedTask() {
		CyclicBarrier startWaitBarrier = new CyclicBarrier(2);
		CyclicBarrier endWaitBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testForceRunWithBlockedTask", startWaitBarrier, endWaitBarrier);
		waitOnBarrier(startWaitBarrier);
		try {
			BarrierTestTask task = createNeverBarrierTestTask("testForceRunWithBlockedTask");
			scheduler.addTask(task);
			assertForceRunWithBlockedTask(task);
		} finally {
			stopBarrierScheduler();
		}
	}

	public void testForceRunWithBlockedAndDisabledTask() {
		CyclicBarrier startWaitBarrier = new CyclicBarrier(2);
		CyclicBarrier endWaitBarrier = new CyclicBarrier(2);
		createSchedulerWaitingOnBarrier("testForceRunWithBlockedAndDisabledTask", startWaitBarrier, endWaitBarrier);
		waitOnBarrier(startWaitBarrier);
		try {
			BarrierTestTask task = createNeverBarrierTestTask("testForceRunWithBlockedAndDisabledTask");
			scheduler.addDisabledTask(task);
			assertForceRunWithBlockedTask(task);
		} finally {
			stopBarrierScheduler();
		}
	}

	private void assertForceRunWithBlockedTask(BarrierTestTask task) {
		scheduler.blockTask(task);
		assertSchedulerStartsNothing();
		scheduler.startTask(task);
		// Blocked tasks cannot be started, not even manually:
		assertSchedulerStartsNothing();
		scheduler.unblockTask(task);
		// When a task is unblocked, all missed schedules are discarded, the manual ones, too:
		assertSchedulerStartsNothing();
		scheduler.startTask(task);
		// After it has been unblocked, manual schedules have to work normally:
		assertSchedulerStartsTask(task);
		assertSchedulerStartsNothing();
	}

	private void assertSchedulerStartsTask(BarrierTestTask task) {
		int oldStartedTasks = scheduler.getStartedTasks();
		// Scheduler starts dispatching:
		waitOnBarrier(scheduler.getEndWaitBarrier());
		// Scheduler finishes dispatching:
		waitOnBarrier(scheduler.getBarrier());
		waitForTaskTerminated(task);
		assertEquals(oldStartedTasks + 1, scheduler.getStartedTasks());
	}

	private void assertSchedulerStartsNothing() {
		int oldStartedTasks = scheduler.getStartedTasks();
		// Scheduler starts dispatching:
		waitOnBarrier(scheduler.getEndWaitBarrier());
		// Scheduler finishes dispatching:
		waitOnBarrier(scheduler.getBarrier());
		// No tasks have been started:
		assertEquals(oldStartedTasks, scheduler.getStartedTasks());
	}

	private void stopBarrierScheduler() {
		stopScheduler();
		CyclicBarrier startWaitBarrier = scheduler.getStartWaitBarrier();
		CyclicBarrier endWaitBarrier = scheduler.getEndWaitBarrier();
		if (startWaitBarrier != null) {
			scheduler.setStartWaitBarrier(null);
			assertEquals(2, startWaitBarrier.getParties());
			if (startWaitBarrier.getNumberWaiting() == 1) {
				waitOnBarrier(startWaitBarrier);
			}
			// The scheduler might be waiting, stop that.
			// It might cause an exception in the scheduler thread, but that is okay here.
			startWaitBarrier.reset();
		}
		if (endWaitBarrier != null) {
			scheduler.setEndWaitBarrier(null);
			assertEquals(2, endWaitBarrier.getParties());
			if (endWaitBarrier.getNumberWaiting() == 1) {
				waitOnBarrier(endWaitBarrier);
			}
			// dito
			endWaitBarrier.reset();
		}
		try {
			joinScheduler();
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static int waitOnBarrier(CyclicBarrier schedulerBarrier) {
		try {
			return schedulerBarrier.await(BARRIER_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (BrokenBarrierException ex) {
			throw new RuntimeException(ex);
		} catch (TimeoutException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static AlwaysBarrierTestTask createAlwaysBarrierTestTask(String name) {
		return createAlwaysBarrierTestTask(name, false);
	}

	private static AlwaysBarrierTestTask createAlwaysBarrierTestTask(String name,
			boolean persistent) {
		BarrierTestTask.BarrierTestTaskConfig taskConfig =
			TypedConfiguration.newConfigItem(BarrierTestTask.BarrierTestTaskConfig.class);
		taskConfig.setImplementationClass(AlwaysBarrierTestTask.class);
		taskConfig.setName(name);
		taskConfig.setFirstBarrierTimeout(BARRIER_TIMEOUT);
		taskConfig.setSecondBarrierTimeout(BARRIER_TIMEOUT);
		taskConfig.setPersistent(persistent);
		return (AlwaysBarrierTestTask) getInstantiationContext().getInstance(taskConfig);
	}

	private static BarrierTestTask createNeverBarrierTestTask(String name) {
		BarrierTestTaskConfig taskConfig = TypedConfiguration.newConfigItem(BarrierTestTaskConfig.class);
		taskConfig.setImplementationClass(BarrierTestTask.class);
		taskConfig.setName(name);
		taskConfig.setFirstBarrierTimeout(BARRIER_TIMEOUT);
		taskConfig.setSecondBarrierTimeout(BARRIER_TIMEOUT);
		return getInstantiationContext().getInstance(taskConfig);
	}

	/**
	 * @see #createSchedulerWaitingOnBarrier(String, CyclicBarrier, CyclicBarrier)
	 */
	private void createSchedulerWaitingOnBarrier(String name, CyclicBarrier barrier) {
		createSchedulerWaitingOnBarrier(name, barrier, null);
	}

	/**
	 * Creates a {@link TestingScheduler} which is already started and will wait on the barrier in
	 * {@link Scheduler#dispatch()}.
	 */
	private void createSchedulerWaitingOnBarrier(String name,
			CyclicBarrier startWaitBarrier, CyclicBarrier endWaitBarrier) {
		SchedulerConfig config = TestingScheduler.createSchedulerConfiguration(
			/* thread name: */"Schedulder[" + name + "]",
			/* polling interval: */10,
			/* max task time: */5000,
			/* time before task kill: */1000,
			/* max tasks: */1000);
		scheduler = (TestingScheduler) getInstantiationContext().getInstance(config);
		scheduler.setBarrier(startWaitBarrier);
		scheduler.setEndWaitBarrier(endWaitBarrier);
		scheduler.accessibleStartUp();
	}

	private static InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

    /** 
     * Return the suite of tests to execute.
     */
    public static Test suite () {
		return TestingScheduler.wrapSchedulerDependenciesSetup(TestScheduler.class);
	}

	/**
	 * creates a new {@link TestingScheduler} with the given properties
	 * 
	 * @param name
	 *        Name of the scheduler
	 * @param pollingInterval
	 *        Sleeping time between checks.
	 * @param maxTasktime
	 *        maximum time a task or batch may run until it will be forcefully stopped.
	 * @param maxTask
	 *        Maximum concurrent number of tasks to execute.
	 */
	public void createScheduler(String name, long pollingInterval, long maxTasktime, int maxTask) {
		scheduler = TestingScheduler.newStartedScheduler(name, pollingInterval, maxTasktime, 1000, maxTask);
    }

	private void stopAndJoin() throws InterruptedException {
		stopScheduler();
		joinScheduler();
	}

	private void stopScheduler() {
		if (scheduler != null) {
			scheduler.signalStop();
		}
	}
	
	private void joinScheduler() throws InterruptedException {
		if (scheduler != null) {
			scheduler.getSchedulerThread().join();
			scheduler = null;
		}
	}

}
