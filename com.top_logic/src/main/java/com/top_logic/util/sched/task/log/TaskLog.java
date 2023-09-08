/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.log;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.GCQueue;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskListenerNotification;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Logging class for providing results of different runs of a {@link Task} (started by the
 * {@link Scheduler}).
 * <p>
 * <b>Implementations have to inform the {@link Scheduler#getTaskUpdateQueue()} when the
 * {@link #getState() state} changes.</b>
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface TaskLog {

	/**
	 * Filter for {@link ResultType#NOT_FINISHED}.
	 */
	final Filter<? super TaskResult> ACTIVE_FILTER = result -> result.getResultType() == ResultType.NOT_FINISHED;

	/**
	 * Filter for {@link ResultType#SUCCESS}.
	 * <p>
	 * The negation of the this filter is not the {@link #PROBLEM_FILTER}, as there are some
	 * {@link ResultType}s that don't indicate a problem but are not a {@link ResultType#SUCCESS}
	 * either.
	 * </p>
	 */
	final Filter<? super TaskResult> SUCCESS_FILTER = aTask -> (aTask.getResultType() == ResultType.SUCCESS);

	/**
	 * Filter for {@link TaskResult}s which indicate a problem.
	 * <p>
	 * The negation of the this filter is the {@link #NO_PROBLEM_FILTER}, not the
	 * {@link #SUCCESS_FILTER}, as there are some {@link ResultType}s that don't indicate a problem
	 * but are not a {@link ResultType#SUCCESS} either.
	 * </p>
	 */
	final Filter<? super TaskResult> PROBLEM_FILTER = taskResult -> {
		ResultType theResult = taskResult.getResultType();
		return (theResult == ResultType.FAILURE) || (theResult == ResultType.ERROR)
			|| (theResult == ResultType.CANCELED) || (theResult == ResultType.WARNING)
			|| (theResult == ResultType.UNKNOWN);
	};

	/**
	 * Negation of the {@link #PROBLEM_FILTER}.
	 */
	final Filter<? super TaskResult> NO_PROBLEM_FILTER = FilterFactory.not(PROBLEM_FILTER);

	/** The maximum number of stored successes per {@link Task}. */
	final int MAX_SUCCESSES = 100;

	/** The maximum number of stored failures per {@link Task}. */
	final int MAX_FAILURES = 5000;

	/**
	 * Getter for the {@link TaskState} of the {@link Task}.
	 * <p>
	 * <b>Implementations have to inform the {@link Scheduler#getTaskUpdateQueue()} when this
	 * state changes.</b>
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	TaskState getState();

	/**
	 * Returns the current {@link TaskResult}.
	 * <p>
	 * If the task is not running, this method returns the same as {@link #getLastResult()}. If the
	 * task is running, this method returns the {@link TaskResult} for the current run. <br/>
	 * </p>
	 * 
	 * @return <code>null</code>, if this {@link TaskLog} has no current and no last result.
	 */
	TaskResult getCurrentResult();

	/**
	 * Returns the {@link TaskResult} of the last finished run.
	 * <p>
	 * If the task is not running, this method returns the same as {@link #getCurrentResult()}. If
	 * the task is running, this method returns the {@link TaskResult} for the last finished run,
	 * not the current run.
	 * </p>
	 * 
	 * @return null, if this {@link TaskLog} has no last result.
	 */
	TaskResult getLastResult();

	/**
	 * Returns all {@link TaskResult}s.
	 * <p>
	 * Including the current result, if the {@link Task} is still {@link TaskState#RUNNING}.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	Collection<? extends TaskResult> getResults();

	/**
	 * Returns all {@link TaskResult}s with the given {@link ResultType}.
	 * 
	 * @return Never <code>null</code>.
	 */
	Collection<? extends TaskResult> getResults(ResultType resultType);

	/**
	 * Returns all {@link TaskResult}s partitioned by their {@link ResultType}.
	 * 
	 * @return Never <code>null</code>.
	 */
	Map<ResultType, ? extends Collection<? extends TaskResult>> getResultsByType();

	/**
	 * Return all none successful task results for the held task.
	 * <p>
	 * "None successful" means everything accepted by the {@link #PROBLEM_FILTER}.
	 * </p>
	 * 
	 * @return The task results, never <code>null</code>.
	 */
	Collection<? extends TaskResult> getFailures();

	/**
	 * Return all {@link Task}s that are matched by {@link #NO_PROBLEM_FILTER}.
	 * 
	 * @return The task results, never <code>null</code>.
	 */
	Collection<? extends TaskResult> getSuccess();

	/**
	 * Is called by the {@link Task} when it is started.
	 * <p>
	 * Sets an "is running" {@link TaskResult} as {@link #getCurrentResult() current result}.
	 * </p>
	 */
	void taskStarted();

	/**
	 * Convenience variant of {@link #taskEnded(ResultType, ResKey, Throwable)} where the
	 * {@link Throwable} is <code>null</code>.
	 * 
	 * <p>
	 * <b>Must not be called twice for one run.</b>
	 * </p>
	 */
	void taskEnded(ResultType type, ResKey message);

	/**
	 * Is called by the {@link Task} when it ends, no matter how it ends.
	 * <p>
	 * Creates a new {@link TaskResult}, sets it as the {@link #getCurrentResult() last result} and
	 * clears the {@link #getCurrentResult() current result}. <br/>
	 * <b>Must not be called twice for one run.</b>
	 * </p>
	 * 
	 * @param type
	 *        The result of the run, must not be <code>null</code>, {@link ResultType#NOT_FINISHED}
	 *        and {@link ResultType#UNKNOWN}.
	 * @param message
	 *        The I18N message key, must not be <code>null</code>.
	 * @param exception
	 *        The exception, may be <code>null</code>.
	 */
	void taskEnded(ResultType type, ResKey message, Throwable exception);

	/**
	 * Is called by the {@link Task} when it is told to cancel it's execution.
	 * <p>
	 * Has to be called even if the {@link Task} decides to ignore the cancel request.
	 * </p>
	 */
	void taskCanceling();

	/**
	 * Stores that the {@link Task} died without storing an end result.
	 * 
	 * @param messageKey
	 *        The I18N message key, must not be <code>null</code>.
	 */
	void taskDied(ResKey messageKey);

	/**
	 * Registers an event queue that should receive {@link TaskListenerNotification}s about task
	 * results.
	 * <p>
	 * <b>Only one event queue at a time is permitted.</b> The {@link Scheduler} is the only
	 * (direct) listener. All other listeners should register there:
	 * {@link Scheduler#getTaskUpdateQueue()} <br/>
	 * Passing null removes the registered queue. If no queue is registered when null is passed,
	 * nothing happens.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *         If the given queue is not null and another queue is already registered.
	 */
	void setEventQueue(GCQueue<TaskListenerNotification> taskUpdateQueue) throws IllegalStateException;

}
