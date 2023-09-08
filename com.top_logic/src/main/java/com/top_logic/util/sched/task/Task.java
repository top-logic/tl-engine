/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.sched.Batch;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.schedule.DailySchedule;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * A task is a thread that will be executed periodically by the Scheduler.
 * <p>
 * Tasks have to inform the {@link Scheduler#getTaskUpdateQueue()} if their {@link TaskState}
 * changes. Most do this by using a {@link TaskLog} that takes care of it. <br/>
 * It is not allowed to run a {@link Task} manually. Always use
 * {@link Scheduler#getTaskByName(String)} and {@link Scheduler#startTask(Task)} instead. See:
 * #14112 for details.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface Task extends Batch {
	
	/** Typed configuration for {@link Task}. */
	public interface Config<I extends Task> extends PolymorphicConfiguration<I>, NamedConfigMandatory {

		/** Default value for {@link #isNeedingMaintenanceMode()}. */
		final boolean DEFAULT_NEEDS_MAINTENANCE_MODE = false;

		/** Default value for {@link #getMaintenanceModeDelay()}. */
		final int DEFAULT_MAINTENANCE_MODE_DELAY = 0;

		/** Default value for {@link #isMaintenanceModeSafe()}. */
		final boolean DEFAULT_MAINTENANCE_MODE_SAFE = false;

		/** Default value for {@link #isBlockingAllowed()}. */
		final boolean DEFAULT_BLOCKING_ALLOWED = true;

		/** Default value for {@link #isBlockingAllowed()}. */
		final boolean DEFAULT_BLOCKED_BY_DEFAULT_VALUE = false;
		
		/** The name of the property {@link #getSchedules()}. */
		String SCHEDULES = "schedules";

		/**
		 * Property name of {@link #isBlockedByDefault()}.
		 */
		String BLOCKED_BY_DEFAULT_PROPERTY = "blocked-by-default";

		/**
		 * The name of the task.
		 * 
		 * <p>
		 * Must be case-insensitive unique among {@link Scheduler#getAllKnownTasks() all tasks}.
		 * </p>
		 */
		@Override
		String getName();

		/**
		 * Whether the task should be considered in the schedule.
		 * 
		 * <p>
		 * A task that is not enabled is never scheduled independently of its
		 * {@link #getSchedules()}.
		 * </p>
		 */
		@BooleanDefault(true)
		boolean isEnabled();

		/**
		 * The {@link SchedulingAlgorithm}s to use to calculate the next schedule (=run time) of
		 * this {@link Task}.
		 * 
		 * <p>
		 * The task runs at the point in time of all schedules that next to the current time.
		 * </p>
		 */
		@Name(SCHEDULES)
		@Options(fun = AllInAppImplementations.class)
		@ImplementationClassDefault(DailySchedule.class)
		List<PolymorphicConfiguration<? extends SchedulingAlgorithm>> getSchedules();

		/**
		 * Whether this {@link Task} requires entering maintenance mode before startup.
		 * 
		 * <p>
		 * If maintenance mode is not active, it is automatically activated before starting the
		 * task. After the task has finished, maintenance mode is deactivated again.
		 * </p>
		 * 
		 * <p>
		 * If the task {@link #isNeedingMaintenanceMode() requires maintenance mode}, and
		 * maintenance mode is already active, the task is not run until the time the maintenance
		 * mode is deactivated again. The scheduler will then activate maintenance mode for the task
		 * before starting and deactivates it after the task is done.
		 * </p>
		 * 
		 * <p>
		 * In other words, a task requiring maintenance mode must activate maintenance mode for its
		 * run exclusively. Maintenance mode is for operations during which nothing else is allowed
		 * to happen on the system. If the maintenance mode is already active when a task is about
		 * to start, it means the administrator is doing something that should not be interrupted
		 * with anything else.
		 * </p>
		 * 
		 * <p>
		 * On the other hand, if a task is manually started, it is started even if maintenance mode
		 * is already active. In this case, maintenance mode is not automatically deactivated after
		 * the task finishes. In other words, if maintenance mode should stay active after finishing
		 * the manually started task, maintenance mode has to be activated manually before starting
		 * the task.
		 * </p>
		 * 
		 * @see #isMaintenanceModeSafe()
		 */
		@BooleanDefault(DEFAULT_NEEDS_MAINTENANCE_MODE)
		@Label("Maintenance mode required")
		boolean isNeedingMaintenanceMode();

		/**
		 * Delay (in milliseconds) from the request until the maintenance mode is set.
		 * <p>
		 * Once the maintenance mode is set, all users are thrown out. The delay is useful to give
		 * them some time to save their work. <br/>
		 * The {@link Task} is started once the maintenance mode is set.
		 * </p>
		 */
		@LongDefault(DEFAULT_MAINTENANCE_MODE_DELAY)
		@Format(MillisFormat.class)
		long getMaintenanceModeDelay();

		/**
		 * Whether this {@link Task} can run even if maintenance mode is active.
		 * 
		 * <p>
		 * In contrast to {@link #isNeedingMaintenanceMode()}, a task that is maintenance-mode safe
		 * can run in with or without maintenance mode. If {@link #isNeedingMaintenanceMode()} is
		 * active, maintenance mode is actively entered before running the task.
		 * </p>
		 * 
		 * @see #isNeedingMaintenanceMode()
		 */
		@BooleanDefault(DEFAULT_MAINTENANCE_MODE_SAFE)
		boolean isMaintenanceModeSafe();

		/**
		 * Whether this {@link Task} can be {@link Scheduler#blockTask(Task) blocked}.
		 */
		@BooleanDefault(DEFAULT_BLOCKING_ALLOWED)
		boolean isBlockingAllowed();

		/**
		 * Whether this {@link Task} is {@link Scheduler#isTaskBlocked(Task) blocked} by default
		 * (but can be enabled by the administrator).
		 */
		@Name(BLOCKED_BY_DEFAULT_PROPERTY)
		@BooleanDefault(DEFAULT_BLOCKED_BY_DEFAULT_VALUE)
		boolean isBlockedByDefault();
		
	}

	/**
	 * @see Config#getSchedules()
	 */
	public SchedulingAlgorithm getSchedulingAlgorithm();

	/**
	 * Calculate and return the time when Task should be executed next.
	 * 
	 * @param notBefore
	 *        The current time meaning that no schedules should be returned that are in the past
	 *        (before the current time).
	 * 
	 * @return The time when the task should run the next time, or
	 *         {@link SchedulingAlgorithm#NO_SCHEDULE} to indicate that no further Scheduling is
	 *         needed.
	 */
	public long calcNextShed(long notBefore);

	/**
	 * Accessor to nextShed.
	 * <p>
	 * This must be the same value as last returned by {@link #calcNextShed(long)} anything else
	 * will confuse the scheduler. It is {@link SchedulingAlgorithm#NO_SCHEDULE} while the Task is
	 * running and will be recalculated as triggered by the {@link Scheduler}. <br/>
	 * If the value is {@link SchedulingAlgorithm#NO_SCHEDULE} when the {@link Task} is not running,
	 * the {@link Scheduler} will stop scheduling this {@link Task} and log an error.
	 * </p>
	 */
    public long getNextShed();

	/**
	 * The next run time of this {@link Task}.
	 * <p>
	 * {@link Maybe#none()} means: It should not be run.
	 * </p>
	 * 
	 * @return Never <code>null</code>. Never contains <code>null</code>. Always returns a new
	 *         {@link Calendar} object, if not {@link Maybe#none()}.
	 */
	public Maybe<Calendar> getNextSchedule();

	/**
	 * Marks the {@link Task} as being run the last time on the given {@link Date}.
	 * <p>
	 * Only increases the time of the last run, never decreases it. <br/>
	 * The time when the task is run, has to be executed based on that date. <br/>
	 * The value {@link SchedulingAlgorithm#NO_SCHEDULE} means: Has not run, yet.
	 * </p>
	 * 
	 * @param start
	 *        The start time of the current task run.
	 * @return Whether the current schedule must be updated.
	 */
	public boolean markAsRun(long start);

	/**
	 * The last time, this {@link Task} was run.
	 * 
	 * <p>
	 * {@link SchedulingAlgorithm#NO_SCHEDULE} means that the {@link Task} has not run, yet.
	 * </p>
	 */
	public long getLastSchedule();

	/**
	 * Force the execution at the given time.
	 * <p>
	 * <b>Must only be called by the scheduler. Otherwise, it may have no effect, as the Scheduler
	 * needs to know about changes in the schedule of a task.</b> <br/>
	 * If the task is run before the given time anyway, this method has no effect. <br/>
	 * </p>
	 */
	public void forceRun(long start);

	/**
	 * Is the next run a {@link #forceRun(long) forced run}?
	 */
	public boolean isForcedRun();

	/**
	 * Defines, if this task need to run in every cluster node by itself.
	 * 
	 * In some cases it may be needful, if a task runs only on one cluster node (e.g. refresh of
	 * external user data), these tasks need to return <code>false</code> here.
	 * 
	 * <p>
	 * If this method returns <code>false</code>, the {@link Task} has to call
	 * {@link TaskLog#taskStarted()} and
	 * {@link TaskLog#taskEnded(TaskResult.ResultType, ResKey, Throwable)} only on one of the nodes.
	 * The node is allowed to change from one run to another run, but has to be the same during one
	 * run.
	 * </p>
	 * 
	 * @return <code>true</code> when task must run in every cluster node.
	 */
	public boolean isNodeLocal();

	/**
	 * Flag, if this task must be executed in a maintenance mode.
	 * 
	 * See {@link Config#isNeedingMaintenanceMode()} and {@link Config#isMaintenanceModeSafe()} for
	 * the exact semantics.
	 * 
	 * @return <code>true</code> when the task needs the maintenance mode.
	 */
	public boolean needsMaintenanceMode();

	/** See: {@link Config#getMaintenanceModeDelay()} */
	public long getMaintenanceModeDelay();

	/**
	 * Flag, if this task is safe to be run during maintenance mode.
	 * 
	 * See {@link Config#isMaintenanceModeSafe()} and {@link Config#isNeedingMaintenanceMode()} for
	 * the exact semantics.
	 */
	public boolean isMaintenanceModeSafe();

	/**
	 * <code>false</code> to avoid running on system startup.
	 */
	public boolean isRunOnStartup();

	/**
	 * Whether {@link TaskResult results} of this {@link Task} are persisted.
	 * 
	 * <p>
	 * If <code>true</code>, the {@link Task} implementation has to ensure that:
	 * <ul>
	 * <li>The {@link Task} runs with a {@link ThreadContext}, for committing the status and
	 * results.</li>
	 * <li>The host names of all cluster nodes must be unique (no application nodes must share the
	 * same machine).</li>
	 * </ul>
	 * </p>
	 */
	public boolean isPersistent();

	/**
	 * Getter for the {@link TaskLog} of this {@link Task}.
	 * 
	 * @return Never <code>null</code>
	 */
	public TaskLog getLog();

	/**
	 * Initialize the {@link Task} so its ready to be scheduled by the given {@link Scheduler}.
	 * <p>
	 * A {@link Task} cannot be attached to more than one {@link Scheduler}. For a {@link Task} to
	 * be run, it has to be attached to a {@link Scheduler}. Only that {@link Scheduler} is allowed
	 * to run the {@link Task}.
	 * </p>
	 * <p>
	 * Is guaranteed to be called within
	 * {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.util.ComputationEx2)}.
	 * </p>
	 * <p>
	 * Is not allowed to be called twice without a {@link #detachFromScheduler()} in between.
	 * </p>
	 * 
	 * @param scheduler
	 *        Has to be {@link BasicRuntimeModule#isActive()}.
	 * @see #detachFromScheduler()
	 */
	@FrameworkInternal
	void attachTo(Scheduler scheduler);

	/**
	 * Removes all connections from the {@link Task} to its {@link Scheduler}.
	 * <p>
	 * Is not allowed to be called twice without a {@link #attachTo(Scheduler)} in between.
	 * </p>
	 * 
	 * @see #attachTo(Scheduler)
	 */
	@FrameworkInternal
	void detachFromScheduler();

	/**
	 * Logs a gui warning for the current {@link TaskResult}.
	 * 
	 * @see TaskResult#getWarnings()
	 */
	public void logGuiWarning(String message);

	/**
	 * In addition to what's already described in {@link Batch#signalStop()}, this method has to
	 * change the {@link TaskState} to {@link TaskState#CANCELING}.
	 */
	@Override
	public boolean signalStop();

	/**
	 * In addition to what's already described in {@link Batch#signalShutdown()}, this method has to
	 * change the {@link TaskState} to {@link TaskState#CANCELING}.
	 */
	@Override
	public void signalShutdown();

	/**
	 * @see Config#isBlockingAllowed()
	 */
	boolean isBlockingAllowed();

	/**
	 * @see Config#isBlockedByDefault()
	 */
	boolean isBlockedByDefault();

}
