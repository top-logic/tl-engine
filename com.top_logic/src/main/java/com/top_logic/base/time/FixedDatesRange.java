/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.top_logic.basic.time.CalendarUtil;

/**
 * This is a TimeRangeIterator for some fixed number of dates.
 * 
 * The time Range is defined by the Time from the one Date
 * to the next. Might be used for things like Date- (Milestone-) 
 * Based PerformanceIndicators or such. 
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class FixedDatesRange extends TimeRangeIterator {

    /** The fixed dates we actually care for */
    protected Date[] fixedDates;
    
    /** index into fixedDates identifying the current date */
    protected int currDate;
    
    /**
     * @param someDates must be in ascending order.
     */
    public FixedDatesRange(Locale loc, Date[] someDates) {
        super();
        int len = someDates.length;
        for (int i=1; i< len; i++) {
            if (someDates[i].before(someDates[i-1])) {
                throw new IllegalArgumentException(
                    "Dates must be in ascending order");
            }
        }
        fixedDates = someDates;
        currDate   = -1;
        this.start   = someDates[0];
        this.end     = someDates[len - 1];
		this.cal = CalendarUtil.createCalendar(loc);
		this.calcCal = CalendarUtil.clone(cal);
		intForm = CalendarUtil.newSimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS", Locale.US);
        guiForm    = intForm;
        simpleForm = intForm;
    }
    
    /**
     * Secondary Constructor to be called by the factory functions.
     * 
     * Subclasses must override this to create the needed formatters.
     * The Internal State will be set to the interval just before aStart.
     *       
     * @param loc Locale to use for Formatting and for creting the internal
     *        Calendar(s)
     * @param aStart first start for the time Ranges. 
     *         will be aligned to the very first allowed Date for the TimeRange. 
     * @param anEnd last date for the Time Ranges, 
     *         will be aligned to the very last allowed Date for the TimeRange. 
     * 
     * @throws IllegalArgumentException when anEnd is before start.
     */
    @Override
	public void init(Locale loc, Date aStart, Date anEnd) {
        throw new IllegalArgumentException(
            "FixedDatesRange cannot be created via factory");
    }

    // overriden abstract methods.
    
    /** 
     * Advance internal calendar to next interval. 
     */
    @Override
	protected void nextInterval() {
        currDate ++;
        if (currDate > 0 && currDate < fixedDates.length)
            this.cal.setTime(fixedDates[currDate]);

    }

    /** 
     * Set internal calendar to previous interval. 
     */
     @Override
	protected void prevInterval() {
        currDate --;
        if (currDate > 0 && currDate < fixedDates.length)
            this.cal.setTime(fixedDates[currDate]);
    }

     /** all Alignment methods actually fall back to this method */
     protected int alignFixed(Date aDate) {
         int index = Arrays.binarySearch(fixedDates, aDate);
         if (index < 0) { // quite normal
             index = -index -2;
             if (index < 0) // befor first date
                 return 0;
         }
         if (index < fixedDates.length) {
             return index;
         }
         else { // last date
             return fixedDates.length - 1;
         }
     }

     /** 
      * Normalize Calendar to be one of the fixed dates.
      */
    @Override
	protected void align(Calendar aCal) {
        int index = alignFixed(aCal.getTime());
        aCal.setTime(fixedDates[index]);
    }

    /** 
     * Normalize aDate to be in some well defined "middle" of an interval.
     */
    @Override
	public Date align(Date aDate) {
        int index = alignFixed(aDate);
        return fixedDates[index];
    }
    
    /** 
     * Normalize Date to be the first date for the Interval it is contained. 
     */
    @Override
	public Date alignToStart(Date aDate) {
        int index = alignFixed(aDate);
        return fixedDates[index];
    }

    /** 
     * Normalize Date to be the last date for the Interval it is contained. 
     */
    @Override
	public Date alignToEnd(Date aDate) {
        int index =  1 + alignFixed(aDate);
        if (index < fixedDates.length) {
            return new Date(fixedDates[index].getTime()-1);
        }
        else
            return new Date(fixedDates[fixedDates.length - 1].getTime()+2);
    }

    /** Return current, internal date.
    *
    * @return null when current state is out of defined interval. 
    */
   @Override
public Date current() {
       if (currDate < 0 || currDate >= fixedDates.length)
           return null;
       return fixedDates[currDate];
   }

   /** 
    * Return number of Ranges which is just fixedDates.length.
    */
    @Override
	public int getNumRanges() {
        return fixedDates.length;
    }
}
