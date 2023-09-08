/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.result;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;

/**
 * Information about the result of a tasks run.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface TaskResult {

	/**
	 * Type of the task result.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public enum ResultType {

		/** The {@link Task} is still running, it's result therefore only initialized. */
		NOT_FINISHED(
				I18NConstants.NOT_FINISHED, I18NConstants.NOT_FINISHED_CHILDREN,
				Icons.NOT_FINISHED),

		/** Task finished as expected. */
		SUCCESS(
				I18NConstants.SUCCESS, I18NConstants.SUCCESS_CHILDREN,
				Icons.SUCCESS),

		/** Task has been canceled by a user. */
		CANCELED(
				I18NConstants.CANCELED, I18NConstants.CANCELED_CHILDREN,
				Icons.CANCELED),

		/** Task has logged {@link TaskResult#getWarnings() gui warnings} and finished its work. */
		WARNING(
				I18NConstants.WARNING, I18NConstants.WARNING_CHILDREN,
				com.top_logic.util.error.Icons.WARNING),

		/** Task has a failure and finished its work. */
		FAILURE(
				I18NConstants.FAILURE, I18NConstants.FAILURE_CHILDREN,
				Icons.FAILURE),

		/** Task has an error and finished its work. */
		ERROR(
				I18NConstants.ERROR, I18NConstants.ERROR_CHILDREN,
				com.top_logic.util.error.Icons.ERROR),

		/** Unknown state of task (will come up from TaskAccessor). */
		UNKNOWN(
				I18NConstants.UNKNOWN, I18NConstants.UNKNOWN_CHILDREN,
				Icons.UNKNOWN);

		private final ThemeImage _icon;

		private final ResKey _defaultMessageKey;

		private final ResKey _defaultCompositeMessageKey;

		private ResultType(ResKey defaultMessageKey, ResKey defaultCompositeMessageKey, ThemeImage icon) {
			_defaultMessageKey = defaultMessageKey;
			_defaultCompositeMessageKey = defaultCompositeMessageKey;
			_icon = icon;
		}

		/** Default I18N for the message of a {@link Task} that ended with this {@link ResultType}. */
		public ResKey getMessageI18N() {
			return _defaultMessageKey;
		}

		/**
		 * Default I18N key for the message of a {@link CompositeTask} where one of its children
		 * ended with this {@link ResultType}.
		 */
		public ResKey getCombinedMessageI18N() {
			return _defaultCompositeMessageKey;
		}

		/**
		 * The icon to display this result type.
		 */
		public ThemeImage getIcon() {
			return _icon;
		}

	}

	/**
	 * Getter for the {@link Task} that produced this {@link TaskResult}.
	 */
	Task getTask();

	/**
	 * Return the I18N message key of this task.
	 * 
	 * @return The I18N message key, never <code>null</code>.
	 */
	ResKey getMessage();

	/**
	 * Return the type of result for this task.
	 * 
	 * @return The requested result, never <code>null</code>.
	 */
	ResultType getResultType();

	/**
	 * This method returns the exception.
	 * 
	 * @return The string representation of the exception, may be <code>null</code>.
	 */
	String getExceptionDump();

	/**
	 * This method returns the start date.
	 * 
	 * @return Returns the start date.
	 */
	Date getStartDate();

	/**
	 * This method returns the end date.
	 * 
	 * @return Returns the end date.
	 */
	Date getEndDate();

	/**
	 * Return the duration of the executed task.
	 * 
	 * @return The duration, may be <code>null</code> when {@link #getStartDate()} or
	 *         {@link #getEndDate()} is <code>null</code>.
	 */
	Long getDuration();

	/**
	 * The ui name for the cluster node on which this {@link TaskResult} was produced.
	 */
	String getClusterNode();

	/**
	 * The {@link File} which is used for logging.
	 * <p>
	 * Is <code>null</code> if there is none. <br/>
	 * It is not guaranteed, that this file still exists. Log files are often automatically deleted
	 * after some time, or move to another location. But if there ever was log file, it's original
	 * location has to be returned by this method. Even if it no longer exists or has been moved
	 * somewhere else. (It is still useful for administrators for restoring the log file from
	 * backups.)
	 * </p>
	 */
	File getLogFile();

	/**
	 * @see #getLogFile()
	 * 
	 * @param logFile
	 *        Is allowed to be <code>null</code>.
	 */
	void setLogFile(File logFile);

	/**
	 * Is there a gui warning?
	 * 
	 * @see #getWarnings()
	 */
	boolean hasWarnings();

	/**
	 * The gui warnings logged for this {@link TaskResult}.
	 * <p>
	 * A "gui warning" is a message shown in the scheduler gui and not in the normal application
	 * log.
	 * </p>
	 * 
	 * @return A copy of the list of warnings. It may be unmodifiable. Never <code>null</code>. Does
	 *         not contain <code>null</code>.
	 */
	List<String> getWarnings();

	/**
	 * Appends the given warning to the {@link #getWarnings() gui warning list}.
	 * 
	 * @param warning
	 *        Must not be <code>null</code>.
	 */
	void addWarning(String warning);

}
