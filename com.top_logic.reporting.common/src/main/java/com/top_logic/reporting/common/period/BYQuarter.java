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

import com.top_logic.base.time.BusinessYearConfiguration;
import com.top_logic.base.time.Quarter;
import com.top_logic.base.time.TimePeriod;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.TLContext;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class BYQuarter extends BusinessYearPeriodBase {
    
    public BYQuarter(Date time, TimeZone zone, Locale locale) {
        super(time, zone, locale);
    }

    private BYQuarter(TimePeriod period, TimeZone zone, Locale locale) {
        super(period, zone, locale);
    }
    
    public BYQuarter(Date time, TimeZone zone) {
        this(time, zone, TLContext.getLocale());
    }

    @Override
    protected TimePeriod createPeriod(Date time) {
		return new Quarter(createCalendar(time), BusinessYearConfiguration.getBeginMonth(),
			BusinessYearConfiguration.getBeginDay());
    }
    
    @Override
    public RegularTimePeriod previous() {
        return new BYQuarter(DateUtil.addSeconds(this.period.getBegin(), -1), this.timezone, this.locale);
    }

    @Override
    public RegularTimePeriod next() {
        return new BYQuarter(this.period.getNextPeriod(), this.timezone, this.locale);
    }
    
    @Override
    public String toString() {
		Date periodBegin = this.period.getBegin();
		Calendar cal = createCalendar(periodBegin);
		return "Q " + BusinessYearConfiguration.getQuarter(periodBegin, CalendarUtil.clone(cal)) + "/"
			+ BusinessYearConfiguration.getYear(cal) + " BY";
    }
}
