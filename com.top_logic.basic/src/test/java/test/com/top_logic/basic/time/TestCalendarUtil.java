/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.time;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;

/**
 * Test class for {@link CalendarUtil}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCalendarUtil extends TestCase {
	
	public void testCreateCalendar() {
		Date d = new Date();
		assertEquals(d.getTime(), CalendarUtil.createCalendar(d.getTime()).getTimeInMillis());
		assertEquals(d, CalendarUtil.createCalendar(d).getTime());
	}
	
	public void testClone() {
		Date d = new Date();
		Calendar cal = Calendar.getInstance();
		assertEquals(cal, CalendarUtil.clone(cal));
		cal.setTime(d);
		assertEquals(cal, CalendarUtil.clone(cal));
	}

	public void testConvertTimeZone() {
		Calendar calendar = Calendar.getInstance(BasicTestCase.getTimeZoneBerlin());
		// daylight saving time
		calendar.set(2018, Calendar.AUGUST, 3, 12, 15, 12);
		assertSameFields(calendar, CalendarUtil.convertToTimeZone(calendar, BasicTestCase.getTimeZoneLosAngeles()));
		assertSameFields(calendar, CalendarUtil.convertToTimeZone(calendar, TimeZones.UTC));
		// normal time
		calendar.set(2018, Calendar.JANUARY, 3, 12, 15, 12);
		assertSameFields(calendar, CalendarUtil.convertToTimeZone(calendar, BasicTestCase.getTimeZoneLosAngeles()));
		assertSameFields(calendar, CalendarUtil.convertToTimeZone(calendar, TimeZones.UTC));

		calendar = Calendar.getInstance(TimeZones.UTC);
		// daylight saving time
		calendar.set(2018, Calendar.AUGUST, 3, 12, 15, 12);
		assertSameFields(calendar, CalendarUtil.convertToTimeZone(calendar, BasicTestCase.getTimeZoneBerlin()));
		// normal time
		calendar.set(2018, Calendar.JANUARY, 3, 12, 15, 12);
		assertSameFields(calendar, CalendarUtil.convertToTimeZone(calendar, BasicTestCase.getTimeZoneBerlin()));

		calendar = Calendar.getInstance(TimeZones.UTC);
		calendar.set(2018, Calendar.MARCH, 25, 02, 30, 0);
		Calendar target = Calendar.getInstance(BasicTestCase.getTimeZoneBerlin());
		CalendarUtil.convertToTargetCalendar(calendar, target);
		assertSame(3, target.get(Calendar.HOUR_OF_DAY));

		target = Calendar.getInstance(BasicTestCase.getTimeZoneBerlin());
		target.setLenient(false);
		try {
			CalendarUtil.convertToTargetCalendar(calendar, target);
			fail("25.03.2018 02:30 does not exist in Berlin, because at 02:00 the daylight saving time starts.");
		} catch (IllegalArgumentException ex) {
			// expected.
		}

	}

	private static void assertSameFields(Calendar expected, Calendar actual) {
		assertEquals(expected, actual, Calendar.ERA);
		assertEquals(expected, actual, Calendar.YEAR);
		assertEquals(expected, actual, Calendar.MONTH);
		assertEquals(expected, actual, Calendar.WEEK_OF_YEAR);
		assertEquals(expected, actual, Calendar.WEEK_OF_MONTH);
		assertEquals(expected, actual, Calendar.DAY_OF_MONTH);
		assertEquals(expected, actual, Calendar.DAY_OF_YEAR);
		assertEquals(expected, actual, Calendar.DAY_OF_WEEK);
		assertEquals(expected, actual, Calendar.DAY_OF_WEEK_IN_MONTH);
		assertEquals(expected, actual, Calendar.AM_PM);
		assertEquals(expected, actual, Calendar.HOUR);
		assertEquals(expected, actual, Calendar.HOUR_OF_DAY);
		assertEquals(expected, actual, Calendar.MINUTE);
		assertEquals(expected, actual, Calendar.SECOND);
		assertEquals(expected, actual, Calendar.MILLISECOND);

	}

	private static void assertEquals(Calendar expected, Calendar actual, int field) {
		assertEquals(expected.get(field), actual.get(field));
	}

}

