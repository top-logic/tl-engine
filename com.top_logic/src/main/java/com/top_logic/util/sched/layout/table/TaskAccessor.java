/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.ActiveState;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.layout.table.results.TaskResultAccessor;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.log.TaskLogWrapper;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * Accessor for the {@link Task} object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TaskAccessor extends ReadOnlyAccessor<Task> {

	/** Accessing instance to this class. */
    public static final TaskAccessor INSTANCE = new TaskAccessor();

    /** The name of the task. */
    public static final String NAME = "name";

	/**
	 * Column containing either {@link TaskLog#getState()} for active tasks, or
	 * {@link TaskResult#getResultType()} for inactive completed ones.
	 */
	public static final String COMBINED_STATE = "combinedState";

	/**
	 * Column containing the {@link ActiveState} of a {@link Task}.
	 * 
	 * @see Scheduler#isActive(Task)
	 */
	public static final String ACTIVE = "active";


    /** The name of the implementing class. */
    public static final String CLASS_NAME = "className";

    /** Date of last scheduled. */
    public static final String LAST_SCHEDULE = "lastSched";

    /** Date of next scheduled. */
    public static final String NEXT_SCHEDULE = "nextSched";

	/**
	 * Whether the {@link TaskResult}s of the given {@link Task} are being persisted.
	 * 
	 * @see Task#isPersistent()
	 */
	public static final String PERSISTENT = "persistent";

	/**
	 * Whether the given {@link Task} is being executed on every cluster node. (or just one)
	 * 
	 * @see Task#isNodeLocal()
	 */
	public static final String PER_NODE = "perNode";

	/**
	 * The cluster lock value for the given {@link Task}.
	 * 
	 * @see TaskLogWrapper#getClusterLockName()
	 * @see TaskLogWrapper#getClusterLockId()
	 */
	public static final String CLUSTER_LOCK = "clusterLock";

	/**
	 * @see Task#needsMaintenanceMode()
	 */
	public static final String NEEDS_MAINTENANCE_MODE = "needsMaintenanceMode";

    @Override
	public Object getValue(Task aTask, String aProperty) {
        if (NAME.equals(aProperty)) {
			return Scheduler.getTaskLabel(aTask);
        }
		else if (COMBINED_STATE.equals(aProperty)) {
			TaskState state = aTask.getLog().getState();
			if (state != TaskState.INACTIVE) {
				return state;
			}
			TaskResult currentResult = aTask.getLog().getCurrentResult();
			if (currentResult == null) {
				return TaskState.INACTIVE;
			}
			return currentResult.getResultType();
		}
		else if (ACTIVE.equals(aProperty)) {
			return ActiveState.of(aTask);
		}
        else if (CLASS_NAME.equals(aProperty)) {
            return aTask.getClass().getName();
        }
        else if (NEXT_SCHEDULE.equals(aProperty)) {
			return Scheduler.getSchedulerInstance().getNextRun(aTask);
        }
        else if (LAST_SCHEDULE.equals(aProperty)) {
			TaskResult lastResult = aTask.getLog().getLastResult();
			return lastResult == null ? null : lastResult.getStartDate();
        }
		else if (PERSISTENT.equals(aProperty)) {
			return Boolean.valueOf(aTask.isPersistent());
		}
		else if (PER_NODE.equals(aProperty)) {
			return Boolean.valueOf(aTask.isNodeLocal());
		}
		else if (CLUSTER_LOCK.equals(aProperty)) {
			return getClusterLockValue(aTask);
		}
		else if (NEEDS_MAINTENANCE_MODE.equals(aProperty)) {
			return Boolean.valueOf(aTask.needsMaintenanceMode());
		}
        else {
			TaskResult currentResult = aTask.getLog().getCurrentResult();
			return TaskResultAccessor.INSTANCE.getValue(currentResult, aProperty);
        }
    }

	/**
	 * The value of the cluster lock as {@link String}.
	 * 
	 * @return null, if the {@link Task} cannot have a lock or if the lock is not held by anyone.
	 */
	protected String getClusterLockValue(Task task) {
		if (task.isNodeLocal() || !task.isPersistent()) {
			return null;
		}
		String holder = Scheduler.getClusterLockHolder(task);
		if (holder != null) {
			return holder;
		}
		if (task.getLog().getState() != TaskState.INACTIVE) {
			return Resources.getInstance().getString(I18NConstants.CLUSTER_LOCK_FREE_BUT_STATE_NOT_INACTIVE);
		}
		return null;
	}
}
