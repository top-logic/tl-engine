/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Log} adapter logging to a {@link Logger}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class JavaLoggingLog implements Log {
	private final Logger _log;

	/**
	 * Creates a {@link JavaLoggingLog}.
	 */
	public JavaLoggingLog(Class<?> context) {
		this(Logger.getLogger(context.getName()));
	}

	/**
	 * Creates a {@link JavaLoggingLog}.
	 */
	public JavaLoggingLog(Logger log) {
		_log = log;
	}

	@Override
	public void error(String message, Throwable ex) {
		_log.log(Level.SEVERE, message, ex);
	}

	@Override
	public void info(String message, int verbosityLevel) {
		switch (verbosityLevel) {
			case Log.DEBUG:
				_log.log(Level.FINER, message);
				break;
			case Log.VERBOSE:
				_log.log(Level.FINE, message);
				break;
			case Log.WARN:
				_log.log(Level.WARNING, message);
				break;
			case Log.INFO:
			default:
				_log.log(Level.INFO, message);
				break;
		}
	}
}