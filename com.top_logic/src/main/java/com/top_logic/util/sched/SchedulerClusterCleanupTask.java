/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import static com.top_logic.util.sched.task.TaskCommon.*;

import java.util.List;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.NodeState;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ExponentialBackoff;
import com.top_logic.basic.util.retry.Retry;
import com.top_logic.basic.util.retry.RetryResult;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.impl.StateHandlingTask;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.log.TaskLogWrapper;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * {@link Task} that regularly checks whether a cluster node died while a task was still running on
 * it.
 * <p>
 * If an unfinished {@link Task} is found, {@link TaskLog#getState() it's state} is set to
 * {@link TaskState#INACTIVE} and all {@link TaskResult}s in {@link TaskResult#getResultType()
 * state} {@link ResultType#NOT_FINISHED} or {@link ResultType#UNKNOWN} are set to
 * {@link ResultType#ERROR}.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@InApp
public final class SchedulerClusterCleanupTask extends StateHandlingTask<StateHandlingTask.Config<?>> {

	/**
	 * Creates a new {@link SchedulerClusterCleanupTask}.
	 */
	public SchedulerClusterCleanupTask(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected boolean signalStopHook() {
		// This task will try to stop as soon as possible,
		// but there is no guarantee, how long this can take.
		return false;
	}

	@Override
	public void runHook() {
		RetryResult<Boolean, List<Throwable>> result = checkClusterStateRetrying();
		if (!result.isSuccess()) {
			String message = getClass().getSimpleName() + " failed after " + COMMIT_RETRIES + " attempts.";
			RuntimeException exception = ExceptionUtil.createException(message, result.getReason());
			getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), exception);
		}
	}

	private RetryResult<Boolean, List<Throwable>> checkClusterStateRetrying() {
		// This task is executed on every cluster node.
		// By waiting a random amount of time,
		// collisions of the transactions of these executions are minimized.
		ExponentialBackoff backoff = createExponentialBackoff();
		return Retry.retry(COMMIT_RETRIES, backoff, this::checkClusterState);
	}

	private RetryResult<Boolean, Throwable> checkClusterState() {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			for (Task task : Scheduler.getSchedulerInstance().getAllKnownTasks()) {
				if (getShouldStop()) {
					break;
				}
				check(task);
			}
			transaction.commit();
			return RetryResult.createSuccess(true);
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Boolean, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 */
	private void check(Task task) {
		for (TaskLogWrapper taskLog : TaskLogWrapper.getAllTaskLogs(task)) {
			if (getShouldStop()) {
				break;
			}
			if (isLogValid(taskLog)) {
				continue;
			}
			taskLog.touch();
			// By checking before locking, the number of locks (and therefore changes that will
			// be committed) is drastically reduced.
			// By checking again after locking, race conditions are prevented.
			if (isLogValid(taskLog)) {
				continue;
			}
			taskLog.forceUncheckedMarkTaskAsInactive(task, I18NConstants.TASK_ACTIVE_ON_SHUTDOWN);
		}
	}

	private boolean isLogValid(TaskLogWrapper taskLog) {
		Long clusterNodeId = taskLog.getClusterLockId();
		if (clusterNodeId == null) {
			return true;
		}
		if (isNodeDead(clusterNodeId)) {
			return false;
		}
		return true;
	}

	private boolean isNodeDead(long clusterNodeId) {
		NodeState nodeState = ClusterManager.getInstance().getNodeState(clusterNodeId);
		return nodeState == null;
	}

}
