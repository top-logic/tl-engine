/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.util.error.TopLogicException;

/**
 * Counts points in time per period - hour, day, week, month or year - of a time range, e.g. the
 * logins of the users per day.
 */
public class PeriodCounts {

	/** Key of the start of a period in the result of {@link #count}. */
	public static final String START = "start";

	/** Key of the number of points in time in a period in the result of {@link #count}. */
	public static final String COUNT = "count";

	/** Period of an hour. */
	public static final String HOUR = "hour";

	/** Period of a day. */
	public static final String DAY = "day";

	/** Period of a week, starting with the first day of the week of the calendar's locale. */
	public static final String WEEK = "week";

	/** Period of a month. */
	public static final String MONTH = "month";

	/** Period of a year. */
	public static final String YEAR = "year";

	/** Maximum number of periods {@link #count} reports. */
	public static final int MAX_PERIODS = 5000;

	/**
	 * The number of the given points in time in each period of a time range.
	 *
	 * @param times
	 *        The points in time to count. Those outside the range are not counted.
	 * @param from
	 *        The start of the range (inclusive), <code>null</code> to start with the first of the
	 *        given points in time.
	 * @param to
	 *        The end of the range (exclusive).
	 * @param period
	 *        The length of a period: {@value #HOUR}, {@value #DAY}, {@value #WEEK}, {@value #MONTH}
	 *        or {@value #YEAR}; <code>null</code> for {@value #DAY}.
	 * @param calendar
	 *        The calendar whose time zone and locale define the periods.
	 * @return One dictionary per period in their order in time, from the one containing the start
	 *         of the range to the one containing its end, with the start of the period under
	 *         {@value #START} and the number of points in time in it under {@value #COUNT}. Empty
	 *         if the range has no start and no point in time is given.
	 * @throws TopLogicException
	 *         If the period is unknown or the range has more than {@link #MAX_PERIODS} periods.
	 */
	public static List<Map<String, Object>> count(Collection<Date> times, Date from, Date to, String period,
			Calendar calendar) {
		int field = periodField(period);
		Date first = from;
		Map<Date, Integer> counts = new HashMap<>();
		for (Date time : times) {
			if (from != null && time.before(from) || !time.before(to)) {
				continue;
			}
			if (first == null || time.before(first)) {
				first = time;
			}
			counts.merge(periodStart(calendar, time, field), 1, Integer::sum);
		}
		List<Map<String, Object>> result = new ArrayList<>();
		if (first == null) {
			return result;
		}
		calendar.setTime(periodStart(calendar, first, field));
		while (calendar.getTime().before(to)) {
			if (result.size() == MAX_PERIODS) {
				throw new TopLogicException(I18NConstants.ERROR_TOO_MANY_PERIODS__MAX.fill(MAX_PERIODS));
			}
			Date start = calendar.getTime();
			result.add(Map.of(START, start, COUNT, counts.getOrDefault(start, 0)));
			calendar.add(field, 1);
		}
		return result;
	}

	private static int periodField(String period) {
		if (period == null) {
			return Calendar.DAY_OF_MONTH;
		}
		switch (period) {
			case HOUR:
				return Calendar.HOUR_OF_DAY;
			case DAY:
				return Calendar.DAY_OF_MONTH;
			case WEEK:
				return Calendar.WEEK_OF_YEAR;
			case MONTH:
				return Calendar.MONTH;
			case YEAR:
				return Calendar.YEAR;
			default:
				throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_PERIOD__PERIOD.fill(period));
		}
	}

	/**
	 * The start of the period of the given calendar field containing the given point in time.
	 */
	private static Date periodStart(Calendar calendar, Date time, int field) {
		calendar.setTime(time);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		if (field == Calendar.HOUR_OF_DAY) {
			return calendar.getTime();
		}
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		switch (field) {
			case Calendar.WEEK_OF_YEAR:
				calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
				break;
			case Calendar.MONTH:
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				break;
			case Calendar.YEAR:
				calendar.set(Calendar.DAY_OF_YEAR, 1);
				break;
			default:
				break;
		}
		return calendar.getTime();
	}

}
