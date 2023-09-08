/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.log;

import java.io.File;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;


/**
 * Methods used by multiple implementations of {@link TaskLog}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskLogCommon {

	/**
	 * Copy the warnings from the old {@link TaskResult} to the new one, if both are not
	 * <code>null</code>.
	 * 
	 * @param oldResult
	 *        is allowed to be <code>null</code>.
	 * @param newResult
	 *        is allowed to be <code>null</code>.
	 */
	public static void copyWarnings(TaskResult oldResult, TaskResult newResult) {
		if (oldResult != null && newResult != null) {
			for (String warning : oldResult.getWarnings()) {
				newResult.addWarning(warning);
			}
		}
	}

	/** Copy the log file from the old {@link TaskResult} to the new one. */
	public static void copyLogFile(TaskResult oldResult, TaskResult newResult) {
		if (oldResult != null && newResult != null) {
			File logFile = oldResult.getLogFile();
			newResult.setLogFile(logFile);
		}
	}

	/** Writes into the application log that the task is starting. */
	public static void logTaskStarted(String taskName) {
		logInfo("Task '" + taskName + "' started.");
	}

	/** Writes into the application log that the task is canceling. */
	public static void logTaskCanceling(String taskName) {
		logInfo("Task '" + taskName + "' canceling.");
	}

	/** Writes into the application log that the task is ending. */
	public static void logTaskEnd(String taskName, ResultType resultType, ResKey message, Throwable exception) {
		String fullMessage = "Task '" + taskName + "' ending: " + Resources.getLogInstance().getString(message);
		if (resultType == null) {
			logError(fullMessage, exception);
			return;
		}
		switch (resultType) {
			case SUCCESS: {
				logInfo(fullMessage, exception);
				return;
			}
			case CANCELED:
			case WARNING: {
				logWarning(fullMessage, exception);
				return;
			}
			case NOT_FINISHED:
			case FAILURE:
			case ERROR:
			case UNKNOWN: {
				logError(fullMessage, exception);
				return;
			}
			default: {
				logError(fullMessage, exception);
				return;
			}
		}
	}

	private static void logInfo(String message) {
		Logger.info(message, TaskLogCommon.class);
	}

	private static void logInfo(String message, Throwable exception) {
		Logger.info(message, exception, TaskLogCommon.class);
	}

	private static void logWarning(String message, Throwable exception) {
		Logger.warn(message, exception, TaskLogCommon.class);
	}

	private static void logError(String message, Throwable exception) {
		Logger.error(message, exception, TaskLogCommon.class);
	}

}
