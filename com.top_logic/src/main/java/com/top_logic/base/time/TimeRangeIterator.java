/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.IDRangeIterator;
import com.top_logic.basic.time.CalendarUtil;

/**
 * An Iterator for TimeRanges with an Start and EndDate.
 * <p>
 * A TimeRange is an Interval of time defined by a start and end time (or Date in Java) inclusively.
 * Due to the nature of time and the limitations of the Java Calendar class Sequences of Time Range
 * may contain gaps. Be aware that start and end date are inclusively, which is not standard
 * for-loop semantic.
 * </p>
 * <p>
 * In addition to its functions as an Iterator this class has many utility functions for date
 * Ranges. This class is designed as lightweight object and therefore not Thread safe.
 * </p>
 * <p>
 * This class is a simple factory for configured TimeRanges, too.
 * </p>
 * 
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class TimeRangeIterator implements Cloneable, IDRangeIterator {

	/** Number of milliseconds per Hour. */
	protected static final long MILLIS_PER_HOUR = 60 * 60 * 1000L;
	
	/**
	 * Number of milliseconds per Day.
	 * 
	 * Is incorrect for days with a leap second, but will work correctly here.
	 */
    protected static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

    /** return value for containsRange() when anIter is before this. */ 
    public static final int BEFORE_COMPLETE = -2;
    
    /** return value for containsRange() when anIter overlaps at the beginning. */ 
    public static final int BEFORE_PARTIAL  = -1;
    
    /** return value for containsRange() when anIter overlaps. */
    public static final int OVERLAP         = 0;

    /** return value for containsRange() when anIter overlaps  at the end. */ 
    public static final int AFTER_PARTIAL   =  1;

    /** return value for containsRange() when anIter iwhen anIter is after this. */ 
    public static final int AFTER_COMPLETE  =  2;

    // variable members
    
    /** The calendar used for the current TimeRange. */
    protected Calendar      cal;
    
    /** An calednar used for internal calculations. */
    protected Calendar      calcCal;

    /** The formatter used to create unique Strings, independent of Locale */
    protected DateFormat    intForm;  

    /** The formatter used to create and parse Human readable Strings, based on Locale */
    protected DateFormat    guiForm;  
    
    /** The formatter used to create sime Human readable Strings  */
    protected DateFormat    simpleForm;  

    /** The first date for the time Ranges. 
     * 
     * Will be aligned to the very first allowed Date for the TimeRange. 
     */
    protected Date          start;

    /** The last date for the Time Ranges. 
     * 
     * Will be aligned to the very last allowed Date for the TimeRange. 
     */
    protected Date          end;

    /**
     * Created via introspection so we need any empty CTor.
     */
    public TimeRangeIterator() {  /* need any empty CTor */ }

    /**
	 * Secondary Constructor to be called by the factory functions.
	 * 
	 * Subclasses must override this to create the needed formatters. The Internal State will be set to
	 * the interval just before aStart.
	 * 
	 * @param loc
	 *        Locale to use for Formatting and for creating the internal Calendar(s)
	 * @param aStart
	 *        first start for the time Ranges. will be aligned to the very first allowed Date for the
	 *        TimeRange.
	 * @param anEnd
	 *        last date for the Time Ranges, will be aligned to the very last allowed Date for the
	 *        TimeRange.
	 * 
	 * @throws IllegalArgumentException
	 *         when anEnd is before start.
	 */
    public void init(Locale loc, Date aStart, Date anEnd) {
        if (anEnd.before(aStart)) {
            throw new IllegalArgumentException(
                "End ("+ anEnd+") before Start (" + anEnd + ")");
        }
        
		this.cal = CalendarUtil.createCalendar(loc);
		this.calcCal = CalendarUtil.clone(cal);
        this.start   = alignToStart(aStart);
        this.end     = alignToEnd  (anEnd);

        resetBefore(aStart);
    }
    
    /** Reset the Iterator to some given Date one interval before given one */
    public void resetBefore(Date aDate) {
        this.cal.setTime(aDate);
        align(this.cal);
        prevInterval();
    }
    
    /** Reset the Iterator to intial state (interval before start) */
    @Override
	public void reset() {
        resetBefore(this.start);
    }

    /** 
     * Return a reasonable String for debugging.
     */
    @Override
	public String toString() {
        return this.getClass() + " ["  + guiForm.format(start)
                               + " - " + guiForm.format(end)
                               + "]:"  + guiForm.format(cal.getTime());
    }

    /** 
     * The Basic cloning should be quite OK 
     */
    @Override
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    /** Return the very first date of the TimeRange.
     *
     * Will be aligned to the very first allowed Date for the TimeRange. 
     */
    public Date getStart() {
       return start;
    }

   /** Return the very last date of the TimeRange.
    *
    * Will be aligned to the very last allowed Date for the TimeRange. 
    */
    public Date getEnd() {
        return end;
    }
    
    /**
	 * Format given Date using an internal format.
	 * 
	 * The returned String will result in an equivalent (but not equal !) String when given to parse.
	 * The String must only contain numbers and eventually ASCII characters. It Should be as short as
	 * possible. It must be smaller that 24 characters (Which would be a complete ISO DateTime format
	 * 2005-01-21:09:41:20:999)
	 */
    public String formatInternal(Date aDate) {
        return intForm.format(aDate); 
    }

    /** 
     * Format current Date using an internal format.
     * 
     * Subclasses may override this for performance reasons.
     * 
     * @return null in case {@link #current()} does.
     */
    public String formatCurrent() {
        Date current = current();
        if (current != null) {
            return intForm.format(current);
        }
        return null;
    }

    /**
	 * Parse given String using an internal format.
	 * 
	 * @param aDateString
	 *        must be one created via {@link #formatInternal(Date)}, may be null, resulting in null output
	 * @return null when parsing fails, an aligned date, otherwise.
	 */
    public Date parseInternal(String aDateString) {
        if (aDateString != null)
          try {
            Date result = intForm.parse(aDateString);
            return align(result);
        } catch (ParseException exp) {
            Logger.info("Failed to parseInternal() '" + aDateString + "'", this);
        }
        return null;
    }

    /**
	 * Return a Formatter to be used for simple Human readable, localized dates.
	 * 
	 * This formatter must not be used to parse date input since the values are usually incomplete
	 * 
	 * Should work well with RangedFormConstraint.
	 */
    public DateFormat getSimpleFormat() {
        return simpleForm; 
    }

    /**
	 * Return a Formatter to be used for Human readable, localized dates.
	 * 
	 * Should work well with RangedFormConstraint.
	 */
    public DateFormat getDateFormat() {
        return guiForm;
    }
    
    /** Return current, internal date.
     *
     * @return null when current state is out of defined interval. 
     */
    public Date current() {
        Date result = cal.getTime();
        if (result.after (end) 
         || result.before(start)) {
            return null;    // Out of Range
        }
        return result;
    }
    
    /** 
     * Advance to next interval.
     * 
     * @return Date of the next interval or null when out of range. 
     */
    public Date next() {
        nextInterval();
        return current();
    }

    /** 
     * Go back to previous interval.
     * 
     * @return Date of the previous interval or null when out of range. 
     */
    public Date previous() {
        prevInterval();
        return current();
    }
    
    /**
	 * Skip (forward or backward) given number of intervals
	 * 
	 * Subclasses may override this with optimized variants.
	 */
    public Date skip(int to) {
        while (to > 0) {
            nextInterval();
            to--;
        }
        while (to < 0) {
            prevInterval();
            to++;
        }
        return current();
    }
    
    /**
	 * Check if both dates are in same interval.
	 * 
	 * One may wish to override this function for a faster implementation.
	 * 
	 * @param aDate1
	 *        must not be null.
	 * @param aDate2
	 *        must not be null.
	 */
    public boolean equalsRange(Date aDate1, Date aDate2) {
        aDate1 = align(aDate1);
        aDate2 = align(aDate2);
        return aDate1.equals(aDate2);
    }

    /** 
     * Normalize aDate to be in some well defined "middle" of an interval.
     */
    public Date align(Date aDate) {
        calcCal.setTime(aDate);
        align(calcCal);
        return calcCal.getTime();
    }

    /** 
     * Check if given date is in current interval.
     */
    public boolean contains(Date aDate) {
        calcCal.setTime(aDate);
        align(calcCal);
        return calcCal.getTime().equals(current());
    }

    /**
     * @param aDate the date to check
     * @return <code>true</code> if the given data is contained in the
     *         TimeRange defined by this class.
     */
    public boolean isContainedInRange (Date aDate) {
        if (aDate == null) {
            return false;
        }
        return aDate.compareTo(this.getStart()) >= 0
               && aDate.compareTo(this.getEnd()) <= 0; 
    }
    /** 
     * Check if (complete) TimeRangeIterator is contained in this one.
     */
    public boolean containsRange(TimeRangeIterator anIter) {
        return anIter.getStart().compareTo(this.getStart()) >= 0
            && anIter.getEnd()  .compareTo(this.getEnd())   <= 0;
    }

    /** 
     * Check if (complete) TimeRangeIterator is before in this one.
     * 
     * (Not including overlapping Ranges).
     */
    public boolean before(TimeRangeIterator anIter) {
        return overlaps(anIter) == BEFORE_COMPLETE;
    }

    /** 
     * Check if (complete) TimeRangeIterator is after in this one.
     * 
     * (Not including  overlapping Ranges).
     */
    public boolean after(TimeRangeIterator anIter) {
        return overlaps(anIter) == AFTER_COMPLETE;
    }

    /** 
     * Check complex overlapping of time ranges.
     * 
     * @return  BEFORE_COMPLETE (-2) when anIter is before this.
     *          AFTER_COMPLETE  ( 2) when anIter is after this.
     *          CONTAINS        ( 0) when anIter is completely contained in this (same as containsRange)
     *          BEFORE__PARTIAL (-1) when anIter overlaps at the beginning.
     *          AFTER_PARTIAL   ( 1) when anIter overlaps at the end.
     * 
     *          &lt; 0 if anIter is before this in some way
     *          &gt; 0 if anIter is after this in some way
     */
    public int overlaps(TimeRangeIterator anIter) {
        if (anIter.getEnd().before(this.getStart()))
            return AFTER_COMPLETE ;
        if (anIter.getStart().after(this.getEnd()))
            return BEFORE_COMPLETE;
        // Must be some kind of overlapping here 
        if (anIter.getStart().before(this.getStart())
         && anIter.getEnd()  .before(this.getEnd()))
            return BEFORE_PARTIAL;
        if (anIter.getEnd()  .after(this.getEnd())
         && anIter.getStart().after(this.getStart()))
            return AFTER_PARTIAL;

        return OVERLAP;
    }

    /** 
     * Return number of Ranges from start- to endDate (inclusive)
     */
    public abstract int getNumRanges();

    /** 
     * Advance internal calendar to next interval. 
     */
    protected abstract void nextInterval();

    /** 
     * Set internal calendar to previous interval. 
     */
    protected abstract void prevInterval();

    /** 
     * Normalize Calendar to be in some well defined "middle" of an interval.
     */
    protected abstract void align(Calendar aCal);

    /** 
     * Normalize Date to be the first date for the Interval it is contained. 
     */
    public abstract Date alignToStart(Date aDate);

    /** 
     * Normalize Date to be the last date for the Interval it is contained. 
     */
    public abstract Date alignToEnd(Date aDate);

    /**
	 * Factory method to create configured TimeRanges.
	 * 
	 * @param key
	 *        A Key as configured in the configuration.
	 * @param loc
	 *        Locale to use for Formatting and for creating the internal Calendar(s)
	 * @param aStart
	 *        first start for the time Ranges. will be aligned to the very first allowed Date for the
	 *        TimeRange.
	 * @param anEnd
	 *        last date for the Time Ranges, will be aligned to the very last allowed Date for the
	 *        TimeRange.
	 * 
	 * @return null when key is not configured or creation fails.
	 * 
	 * @throws IllegalArgumentException
	 *         when anEnd is before start.
	 */
    public static TimeRangeIterator createTimeRange(String key, Locale loc, Date aStart, Date anEnd) {
        
		Class<?> theClass = TimeRangeService.getInstance().getTimeRange(key);
        if (theClass == null) {
			Logger.warn("No TimeRangeIterator configured for '" + key + "'", TimeRangeIterator.class);
            return null;
        }
        TimeRangeIterator result;
        try {
            result = (TimeRangeIterator) theClass.newInstance();
        } catch (Exception ex) {
			Logger.warn("Failed to create TimeRangeIterator for '" + key + "' " + theClass.getName(), ex, TimeRangeIterator.class);
            return null;
        }
        result.init(loc, aStart, anEnd);
        return result;
    }
    
    /** 
     * Return a set of valid time ranges (Strings).
     */
	public static Set<String> getValidTimeRanges() {
		return TimeRangeService.getInstance().getTimeRanges().keySet();
    }
    
    /** 
     * Check if given String is a configured TimeRange
     */
    public static boolean isValidTimeRange(String anID) {
		return TimeRangeService.getInstance().getTimeRanges().containsKey(anID);
    }
    
    /** 
     * Use a this TimeRangeIterator to setup coordinates at String[].
     * 
     * This will {@link #reset()} this iterator
    */ 
    @Override
	public Object[] createCoords() {
        String[] result = new String[getNumRanges()];
        int      i      = 0;
        reset();
        while (null != next())
            result[i++] = formatCurrent();
        
        return result;
    }

   /** 
    * Use a TimeRangeIterator to setup coordinates at String[] 
    * 
    * @deprecated this is now a Member function
    */ 
    @Deprecated
	public static Object[] createTimeCoords(TimeRangeIterator iter) {
        return iter.createCoords();
    }


    @Override
	public String getIDFor(Object anObject) {
        return formatInternal((Date)anObject);
    }

    /** 
     * @see com.top_logic.basic.col.IDRangeIterator#getUIStringFor(java.lang.Object)
     */
    @Override
	public String getUIStringFor(Object anObject) {
        return getDateFormat().format(((Date)anObject));
    }

    /** 
     * @see com.top_logic.basic.col.IDRangeIterator#nextObject()
     */
    @Override
	public Object nextObject() {
        return next();
    }
}
