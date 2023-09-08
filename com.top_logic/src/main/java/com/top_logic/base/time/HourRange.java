/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.top_logic.basic.time.CalendarUtil;

/**
 * A TimeRangeIterator for hours
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class HourRange extends TimeRangeIterator {

    /**
     * Created via introspection so I need an empty CTor.
     */
	public HourRange() {
		super();
	}

	/**
     * Allow direct construction as well.
     */
    public HourRange(Locale loc, Date aStart, Date anEnd) {
        init(loc, aStart, anEnd);
    }
    
    /**
     * @see com.top_logic.base.time.TimeRangeIterator#init(java.util.Locale, java.util.Date, java.util.Date)
     */
    @Override
	public void init( Locale loc, Date aStart, Date aAnEnd ) {
    	super.init(loc, aStart, aAnEnd);

		intForm = getHourRangeFormat();
        // TODO tbe, kha: Maybe not the right format for hours.
        guiForm    = CalendarUtil.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc);
        simpleForm = CalendarUtil.newSimpleDateFormat("HH", loc);
    }
    
	/**
	 * @see com.top_logic.base.time.TimeRangeIterator#align(java.util.Calendar)
	 */
	@Override
	protected void align( Calendar aCal ) {
		int max  = aCal.getActualMaximum(Calendar.MINUTE);
        int min  = aCal.getActualMinimum(Calendar.MINUTE);
        int middle = 1 + (max - min) >> 1; //  / 2
        aCal.set(Calendar.MINUTE     , middle);
        aCal.set(Calendar.SECOND     , aCal.getActualMinimum(Calendar.SECOND));
        aCal.set(Calendar.MILLISECOND, aCal.getActualMinimum(Calendar.MILLISECOND));
	}

	/**
	 * @see com.top_logic.base.time.TimeRangeIterator#alignToStart(java.util.Date)
	 */
	@Override
	public Date alignToStart( Date aDate ) {
		calcCal.setTime(aDate);
		calcCal.set(Calendar.MINUTE     ,calcCal.getActualMinimum(Calendar.MINUTE));
		calcCal.set(Calendar.SECOND     , calcCal.getActualMinimum(Calendar.SECOND));
		calcCal.set(Calendar.MILLISECOND, calcCal.getActualMinimum(Calendar.MILLISECOND));
		return calcCal.getTime();
	}

	/**
	 * @see com.top_logic.base.time.TimeRangeIterator#alignToEnd(java.util.Date)
	 */
	@Override
	public Date alignToEnd( Date aDate ) {
		calcCal.setTime(aDate);
        calcCal.set(Calendar.MINUTE     ,calcCal.getActualMaximum(Calendar.MINUTE));
        calcCal.set(Calendar.SECOND     , calcCal.getActualMaximum(Calendar.SECOND));
        calcCal.set(Calendar.MILLISECOND, calcCal.getActualMaximum(Calendar.MILLISECOND));
        return calcCal.getTime();
	}


	/**
	 * @see com.top_logic.base.time.TimeRangeIterator#getNumRanges()
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
        return 1 + (int)Math.round(((double)diff) / ((double)MILLIS_PER_HOUR));
	}

	/**
	 * @see com.top_logic.base.time.TimeRangeIterator#nextInterval()
	 */
	@Override
	protected void nextInterval() {
		cal.add(Calendar.HOUR_OF_DAY, 1);
    }

	/**
	 * @see com.top_logic.base.time.TimeRangeIterator#prevInterval()
	 */
	@Override
	protected void prevInterval() {
		cal.add(Calendar.HOUR_OF_DAY, -1);
	}

	/** Unified Format used internally by this HourRange */
	public static DateFormat getHourRangeFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyy-DDD:HH", Locale.US);
	}

}

