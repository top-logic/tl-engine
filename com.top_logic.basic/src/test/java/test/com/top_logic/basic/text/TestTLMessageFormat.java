/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.text;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.text.TLMessageFormat;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.Computation;

/**
 * Test for {@link TLMessageFormat}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTLMessageFormat extends BasicTestCase {

	public void testCustomPattern() {
		String shortDateStyle = Formatter.SHORT_DATE_STYLE;
		Date date = new Date();
		TLMessageFormat format = new TLMessageFormat("Datum: {0," + shortDateStyle + "}", Locale.GERMAN);
		String expectedFormatted = FormatterService.getFormatter(Locale.GERMAN).getFormat(shortDateStyle).format(date);
		assertEquals("Datum: " + expectedFormatted, format.format(new Object[] { date }));
	}

	public void testTimePattern() {
		testTimeInUserTimeZone(TimeZones.UTC);
		testTimeInUserTimeZone(getTimeZoneBerlin());
		testTimeInUserTimeZone(getTimeZoneAuckland());
	}

	private void testTimeInUserTimeZone(TimeZone timeZone) {
		ThreadContext.getThreadContext().setCurrentTimeZone(timeZone);
		Date date = new Date();
		DateFormat timeInstance = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.GERMAN);
		timeInstance.setTimeZone(timeZone);
		TLMessageFormat format = new TLMessageFormat("{0,time}", Locale.GERMAN);
		assertEquals("TLMessageFormat formats time in user time zone.", timeInstance.format(date),
			format.format(new Object[] { date }));
	}

	public void testDatePattern() {
		testDateInSystemTimeZone(TimeZones.UTC);
		testDateInSystemTimeZone(getTimeZoneBerlin());
		testDateInSystemTimeZone(getTimeZoneAuckland());
	}

	private void testDateInSystemTimeZone(TimeZone timeZone) {
		BasicTestCase.executeInSystemTimeZone(timeZone, new Computation<Void>() {

			@Override
			public Void run() {
				Date date = new Date();
				DateFormat timeInstance = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.GERMAN);
				timeInstance.setTimeZone(timeZone);
				TLMessageFormat format = new TLMessageFormat("{0,date}", Locale.GERMAN);
				assertEquals("TLMessageFormat formats date in system time zone.", timeInstance.format(date),
					format.format(new Object[] { date }));
				return null;
			}

		});
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTLMessageFormat}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestTLMessageFormat.class);
		test = ServiceTestSetup.createSetup(null, test, FormatterService.Module.INSTANCE, TimeZones.Module.INSTANCE);
		return BasicTestSetup.createBasicTestSetup(test);
	}

}

