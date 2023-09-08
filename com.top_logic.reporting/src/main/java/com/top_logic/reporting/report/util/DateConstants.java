/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import java.text.DateFormat;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.reporting.common.format.ReportingDateFormat;
import com.top_logic.reporting.common.period.BYHalfYear;
import com.top_logic.reporting.common.period.BYQuarter;
import com.top_logic.reporting.common.period.BYYear;
import com.top_logic.reporting.common.period.HalfYear;
import com.top_logic.util.Resources;

/**
 * This interface contains some useful constants for date granularities
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class DateConstants {

	/** int representation of date ranges */
	/** the int representation of an hour (just for consistency) */
	public static final int					HOUR			= 1;
	/** the int representation of a day in hours */
	public static final int					DAY				= 24;
	/** the int representation of a week in hours */
	public static final int					WEEK			= 24 * 7; // 168
	/** the int representation of a month in hours */
	public static final int					MONTH			= DAY * 30; // 720
	/** the int representation of a quarter in hours */
	public static final int					QUARTER			= DAY * 90; // 2160
	/** the int representation of a halfyear in hours */
	public static final int				    HALFYEAR		= DAY * 90 * 2; // 4320
	/** the int representation of a year in hours */
	public static final int					YEAR			= DAY * 365; // 8760

	public static DateFormat getDateFormat(int aDateRange, boolean useBusinessYear) {
		switch (aDateRange) {
    		case HOUR:
				return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm");
			case DAY:
				return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd");
			case WEEK:
				String cw = Resources.getInstance().getString(I18NConstants.CALENDAR_WEEK);
				return CalendarUtil.newSimpleDateFormat("'" + cw + "' ww-yyyy");
			case MONTH:
				return CalendarUtil.newSimpleDateFormat("MMM yyyy");
            case QUARTER:
                return useBusinessYear ? ReportingDateFormat.getQuarterFormatBusinessYear() : ReportingDateFormat.getQuarterFormatBusinessYear();
            case HALFYEAR:
                return useBusinessYear ? ReportingDateFormat.getHalfYearFormatBusinessYear() : ReportingDateFormat.getHalfYearFormat();
            case YEAR:
                return useBusinessYear ? ReportingDateFormat.getYearFormatBusinessYear() : ReportingDateFormat.getYearFormat();
			default:
			    throw new IllegalArgumentException("Unsupported range " + aDateRange);
		}
	}
	
	public static DateFormat getDateFormat(Class aPeriod) {
        if(aPeriod.equals(Hour.class)) {
			return CalendarUtil.newSimpleDateFormat("dd");
        }
		if(aPeriod.equals(Day.class)) {
			return CalendarUtil.newSimpleDateFormat("dd");
		}
		else if(aPeriod.equals(Week.class)) {
			String cw = Resources.getInstance().getString(I18NConstants.CALENDAR_WEEK);
			return CalendarUtil.newSimpleDateFormat("'" + cw + "' ww-yyyy");
		}
		else if(aPeriod.equals(Month.class)) {
			return CalendarUtil.newSimpleDateFormat("MMM");
		}
        else if(aPeriod.equals(Quarter.class)) {
            return ReportingDateFormat.getQuarterFormat(); 
        }
        else if (aPeriod.equals(BYQuarter.class)) {
            return ReportingDateFormat.getQuarterFormatBusinessYear();
        }
        else if(aPeriod.equals(HalfYear.class)) {
            return ReportingDateFormat.getHalfYearFormat();
        }
        else if (aPeriod.equals(BYHalfYear.class)) {
            return ReportingDateFormat.getHalfYearFormatBusinessYear();
        }
        else if(aPeriod.equals(Year.class)) {
            return ReportingDateFormat.getYearFormat();
        }
        else if (aPeriod.equals(BYYear.class)) {
            return ReportingDateFormat.getYearFormatBusinessYear();
        }
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm");
	}
	
	public static DateFormat getDateFormatForTooltip(Class aPeriod) {
        if(aPeriod.equals(Hour.class)) {
			return CalendarUtil.newSimpleDateFormat("dd");
        }
        if(aPeriod.equals(Day.class)) {
			return CalendarUtil.newSimpleDateFormat("dd");
        }
        else if(aPeriod.equals(Week.class)) {
			String cw = Resources.getInstance().getString(I18NConstants.CALENDAR_WEEK);
			return CalendarUtil.newSimpleDateFormat("'" + cw + "' ww-yyyy");
        }
        else if(aPeriod.equals(Month.class)) {
			return CalendarUtil.newSimpleDateFormat("MMM yyyy");
        }
        else if(aPeriod.equals(Quarter.class)) {
            return ReportingDateFormat.getQuarterFormatTooltip(); 
        }
        else if (aPeriod.equals(BYQuarter.class)) {
            return ReportingDateFormat.getQuarterFormatBusinessYearTooltip();
        }
        else if(aPeriod.equals(HalfYear.class)) {
            return ReportingDateFormat.getHalfYearFormatTooltip();
        }
        else if (aPeriod.equals(BYHalfYear.class)) {
            return ReportingDateFormat.getHalfYearFormatBusinessYearTooltip();
        }
        else if(aPeriod.equals(Year.class)) {
            return ReportingDateFormat.getYearFormatTooltip();
        }
        else if (aPeriod.equals(BYYear.class)) {
            return ReportingDateFormat.getYearFormatBusinessYearTooltip();
        }
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm");
	}
}
