/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.sched.task.Task;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix MEMORY_OBSERVER = legacyPrefix("admin.sys.memoryObserver.");

	/** The task did not write an end result. */
	public static ResKey TASK_DONE_WITHOUT_RESULT;

	/** The task was still active when the system or cluster node was shut down. */
	public static ResKey TASK_ACTIVE_ON_SHUTDOWN;

	public static ResKey TASK_MARKED_AS_DONE_BY_SCHEDULER;

	/** A {@link Task} dies with an unexpected error. */
	public static ResKey UNEXPECTED_ERROR;

	public static ResKey NOTHING_TO_CLEANUP;

	public static ResKey1 CLEANUP_SUCCESS__COUNT;

	@CustomKey("admin.sys.memoryObserver.chart.labelX")
	public static ResKey MEMORY_CHART_AXIS_TIME;

	@CustomKey("admin.sys.memoryObserver.chart.labelY")
	public static ResKey MEMORY_CHART_AXIS_VALUE;

	@CustomKey("admin.sys.memoryObserver.chart.title")
	public static ResKey MEMORY_CHART_TITLE;

	/**
	 * @en Cleaned task log: {0}
	 */
	public static ResKey1 CLEANED_TASK_LOG__TASK;

	/**
	 * @en Cleaned task log of global task: {0}
	 */
	public static ResKey1 CLEANED_GLOBAL_TASK_LOG__TASK;

	/**
	 * @en Requested cluster lock for task: {0}
	 */
	public static ResKey1 REQUESTED_CLUSTER_LOCK_FOR__TASK;

	/**
	 * @en Created task: {0}
	 */
	public static ResKey1 CREATED_TASK__TASK;

	/**
	 * @en Started task: {0}
	 */
	public static ResKey1 STARTED_TASK__TASK;

	/**
	 * @en Compressed task result list: {0}
	 */
	public static ResKey1 COMPRESSED_TASK_RESULT_LIST__TASK;

	/**
	 * @en Task finished: {0}
	 */
	public static ResKey1 TASK_FINISHED__TASK;

	/**
	 * @en Task canceled: {0}
	 */
	public static ResKey1 TASK_CANCELED__TASK;

	/**
	 * @en Cleared cluster lock for task: {0}
	 */
	public static ResKey1 CLEARED_CLUSTER_LOCK__TASK;

	/**
	 * @en Cleaned up dead cluster-global tasks.
	 */
	public static ResKey CLUSER_TASK_CLEANUP;

	/**
	 * @en Completed task: {0}
	 */
	public static ResKey1 COMPLETED_TASK__TASK;

	/**
	 * Prefix of the keys of the labels of the tasks: the prefix followed by the name of the task.
	 */
	public static ResPrefix TASK_NAMES = legacyPrefix("tl.task.");

	/**
	 * @en The task "{0}" has no cluster lock, since it runs on every cluster node on its own.
	 */
	public static ResKey1 ERROR_CLUSTER_LOCK_TASK_NODE_LOCAL__TASK;

	/**
	 * @en The task "{0}" has no cluster lock, since its state is not stored in the database.
	 */
	public static ResKey1 ERROR_CLUSTER_LOCK_TASK_NOT_PERSISTENT__TASK;

	/**
	 * @en The cluster lock of the task "{0}" is not set.
	 */
	public static ResKey1 ERROR_CLUSTER_LOCK_NOT_SET__TASK;

	/**
	 * @en The cluster lock of the task "{0}" is held by this cluster node for a run in progress. It
	 *     is released when the run ends; stop the task to end the run early.
	 */
	public static ResKey1 ERROR_CLUSTER_LOCK_TASK_RUNNING_HERE__TASK;

	// Commit message of Scheduler#releaseClusterLock(Task): stored with each commit, so the key
	// must not change.
	/**
	 * @en Released cluster lock for task: {0}
	 */
	@CustomKey("class.com.top_logic.util.sched.layout.I18NConstants.RELEASED_CLUSTER_LOCK__TASK")
	public static ResKey1 RELEASED_CLUSTER_LOCK__TASK;

	// Result message of a task ended by Scheduler#releaseClusterLock(Task): stored with each task
	// result, so the key must not change.
	/**
	 * @en The task has been marked as ended when its cluster lock was manually released. Task:
	 *     "{0}"
	 */
	@CustomKey("class.com.top_logic.util.sched.layout.I18NConstants.MANUAL_LOCK_RELEASE_TASK_END_BY_MANUAL_LOCK_RELEASE__TASK")
	public static ResKey1 TASK_ENDED_BY_CLUSTER_LOCK_RELEASE__TASK;

	static {
		initConstants(I18NConstants.class);
	}
}
