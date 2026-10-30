/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.label.I18NClassNameProvider;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.ActiveState;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResultWrapper;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * TL-Script functions about the tasks of the {@link Scheduler}: what a view administering the
 * scheduler displays of a task, and whether a command applies to it.
 *
 * <p>
 * Every function accepts no task at all and answers with nothing, so that a channel computed from a
 * task that is not yet selected stays empty.
 * </p>
 *
 * <pre>
 * &lt;derived-channel name="running"
 *   expr="schedulerRunning($selection)"
 *   inputs="selection, tasks"/&gt;
 * </pre>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #nextRun(Task)} is called <code>schedulerNextRun</code>. The functions read the
 *           state of the tasks, which only an administrator may inspect from a script entered
 *           interactively.
 */
@ScriptPrefix(SchedulerFunctions.PREFIX)
@AdminOnly
public class SchedulerFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "scheduler";

	/**
	 * Content type of the log file of a task run: tasks write their log files as UTF-8 text.
	 */
	private static final String LOG_FILE_CONTENT_TYPE = "text/plain;charset=" + StandardCharsets.UTF_8.name();

	/**
	 * The given snapshot of the tasks, or one taken now.
	 *
	 * @param snapshot
	 *        The tasks as read at one point in time, e.g. the value a scheduler command wrote to a
	 *        channel.
	 * @return The given snapshot; a snapshot taken now without one.
	 */
	@Label("Snapshot of the scheduler tasks")
	@SideEffectFree
	public static SchedulerSnapshot snapshot(SchedulerSnapshot snapshot) {
		return snapshot == null ? SchedulerSnapshot.current() : snapshot;
	}

	/**
	 * The application service of the scheduler, e.g. to open its configuration in the services
	 * administration.
	 *
	 * @return The runtime module of the {@link Scheduler}.
	 */
	@Label("Service of the scheduler")
	@SideEffectFree
	public static BasicRuntimeModule<?> service() {
		return Scheduler.Module.INSTANCE;
	}

	/**
	 * The tasks below the given node of a tree of the scheduler tasks.
	 *
	 * @param node
	 *        A snapshot of the tasks, see {@link #snapshot(SchedulerSnapshot)}, or a task.
	 * @return The top-level tasks of a snapshot, the subtasks of a composite task, both sorted by
	 *         their labels; nothing for another task or without a node.
	 */
	@Label("Subtasks in the scheduler")
	@SideEffectFree
	public static List<Task> children(Object node) {
		if (node instanceof SchedulerSnapshot snapshot) {
			return snapshot.getTasks();
		}
		if (node instanceof CompositeTask composite) {
			return SchedulerSnapshot.sorted(composite.getChildren());
		}
		return List.of();
	}

	/**
	 * The label of the given task.
	 *
	 * @param task
	 *        The task.
	 * @return Its translated label, its name if the application has no translation;
	 *         <code>null</code> without a task.
	 */
	@Label("Label of a scheduler task")
	@SideEffectFree
	public static String label(Task task) {
		return task == null ? null : Scheduler.getTaskLabel(task);
	}

	/**
	 * The technical name of the given task, which identifies it in the scheduler.
	 *
	 * @param task
	 *        The task.
	 * @return The configured name, <code>null</code> without a task.
	 */
	@Label("Name of a scheduler task")
	@SideEffectFree
	public static String name(Task task) {
		return task == null ? null : task.getName();
	}

	/**
	 * Whether the given task is a top-level task of the scheduler, which is run by a schedule of
	 * its own - not a subtask, which runs with its parent.
	 *
	 * @param task
	 *        The task.
	 * @return Whether the task is a top-level task, <code>false</code> without a task.
	 *
	 * @see Scheduler#getKnownTopLevelTasks()
	 */
	@Label("Whether a scheduler task is a top-level task")
	@SideEffectFree
	public static boolean topLevel(Task task) {
		return task != null && Scheduler.getSchedulerInstance().getKnownTopLevelTasks().contains(task);
	}

	/**
	 * Whether the given task is active, and whether this is its configured state or an override of
	 * an administrator.
	 *
	 * @param task
	 *        The task.
	 * @return What the user reads for the state, see {@link ActiveState}; <code>null</code>
	 *         without a task.
	 */
	@Label("Active state of a scheduler task")
	@SideEffectFree
	public static String state(Task task) {
		return task == null ? null : Resources.getInstance().getString(ResKey.forEnum(ActiveState.of(task)));
	}

	/**
	 * Whether the given task is active, i.e. run by its schedule.
	 *
	 * @param task
	 *        The task.
	 * @return Whether the task is active, <code>false</code> without a task.
	 *
	 * @see Scheduler#isActive(Task)
	 */
	@Label("Whether a scheduler task is active")
	@SideEffectFree
	public static boolean active(Task task) {
		return task != null && Scheduler.getSchedulerInstance().isActive(task);
	}

	/**
	 * Whether an administrator has overridden the configured active state of the given task.
	 *
	 * @param task
	 *        The task.
	 * @return Whether a reset to the configured state would change the task, <code>false</code>
	 *         without a task.
	 *
	 * @see Scheduler#hasActiveOverride(Task)
	 */
	@Label("Whether the active state of a scheduler task is overridden")
	@SideEffectFree
	public static boolean activeOverride(Task task) {
		return task != null && Scheduler.getSchedulerInstance().hasActiveOverride(task);
	}

	/**
	 * The kind of schedule the given task is run by.
	 *
	 * @param task
	 *        The task.
	 * @return The label of the scheduling algorithm of the task, <code>null</code> for a task
	 *         without one (e.g. a subtask, which runs with its parent), and without a task.
	 */
	@Label("Schedule of a scheduler task")
	@SideEffectFree
	public static String schedule(Task task) {
		if (task == null) {
			return null;
		}
		SchedulingAlgorithm algorithm = task.getSchedulingAlgorithm();
		return algorithm == null ? null : I18NClassNameProvider.INSTANCE.getLabel(algorithm.getClass());
	}

	/**
	 * When the last finished run of the given task started.
	 *
	 * @param task
	 *        The task.
	 * @return The start of the last finished run, <code>null</code> if the task has not run, and
	 *         without a task.
	 */
	@Label("Last run of a scheduler task")
	@SideEffectFree
	public static Date lastRun(Task task) {
		TaskResult result = lastResultOf(task);
		return result == null ? null : result.getStartDate();
	}

	/**
	 * When the given task is run next.
	 *
	 * @param task
	 *        The task.
	 * @return The next planned run, <code>null</code> if none is planned, and without a task.
	 *
	 * @see Scheduler#getNextRun(Task)
	 */
	@Label("Next run of a scheduler task")
	@SideEffectFree
	public static Date nextRun(Task task) {
		return task == null ? null : Scheduler.getSchedulerInstance().getNextRun(task);
	}

	/**
	 * How the last finished run of the given task ended.
	 *
	 * @param task
	 *        The task.
	 * @return What the user reads for the result of the last finished run, <code>null</code> if
	 *         the task has not run, and without a task.
	 */
	@Label("Last result of a scheduler task")
	@SideEffectFree
	public static String lastResult(Task task) {
		TaskResult result = lastResultOf(task);
		return result == null ? null : Resources.getInstance().getString(result.getResultType().getMessageI18N());
	}

	private static TaskResult lastResultOf(Task task) {
		return task == null ? null : task.getLog().getLastResult();
	}

	/**
	 * Whether the given task is executed right now.
	 *
	 * @param task
	 *        The task.
	 * @return Whether the task is running and has not been told to stop, <code>false</code>
	 *         without a task.
	 *
	 * @see #execution(Task)
	 */
	@Label("Whether a scheduler task is running")
	@SideEffectFree
	public static boolean running(Task task) {
		return task != null && task.getLog().getState() == TaskState.RUNNING;
	}

	/**
	 * Whether the given task is executed right now, or stopping.
	 *
	 * @param task
	 *        The task.
	 * @return What the user reads for the execution state of the task, <code>null</code> without
	 *         a task.
	 *
	 * @see TaskState
	 */
	@Label("Execution state of a scheduler task")
	@SideEffectFree
	public static String execution(Task task) {
		return task == null ? null : Resources.getInstance().getString(task.getLog().getState().getMessageI18N());
	}

	/**
	 * The cluster node holding the cluster lock of the given task.
	 *
	 * @param task
	 *        The task.
	 * @return The name of the node followed by its id, <code>null</code> if no node holds the lock
	 *         and without a task.
	 *
	 * @see Scheduler#getClusterLockHolder(Task)
	 */
	@Label("Cluster lock holder of a scheduler task")
	@SideEffectFree
	public static String clusterLock(Task task) {
		return task == null ? null : Scheduler.getClusterLockHolder(task);
	}

	/**
	 * Whether the cluster lock of the given task can be released.
	 *
	 * @param task
	 *        The task.
	 * @return Whether a cluster node holds the lock of the task - not this node for a run in
	 *         progress here, whose lock is released when the run ends; <code>false</code> without
	 *         a task.
	 *
	 * @see Scheduler#canReleaseClusterLock(Task)
	 */
	@Label("Whether the cluster lock of a scheduler task can be released")
	@SideEffectFree
	public static boolean clusterLockReleasable(Task task) {
		return task != null && Scheduler.getSchedulerInstance().canReleaseClusterLock(task);
	}

	/**
	 * The qualified name of the class implementing the given task.
	 *
	 * @param task
	 *        The task.
	 * @return The class name, <code>null</code> without a task.
	 */
	@Label("Implementation of a scheduler task")
	@SideEffectFree
	public static String implementation(Task task) {
		return task == null ? null : task.getClass().getName();
	}

	/**
	 * The task of the given technical name.
	 *
	 * @param name
	 *        The name of the task, see {@link #name(Task)}.
	 * @return The task, <code>null</code> if the scheduler has no task of that name.
	 */
	@Label("Scheduler task by its name")
	@SideEffectFree
	public static Task byName(String name) {
		return name == null ? null : Scheduler.getSchedulerInstance().getTaskByName(name);
	}

	/**
	 * Whether the given task runs on every cluster node on its own, instead of once in the cluster
	 * under a cluster lock.
	 *
	 * @param task
	 *        The task.
	 * @return Whether the task is node local, <code>false</code> without a task.
	 *
	 * @see Task#isNodeLocal()
	 */
	@Label("Whether a scheduler task runs on every cluster node")
	@SideEffectFree
	public static boolean nodeLocal(Task task) {
		return task != null && task.isNodeLocal();
	}

	/**
	 * Whether the state and the results of the given task are stored in the database.
	 *
	 * @param task
	 *        The task.
	 * @return Whether the task is persistent, <code>false</code> without a task.
	 *
	 * @see Task#isPersistent()
	 */
	@Label("Whether a scheduler task is persistent")
	@SideEffectFree
	public static boolean persistent(Task task) {
		return task != null && task.isPersistent();
	}

	/**
	 * Whether the given task runs only while the application is in maintenance mode.
	 *
	 * @param task
	 *        The task.
	 * @return Whether the task needs the maintenance mode, <code>false</code> without a task.
	 *
	 * @see Task#needsMaintenanceMode()
	 */
	@Label("Whether a scheduler task needs the maintenance mode")
	@SideEffectFree
	public static boolean needsMaintenanceMode(Task task) {
		return task != null && task.needsMaintenanceMode();
	}

	/**
	 * The configuration the given task was created from, e.g. for displaying its schedules.
	 *
	 * @param task
	 *        The task.
	 * @return A copy of the configuration, <code>null</code> for a task that is not configured in
	 *         the scheduler, and without a task.
	 *
	 * @see Scheduler#getTaskConfiguration(Task)
	 */
	@Label("Configuration of a scheduler task")
	@SideEffectFree
	public static Task.Config<?> configuration(Task task) {
		if (task == null) {
			return null;
		}
		Task.Config<?> config = Scheduler.getSchedulerInstance().getTaskConfiguration(task);
		return config == null ? null : TypedConfiguration.copy(config);
	}

	/**
	 * The recorded runs of the given task.
	 *
	 * @param task
	 *        The task.
	 * @return The results of the runs of the task, the latest first; nothing without a task.
	 */
	@Label("Results of a scheduler task")
	@SideEffectFree
	public static List<TaskResult> results(Task task) {
		if (task == null || task.getLog() == null) {
			return List.of();
		}
		return task.getLog().getResults().stream()
			.filter(SchedulerFunctions::valid)
			.sorted(Comparator.comparing(TaskResult::getStartDate,
				Comparator.nullsLast(Comparator.<Date> reverseOrder())))
			.map(TaskResult.class::cast)
			.toList();
	}

	private static boolean valid(TaskResult result) {
		return !(result instanceof TaskResultWrapper wrapper) || wrapper.tValid();
	}

	/**
	 * How the given run of a task ended.
	 *
	 * @param result
	 *        The result of the run, see {@link #results(Task)}.
	 * @return What the user reads for the result, <code>null</code> without a result.
	 *
	 * @see #resultKind(TaskResult)
	 */
	@Label("Result of a scheduler task run")
	@SideEffectFree
	public static String resultType(TaskResult result) {
		return result == null ? null : Resources.getInstance().getString(result.getResultType().getMessageI18N());
	}

	/**
	 * The kind of the result of the given run of a task, e.g. for marking it by its severity.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The name of the result type - <code>NOT_FINISHED</code>, <code>SUCCESS</code>,
	 *         <code>CANCELED</code>, <code>WARNING</code>, <code>FAILURE</code>,
	 *         <code>ERROR</code> or <code>UNKNOWN</code>; <code>null</code> without a result.
	 *
	 * @see TaskResult.ResultType
	 */
	@Label("Kind of the result of a scheduler task run")
	@SideEffectFree
	public static String resultKind(TaskResult result) {
		return result == null ? null : result.getResultType().name();
	}

	/**
	 * When the given run of a task started.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The start of the run, <code>null</code> without a result.
	 */
	@Label("Start of a scheduler task run")
	@SideEffectFree
	public static Date resultStart(TaskResult result) {
		return result == null ? null : result.getStartDate();
	}

	/**
	 * When the given run of a task ended.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The end of the run, <code>null</code> while it runs, and without a result.
	 */
	@Label("End of a scheduler task run")
	@SideEffectFree
	public static Date resultEnd(TaskResult result) {
		return result == null ? null : result.getEndDate();
	}

	/**
	 * How long the given run of a task took.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The duration in a readable form, <code>null</code> while the run is not finished,
	 *         and without a result.
	 */
	@Label("Duration of a scheduler task run")
	@SideEffectFree
	public static String resultDuration(TaskResult result) {
		if (result == null) {
			return null;
		}
		Long duration = result.getDuration();
		return duration == null ? null : DebugHelper.toDuration(duration.longValue());
	}

	/**
	 * The cluster node the given run of a task was executed on.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The name of the cluster node, <code>null</code> without a result.
	 */
	@Label("Cluster node of a scheduler task run")
	@SideEffectFree
	public static String resultClusterNode(TaskResult result) {
		return result == null ? null : result.getClusterNode();
	}

	/**
	 * The message the given run of a task ended with.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The message in the language of the user, <code>null</code> without one.
	 */
	@Label("Message of a scheduler task run")
	@SideEffectFree
	public static String resultMessage(TaskResult result) {
		if (result == null || result.getMessage() == null) {
			return null;
		}
		return Resources.getInstance().getString(result.getMessage());
	}

	/**
	 * The warnings the given run of a task reported.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The warnings, one per line; <code>null</code> without warnings, and without a
	 *         result.
	 */
	@Label("Warnings of a scheduler task run")
	@SideEffectFree
	public static String resultWarnings(TaskResult result) {
		if (result == null || !result.hasWarnings()) {
			return null;
		}
		return String.join("\n", result.getWarnings());
	}

	/**
	 * The exception the given run of a task failed with.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The stack trace of the exception, <code>null</code> if the run did not fail with
	 *         an exception, and without a result.
	 */
	@Label("Exception of a scheduler task run")
	@SideEffectFree
	public static String resultException(TaskResult result) {
		return result == null ? null : result.getExceptionDump();
	}

	/**
	 * The log file the given run of a task wrote.
	 *
	 * @param result
	 *        The result of the run.
	 * @return The content of the log file, e.g. for a download; <code>null</code> if the run
	 *         wrote no log file, if the file no longer exists, and without a result.
	 */
	@Label("Log file of a scheduler task run")
	@SideEffectFree
	public static BinaryData resultLogFile(TaskResult result) {
		if (result == null) {
			return null;
		}
		File logFile = result.getLogFile();
		if (logFile == null || !logFile.isFile()) {
			return null;
		}
		return BinaryDataFactory.createBinaryData(logFile, LOG_FILE_CONTENT_TYPE, logFile.getName());
	}

}
