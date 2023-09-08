/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.top_logic.basic.time.CalendarUtil;

/**
 * A TimeRangeIterator for Month.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class YearRange extends TimeRangeIterator {

    /** 
     * Number of  millis per YEAR.
     * 
     * Correct as long as we have about days per 365.25 year.
     */
    protected static final long MILLIS_PER_YEAR    = 36525 * MILLIS_PER_DAY / 100L;
    
    /**
     * Created via introspection so thy need any empty CTor.
     */
    public YearRange() {
        super();
    }
    
    /**
     * Allow direct construction as well.
     */
    public YearRange(Locale loc, Date aStart, Date anEnd) {
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
        super.init(loc, aStart, anEnd);  // 2005
		intForm = CalendarUtil.newSimpleDateFormat("yyyy", Locale.US);
		guiForm = CalendarUtil.newSimpleDateFormat("yyyy", loc);
        simpleForm = guiForm;

        // two digit year format makes probmlems, well
    }
    
    /** 
     * Advance internal calendar to next day. 
     */
    @Override
	protected void nextInterval() {
        cal.add(Calendar.YEAR, 1);
    }

    /** 
     * Set internal calendar to previous Day.
     */
    @Override
	protected void prevInterval() {
        cal.add(Calendar.YEAR, -1);
    }

    /** 
     * Align the given Date to Midnight of first day in month.
     */
    @Override
	public Date alignToStart(Date aDate) {
        calcCal.setTime(aDate);
        calcCal.set(Calendar.DAY_OF_YEAR , calcCal.getActualMinimum(Calendar.DAY_OF_YEAR));
        calcCal.set(Calendar.HOUR_OF_DAY , calcCal.getActualMinimum(Calendar.HOUR_OF_DAY));
        calcCal.set(Calendar.MINUTE      , calcCal.getActualMinimum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND      , calcCal.getActualMinimum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND , calcCal.getActualMinimum(Calendar.MILLISECOND));
        return calcCal.getTime();
    }

    /** 
     * Align to one milli-second before Midnight of last day in month
     */
    @Override
	public Date alignToEnd(Date aDate) {
        calcCal.setTime(aDate);
        calcCal.set(Calendar.DAY_OF_YEAR , calcCal.getActualMaximum(Calendar.DAY_OF_YEAR));
        calcCal.set(Calendar.HOUR_OF_DAY , calcCal.getActualMaximum(Calendar.HOUR_OF_DAY));
        calcCal.set(Calendar.MINUTE      , calcCal.getActualMaximum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND      , calcCal.getActualMaximum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND , calcCal.getActualMaximum(Calendar.MILLISECOND));
        return calcCal.getTime();
    }

    /** 
     * Align the given Calendar to midnight in the middle of the month.
     */
    @Override
	public void align(Calendar aCal) {
        int max    = aCal.getActualMaximum(Calendar.DAY_OF_YEAR);
        int min    = aCal.getActualMinimum(Calendar.DAY_OF_YEAR);
        int middle = 1 + (max - min) >> 1; //  / 2
        aCal.set(Calendar.DAY_OF_YEAR, middle);
        aCal.set(Calendar.HOUR_OF_DAY , calcCal.getActualMinimum(Calendar.HOUR_OF_DAY));
        aCal.set(Calendar.MINUTE      , calcCal.getActualMinimum(Calendar.MINUTE));
        aCal.set(Calendar.SECOND      , calcCal.getActualMinimum(Calendar.SECOND));
        aCal.set(Calendar.MILLISECOND , calcCal.getActualMinimum(Calendar.MILLISECOND));
    }

    
    /** 
     * Return number of year from start- to endDate (inclusive)
     * 
     * Hope this optimization does not hit back.
     */
     @Override
	public int getNumRanges() {
         calcCal.setTime(start);
         int starYear  = calcCal.get(Calendar.YEAR);
         calcCal.setTime(end);
         int endYear  = calcCal.get(Calendar.YEAR);
         return 1 + endYear - starYear;
    }
}
