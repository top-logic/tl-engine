/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.top_logic.base.time.DayRange;
import com.top_logic.base.time.HalfYearRange;
import com.top_logic.base.time.HourRange;
import com.top_logic.base.time.MonthRange;
import com.top_logic.base.time.QuarterYearRange;
import com.top_logic.base.time.TimeRangeIterator;
import com.top_logic.base.time.WeekRange;
import com.top_logic.base.time.YearRange;
import com.top_logic.basic.Logger;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.util.DateConstants;

/**
 * TODo FSC/KHA this class overlaps with {@link TimeRangeIterator#createTimeRange(String, Locale, Date, Date)}
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class TimeRangeFactory {

    // TODO check is this could be an enum ..
    public static final Long     HOUR_INT    = Long.valueOf(DateConstants.HOUR);        // 1
    public static final Long     DAY_INT     = Long.valueOf(DateConstants.DAY);         // 24
    public static final Long     WEEK_INT    = Long.valueOf(DateConstants.WEEK);        // 168 
    public static final Long     MONTH_INT   = Long.valueOf(DateConstants.MONTH);       // 720
    public static final Long     QUARTER_INT = Long.valueOf(DateConstants.QUARTER);     // 2160
    public static final Long     HALFYEAR_INT= Long.valueOf(DateConstants.HALFYEAR);    // 4320 
    public static final Long     YEAR_INT    = Long.valueOf(DateConstants.YEAR);        // 8760

	private static TimeRangeFactory	instance;

	private HashMap<Long,Constructor<? extends TimeRangeIterator>>   constructors;

	private TimeRangeFactory() {
		constructors = new HashMap<>();

		try {
			constructors.put( HOUR_INT,     HourRange       .class.getConstructor() );
			constructors.put( DAY_INT,      DayRange        .class.getConstructor() );
			constructors.put( WEEK_INT,     WeekRange       .class.getConstructor() );
			constructors.put( MONTH_INT,    MonthRange      .class.getConstructor() );
			constructors.put( QUARTER_INT,  QuarterYearRange.class.getConstructor() );
			constructors.put( HALFYEAR_INT, HalfYearRange   .class.getConstructor() );
			constructors.put( YEAR_INT,     YearRange       .class.getConstructor() );

		}
		catch (NoSuchMethodException ex) {
			Logger.error( "Cannot set up class constructors!", ex, this );
			throw new ReportingException( TimeRangeFactory.class, "setupError", ex );
		}
	}

	public static synchronized TimeRangeFactory getInstance() {
		if (instance == null) {
			instance = new TimeRangeFactory();
		}
		return instance;
	}

	public TimeRangeIterator getTimeRange( Long aGranularity, Locale aLocale, Date aStartDate,
			Date anEndDate ) {

		Constructor<? extends TimeRangeIterator> theConstructor =  this.constructors.get( aGranularity );

		if (theConstructor == null) {
			throw new ReportingException( TimeRangeFactory.class, "unknownRange");
		}

		try {
			TimeRangeIterator theIterator = theConstructor.newInstance();
			theIterator.init( aLocale, aStartDate, anEndDate );
			return theIterator;
		}
		catch (Exception ex) {
			throw new ReportingException(TimeRangeFactory.class,
					"unexpectedError", ex);
		}
	}
}
