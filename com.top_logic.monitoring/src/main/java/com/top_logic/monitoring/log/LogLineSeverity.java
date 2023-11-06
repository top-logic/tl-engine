/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.NameBuilder;

/**
 * The severity of a {@link LogLine}.
 * <p>
 * As Log4j supports custom log severities, this cannot be a fixed enum.
 * </p>
 * <p>
 * Using the Log4j <code>org.apache.logging.log4j.Level</code> type or even just referencing it and
 * its constants is not possible, as Top-Logic can be used without Log4j and therefore must not have
 * any compile time dependencies to it.
 * </p>
 * <p>
 * This class is immutable and therefore thread-safe.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogLineSeverity implements Comparable<LogLineSeverity> {

	/** {@link Level#FATAL} */
	public static final LogLineSeverity FATAL = new LogLineSeverity("FATAL", 6000);

	/** {@link Level#ERROR} */
	public static final LogLineSeverity ERROR = new LogLineSeverity("ERROR", 5000);

	/** {@link Level#WARN} */
	public static final LogLineSeverity WARN = new LogLineSeverity("WARN", 4000);

	/** {@link Level#INFO} */
	public static final LogLineSeverity INFO = new LogLineSeverity("INFO", 3000);

	/** {@link Level#DEBUG} */
	public static final LogLineSeverity DEBUG = new LogLineSeverity("DEBUG", 2000);

	/** Not used in Top-Logic, but one of the official Log4j log levels. */
	public static final LogLineSeverity TRACE = new LogLineSeverity("TRACE", 1000);

	/** The standard log severities as defined by Log4j. */
	public static final Map<String, LogLineSeverity> STANDARD_SEVERITIES = Map.of(
		FATAL.getName(), FATAL,
		ERROR.getName(), ERROR,
		WARN.getName(), WARN,
		INFO.getName(), INFO,
		DEBUG.getName(), DEBUG,
		TRACE.getName(), TRACE);

	private final String _name;

	private final int _sortOder;

	private final String _cssClass;

	/**
	 * Creates a {@link LogLineSeverity}.
	 * 
	 * @param sortOrder
	 *        The higher the value, the more sever.
	 */
	public LogLineSeverity(String name, int sortOrder) {
		_name = name;
		_sortOder = sortOrder;
		/* Using a non-compiled regex is no problem here, as new log levels should be very, very rare. */
		_cssClass = "tl-log-lines-table--" + name.toLowerCase().replaceAll("[^a-z0-9_-]", "");
	}

	/** The text in log files used for this severity. */
	public String getName() {
		return _name;
	}

	/** A number for sorting the severity: Higher numbers means higher severity. */
	public int getSortOrder() {
		return _sortOder;
	}

	/** The CSS class for displaying a {@link LogLine} with this severity. */
	public String getCssClass() {
		return _cssClass;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_name, _sortOder);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof LogLineSeverity)) {
			return false;
		}
		LogLineSeverity otherSeverity = (LogLineSeverity) other;
		return Objects.equals(getName(), otherSeverity.getName())
			&& getSortOrder() == otherSeverity.getSortOrder();
	}

	@Override
	public int compareTo(LogLineSeverity other) {
		if (other == null) {
			/* "null" has the least severity: If a message does not even have a severity, it
			 * shouldn't be important. */
			return 1;
		}
		return Integer.compare(getSortOrder(), other.getSortOrder());
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.addUnnamed(getName())
			.add("sort-order", getSortOrder())
			.build();
	}

}
