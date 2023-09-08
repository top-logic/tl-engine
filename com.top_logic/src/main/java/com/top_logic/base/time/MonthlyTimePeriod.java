/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Time period constructed of monthly intervals.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MonthlyTimePeriod extends TimePeriod {

	private final int yearBeginMonth;

	private final int yearBeginDay;

	private final int intervalLength;

	private Calendar _calendar;

	/**
	 * Create new {@link MonthlyTimePeriod}
	 * 
	 * @param aDate
	 *        a date
	 * @param yearBeginMonth
	 *        the month of the first interval's start date
	 * @param yearBeginDay
	 *        the day of the first interval's start date
	 * @param intervalLength
	 *        the length in month of one interval
	 */
	public MonthlyTimePeriod(Calendar aDate, int yearBeginMonth, int yearBeginDay, int intervalLength) {
		_calendar = CalendarUtil.clone(aDate);
		this.yearBeginMonth = yearBeginMonth;
		this.yearBeginDay = yearBeginDay;
		if (intervalLength < 1 || intervalLength > 12) {
			throw new IllegalArgumentException("Length of interval must in between 1 and 12!");
		}
		this.intervalLength = intervalLength;
		this.adjustBeginEnd(_calendar.getTime());
	}

	@Override
	protected void adjustBeginEnd(Date aDate) {
		// The day we want the interval for
		_calendar.setTime(aDate);
		Calendar referenceDate = CalendarUtil.clone(_calendar); // same year

		DateUtil.adjustToDayBegin(_calendar);

		// The turn around point/start day of the interval in the same year of the day
		referenceDate.set(Calendar.MONTH, this.yearBeginMonth);

		// The 28/29/30/31 days per month thing
		int maxReferenceDay = Math.min(this.yearBeginDay, referenceDate.getActualMaximum(Calendar.DAY_OF_MONTH));
		referenceDate.set(Calendar.DAY_OF_MONTH, maxReferenceDay);
		DateUtil.adjustToDayBegin(referenceDate);

		// Three cases:

		// cal is 0 times afar from start (=in the same month),
		// then interval is [start, start+length]

		// cal is n>0 times in the future from start
		// then interval is [start + n*length, start + (n+1)*length);

		// cal is n<0 times in the past from start
		// then interval is [start - n*length, start - (n-1)*length)

		int currentMonth = _calendar.get(Calendar.MONTH);

		// The 28/29/30/31 days per month thing
		// If the asked day is before the configured first day, then the resulting interval is
		// the interval that intersects the month before.
		int maxDay = Math.min(this.yearBeginDay, _calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		if (maxDay > _calendar.get(Calendar.DAY_OF_MONTH)) {
			currentMonth--;
		}
		int monthDiff = currentMonth - this.yearBeginMonth;
		double ntimes = (monthDiff * 1d) / (intervalLength * 1d);

		if (ntimes > 0) {
			referenceDate.add(Calendar.MONTH, (int) (Math.floor(ntimes)) * intervalLength);
			this.begin = referenceDate.getTime();
		} else if (ntimes < 0) {
			referenceDate.add(Calendar.MONTH, (int) (Math.floor(ntimes)) * intervalLength);
			this.begin = referenceDate.getTime();
		} else {
			this.begin = referenceDate.getTime();
		}

		referenceDate.add(Calendar.MONTH, intervalLength);
		// The 28/29/30/31 days per month thing
		referenceDate.set(Calendar.DAY_OF_MONTH, Math.min(this.yearBeginDay, referenceDate
			.getActualMaximum(Calendar.DAY_OF_MONTH)));
		referenceDate.add(Calendar.DAY_OF_MONTH, -1);
		this.end = DateUtil.adjustToDayEnd(referenceDate);

	}

	@Override
	public TimePeriod getNextPeriod() {
		Calendar nextPeriodCalendar = CalendarUtil.clone(_calendar);
		nextPeriodCalendar.setTime(this.end);
		DateUtil.addDays(nextPeriodCalendar, this.end, +1);
		return new MonthlyTimePeriod(nextPeriodCalendar, this.yearBeginMonth, this.yearBeginDay, this.intervalLength);
	}

	@Override
	public String toString() {
		return this.begin + " - " + this.end;
	}

}
