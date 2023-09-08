/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.top_logic.basic.time.CalendarUtil;

/**
 * A TimeRangeIterator for Weeks, be carefull works mostly lie Lcoale.US.
 * 
 * Some definitions about the beginning of the Week (Monday/Sunday) and
 * the first week of the year depend on the locale. Since we need
 * well defined, internal values independend of the locale we define as
 * follows: The first Day of the Week is Monday.  The minmal days for
 * the first week in a year are 4. (Same as for Locale.German). Due
 * to the usage of formatters with the desired locale the values
 * shown to the user may differ !
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class WeekRange extends TimeRangeIterator {

    /** 
     * Number of  millis per week.
     * 
     * Correct as long as we have 7 days a week.
     */
    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * 7;

    /**
     * Created via introspection so thy need any empty CTor.
     */
    public WeekRange() {
        super();
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
        // Must not call super since I must tweek the Calendar first.
        if (anEnd.before(aStart)) {
            throw new IllegalArgumentException(
                "End ("+ anEnd+") before Start (" + anEnd + ")");
        }
		this.cal = CalendarUtil.createCalendar(Locale.US);
        // This is a Business calendar, sorry
        this.cal    .setFirstDayOfWeek(Calendar.MONDAY);
        // Must nail this to avoid future problems
        this.cal    .setMinimalDaysInFirstWeek(4);
        
		this.calcCal = CalendarUtil.clone(cal);
        this.start   = alignToStart(aStart);
        this.end     = alignToEnd  (anEnd);
        this.cal.setTime(aStart);
        align(this.cal);
        prevInterval();  // cannot use yyyy-ww alone does not carry enough information
        SimpleDateFormat sdf = CalendarUtil.newSimpleDateFormat("yyyy-MM-dd:ww", Locale.US);
		sdf.setCalendar(CalendarUtil.clone(cal));
        intForm = sdf; 
        guiForm    = CalendarUtil.getDateInstance(DateFormat.SHORT, loc);
        simpleForm = CalendarUtil.newSimpleDateFormat("ww"            , loc);

    }
    
    /** 
     * Advance internal calendar to next day. 
     */
    @Override
	protected void nextInterval() {
        cal.add(Calendar.WEEK_OF_YEAR, 1);
    }

    /** 
     * Set internal calendar to previous Day.
     */
    @Override
	protected void prevInterval() {
        cal.add(Calendar.WEEK_OF_YEAR, -1);
    }

    /** 
     * Align the given Date to midnight at the first day of the week. 
     */
    @Override
	public Date alignToStart(Date aDate) {
        calcCal.setTime(aDate);
        calcCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calcCal.set(Calendar.HOUR_OF_DAY, calcCal.getActualMinimum(Calendar.HOUR_OF_DAY));
        calcCal.set(Calendar.MINUTE     , calcCal.getActualMinimum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND     , calcCal.getActualMinimum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND, calcCal.getActualMinimum(Calendar.MILLISECOND));
        return calcCal.getTime();
    }

    /** 
     * Align to one milli-second before Midnight of last day of the week.
     */
    @Override
	public Date alignToEnd(Date aDate) {
        calcCal.setTime(aDate);
        calcCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calcCal.set(Calendar.HOUR_OF_DAY, calcCal.getActualMaximum(Calendar.HOUR_OF_DAY));
        calcCal.set(Calendar.MINUTE     , calcCal.getActualMaximum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND     , calcCal.getActualMaximum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND, calcCal.getActualMaximum(Calendar.MILLISECOND));
        return calcCal.getTime();
    }

    /** 
     * Align the given Calendar to Noon of Wednesday.
     */
    @Override
	public void align(Calendar aCal) {
        // Work around a glitch in the calendat
        // int year =  calcCal.get(Calendar.YEAR);
        aCal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        /*
        if (year != calcCal.get(Calendar.YEAR)) {
            aCal.add(Calendar.WEEK_OF_YEAR, aCal.getActualMaximum(Calendar.WEEK_OF_YEAR));
        }
        */
        int max  = aCal.getActualMaximum(Calendar.HOUR_OF_DAY);
        int min  = aCal.getActualMinimum(Calendar.HOUR_OF_DAY);
        int noon = 1 + (max - min) >> 1; //  / 2
        aCal.set(Calendar.HOUR_OF_DAY, noon);
        aCal.set(Calendar.MINUTE     , calcCal.getActualMinimum(Calendar.MINUTE));
        aCal.set(Calendar.SECOND     , calcCal.getActualMinimum(Calendar.SECOND));
        aCal.set(Calendar.MILLISECOND, calcCal.getActualMinimum(Calendar.MILLISECOND));
    }

    /** 
     * Return number of weeks from start to endDate (inclusive).
     * 
     * Hope this optimization does not hit back.
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
        return 1 + (int)Math.round(((double)diff) / ((double)MILLIS_PER_WEEK));

        //long    diff = end.getTime() - start.getTime();
        //return (int) (diff / MILLIS_PER_WEEK);
    }
}
