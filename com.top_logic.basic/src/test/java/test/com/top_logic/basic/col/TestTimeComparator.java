/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.basic.col;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.TimeComparator;
import com.top_logic.basic.col.TimeComparator.SystemTimeComparator;
import com.top_logic.basic.col.TimeComparator.UserTimeComparator;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Test for {@link TimeComparator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTimeComparator extends BasicTestCase {

	public void testTrivial() {
		testTrivial(CalendarUtil.createCalendarInUserTimeZone(), UserTimeComparator.INSTANCE);
		testTrivial(CalendarUtil.createCalendar(), SystemTimeComparator.INSTANCE);
	}

	private void testTrivial(Calendar cal, TimeComparator comparator) {
		cal.set(2020, Calendar.JANUARY, 1, 2, 12, 3);
		Date d = cal.getTime();
		Date d1 = cal.getTime();
		assertTrue("Different instances", d1 != d);
		assertTrue(comparator.compare(null, d) > 0);
		assertTrue(comparator.compare(d, null) < 0);
		assertTrue(comparator.compare(d, d) == 0);
		assertTrue(comparator.compare(null, null) == 0);
		assertTrue(comparator.compare(d1, d) == 0);
	}

	public void testCompare() {
		testCompare(CalendarUtil.createCalendarInUserTimeZone(), UserTimeComparator.INSTANCE);
		testCompare(CalendarUtil.createCalendar(), SystemTimeComparator.INSTANCE);
	}

	private void testCompare(Calendar cal, TimeComparator comparator) {
		cal.set(2020, Calendar.JANUARY, 1, 2, 12, 3);
		Date d1 = cal.getTime();
		cal.set(2000, Calendar.JANUARY, 1, 14, 12, 3);
		Date d2 = cal.getTime();
		assertTrue(d1.compareTo(d2) > 0);
		assertTrue(comparator.compare(d1, d2) < 0);

		cal.set(2002, Calendar.DECEMBER, 1, 14, 12, 3);
		Date d3 = cal.getTime();

		assertTrue(d3.compareTo(d2) > 0);
		assertTrue(comparator.compare(d2, d3) == 0);
	}

	public void testDifferentUserTimeZone() {
		Calendar berlinCal = CalendarUtil.createCalendar(getTimeZoneBerlin());
		berlinCal.set(2015, Calendar.JANUARY, 5, 3, 12, 1);
		Date d1 = berlinCal.getTime();
		berlinCal.set(2015, Calendar.JANUARY, 5, 9, 12, 1);
		Date d2 = berlinCal.getTime();

		executeInTimeZone(getTimeZoneBerlin(), () -> {
			assertTrue("'03:12:01' is smaller than '09:12:01'.", UserTimeComparator.INSTANCE.compare(d1, d2) < 0);
			return null;
		});
		executeInTimeZone(getTimeZoneLosAngeles(), () -> {
			assertTrue("'18:12:01' is greater than '00:12:01'.", UserTimeComparator.INSTANCE.compare(d1, d2) > 0);
			return null;
		});
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestTimeComparator}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestTimeComparator.class);
	}

}
