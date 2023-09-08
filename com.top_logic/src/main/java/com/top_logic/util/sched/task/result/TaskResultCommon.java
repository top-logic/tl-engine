/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Methods used by multiple implementations of {@link TaskResult}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskResultCommon {

	/**
	 * Returns a {@link List} with all the {@link TaskResult}s that have the given
	 * {@link ResultType}.
	 * 
	 * @param taskResults
	 *        Is allowed to be <code>null</code>.
	 * @param resultType
	 *        Is allowed to be <code>null</code>.
	 * 
	 * @return Never <code>null</code>.
	 */
	public static <T extends TaskResult> Collection<T> findAll(Collection<T> taskResults, ResultType resultType) {
		List<T> matches = new ArrayList<>();
		for (T taskResult : CollectionUtil.nonNull(taskResults)) {
			if (taskResult.getResultType().equals(resultType)) {
				matches.add(taskResult);
			}
		}
		return matches;
	}

	/**
	 * Partitions the given {@link TaskResult}s by their {@link ResultType}s.
	 * <p>
	 * The returned {@link Map} contains a {@link List} for every value of
	 * {@link ResultType#values()}, even for the key <code>null</code>.
	 * </p>
	 * 
	 * @param taskResults
	 *        Is allowed to be <code>null</code>.
	 * 
	 * @return Never <code>null</code>.
	 */
	public static <T extends TaskResult> Map<ResultType, Collection<T>> partitionByResultType(
			Collection<? extends T> taskResults) {
		Map<ResultType, Collection<T>> resultMap = new HashMap<>();
		for (ResultType resultType : ResultType.values()) {
			resultMap.put(resultType, new ArrayList<>());
		}
		// Should not be necessary, but better safe than sorry.
		resultMap.put(null, new ArrayList<>());

		for (T taskResult : CollectionUtil.nonNull(taskResults)) {
			resultMap.get(taskResult.getResultType()).add(taskResult);
		}
		return resultMap;
	}

	/**
	 * The duration from start to end in milliseconds.
	 * 
	 * @return <code>null</code> if start or end is <code>null</code>.
	 */
	public static Long calcDuration(Date start, Date end) {
		if ((start != null) && (end != null)) {
			return end.getTime() - start.getTime();
		}
		else {
			return null;
		}
	}

	/**
	 * Convert the given exception into a string.
	 * 
	 * @param exception
	 *        The exception to be converted, may be <code>null</code>.
	 * @return The requested string representation, may be <code>null</code>.
	 */
	public static String dumpException(Throwable exception) {
		if (exception == null) {
			return null;
		}
		else {
			return ExceptionUtil.printThrowableToString(exception);
		}
	}

	/** Writes the warning into the application log. */
	public static void logTaskWarning(String taskName, String warning) {
		logWarn("Task '" + taskName + "' reports a warning: " + warning);
	}

	private static void logWarn(String message) {
		Logger.warn(message, TaskResultCommon.class);
	}

}
