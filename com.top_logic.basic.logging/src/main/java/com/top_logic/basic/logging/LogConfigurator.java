/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.logging;

import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Service for configuring the logging system from within the application.
 * 
 * <p>
 * The concrete instance for the used logging subsystem must be provided in
 * <code>META-INF/services/com.top_logic.basic.logging.LogConfigurator</code>.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LogConfigurator {

	/**
	 * TL logging configuration to set the {@link Exception} level.
	 * 
	 * @see #getExceptionLevel()
	 */
	protected static final String EXCEPTION_LEVEL_PROPERTY = "com.top_logic.basic.Logger" + ".exceptionLevel";

	/**
	 * TL logging configuration to determine whether messages must be traced.
	 * 
	 * @see #isTraceMessages()
	 */
	protected static final String TRACE_MESSAGES_PROPERTY = "com.top_logic.basic.Logger" + ".traceMessages";

	/**
	 * TL logging configuration to determine whether exceptions must be traced.
	 * 
	 * @see #isTraceExceptions()
	 */
	protected static final String TRACE_EXCEPTIONS_PROPERTY = "com.top_logic.basic.Logger" + ".traceExceptions";

	private static final LogConfigurator INSTANCE;

	/** key for log4j so adjust the Loglevel. */
	protected static final String STDOUT_LEVEL_PROPERTY = "com.top_logic.basic.Logger" + ".stdoutLogLevel";

	/** When true Exceptions are logged with their stack trace. */
	private boolean traceExceptions = true;

	/** When true all Messages are logged with their stack trace. */
	private boolean traceMessages = false;

	/** Minimum log level to log stack traces. */
	private Level exceptionLevel = Level.DEBUG;

	static {
		ServiceLoader<LogConfigurator> loader = ServiceLoader.load(LogConfigurator.class);
		Iterator<LogConfigurator> services = loader.iterator();
		if (!services.hasNext()) {
			throw new AssertionError(
				"No LogConfigurator service configuration found in class path: "
					+ System.getProperty("java.class.path"));
		}
		INSTANCE = services.next();
		if (services.hasNext()) {
			throw new AssertionError(
				"More than one LogConfigurator service configuration in class path, inconsistent configuration: "
					+ System.getProperty("java.class.path"));
		}
	}

	/**
	 * The {@link LogConfigurator} instance.
	 */
	public static LogConfigurator getInstance() {
		return INSTANCE;
	}

	public abstract void configure(URL configuration);

	private static boolean getBooleanValue(Properties properties, String name, boolean defaultValue) {
		String value = properties.getProperty(name);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	private static Level getLevelValue(Properties properties, String name, Level defaultLevel) {
		String value = properties.getProperty(name);
		if (value == null || value.isEmpty()) {
			return defaultLevel;
		}
		return Level.valueOf(value);
	}

	public abstract void configureStdout(String aLevel);

	public void configureStdout() {
		String theLevel = System.getProperty(STDOUT_LEVEL_PROPERTY);
		if (theLevel == null) {
			theLevel = "ERROR";
		}

		configureStdout(theLevel);
	}

	public abstract void reset();

	public final boolean isTraceExceptions() {
		return traceExceptions;
	}

	public void setTraceExceptions(boolean value) {
		traceExceptions = value;
	}

	public Level getExceptionLevel() {
		return exceptionLevel;
	}

	public void setExceptionLevel(String level) {
		exceptionLevel = Level.valueOf(level);
	}

	public final boolean isTraceMessages() {
		return traceMessages;
	}

	public void setTraceMessages(boolean value) {
		traceMessages = value;
	}

	/**
	 * Adds a mark for Log4j.
	 * <p>
	 * Use <code>com.top_logic.basic.logging.LogUtil#withLogMark(Runnable, String, String)</code> if
	 * possible, as that already takes care of removing the mark afterwards.
	 * </p>
	 * <p>
	 * After calling this method, call {@link #removeLogMark(String)} in a matching finally block.
	 * </p>
	 * <p>
	 * This mark can be used in the log configuration to redirect every log message from this thread
	 * to a specific log file, no matter which class logs the message.
	 * </p>
	 */
	public abstract void addLogMark(String key, String value);

	/** Removes the mark set with {@link #addLogMark(String, String)}. */
	public abstract void removeLogMark(String key);

}
