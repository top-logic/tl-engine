/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.log;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.sched.task.Task;

/**
 * Factory for log files of {@link Task}s.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class TaskLogFileFactory {

	/** The log file ending. */
	public static final String FILE_ENDING = ".log";

	/** The alias to be resolved with the {@link AliasManager} to get the logging root directory. */
	public static final String LOG_PATH_ALIAS = "%LOG_PATH%";

	/** The time {@link Format} expression used for the log file name. */
	public static final String LOG_NAME_TIME_FORMAT_EXPRESSION = "yyyy_MM_dd_'T'_HH_mm_ss_SSS";

	private SimpleDateFormat _logNameTimeFormat;

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the
	 * {@link DefaultTaskLogFileFactory}.
	 */
	protected TaskLogFileFactory() {
		// See JavaDoc above.
	}

	/**
	 * Is called when a new run of the given {@link Task} starts. Creates the log {@link File} for
	 * this run.
	 * <p>
	 * None of the parameters is allowed to be <code>null</code>.
	 * </p>
	 * 
	 * @return Is <code>null</code> when no log file is used.
	 */
	public File getLogFile(Task task, Date start) {
		String logFilePath = buildLogFilePath(task, start);
		try {
			File logFile = new File(logFilePath);
			createFile(logFile);
			return logFile;
		} catch (IOException e) {
			String message = "Failed to create or access the log file ('" + logFilePath
				+ "') or its parent directories: " + e.getMessage();
			throw new RuntimeException(message, e);
		}
	}

	private void createFile(File logFile) throws IOException {
		if (logFile.exists()) {
			String message = "Log file for this task run already exists. That should not happen. Path: "
				+ logFile.getAbsolutePath();
			Logger.warn(message, TaskLogFileFactory.class);
		} else {
			logFile.getParentFile().mkdirs();
			logFile.createNewFile();
		}
	}

	/** Builds the path and name of the log file for this task run. */
	protected String buildLogFilePath(Task task, Date start) {
		String loggingRoot = AliasManager.getInstance().replace(LOG_PATH_ALIAS);
		String fileName = buildLogFileName(task, start);
		return loggingRoot + File.separator + fileName;
	}

	/** Builds the name (not the path) of the log file for this task run. */
	protected String buildLogFileName(Task task, Date start) {
		String sortableDateString = formatLogNameTime(start);
		return sanitizeForFileName(task.getName()) + "_" + sortableDateString + FILE_ENDING;
	}

	/** Sanitize the name of the given task */
	protected String sanitizeForFileName(String rawName) {
		return rawName.replaceAll("[^a-zA-Z0-9_.]+", "_");
	}

	/**
	 * Formats the time for the log file name.
	 * 
	 * @param time
	 *        The start time of this task run.
	 */
	protected String formatLogNameTime(Date time) {
		return getLogNameTimeFormat().format(time);
	}

	/** The time {@link Format} used for the log file name. */
	protected Format getLogNameTimeFormat() {
		if (_logNameTimeFormat == null) {
			_logNameTimeFormat = CalendarUtil.newSimpleDateFormat(LOG_NAME_TIME_FORMAT_EXPRESSION);
		}
		return _logNameTimeFormat;
	}

}
