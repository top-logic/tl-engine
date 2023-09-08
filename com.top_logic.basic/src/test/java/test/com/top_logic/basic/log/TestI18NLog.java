/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.log;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.Log;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.i18n.log.BufferingI18NLog.Entry;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.util.ResKey;

/**
 * Test case for {@link I18NLog}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestI18NLog extends TestCase {

	public void testInfo() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		I18NLog log = buffer;
		ResKey message = ResKey.text("message");
		assertFalse(buffer.hasEntries());
		log.info(message);
		List<Entry> entries = buffer.getEntries();
		assertTrue(buffer.hasEntries());
		assertEquals(Level.INFO, entries.get(0).getLevel());
		assertEquals(message, entries.get(0).getMessage());
		assertEquals(null, entries.get(0).getProblem());
	}

	public void testError() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		I18NLog log = buffer;
		ResKey message = ResKey.text("message");
		log.error(message);
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.ERROR, entries.get(0).getLevel());
		assertEquals(message, entries.get(0).getMessage());
		assertEquals(null, entries.get(0).getProblem());
	}

	public void testFatal() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		I18NLog log = buffer;
		ResKey message = ResKey.text("message");
		StackTrace ex = new StackTrace();
		log.fatal(message, ex);
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.FATAL, entries.get(0).getLevel());
		assertEquals(message, entries.get(0).getMessage());
		assertEquals(ex, entries.get(0).getProblem());
	}

	public void testFilter() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		I18NLog log = buffer.filter(Level.ERROR);
		ResKey message1 = ResKey.text("message1");
		log.info(message1);
		ResKey message2 = ResKey.text("message2");
		log.error(message2);
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.ERROR, entries.get(0).getLevel());
		assertEquals(message2, entries.get(0).getMessage());
	}

	public void testTee1() {
		BufferingI18NLog all = new BufferingI18NLog();
		BufferingI18NLog errors = new BufferingI18NLog();
		I18NLog log = errors.filter(Level.ERROR).tee(all);
		doTestTee(all, errors, log);
	}

	public void testTee2() {
		BufferingI18NLog all = new BufferingI18NLog();
		BufferingI18NLog errors = new BufferingI18NLog();
		I18NLog log = all.tee(errors.filter(Level.ERROR));
		doTestTee(all, errors, log);
	}

	private void doTestTee(BufferingI18NLog all, BufferingI18NLog errors, I18NLog log) {
		ResKey message1 = ResKey.text("message1");
		log.info(message1);
		ResKey message2 = ResKey.text("message2");
		log.error(message2);

		List<Entry> errorEntries = errors.getEntries();
		assertEquals(1, errorEntries.size());
		Entry entry = errorEntries.get(0);
		assertEquals(Level.ERROR, entry.getLevel());
		assertEquals(message2, entry.getMessage());

		List<Entry> allEntries = all.getEntries();
		assertEquals(2, allEntries.size());
		assertEquals(Level.INFO, allEntries.get(0).getLevel());
		assertEquals(message1, allEntries.get(0).getMessage());
		assertEquals(Level.ERROR, allEntries.get(1).getLevel());
		assertEquals(message2, allEntries.get(1).getMessage());
	}

	public void testAsLogDebug() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		Log log = buffer.asLog();
		log.info("message", Log.DEBUG);
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.DEBUG, entries.get(0).getLevel());
		assertEquals(ResKey.text("message"), entries.get(0).getMessage());
	}

	public void testAsLogInfo() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		Log log = buffer.asLog();
		log.info("message");
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.INFO, entries.get(0).getLevel());
		assertEquals(ResKey.text("message"), entries.get(0).getMessage());
	}

	public void testAsLogWarn() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		Log log = buffer.asLog();
		log.info("message", Log.WARN);
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.WARN, entries.get(0).getLevel());
		assertEquals(ResKey.text("message"), entries.get(0).getMessage());
	}

	public void testAsLogError() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		Log log = buffer.asLog();
		log.error("message");
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.ERROR, entries.get(0).getLevel());
		assertEquals(ResKey.text("message"), entries.get(0).getMessage());
	}

	public void testAsLogWithException() {
		BufferingI18NLog buffer = new BufferingI18NLog();
		Log log = buffer.asLog();
		log.error("message", new StackTrace());
		List<Entry> entries = buffer.getEntries();
		assertEquals(1, entries.size());
		assertEquals(Level.ERROR, entries.get(0).getLevel());
		assertEquals(ResKey.text("message"), entries.get(0).getMessage());
	}

}
