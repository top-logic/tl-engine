/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.result;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskCommon;

/**
 * Information about the result of a tasks run.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TransientTaskResult implements TaskResult {

	private final Task _task;

	private final ResultType _result;

	private final ResKey _message;

	private final String _exception;

	private final Date _startDate;

	private final Date _endDate;

	private final String _clusterNode;

	private File _logFile;

	private List<String> _warnings;

	/**
	 * Convenience variant of
	 * {@link #TransientTaskResult(Task, com.top_logic.util.sched.task.result.TaskResult.ResultType, ResKey, Date, Date, Throwable)}
	 * where the exception is <code>null</code>.
	 */
	public TransientTaskResult(Task task, ResultType type, ResKey message, Date start, Date end) {
		this(task, type, message, start, end, null);
	}

	/**
	 * Creates a {@link TaskResult}.
	 * 
	 * @param type
	 *        The result of the run, must not be <code>null</code>.
	 * @param message
	 *        The I18N message key, must not be <code>null</code>.
	 * @param start
	 *        The date the task has started, must not be <code>null</code>.
	 * @param end
	 *        The date the task has ended, is allowed to be <code>null</code>.
	 * @param exception
	 *        The exception, is allowed to be <code>null</code>.
	 */
	public TransientTaskResult(Task task, ResultType type, ResKey message, Date start, Date end,
			Throwable exception) {
		// Fail early, as the code is based on the assumption that these values are never null.
		checkNonNull(task, type, message, start);
		_task = task;
		_result = type;
		_message = message;
		_startDate = start;
		_endDate = end;
		_exception = TaskResultCommon.dumpException(exception);
		_clusterNode = TaskCommon.getCurrentClusterNodeName();
	}

	private void checkNonNull(Task task, ResultType type, ResKey message, Date start) {
		if (task == null) {
			throw new NullPointerException("Task must not be null");
		}
		if (type == null) {
			throw new NullPointerException("ResultType must not be null");
		}
		if (message == null) {
			throw new NullPointerException("Message must not be null");
		}
		if (start == null) {
			throw new NullPointerException("Start date must not be null");
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + _result + ", '" + _message + "']";
	}

	@Override
	public Task getTask() {
		return _task;
	}

	@Override
	public ResKey getMessage() {
		return _message;
	}

	@Override
	public ResultType getResultType() {
		return _result;
	}

	@Override
	public String getExceptionDump() {
		return _exception;
	}

	@Override
	public Date getStartDate() {
		return _startDate;
	}

	@Override
	public Date getEndDate() {
		return _endDate;
	}

	@Override
	public Long getDuration() {
		return TaskResultCommon.calcDuration(getStartDate(), getEndDate());
	}

	@Override
	public String getClusterNode() {
		return _clusterNode;
	}

	@Override
	public synchronized File getLogFile() {
		return _logFile;
	}

	@Override
	public synchronized void setLogFile(File logFile) {
		_logFile = logFile;
	}

	@Override
	public synchronized boolean hasWarnings() {
		return !CollectionUtil.isEmptyOrNull(_warnings);
	}

	@Override
	public synchronized List<String> getWarnings() {
		if (_warnings == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(_warnings);
	}

	@Override
	public synchronized void addWarning(String message) {
		TaskResultCommon.logTaskWarning(_task.getName(), message);
		if (message == null) {
			throw new NullPointerException("The given 'warning' must not be null!");
		}
		if (_warnings == null) {
			_warnings = new ArrayList<>();
		}
		_warnings.add(message);
	}

}
