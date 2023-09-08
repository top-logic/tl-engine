/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;

/**
 * A TimeRangeIterator for a Half Year.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klasu Halfmann</a>
 */
public class HalfYearRange extends TimeRangeIterator {

    /** 
     * Number of  millis per half a year.
     * 
     * Correct as long as we have about days per 365.25 year.
     */
    protected static final long MILLIS_PER_HALFYEAR    = YearRange.MILLIS_PER_YEAR >> 1;

    /**
     * Created via introspection so this needs any empty CTor.
     */
    public HalfYearRange() {
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
        super.init(loc, aStart, anEnd);     // 2005-12
        // intForm    = CalendarUtil.newSimpleDateFormat("yyyy-MM"             , Locale.US); 
        guiForm    = CalendarUtil.getDateInstance(DateFormat.SHORT, loc);
		simpleForm = CalendarUtil.newSimpleDateFormat("MM/yyyy", loc);
    }
    
    /** 
     * Advance internal calendar to next quarter. 
     */
    @Override
	protected void nextInterval() {
        cal.add(Calendar.MONTH, 6);
        align(cal); // Must realign since middle of month varies 
                    // (For February its the 14.)
    }

    /** 
     * Set internal calendar to previous Day.
     */
    @Override
	protected void prevInterval() {
        cal.add(Calendar.MONTH, -6);
        align(cal);
    }

    /** 
     * Align the given Date to Midnight of first day in Quarter.
     */
    @Override
	public Date alignToStart(Date aDate) {
        calcCal.setTime(aDate);
        // align to first month in quarter
        int month = calcCal.get(Calendar.MONTH);
            month = 6 * (month / 6);
            
		/*
		 * Don't make calendar adjusting its time before setting DAY_OF_MONTH
		 * since this can force overflow
		 */
        calcCal.set(Calendar.MONTH       , month);
        calcCal.set(Calendar.DAY_OF_MONTH, calcCal.getActualMinimum(Calendar.DAY_OF_MONTH));

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
        // align to last month in quarter
        calcCal.setTime(aDate);
        int month = calcCal.get(Calendar.MONTH);
            month = 5 + 6 * (month / 6);
        
        // Prevent Wrap around on 31. of a Month
        /*
         * Don't make calendar adjusting its time before setting DAY_OF_MONTH
         * since this can force overflow
         */
        calcCal.set(Calendar.DAY_OF_MONTH, calcCal.getActualMinimum(Calendar.DAY_OF_MONTH));
        calcCal.set(Calendar.MONTH       , month);

        // Will even work for non 24-Hour Days (e.g. Mars Standard Time ;-)
        calcCal.set(Calendar.DAY_OF_MONTH, calcCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        calcCal.set(Calendar.HOUR_OF_DAY , calcCal.getActualMaximum(Calendar.HOUR_OF_DAY));
        calcCal.set(Calendar.MINUTE      , calcCal.getActualMaximum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND      , calcCal.getActualMaximum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND , calcCal.getActualMaximum(Calendar.MILLISECOND));
        return calcCal.getTime();
    }

    /** 
     * Align the given Calendar to midnight at last day in third month for that halfy year.
     */
    @Override
	public void align(Calendar aCal) {
        // align to last month in quarter
        int month = aCal.get(Calendar.MONTH);
            month = 2 + 6 * (month / 6);
        // Set times in day first to avoid slip at end of year
        aCal.set(Calendar.HOUR_OF_DAY , aCal.getActualMinimum(Calendar.HOUR_OF_DAY));
        aCal.set(Calendar.MINUTE      , aCal.getActualMinimum(Calendar.MINUTE));
        aCal.set(Calendar.SECOND      , aCal.getActualMinimum(Calendar.SECOND));
        aCal.set(Calendar.MILLISECOND , aCal.getActualMinimum(Calendar.MILLISECOND));
        // resest DAY_OF_MONTH first othewise we may slip rom 31/8 to 01/09
        aCal.set(Calendar.DAY_OF_MONTH, aCal.getActualMinimum(Calendar.DAY_OF_MONTH));
        aCal.set(Calendar.MONTH       , month);
        aCal.set(Calendar.DAY_OF_MONTH, aCal.getActualMaximum(Calendar.DAY_OF_MONTH));
    }

    // Special handling since half year is no Standard DateFormat.
    
    /** 
     * Format given Date using a homegrown Format.
     * 
     * @return a String like "yyyy'H'q" where q is a 0-based half of a year.
     */
    @Override
	public String formatInternal(Date aDate) {
        calcCal.setTime(aDate);
        return Integer.toString(calcCal.get(Calendar.YEAR)) 
             + 'H' 
             + Integer.toString(calcCal.get(Calendar.MONTH) / 6); 
    }

    /** Parse given String using an homegrown format.
     * 
     * @param aDateString must be one created via {@link #formatInternal(Date)},
     *        may be null, reulting in null output
     * @return null when parsing fails, an aligend date, otherwise.
     */
    @Override
	public Date parseInternal(String aDateString) {
        if (aDateString != null)
          try {
            if (aDateString.length() < 6 || 'H' != aDateString.charAt(4)) {
                Logger.info("Failed to parseInternal() '" + aDateString + "'", this);
                return null;
            }
            int year  = Integer.parseInt(aDateString.substring(0,4));
            int half = Integer.parseInt(aDateString.substring(5,6));
            calcCal.set(Calendar.YEAR , year);
            calcCal.set(Calendar.MONTH, 6 * half);
            align(calcCal);
            return calcCal.getTime();
        } catch (NumberFormatException exp) {
            Logger.info("Failed to parseInternal() '" + aDateString + "'", this);
        }
        return null;
    }

    /** 
     * Optimized variant of formatCurrent.
     */
    @Override
	public String formatCurrent() {
        return Integer.toString(cal.get(Calendar.YEAR)) 
        + 'H' 
        + Integer.toString(cal.get(Calendar.MONTH) / 3); 
    }
    
    /** 
     * Return number of half years from start- to endDate (inclusive)
     * 
     * Hope this optimization does not hit back.
     */
     @Override
	public int getNumRanges() {
         calcCal.setTime(start);
         int startMonth = calcCal.get(Calendar.MONTH);
         int startYear  = calcCal.get(Calendar.YEAR);
         calcCal.setTime(end);
         int endMonth = calcCal.get(Calendar.MONTH);
         int endYear  = calcCal.get(Calendar.YEAR);
         return 1 + (endMonth - startMonth + 12 * (endYear - startYear)) / 6;
    }


}
