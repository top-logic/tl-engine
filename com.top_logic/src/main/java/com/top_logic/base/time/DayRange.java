/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;

/**
 * A TimeRangeIterator for Days.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class DayRange extends TimeRangeIterator {

    public static final Set<Integer> WEEKEND_DAYS = new HashSet<>();

    static {
        WEEKEND_DAYS.add(Calendar.SATURDAY);
        WEEKEND_DAYS.add(Calendar.SUNDAY);
    }

    /**
     * Created via introspection so I need any empty CTor.
     */
    public DayRange() {
        super();
    }

    /**
     * Allow direct construction as well.
     */
    public DayRange(Locale loc, Date aStart, Date anEnd) {
        init(loc, aStart, anEnd);
    }

    /**
     * Secondary Constructor to be called by the factory functions.
     * 
     * The Internal State will be set to the interval just before aStart.
     *       
     * @param loc Locale to use for Formatting and for creting the internal
     *        Calendar(s)
     * @param aStart first start for the time Ranges. 
     *         will be aligned to the very first allowed Date for the TimeRange. 
     * @param anEnd last date for the Time Ranges, 
     *         will be aligned to the very last allowed Date for the TimeRange. 
     */
    @Override
	public void init(Locale loc, Date aStart, Date anEnd) {
        super.init(loc, aStart, anEnd);  // 2005-365
        intForm    = getDayRangeFormat(); 
        guiForm    = CalendarUtil.getDateInstance(DateFormat.SHORT, loc);
        simpleForm = CalendarUtil.newSimpleDateFormat("DDD", loc);
    }
    
    /** 
     * Advance internal calendar to next day. 
     */
    @Override
	protected void nextInterval() {
        cal.add(Calendar.DAY_OF_YEAR, 1);
    }

    /** 
     * Set internal calendar to previous Day.
     */
    @Override
	protected void prevInterval() {
        cal.add(Calendar.DAY_OF_YEAR, -1);
    }

    /** 
     * Overriden to use {@link Calendar#add(int, int)} directly.
     */
    @Override
	public Date skip(int to) {
        cal.add(Calendar.DAY_OF_YEAR, to);
        return current();
    }

    /** 
     * Align the given Date to Midnight of same day.
     */
    @Override
	public Date alignToStart(Date aDate) {
        calcCal.setTime(aDate);
        calcCal.set(Calendar.HOUR_OF_DAY, calcCal.getActualMinimum(Calendar.HOUR_OF_DAY));
        calcCal.set(Calendar.MINUTE     , calcCal.getActualMinimum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND     , calcCal.getActualMinimum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND, calcCal.getActualMinimum(Calendar.MILLISECOND));
        return calcCal.getTime();
    }

    /** 
     * Align the given Date to one milli-second before Midnight of next day.
     */
    @Override
	public Date alignToEnd(Date aDate) {
        calcCal.setTime(aDate);
        // Will even work for non 24-Hour Days (e.g. Mars Standard Time ;-)
        calcCal.set(Calendar.HOUR_OF_DAY, calcCal.getActualMaximum(Calendar.HOUR_OF_DAY));
        calcCal.set(Calendar.MINUTE     , calcCal.getActualMaximum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND     , calcCal.getActualMaximum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND, calcCal.getActualMaximum(Calendar.MILLISECOND));
        return calcCal.getTime();
    }

    /** 
     * Align the given Calendar to Noon of the same day.
     */
    @Override
	public void align(Calendar aCal) {
        int max  = aCal.getActualMaximum(Calendar.HOUR_OF_DAY);
        int min  = aCal.getActualMinimum(Calendar.HOUR_OF_DAY);
        int noon = 1 + (max - min) >> 1; //  / 2
        aCal.set(Calendar.HOUR_OF_DAY, noon);
        aCal.set(Calendar.MINUTE     , aCal.getActualMinimum(Calendar.MINUTE));
        aCal.set(Calendar.SECOND     , aCal.getActualMinimum(Calendar.SECOND));
        aCal.set(Calendar.MILLISECOND, aCal.getActualMinimum(Calendar.MILLISECOND));
    }

    /** 
     * Return number of days from start to endDate (inclusive).
     * 
     * The number of days is calculated by aligning start and end to to the same
     * time in the day (noon) and then by arithmetic operation which includes division by
     * the number of milliseconds in a day. This must be handled by properly rounding the
     * division or else we get round of errors.
     */
    @Override
	public int getNumRanges() {
        calcCal.setTime(start);
        align(calcCal);
        long startTime = calcCal.getTimeInMillis();
        calcCal.setTime(end);
        align(calcCal);
        long endTime = calcCal.getTimeInMillis();
        long    diff = endTime - startTime;
        return 1 + (int)Math.round(((double)diff) / ((double)MILLIS_PER_DAY));
    }
    
    /**
     * Return true if current Day is a WorkDay.
     */
    public boolean isWorkDay() {
        return DateUtil.isWorkDay(cal);
    }
    
    /**
     * Computes the number of work days in the interval [{@link #getStart()},
     * {@link #getEnd()}] taking into consideration the specified set of week
     * days which should not be considered work days.
     * 
     * <p>
     * <b>NOTE:</b> the specified set must contain the following values only:
     * <ul>
     * <li>{@link Calendar#MONDAY}</li>
     * <li>{@link Calendar#TUESDAY}</li>
     * <li>{@link Calendar#WEDNESDAY}</li>
     * <li>{@link Calendar#THURSDAY}</li>
     * <li>{@link Calendar#FRIDAY}</li>
     * <li>{@link Calendar#SATURDAY}</li>
     * <li>{@link Calendar#SUNDAY}</li>
     * </ul>
     * </p>
     * 
     * @param nonWorkDays
     *            the set of fields defining week days which should not be
     *            considered work days.
     * @return the number of work days in the receiver's range
     */
    public int getWorkdayCount(final Set<Integer> nonWorkDays) {
        final int size = getNumRanges();
        final int weekCnt = size / 7; // might have some rest!
        int holidayCnt = weekCnt * nonWorkDays.size();

        // check if we have a rest value. Nope, we're NOT going to use
        // the modulo operator here since it's not the fastest way to
        // determine the rest.
        final int rest = size - weekCnt * 7;
        if (rest > 0) {
			final Calendar tmp = CalendarUtil.createCalendar(end);
			final int endDay = tmp.get(Calendar.DAY_OF_WEEK);

            // the trick is now to iterate over the DAY_OF_WEEK field until we
            // reach the end DAY_OF_WEEK! All days defined as holidays will
            // increment to total number of holidays.
			tmp.setTime(start);
            int day = -1;
            do {
				day = tmp.get(Calendar.DAY_OF_WEEK);
                if (nonWorkDays.contains(day)) {
                    holidayCnt++;
                }
				tmp.add(Calendar.DAY_OF_WEEK, 1);
            } while (day != endDay);
        }

        // the total number of work days is: daysInRange - holidaysInRange
        return size - holidayCnt;
    }

	/** Unified Format used internally by this DayRange */
	public static DateFormat getDayRangeFormat() {
		// Note: This must not be shared among threads.
		return CalendarUtil.newSimpleDateFormat("yyyy-DDD", Locale.US);
	}

}
