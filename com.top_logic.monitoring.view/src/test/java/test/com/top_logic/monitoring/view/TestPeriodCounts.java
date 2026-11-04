/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.monitoring.view;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.top_logic.monitoring.view.PeriodCounts;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link PeriodCounts}.
 */
@SuppressWarnings("javadoc")
public class TestPeriodCounts extends TestCase {

	private static final TimeZone ZONE = TimeZone.getTimeZone("Europe/Berlin");

	public void testDays() {
		Date from = date(2026, Calendar.MARCH, 27, 0, 0);
		Date to = date(2026, Calendar.MARCH, 31, 0, 0);
		List<Map<String, Object>> periods = PeriodCounts.count(List.of(
			date(2026, Calendar.MARCH, 26, 23, 59), // before the range
			date(2026, Calendar.MARCH, 27, 0, 0),
			date(2026, Calendar.MARCH, 27, 23, 59),
			// The switch to daylight saving time is in the night to March 29.
			date(2026, Calendar.MARCH, 29, 12, 0),
			date(2026, Calendar.MARCH, 31, 0, 0)), // the end is exclusive
			from, to, PeriodCounts.DAY, calendar());

		assertEquals(4, periods.size());
		assertPeriod(periods.get(0), date(2026, Calendar.MARCH, 27, 0, 0), 2);
		assertPeriod(periods.get(1), date(2026, Calendar.MARCH, 28, 0, 0), 0);
		assertPeriod(periods.get(2), date(2026, Calendar.MARCH, 29, 0, 0), 1);
		assertPeriod(periods.get(3), date(2026, Calendar.MARCH, 30, 0, 0), 0);
	}

	public void testHoursFromFirstTime() {
		Date to = date(2026, Calendar.MAY, 4, 12, 30);
		List<Map<String, Object>> periods = PeriodCounts.count(List.of(
			date(2026, Calendar.MAY, 4, 10, 15),
			date(2026, Calendar.MAY, 4, 10, 45),
			date(2026, Calendar.MAY, 4, 12, 0)),
			null, to, PeriodCounts.HOUR, calendar());

		assertEquals(3, periods.size());
		assertPeriod(periods.get(0), date(2026, Calendar.MAY, 4, 10, 0), 2);
		assertPeriod(periods.get(1), date(2026, Calendar.MAY, 4, 11, 0), 0);
		assertPeriod(periods.get(2), date(2026, Calendar.MAY, 4, 12, 0), 1);
	}

	public void testWeeksMonthsYears() {
		// 2026-05-06 is a Wednesday; weeks start on Monday in the German locale.
		List<Map<String, Object>> weeks = PeriodCounts.count(List.of(date(2026, Calendar.MAY, 6, 8, 0)),
			date(2026, Calendar.MAY, 5, 0, 0), date(2026, Calendar.MAY, 12, 0, 0), PeriodCounts.WEEK, calendar());
		assertEquals(2, weeks.size());
		assertPeriod(weeks.get(0), date(2026, Calendar.MAY, 4, 0, 0), 1);
		assertPeriod(weeks.get(1), date(2026, Calendar.MAY, 11, 0, 0), 0);

		List<Map<String, Object>> months = PeriodCounts.count(List.of(date(2026, Calendar.FEBRUARY, 28, 8, 0)),
			date(2026, Calendar.JANUARY, 31, 0, 0), date(2026, Calendar.MARCH, 1, 0, 0), PeriodCounts.MONTH,
			calendar());
		assertEquals(2, months.size());
		assertPeriod(months.get(0), date(2026, Calendar.JANUARY, 1, 0, 0), 0);
		assertPeriod(months.get(1), date(2026, Calendar.FEBRUARY, 1, 0, 0), 1);

		List<Map<String, Object>> years = PeriodCounts.count(List.of(), date(2025, Calendar.JUNE, 1, 0, 0),
			date(2026, Calendar.JUNE, 1, 0, 0), PeriodCounts.YEAR, calendar());
		assertEquals(2, years.size());
		assertPeriod(years.get(0), date(2025, Calendar.JANUARY, 1, 0, 0), 0);
		assertPeriod(years.get(1), date(2026, Calendar.JANUARY, 1, 0, 0), 0);
	}

	public void testNothingWithoutStart() {
		assertEquals(List.of(),
			PeriodCounts.count(List.of(), null, date(2026, Calendar.MAY, 4, 0, 0), PeriodCounts.DAY, calendar()));
	}

	public void testTooManyPeriods() {
		try {
			PeriodCounts.count(List.of(), date(2000, Calendar.JANUARY, 1, 0, 0), date(2026, Calendar.JANUARY, 1, 0, 0),
				PeriodCounts.DAY, calendar());
			fail("More than " + PeriodCounts.MAX_PERIODS + " periods.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	public void testUnknownPeriod() {
		try {
			PeriodCounts.count(List.of(), date(2026, Calendar.JANUARY, 1, 0, 0), date(2026, Calendar.JANUARY, 2, 0, 0),
				"fortnight", calendar());
			fail("Unknown period.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private static void assertPeriod(Map<String, Object> period, Date start, int count) {
		assertEquals(start, period.get(PeriodCounts.START));
		assertEquals(count, period.get(PeriodCounts.COUNT));
	}

	private static Calendar calendar() {
		return Calendar.getInstance(ZONE, Locale.GERMANY);
	}

	private static Date date(int year, int month, int day, int hour, int minute) {
		Calendar calendar = calendar();
		calendar.clear();
		calendar.set(year, month, day, hour, minute);
		return calendar.getTime();
	}

}
