/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.common.period;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.data.time.RegularTimePeriod;

import com.top_logic.base.time.TimePeriod;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class BusinessYearPeriodBase extends RegularTimePeriod {

    
    /** The first millisecond. */
    private long firstMillisecond;

    /** The last millisecond. */
    private long lastMillisecond;

    protected TimePeriod period;
    protected final TimeZone timezone;
    protected final Locale   locale;
    
    
    public BusinessYearPeriodBase(Date time, TimeZone zone, Locale locale) {
        this.timezone = zone;
        this.locale = locale;
        this.period = this.createPeriod(time); 
		peg(createCalendar(time));
    }

	protected final Calendar createCalendar(Date time) {
		Calendar calendar = CalendarUtil.createCalendar(timezone, locale);
        calendar.setTime(time);
		return calendar;
	}

    protected BusinessYearPeriodBase(TimePeriod period, TimeZone zone, Locale locale) {
        this.timezone = zone;
        this.locale = locale;
        this.period = period;
		peg(createCalendar(this.period.getBegin()));
    }
    
    public BusinessYearPeriodBase(Date time, TimeZone zone) {
        this(time, zone, TLContext.getLocale());
    }
    
    protected abstract TimePeriod createPeriod(Date time);

    @Override
    public final long getFirstMillisecond(Calendar calendar) {
        calendar.setTime(this.period.getBegin());
        return calendar.getTime().getTime();
    }

    @Override
    public final long getLastMillisecond(Calendar calendar) {
        calendar.setTime(this.period.getEnd());
        return calendar.getTime().getTime();
    }
    
    @Override
    public final long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    @Override
    public final long getLastMillisecond() {
        return this.lastMillisecond;
    }

    @Override
    public final void peg(Calendar calendar) {
        this.firstMillisecond = getFirstMillisecond(calendar);
        this.lastMillisecond = getLastMillisecond(calendar);
    }

    @Override
    public abstract RegularTimePeriod previous();

    @Override
    public abstract RegularTimePeriod next();

    @Override
    public final long getSerialIndex() {
        return this.period.getBegin().getTime();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof BusinessYearPeriodBase) {
                BusinessYearPeriodBase target = (BusinessYearPeriodBase) obj;
                return Utils.equals(target.period.getBegin(), this.period.getBegin());
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }

    }

    @Override
    public final int hashCode() {
        return this.period.getBegin().hashCode();
    }

    @Override
	public final int compareTo(Object o1) {
        int result;

        // CASE 1 : Comparing to another Quarter object
        // --------------------------------------------
        if (o1 instanceof BusinessYearPeriodBase) {
            BusinessYearPeriodBase q = (BusinessYearPeriodBase) o1;
            return this.period.getBegin().compareTo(q.period.getBegin());
        }

        // CASE 2 : Comparing to another TimePeriod object
        // -----------------------------------------------
        else if (o1 instanceof RegularTimePeriod) {
            // more difficult case - evaluate later...
            result = 0;
        }

        // CASE 3 : Comparing to a non-TimePeriod object
        // ---------------------------------------------
        else {
            // consider time periods to be ordered after general objects
            result = 1;
        }

        return result;

    }
}
