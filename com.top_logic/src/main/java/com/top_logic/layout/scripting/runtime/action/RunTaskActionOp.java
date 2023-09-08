/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.Calendar;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.layout.scripting.action.RunTaskAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * {@link AbstractApplicationActionOp} for a {@link RunTaskAction}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RunTaskActionOp extends AbstractApplicationActionOp<RunTaskAction> {

	private static final int SLEEP_TIME = 100;

	private final Scheduler _scheduler;

	private Filter<? super TaskResult> _filter;

	/**
	 * Creates a new {@link RunTaskActionOp}.
	 */
	public RunTaskActionOp(InstantiationContext context, RunTaskAction config) {
		super(context, config);
		_scheduler = Scheduler.getSchedulerInstance();

		// Note: Java 11 requires a variable for the reduction to accept the types.
		List<Filter<? super TaskResult>> filterInstances = TypedConfiguration.getInstanceList(context, config.getResultFilters());

		_filter = FilterFactory.and(filterInstances);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		String taskName = getConfig().getTaskName();
		Task task = _scheduler.getTaskByName(taskName);
		if (task == null) {
			StringBuilder noTask = new StringBuilder();
			noTask.append("No Task with name '");
			noTask.append(getConfig().getTaskName());
			noTask.append("' available.");
			throw ApplicationAssertions.fail(getConfig(), noTask.toString());
		}
		
		Calendar cal = CalendarUtil.createCalendar();
		long abortTime = cal.getTimeInMillis() + getConfig().getMaxSleep();

		TaskResult result = null;
		do {
			long now = System.currentTimeMillis();
			_scheduler.startTask(task);
			long minStartTime;
			if (result == null) {
				minStartTime = now;
			} else {
				minStartTime = result.getStartDate().getTime() + 1;
			}
			result = getResult(task, minStartTime, abortTime);
			ResultType resultType = getResultType(result, abortTime);
			if (resultType != ResultType.SUCCESS) {
				StringBuilder notSuccesful = new StringBuilder();
				notSuccesful.append("Task '");
				notSuccesful.append(getConfig().getTaskName());
				notSuccesful.append("' not finished successful: ");
				notSuccesful.append(resultType);
				notSuccesful.append(", ");
				notSuccesful.append(Resources.getInstance().getString(result.getMessage()));
				throw ApplicationAssertions.fail(getConfig(), notSuccesful.toString());
			}
			internalSleep(abortTime);
		} while (!_filter.accept(result));
		// Task has been finished successful
		return argument;
	}

	private ResultType getResultType(TaskResult result, long abortTime) throws InterruptedException,
			MergeConflictException {
		while (true) {
			ResultType resultType = result.getResultType();
			if (resultType == ResultType.NOT_FINISHED) {
				// task has been started but is not finished yet
				internalSleep(abortTime);
				continue;
			}
			return resultType;
		}
	}

	private TaskResult getResult(Task task, long minStartTime, long abortTime) throws InterruptedException,
			MergeConflictException {
		while (true) {
			TaskResult result = task.getLog().getLastResult();
			if (result != null && result.getStartDate().getTime() >= minStartTime) {
				// Task has been started.
				return result;
			}
			internalSleep(abortTime);
		}
	}

	private void internalSleep(long abortTime) throws InterruptedException, MergeConflictException {
		if (System.currentTimeMillis() > abortTime) {
			StringBuilder notFinished = new StringBuilder();
			notFinished.append("Task '");
			notFinished.append(getConfig().getTaskName());
			notFinished.append("' not finished after ");
			notFinished.append(getConfig().getMaxSleep());
			notFinished.append("ms.");
			throw ApplicationAssertions.fail(getConfig(), notFinished.toString());
		}
		Thread.sleep(SLEEP_TIME);
		HistoryUtils.updateSessionRevision();
	}

}

