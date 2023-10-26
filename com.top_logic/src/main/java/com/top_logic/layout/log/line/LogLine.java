/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.line;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.util.Date;

import com.top_logic.basic.tools.NameBuilder;

/**
 * A log entry which was parsed from a log file.
 * <p>
 * Every getter can return null, when the log did not contain that data or it could not be parsed.
 * String getter return the empty String instead of null.
 * </p>
 * <p>
 * This class is recursively immutable.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogLine {

	/** @see #getFileCategory() */
	public static final String PROPERTY_FILE_CATEGORY = "file-category";

	/** @see #getFileName() */
	public static final String PROPERTY_FILE_NAME = "file-name";

	/** @see #getTime() */
	public static final String PROPERTY_TIME = "time";

	/** @see #getSeverity() */
	public static final String PROPERTY_SEVERITY = "severity";

	/** @see #getCategory() */
	public static final String PROPERTY_CATEGORY = "category";

	/** @see #getThread() */
	public static final String PROPERTY_THREAD = "thread";

	/** @see #getMessage() */
	public static final String PROPERTY_MESSAGE = "message";

	/** @see #getDetails() */
	public static final String PROPERTY_DETAILS = "details";

	private final String _fileName;

	private final String _fileCategory;

	private final String _message;

	private final Date _time;

	private final LogLineSeverity _severity;

	private final String _category;

	private final String _thread;

	private final String _details;

	/**
	 * Creates a {@link LogLine}.
	 * <p>
	 * All arguments can be null: If the String arguments are null, the empty String is used
	 * instead. If "time" or "severity" are null, their getters will return null.
	 * </p>
	 */
	public LogLine(String fileCategory, String fileName, String message, Date time,
			LogLineSeverity severity, String category, String thread, String details) {
		_fileName = nonNull(fileName);
		_fileCategory = nonNull(fileCategory);
		_message = nonNull(message);
		_time = time;
		_severity = severity;
		_category = nonNull(category);
		_thread = nonNull(thread);
		_details = nonNull(details);
	}

	/** The name of the log file which contained this entry. */
	public String getFileName() {
		return _fileName;
	}

	/** The file name without the counter and the file ending. */
	public String getFileCategory() {
		return _fileCategory;
	}

	/**
	 * The timestamp when this entry was logged.
	 * <p>
	 * Returns null when the log contained no time stamp or it could not be parsed.
	 * </p>
	 */
	public Date getTime() {
		return _time;
	}

	/**
	 * Whether this is just an information, a warning or an error.
	 * <p>
	 * Returns null when the log contained no severity or it could not be parsed.
	 * </p>
	 */
	public LogLineSeverity getSeverity() {
		return _severity;
	}

	/** The "category" is usually the Java class which logged the message. */
	public String getCategory() {
		return _category;
	}

	/** The {@link Thread} which logged the message. */
	public String getThread() {
		return _thread;
	}

	/** The first line of the message. */
	public String getMessage() {
		return _message;
	}

	/** Everything beyond the first line. */
	public String getDetails() {
		return _details;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add(PROPERTY_TIME, getTime())
			.add(PROPERTY_SEVERITY, getSeverity().getName())
			.add(PROPERTY_CATEGORY, getCategory())
			.add(PROPERTY_THREAD, getThread())
			.add(PROPERTY_FILE_NAME, getFileName())
			.add(PROPERTY_MESSAGE, getMessage())
			// Don't include 'file-category', as it is derived from 'file-name'.
			// Don't include the details, as that would be too much.
			.build();
	}

}
