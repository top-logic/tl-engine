/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.thread.ThreadContext;

/**
 * Utilities for working with {@link Calendar} objects.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class CalendarUtil {

	/**
	 * Create a {@link Calendar} for the given time using {@link Calendar#setTimeInMillis(long)},
	 * given {@link TimeZone}, and default {@link Locale}.
	 */
	public static Calendar createCalendar(long timeInMillis, TimeZone timeZone) {
		return createCalendar(timeInMillis, timeZone, ThreadContext.getLocale());
	}

	/**
	 * Create a {@link Calendar} for the given time using {@link Calendar#setTimeInMillis(long)},
	 * default {@link TimeZone}, and given {@link Locale}.
	 */
	public static Calendar createCalendar(long timeInMillis, Locale locale) {
		return createCalendar(timeInMillis, TimeZones.systemTimeZone(), locale);
	}

	/**
	 * Create a {@link Calendar} for the given time using {@link Calendar#setTimeInMillis(long)},
	 * given {@link TimeZone}, and given {@link Locale}.
	 */
	public static Calendar createCalendar(long timeInMillis, TimeZone timeZone, Locale locale) {
		Calendar calendar = createCalendar(timeZone, locale);
		calendar.setTimeInMillis(timeInMillis);
		return calendar;
	}

	/**
	 * Create a {@link Calendar} for the given time using {@link Calendar#setTimeInMillis(long)},
	 * default {@link TimeZone}, and default {@link Locale}.
	 */
	public static Calendar createCalendar(long timeInMillis) {
		return createCalendar(timeInMillis, TimeZones.systemTimeZone(), ThreadContext.getLocale());
	}

	/**
	 * Create a {@link Calendar} for the given date, given {@link TimeZone}, and given
	 * {@link Locale}.
	 */
	public static Calendar createCalendar(Date date, TimeZone timeZone, Locale locale) {
		return createCalendar(date.getTime(), timeZone, locale);
	}

	/**
	 * Create a {@link Calendar} for the given date, given {@link TimeZone}, and default
	 * {@link Locale}.
	 */
	public static Calendar createCalendar(Date date, TimeZone timeZone) {
		return createCalendar(date.getTime(), timeZone);
	}

	/**
	 * Create a {@link Calendar} for the given date, default {@link TimeZone}, and given
	 * {@link Locale}.
	 */
	public static Calendar createCalendar(Date date, Locale locale) {
		return createCalendar(date.getTime(), locale);
	}

	/**
	 * Create a {@link Calendar} for the given date, default {@link TimeZone}, and default
	 * {@link Locale}.
	 */
	public static Calendar createCalendar(Date date) {
		return createCalendar(date.getTime());
	}

	/**
	 * Create a {@link Calendar} with default {@link TimeZone} and default {@link Locale}.
	 */
	public static Calendar createCalendar() {
		return createCalendar(TimeZones.systemTimeZone());
	}

	/**
	 * Create a {@link Calendar} in the user's time zone.
	 * 
	 * @see ThreadContext#getTimeZone()
	 * @see #createCalendar(TimeZone)
	 */
	public static Calendar createCalendarInUserTimeZone() {
		return createCalendar(ThreadContext.getTimeZone());
	}

	/**
	 * Create a {@link Calendar} with given {@link TimeZone} and default {@link Locale}.
	 */
	public static Calendar createCalendar(TimeZone timeZone) {
		return createCalendar(timeZone, ThreadContext.getLocale());
	}

	/**
	 * Create a {@link Calendar} in the user's time zone with the given locale.
	 * 
	 * @see ThreadContext#getTimeZone()
	 * @see #createCalendar(TimeZone)
	 */
	public static Calendar createCalendarInUserTimeZone(Locale locale) {
		return createCalendar(ThreadContext.getTimeZone(), locale);
	}

	/**
	 * Create a {@link Calendar} with default {@link TimeZone} and given {@link Locale}.
	 */
	public static Calendar createCalendar(Locale locale) {
		return createCalendar(TimeZones.systemTimeZone(), locale);
	}

	/**
	 * Create a {@link Calendar} with given {@link TimeZone} and given {@link Locale}.
	 */
	public static Calendar createCalendar(TimeZone timeZone, Locale locale) {
		return Calendar.getInstance(timeZone, locale);
	}

	/** Util: Clone the given {@link Calendar}. */
	public static Calendar clone(Calendar original) {
		return (Calendar) original.clone();
	}

	/**
	 * Creates a {@link SimpleDateFormat} with the given pattern and {@link TimeZones#systemTimeZone()}.
	 * 
	 * @see SimpleDateFormat#SimpleDateFormat(String)
	 */
	public static SimpleDateFormat newSimpleDateFormat(String pattern) {
		return newSimpleDateFormat(pattern, ThreadContext.getLocale());
	}

	/**
	 * Creates a {@link SimpleDateFormat} with the given pattern and {@link TimeZones#systemTimeZone()}.
	 * 
	 * @see SimpleDateFormat#SimpleDateFormat(String, Locale)
	 */
	public static SimpleDateFormat newSimpleDateFormat(String pattern, Locale locale) {
		return setSystemTime(new SimpleDateFormat(pattern, locale));
	}

	private static <T extends DateFormat> T setSystemTime(T dateFormat) {
		dateFormat.setTimeZone(TimeZones.systemTimeZone());
		return dateFormat;
	}

	/**
	 * Converts the given {@link Calendar} to {@link TimeZones#systemTimeZone()}.
	 */
	public static Calendar convertToSystemZone(Calendar source) {
		return convertToTimeZone(source, TimeZones.systemTimeZone());
	}

	/**
	 * Creates a {@link Calendar} with the given {@link TimeZone} which has the same fields
	 * determined from the {@link Calendar#getTime()}.
	 *
	 * @see CalendarUtil#convertToTargetCalendar(Calendar, Calendar)
	 */
	public static Calendar convertToTimeZone(Calendar source, TimeZone zone) {
		/* Clone calendar to have correct Locale. */
		Calendar target = CalendarUtil.clone(source);
		target.setTimeZone(zone);
		convertToTargetCalendar(source, target);
		return target;
	}

	/**
	 * Converts the target {@link Calendar} such that it has the same field values as the source
	 * {@link Calendar}.
	 * 
	 * <p>
	 * The target {@link Calendar} will have a different time than the source calendar in gerneral.
	 * All fields but {@link Calendar#ZONE_OFFSET} and {@link Calendar#DST_OFFSET} to access via
	 * {@link Calendar#get(int)} of the target {@link Calendar} will have the same value as the
	 * source {@link Calendar}.
	 * </p>
	 * 
	 * <p>
	 * In some rare cases the fields may differ, e.g. an UTC calendar at 25.03.2018 02:30 can not be
	 * displayed in {@link Calendar} with {@link TimeZone} Berlin, because this is the transition
	 * between normal time and daylight saving time. In that case it depends on the target calendar,
	 * whether it rolls the fields or throws an exception.
	 * </p>
	 * 
	 * @see Calendar#ERA
	 * @see Calendar#YEAR
	 * @see Calendar#MONTH
	 * @see Calendar#WEEK_OF_YEAR
	 * @see Calendar#WEEK_OF_MONTH
	 * @see Calendar#DATE
	 * @see Calendar#DAY_OF_MONTH
	 * @see Calendar#DAY_OF_YEAR
	 * @see Calendar#DAY_OF_WEEK
	 * @see Calendar#DAY_OF_WEEK_IN_MONTH
	 * @see Calendar#AM_PM
	 * @see Calendar#HOUR
	 * @see Calendar#HOUR_OF_DAY
	 * @see Calendar#MINUTE
	 * @see Calendar#SECOND
	 * @see Calendar#MILLISECOND
	 */
	public static void convertToTargetCalendar(Calendar source, Calendar target) {
		target.clear();
		copyFieldValue(source, target, Calendar.YEAR);
		copyFieldValue(source, target, Calendar.DAY_OF_YEAR);
		copyFieldValue(source, target, Calendar.HOUR_OF_DAY);
		copyFieldValue(source, target, Calendar.MINUTE);
		copyFieldValue(source, target, Calendar.SECOND);
		copyFieldValue(source, target, Calendar.MILLISECOND);
		// Fetch time to ensure computation of consistency of fields.
		target.getTimeInMillis();
	}

	/**
	 * Copies the given {@link Calendar} field from soure to target calendar.
	 *
	 * @param source
	 *        The {@link Calendar} to take the value from.
	 * @param target
	 *        The {@link Calendar} to modify.
	 * @param field
	 *        The field, e.g. {@link Calendar#YEAR}.
	 */
	public static void copyFieldValue(Calendar source, Calendar target, int field) {
		target.set(field, source.get(field));
	}

	/**
	 * Calls {@link DateFormat#getDateInstance()} and sets the system time zone.
	 */
	public static DateFormat getDateInstance() {
		return setSystemTime(DateFormat.getDateInstance());
	}

	/**
	 * Calls {@link DateFormat#getDateInstance(int)} and sets the system time zone.
	 */
	public static DateFormat getDateInstance(int style) {
		return setSystemTime(DateFormat.getDateInstance(style));
	}

	/**
	 * Calls {@link DateFormat#getDateInstance(int, Locale)} and sets the system time zone.
	 */
	public static DateFormat getDateInstance(int style, Locale aLocale) {
		return setSystemTime(DateFormat.getDateInstance(style, aLocale));
	}

	/**
	 * Calls {@link DateFormat#getDateTimeInstance()} and sets the system time zone.
	 */
	public static DateFormat getDateTimeInstance() {
		return setSystemTime(DateFormat.getDateTimeInstance());
	}

	/**
	 * Calls {@link DateFormat#getDateTimeInstance(int, int)} and sets the system time zone.
	 */
	public static DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
		return setSystemTime(DateFormat.getDateTimeInstance(dateStyle, timeStyle));
	}

	/**
	 * Calls {@link DateFormat#getDateTimeInstance(int, int, Locale)} and sets the system time zone.
	 */
	public static DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
		return setSystemTime(DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale));
	}

	/**
	 * Calls {@link DateFormat#getTimeInstance()} and sets the system time zone.
	 */
	public static DateFormat getTimeInstance() {
		return setSystemTime(DateFormat.getTimeInstance());
	}

	/**
	 * Calls {@link DateFormat#getTimeInstance(int)} and sets the system time zone.
	 */
	public static DateFormat getTimeInstance(int style) {
		return setSystemTime(DateFormat.getTimeInstance(style));
	}

	/**
	 * Calls {@link DateFormat#getTimeInstance(int, Locale)} and sets the system time zone.
	 */
	public static DateFormat getTimeInstance(int style, Locale aLocale) {
		return setSystemTime(DateFormat.getTimeInstance(style, aLocale));
	}

}
