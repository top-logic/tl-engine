/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.Utils;

/**
 * Holds the configuration used for date related calculations
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class BusinessYearConfiguration {

	/**
	 * Configuration for {@link BusinessYearConfiguration}.
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * The month the business year starts with. Month of the business year, 0=January, 1=February,...,
		 * 11=December. See {@link Config#getBeginMonth}.
		 */
		String BEGIN_MONTH = "beginMonth";

		/**
		 * The day in month the business year starts with. Day of a month of the business year, 1-31. See
		 * {@link Config#getBeginDay}.
		 */
		String BEGIN_DAY = "beginDay";

		/** Getter for {@link Config#BEGIN_MONTH}. */
		@Name(BEGIN_MONTH)
		@IntDefault(Calendar.JANUARY)
		int getBeginMonth();

		/** Getter for {@link Config#BEGIN_DAY}. */
		@Name(BEGIN_DAY)
		@IntDefault(1)
		int getBeginDay();
	}

	private static Integer beginMonth;

	private static Integer beginDay;

	/**
	 * True, if the business year is not equal to the calendar year.
	 */
	public static boolean isConfigured() {
		return !Utils.equals(beginMonth, Calendar.JANUARY) || !Utils.equals(beginDay, 1);
	}

	/**
	 * Return the date of the first day 00:00:00 of the business year the given date belongs to.
	 */
	public static Calendar adjustToBeginDateForDate(Date date, Calendar cal) {
		int year = getYear(date, cal);
		return adjustToBeginDateForYear(year, cal);
	}

	/**
	 * Adjusts the given {@link Calendar} to the first day 00:00:00 of the business year for the
	 * given year.
	 * 
	 * @return The given {@link Calendar} for chaining.
	 */
	public static Calendar adjustToBeginDateForYear(int year, Calendar businessYear) {
		businessYear.set(Calendar.YEAR, year);
		businessYear.set(Calendar.MONTH, getBeginMonth());
		businessYear.set(Calendar.DAY_OF_MONTH, Math.min(businessYear.getActualMaximum(Calendar.DAY_OF_MONTH),
			getBeginDay()));
		DateUtil.adjustToDayBegin(businessYear);

		return businessYear;
	}

	/**
	 * Returns the quarter (0-3) of the business year the given date belongs to.
	 * 
	 * @param cal
	 *        The given {@link Calendar} is modified, e.g. to get correct
	 *        {@link Calendar#getTimeZone()}.
	 */
	public static int getQuarter(Date date, Calendar cal) {
		cal = adjustToBeginDateForDate(date, cal);

		TimePeriod p = new Quarter(cal, getBeginMonth(), getBeginDay());
		int q = 0;
		while (!p.contains(date) && q < 5) {
			p = p.getNextPeriod();
			q++;
		}
		return q;
	}

	/**
	 * Returns the year of the business year the given date belongs to.
	 * 
	 * @param cal
	 *        The given {@link Calendar} is modified, e.g. to get correct
	 *        {@link Calendar#getTimeZone()}.
	 */
	public static int getYear(Date date, Calendar cal) {
		cal.setTime(date);
		return getYear(cal);
	}

	/**
	 * Returns the year of the business year of the calendar.
	 */
	public static int getYear(Calendar cal) {
		int year = cal.get(Calendar.YEAR);
		if (cal.before(adjustToBeginDateForYear(year, CalendarUtil.clone(cal)))) {
			year--;
		}
		return year;
	}

	/**
	 * Getter for the configuration.
	 */
	public static Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Getter for {@link Config#BEGIN_MONTH}.
	 */
	public static int getBeginMonth() {
		if (beginMonth == null) {
			beginMonth = getConfig().getBeginMonth();
		}
		return beginMonth;
	}

	/**
	 * Getter for {@link Config#BEGIN_DAY}.
	 */
	public static int getBeginDay() {
		if (beginDay == null) {
			beginDay = getConfig().getBeginDay();
		}
		return beginDay;
	}
}
