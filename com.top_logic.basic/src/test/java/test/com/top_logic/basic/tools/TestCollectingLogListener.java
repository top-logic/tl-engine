/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;

/**
 * {@link TestCase} for: {@link CollectingLogListener}
 * 
 * @author     <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public class TestCollectingLogListener extends TestCase {

	private static final LinkedHashSet<Level> ALL_PRIORITIES =
		new LinkedHashSet<>(CollectionUtil.createList(Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL));

	private static final String TESTED_CLASS = CollectingLogListener.class.getSimpleName();
	
	private static final Object CALLER = new NamedConstant("caller(" + TestCollectingLogListener.class.getCanonicalName() + ")");
	
	private static final String STANDARD_LOG_MESSAGE =
		"This is a test message. It does not indicate an error or anything. It is used to test the class: " + TESTED_CLASS;
	
	private static final String STANDARD_EXCEPTION_MESSAGE =
		"This is a test exception. It does not indicate an error or anything. It is used to test the class: " + TESTED_CLASS;

	public static Test suite () {
		return BasicTestSetup.createBasicTestSetup(new TestSuite (TestCollectingLogListener.class));
	}

	public void testLogCollecting() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		// Activate it on both ways, as we check later, whether both of them work.
		// For now, we only check if it collects the log at all.
		logListener.activate();
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " seems to collect nothing at all.";
		assertTrue(errorMessage, !filterLogEntries(logListener.getLogEntries()).isEmpty());
	}
	
	public void testContstructorActivation() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " ignores the constructor parameter 'activate'. It did not collect log entries but the parameter was set to 'true' and a message was logged.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).size() == 1);
	}
	
	public void testContstructorDeactivation() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, false);
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " ignores the constructor parameter 'activate': It collected log entries but the parameter was set to 'false'.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).isEmpty());
	}
	
	public void testActivate() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, false);
		logListener.activate();
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " ignores the method 'activate'.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).size() == 1);
	}
	
	public void testDeactivate() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		logListener.activate();
		logListener.deactivate();
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		String errorMessage = "The " + TESTED_CLASS + " ignores the method 'deactivate'.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).isEmpty());
	}
	
	public void testLogEntryMessage() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " stores a wrong log message.";
		LogEntry logEntry = singleValueOrFail(filterLogEntries(logListener.getLogEntries()));
		assertEquals(errorMessage, STANDARD_LOG_MESSAGE, logEntry.getMessage());
	}
	
	public void testLogEntryPriority1() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " stores a wrong log message priority.";
		LogEntry logEntry = singleValueOrFail(filterLogEntries(logListener.getLogEntries()));
		assertEquals(errorMessage, Level.DEBUG, logEntry.getPriority());
	}
	
	public void testLogEntryPriority2() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		Logger.fatal(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " stores a wrong log message priority.";
		LogEntry logEntry = singleValueOrFail(filterLogEntries(logListener.getLogEntries()));
		assertEquals(errorMessage, Level.FATAL, logEntry.getPriority());
	}
	
	public void testLogEntryException() {
		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		Throwable exception = new RuntimeException(STANDARD_EXCEPTION_MESSAGE);
		Logger.debug(STANDARD_LOG_MESSAGE, exception, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " stores a wrong log message exception.";
		LogEntry logEntry = singleValueOrFail(filterLogEntries(logListener.getLogEntries()));
		assertEquals(errorMessage, exception, logEntry.getException().getElseError());
	}

	private LogEntry singleValueOrFail(List<LogEntry> logEntries) {
		if (logEntries.isEmpty()) {
			fail("No LogEntry given.");
		}
		return CollectionUtil.getSingleValueFromCollection(logEntries);
	}

	public void testExceptionLevelInfo() {
		String theCurrent = Logger.getExceptionLevel();

		assertNotNull("Exception level must not be null", theCurrent);
		Logger.setExceptionLevel("INFO");

		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		Throwable exception = new RuntimeException(STANDARD_EXCEPTION_MESSAGE);
		Logger.debug(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.info(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.warn(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.error(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.fatal(STANDARD_LOG_MESSAGE, exception, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " stores a wrong log message exception.";

		for (LogEntry logEntry : filterLogEntries(logListener.getLogEntries())) {
			switch (logEntry.getPriority()) {
				case DEBUG:
					assertEquals(errorMessage, Maybe.none(), logEntry.getException());
					break;
				case INFO:
				case WARN:
				case ERROR:
				case FATAL:
					assertEquals(errorMessage, exception, logEntry.getException().getElseError());
					break;
			}
		}

		Logger.setExceptionLevel(theCurrent);
	}

	public void testExceptionLevelWarn() {
		String theCurrent = Logger.getExceptionLevel();

		assertNotNull("Exception level must not be null", theCurrent);
		Logger.setExceptionLevel("WARN");

		CollectingLogListener logListener = new CollectingLogListener(ALL_PRIORITIES, true);
		Throwable exception = new RuntimeException(STANDARD_EXCEPTION_MESSAGE);
		Logger.debug(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.info(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.warn(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.error(STANDARD_LOG_MESSAGE, exception, CALLER);
		Logger.fatal(STANDARD_LOG_MESSAGE, exception, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " stores a wrong log message exception.";

		for (LogEntry logEntry : filterLogEntries(logListener.getLogEntries())) {
			switch (logEntry.getPriority()) {
				case DEBUG:
				case INFO:
					assertEquals(errorMessage, Maybe.none(), logEntry.getException());
					break;
				case WARN:
				case ERROR:
				case FATAL:
					assertEquals(errorMessage, exception, logEntry.getException().getElseError());
					break;
			}
		}

		Logger.setExceptionLevel(theCurrent);
	}
	
	public void testCollectOnlyRequestedPriorities1() {
		CollectingLogListener logListener = new CollectingLogListener(Collections.singleton(Level.DEBUG), true);
		Logger.info(STANDARD_LOG_MESSAGE, CALLER);
		Logger.warn(STANDARD_LOG_MESSAGE, CALLER);
		Logger.error(STANDARD_LOG_MESSAGE, CALLER);
		Logger.fatal(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " logged messages with priorities that are not requested.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).isEmpty());
	}
	
	public void testCollectOnlyRequestedPriorities2() {
		CollectingLogListener logListener = new CollectingLogListener(Collections.singleton(Level.FATAL), true);
		Logger.debug(STANDARD_LOG_MESSAGE, CALLER);
		Logger.info(STANDARD_LOG_MESSAGE, CALLER);
		Logger.warn(STANDARD_LOG_MESSAGE, CALLER);
		Logger.error(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " logged messages with priorities that are not requested.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).isEmpty());
	}
	
	public void testCollectOnlyRequestedPriorities3() {
		CollectingLogListener logListener = new CollectingLogListener(CollectionUtil.createSet(Level.DEBUG, Level.INFO, Level.WARN), true);
		Logger.info(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " did not log a messages with a requested priority.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).size() == 1);
	}
	
	public void testCollectOnlyRequestedPriorities4() {
		CollectingLogListener logListener = new CollectingLogListener(CollectionUtil.createSet(Level.DEBUG, Level.INFO, Level.WARN), true);
		Logger.warn(STANDARD_LOG_MESSAGE, CALLER);
		logListener.deactivate();
		String errorMessage = "The " + TESTED_CLASS + " did not log a messages with a requested priority.";
		assertTrue(errorMessage, filterLogEntries(logListener.getLogEntries()).size() == 1);
	}
	
	private List<LogEntry> filterLogEntries(List<LogEntry> unfilteredLogEntries) {
		List<LogEntry> filteredLogEntries = new ArrayList<>();
		for (LogEntry logEntry : unfilteredLogEntries) {
			if (logEntry.getCaller().equals(CALLER)) {
				filteredLogEntries.add(logEntry);
			}
		}
		return filteredLogEntries;
	}
	
}
