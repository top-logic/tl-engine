/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.basic.col;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import com.top_logic.basic.time.CalendarUtil;

/**
 * {@link Comparator} that compares {@link Date}s by their time aspect.
 * 
 * <p>
 * A {@link TimeComparator} ignores the date aspect of a {@link Date} when comparing. For example
 * the date "2024-02-28T16:00:00" is smaller than "2000-01-19T19:00:15" if it is compared with a
 * {@link TimeComparator}, whereas this is not the case if the objects are compared as dates.
 * </p>
 * 
 * <p>
 * <code>null</code> is larger than any date.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TimeComparator implements Comparator<Date> {

	@Override
	public int compare(Date o1, Date o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (o2 == null) {
				return -1;
			} else {
				Calendar c1 = createCalendar();
				Calendar c2 = CalendarUtil.clone(c1);
				// Set dates
				c1.setTime(o1);
				c2.setTime(o2);
				// Normalize year, month and date.
				c1.set(1970, Calendar.JANUARY, 1);
				c2.set(1970, Calendar.JANUARY, 1);
				return c1.compareTo(c2);
			}
		}
	}

	/**
	 * Creates the calendar that is used to compare the time aspect of the dates.
	 */
	protected abstract Calendar createCalendar();

	/**
	 * {@link TimeComparator} that compares the time aspects of dates in the users timezone.
	 * 
	 * @see SystemTimeComparator
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class UserTimeComparator extends TimeComparator {

		/** Singleton {@link UserTimeComparator} instance. */
		public static final UserTimeComparator INSTANCE = new UserTimeComparator();

		/**
		 * Creates a new {@link TimeComparator.UserTimeComparator}.
		 */
		protected UserTimeComparator() {
			// singleton instance
		}

		@Override
		protected Calendar createCalendar() {
			return CalendarUtil.createCalendarInUserTimeZone();
		}
	}

	/**
	 * {@link TimeComparator} that compares the time aspects of dates in the applications timezone.
	 * 
	 * @see UserTimeComparator
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SystemTimeComparator extends TimeComparator {

		/** Singleton {@link SystemTimeComparator} instance. */
		public static final SystemTimeComparator INSTANCE = new SystemTimeComparator();

		/**
		 * Creates a new {@link SystemTimeComparator}.
		 */
		protected SystemTimeComparator() {
			// singleton instance
		}

		@Override
		protected Calendar createCalendar() {
			return CalendarUtil.createCalendar();
		}
	}

}
