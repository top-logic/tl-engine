/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.logging;

import java.util.function.Supplier;

import com.top_logic.basic.Logger;

/**
 * Utility methods for logging.
 * 
 * @see Logger
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogUtil {

	/**
	 * Runs the {@link Runnable} and logs the begin and the end, the latter in a finally block.
	 * 
	 * @param caller
	 *        The class for which the logging is performed. The same as the second parameter of
	 *        {@link Logger#info(String, Object)} if the class would do the logging itself.
	 * @param description
	 *        The suffix for the log message: <em>What</em> is being run. Is not allowed to be null.
	 * @param runnable
	 *        Is not allowed to be null.
	 */
	public static void withBeginEndLogging(Class<?> caller, String description, Runnable runnable) {
		Logger.info("Begin: " + description, caller);
		try {
			runnable.run();
			Logger.info("End: " + description, caller);
		} catch (Throwable exception) {
			Logger.error("End with exception: " + description + " Cause: " + exception.getMessage(), exception, caller);
			throw exception;
		}
	}

	/**
	 * Runs the {@link Supplier} and logs the begin and the end, the latter in a finally block.
	 * 
	 * @param caller
	 *        The class for which the logging is performed. The same as the second parameter of
	 *        {@link Logger#info(String, Object)} if the class would do the logging itself.
	 * @param description
	 *        The suffix for the log message: WHAT is being run. Is not allowed to be null.
	 * @param supplier
	 *        Is not allowed to be null.
	 */
	public static <T> T withBeginEndLogging(Class<?> caller, String description, Supplier<T> supplier) {
		Logger.info("Begin: " + description, caller);
		try {
			T result = supplier.get();
			Logger.info("End: " + description, caller);
			return result;
		} catch (Throwable exception) {
			Logger.error("End with exception: " + description + " Cause: " + exception.getMessage(), exception, caller);
			throw exception;
		}
	}

	/**
	 * Adds a mark for Log4j.
	 * <p>
	 * This mark can be used in the log configuration to redirect every log message from within this
	 * method to a specific log file, no matter which class logs the message.
	 * </p>
	 * <p>
	 * If a {@link Throwable} is thrown, it is wrapped into a {@link LogMarkRuntimeException} to
	 * make sure the log mark is not lost when it is eventually logged. To filter for such log
	 * messages in the Log4j configuration, filter for:
	 * 
	 * <pre>
	 * LOG MARK: 'your-key' = 'your-value'.
	 * </pre>
	 * 
	 * Example Regex:
	 * 
	 * <pre>
	 * .*\QLOG MARK: 'your-key' = 'your-value'.\E.*
	 * </pre>
	 * 
	 * {@link ThreadDeath} is not wrapped but directly rethrown.
	 * </p>
	 */
	public static void withLogMark(String key, String value, Runnable runnable) {
		LogConfigurator.getInstance().addLogMark(key, value);
		try {
			runnable.run();
		} catch (Throwable exception) {
			throw new LogMarkRuntimeException(key, value, exception);
		} finally {
			LogConfigurator.getInstance().removeLogMark(key);
		}
	}

	/**
	 * Adds a mark for Log4j.
	 * <p>
	 * A variant of {@link #withLogMark(String, String, Runnable)} which returns a value. See there
	 * for details.
	 * </p>
	 */
	public static <T> T withLogMark(String key, String value, Supplier<T> supplier) {
		LogConfigurator.getInstance().addLogMark(key, value);
		try {
			return supplier.get();
		} catch (Throwable exception) {
			throw new LogMarkRuntimeException(key, value, exception);
		} finally {
			LogConfigurator.getInstance().removeLogMark(key);
		}
	}

}
