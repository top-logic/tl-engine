/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.util.sched.model.TestScheduler;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.entry.SchedulerEntry;
import com.top_logic.util.sched.entry.SchedulerEntryStorage;
import com.top_logic.util.sched.task.Task;

/**
 * Scheduler for Testing only.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestingScheduler extends Scheduler {

	public interface TestingSchedulerConfig extends SchedulerConfig {

		@Override
		@ClassDefault(TestingScheduler.class)
		Class<? extends Scheduler> getImplementationClass();
	}
    
	/** Calendar we use to accelerate Things a bit ;-) */
    public volatile Calendar accelCal;

	private int _startedTasks = 0;

	private volatile CyclicBarrier _startWaitBarrier;

	private volatile CyclicBarrier _endWaitBarrier;

	public TestingScheduler(InstantiationContext context, TestingScheduler.TestingSchedulerConfig config) {
    	super(context, config);
    }

	/** @see #getStartWaitBarrier() */
	public CyclicBarrier getBarrier() {
		return getStartWaitBarrier();
	}

	/**
	 * If the barrier is not null, the {@link TestingScheduler} waits on it at the beginning of
	 * {@link #waitForWork()}.
	 */
	public CyclicBarrier getStartWaitBarrier() {
		return _startWaitBarrier;
	}

	/** @see #setStartWaitBarrier(CyclicBarrier) */
	public void setBarrier(CyclicBarrier barrier) {
		setStartWaitBarrier(barrier);
	}

	/**
	 * See: {@link #getStartWaitBarrier()}
	 * <p>
	 * Set to null if the {@link TestingScheduler} should not wait.
	 * </p>
	 */
	public void setStartWaitBarrier(CyclicBarrier barrier) {
		_startWaitBarrier = barrier;
	}

	/**
	 * If the barrier is not null, the {@link TestingScheduler} waits on it at the end of
	 * {@link #waitForWork()}.
	 */
	public CyclicBarrier getEndWaitBarrier() {
		return _endWaitBarrier;
	}

	/**
	 * See: {@link #getEndWaitBarrier()}
	 * <p>
	 * Set to null if the {@link TestingScheduler} should not wait.
	 * </p>
	 */
	public void setEndWaitBarrier(CyclicBarrier barrier) {
		_endWaitBarrier = barrier;
	}

    /**
	 * Overridden to use the accelerated calendar.
     */
    @Override
	public long now() {
    	if (accelCal != null) {
    		return accelCal.getTimeInMillis();
    	}
    	return super.now();
     }

    /**
     * Make function visible for testing.
     */
    @Override
	public void dispatch() {
        super.dispatch();
    }

	@Override
	protected void dispatchWithThreadContext() {
		/* Updates the session revision here: This should actually be unnecessary and a noop,
		 * but by an implementation detail the session revision is lazy created, i.e. on the
		 * first access it is installed and the last revision of the knowledge base. For some
		 * tests it is necessary the the session revision is determined when the sub session is
		 * installed, therefore this method is called because it pins the session revision. */
		try {
			HistoryUtils.updateSessionRevision();
		} catch (MergeConflictException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
		super.dispatchWithThreadContext();
	}

	@Override
	protected void waitForWork() {
		super.waitForWork();
		CyclicBarrier startWaitBarrier = _startWaitBarrier;
		if (startWaitBarrier != null) {
			TestScheduler.waitOnBarrier(startWaitBarrier);
		}
		CyclicBarrier endWaitBarrier = _endWaitBarrier;
		if (endWaitBarrier != null) {
			TestScheduler.waitOnBarrier(endWaitBarrier);
		}
	}

	@Override
	protected synchronized void startTask(SchedulerEntry entry) {
		_startedTasks += 1;
		super.startTask(entry);
	}

	/** The number of times the {@link Scheduler} started a {@link Task}. */
	public synchronized int getStartedTasks() {
		return _startedTasks;
	}

    /**
     * Make function visible for testing.
     */
    public final void accessibleStartUp() {
    	super.startUp();
    }

	@Override
	public SchedulerEntryStorage getEntries() {
		return super.getEntries();
	}

	/**
	 * Stops this scheduler and waits until thread has been stopped.
	 * 
	 * @param timeout
	 *        Expected time in milliseconds which are necessary to stop the scheduler thread. If it
	 *        does not stop within these timeout, an {@link AssertionFailedError} is thrown.
	 */
	public void stopAndJoin(long timeout) throws InterruptedException {
		signalStop();
		Thread schedulerThread = getSchedulerThread();
		schedulerThread.join(timeout);
		if (schedulerThread.isAlive()) {
			throw new AssertionFailedError("Scheduler '" + getName() + "' does not stop within " + timeout + "ms.");
		}
	}

	/**
	 * Returns the {@link Thread} used by this {@link TestingScheduler}.
	 */
	public Thread getSchedulerThread() {
		return getThread(this);
	}

	/** Reflective access to the scheduler-thread field in the {@link Scheduler}. */
	public static final Thread getThread(Scheduler sc) {
		return (Thread) ReflectionUtils.getValue(sc, "schedulerThread");
	}

	/**
	 * Wrap a setup around the given test class that starts the dependencies of the
	 * {@link Scheduler} needed for tests, but not the {@link Scheduler} itself.
	 */
	public static Test wrapSchedulerDependenciesSetup(Class<? extends Test> testClass) {
		Test innerSetup = moduleSetup(testClass, MaintenanceWindowManager.Module.INSTANCE);
		return KBSetup.getSingleKBTest(innerSetup);
	}

	@SuppressWarnings("unused")
	private static Test moduleSetup(Class<? extends Test> testClass, BasicRuntimeModule<?>... modules) {
		if (!true) {
			Test test = TestSuite.createTest(testClass, "testShutdown");
			return ServiceTestSetup.createSetup(test, modules);
		}
		return ServiceTestSetup.createSetup(testClass, modules);
	}

	/**
	 * creates a new {@link TestingScheduler} with the given properties
	 *
	 */
	public static TestingScheduler newStartedScheduler(String name) {
		return newStartedScheduler(name, 10, TestScheduler.TASK_TIMEOUT, 1000*60, 4);
	}

	/**
	 * creates a new {@link TestingScheduler} with the given properties
	 * 
	 * @param name
	 *        Part of the name of the scheduler.
	 * @param pollingInterval
	 *        Sleeping time between checks.
	 * @param maxTasktime
	 *        maximum time a task or batch may run until it will be forcefully stopped.
	 * @param timeToKill
	 *        Default maximum time a {@link Task} may use to react to stop.
	 * @param maxTask
	 *        Maximum concurrent number of tasks to execute.
	 */
	public static TestingScheduler newStartedScheduler(String name, long pollingInterval, long maxTasktime,
			long timeToKill, int maxTask) {
		TestingSchedulerConfig config =
			createSchedulerConfiguration(
				"Schedulder[" + name + "]", pollingInterval, maxTasktime, timeToKill, maxTask);
		return createAndStartScheduler(config);
	}

	/**
	 * Creates a new started {@link TestingScheduler}.
	 * 
	 * @param config
	 *        Configuration of the result.
	 */
	public static TestingScheduler createAndStartScheduler(TestingSchedulerConfig config) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(new AssertProtocol());
		TestingScheduler scheduler = (TestingScheduler) context.getInstance(config);
		try {
			context.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		scheduler.accessibleStartUp();
		return scheduler;
	}

	/**
	 * Creates a {@link TestingSchedulerConfig} with given values.
	 * 
	 * @param threadName
	 *        See {@link TestingSchedulerConfig#getThreadName()}.
	 * @param pollingInterval
	 *        See {@link TestingSchedulerConfig#getPollingInterval()}.
	 * @param maxTasktime
	 *        See {@link TestingSchedulerConfig#getMaxTasktime()}.
	 * @param timeToKill
	 *        See {@link TestingSchedulerConfig#getTimeToKill()}.
	 * @param maxTask
	 *        See {@link TestingSchedulerConfig#getMaxTask()}.
	 */
	public static TestingSchedulerConfig createSchedulerConfiguration(String threadName, long pollingInterval,
			long maxTasktime, long timeToKill, int maxTask) {
		Map<String, String> initialValues = new HashMap<>();
		initialValues.put("polling-interval", Long.toString(pollingInterval));
		initialValues.put("max-tasktime", Long.toString(maxTasktime));
		initialValues.put("time-to-kill", Long.toString(timeToKill));
		initialValues.put("max-task", Integer.toString(maxTask));
		initialValues.put("thread-name", threadName);
		try {
			return TypedConfiguration.newConfigItem(TestingSchedulerConfig.class, initialValues.entrySet());
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

}
