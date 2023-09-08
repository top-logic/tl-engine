/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import static java.util.Calendar.*;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.time.CalendarUtil;

/**
 * This class provides utility functions for dates.
 * 
 * Please do <em>not</em> Use these functions for iterative/massive
 * manipulation of dates. Better (re-) use a {@link Calendar} yourself
 * and think about what your are doing.
 * 
 * @author <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 * @author <a href=mailto:fma@top-logic.com>fma</a>
 */
public abstract class DateUtil {

	/**
	 * Milliseconds of one second.
	 */
	public static final long SECOND_MILLIS = 1000;

	/**
	 * Milliseconds of one minute.
	 */
	public static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;

	/**
	 * Milliseconds of one hour.
	 */
	public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;

	/**
	 * Milliseconds of one day.
	 */
	public static final long DAY_MILLIS = 24 * HOUR_MILLIS;

	/**
	 * Number of milliseconds per day.<br/>
	 * Is incorrect for days with a leap second, but will work correctly here.
	 */
	public static final long MILLIS_PER_DAY = DAY_MILLIS;

	public static final int		EXCLUDE_FIRST_DATE	= 1;
	public static final int		EXCLUDE_SECOND_DATE	= 1;
	public static final int		EXCLUDE_BOTH_DATES	= 2;
	public static final int		EXCLUDE_NO_DATE		= 0;

	/**
	 * Creates a new date instance with the given year.
	 * 
	 * @param year
	 *            the year to set
	 * @return a Date instance of the begin of the given year.
	 */
	public static Date createYear(int year) {
		Calendar theCal = CalendarUtil.createCalendar();
		theCal.set(YEAR, year);
		return adjustDateToYearBegin(theCal);
	}

	/**
	 * Creates a new date instance with the given year.
	 * 
	 * @param year
	 *            the year to set
	 * @return a Date instance of the begin of the given year.
	 */
	public static Date createYear(String year) {
		try {
			return createYear(Integer.parseInt(year));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

    /**
	 * Creates a new {@link Date} instance with the given parameters.
	 * <p>
	 * Note that month index begins with 0! Use constants from {@link Calendar} instead of integer
	 * literals. See {@link Calendar#MONTH} for more information.
	 * </p>
	 */
	public static Date createDate(int year, int month, int day) {
		return createDate(year, month, day, 0, 0);
	}

	/**
	 * Creates a new {@link Date} instance with the given parameters.
	 * <p>
	 * Note that month index begins with 0! Use constants from {@link Calendar} instead of integer
	 * literals. See {@link Calendar#MONTH} for more information.
	 * </p>
	 */
	public static Date createDate(Calendar calendar, int year, int month, int day) {
		return createDate(calendar, year, month, day, 0, 0);
	}

	/**
	 * Creates a new {@link Date} instance with the given parameters.
	 * <p>
	 * Note that month index begins with 0! Use constants from {@link Calendar} instead of integer
	 * literals. See {@link Calendar#MONTH} for more information.
	 * </p>
	 */
	public static Date createDate(int year, int month, int day, int hour, int minute) {
		return createDate(year, month, day, hour, minute, 0);
	}

	/**
	 * Creates a new {@link Date} instance with the given parameters.
	 * <p>
	 * Note that month index begins with 0! Use constants from {@link Calendar} instead of integer
	 * literals. See {@link Calendar#MONTH} for more information.
	 * </p>
	 */
	public static Date createDate(Calendar calendar, int year, int month, int day, int hour, int minute) {
		return createDate(calendar, year, month, day, hour, minute, 0);
	}

	/**
	 * Creates a new {@link Date} instance with the given parameters.
	 * <p>
	 * Note that month index begins with 0! Use constants from {@link Calendar} instead of integer
	 * literals. See {@link Calendar#MONTH} for more information.
	 * </p>
	 */
	public static Date createDate(int year, int month, int day, int hour, int minute, int second) {
		return createDate(CalendarUtil.createCalendar(), year, month, day, hour, minute, second);
	}

    /**
	 * Creates a new {@link Date} instance with the given parameters.
	 * <p>
	 * Note that month index begins with 0! Use constants from {@link Calendar} instead of integer
	 * literals. See {@link Calendar#MONTH} for more information.
	 * </p>
	 */
    public static Date createDate(Calendar aCal, int year, int month, int day, int hour, int minute, int second) {
        aCal.set(year, month, day, hour, minute, second);
        aCal.set(MILLISECOND, 0);
        return aCal.getTime();
    }

    /**
     * Compares two dates exactly by milliseconds.
     * 
     * There is no real need for this function, use {@link Date#compareTo(Date)} directly. 
     *
     * @param date1
     *        the first date to compare; must not be <code>null</code>
     * @param date2
     *        the second date to compare; must not be <code>null</code>
     * @return -1, if date1 is before date2<br/>
     *         0, if date1 is equal to date2<br/>
     *         +1, if date1 is after date2
     */
    public static final int compareDates(Date date1, Date date2) {
        return date1.compareTo(date2);
    }

	/**
	 * Merge the time aspect of <code>time</code> and the date aspect of
	 * <code>date</code> into the returned {@link Date}. The given {@link Date}s
	 * are not modified.
	 * 
	 * Consider using the variant with {@link Calendar} when using this more
	 * than once.
	 */
    public static Date mergeDateTime(Date dateAspect, Date timeAspect) {
		return mergeDateTime(CalendarUtil.createCalendar(), dateAspect, timeAspect).getTime();
    }

	/**
	 * Merge the time aspect of <code>time</code> and the date aspect of
	 * <code>date</code> into aCal. The given {@link Date}s are not modified.
	 * 
	 * <b>Warning this will not be correct in case time uses a date value with
	 * year 0.</b>
	 * 
	 * @param cal
	 *        will be modified by the calculation will be returned.
	 * @return the given {@link Calendar}
	 */
    public static Calendar mergeDateTime(Calendar cal, Date dateAspect, Date timeAspect) {
        cal.setTime(timeAspect); // Fetch Time
        
        int hour   = cal.get(HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int millis = cal.get(Calendar.MILLISECOND);

        cal.setTime(dateAspect); // inject time into Date

        cal.set(Calendar.HOUR_OF_DAY , hour);
        cal.set(Calendar.MINUTE      , minute);
        cal.set(Calendar.SECOND      , second);
        cal.set(Calendar.MILLISECOND , millis);
        
        return cal;
    }
    

    /**
     * Compares two dates by day.<br/>
     *
     * @param date1
     *        the first date to compare; must not be <code>null</code>
     * @param date2
     *        the second date to compare; must not be <code>null</code>
     * @return a negative value, if date1 is a day or year before date2<br/>
     *         0, if date1 is the same day and year as date2<br/>
     *         a positive value, if date1 is a day or year after date2
     */
    public static int compareDatesByDay(Date date1, Date date2) {
        int yearDiff = compareDatesByYear(date1, date2);
        if (yearDiff < 0) return -1;
        if (yearDiff > 0) return +1;
		Calendar cal = CalendarUtil.createCalendar(date1);
        int day1 = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTime(date2);
        int day2 = cal.get(Calendar.DAY_OF_YEAR);
        return day1 - day2;
    }

    /**
     * Compares two dates by month.<br/>
     * The returned value is the month difference between the given dates.
     *
     * @param date1
     *        the first date to compare; must not be <code>null</code>
     * @param date2
     *        the second date to compare; must not be <code>null</code>
     * @return a negative value, if date1 is a month or year before date2<br/>
     *         0, if date1 is the same month and year as date2<br/>
     *         a positive value, if date1 is a month or year after date2
     */
    public static int compareDatesByMonth(Date date1, Date date2) {
		Calendar cal = CalendarUtil.createCalendar(date1);
        int month1 = cal.get(Calendar.MONTH);
        cal.setTime(date2);
        int month2 = cal.get(Calendar.MONTH);
        int monthDiff = month1 - month2;
        int yearDiff = compareDatesByYear(date1, date2);
        return (yearDiff * 12) + monthDiff;
    }

    /**
     * Compares two dates by year.<br/>
     * The returned value is the year difference between the given dates.
     *
     * @param date1
     *        the first date to compare; must not be <code>null</code>
     * @param date2
     *        the second date to compare; must not be <code>null</code>
     * @return a negative value, if date1 is a year before date2<br/>
     *         0, if date1 is the same year as date2<br/>
     *         a positive value, if date1 is a year after date2
     */
    public static int compareDatesByYear(Date date1, Date date2) {
		Calendar cal = CalendarUtil.createCalendar(date1);
        int year1 = cal.get(Calendar.YEAR);
        cal.setTime(date2);
        int year2 = cal.get(Calendar.YEAR);
        return year1 - year2;
    }



	/** 
	 * Return the given date as string in format "yyyyMMdd-HHmmss". 
	 * 
	 * @param    aDate    The date to be formatted, may be <code>null</code>.
	 * @return   The requested date or <code>null</code> if given date is null.
	 */
	public static String toSortableString(Date aDate) {
	    return (aDate == null) ? null : getFullDateFormat().format(aDate);
	}

	/**
	 * This method adjusts the given date to the year end 31.12.year 23:59:59:999
	 * 
	 * @param aCal Must not be <code>null</code>.
	 */
	public static Date adjustDateToYearEnd(Calendar aCal) {
	    aCal.set(Calendar.MONTH       , aCal.getActualMaximum(Calendar.MONTH));
	    aCal.set(Calendar.DAY_OF_MONTH, aCal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return adjustToDayEnd(aCal);
	}
	
	/**
	 * This method adjusts the given date to the year begin 1.1.year 0:0:0:0
	 * 
	 * @param aDate
	 *            A date. Must not be <code>null</code>.
	 */
	public static Date adjustDateToYearEnd(Calendar aCalendar, Date aDate) {
		aCalendar.setTime(aDate);
		return adjustDateToYearEnd(aCalendar);
	}


    /**
     * This method adjusts the given date to the year end 31.12.year 23:59:59:999
     * 
     * @param aDate
     *            A date. Must not be <code>null</code>.
     */
    public static Date adjustDateToYearEnd(Date aDate) {
		return adjustDateToYearEnd(CalendarUtil.createCalendar(aDate));
    }

	/**
	 * This method adjusts the given Calendar to the year begin 1.1.year 0:0:0:0
	 * 
	 * @param aCal
	 *        Must not be <code>null</code>.
	 */
    public static Date adjustDateToYearBegin(Calendar aCal) {
        aCal.set(Calendar.MONTH, Calendar.JANUARY);
        aCal.set(Calendar.DAY_OF_MONTH, 1);
        return adjustToDayBegin(aCal);
    }


    /**
	 * This method adjusts the given date to the year begin 1.1.year 0:0:0:0
	 * 
	 * @param aDate
	 *            A date. Must not be <code>null</code>.
	 */
	public static Date adjustDateToYearBegin(Date aDate) {
		return adjustDateToYearBegin(CalendarUtil.createCalendar(aDate));
	}
	
	/**
	 * This method adjusts the given date to the year begin 1.1.year 0:0:0:0
	 * 
	 * @param aDate
	 *            A date. Must not be <code>null</code>.
	 */
	public static Date adjustDateToYearBegin(Calendar aCalendar, Date aDate) {
		aCalendar.setTime(aDate);
		return adjustDateToYearBegin(aCalendar);
	}



    /**
     * This method adjusts the given Calendar to month begin (e.g. 01.03.2007 00:00:00:000).
     *
     * @param aCalendar
     *        Must not be <code>null</code>.
     */
    public static Date adjustDateToMonthBegin(Calendar aCalendar) {
        aCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return adjustToDayBegin(aCalendar);
    }

    /**
     * This method adjusts the given date to month begin (e.g. 01.03.2007 00:00:00:000).
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustDateToMonthBegin(Date aDate) {
		return adjustDateToMonthBegin(CalendarUtil.createCalendar(aDate));
    }

    /**
     * This method adjusts the given date to month begin (e.g. 01.03.2007 00:00:00:000).
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustDateToMonthBegin(Calendar aCalendar, Date aDate) {
        aCalendar.setTime(aDate);
        return adjustDateToMonthBegin(aCalendar);
    }

    /**
     * This method adjusts the given Calendar to month end (e.g. 31.03.2007 23:59:59:999).
     *
     * @param aCalendar
     *        Must not be <code>null</code>.
     */
    public static Date adjustDateToMonthEnd(Calendar aCalendar) {
        aCalendar.set(Calendar.DAY_OF_MONTH, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return adjustToDayEnd(aCalendar);
    }

    /**
     * This method adjusts the given date to month end (e.g. 31.03.2007 23:59:59:999).
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustDateToMonthEnd(Date aDate) {
		return adjustDateToMonthEnd(CalendarUtil.createCalendar(aDate));
    }

    /**
     * This method adjusts the given date to month end (e.g. 31.03.2007 23:59:59:999).
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustDateToMonthEnd(Calendar aCalendar, Date aDate) {
        aCalendar.setTime(aDate);
        return adjustDateToMonthEnd(aCalendar);
    }

    /**
     * This method adjusts the given date to Monday of the week
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustToWeekBegin(Date aDate) {
		return adjustToWeekBegin(CalendarUtil.createCalendar(aDate));
    }
    
    /**
     * This method adjusts the given date to Monday of the week
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustToWeekBegin(Calendar aCalendar, Date aDate) {
        aCalendar.setTime(aDate);
        return adjustToWeekBegin(aCalendar);
    }
    
    /**
     * This method adjusts the given Calendar to Monday of the week
     *
     * @param aCalendar The Calendar to adjust
     */
    public static Date adjustToWeekBegin(Calendar aCalendar) {
        aCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return adjustToDayBegin(aCalendar);
    }
    
    /**
     * This method adjusts the given date to Sunday of the week
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustToWeekEnd(Date aDate) {
		return adjustToWeekEnd(CalendarUtil.createCalendar(aDate));
    }
    
    /**
     * This method adjusts the given date to Sunday of the week
     *
     * @param aDate
     *        A date. Must not be <code>null</code>.
     */
    public static Date adjustToWeekEnd(Calendar aCalendar, Date aDate) {
        aCalendar.setTime(aDate);
        return adjustToWeekEnd(aCalendar);
    }
    
    /**
     * This method adjusts the given Calendar to Sunday of the week
     *
     * @param aCalendar The Calendar to adjust
     */
    public static Date adjustToWeekEnd(Calendar aCalendar) {
        aCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return adjustToDayEnd(aCalendar);
    }
    
	/**
	 * Returns a date with the same day as aDate and time as indicated in hour, minute, second,
	 * millisecond
	 * 
	 * @return a new date with the given time
	 */
	public static Date adjustTime(Date aDate, int anHour, int aMinute, int aSecond, int aMillisecond) {
		return adjustTime(CalendarUtil.createCalendar(aDate), anHour, aMinute, aSecond, aMillisecond);
	}

	/**
	 * Returns a date with the same day as aDate and time as indicated in hour, minute, second,
	 * millisecond.
	 * 
	 * @return a new date with the given time
	 */
	public static Date adjustTime(Calendar cal, int anHour, int aMinute, int aSecond, int aMillisecond) {
		cal.set(Calendar.HOUR_OF_DAY  , anHour);
		cal.set(Calendar.MINUTE       , aMinute);
		cal.set(Calendar.SECOND       , aSecond);
		cal.set(Calendar.MILLISECOND  , aMillisecond);
		return cal.getTime();
	}

	/**
	 * Return a date which where the time has been taken from the second argument.
	 * 
	 * @param aDate
	 *        The date to take the day in year, <code>null</code> will be returned as such.
	 * @param aTime
	 *        The date to take the time from, <code>null</code> will result in aDate
	 * @return A date composed from aDate and aTime.
	 * 
	 * @see #adjustTime(Calendar, Date)
	 */
	public static Date adjustTime(Date aDate, Date aTime) {
		if (aDate == null || aTime == null) {
			return (aDate);
		}
		
		return adjustTime(CalendarUtil.createCalendar(aDate), aTime).getTime();
	}

	/**
	 * Adjusts the given {@link Calendar} by setting the time from the given {@link Date}.
	 * 
	 * @param calendar
	 *        The {@link Calendar} to take the day in year and year from. Must not be
	 *        <code>null</code>.
	 * @param time
	 *        The date to take the time from. Must not be <code>null</code>.
	 * @return The given calendar.
	 */
	public static Calendar adjustTime(Calendar calendar, Date time) {
		int year = calendar.get(Calendar.YEAR);
		int doy = calendar.get(Calendar.DAY_OF_YEAR);

		calendar.setTime(time);

		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.DAY_OF_YEAR, doy);
		return calendar;
	}

	public static Date adjustToDayBegin(Date aDate) {
		return adjustTime(aDate, 0, 0, 0, 0);
	}

	public static Date adjustToDayBegin(Calendar aCal) {
		return adjustTime(aCal, 0, 0, 0, 0);
	}

	public static Date adjustToDayEnd(Date aDate) {
		return adjustTime(aDate, 23, 59, 59, 999);
	}

	public static Date adjustToDayEnd(Calendar cal) {
		return adjustTime(cal, 
		         cal.getActualMaximum(Calendar.HOUR_OF_DAY), 
		         cal.getActualMaximum(Calendar.MINUTE), 
		         cal.getActualMaximum(Calendar.SECOND), 
		         cal.getActualMaximum(Calendar.MILLISECOND));
	}

	public static Date adjustToNoon(Date aDate) {
		return adjustTime(aDate, 12, 0, 0, 0);
	}

	public static Date adjustToNoon(Calendar cal) {
		return adjustTime(cal, 12, 0, 0, 0);
	}

	/**
	 * Returns true if the two dates represent the same day.
	 * 
	 * @param aDate1
	 *            first date to compare, may be null
	 * @param aDate2
	 *            second day to compare, may be null
	 * @return true if aDate1 represents the same day as aDate2 and BOTH vales are not null
	 */
	public static boolean sameDay(Date aDate1, Date aDate2) {
		if (aDate1 != null && aDate2 != null) {
			Calendar cal = CalendarUtil.createCalendar(aDate1);
			int year1 = cal.get(Calendar.YEAR);
			int day1 = cal.get(Calendar.DAY_OF_YEAR);

			cal.setTime(aDate2);
			int year2 = cal.get(Calendar.YEAR);
			int day2 = cal.get(Calendar.DAY_OF_YEAR);
			return year1 == year2 && day1 == day2;
		}
		// else{
		return false;
	}

	/**
	 * This method returns <code>true</code> if and only if the two dates have the same year,
	 * <code>false</code> otherwise.
	 * 
	 * @param aDate1
	 *            The first date. Maybe <code>null</code>.
	 * @param aDate2
	 *            The second date. Maybe <code>null</code>.
	 */
	public static boolean sameYear(Date aDate1, Date aDate2) {
		return sameYear(CalendarUtil.createCalendar(), aDate1, aDate2);
	}

	/**
	 * This method works like {@link #sameYear(Date, Date)} but this method uses the given calendar for
	 * calculation.
	 */
	public static boolean sameYear(Calendar aCalendar, Date aDate1, Date aDate2) {
		if (aDate1 != null && aDate2 != null) {
			aCalendar.setTime(aDate1);
			int year1 = aCalendar.get(Calendar.YEAR);

			aCalendar.setTime(aDate2);
			int year2 = aCalendar.get(Calendar.YEAR);
			return year1 == year2;
		}

		return false;
	}

    /**
     * This method returns <code>true</code> if and only if the given dates are
     * in the same year and month, otherwise <code>false</code>.
     *
     * @param aDate1
     *            First date to compare
     * @param aDate2
     *            Second date to compare
     */
    public static boolean sameMonthAndYear(Date aDate1, Date aDate2) {
		return sameMonthAndYear(CalendarUtil.createCalendar(), aDate1, aDate2);
    }

    /**
     * This method returns <code>true</code> if and only if the given dates are
     * in the same year and month, otherwise <code>false</code>.
     *
     * @param aCalendar
     *            A calendar must NOT be <code>null</code>.
     * @param aDate1
     *            First date to compare
     * @param aDate2
     *            Second date to compare
     */
    public static boolean sameMonthAndYear(Calendar aCalendar, Date aDate1, Date aDate2) {
        if (aDate1 != null && aDate2 != null) {
            if (sameYear(aCalendar, aDate1, aDate2)) {
                aCalendar.setTime(aDate1);
                int monthAsInt1 = aCalendar.get(Calendar.MONTH);
                
                aCalendar.setTime(aDate2);
                int monthAsInt2 = aCalendar.get(Calendar.MONTH);
                
                return monthAsInt1 == monthAsInt2;
            }
        }
        return false;
    }
    
	/**
	 * Returns <code>true</code> if and only if the given dates are in the same
	 * week of the same year.
	 * 
	 * @param aDate1  First date to compare
	 * @param aDate2  First date to compare
	 */
    public static boolean sameWeekOfYear(Date aDate1, Date aDate2) {
    	if (sameYear(aDate1, aDate2)) {
			return getSystemWeekOfYearFor(aDate1) == getSystemWeekOfYearFor(aDate2);
    	}
    	else {
    		return false;
    	}
    }
    
    /** 
     * This method returns the quarter of year of the given date.
     * 
     * @param aDate The date to get the quarter from. Must NOT be <code>null</code>.
     */
    public static int getQuarterOfYearFor(Date aDate) {
		return (getMonthOfDate(aDate) / 3);
    }
    
    /** 
     * This method returns the half of year of the given date.
     * 
     * @param aDate The date to get the half from. Must NOT be <code>null</code>.
     */
    public static int getHalfOfYearFor(Date aDate) {
		return (getMonthOfDate(aDate) / 6);
    }
    
    /** 
     * Returns <code>true</code> if and only if the given dates are in the same
	 * quarter of the same year.
	 * 
	 * @param aDate1  First date to compare
	 * @param aDate2  First date to compare
     */
    public static boolean sameQuarterOfYear(Date aDate1, Date aDate2) {
    	if (sameYear(aDate1, aDate2)) {
			return getQuarterOfYearFor(aDate1) == getQuarterOfYearFor(aDate2);
    	}
    	else {
    		return false;
    	}
    }
    
    /** 
     * Returns <code>true</code> if and only if the given dates are in the same
	 * half of the same year.
	 * 
	 * @param aDate1  First date to compare
	 * @param aDate2  First date to compare
     */
    public static boolean sameHalfOfYear(Date aDate1, Date aDate2) {
    	if (sameYear(aDate1, aDate2)) {
    		return getHalfOfYearFor(aDate1) == getHalfOfYearFor(aDate2);
    	}
    	else {
    		return false;
    	}
    }


	/**
	 * Computes the time difference between the given dates (in years).
	 * 
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the difference (in years) between date1 and date2; a value &lt; 0 means date1 is after
	 *         date2, a value > 0 means date1 is before date2, a value = 0 means the two dates
	 *         represents the same year.
	 */
	public static int differenceInYears(Date date1, Date date2) {
		Calendar cal1 = CalendarUtil.createCalendar(date1);
		Calendar cal2 = CalendarUtil.createCalendar(date2);
		return cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
	}

	/**
	 * Computes the time difference between the given dates (in quarters).
	 * 
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the difference (in quarters) between date1 and date2; a value &lt; 0 means date1 is
	 *         after date2, a value > 0 means date1 is before date2, a value = 0 means the two dates
	 *         represents the same quarter.
	 */
	public static int differenceInQuarters(Date date1, Date date2) {
		Calendar cal1 = CalendarUtil.createCalendar(date1);
		Calendar cal2 = CalendarUtil.createCalendar(date2);
		int years = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
		int quarters = cal2.get(Calendar.MONTH) / 3 - cal1.get(Calendar.MONTH) / 3;
		return (years * 4) + quarters;
	}

    /**
	 * Computes the time difference between the given dates (in months).
	 * 
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the difference (in months) between date1 and date2; a value &lt; 0 means date1 is after
	 *         date2, a value > 0 means date1 is before date2, a value = 0 means the two dates
	 *         represents the same month.
	 */
	public static int differenceInMonths(Date date1, Date date2) {
		Calendar cal1 = CalendarUtil.createCalendar(date1);
		Calendar cal2 = CalendarUtil.createCalendar(date2);
		int years = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
		int months = cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
		return (years * 12) + months;
	}

	/**
	 * Computes the time difference between the given dates (in weeks).
	 * 
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the difference (in weeks) between date1 and date2; a value &lt; 0 means date1 is after
	 *         date2, a value > 0 means date1 is before date2, a value = 0 means the two dates
	 *         represents the same week.
	 */
	public static int differenceInWeeks(Date date1, Date date2) {
		return differenceInDays(adjustToWeekBegin(date1), adjustToWeekBegin(date2)) / 7;
	}

	/**
	 * Computes the time difference between the given dates (in days).
	 * 
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the difference (in days) between date1 and date2; a value &lt; 0 means date1 is after
	 *         date2, a value > 0 means date1 is before date2, a value = 0 means the two dates
	 *         represents the same day.
	 */
	public static int differenceInDays(Date date1, Date date2) {
		return dayDiff(date1, date2, DateUtil.EXCLUDE_FIRST_DATE);
    }

    /**
	 * Computes the time difference between the given dates (in milliseconds).
	 * 
	 * @param date1
	 *        the first date
	 * @param date2
	 *        the second date
	 * @return the difference (in milliseconds) between date1 and date2; a value &lt; 0 means date1 is
	 *         after date2, a value > 0 means date1 is before date2, a value = 0 means the two dates
	 *         are equal.
	 */
    public static long difference(Date date1, Date date2) {
		return date2.getTime() - date1.getTime();
    }

    /**
     * Computes the time distance (in milliseconds) between the given dates.<br/>
     * this is a shortcut to Math.abs(difference(date1, date2));
     *
     * @param date1
     *        the first date
     * @param date2
     *        the second date
     * @return the distance between date1 and date2 (in milliseconds)
     */
    public static long distance(Date date1, Date date2) {
        return Math.abs(difference(date1, date2));
    }

    /**
     * Gets the newest (greatest) of the given dates.
     *
     * @param date1
     *        the first date
     * @param date2
     *        the second date
     * @return the newest of the given dates.
     */
    public static Date newest(Date date1, Date date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.before(date2) ? date2 : date1;
    }

    /**
     * Gets the oldest (lowest) of the given dates.
     *
     * @param date1
     *        the first date
     * @param date2
     *        the second date
     * @return the oldest of the given dates.
     */
    public static Date oldest(Date date1, Date date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.after(date2) ? date2 : date1;
    }

	/**
	 * Returns number of days between two dates including start and stop day. The value is positive if
	 * aDate1 is before aDate2 and negative if otherwise. For the same day this will return 1.
	 */
	public static int dayDiff(Date aDate1, Date aDate2) {
		long diff = adjustToNoon(aDate2).getTime() - adjustToNoon(aDate1).getTime();
		// Rounding is required because of the switch between summer and
        // winter time in March and October, which will result in not exact
		// MILLIS_PER_DAY milliseconds each day
		int result = (int)Math.round(diff / (double)MILLIS_PER_DAY); // adjust to days
		if (result < 0) { // Adjust to inclusion semantics
			return result - 1;
		}
		return result + 1;
	}

	/**
	 * This method returns the number of months between two dates including
	 * start and end month. The difference is positive if date1 is before date2,
	 * otherwise negative. 
	 * 
	 * Note, for the same month this method will return 1.
	 * 
	 * @param date1
	 *            A date 1 must NOT be <code>null</code>.
	 * @param date2
	 *            A date 2 must NOT be <code>null</code>.
	 * @return Returns the month difference.
	 */
	public static int monthDiffNo0(Date date1, Date date2) {
		if (date2.after(date1)) {
			return differenceInMonths(date1, date2) + 1;
		} else if (date1.after(date2)) {
			return -(differenceInMonths(date2, date1) + 1);
		} else {
			return 1;
		}
	}

	/**
	 * @deprecated Use {@link #differenceInMonths(Date, Date)} instead.
	 */
	@Deprecated
	public static int monthDiff(Date date1, Date date2) {
		return differenceInMonths(date1, date2);
	}

	/**
	 * Returns number of days between two dates. You can specify that The value is positive if aDate1 is
	 * before aDate2 and negative if otherwise. For the same day this will return 1.
	 * 
	 * @param aFirstDate
	 *            first day
	 * @param aSecondDate
	 *            second day
	 * @param exclusiveDay
	 *            is the amount of days that should be excluded. Value must be between zero and two! See
	 *            {@link #EXCLUDE_FIRST_DATE}, {@link #EXCLUDE_SECOND_DATE} and
	 *            {@link #EXCLUDE_BOTH_DATES}.
	 * 
	 * @throws IllegalArgumentException
	 *             if value is less than zero or greater than two.
	 */
	public static int dayDiff(Date aFirstDate, Date aSecondDate, int exclusiveDay) {
		if (exclusiveDay < 0 || exclusiveDay > 2) {
			throw new IllegalArgumentException("Value must be between zero and two but was "
					+ exclusiveDay);
		}

		int diff = dayDiff(aFirstDate, aSecondDate);

		if (diff == 1 && exclusiveDay > 1) {
			return 0;
		}

		if (diff > 0) {
			return diff - exclusiveDay;
		}
		return diff + exclusiveDay;
	}

    /**
     * This method returns a new date which is one day after the given date.
     *
     * @param aDate
     *            A {@link Date}. Must not be <code>null</code>.
     * @return Returns a new date which is one day after the given date.
     */
    public static Date nextDay(Date aDate) {
        return addDays(aDate, 1);
    }

    /**
     * This method returns a new date which is one day before the given date.
     *
     * @param aDate
     *            A {@link Date}. Must not be <code>null</code>.
     * @return Returns a new date which is one day before the given date.
     */
    public static Date prevDay(Date aDate) {
        return addDays(aDate, -1);
    }

    /**
     * This method returns a new date which is one month after the given date.
     *
     * @param aDate
     *            A {@link Date}. Must not be <code>null</code>.
     * @return Returns a new date which is one month after the given date.
     */
    public static Date nextMonth(Date aDate) {
        return addMonths(aDate, 1);
    }

    /**
     * This method returns a new date which is one month before the given date.
     *
     * @param aDate
     *            A {@link Date}. Must not be <code>null</code>.
     * @return Returns a new date which is one month before the given date.
     */
    public static Date prevMonth(Date aDate) {
        return addMonths(aDate, -1);
    }

	/**
	 * Adds the given amount of years to the given date. May be negative to subtract years from the date.
	 * 
	 * @param aDate
	 *            the date to add years to; must not be <code>null</code>
	 * @param years
	 *            the amount of years to add; may be negative
	 * @return a new Date with the added years
	 */
	public static Date addYears(Date aDate, int years) {
		Calendar theCal = CalendarUtil.createCalendar(aDate);
		theCal.add(Calendar.YEAR, years);
		return theCal.getTime();
	}

	/**
	 * Adds the given amount of quarter of years (3 Month) to the given date. Negative numbers are
	 * allowed to subtract quarters.
	 * 
	 * @param aDate
	 *            the date to add quarters; must not be <code>null</code>
	 * @param aNumberOfQuarters
	 *            the amount of quarters to add; may be negative
	 * @return new {@link Date} with the added quarters
	 */
	public static Date addQuarters(Date aDate, int aNumberOfQuarters) {
		return addMonths(aDate, aNumberOfQuarters * 3);
	}

	/**
	 * Adds the given amount of months to the given date. Negative numbers are allowed to subtract
	 * months.
	 * 
	 * @param aDate
	 *            the date to add months; must not be <code>null</code>
	 * @param aNumberOfMonths
	 *            the amount of months to add; may be negative
	 * @return new {@link Date} with the added months
	 */
	public static Date addMonths(Date aDate, int aNumberOfMonths) {
		Calendar theCal = CalendarUtil.createCalendar(aDate);
		theCal.add(Calendar.MONTH, aNumberOfMonths);
		return theCal.getTime();
	}

	/**
	 * This method returns a new date with the number of day after/before the given date. May be
	 * negative to subtract days from the date.
	 * 
	 * @param aDate
	 *        A {@link Date}. Must not be <code>null</code>.
	 * @param aNumberOfDays
	 *        A number of days.
	 * @return Returns a new date which is the given amount of day after/before the given date.
	 */
	public static Date addDays(Date aDate, int aNumberOfDays) {
		return addDays(CalendarUtil.createCalendar(), aDate, aNumberOfDays);
	}
	
    /**
	 * This method returns a new date with the number of day after/before the given date.
	 * 
	 * May be negative to subtract days from the date.
	 * 
	 * @param aDate
	 *        A {@link Date}. Must not be <code>null</code>.
	 * @param aNumberOfDays
	 *        A number of days.
	 * @return Returns a new date which is the given amount of day after/before the given date.
	 */
    public static Date addDays(Calendar aCal, Date aDate, int aNumberOfDays) {
        aCal.setTime(aDate);
        aCal.add(Calendar.DAY_OF_YEAR, aNumberOfDays);
        return aCal.getTime();
    }

    /**
	 * This method returns a new date with the number of hours after/before the given date. May be
	 * negative to subtract hours from the date.
	 * 
	 * @param aDate
	 *            A {@link Date}. Must not be <code>null</code>.
	 * @param aNumberOfHours
	 * 			  A number of Hours.
	 * @return Returns a new date which is aNumberOfHours hours after/before the given date.
	 */
	public static Date addHours(Date aDate, int aNumberOfHours) {
		Calendar theCalendar = CalendarUtil.createCalendar(aDate);
		theCalendar.add(Calendar.HOUR, aNumberOfHours);
		return theCalendar.getTime();
	}

	/**
	 * This method returns a new date with the number of minutes after/before the given date. May be
	 * negative to subtract minutes from the date.
	 * 
	 * @param date
	 *        A {@link Date}. Must not be <code>null</code>.
	 * @param minutes
	 *        The number of minutes to add.
	 * @return Returns a new date which is the given amount of minutes after/before the given date.
	 */
	public static Date addMinutes(Date date, int minutes) {
		Calendar calendar = CalendarUtil.createCalendar(date);
		calendar.add(Calendar.MINUTE, minutes);
		return calendar.getTime();
	}

	/**
	 * This method returns a new date with the number of seconds after/before the given date. May be
	 * negative to subtract seconds from the date.
	 * 
	 * @param aDate
	 *        A {@link Date}. Must not be <code>null</code>.
	 * @param aNumberOfSeconds
	 *        A number of seconds.
	 * @return Returns a new date which is the given amount of seconds after/before the given date.
	 */
	public static Date addSeconds(Date aDate, int aNumberOfSeconds) {
		Calendar theCalendar = CalendarUtil.createCalendar(aDate);
		theCalendar.add(Calendar.SECOND, aNumberOfSeconds);
		return theCalendar.getTime();
	}

	/**
	 * This method returns a new date with the number of milliseconds after/before the given date.
	 * May be negative to subtract milliseconds from the date.
	 * 
	 * @param date
	 *        A {@link Date}. Must not be <code>null</code>.
	 * @param milliseconds
	 *        The number of milliseconds to add.
	 * @return Returns a new date which is the given amount of milliseconds after/before the given
	 *         date.
	 */
	public static Date addMilliseconds(Date date, int milliseconds) {
		Calendar calendar = CalendarUtil.createCalendar(date);
		calendar.add(Calendar.MILLISECOND, milliseconds);
		return calendar.getTime();
	}

    /**
     * Returns true if the date is in the interval (compared in milliseconds).
     *
     * @param aDate
     *        The date to be tested.
     * @param aStart
     *        The start of the interval (inclusive). Null is allowed.
     * @param anEnd
     *        The end of the interval (inclusive). Null is allowed.
     * @return true when aDate is in the interval [aStart, anEnd].
     */
    public static boolean inInterval(Date aDate, Date aStart, Date anEnd) {
        boolean afterStart = aStart == null ? true : aDate.compareTo(aStart) >= 0;
        boolean beforeEnd = anEnd == null ? true : aDate.compareTo(anEnd) <= 0;
        return afterStart && beforeEnd;
    }

    /**
     * Returns true if the date is in the interval compared by days.
     *
     * @param aDate
     *        The date to be tested.
     * @param aStart
     *        The start of the interval (inclusive). Null is allowed.
     * @param anEnd
     *        The end of the interval (inclusive). Null is allowed.
     * @return true when aDate is in the interval [aStart, anEnd].
     */
    public static boolean inDayInterval(Date aDate, Date aStart, Date anEnd) {
        boolean afterStart = aStart == null ? true : compareDatesByDay(aDate, aStart) >= 0;
        boolean beforeEnd = anEnd == null ? true : compareDatesByDay(aDate, anEnd) <= 0;
        return afterStart && beforeEnd;
    }

    /**
     * Returns true if the date is in the interval compared by months.
     *
     * @param aDate
     *        The date to be tested.
     * @param aStart
     *        The start of the interval (inclusive). Null is allowed.
     * @param anEnd
     *        The end of the interval (inclusive). Null is allowed.
     * @return true when aDate is in the interval [aStart, anEnd].
     */
    public static boolean inMonthInterval(Date aDate, Date aStart, Date anEnd) {
        boolean afterStart = aStart == null ? true : compareDatesByMonth(aDate, aStart) >= 0;
        boolean beforeEnd = anEnd == null ? true : compareDatesByMonth(aDate, anEnd) <= 0;
        return afterStart && beforeEnd;
    }
    
    /**
     * Returns true if the date is in the interval compared by years.
     *
     * @param aDate
     *        The date to be tested.
     * @param aStart
     *        The start of the interval (inclusive). Null is allowed.
     * @param anEnd
     *        The end of the interval (inclusive). Null is allowed.
     * @return true when aDate is in the interval [aStart, anEnd].
     */
    public static boolean inYearInterval(Date aDate, Date aStart, Date anEnd) {
        boolean afterStart = aStart == null ? true : compareDatesByYear(aDate, aStart) >= 0;
        boolean beforeEnd = anEnd == null ? true : compareDatesByYear(aDate, anEnd) <= 0;
        return afterStart && beforeEnd;
    }
    
	/**
	 * Returns true if the intervals [a1,a2] and [b1,b2] intersects each other. 
	 * 
	 * @param a1
	 *         Start of the first interval, may be <code>null</code>.
	 * @param a2
	 *         End of the first interval, may be <code>null</code>
	 * @param b1
	 *         Start of the second interval, may be <code>null</code>
     * @param b2
     *         End of the second interval, may be <code>null</code>
     *         
     * @return true if [a1,a2] and [b1,b2] have common dates.
     * 
     * @see DateUtil#inInterval(Date, Date, Date)
	 */
    public static boolean overlaps(Date a1, Date a2, Date b1, Date b2) {
        // one of the intervals is [null,null]
        if ((a1 == null && a2 == null) || (b1 == null && b2 == null)) {
            return false;
        }
        
        // [null,a2] is interpreted as [a2,a2]
        if (a1 == null) {
            a1 = a2;
        }
        // [a1, null] is interpreted [a1,a1]
        else if (a2 == null) {
            a2 = a1;
        }
        
        return inInterval(a1, b1, b2) || inInterval(a2, b1, b2) || inInterval(b1, a1, a2) || inInterval(b2, a1, a2);
    }
	
	/**
	 * Asserts that the given date is in the interval.
	 * 
	 * @see #inInterval(Date, Date, Date)
	 */
	public static void assertInIntervall(Date d, Date intervalStart, Date intervalEnd) {
		if (!inInterval(d, intervalStart, intervalEnd)) {
			throw new RuntimeException("The given date " + d.toString()
					+ " does not lie in the interval from " + intervalStart.toString() + " to "
					+ intervalEnd.toString());
		}
	}

	/**
	 * This Method returns a date with the last Day of this month. Time will be the same.
	 * 
	 * @see #lastDayofTheMonth(Calendar, Date)
	 * 
	 * @param aDate
	 *            the initial Date
	 * @return the Date with the last day of the month
	 * @deprecated use {@link #adjustDateToMonthEnd(Date)} instead.
	 */
	@Deprecated
	public static Date lastDayofTheMonth(Date aDate) {
		Calendar cal = CalendarUtil.createCalendar();
		return lastDayofTheMonth(cal, aDate);
	}

	/**
	 * This Method returns a date with the last Day of this month. Time will be the same.
	 * 
	 * @param aDate
	 *            the initial Date
	 * @param aCal
	 *            your calendar instance
	 * @return the Date with the last day of the month
     * @deprecated use {@link #adjustDateToMonthEnd(Calendar, Date)} instead.
	 */
	@Deprecated
	public static Date lastDayofTheMonth(Calendar aCal, Date aDate) {
		aCal.setTime(aDate);
		aCal.set(Calendar.DAY_OF_MONTH, aCal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return aCal.getTime();
	}

	/**
	 * This method returns for the given dates the years between start date and end date.
	 * E.g. 01.05.2007 and 18.03.2010 = 2007, 2008, 2009, 2010.
	 * 
	 * @param aStartDate
	 *            A start date. The start date must be before the end date.
	 * @param aEndDate
	 *            A end date. The end date must be after the start date.
     * @return a sorted list with the years between the given dates
	 */
	public static List<String> getYearsAsStrings(Date aStartDate, Date aEndDate) {
		ArrayList<String> dates = new ArrayList<>();
		if (compareDatesByYear(aStartDate, aEndDate) > 0) return dates;
		Calendar calendar = CalendarUtil.createCalendar();

		// Get the start year
		calendar.setTime(aStartDate);
		int startYear = calendar.get(Calendar.YEAR);

		// Get the end year
		calendar.setTime(aEndDate);
		int endYear = calendar.get(Calendar.YEAR);

		int i = startYear;
		while (i <= endYear) {
			String year = String.valueOf(i);
			dates.add(year);
			i++;
		}

		return dates;
	}

    /**
     * This method returns for the given dates the month and years between start date and end date
     * in form "[year][separator][month]".
     * E.g. 01.10.2007 and 18.03.2008 = 2007.10, 2007.11, 2007.12, 2008.01, 2008.02, 2008.03.
     *
     * @param aStartDate
     *            A start date. The start date must be before the end date.
     * @param aEndDate
     *            A end date. The end date must be after the start date.
     * @param aSeparator
     *            The separator between year and month to use.
     * @return a sorted list with the years and months between the given dates
     */
    public static List<String> getMonthsAsStrings(Date aStartDate, Date aEndDate, String aSeparator) {
        ArrayList<String> dates = new ArrayList<>();
		Calendar calendar = CalendarUtil.createCalendar();
        Date startDate = DateUtil.adjustDateToMonthBegin(calendar, aStartDate);
        Date endDate = DateUtil.adjustDateToMonthEnd(calendar, aEndDate);

        if (startDate.after(endDate)) {
            return dates;
        }

        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumIntegerDigits(2);
        format.setMaximumIntegerDigits(2);

		// Get the start year and month
		calendar.setTime(aStartDate);
		int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH);

        // Get the end year and month
        calendar.setTime(aEndDate);
        int endYear = calendar.get(Calendar.YEAR);
        int endMonth = calendar.get(Calendar.MONTH);

        int year = startYear;
        while (year <= endYear) {
            int month = (year == startYear ? startMonth : calendar.getActualMinimum(Calendar.MONTH));
            int lastMonth = (year == endYear ? endMonth : calendar.getActualMaximum(Calendar.MONTH));
            while (month <= lastMonth) {
                String monthString = format.format(++month);
                String date = String.valueOf(year) + aSeparator + monthString;
                dates.add(date);
            }
            year++;
        }
        return dates;
    }

    /** 
     * See {@link #getMonthsAsStrings(Date, Date, String)}. 
     */
    public static String getMonthAsString(Date aDate, String aSeparator) {
    	return getMonthsAsStrings(aDate, aDate, aSeparator).get(0);
    }

    /** 
     * See {@link #getMonthsAsStrings(Date, Date)}. 
     */
    public static String getMonthAsString(Date aDate) {
    	return getMonthsAsStrings(aDate, aDate).get(0);
    }

    /**
     * This method returns for the given dates the month and years between start date and end date
     * in form "[year].[month]".
     * E.g. 01.10.2007 and 18.03.2008 = 2007.10, 2007.11, 2007.12, 2008.01, 2008.02, 2008.03.
     * 
     * @param aStartDate
     *            A start date. The start date must be before the end date.
     * @param aEndDate
     *            A end date. The end date must be after the start date.
     * @return a sorted list with the years and months between the given dates
     */
    public static List<String> getMonthsAsStrings(Date aStartDate, Date aEndDate) {
        return getMonthsAsStrings(aStartDate, aEndDate, "."); 
    }

    /**
     * Creates a short months list from an long month list, that means converts a month
     * list with elements in form "[year].[month]" into a list with elements "[month]".
     * E.g. converts "2007.11", "2007.12", "2008.01", "2008.02" into "11", "12", "01", "02".
     *
     * @param aMonthList
     *        The list to convert
     */
    public static void shortMonthsInList(List<String> aMonthList) {
        for (int i = 0, length = aMonthList.size(); i < length; i++) {
            String theMonth = aMonthList.get(i);
            aMonthList.set(i, theMonth.substring(theMonth.length() - 2));
        }
    }

    /**
     * This method returns for a given year as string (e.g. 2005) the following year as
     * string (e.g. 2006).
     * 
     * @param yearAsString
     *        the year as string
     * @return the year after the given one
     */
    public static String nextYear(String yearAsString) {
        int year = Integer.parseInt(yearAsString);
        return String.valueOf(++year);
	}

	/**
     * This method returns for a given year as string (e.g. 2005) the previous year as
     * string (e.g. 2004).
	 * 
	 * @param yearAsString
     *        the year as string
     * @return the year before the given one
	 */
	public static String previousYear(String yearAsString) {
    	int year = Integer.parseInt(yearAsString);
    	
    	return String.valueOf(--year);
    }
	
	public static int getYearOfDate(Date aDate) {
		return CalendarUtil.createCalendar(aDate).get(Calendar.YEAR);
	}

	/**
	 * This method returns the year of the given date.
	 * 
	 * @param aDate
	 *            The date to get the year from. Must NOT be <code>null</code>.
	 */
	public static String getYearAsStringFor(Date aDate) {
		return String.valueOf(getYearOfDate(aDate));
	}
	
	public static int getMonthOfDate(Date aDate) {
		return CalendarUtil.createCalendar(aDate).get(Calendar.MONTH);
	}

	/**
	 * This method returns the day of month for the given date.
	 * 
	 * @param aDate
	 *            The date to get the day of month. Must NOT be
	 *            <code>null</code>.
	 */
	public static int getDayOfMonthFor(Date aDate) {
		return CalendarUtil.createCalendar(aDate).get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * This method returns the day of month of the given date. If the
	 * leadingNil flag is enabled all month form 1..9 get a leading nil (e.g.
	 * 01, 02, 03,...).
	 */
	public static String getDayOfMonthAsString(boolean leadingNil, Date aDate) {
		String day = String.valueOf(getDayOfMonthFor(aDate));
		
		if (leadingNil) {
			if (day.length() < 2) {
				day = "0" + day;
			}
		}
		
		return day;
	}
	
	/**
	 * This method returns the month of the date (1..12). That is the difference
	 * to the {@link #getMonthOfDate(Date)} method (0..11).
	 * 
	 * @param aDate
	 *            The date to get the month from. Must NOT be <code>null</code>.
	 */
	public static int getMonthFor(Date aDate) {
		return getMonthOfDate(aDate) + 1;
	}
	
	/**
	 * This method returns the month of year of the given date. If the
	 * leadingNil flag is enabled all month form 1..9 get a leading nil (e.g.
	 * 01, 02, 03,...).
	 */
	public static String getMonthAsStringFor(boolean leadingNil, Date aDate) {
		String month = String.valueOf(getMonthFor(aDate));
		if (leadingNil) {
			if (month.length() < 2) {
				month = "0" + month;
			}
		}
		return month;
	}
	
	/**
	 * This method returns the week of year of the given date in the system locale.
	 * 
	 * @param date
	 *        The date to get the week from.
	 * 
	 * @return The week of the year in the system locale.
	 */
	public static int getSystemWeekOfYearFor(Date date) {
		return getWeekOfYearFor(systemLocale(), date);
	}

	/**
	 * This method returns the week of year of the given date in the given locale.
	 * 
	 * @param inLocale
	 *        The {@link Locale} that specifies the number of the week for the given timestamp.
	 * @param date
	 *        The date to get the week from.
	 * 
	 * @return The week of the year in the given locale.
	 */
	public static int getWeekOfYearFor(Locale inLocale, Date date) {
		Calendar cal = CalendarUtil.createCalendar(date, inLocale);

		return cal.get(Calendar.WEEK_OF_YEAR);
	}
	
	/**
	 * This method returns the week of year of the given date in the system locale. If the
	 * <code>leadingZero</code> flag is enabled all weeks from 0,...,9 get a leading zero (e.g. 00,
	 * 01, 02, 03,...).
	 * 
	 * @param leadingZero
	 *        Whether one digit weeks should have a leading zero.
	 * @param date
	 *        The date to get week for.
	 * 
	 * @return The week of the year in the system locale.
	 * 
	 */
	public static String getSystemWeekAsStringFor(boolean leadingZero, Date date) {
		return getWeekAsStringFor(systemLocale(), leadingZero, date);
	}

	private static Locale systemLocale() {
		return Locale.getDefault();
	}

	/**
	 * This method returns the week of year of the given date in the given locale. If the
	 * <code>leadingZero</code> flag is enabled all weeks from 0,...,9 get a leading zero (e.g. 00,
	 * 01, 02, 03,...).
	 * 
	 * @param inLocale
	 *        The {@link Locale} that specifies the number of the week for the given timestamp.
	 * @param leadingZero
	 *        Whether one digit weeks should have a leading zero.
	 * @param date
	 *        The date to get week for.
	 * 
	 * @return The week of the year in the given locale.
	 * 
	 */
	public static String getWeekAsStringFor(Locale inLocale, boolean leadingZero, Date date) {
		String weekOfYear = String.valueOf(getWeekOfYearFor(inLocale, date));
		
		if (leadingZero) {
			if (weekOfYear.length() < 2) {
				weekOfYear = "0" + weekOfYear;
			}
		}
		
		return weekOfYear;
	}

    /**
     * This method returns a date for the given object.
     * 
     * @param aObject
     *            An object (e.g. Date, Calendar, Number).
     */
    public static Date getDateFor(Object aObject) {
        if (aObject instanceof Date) {
            return (Date)aObject;
        }
        else if (aObject instanceof Calendar) {
            return ((Calendar)aObject).getTime();
        }
        else if (aObject instanceof Number) {
			Calendar calendar = CalendarUtil.createCalendar();
            calendar.set(Calendar.YEAR, ((Number)aObject).intValue());
            return calendar.getTime();
        }
        return null;
    }

    /**
     * This method parses a date from the given month string.
     * 
     * @param aMonthString
     *        A string in format "yyyy.mm" to parse a date from, as returned by the
     *        {@link #getMonthAsString(Date)} method.
     */
    public static Date getDateFromMonthString(String aMonthString) {
        if (StringServices.isEmpty(aMonthString)) return null;
		Calendar theCal = CalendarUtil.createCalendar();
        theCal.set(Calendar.YEAR, Integer.parseInt(aMonthString.substring(0, 4)));
        theCal.set(Calendar.MONTH, Integer.parseInt(aMonthString.substring(5)) - 1);
        return adjustDateToMonthBegin(theCal);
    }

	/**
	 * Check if given Calendar defines a working day !(Saturday or Sunday). This method does not respect
	 * holidays.
	 */
	public static boolean isWorkDay(Calendar cal) {
		int weekDay = cal.get(Calendar.DAY_OF_WEEK);
		return weekDay != Calendar.SATURDAY && weekDay != Calendar.SUNDAY;
	}

	/**
	 * Check if given Calendar defines a working day !(Saturday or Sunday). This method does not respect
	 * holidays.
	 */
	public static boolean isWorkDay(Date date) {
		return isWorkDay(CalendarUtil.createCalendar(date));
	}

	/**
	 * Add the period defined by aTimeString to a given date.
	 * 
	 * <p>
	 * A valid time string matches the pattern <code>([+-])?\\s*(([0-9]+)([yMwdhmsS])\\s*)*</code>
	 * (i.e. <code>'1d30m'</code> would add 1 day and 30 minutes and '- 5y 3M' would subtract 1 year
	 * and 3 months)
	 * </p>
	 * 
	 * The character indicator defining the period are:
	 * <ul>
	 * <li>'y' for year</li>
	 * <li>'M' for Month</li>
	 * <li>'w' for week</li>
	 * <li>'d' for day</li>
	 * <li>'h' for hour</li>
	 * <li>'m' for minutes</li>
	 * <li>'s' for seconds</li>
	 * <li>'S' for milliseconds</li>
	 * </ul>
	 * @param aReferenceDate
	 *        the reference date that should be moved, MUST NOT be <code>null</code>
	 * @param aTimeString
	 *        the string defining the period the date should be moved by, MIGHT be <code>null</code>
	 *        or empty. In that case the reference date would be returned without any changes.
	 * 
	 * @return the resulting {@link Date}
	 * 
	 * @see #add(Calendar, String)
	 */
	public static Date add(Date aReferenceDate, String aTimeString) {
		if (StringServices.isEmpty(aTimeString)) {
			return aReferenceDate;
		}

		return add(CalendarUtil.createCalendar(aReferenceDate), aTimeString).getTime();
	}

	/**
	 * Add the period defined by <code>modification</code> to a given {@link Calendar}.
	 * 
	 * <p>
	 * A valid modification string matches the pattern
	 * <code>([+-])?\\s*(([0-9]+)([yMwdhmsS])\\s*)*</code> (i.e. <code>'1d30m'</code> would add 1
	 * day and 30 minutes and '- 5y 3M' would subtract 1 year and 3 months)
	 * </p>
	 * 
	 * The character indicator defining the period are:
	 * <ul>
	 * <li>'y' for year</li>
	 * <li>'M' for Month</li>
	 * <li>'w' for week</li>
	 * <li>'d' for day</li>
	 * <li>'h' for hour</li>
	 * <li>'m' for minutes</li>
	 * <li>'s' for seconds</li>
	 * <li>'S' for milliseconds</li>
	 * </ul>
	 * 
	 * @param calendar
	 *        The calendar that should be moved. Must not be <code>null</code>.
	 * @param modification
	 *        The string defining the period the date should be moved by. Must not be null and
	 *        matches the defined pattern.
	 * 
	 * @return The given {@link Calendar}.
	 */
	public static Calendar add(Calendar calendar, String modification) {
		// check for sign
		int theFactor;
		modification = modification.trim();
		switch (modification.charAt(0)) {
			case '+':
				theFactor = 1;
				modification = modification.substring(1);
				break;
			case '-':
				theFactor = -1;
				modification = modification.substring(1);
				break;
			default:
				theFactor = 1;
		}

		//								 grp1		 grp2
		Pattern p = Pattern.compile("\\s*([0-9]+)([yMwdhmsS])\\s*");
		Matcher m = p.matcher(modification);

		int start = 0;
		while (m.lookingAt()) {
			// grep the two groups
			String value = m.group(1);
			String period = m.group(2);

			// reset the matcher for the rest of the time string
			start += m.end();
			m.reset(modification.substring(start));

			int field = period.charAt(0);
			switch (field) {
				case 'y':
					field = Calendar.YEAR;
					break;
				case 'M':
					field = Calendar.MONTH;
					break;
				case 'w':
					field = Calendar.WEEK_OF_YEAR;
					break;
				case 'd':
					field = Calendar.DAY_OF_YEAR;
					break;
				case 'h':
					field = Calendar.HOUR;
					break;
				case 'm':
					field = Calendar.MINUTE;
					break;
				case 's':
					field = Calendar.SECOND;
					break;
				case 'S':
					field = Calendar.MILLISECOND;
					break;
				default:
					throw new IllegalArgumentException("Given string '" + modification
						+ "' contains an unknown period character after position " + start);
			}
			calendar.add(field, Integer.parseInt(value) * theFactor);
		}

		if (start != modification.length()) {
			throw new IllegalArgumentException(
				"Given string '" + modification + "' does not fullfil required pattern after position " + start);
		}
		return calendar;
	}

	/** DateFormat used by {@link #toSortableString(Date)}. */
	public static DateFormat getFullDateFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyyMMdd-HHmmss");
	}

	/** DateFormat as specified in ISO 8601 e.g. used as default in log4j */
	public static DateFormat getIso8601DateFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}

}
