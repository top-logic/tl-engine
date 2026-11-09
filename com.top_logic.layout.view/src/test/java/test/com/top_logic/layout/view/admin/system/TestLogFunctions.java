/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin.system;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.view.admin.system.LogFunctions;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.monitoring.log.LogLine;
import com.top_logic.monitoring.log.LogLineSeverity;

/**
 * Test of {@link LogFunctions}.
 */
@SuppressWarnings("javadoc")
public class TestLogFunctions extends TestCase {

	public void testNoMarkOutsideRequest() {
		assertNull("The test runs outside a request of a session.", LogFunctions.currentSessionMark());
		assertNull(LogFunctions.sessionMark(null));
	}

	public void testDump() {
		LogLine entry = new LogLine("tl", "tl.log", "Something failed.", new Date(),
			LogLineSeverity.STANDARD_SEVERITIES.values().iterator().next(), "test.Category", "main",
			"java.lang.RuntimeException: Something failed.\n\tat test.Category.run(Category.java:1)");

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream out = System.out;
		System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
		try {
			LogFunctions.dump(entry);
		} finally {
			System.setOut(out);
		}

		String printed = buffer.toString(StandardCharsets.UTF_8);
		assertTrue(printed, printed.startsWith("Log entry printed from the log viewer:"));
		assertTrue(printed, printed.contains("Something failed."));
		assertTrue(printed, printed.contains("at test.Category.run(Category.java:1)"));
	}

	/**
	 * The functions are called by the names the views use.
	 */
	public void testScriptNames() throws Exception {
		ThreadContext.inSystemContext(TestLogFunctions.class, () -> {
			assertNull(compile("logCurrentSessionMark()").execute());
			assertNull(compile("s -> logSessionMark($s)").execute((Object) null));
			assertNotNull(compile("l -> logDump($l)"));
			return null;
		});
	}

	private static QueryExecutor compile(String script) throws ConfigurationException {
		return QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestLogFunctions.class),
				TypeIndex.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE));
	}

}
