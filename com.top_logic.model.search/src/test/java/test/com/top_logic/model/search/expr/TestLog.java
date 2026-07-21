/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.EnumSet;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for the {@code log()} and {@code info()} functions and their {@code level} argument.
 *
 * @see com.top_logic.model.search.expr.Log
 * @see com.top_logic.model.search.expr.Info
 * @see com.top_logic.model.search.expr.LogLevel
 */
@SuppressWarnings("javadoc")
public class TestLog extends AbstractSearchExpressionTest {

	private CollectingLogListener _log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_log = new CollectingLogListener(EnumSet.allOf(Level.class), true);
	}

	@Override
	protected void tearDown() throws Exception {
		_log.deactivate();
		_log = null;
		super.tearDown();
	}

	public void testDefaultLevelIsInfo() throws Exception {
		assertNull(eval("log('marker-default')"));
		assertLogged(Level.INFO, "marker-default");
	}

	public void testFormatArguments() throws Exception {
		assertNull(eval("log('marker-format: {0} {1}', 'a', 'b')"));
		assertLogged(Level.INFO, "marker-format: a b");
	}

	public void testNamedLevelWarn() throws Exception {
		assertNull(eval("log('marker-warn', level: 'warn')"));
		assertLogged(Level.WARN, "marker-warn");
	}

	public void testFormatArgumentsWithLevel() throws Exception {
		assertNull(eval("log('marker-both: {0}', 42, level: 'error')"));
		assertLogged(Level.ERROR, "marker-both: 42");
	}

	public void testLevelCaseInsensitive() throws Exception {
		assertNull(eval("log('marker-case', level: 'FaTaL')"));
		assertLogged(Level.FATAL, "marker-case");
	}

	public void testInvalidLevelIsRejected() throws Exception {
		try {
			eval("log('marker-invalid', level: 'bogus')");
			fail("Expected " + TopLogicException.class.getName() + " for an invalid level.");
		} catch (TopLogicException expected) {
			// Expected.
		}
	}

	/**
	 * The UI message can not be inspected without a display context; the {@code InfoService}
	 * silently ignores the message in this case, so these tests only assert that the various
	 * argument combinations evaluate without failure.
	 */
	public void testInfoArguments() throws Exception {
		assertNull(eval("info('Hello')"));
		assertNull(eval("info('Hello', 'Detail')"));
		assertNull(eval("info('Hello', 'Detail', 'warn')"));
		assertNull(eval("info('Hello', level: 'error')"));
	}

	public void testInfoInvalidLevelIsRejected() throws Exception {
		try {
			eval("info('Hello', 'Detail', 'bogus')");
			fail("Expected " + TopLogicException.class.getName() + " for an invalid level.");
		} catch (TopLogicException expected) {
			// Expected.
		}
	}

	private void assertLogged(Level expectedLevel, String messageMarker) {
		LogEntry entry = findEntry(messageMarker);
		assertNotNull("No log entry containing '" + messageMarker + "'.", entry);
		assertEquals("Wrong log level for '" + messageMarker + "'.", expectedLevel, entry.getPriority());
	}

	private LogEntry findEntry(String messageMarker) {
		List<LogEntry> entries = _log.getLogEntries();
		for (LogEntry entry : entries) {
			if (entry.getMessage().contains(messageMarker)) {
				return entry;
			}
		}
		return null;
	}

	public static Test suite() {
		return suite(TestLog.class);
	}

}
