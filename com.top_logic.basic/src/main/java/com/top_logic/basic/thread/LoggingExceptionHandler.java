/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.lang.Thread.UncaughtExceptionHandler;

import com.top_logic.basic.Logger;

/**
 * An {@link UncaughtExceptionHandler} that logs the {@link Throwable} via
 * {@link Logger#error(String, Throwable, Object)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class LoggingExceptionHandler implements UncaughtExceptionHandler {

	/** Singleton instance of the {@link LoggingExceptionHandler}. */
	public static final LoggingExceptionHandler INSTANCE = new LoggingExceptionHandler();

	private LoggingExceptionHandler() {
		// Reduce visibility
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		String message = "Thread '" + thread.getName() + "' was destroyed by uncaught exception of type "
			+ exception.getClass().getName() + ". Message: " + exception.getMessage();
		Logger.error(message, exception, LoggingExceptionHandler.class);
	}

}
