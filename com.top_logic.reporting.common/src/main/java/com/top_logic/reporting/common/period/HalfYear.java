/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.common.period;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.chart.date.SerialDate;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Year;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;

/**
 * Defines a half-year (in a given year). The range supported is half-year 1
 * 1900 to half-year 2 9999.  This class is immutable, which is a requirement 
 * for all {@link RegularTimePeriod} subclasses.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class HalfYear extends RegularTimePeriod {

    public static final int HALF_YEAR_1 = 1;
    public static final int HALF_YEAR_2 = 2;

    /** The year to which this half-year belongs to. */
    private Year year;
    /** The half-year (1-2). */
    private int  halfYear;
    
    /** The first millisecond. */
    private long firstMillisecond;
    /** The last millisecond. */
    private long lastMillisecond;

    /** Creates a {@link HalfYear} with the given
     *  date. */
    public HalfYear(Date aDate) {
		this(aDate, TimeZones.systemTimeZone());
    }

    /** Creates a {@link HalfYear} with the given
     *  date and timezone. This constructor is needed by JFreeChart*/
    public HalfYear(Date aDate, TimeZone zone) {
		this(aDate, zone, ThreadContext.getLocale());
    }
    
    public HalfYear(Date aDate, TimeZone zone, Locale locale) {
		this(CalendarUtil.createCalendar(aDate, zone, locale));
	}

	private HalfYear(Calendar calendar) {
		this.halfYear = getHalfYearFor(calendar.getTime(), calendar);
		this.year = new Year(calendar.get(Calendar.YEAR));
		year.peg(calendar);
		peg(calendar);
    }
    
    /** Creates a {@link HalfYear} with the given
     *  half-year and year. */
    public HalfYear(int aHalfYear, int aYear) {
        this(aHalfYear, new Year(aYear));
    }

    /** Creates a {@link HalfYear} with the given
     *  half-year and year. */
    public HalfYear(int aHalfYear, Year aYear) {
		Calendar calendar = CalendarUtil.createCalendar();
        this.year     = aYear;
		year.peg(calendar);
        this.halfYear = aHalfYear;
		peg(calendar);
    }

    /** See {@link #getHalfYearFor(Date, Calendar)}. */
    public static int getHalfYearFor(Date aDate) {
		Calendar calendar = CalendarUtil.createCalendar(aDate);
        
        return getHalfYearFor(aDate, calendar);
    }
    
    /**
     * This method returns the half-year as int ({@link #HALF_YEAR_1} or
     * {@link #HALF_YEAR_2}) for the given date.
     * 
     * @param aDate
     *        A date. Must not be <code>null</code>.
     * @param aCalendar
     *        A calendar. Must not be <code>null</code>.
     */
    public static int getHalfYearFor(Date aDate, Calendar aCalendar) {
        int monthAsInt = aCalendar.get(Calendar.MONTH);
        if (monthAsInt <= 6) {
            return HALF_YEAR_1;
        } else {
            return HALF_YEAR_2;
        }
    }

    public Year getYear() {
        return this.year;
    }
    
    public int getHalfYear() {
        return this.halfYear;
    }
    
    @Override
	public long getFirstMillisecond(Calendar calendar) {
		calendar.clear();
		calendar.set(this.year.getYear(), (this.halfYear * 6) - 5, 1, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
    }

    @Override
	public long getLastMillisecond(Calendar calendar) {
		int monthAsInt = this.halfYear * 6;
		int lastDayAsInt = SerialDate.lastDayOfMonth(monthAsInt, this.year.getYear());
		calendar.set(this.year.getYear(), monthAsInt, lastDayAsInt, 23, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTimeInMillis();
    }

    @Override
	public long getSerialIndex() {
        return this.year.getYear() * 2L + this.halfYear;
    }

    @Override
	public RegularTimePeriod next() {
        if (this.halfYear == HALF_YEAR_1) {
            return new HalfYear(HALF_YEAR_2, this.year);
        }
        else {
            Year nextYear = (Year) this.year.next();
            if (nextYear != null) {
                return new HalfYear(HALF_YEAR_1, nextYear);
            }
        }
        return null;
    }

    @Override
	public RegularTimePeriod previous() {
        if (this.halfYear == HALF_YEAR_2) {
            return new HalfYear(HALF_YEAR_1, this.year);
        }
        else {
            Year prevYear = (Year) this.year.previous();
            if (prevYear != null) {
                return new HalfYear(HALF_YEAR_2, prevYear);
            }
        }
        
        return null;
    }

    @Override
	public int compareTo(Object o1) {
        int result;

        if (o1 instanceof HalfYear) {
            HalfYear halfYear = (HalfYear) o1;
            result = this.year.getYear() - halfYear.getYear().getYear();
            if (result == 0) {
                result = this.halfYear - halfYear.getHalfYear();
            }
        }

        else if (o1 instanceof RegularTimePeriod) {
            result = 0;
        }

        else {
            // Consider time periods to be ordered after general objects
            result = 1;
        }

        return result;
    }

    @Override
	public long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    @Override
	public long getLastMillisecond() {
        return this.lastMillisecond;
    }

    @Override
	public void peg(Calendar aCalendar) {
        this.firstMillisecond = getFirstMillisecond(aCalendar);
        this.lastMillisecond  = getLastMillisecond(aCalendar);
    }

}

