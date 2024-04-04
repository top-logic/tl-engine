/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import static com.top_logic.basic.col.filter.FilterFactory.*;
import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static com.top_logic.util.sched.entry.SchedulerEntryUtil.*;
import static com.top_logic.util.sched.task.TaskCommon.*;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.NodeState;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.GCQueue;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.format.MillisFormatInt;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.ScheduledThread;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.thread.LoggingExceptionHandler;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Suspendable;
import com.top_logic.basic.util.retry.Retry;
import com.top_logic.basic.util.retry.RetryResult;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.sched.Scheduler.SchedulerConfig;
import com.top_logic.util.sched.entry.SchedulerEntry;
import com.top_logic.util.sched.entry.SchedulerEntryStorage;
import com.top_logic.util.sched.entry.comparator.SchedulerEntryByTaskComparator;
import com.top_logic.util.sched.entry.filter.BrokenSchedulerEntryFilter;
import com.top_logic.util.sched.entry.filter.EnabledSchedulerEntryFilter;
import com.top_logic.util.sched.entry.filter.ManuallyStartedSchedulerEntryFilter;
import com.top_logic.util.sched.entry.filter.SchedulerEntryStateFilter;
import com.top_logic.util.sched.entry.filter.TopLevelSchedulerEntryFilter;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskComparator;
import com.top_logic.util.sched.task.TaskListenerNotification;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.log.TaskLogWrapper;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResultWrapper;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Scheduler for {@link Task}s.
 * 
 * <p>
 * {@link Task}s are executed periodically with configurable intervals.
 * </p>
 * 
 * @implNote The {@link Scheduler} is the only mechanism to run a {@link Task}. It is especially
 *           invalid to directly call a {@link Task}'s {@link Task#run() run method}. To explicitly
 *           request a task run, the {@link Task} must be {@link #getTaskByName(String) looked up}
 *           in the {@link Scheduler} and then {@link #startTask(Task) forced to run}.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	ClusterManager.Module.class,
	MaintenanceWindowManager.Module.class,
	SchedulerService.Module.class,
	ThreadContextManager.Module.class,
})
public class Scheduler extends ConfiguredManagedClass<SchedulerConfig> implements Reloadable, Runnable, Suspendable {

	/**
	 * Default value for {@link SchedulerConfig#getPollingInterval()}.
	 */
	public static final int DEFAULT_POLLING_INTERVAL = 60 * 1000;
	
	/**
	 * Configuration for {@link Scheduler}
	 */
	public interface SchedulerConfig extends ConfiguredManagedClass.Config<Scheduler> {

		/**
		 * How long to wait for the thread to finish during shutdown.
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@IntDefault(5000)
		@Format(MillisFormatInt.class)
		int getShutdownJoinTime();

		/**
		 * How often the {@link Scheduler} should check during its shutdown period whether all
		 * {@link Task}s have finished.
		 */
		@IntDefault(10)
		int getShutdownCountdown();

		/**
		 * How long to suspend after an error occurred.
		 * 
		 * <p>
		 * This suspend time prevents flooding the log files.
		 * </p>
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@LongDefault(30000)
		@Format(MillisFormat.class)
		long getWaitAfterFailTime();

		/**
		 * The maximum time a task waits for the maintenance mode to become active before checking
		 * whether it is already active.
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@LongDefault((10 * 1000))
		@Format(MillisFormat.class)
		long getMaxMaintenanceModePollingInterval();

		/**
		 * A task with a schedule in the past will result in a warning when this limit is reached.
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@LongDefault((10 * 60 * 1000))
		@Format(MillisFormat.class)
		long getPastTaskTime();

		/**
		 * Sleep time while waiting for startup.
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@LongDefault((1000L * 10L))
		@Format(MillisFormat.class)
		long getStartupSleep();

		/**
		 * Polling interval.
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@LongDefault(DEFAULT_POLLING_INTERVAL)
		@Format(MillisFormat.class)
		long getPollingInterval();

		/**
		 * Maximum amount of time that a task may run until it is forcefully stopped.
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@LongDefault((10 * 60 * 1000))
		@Format(MillisFormat.class)
		long getMaxTasktime();

		/**
		 * Maximum time that a {@link Task} may ignore a stop request.
		 * 
		 * <p>
		 * If this time is exceeded, a {@link Task} is killed.
		 * </p>
		 * 
		 * @implNote The configuration value is given in milliseconds.
		 */
		@LongDefault((1000 * 60))
		@Format(MillisFormat.class)
		long getTimeToKill();

		/**
		 * Maximum number of concurrent tasks to execute.
		 */
		@IntDefault(4)
		int getMaxTask();

		/**
		 * All configured tasks.
		 * 
		 * <p>
		 * A configured {@link Task} is only considered for scheduling, if its
		 * {@link com.top_logic.util.sched.task.Task.Config#isEnabled() enabled} flag is set.
		 * </p>
		 */
		@Key(Task.Config.NAME_ATTRIBUTE)
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		Map<String, Task.Config<?>> getTasks();

		/**
		 * Name of the system thread that executes the schedule.
		 */
		@StringDefault("DefaultScheduler")
		String getThreadName();

		/**
		 * Whether tasks should not run during system startup.
		 */
		boolean isDontRunTasksOnStartup();

		/**
		 * Types of persistent tasks. All instances of the given types are added as tasks.
		 */
		@Hidden
		@Format(CommaSeparatedStrings.class)
		List<String> getKbTasks();

	}

	private enum TaskStartResponse {

		/** Task has been started. Continue with the next task. */
		NEXT,

		/**
		 * Reschedule, but not immediately. Wait until the other tasks have been started. Wait, as
		 * otherwise the scheduler might end up trying to start just this task and ignoring the
		 * others. That would happen if this task has the lowest "getNextShed" value, but cannot be
		 * started for some reason. (Maintenance mode, cluster lock, ...)
		 */
		RESCHEDULE_DELAYED,

	}

	private static final String RESTART_FAILED =
		"Failed to restart the Scheduler. Tasks are not executed until the system is restarted.";

	private static final String DB_PROPERTY_BLOCKED_TASKS = Scheduler.class.getName() + ".";

	private final SchedulerEntryStorage _entries = new SchedulerEntryStorage();

    /**
	 * Polling interval.
	 */
    protected final long pollingInterval;
  
    /**
	 * maximum time a {@link Task} may run until it will be forcefully stopped.
	 */
    protected final long maxTasktime;

    /**
	 * Maximum Time a {@link Task} may use to react to <code>signalStop()</code>
	 */
    protected final long timeToKill;

    /**
     * Maximum concurrent number of tasks to execute.
     */
    protected final int maxTask;
    
    /**
     * Arbitrary String showing the current doings of the Scheduler.
     */
    protected final StringBuffer status;

    /** Signals that the Scheduler should stop.
     */
	private final AtomicBoolean _shouldStop = new AtomicBoolean(false);
    
    private volatile Thread schedulerThread;

	private final String threadName;

	private Date startTime;

	/** Flag indicating whether to wait for startup before running tasks. */
	protected final boolean dontRunTasksOnStartup;

	private Task _maintenanceModeRequester;

	private final Object _maintenanceModeLockObject = new Object();

	private final GCQueue<TaskListenerNotification> taskListenerRegistry = new GCQueue<>();

	private final List<Task> _clusterLocksToFix = new ArrayList<>();

	private final List<Task> _taskEndResultFix = new ArrayList<>();

	private boolean _fixLeaveMaintenanceMode = false;

	/**
	 * Stores the names and not the tasks itself to prevent an infinite recursion. This happens if a
	 * Task is searched by name via {@link #getTaskByName(String)} during the scheduler creation.
	 * And the task log cleanup is done during the scheduler creation.
	 */
	private final List<String> _taskLogsToFix = new ArrayList<>();

	private volatile boolean _suspended = false;

	private boolean _isDuringModuleStartup = true;

	private final DBProperties _dbProperties;

	/** Called by the typed configuration for creating a {@link Scheduler}. */
	@CalledByReflection
	public Scheduler(InstantiationContext context, SchedulerConfig config) {
		super(context, config);
		threadName = config.getThreadName();
		status = new StringBuffer("Setting up ").append(threadName);
		pollingInterval = config.getPollingInterval();
		maxTasktime = config.getMaxTasktime();
		timeToKill = config.getTimeToKill();
		maxTask = config.getMaxTask();
		dontRunTasksOnStartup = config.isDontRunTasksOnStartup();
		registerTasks(context, config);
		_dbProperties = new DBProperties(ConnectionPoolRegistry.getDefaultConnectionPool());
	}

	private void registerTasks(InstantiationContext context, SchedulerConfig config) {
		registerConfiguredTasks(context, config);
		registerKBTask(context, config);
	}

	private void registerConfiguredTasks(InstantiationContext context, SchedulerConfig config) {
		for (Task.Config<?> taskConfig : config.getTasks().values()) {
			Task configuredTask = context.getInstance(taskConfig);
			if (taskConfig.isEnabled()) {
				addTask(configuredTask);
			} else {
				addDisabledTask(configuredTask);
			}
		}
	}

	/**
	 * Add all the tasks found in the default knowledgeBase
	 */
	private void registerKBTask(InstantiationContext context, SchedulerConfig config) {

		KnowledgeBase aKB = PersistencyLayer.getKnowledgeBase();

		for (String kbType : config.getKbTasks()) {
			// Add all tasks of the given Type.
			for (KnowledgeObject ko : aKB.getAllKnowledgeObjects(kbType)) {
				Wrapper koTask = WrapperFactory.getWrapper(ko);
				if (koTask instanceof Task) {
					this.addTask((Task) koTask);
				} else {
					context.error("Knowledge object '" + ko + "' of type '" + kbType + "' is not a task");
				}
			}
		}
	}

	private void startNewThread() {
		_shouldStop.set(false);
        Thread t = new Thread(this, threadName);
        t.setDaemon(true);
        t.start();
        this.schedulerThread = t;
    }
    
    /**
	 * The {@link SchedulerEntryStorage} stores all the {@link Task}s known to the Scheduler along
	 * with management information in {@link SchedulerEntry}s.
	 */
	protected SchedulerEntryStorage getEntries() {
		return _entries;
	}

	/**
	 * Add the {@link Task} to the list of known tasks.
	 * <p>
	 * The {@link Task} will be {@link #enable(Task) enabled} and therefore scheduled for execution.
	 * </p>
	 * 
	 * @param aTask
	 *        Is not allowed to be null. It's {@link Task#getName()} is not allowed to be null or
	 *        empty and has to be case-insensitive unique among {@link #getAllKnownTasks()}.
	 *        Otherwise, an exception is thrown.
	 * @throws NullPointerException
	 *         If the {@link Task} is null.
	 */
	public void addTask(Task aTask) {
		addKnownTask(aTask);
		enable(aTask);
    }

	/**
	 * Add the {@link Task} to the list of known tasks but don't {@link #isTaskEnabled(Task) enable}
	 * it.
	 * 
	 * @param task
	 *        Is not allowed to be null. It's {@link Task#getName()} is not allowed to be null or
	 *        empty and has to be case-insensitive unique among {@link #getAllKnownTasks()}.
	 *        Otherwise, an exception is thrown.
	 * @throws NullPointerException
	 *         If the {@link Task} is null.
	 */
	public void addDisabledTask(Task task) {
		addKnownTask(task);
	}

	/**
	 * @param task
	 *        Is not allowed to be null. Has to have a case-insensitive unique
	 *        {@link Task#getName() name} among {@link #getAllKnownTasks()}. The same rules apply
	 *        for the children of {@link CompositeTask} s, too, recursively.
	 * @throws NullPointerException
	 *         If the {@link Task} is null.
	 */
	private void addKnownTask(Task task) {
		getEntries().add(task);
		attach(getEntries().get(task).get());
	}

	/**
	 * Returns a new {@link List} containing all top-level tasks known to the {@link Scheduler}.
	 * 
	 * @see #getAllKnownTasks()
	 * 
	 * @return Never <code>null</code>
	 */
	public List<Task> getKnownTopLevelTasks() {
		return toTasks(getEntries().getTopLevelEntries());
	}

	/**
	 * Returns a new {@link List} containing all tasks and subtasks (recursively) known to the
	 * {@link Scheduler}.
	 * <p>
	 * There is no guaranteed order.<br/>
	 * It might be unmodifiable.
	 * </p>
	 * 
	 * @see #getKnownTopLevelTasks()
	 */
	public List<Task> getAllKnownTasks() {
		return toTasks(getEntries().getAllEntries());
	}

	/**
	 * Searches for a {@link Task} with the given name in {@link Scheduler#getAllKnownTasks()}.
	 * <p>
	 * Performance: Uses a {@link Map}: Name -> {@link Task}.
	 * </p>
	 * 
	 * @see #getTask(String)
	 * 
	 * @return <code>null</code>, if there is no {@link Task} with the given name.
	 */
	public Task getTaskByName(String name) {
		Maybe<SchedulerEntry> task = getEntries().get(name);
		if (task.hasValue()) {
			return task.get().getTask();
		}
		return null;
	}

	/**
	 * Searches for a {@link Task} with the given name in {@link Scheduler#getAllKnownTasks()}.
	 * <p>
	 * Performance: Uses a {@link Map}: Name -> {@link Task}.
	 * </p>
	 */
	public Maybe<Task> getTask(String name) {
		return toTask(getEntries().get(name));
	}

	/**
	 * Remove a Task.
	 * <p>
	 * If there are knowledge based tasks they have to be removed, when the task is finished (this
	 * is needed for EWE activities).
	 * </p>
	 * 
	 * @return true when {@link Task} was successfully removed. If the {@link Task} is not
	 *         registered at the Scheduler: false.
	 * */
    public boolean removeTask(Task aTask) {
		Maybe<SchedulerEntry> entry = getEntries().get(aTask);
		if (!entry.hasValue()) {
			return false;
		}
		disable(entry.get());
		getEntries().remove(aTask);
		if (entry.get().isAttached()) {
			aTask.detachFromScheduler();
		}
		return true;
    }

    /**
	 * Signal the {@link Scheduler} that it should stop.
	 * <p>
	 * Expect the {@link Scheduler} to run some seconds until all {@link Task}s could be stopped
	 * successfully.
	 * </p>
	 */
    public void signalStop() {
		// Excluding SpotBugs(JLM_JSR166_UTILCONCURRENT_MONITORENTER).
		// "synchronized" and "notifyAll()" could be replaced by a java.util.concurrent.locks.Lock,
		// but this would need a lot of refactoring where small mistakes could alter the outcome.
		// There exist other solutions to this problem, but it is unclear whether they would change
		// the behavior.
		synchronized (_shouldStop) {
			_shouldStop.set(true);
			_shouldStop.notifyAll();
		}
    }

    /**
	 * Signal shutdown to all {@link Task}s.
	 * 
	 * Called when Scheduler is going down.
	 */
	protected synchronized void signalShutdown() {
		Iterator<SchedulerEntry> entryIterator = getActiveEntries().iterator();
		int count = 0;
		while (entryIterator.hasNext()) {
			SchedulerEntry entry = entryIterator.next();
			ScheduledThread thread = entry.getThread();
			if ((thread == null) || thread.isAlive()) {
				entry.getTask().signalShutdown();
				count++;
			}
		}
		if (count > 0) {
			status.append(" Signaled ").append(count).append(" Threads to stop");
		}
    }
    
    /** helper function to limit deprecation warning to one place.*/
	protected static final void stop(SchedulerEntry entry) {
		Logger.error("Stopping " + entry.getTask().getName(), Scheduler.class);
		entry.getThread().stop();
    }
    
    
	/**
	 * Stop all {@link Task}s that are still active.
	 * 
	 * Called when Scheduler is going down.
	 */
	protected synchronized void stopStillActive() {
		Iterator<SchedulerEntry> entriesIterator = getActiveEntries().iterator();
		while (entriesIterator.hasNext()) {
			SchedulerEntry entry = entriesIterator.next();
			ScheduledThread thread = entry.getThread();
			if ((thread != null) && thread.isAlive()) {
				stop(entry);
			}
		}
    }

	/** Starts the given task. */
	protected void startTask(SchedulerEntry entry) {
		Task task = entry.getTask();
		if (task.needsMaintenanceMode()) {
			synchronized (_maintenanceModeLockObject) {
				if ((!isInMaintenanceMode()) && !isEnteringMaintenanceMode()) {
					_maintenanceModeRequester = task;
					requestMaintenanceMode(task.getMaintenanceModeDelay());
				}
			}
			if (!isInMaintenanceMode()) {
				Logger.info("Task '" + task.getName() + "' has to wait for the maintenance mode.", Scheduler.class);
				waitInNewThread(entry);
				return;
			}
		}
		startTaskNow(entry);
    }

	private void waitInNewThread(final SchedulerEntry entry) {
		final String name = entry.getTask().getName();
		Runnable runnable = () -> {
			boolean success = false;
			try {
				waitForMaintenanceMode();
				if (_shouldStop.get()) {
					Logger.info("Task '" + name + "' will not be executed, because scheduler is stopping.",
						Scheduler.class);
					return;
				}
				Logger.info("Task '" + name
					+ "' finished waiting for the maintenance mode, as it is active now.", Scheduler.class);
				startTaskNow(entry);
				success = true;
			} finally {
				if (!success) {
					Logger.error("Task '" + name + "' failed while waiting for the maintenance mode."
						+ " It will be scheduled again as soon as possible.", Scheduler.class);
					entry.setState(SchedulerEntry.State.WAITING);
				}
			}
		};
		Thread waitThread = new Thread(runnable, "Waiting on maintenance mode for " + name);
		waitThread.setUncaughtExceptionHandler(LoggingExceptionHandler.INSTANCE);
		waitThread.start();
		entry.setState(SchedulerEntry.State.WAITING_FOR_MAINTENANCE_MODE);
	}

	private void startTaskNow(SchedulerEntry entry) {
		entry.createThread();
		entry.getThread().setUncaughtExceptionHandler(LoggingExceptionHandler.INSTANCE);
		entry.getThread().start(now());
		entry.setState(SchedulerEntry.State.RUNNING);
		Logger.info("Started task: " + entry, this);
	}

	private void startTasks() {
		long count = 0;
		List<SchedulerEntry> waiting = getWaitingEntries();
		while (waiting.size() > 0) {
			int running = getActiveEntries().size();
			if (running >= maxTask) {
				Logger.info("Tasks are not scheduled, as " + running + " are alreay running and "
						+ maxTask + " is the maximum.", Scheduler.class);
				break;
			}

			SchedulerEntry entry = waiting.remove(0);
			long nextShed = entry.getTask().getNextShed();
			if (nextShed == SchedulingAlgorithm.NO_SCHEDULE) {
				logNoNextSchedule(entry.getTask());
				entry.setState(SchedulerEntry.State.DONE);
				// Implicitly remove the task from 'tasks' by not re-adding it.
				continue;
			}
			if (now() < nextShed) {
				// If it is too early for this task, it is also too early for all other tasks in
				// the list, as the list is sorted by the task start times. Therefore, don't try to
				// schedule any further tasks this time.
				break;
			}
			entry.setState(SchedulerEntry.State.STARTING);
			TaskStartResponse requestedResponse = tryStartTask(entry);
			switch (requestedResponse) {
				case NEXT:
					count++;
					break;
				case RESCHEDULE_DELAYED:
					entry.setState(SchedulerEntry.State.WAITING);
					break;
			}
		}
		if (count > 0) {
			status.append(" Started ").append(count);
		}
	}

	private List<SchedulerEntry> getWaitingEntries() {
		@SuppressWarnings("unchecked")
		List<SchedulerEntry> result = toList(getEntries().getAllEntries(
			and(
				TopLevelSchedulerEntryFilter.INSTANCE,
				or(
					EnabledSchedulerEntryFilter.INSTANCE,
					ManuallyStartedSchedulerEntryFilter.INSTANCE),
				not(BrokenSchedulerEntryFilter.INSTANCE),
				new SchedulerEntryStateFilter(SchedulerEntry.State.WAITING))));
		Collections.sort(result, new SchedulerEntryByTaskComparator(TaskComparator.INSTANCE));
		return result;
	}

	private TaskStartResponse tryStartTask(SchedulerEntry entry) {
		Task task = entry.getTask();
		try {
			return tryStartTaskUnsafe(entry);
		} catch (Throwable ex) {
			logError("Failed to start task '" + task.getName() + "'. The task might be in an inconsistent state."
				+ " That can cause duplicate, missing or wrong executions.", ex);
			synchronized (_maintenanceModeLockObject) {
				_fixLeaveMaintenanceMode = (_maintenanceModeRequester == task);
			}
			if ((!task.isNodeLocal()) && task.isPersistent() && ((TaskLogWrapper) task.getLog()).hasClusterLock()) {
				_clusterLocksToFix.add(task);
			}
			return ensureActiveOrScheduled(entry);
		}
	}

	private synchronized TaskStartResponse ensureActiveOrScheduled(SchedulerEntry entry) {
		switch (entry.getState()) {
			case STARTING: {
				entry.removeThread();
				return TaskStartResponse.RESCHEDULE_DELAYED;
			}
			case WAITING: {
				return TaskStartResponse.RESCHEDULE_DELAYED;
			}
			case DONE: {
				return TaskStartResponse.NEXT;
			}
			case RUNNING: {
				return TaskStartResponse.NEXT;
			}
			case WAITING_FOR_MAINTENANCE_MODE: {
				return TaskStartResponse.NEXT;
			}
			default: {
				String message = "Task in unknown state '" + entry.getState() + "'. " + entry.toString();
				Logger.error(message, new UnreachableAssertion(message), Scheduler.class);
				entry.setState(SchedulerEntry.State.RUNNING);
				return TaskStartResponse.RESCHEDULE_DELAYED;
			}
		}
	}

	private TaskStartResponse tryStartTaskUnsafe(SchedulerEntry entry) {
		Task task = entry.getTask();
		enforceTaskLog(task);
		if (_taskLogsToFix.contains(task.getName())) {
			Logger.warn("Task '" + task.getName() + "' cannot be started, as the task log has to be repaired first.",
				Scheduler.class);
			return TaskStartResponse.RESCHEDULE_DELAYED;
		}
		long now = now();
		if (isTaskBlocked(task)) {
			// Don't just delay the execution until it is unblocked,
			// but skip to the next schedule time.
			// If the task should be executed directly after being unblocked,
			// the user can start it manually.
			// But the user cannot wait to unblock it until it should be executed,
			// as that might be in the middle of the night.
			task.markAsRun(now);
			task.calcNextShed(now);
			Logger.info("Execution of Task '" + task.getName()
				+ "' has been skipped, as it is blocked.", Scheduler.class);
			return TaskStartResponse.RESCHEDULE_DELAYED;
		}
		if (delayDueToMaintenanceMode(task)) {
			Logger.info("Task '" + task.getName()
				+ "' cannot be started, as the maintenance mode is active (or requested)"
				+ " and the task is not configured as maintenance mode compatible.", Scheduler.class);
			return TaskStartResponse.RESCHEDULE_DELAYED;
		}
		if ((!task.isNodeLocal()) && !task.isPersistent()) {
			String message = "A none local but none-persistent Task ('" + task.getName()
				+ "') is not (yet) supported. The task cannot be executed.";
			Exception exception = new UnsupportedOperationException(message);
			Logger.error(message, exception, this);
			entry.markAsBroken(exception);
			entry.setState(SchedulerEntry.State.DONE);
			return TaskStartResponse.NEXT;
		}
		if ((!task.isNodeLocal()) && task.isPersistent()) {
			boolean runLocally = requestClusterLock(task);
			if (!runLocally) {
				task.markAsRun(getRemoteStartDate(task).getTime());
				task.calcNextShed(now);
				Logger.info("Task '" + task.getName() + "' cannot be started,"
					+ " as it is or recently was running on an other cluster node.", Scheduler.class);
				return TaskStartResponse.RESCHEDULE_DELAYED;
			}
		}

		if ((now - task.getNextShed()) > getConfig().getPastTaskTime()) {
			Logger.warn("Task " + task.getName() + " execution to late " + nowDate(), this);

		}
		startTask(entry);
		return TaskStartResponse.NEXT;
	}

	/**
	 * Make sure the {@link Task#getLog()} is initialized without an outer transaction that could
	 * fail and invalidate a {@link TaskLogWrapper} that is stored as instance variable of the
	 * {@link Task}.
	 */
	private void enforceTaskLog(final Task task) {
		task.getLog();
	}

	private boolean delayDueToMaintenanceMode(Task task) {
		if (isAllowedInMaintenanceMode(task)) {
			return false;
		}
		return isInMaintenanceMode() || isEnteringMaintenanceMode();
	}

	private Date getRemoteStartDate(final Task task) {
		TaskResult currentResult = task.getLog().getCurrentResult();
		if (currentResult == null) {
			return new Date(SchedulingAlgorithm.NO_SCHEDULE);
		}
		return currentResult.getStartDate();
	}

	/**
	 * Requests the cluster lock to run this {@link Task} on this cluster node.
	 * 
	 * @return <code>true</code> if the cluster lock was set and the task has to be executed on this
	 *         cluster node. <code>false</code> if another node got the lock and the task must not
	 *         be run on this one.
	 */
	private boolean requestClusterLock(final Task task) {
		// No retry necessary, as a failed request means, an other cluster node is running the task.
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			TaskLogWrapper logWrapper = (TaskLogWrapper) task.getLog();
			logWrapper.touch();
			if (isTaskLocked(logWrapper) || runRecently(task)) {
				return false;
			}
			logWrapper.markAsRunning();
			logWrapper.setClusterLock();
			transaction.commit();
			_clusterLocksToFix.remove(task);
			return true;
		} catch (KnowledgeBaseException ex) {
			// A failed request happens if the task is running somewhere else.
			// Therefore, it is a lock request denial.
			// It can also happen if the database is down.
			// But in that case, no lock can be requested anyway
			// and the task cannot be executed now.
			lockRequestDenied(task, ex);
			return false;
		} finally {
			transaction.rollback();
		}
	}

	private void lockRequestDenied(Task task, Throwable exception) {
		String message = "Cluster lock request was denied, schedule of task '" + task.getName()
			+ "' will be updated. Technical reason: " + exception;
		Logger.info(message, exception, Scheduler.class);
	}

	private boolean isTaskLocked(TaskLogWrapper logWrapper) {
		return logWrapper.isClusterLockSet() && !logWrapper.hasClusterLock();
	}

	private boolean runRecently(Task task) {
		TaskLogWrapper logWrapper = (TaskLogWrapper) task.getLog();
		TaskResultWrapper currentResult = logWrapper.getCurrentResult();
		if (currentResult == null) {
			return false;
		}
		long lastRun = currentResult.getStartDate().getTime();
		boolean update = task.markAsRun(lastRun);
		if (update) {
			long now = now();

			long nextSchedule = task.calcNextShed(Math.max(task.getLastSchedule() + 1, now - pollingInterval));
			if (nextSchedule == SchedulingAlgorithm.NO_SCHEDULE) {
				// If the task does not want to run any more, something changed,
				// as this method is only called to run the task.
				return true;
			}

			return nextSchedule > now;
		}
		return false;
	}

	/** Stop all {@link Task}s that where executing to long, some way */
	protected void handleOvertime(Stack<SchedulerEntry> overtime) {
        while (overtime.size() > 0) {
			SchedulerEntry entry = overtime.pop();
			entry.setOvertimeIgnored(true);
			Task theCulprit = entry.getTask();
			Logger.info("Signal stop to '" + theCulprit.getName() + "' as it is running too long. Started: "
				+ format(new Date(theCulprit.getStarted())), Scheduler.class);
			if (!signalStop(entry)) {
				Logger.error("Failed to signalStop() to " + theCulprit + " will be ignored", this);
				continue;
            }
            try {
				entry.getThread().join(timeToKill);
            }
            catch (InterruptedException ix) {
				Logger.error("Interupted while handleOvertime()", this);
            }
			if (entry.getThread().isAlive()) {
				Logger.error("Failed to signalStop() to " + theCulprit + " calling stop()", this);
				stop(entry);
            }
        }
        
    }

	private boolean signalStop(SchedulerEntry entry) {
		try {
			return entry.getThread().signalStop();
		} catch (RuntimeException ex) {
			Logger.error("Failed to signal task '" + entry.getTask().getName() + "' to stop: " + ex.getMessage(), ex,
				Scheduler.class);
			return false;
		}
	}

	/** Get a {@link Date} based on the (faked) time from {@link #now()}. */
	protected final Date nowDate() {
		return new Date(now());
	}

	/** The current {@link Scheduler} time. */
	public long now() {
        return System.currentTimeMillis();
    }

	/** Reschedule {@link Task} if they desire so. */
	protected void handleDone(Stack<SchedulerEntry> done) {
		// It is possible that the done thread changed its state after the scheduler awoke.
		updateSessionRevision();
        while (done.size() > 0) {
			SchedulerEntry entry = done.pop();
			entry.setOvertimeIgnored(false);
			taskDone(entry);
			reschedule(entry, now());
        }
    }

	/**
	 * Is called in {@link #handleDone(Stack)}.
	 * <p>
	 * Subclasses which override this method have to call 'super', before doing anything else.
	 * </p>
	 * 
	 * @param entry
	 *        The one that is done now.
	 */
	protected void taskDone(SchedulerEntry entry) {
		Task task = entry.getTask();
		try {
			// This check has to come before the cluster lock is released.
			// Otherwise, the task may start on another node and
			// this check will fail and cause a wrong TaskResults to be written.
			checkForTaskEndResult(task);
		} finally {
			// This code has to be in the "finally" clause to ensure it is always run,
			// even if the methods above fails. But it cannot come first, as explained above.
			if ((!task.isNodeLocal()) && task.isPersistent()) {
				clearClusterLock(task);
			}
		}
		cleanupMaintenanceMode(task);
		entry.removeThread();
		entry.setState(SchedulerEntry.State.WAITING);
	}

	private void checkForTaskEndResult(Task task) {
		if (task.getLog().getState() != TaskState.INACTIVE) {
			Logger.error("Task '" + task.getName() + "' ended, but no task end result was written.", Scheduler.class);
			_taskEndResultFix.add(task);
		}
	}

	/**
	 * Can be called by a {@link Task} that initially needed the maintenance mode, but does not need
	 * it any longer. The {@link Scheduler} will leave the maintenance mode if it otherwise would
	 * have left it when the task was done. (Which means: If the maintenance mode was requested for
	 * this task and the task was not started manually.)
	 * 
	 * @param waivingTask
	 *        Has to be the {@link Task} calling this method.
	 */
	public void waiveMaintenanceMode(Task waivingTask) {
		cleanupMaintenanceMode(waivingTask);
	}

	private void cleanupMaintenanceMode(Task task) {
		synchronized (_maintenanceModeLockObject) {
			if (_maintenanceModeRequester == task) {
				_maintenanceModeRequester = null;
				if (isInMaintenanceMode()) {
					leaveMaintenanceMode();
				} else {
					Logger.info("Task '" + task.getName()
						+ "' requested the maintenance mode, but it was deactivated before the task ended.",
						Scheduler.class);
				}
			}
		}
	}

	/**
	 * Recalculate the next execution time of the {@link Task}.
	 * <p>
	 * The {@link Task} has to be registered at the {@link Scheduler}.
	 * </p>
	 */
	private void reschedule(SchedulerEntry schedulerEntry, long notBefore) {
		Task task = schedulerEntry.getTask();
		task.calcNextShed(notBefore);
		synchronized (this) {
			SchedulerEntry.State state = schedulerEntry.getState();
			if ((state != SchedulerEntry.State.WAITING) && (state != SchedulerEntry.State.DONE)) {
				return;
			}
			if (task.getNextSchedule().hasValue()) {
				schedulerEntry.setState(SchedulerEntry.State.WAITING);
			} else {
				logNoNextSchedule(task);
				schedulerEntry.setState(SchedulerEntry.State.DONE);
			}
		}
	}

	/**
	 * Logs a message on level "info" that the given {@link Task} will not be scheduled anymore.
	 * 
	 * @param task
	 *        Is not allowed to be null.
	 */
	protected void logNoNextSchedule(Task task) {
		String message = "Task requested to be scheduled no more. Task: "
			+ StringServices.getObjectDescription(task);
		Logger.info(message, Scheduler.class);
	}

	/**
	 * Compatibility redirect to {@link #isEnabled(Task)}, which has a more concise name.
	 * <p>
	 * The fact that the method {@link #isEnabled(Task)} is about a {@link Task}, is already
	 * expressed by its parameter type. Therefore, the word 'Task' is not needed in the name.
	 * </p>
	 * 
	 * @throws NullPointerException
	 *         If the {@link Task} is null.
	 */
	public boolean isTaskEnabled(Task task) {
		return isEnabled(task);
	}

	/**
	 * Is the {@link Task} enabled? If not, it is never run by the scheduler. (Unless the user
	 * explicitly forces the Scheduler to run the task.)
	 * 
	 * @see #enable(Task)
	 * @see #disable(Task)
	 * 
	 * @return false, if the {@link Task} is not registered at the {@link Scheduler}.
	 * 
	 * @throws NullPointerException
	 *         If the {@link Task} is null.
	 */
	public final boolean isEnabled(Task task) {
		Maybe<SchedulerEntry> entry = getEntries().get(task);
		if (entry.hasValue()) {
			return isEnabled(entry.get());
		}
		return false;
	}

	/**
	 * The part of the implementation of {@link #isEnabled(Task)} that can be overridden by
	 * subclasses.
	 */
	protected boolean isEnabled(SchedulerEntry entry) {
		return entry.isEnabled();
	}

	/**
	 * Enable the {@link Task} and schedule it for execution.
	 * 
	 * <p>
	 * <b>This settings is cluster node local and is not persisted, i.e. it will be reset on node
	 * restart and {@link Scheduler#reload()}.</b>
	 * </p>
	 * 
	 * <p>
	 * If the {@link Task} is not registered at the {@link Scheduler}, nothing happens.
	 * </p>
	 * 
	 * @see #isEnabled(Task)
	 * @see #disable(Task)
	 * 
	 * @throws NullPointerException
	 *         If the {@link Task} is null.
	 */
	public final void enable(Task task) {
		Maybe<SchedulerEntry> entry = getEntries().get(task);
		if (entry.hasValue()) {
			enable(entry.get());
		}
	}

	/**
	 * The part of the implementation of {@link #enable(Task)} that can be overridden by subclasses.
	 */
	protected void enable(SchedulerEntry entry) {
		entry.setEnabled(true);
		reschedule(entry, now());
	}

	/**
	 * Disable the {@link Task} and stop it from being executed.
	 * <p>
	 * <b>This settings is cluster node local and is not persisted, i.e. it will be reset on node
	 * restart and {@link Scheduler#reload()}.</b> <br/>
	 * If the {@link Task} is already executing, that execution will not be stopped. If the
	 * {@link Task} is already in the processing of being started "right now", it is unspecified
	 * whether that execution will happen or not.
	 * </p>
	 * <p>
	 * If the {@link Task} is not registered at the {@link Scheduler}, nothing happens.
	 * </p>
	 * 
	 * @see #isEnabled(Task)
	 * @see #enable(Task)
	 * 
	 * @throws NullPointerException
	 *         If the {@link Task} is null.
	 */
	public final void disable(Task task) {
		Maybe<SchedulerEntry> entry = getEntries().get(task);
		if (entry.hasValue()) {
			disable(entry.get());
		}
	}

	/**
	 * The part of the implementation of {@link #disable(Task)} that can be overridden by
	 * subclasses.
	 */
	protected void disable(SchedulerEntry entry) {
		entry.setEnabled(false);
	}

	/**
	 * Is the {@link Task} activated in the configuration?
	 * <p>
	 * Is also false for all {@link Task}s which are not configured, for example because they have
	 * been added to the {@link Scheduler} programmatically. <br/>
	 * Use {@link #isEnabled(Task)} to check if the {@link Task} is enabled in the {@link Scheduler}
	 * .
	 * </p>
	 */
	public boolean isEnabledInConfig(Task task) {
		Task.Config<?> config = getConfig().getTasks().get(task.getName());
		return config != null && config.isEnabled();
	}

	/** Is it allowed to run this {@link Task} in maintenance mode? */
	protected boolean isAllowedInMaintenanceMode(Task task) {
		return task.isForcedRun() || task.isMaintenanceModeSafe();
	}

	private boolean isInMaintenanceMode() {
		return getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE;
	}

	private boolean isEnteringMaintenanceMode() {
		return getMaintenanceModeState() == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE;
	}

	private int getMaintenanceModeState() {
		return MaintenanceWindowManager.getInstance().getMaintenanceModeState();
	}

	private long getTimeToWaitForMaintenanceMode() {
		long timeLeft = MaintenanceWindowManager.getInstance().getTimeLeft();
		if (timeLeft <= 0) {
			// never return 0 (thread would wait forever)
			// or negative value (Thread would throw IllegalArgumentException)
			return 1;
		}
		long maxPollingInterval = getConfig().getMaxMaintenanceModePollingInterval();
		if (timeLeft > maxPollingInterval) {
			/* wait at most the given number of milliseconds at once to avoid problems with big
			 * numbers from MaintenanceWindowManager specially if system is to be shut down */
			return maxPollingInterval;
		}
		return timeLeft;
	}

	private void waitForMaintenanceMode() {
		// Loop, as sleep can return early when interrupted or when the timer is inaccurate.
		while (true) {
			if (_shouldStop.get()) {
				break;
			}
			if (isInMaintenanceMode()) {
				Logger.info("Scheduler entered maintenance mode.", Scheduler.class);
				break;
			}
			internalWait(getTimeToWaitForMaintenanceMode());
		}
	}

	/**
	 * Sleeps for the given amount of milliseconds. Returns early if woken by an
	 * {@link InterruptedException}.
	 */
	private void sleep(long duration) {
		try {
			// 'max' is used to ensure sleep is not called with 0 or negative values,
			// as the behavior for these values is not documented.
			Thread.sleep(Math.max(duration, 1));
		} catch (InterruptedException ex) {
			return;
		}
	}

	private void requestMaintenanceMode(final long requestedDelay) {
		long actualDelay = Math.max(requestedDelay, getMaintenanceModeMinDelay());
		Logger.info("Scheduler is requesting maintenance mode in " + actualDelay + " ms.", Scheduler.class);
		MaintenanceWindowManager.getInstance().enterMaintenanceWindow(actualDelay);
	}

	private long getMaintenanceModeMinDelay() {
		return MaintenanceWindowManager.getInstance().minIntervallInCluster;
	}

	private void leaveMaintenanceMode() {
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			this::leaveMaintenanceModeUnsafe);
		if (!result.isSuccess()) {
			String message = "Scheduler failed to leave the maintenance mode after " + COMMIT_RETRIES + " attempts."
				+ " The scheduler will continue working normally,"
				+ " but will also keep trying to leave the maintenance mode."
				+ " The maintenance mode will remain active until that succeeds or an administrator deactivates it.";
			RuntimeException retryError = ExceptionUtil.createException(message, result.getReason());
			Logger.error(retryError.getMessage(), retryError, Scheduler.class);
			_fixLeaveMaintenanceMode = true;
		}
	}

	private RetryResult<Void, Throwable> leaveMaintenanceModeUnsafe() {
		try {
			Logger.info("Scheduler is leaving maintenance mode.", Scheduler.class);
			MaintenanceWindowManager.getInstance().leaveMaintenanceWindow();
			return RetryResult.createSuccess();
		} catch (RuntimeException ex) {
			return RetryResult.<Void, Throwable> createFailure(ex);
		}
	}

	private void clearClusterLock(final Task task) {
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			() -> clearClusterLockUnsafe(task));
		if (!result.isSuccess()) {
			String message = "Scheduler failed to clear the cluster lock for task '" + task.getName() + "' after "
				+ COMMIT_RETRIES + " attempts. The scheduler will continue working normally,"
				+ "but will also keep on trying to release the lock."
				+ " Until that succeeds, the cluster lock will remain"
				+ " and prevent any other cluster node from executing this task.";
			RuntimeException retryError = ExceptionUtil.createException(message, result.getReason());
			Logger.error(retryError.getMessage(), retryError, Scheduler.class);
			_clusterLocksToFix.add(task);
		}
	}

	private RetryResult<Void, Throwable> clearClusterLockUnsafe(Task task) {
		TaskLogWrapper logWrapper = (TaskLogWrapper) task.getLog();
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			logWrapper.touch();
			if (logWrapper.hasClusterLock()) {
				logWrapper.clearClusterLock();
				transaction.commit();
				return RetryResult.<Void, Throwable> createSuccess();
			} else if (logWrapper.isClusterLockSet()) {
				Logger.error("Cluster lock violation for task '" + task.getName() + "', lock value: name="
					+ StringServices.getObjectDescription(logWrapper.getClusterLockName()) + " id="
					+ logWrapper.getClusterLockId(), Scheduler.class);
				transaction.rollback();
				// "Success", as the lock is no longer held by this cluster node.
				return RetryResult.<Void, Throwable> createSuccess();
			} else {
				Logger.error("Cluster lock violation for task '" + task.getName() + "', lock has been reset.",
					Scheduler.class);
				// dito
				return RetryResult.<Void, Throwable> createSuccess();
			}
		} catch (KnowledgeBaseException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	/** internal method check for {@link Task}s running too long */
	protected void checkRunning() {
       
		// Tasks that where executing too long
       Stack<SchedulerEntry> overtime = null;
       
		// Tasks that are done are collected here
       Stack<SchedulerEntry> done     = null;
       
		synchronized (this) {
			long now = now();
			Iterator<SchedulerEntry> entries = getRunningTopLevelEntries().iterator();
			while (entries.hasNext()) {
				SchedulerEntry entry = entries.next();
				Task task = entry.getTask();
				if (entry.getThread() == null) {
					// Happens only in error cases
				}
				if (entry.getThread().getState() == State.TERMINATED) {
					// fine remove / reshedule it
                    if (done == null) {
                        done = new Stack<>();
                    }
                    done.push(entry);
					continue;
				}
				long started = task.getStarted();
				if (started == Task.STARTING) {
					// Task is about to start
						continue;
					}
				long diff = now - started;
				if (diff > maxTasktime && !entry.isOvertimeIgnored()) {
					Logger.info("Task " + task + " running too long " + (diff / 1000.0) + " seconds",
							Scheduler.class);
					if (overtime == null) {
						overtime = new Stack<>();
					}
					overtime.push(entry);
				}
			} // while
		} // synchronized
        
        // handle overtime outside of synchronized block ...
        if (overtime != null) {
            status.append(" Overtime ").append(overtime.size());
            handleOvertime(overtime);
        }

        if (done != null) {
            status.append(" Done ").append(done.size());
            handleDone(done);
        }
    }

	/**
	 * Returns a new {@link Collection} containing the {@link SchedulerEntry}s for all running
	 * top-level tasks.
	 * <p>
	 * There is no guaranteed order.<br/>
	 * The returned {@link Collection} is modifiable and resizeable.
	 * </p>
	 * 
	 * @return Never <code>null</code>
	 */
	protected Collection<SchedulerEntry> getRunningTopLevelEntries() {
		return getEntries().getAllEntries(
			and(
				TopLevelSchedulerEntryFilter.INSTANCE,
				new SchedulerEntryStateFilter(SchedulerEntry.State.RUNNING)));
	}

    /**
     * The inner part of the run method that dispatches the work. 
     * 
     * Allow subclasses/Testcases to access this */ 
    protected void dispatch() {
		try {
			dispatchUnsafe();
		} catch (Throwable ex) {
			Logger.error("Scheduler failed. It might be in an inconsistent or broken state, "
				+ "but will try to continue its work and fix its state. Cause: " + ex.getMessage(),
				ex, Scheduler.class);

			// If this problem will directly occur again on next dispatch, the server would flood
			// the log when not delaying for some time.
			internalWait(getConfig().getWaitAfterFailTime());
		}
	}

	private void dispatchUnsafe() {
		// The actual scheduling code has to run with a ThreadContext,
		// as some of the tasks persist their information in the KnowledgeBase.
		// And as the scheduler changes causes changes to these information,
		// it needs a ThreadContext to commit them.
		ThreadContext.inSystemContext(getClass(), this::dispatchWithThreadContext);
	}

	protected void dispatchWithThreadContext() {
		if (dontRunTasksOnStartup) {
			NodeState nodeState = ClusterManager.getInstance().getNodeState();
			if (nodeState != NodeState.RUNNING) {
				logInfo("Waiting for system startup to complete.");
				internalWait(getConfig().getStartupSleep());
				return;
			}
		}
		setStartTime();

		waitForWork();
		if (_shouldStop.get()) {
			return;
		}

		// ensure that the Scheduler finds the current results.
		updateSessionRevision();
        status.setLength(0);
		if (isSuspended()) {
			logDebug(Scheduler.class.getSimpleName() + " is suspended.");
			return;
		}
		status.append("Checking Tasks [");
		checkRunning();
        status.append(']');
		if (_shouldStop.get()) {
            return; 
        }
		status.append(" Running Tasks [");
		startTasks();
        status.append(']');
		applyFixes();
    }

	private void updateSessionRevision() {
		try {
			HistoryUtils.updateSessionRevision();
		} catch (MergeConflictException ex) {
			Logger.error("Merge conflict.", ex, Scheduler.class);
		}
	}

	/**
	 * Suspends this thread until new work has to be done.
	 */
	protected void waitForWork() {
		status.append(" ... sleeping ").append(DebugHelper.getTime(pollingInterval));
		Logger.info(status.toString(), this);
		internalWait(pollingInterval);
	}

	/**
	 * Sleep for the given amount of milliseconds.
	 * <p>
	 * Wakes up when the scheduler {@link #_shouldStop}.
	 * </p>
	 * <p>
	 * Values <= 0 mean: Don't wait.
	 * </p>
	 */
	private void internalWait(long waitTime) {
		if (waitTime <= 0) {
			return;
		}
		// Excluding SpotBugs(JLM_JSR166_UTILCONCURRENT_MONITORENTER).
		// See comment in signalStop() for further details.
		synchronized (_shouldStop) {
			if (_shouldStop.get()) {
				return;
			}
			try {
				_shouldStop.wait(waitTime);
			} catch (InterruptedException e) {
				Logger.error("Unexpected InterruptedException", e, this);
			}
		}
	}

	private void applyFixes() {
		leaveMaintenanceModeFix();
		releaseClusterLocksFix();
		writeTaskEndedResultFix();
		fixTaskLog();
	}

	private void leaveMaintenanceModeFix() {
		if (!_fixLeaveMaintenanceMode) {
			return;
		}
		Logger.info("Applying Fix: Deactivate Maintenance Mode.", Scheduler.class);
		try {
			synchronized (_maintenanceModeLockObject) {
				if (!isInMaintenanceMode()) {
					_fixLeaveMaintenanceMode = false;
					Logger.info("Applied Fix: Maintenance Mode was already deactivated.", Scheduler.class);
					return;
				}
			}
			Logger.info("Applying fix: Maintenance Mode is still active.", Scheduler.class);
			RetryResult<Void, Throwable> result = leaveMaintenanceModeUnsafe();
			if (result.isSuccess()) {
				_fixLeaveMaintenanceMode = false;
				Logger.info("Applied Fix: Deactivated Maintenance Mode.", Scheduler.class);
			} else {
				Throwable reason = result.getReason();
				Logger.error("Fix failed: Scheduler failed to leave the maintenance mode."
					+ " It will try again later. Cause: " + reason.getMessage(), reason, Scheduler.class);
			}
		} catch (Throwable ex) {
			Logger.error("Fix failed: Scheduler failed to leave the maintenance mode."
				+ " It will try again later. Cause: " + ex.getMessage(), ex, Scheduler.class);
		}
	}

	private void releaseClusterLocksFix() {
		if (_clusterLocksToFix.isEmpty()) {
			return;
		}
		Logger.info("Applying Fixes: Removing cluster locks.", Scheduler.class);
		Iterator<Task> fixesIterator = _clusterLocksToFix.iterator();
		while (fixesIterator.hasNext()) {
			Task task = fixesIterator.next();
			try {
				Logger.info("Applying Fix: Removing cluster lock for task '" + task.getName() + "'.", Scheduler.class);
				forceUncheckedMarkTaskAsInactive(task);
				fixesIterator.remove();
				Logger.info(
					"Applied Fix: Removed cluster lock for task '" + task.getName() + "'.", Scheduler.class);
			} catch (Throwable ex) {
				Logger.error("Fix failed: Scheduler failed to remove cluster lock for task '" + task.getName()
					+ "'. It will try again later. Cause: " + ex.getMessage(), ex, Scheduler.class);
			}
		}
	}

	private void forceUncheckedMarkTaskAsInactive(Task task) {
		TaskLogWrapper logWrapper = (TaskLogWrapper) task.getLog();
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			logWrapper.forceUncheckedMarkTaskAsInactive(task, I18NConstants.TASK_MARKED_AS_DONE_BY_SCHEDULER);
			transaction.commit();
		} finally {
			transaction.rollback();
		}
	}

	private void writeTaskEndedResultFix() {
		if (_taskEndResultFix.isEmpty()) {
			return;
		}
		Logger.info("Applying Fixes: Writing task end results.", Scheduler.class);
		Iterator<Task> fixesIterator = _taskEndResultFix.iterator();
		while (fixesIterator.hasNext()) {
			Task task = fixesIterator.next();
			try {
				Logger.info(
					"Applying Fix: Writing task end result for task '" + task.getName() + "'.", Scheduler.class);
				task.getLog().taskDied(I18NConstants.TASK_DONE_WITHOUT_RESULT);
				fixesIterator.remove();
				Logger.info("Applied Fix: Wrote task end result for task '" + task.getName() + "'.", Scheduler.class);
			} catch (Throwable ex) {
				Logger.error("Fix failed: Scheduler failed to write missing result for task '" + task.getName()
					+ "'. It will try again later. Cause: " + ex.getMessage(), ex, Scheduler.class);
			}
		}
	}

	private void fixTaskLog() {
		if (_taskLogsToFix.isEmpty()) {
			return;
		}
		Logger.info("Applying Fixes: Fixing task logs.", Scheduler.class);
		Iterator<String> fixesIterator = _taskLogsToFix.iterator();
		while (fixesIterator.hasNext()) {
			String taskName = fixesIterator.next();
			Maybe<Task> maybeTask = getTask(taskName);
			if (!maybeTask.hasValue()) {
				Logger.info("Fix failed: Fixing log of task '" + taskName + "' is not possible,"
					+ " as the Task is no longer registered at the Scheduler.", Scheduler.class);
				fixesIterator.remove();
				continue;
			}
			Task task = maybeTask.get();
			try {
				Logger.info("Applying Fix: Fixing log of task '" + task.getName() + "'.", Scheduler.class);
				// TransientTaskLogs are empty and therefore never corrupted on startup.
				// Therefore, this cast is safe here.
				TaskLogWrapper logWrapper = (TaskLogWrapper) task.getLog();
				RetryResult<Void, Throwable> result = logWrapper.tryStartupNodeClean(task);
				if (result.isSuccess()) {
					fixesIterator.remove();
					Logger.info("Applied Fix: Fixed log of task '" + task.getName() + "'.", Scheduler.class);
				} else {
					Throwable reason = result.getReason();
					Logger.error("Fix failed: Scheduler failed to fix log of task '" + task.getName()
						+ "', will try again later. Cause: " + reason.getMessage(), reason, Scheduler.class);
				}
			} catch (Throwable ex) {
				Logger.error("Fix failed: Scheduler failed to fix log of task '" + task.getName()
					+ "'. It will try again later. Cause: " + ex.getMessage(), ex, Scheduler.class);
			}
		}
	}

	/**
	 * Add a {@link TaskLog} that has to be fixed by the scheduler. Until then, the {@link Task}
	 * will not be scheduled.
	 */
	public void addTaskLogToFix(String taskName) {
		_taskLogsToFix.add(taskName);
	}

	/** Main Loop that will execute {@link Task}s */
	@Override
	public void run() {
        try {
			initThread();
			while (!_shouldStop.get()) {
			    dispatch();
			}
			shutDownThread();
		} catch (Throwable ex) {
			String message = "Scheduler failed. Tasks can no longer be scheduled. Cause: " + ex.getMessage();
			Logger.fatal(message, ex, Scheduler.class);
			throw new RuntimeException(message, ex);
		}
    }

	private final void initThread() {
		ThreadContext.inSystemContext(getClass(), this::initThreadWithThreadContext);
	}

	/**
	 * Initialization of the {@link Scheduler} in its own {@link Thread}.
	 */
	protected void initThreadWithThreadContext() {
		_isDuringModuleStartup = false;
		/* Initializing a {@link TaskLog} can cause a transaction that should not happen
		 * indeterministically on their first access. Additionally, the logs are checked for
		 * consistency problems caused by killed cluster nodes. That can cause transactions that
		 * might need to be rolled back. This must not happen during system startup, as there is an
		 * enclosing transaction that is not allowed to be rolled back. */
		attachTasks();
	}

	/**
	 * Initialize the {@link TaskLog}s.
	 */
	protected void attachTasks() {
		for (SchedulerEntry entry : getEntries().getTopLevelEntries()) {
			attach(entry);
		}
	}

	/**
	 * Attach the {@link Task} to this {@link Scheduler}.
	 */
	protected void attach(SchedulerEntry entry) {
		synchronized (this) {
			/* Tasks cannot be attached during the module startup, as there is an outer transaction
			 * that is not allowed to be rolled back, but attaching a Task can cause its TaskLog to
			 * be committed. */
			if (_isDuringModuleStartup) {
				return;
			}
			if (entry.isAttached()) {
				return;
			}
			entry.setAttached(true);
		}
		entry.getTask().attachTo(this);
	}

    /** Handle the shutting down */
	protected void shutDownThread() {
		// Has to run with a ThreadContext, as some of the tasks persist
		// their information in the KnowledgeBase. And as this method
		// changes causes changes to these information,
		// it needs a ThreadContext to commit them.
		ThreadContext.inSystemContext(getClass(), this::shutDownThreadWithThreadContext);
	}

	private void shutDownThreadWithThreadContext() {
        status.setLength(0);
        status.append("Shutting Down");

		int runCount = getActiveEntries().size();
        if (runCount == 0) {
            return; // fine fine
        }
        
        status.append(" Tasks");
		signalShutdown();
        
		int countdown = getConfig().getShutdownCountdown();
        long sleep     = timeToKill / countdown;
		while (runCount > 0 && countdown-- > 0) {
			sleep(sleep);
			checkRunning();
			runCount = getActiveEntries().size();
        }
        if (runCount > 0) {
			status.append(" Stopping ").append(runCount).append(" Tasks");
			stopStillActive();
        }
        Logger.info(status.toString(), this);
    }

    /**
     * Acessor for the polling interval, default DEFAULT_POLLING_INTERVAL.
     */
    public long getPollingInterval() {
        return pollingInterval;
    }
    
    /**
	 * Is the {@link Task} registered at the Scheduler?
	 */
	public boolean isKnown(Task task) {
		return getEntries().get(task).hasValue();
    }
    
    /**
     * This method returns the maxTasktime.
     */
    public long getMaxTasktime() {
        return (this.maxTasktime);
    }
    
    /**
     * This method returns the maxTask.
     */
    public int getMaxTask() {
        return (this.maxTask);
    }
    
    /**
	 * This method returns the number of Tasks (exclusive running ones).
	 */
    public int getNumTasks() {
		return getEntries().getTopLevelEntries().size();
    }

    /**
	 * This method returns the number of currently running top-level {@link Task}s.
	 * <p>
	 * Includes starting {@link Task}s.
	 * </p>
	 */
	public int getNumRunningTasks() {
		return getActiveEntries().size();
    }

	/**
	 * The {@link Task}s that are running or starting.
	 */
	protected Collection<SchedulerEntry> getActiveEntries() {
		return getEntries().getAllEntries(
			and(
				TopLevelSchedulerEntryFilter.INSTANCE,
				or(
					new SchedulerEntryStateFilter(SchedulerEntry.State.RUNNING),
					new SchedulerEntryStateFilter(SchedulerEntry.State.STARTING),
					new SchedulerEntryStateFilter(SchedulerEntry.State.WAITING_FOR_MAINTENANCE_MODE))
				)
			);
	}

    /**
     * This method returns the status.
     */
    public String getStatus() {
        return status.toString();
    }

    /**
	 * Return the time this scheduler has been executed last time.
	 * 
	 * @return The requested time, may be <code>null</code>.
	 */
	public synchronized Date getStartTime() {
		return startTime;
	}

	private synchronized void setStartTime() {
		startTime = nowDate();
	}

	/** Log a message on level "Debug". */
	protected void logDebug(String message) {
		if (Logger.isDebugEnabled(Scheduler.class)) {
			Logger.debug(message, Scheduler.class);
		}
	}

	/** Log a message on level "Info". */
	protected void logInfo(String message) {
		Logger.info(message, Scheduler.class);
	}

	/** Log a message with exception on level "error". */
	protected void logError(String message, Throwable throwable) {
		Logger.error(message + " Cause: " + throwable.getMessage(), throwable, Scheduler.class);
	}

	@Override
	public void suspend() {
		_suspended = true;
		logInfo("Suspending");
	}

	@Override
	public void resume() {
		skipAllMissedSchedules();
		_suspended = false;
		logInfo("Resuming");
	}

	private synchronized void skipAllMissedSchedules() {
		for (Task task : toTasks(getEntries().getTopLevelEntries())) {
			long now = now();
			if (task.getNextShed() < now) {
				task.markAsRun(now);
				task.calcNextShed(now);
			}
		}
	}

	@Override
	public boolean isSuspended() {
		return _suspended;
	}

    /**
     * Return our Name as Description.
     */
    @Override
	public String getDescription() {
        return getName();
    }
    
    /**
     * true since we actually use XMLProperties.
     * 
     * @see com.top_logic.basic.Reloadable#usesXMLProperties()
     */
    @Override
	public boolean usesXMLProperties() {
        return true;
    }

    @Override
	public boolean reload() {
		boolean unreportedError = true;
		try {
			Logger.info("Restarting Scheduler.", Scheduler.class);
			ModuleUtil.INSTANCE.restart(Scheduler.Module.INSTANCE, null);
			Logger.info("Scheduler restarted successfully.", Scheduler.class);
			unreportedError = false;
			return true;
		} catch (RuntimeException ex) {
			Logger.fatal(RESTART_FAILED + " Cause: " + ex.getMessage(), ex, Scheduler.class);
			unreportedError = false;
			throw new RuntimeException(RESTART_FAILED + ex.getMessage(), ex);
		} catch (RestartException ex) {
			Logger.fatal(RESTART_FAILED + " Cause: " + ex.getMessage(), ex, Scheduler.class);
			unreportedError = false;
			throw new RuntimeException(RESTART_FAILED + ex.getMessage(), ex);
		} finally {
			if (unreportedError) {
				Logger.fatal(RESTART_FAILED, Scheduler.class);
			}
		}
    }

    @Override
	public String getName() {
    	return schedulerThread.getName();
    }

	/** The {@link Scheduler}, if the service is active, <code>null</code> otherwise. */
    @CalledByReflection
    public static Scheduler getSchedulerInstance() {
		if (!Module.INSTANCE.isActive()) {
			return null;
		}
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    @Override
    protected void startUp() {
    	super.startUp();
    	startNewThread();

        ReloadableManager.getInstance().addReloadable(this);

    }

	@Override
	protected void shutDown() {
        signalStop();
        try {
			schedulerThread.join(getConfig().getShutdownJoinTime());
        } catch (InterruptedException e) {
            e.printStackTrace();    // This is ok here
        }
        
        if (schedulerThread.isAlive())
			System.err.println("Scheduler still running");
		unregisterTasks();
		super.shutDown();
	}

	private void unregisterTasks() {
		// Copy the list to prevent concurrent modification error
		for (Task task : new ArrayList<>(getKnownTopLevelTasks())) {
			removeTask(task);
		}
	}

	/**
	 * {@link BasicRuntimeModule} for access to the {@link Scheduler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<Scheduler> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<Scheduler> getImplementation() {
			return Scheduler.class;
		}

	}

	/**
	 * The update queue for {@link TaskListenerNotification task updates}.
	 */
	public GCQueue<TaskListenerNotification> getTaskUpdateQueue() {
		return taskListenerRegistry;
	}

	/**
	 * Force the execution of the given {@link Task} in the next possible time slot.
	 * 
	 * <p>
	 * If the task is run before the given time anyway, the task is run at the requested time,
	 * again. <br/>
	 * If the task is not {@link Task#isNodeLocal() node local} and is run on another cluster node,
	 * the forced run will still be executed. <br/>
	 * If the task is {@link Scheduler#isTaskBlocked(Task) blocked}, this method has no effect.
	 * </p>
	 * 
	 * <p>
	 * Whether the task is {@link Scheduler#isEnabled(Task) enabled or disabled in the
	 * configuration} is not relevant.
	 * </p>
	 */
	public void startTask(Task task) {
		Maybe<SchedulerEntry> handle = getEntries().get(task);
		if (handle.hasValue()) {
			SchedulerEntry entry = handle.get();

			Logger.info("Task '" + task.getName() + "' is started manually.", Scheduler.class);

			long now = now();
			task.forceRun(now);
			reschedule(entry, now);
			notifyUpdate(task);
		}
	}

	private String format(Date start) {
		return HTMLFormatter.getInstance().formatDate(start);
	}

	/**
	 * Is it allowed to block the given {@link Task}?
	 * <p>
	 * Some system critical tasks are not allowed to be blocked.
	 * </p>
	 * 
	 * @see #blockTask(Task)
	 */
	public boolean isBlockingAllowed(Task task) {
		return task.isBlockingAllowed();
	}

	/**
	 * Blocks the given {@link Task}.
	 * <p>
	 * Blocked tasks are not executed by the scheduler. Tasks can be {@link #blockTask(Task)
	 * blocked} and {@link #unblockTask(Task) unblocked} by an administrator in the task maintenance
	 * view.
	 * </p>
	 */
	public final void blockTask(Task task) {
		if (!isBlockingAllowed(task)) {
			throw new IllegalArgumentException("Blocking task '" + task.getName() + "' is not allowed.");
		}
		storeTaskBlocked(task, true);
		notifyUpdate(task);
		Logger.info("Task " + task.getName() + " is being blocked.", new StackTrace(), Scheduler.class);
	}

	/**
	 * Unblock the given {@link Task}.
	 * 
	 * @see #blockTask(Task)
	 */
	public final void unblockTask(Task task) {
		storeTaskBlocked(task, false);
		notifyUpdate(task);
		Logger.info("Task " + task.getName() + " is being unblocked.", new StackTrace(), Scheduler.class);
	}

	/**
	 * Whether the given {@link Task} is blocked.
	 * 
	 * @see #blockTask(Task)
	 */
	public final boolean isTaskBlocked(Task task) {
		return isBlockingAllowed(task) && fetchTaskBlocked(task);
	}

	/**
	 * Store in the database that the given {@link Task} is blocked/unblocked.
	 * 
	 * @param isBlocked
	 *        If <code>true</code>, the Task is blocked. Otherwise unblocked.
	 * 
	 * @see #blockTask(Task)
	 */
	protected void storeTaskBlocked(final Task task, final boolean isBlocked) {
		RetryResult<Void, List<Throwable>> result =
			Retry.<Void, Throwable> retry(COMMIT_RETRIES, () -> {
				try {
					_dbProperties.setProperty(
						DBProperties.GLOBAL_PROPERTY, getPropertyNameTaskBlock(task),
						Boolean.valueOf(isBlocked).toString());
					return RetryResult.createSuccess();
				} catch (Throwable ex) {
					return RetryResult.createFailure(ex);
				}
			});
		if (!result.isSuccess()) {
			String message = "Failed to store that task '" + task.getName() + "' is "
				+ (isBlocked ? "" : "un") + "blocked.";
			throw ExceptionUtil.createException(message, result.getReason());
		}
	}

	/**
	 * Fetch from the database whether the given {@link Task} is blocked.
	 * 
	 * @see #blockTask(Task)
	 */
	protected boolean fetchTaskBlocked(final Task task) {
		RetryResult<Boolean, List<Throwable>> result =
			Retry.<Boolean, Throwable> retry(COMMIT_RETRIES, () -> {
				try {
					String rawValue = _dbProperties.getProperty(getPropertyNameTaskBlock(task));
					Boolean resultValue;
					if (StringServices.isEmpty(rawValue)) {
						resultValue = Boolean.valueOf(task.isBlockedByDefault());
					} else {
						resultValue = Boolean.valueOf(rawValue);
					}
					return RetryResult.createSuccess(resultValue);
				} catch (Throwable ex) {
					return RetryResult.createFailure(ex);
				}
			});
		if (!result.isSuccess()) {
			String message = "Failed to fetch whether task '" + task.getName() + "' is blocked or not."
				+ " Pretending it is blocked, until the database is available again.";
			RuntimeException error = ExceptionUtil.createException(message, result.getReason());
			Logger.error(message, error, Scheduler.class);
			// As long as the database is unavailable, no blockable tasks are scheduled.
			// This should not be a problem, as there are only very few things that can be done
			// while the database is unavailable.
			// Throwing an exception here instead would cause big problems
			// because of the various callers of this method.
			// And returning "false" would cause tasks to be scheduled that were blocked.
			return true;
		}
		return result.getValue();
	}

	private String getPropertyNameTaskBlock(Task task) {
		return DB_PROPERTY_BLOCKED_TASKS + task.getName();
	}

	private void notifyUpdate(Task task) {
		getTaskUpdateQueue().add(buildNotification(task));
	}

	private TaskListenerNotification buildNotification(Task task) {
		TaskState state = task.getLog().getState();
		return new TaskListenerNotification(task, state, state, null);
	}

}
