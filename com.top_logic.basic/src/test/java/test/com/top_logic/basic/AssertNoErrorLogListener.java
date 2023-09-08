/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.MultiError;
import com.top_logic.basic.tools.CollectingLogListener;

/**
 * An {@link AssertNoErrorLogListener} listens if any errors are logged.
 * <p>
 * They start listening as soon as they are created. <br/>
 * When {@link #assertNoErrorLogged(String)} is called, it checks whether an error was logged since
 * it was called the last time. If so, it throws an {@link AssertionFailedError} containing all that
 * errors. If no error was logged, it does nothing. <br/>
 * Whether warnings count as errors or not is specified by a constructor parameter. <br/>
 * {@link #getLogEntries()} and {@link #getAndClearLogEntries()} return only log entries that count
 * as an error.
 * </p>
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AssertNoErrorLogListener extends CollectingLogListener {
	
	public final static Set<Level> ERROR_AND_ABOVE =
		Collections.unmodifiableSet(CollectionUtil.createSet(Level.FATAL, Level.ERROR));
	
	public final static Set<Level> WARNING_AND_ABOVE =
		Collections.unmodifiableSet(CollectionUtil.createSet(Level.FATAL, Level.ERROR, Level.WARN));

	/**
	 * Creates a new {@link AssertNoErrorLogListener} that will treat warnings as errors.
	 * <br/>
	 * This shortcut exists to make the cleaner/preferred setting ("treat warnings as errors, too") the concise one.
	 * 
	 * @see #AssertNoErrorLogListener(boolean)
	 */
	public AssertNoErrorLogListener() {
		this(true);
	}
	
	/**
	 * Creates a new {@link AssertNoErrorLogListener} that will immediately start listening to the log.
	 * @param treatWarningsAsErrors Shall warnings count as errors, too?
	 */
	public AssertNoErrorLogListener(boolean treatWarningsAsErrors) {
		super(treatWarningsAsErrors ? WARNING_AND_ABOVE : ERROR_AND_ABOVE, true);
	}
	
	/**
	 * Asserts that no errors were logged since this method was called the last time. (Or since this
	 * object was created, if this method is called the first time.)
	 * <p>
	 * If an error was logged and this method was called and failed, another call of this method
	 * will only fail, if a new error was logged. That means, this method will not fail twice for
	 * the same log entry. <br/>
	 * Depending on the setting given in the constructor, warnings may count as errors or not.
	 * </p>
	 * 
	 * @throws AssertionFailedError
	 *         if an error was logged.
	 */
	public void assertNoErrorLogged(String errorMessagePrefix) {
		List<LogEntry> errorLog = getAndClearLogEntries();
		if (!errorLog.isEmpty()) {
			String message = errorMessagePrefix + " Assertion failed: " + errorLog.size()
				+ " error(s) were logged. Log messages:\n\t" + LogEntry.joinMessages(errorLog, "\n\t");
			Throwable cause = createErrorLoggedException(errorLog);
			throw (AssertionFailedError) new AssertionFailedError(message).initCause(cause);
		}
	}
	
	private Throwable createErrorLoggedException(List<LogEntry> errorLog) {
		if (errorLog.size() == 1) {
			return createExceptionFromLogEntry(errorLog.get(0));
		} else {
			List<Throwable> wrappedErrors = new ArrayList<>();
			for (LogEntry logEntry : errorLog) {
				Throwable wrappedError = createExceptionFromLogEntry(logEntry);
				wrappedErrors.add(wrappedError);
			}
			return new MultiError(errorLog.size() + " errors were logged during test execution.", wrappedErrors);
		}
	}

	private Throwable createExceptionFromLogEntry(LogEntry logEntry) {
		if (logEntry.getException().hasValue()) {
			return new RuntimeException(createErrorMessage(logEntry), logEntry.getException().get());
		} else {
			return new RuntimeException(createErrorMessage(logEntry));
		}
	}

	private String createErrorMessage(LogEntry logEntry) {
		return "An error was logged. Priority of logged message: " + logEntry.getPriority()
			+ "; Log Caller: '" + logEntry.getCaller() + "'; Message: " + logEntry.getMessage();
	}

}
