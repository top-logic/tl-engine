/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;

import java.util.Date;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.layout.block.BlockState;
import com.top_logic.util.sched.layout.table.results.TaskResultAccessor;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.composite.CompositeTask;
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

	public static final String IS_BLOCKED = "isBlocked";

	public static final String IS_BLOCKING_ALLOWED = "isBlockingAllowed";

    /** The name of the implementing class. */
    public static final String CLASS_NAME = "className";

    /** Date of last scheduled. */
    public static final String LAST_SCHEDULE = "lastSched";

    /** Date of next scheduled. */
    public static final String NEXT_SCHEDULE = "nextSched";

	/** <code>true</code> when {@link Task} {@link Scheduler#isEnabled(Task) is enabled}. */
	public static final String IS_ENABLED = "isEnabled";

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
	 * Whether the given {@link Task} is being executed on startup.
	 * 
	 * @see Task#isRunOnStartup()
	 */
	public static final String RUN_ON_STARTUP = "runOnStartup";

	/**
	 * @see Task#needsMaintenanceMode()
	 */
	public static final String NEEDS_MAINTENANCE_MODE = "needsMaintenanceMode";

    @Override
	public Object getValue(Task aTask, String aProperty) {
        if (NAME.equals(aProperty)) {
            String theName = aTask.getName();

			return Resources.getInstance().getString(I18NConstants.TASK_NAMES.key(theName), theName);
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
		else if (IS_BLOCKED.equals(aProperty)) {
			return getBlockState(aTask);
		}
		else if (IS_BLOCKING_ALLOWED.equals(aProperty)) {
			return Boolean.valueOf(Scheduler.getSchedulerInstance().isBlockingAllowed(aTask));
		}
        else if (CLASS_NAME.equals(aProperty)) {
            return aTask.getClass().getName();
        }
        else if (NEXT_SCHEDULE.equals(aProperty)) {
			if (!Scheduler.getSchedulerInstance().isEnabled(aTask)) {
				return null;
			}
			return this.getDate(aTask.getNextShed());
        }
        else if (LAST_SCHEDULE.equals(aProperty)) {
			TaskResult lastResult = aTask.getLog().getLastResult();
			return lastResult == null ? null : lastResult.getStartDate();
        }
		else if (IS_ENABLED.equals(aProperty)) {
            return Boolean.valueOf(Scheduler.getSchedulerInstance().isEnabled(aTask));
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
		else if (RUN_ON_STARTUP.equals(aProperty)) {
			return Boolean.valueOf(aTask.isRunOnStartup());
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
		TaskLogWrapper taskLogWrapper = (TaskLogWrapper) task.getLog();
		if (taskLogWrapper.isClusterLockSet()) {
			String lockName = taskLogWrapper.getClusterLockName();
			Long lockId = taskLogWrapper.getClusterLockId();
			return lockName + " (" + lockId + ")";
		}
		if (taskLogWrapper.getState() != TaskState.INACTIVE) {
			return Resources.getInstance().getString(I18NConstants.CLUSTER_LOCK_FREE_BUT_STATE_NOT_INACTIVE);
		}
		return null;
	}

	private BlockState getBlockState(Task task) {
		if (Scheduler.getSchedulerInstance().isTaskBlocked(task)) {
			return BlockState.BLOCKED;
		}
		if (!(task instanceof CompositeTask)) {
			return BlockState.NOT_BLOCKED;
		}
		if (isChildBlocked((CompositeTask) task)) {
			return BlockState.CHILD_BLOCKED;
		}
		return BlockState.NOT_BLOCKED;
	}

	private boolean isChildBlocked(CompositeTask task) {
		for (Task child : task.getChildren()) {
			if (Scheduler.getSchedulerInstance().isTaskBlocked(child)) {
				return true;
			}
			if ((child instanceof CompositeTask) && isChildBlocked((CompositeTask) child)) {
				return true;
			}
		}
		return false;
	}

    /** 
     * Return a date for the given time.
     * 
     * @param    aTime    The time as long.
     * @return   The requested date or <code>null</code> when give time is 0.
     */
    protected Date getDate(long aTime) {
		return (aTime > 0) ? new Date(aTime) : null;
	}
}

