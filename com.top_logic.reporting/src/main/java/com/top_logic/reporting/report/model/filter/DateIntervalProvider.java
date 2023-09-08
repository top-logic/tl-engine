/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.top_logic.base.time.BusinessYearConfiguration;
import com.top_logic.base.time.MonthlyTimePeriod;
import com.top_logic.base.time.TimePeriod;
import com.top_logic.base.time.TimeRangeIterator;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;
import com.top_logic.util.TLContext;

/**
 * The DateIntervalProvider returns intervals specified by two borders and a granuarity.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class DateIntervalProvider extends IntervalProvider{

    private final boolean useBusinessYear;
    
    public DateIntervalProvider(Long aGranularity, boolean useBusinessYear) {
        super(aGranularity);
        this.useBusinessYear = useBusinessYear;
    }
    
	/**
	 * Creates {@link DateInterval}s from a given startDate, endDate and a granularity in hours.
	 * 
	 * @param aStart
	 *        the start of the interval that is to be subdivided
	 * @param anEnd
	 *        the end of the interval that is to be subdevided
	 * @return a {@link List} of {@link DateInterval}s. Can not return <code>null</code>.
	 */
	@Override
	public List getIntervals(Object aStart, Object anEnd) {
	    Long theGranularity = (Long) this.getGranularity();
		List theIntervals = new ArrayList();
		Locale theLoc = TLContext.getLocale();
		Date theStart;
		Date theEnd;
		if(aStart == null && anEnd == null) {
			theEnd = new Date();
			theStart = DateUtil.addHours(theEnd, -theGranularity.intValue());
		}
		else if(aStart == null && anEnd != null) {
			theEnd = (Date) anEnd;
			theStart = DateUtil.addHours(theEnd, -theGranularity.intValue());
		}
		else if(anEnd == null && aStart != null) {
			theStart = (Date) aStart;
			theEnd = DateUtil.addHours(theStart, theGranularity.intValue());
		}
		else {
			theStart = (Date)aStart;
			theEnd = (Date)anEnd;
		}

		if (this.useBusinessYear) {
     		if (theGranularity == TimeRangeFactory.HALFYEAR_INT) {
    		    return createIntervals(theStart, theEnd, 6);
    		}
    		else if (theGranularity == TimeRangeFactory.QUARTER_INT) {
    		    return createIntervals(theStart, theEnd, 3);
    		}
    		else if (theGranularity == TimeRangeFactory.YEAR_INT) {
    		    return createIntervals(theStart, theEnd, 12);
    		}
		}
		

		TimeRangeIterator iter = TimeRangeFactory.getInstance().getTimeRange(theGranularity, theLoc, theStart, theEnd);
		Date period = iter.next();
		Date periodStart;
		Date periodEnd; 
		    
		while (period != null) {
		    periodStart = iter.alignToStart(period);
		    periodEnd   = iter.alignToEnd(period);
		    theIntervals.add(new DateInterval(periodStart, periodEnd));
		    period = iter.next();
		}
				
		return theIntervals;
	}
	
    private List createIntervals(Date theStart, Date theEnd, int intervalLength) {
        List<Interval> theIntervals = new ArrayList<>();
		TimePeriod period = new MonthlyTimePeriod(CalendarUtil.createCalendar(theStart),
			BusinessYearConfiguration.getBeginMonth(), BusinessYearConfiguration.getBeginDay(), intervalLength);
        
        Date periodStart = period.getBegin();
        Date periodEnd   = period.getEnd();
        while (DateUtil.overlaps(theStart, theEnd, periodStart, periodEnd)) {
            theIntervals.add(new DateInterval(periodStart, periodEnd));
            period = period.getNextPeriod();
            periodStart = period.getBegin();
            periodEnd   = period.getEnd();
        }
        return theIntervals;
    }
}
