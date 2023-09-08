/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.result;

import static com.top_logic.util.sched.task.TaskCommon.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.retry.Retry;
import com.top_logic.basic.util.retry.RetryResult;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.log.TaskLogWrapper;

/**
 * A persistent implementation of {@link TaskResult}.
 * <p>
 * Commits in this class have to be retried in most cases, if they fail. The reason is, that the
 * different schedulers of the cluster nodes are synchronized and communicating via the
 * KnowledgeBase. Therefore, concurrent write accesses can happen. Example: A task which is not node
 * local is told to stop from a user that is logged in on a different cluster node. But at the same
 * time the task is storing a warning. If they happen "concurrently" one of them will fail and has
 * to be retried.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TaskResultWrapper extends AbstractWrapper implements TaskResult {

	private static class WarningsProcessor {

		/**
		 * ASCII Character 'Escape' (<a
		 * href="https://en.wikipedia.org/wiki/Escape_character">Wikipedia</a>)
		 */
		private static final char ESCAPE_CHAR = '\u001B';

		/**
		 * ASCII Character 'Unit Separator' (<a
		 * href="https://en.wikipedia.org/wiki/Unit_separator#Field_separators">Wikipedia</a>)
		 */
		private static final char SEPARATOR_CHAR = '\u001F';

		private static final String ESCAPE_STRING = String.valueOf(ESCAPE_CHAR);

		private static final String SEPARATOR_STRING = String.valueOf(SEPARATOR_CHAR);

		public static List<String> decode(String rawWarnings) {
			if (StringServices.isEmpty(rawWarnings)) {
				return Collections.emptyList();
			}
			return splitWarnings(rawWarnings);
		}

		private static List<String> splitWarnings(String concatenatedWarnings) throws UnreachableAssertion {
			List<String> warnings = new ArrayList<>();
			StringBuilder builder = new StringBuilder();
			boolean unescape = false;
			for (char currentChar : concatenatedWarnings.toCharArray()) {
				if (unescape) {
					if ((currentChar != ESCAPE_CHAR) && (currentChar != SEPARATOR_CHAR)) {
						throw new RuntimeException("Illegal escape for character '" + currentChar
							+ "' with integer value '" + (int) currentChar + "'.");
					}
					unescape = false;
					builder.append(currentChar);
				} else {
					switch (currentChar) {
						case ESCAPE_CHAR:
							unescape = true;
							break;
						case SEPARATOR_CHAR:
							warnings.add(builder.toString());
							builder.delete(0, builder.length());
							break;
						default:
							builder.append(currentChar);
							break;
					}
				}
			}
			if (unescape) {
				throw new RuntimeException("Illegal warnings string. It ends with a single escape character.");
			}
			warnings.add(builder.toString());
			return warnings;
		}

		public static String encode(String oldEncodedWarnings, String newUnencodedWarning) {
			if (StringServices.isEmpty(oldEncodedWarnings)) {
				return escapeText(newUnencodedWarning);
			}
			int builderCapacity = oldEncodedWarnings.length() + newUnencodedWarning.length() + 10;
			StringBuilder warningsBuilder = new StringBuilder(builderCapacity);
			warningsBuilder.append(oldEncodedWarnings).append(SEPARATOR_CHAR).append(escapeText(newUnencodedWarning));
			return warningsBuilder.toString();
		}

		private static String escapeText(String warning) {
			return warning
				.replaceAll(ESCAPE_STRING, ESCAPE_STRING + ESCAPE_STRING)
				.replaceAll(SEPARATOR_STRING, ESCAPE_STRING + SEPARATOR_STRING);
		}

	}

	private static final String TYPE = "TaskResult";

	private static final String PROPERTY_TASK_NAME = "taskName";

	private static final String PROPERTY_MESSAGE = "message";

	private static final String PROPERTY_START = "start";

	private static final String PROPERTY_END = "end";

	private static final String PROPERTY_EXCEPTION_DUMP = "exceptionDump";

	private static final String PROPERTY_WARNINGS = "warnings";

	private static final String PROPERTY_LOG_FILE = "logFile";

	private static final String PROPERTY_RESULT_TYPE = "resultType";

	private static final String PROPERTY_CLUSTER_NAME = "clusterName";

	private static final String PROPERTY_CLUSTER_ID = "clusterId";

	/**
	 * Lazy initialized to prevent a cyclic dependency on system startup: <br/>
	 * 
	 * <pre>
	 * Scheduler.getSchedulerInstance().getKnownTasks() // where Scheduler.getSchedulerInstance() fails
	 * TaskUtil.findTaskByName()
	 * TaskResultWrapper constructor
	 * TaskLogWrapper.startupNodeCleanInternal()
	 * TaskImpl.initLog()
	 * TaskImpl constructor
	 * Scheduler.registerConfiguredTasks()
	 * </pre>
	 * <p>
	 * Synchronization is not necessary, as all threads compute the same task and if it is set
	 * multiple times, nothing breaks.
	 * </p>
	 */
	private Task _lazyTask;

	/**
	 * Creates a new {@link TaskResultWrapper}.
	 * <p>
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 * <br/>
	 * <b>Internal method!</b>
	 * </p>
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
	public static TaskResultWrapper createTaskResult(String taskName, ResultType type, ResKey message, Date start,
			Date end, Throwable exception) throws DataObjectException {
		// Fail early, as the code is based on the assumption that these values are never null.
		checkNonNull(taskName, type, message, start);
		KnowledgeObject taskResultKO = PersistencyLayer.getKnowledgeBase().createKnowledgeObject(TYPE);
		taskResultKO.setAttributeValue(PROPERTY_TASK_NAME, taskName);
		taskResultKO.setAttributeValue(PROPERTY_MESSAGE, ResKey.encode(message));
		taskResultKO.setAttributeValue(PROPERTY_START, start);
		taskResultKO.setAttributeValue(PROPERTY_END, end);
		taskResultKO.setAttributeValue(PROPERTY_EXCEPTION_DUMP, TaskResultCommon.dumpException(exception));
		taskResultKO.setAttributeValue(PROPERTY_WARNINGS, "");
		taskResultKO.setAttributeValue(PROPERTY_LOG_FILE, null);
		taskResultKO.setAttributeValue(PROPERTY_RESULT_TYPE, type.name());
		taskResultKO.setAttributeValue(PROPERTY_CLUSTER_NAME, getCurrentClusterNodeName());
		taskResultKO.setAttributeValue(PROPERTY_CLUSTER_ID, getCurrentClusterNodeId());
		return (TaskResultWrapper) WrapperFactory.getWrapper(taskResultKO);
	}

	private static void checkNonNull(String taskName, ResultType type, ResKey message, Date start) {
		if (taskName == null) {
			throw new NullPointerException("Task name must not be null");
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

	/**
	 * <b>NEVER USE THIS CONSTRUCTOR!</b> Use
	 * {@link TaskLogWrapper#createTaskResult(com.top_logic.util.sched.task.result.TaskResult.ResultType, ResKey, Date, Date, Throwable)}
	 * instead.
	 * <p>
	 * This constructor is only for the WrapperFactory!
	 * </p>
	 */
	public TaskResultWrapper(KnowledgeObject ko) {
		super(ko);
	}

	private String getTaskNameInternal() {
		try {
			return (String) tHandle().getAttributeValue(PROPERTY_TASK_NAME);
		} catch (NoSuchAttributeException ex) {
			// Unreachable, as there is a name attribute.
			throw new UnreachableAssertion(ex);
		}
	}

	@Override
	public Task getTask() {
		if (_lazyTask == null) {
			_lazyTask = Scheduler.getSchedulerInstance().getTaskByName(getTaskNameInternal());
		}
		return _lazyTask;
	}

	@Override
	public ResKey getMessage() {
		return ResKey.decode(getString(PROPERTY_MESSAGE));
	}

	@Override
	public ResultType getResultType() {
		return ResultType.valueOf(getString(PROPERTY_RESULT_TYPE));
	}

	@Override
	public String getExceptionDump() {
		return getString(PROPERTY_EXCEPTION_DUMP);
	}

	@Override
	public Date getStartDate() {
		return getDate(PROPERTY_START);
	}

	@Override
	public Date getEndDate() {
		return getDate(PROPERTY_END);
	}

	@Override
	public Long getDuration() {
		return TaskResultCommon.calcDuration(getStartDate(), getEndDate());
	}

	@Override
	public String getClusterNode() {
		return getString(PROPERTY_CLUSTER_NAME);
	}

	/** Getter for the id of the cluster node that produced this {@link TaskResult}. */
	public long getClusterNodeId() {
		return getLong(PROPERTY_CLUSTER_ID);
	}

	@Override
	public File getLogFile() {
		String logFilePath = getString(PROPERTY_LOG_FILE);
		return logFilePath == null ? null : new File(logFilePath);
	}

	@Override
	public void setLogFile(File logFile) {
		final String logFilePath = getFilePath(logFile);
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			() -> trySetLogFile(logFilePath));
		if (!result.isSuccess()) {
			String message = "Setting the log file for task '" + getTask().getName() + "' failed after "
				+ COMMIT_RETRIES + " attempts. Log file: '" + logFilePath + "'";
			throw ExceptionUtil.createException(message, result.getReason());
		}
	}

	private RetryResult<Void, Throwable> trySetLogFile(String logFilePath) {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			setString(PROPERTY_LOG_FILE, logFilePath);
			transaction.commit();
			return RetryResult.<Void, Throwable> createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	private String getFilePath(File logFile) {
		return logFile == null ? null : logFile.getAbsolutePath();
	}

	@Override
	public boolean hasWarnings() {
		return !StringServices.isEmpty(getWarningsInternal());
	}

	@Override
	public List<String> getWarnings() {
		return WarningsProcessor.decode(getWarningsInternal());
	}

	@Override
	public void addWarning(String warning) {
		TaskResultCommon.logTaskWarning(getTaskNameInternal(), warning);
		if (warning == null) {
			throw new NullPointerException("The given 'warning' must not be null!");
		}
		setWarningsInternal(WarningsProcessor.encode(getWarningsInternal(), warning));
	}

	private String getWarningsInternal() {
		return getString(PROPERTY_WARNINGS);
	}

	private void setWarningsInternal(final String warnings) {
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			() -> trySetWarnings(warnings));
		if (!result.isSuccess()) {
			String message = "Setting the warnings for task '" + getTask().getName() + "' failed after "
				+ COMMIT_RETRIES + " attempts. Warnings: '" + warnings + "'";
			throw ExceptionUtil.createException(message, result.getReason());
		}
	}

	private RetryResult<Void, Throwable> trySetWarnings(String warnings) {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			setString(PROPERTY_WARNINGS, warnings);
			transaction.commit();
			return RetryResult.<Void, Throwable> createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	/** <b>Internal Method, don't use!</b> */
	public void setResultType(ResultType type) {
		setString(PROPERTY_RESULT_TYPE, type.name());
	}

	/** <b>Internal Method, don't use!</b> */
	public void setMessage(ResKey message) {
		setString(PROPERTY_MESSAGE, ResKey.encode(message));
	}

	/** <b>Internal Method, don't use!</b> */
	public void setEnd(Date end) {
		setDate(PROPERTY_END, end);
	}

	/** <b>Internal Method, don't use!</b> */
	public void setException(Throwable exception) {
		setString(PROPERTY_EXCEPTION_DUMP, TaskResultCommon.dumpException(exception));
	}

	@Override
	protected String toStringValues() {
		return new StringBuilder()
			.append(", ").append(PROPERTY_TASK_NAME).append(": ").append(getTaskNameInternal())
			.append(", ").append(PROPERTY_CLUSTER_NAME).append(": ").append(getClusterNode())
			.append(", ").append(PROPERTY_CLUSTER_ID).append(": ").append(Long.toString(getClusterNodeId()))
			.append(", ").append(PROPERTY_START).append(": ").append(getStartDate())
			.append(", ").append(PROPERTY_END).append(": ").append(getEndDate())
			.append(", ").append(PROPERTY_RESULT_TYPE).append(": ").append(getResultType())
			.toString();
	}

}
