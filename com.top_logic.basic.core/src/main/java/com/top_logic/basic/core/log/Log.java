/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.log;

/**
 * Light-weight logging interface for service classes.
 * 
 * <p>
 * {@link Log} supports both, reporting detailed information about non-fatal errors and feedback
 * about the occurrence of (potentially multiple) non-fatal errors to the caller.
 * </p>
 * 
 * <p>
 * Using the {@link Log} interface is especially useful for tooling implementations that inform the
 * the programmer of multiple errors e.g. in a configuration file. Stopping with a (fatal) exception
 * at the first encountered problem results in inconvenient usage characteristics, because the tool
 * has to be restarted after fixing each single problem. At the other hand, it is absolutely
 * necessary that the tool implementation terminates abnormally, if any error has occurred, to
 * ensure that a tool chain can interrupt.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Log {

	/**
	 * Verbosity level that informs about implementation details.
	 * 
	 * @see #info(String, int)
	 */
	int DEBUG = 3;

	/**
	 * Verbosity level that gives minimal status information.
	 * 
	 * @see #info(String, int)
	 */
	int INFO = 1;

	/**
	 * Verbosity level that gives extensive status information.
	 * 
	 * @see #info(String, int)
	 */
	int VERBOSE = 2;

	/**
	 * Verbosity level that gives information about unexpected behaviour which does not lead to
	 * problems which make parts of the system unusable.
	 * 
	 * @see #info(String, int)
	 */
	int WARN = 0;

	/**
	 * Reports a non-fatal error.
	 * 
	 * @param message
	 *        The error message to report.
	 */
	default void error(String message) {
		error(message, null);
	}

	/**
	 * Converts an exception that occurred within the service implementation into a non-fatal error.
	 * 
	 * @param message
	 *        The error message to report.
	 * @param ex
	 *        The problem that is converted into a non-fatal error.
	 */
	void error(String message, Throwable ex);

	/**
	 * Reports a status information with verbosity {@link #INFO}.
	 * 
	 * @param message The message to report. 
	 */
	default void info(String message) {
		info(message, INFO);
	}

	/**
	 * Reports a status information with the given verbosity.
	 * 
	 * @param verbosityLevel
	 *        One of {@link #WARN}, {@link #INFO}, {@link #VERBOSE}, or {@link #DEBUG}.
	 * @param message
	 *        The message to report.
	 */
	void info(String message, int verbosityLevel);

}
