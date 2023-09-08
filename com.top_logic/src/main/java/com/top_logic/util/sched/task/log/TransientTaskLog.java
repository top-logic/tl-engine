/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.GCQueue;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskListenerNotification;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.result.TaskResultCommon;
import com.top_logic.util.sched.task.result.TransientTaskResult;

/**
 * A transient version of the task log.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public final class TransientTaskLog implements TaskLog {

	/** The transient list of failed {@link TaskResult}s. */
	private Collection<TaskResult> problems;

	/** The transient list of {@link TaskResult}s that are not failed. */
	private Collection<TaskResult> noProblems;

	private final Task _task;

	/**
	 * Must only be set via {@link #setState(TaskState)} to ensure the
	 * {@link Scheduler#getTaskUpdateQueue()} is informed.
	 */
	private TaskState _state;

	private TaskResult _currentResult;

	private TaskResult _lastResult;

	private GCQueue<TaskListenerNotification> _events;

	/**
	 * Creates a {@link TransientTaskLog}.
	 * 
	 * @param task
	 *        The {@link Task} to create the log for, must not be <code>null</code>.
	 */
	public TransientTaskLog(Task task) {
		_task = task;
		_state = TaskState.INACTIVE;
		this.problems = new CircularFifoQueue<>(MAX_FAILURES);
		this.noProblems = new CircularFifoQueue<>(MAX_SUCCESSES);
	}

	@Override
	public synchronized void setEventQueue(GCQueue<TaskListenerNotification> events) throws IllegalStateException {
		if (_events != null && events != null) {
			throw new IllegalStateException("An event queue is already registered.");
		}
		_events = events;
	}

	/**
	 * This method returns the task.
	 * 
	 * @return Returns the task, never <code>null</code>.
	 */
	public synchronized Task getTask() {
		return _task;
	}

	@Override
	public synchronized TaskState getState() {
		return _state;
	}

	private void setState(TaskState newState) {
		TaskState oldState = _state;
		_state = newState;

		notifyListener(newState, oldState);
	}

	private synchronized void notifyListener(TaskState newState, TaskState oldState) {
		if (_events != null) {
			TaskListenerNotification notification =
				new TaskListenerNotification(getTask(), newState, oldState, getCurrentResult());
			_events.add(notification);
		}
	}

	@Override
	public synchronized TaskResult getCurrentResult() {
		if (_currentResult == null) {
			return _lastResult;
		}
		return _currentResult;
	}

	@Override
	public synchronized TaskResult getLastResult() {
		return _lastResult;
	}

	/**
	 * Return all held task results for the held task.
	 * 
	 * @return A complete list of the held task results, never <code>null</code>.
	 * @see #getFailures()
	 * @see #getSuccess()
	 */
	@Override
	public synchronized Collection<? extends TaskResult> getResults() {
		Collection<? extends TaskResult> noProblemResults = getNoProblems();
		ArrayList<TaskResult> results = new ArrayList<>(noProblemResults.size() + problems.size());
		results.addAll(noProblemResults);
		results.addAll(problems);
		return results;
	}

	@Override
	public synchronized Collection<? extends TaskResult> getResults(ResultType resultType) {
		switch (resultType) {
			case ERROR:
			case FAILURE:
			case CANCELED: {
				return TaskResultCommon.<TaskResult> findAll(problems, resultType);
			}
			default: {
				return TaskResultCommon.<TaskResult> findAll(getNoProblems(), resultType);
			}
		}
	}

	@Override
	public synchronized Map<ResultType, ? extends Collection<? extends TaskResult>> getResultsByType() {
		return TaskResultCommon.partitionByResultType(getResults());
	}

	@Override
	public synchronized Collection<? extends TaskResult> getFailures() {
		return new ArrayList<>(this.problems);
	}

	@Override
	public synchronized Collection<? extends TaskResult> getSuccess() {
		return new ArrayList<>(getNoProblems());
	}

	private Collection<TaskResult> getNoProblems() {
		ArrayList<TaskResult> result = new ArrayList<>(noProblems);
		if (_currentResult != null) {
			result.add(_currentResult);
		}
		return result;
	}

	@Override
	public synchronized void taskStarted() {
		TaskLogCommon.logTaskStarted(_task.getName());
		if (_currentResult != null) {
			Logger.error("The task '" + _task.getName() + "' started, but it appears to be still running.",
				TransientTaskLog.class);
		}
		_currentResult =
			new TransientTaskResult(
				_task,
				ResultType.NOT_FINISHED,
				ResultType.NOT_FINISHED.getMessageI18N(),
				new Date(),
				null);
		setState(TaskState.RUNNING);
	}

	@Override
	public synchronized void taskEnded(ResultType type, ResKey message) {
		taskEnded(type, message, null);
	}

	@Override
	public synchronized void taskEnded(ResultType type, ResKey message, Throwable exception) {
		TaskLogCommon.logTaskEnd(_task.getName(), type, message, exception);
		checkIsTaskEndResultType(type);
		TaskResult oldResult = _currentResult;
		Date start;
		if (_currentResult == null) {
			start = new Date();
			Logger.error("The task is about to end, but there is no current result.", TransientTaskLog.class);
		} else if (_currentResult.getResultType() != ResultType.NOT_FINISHED) {
			start = _currentResult.getStartDate();
			Logger.error("The task is about to end, but the last result is not a 'currently running' result.",
				TransientTaskLog.class);
		} else {
			start = _currentResult.getStartDate();
		}
		_currentResult = null;
		TaskResult newResult = new TransientTaskResult(_task, type, message, start, new Date(), exception);
		TaskLogCommon.copyWarnings(oldResult, newResult);
		TaskLogCommon.copyLogFile(oldResult, newResult);
		_lastResult = newResult;
		addResultInternal(newResult);
		setState(TaskState.INACTIVE);
	}

	private void checkIsTaskEndResultType(ResultType type) {
		if (type.equals(ResultType.NOT_FINISHED) || type.equals(ResultType.UNKNOWN)) {
			throw new IllegalArgumentException("A " + Task.class.getSimpleName() + " cannot end in the state "
				+ ResultType.NOT_FINISHED + " or " + ResultType.UNKNOWN + ".");
		}
	}

	@Override
	public synchronized void taskCanceling() {
		TaskLogCommon.logTaskCanceling(_task.getName());
		setState(TaskState.CANCELING);
	}

	private void addResultInternal(TaskResult result) {
		if (TaskLog.PROBLEM_FILTER.accept(result)) {
			problems.add(result);
		} else {
			noProblems.add(result);
		}
	}

	/**
	 * Return the values to be displayed via {@link #toString()}.
	 * 
	 * @param aBuffer
	 *        The string buffer to be filled, must not be <code>null</code>.
	 * @return The buffer holding the requested values, never <code>null</code>.
	 */
	protected StringBuffer toStringValues(StringBuffer aBuffer) {
		return aBuffer
			.append("', success: #").append(getNoProblems().size())
			.append("', failures: #").append(this.problems.size());
	}

	@Override
	public synchronized String toString() {
		return this.getClass().getSimpleName() + " [" + this.toStringValues(new StringBuffer()).toString() + ']';
	}

	@Override
	public void taskDied(ResKey messageKey) {
		taskEnded(ResultType.ERROR, messageKey);
	}

}
