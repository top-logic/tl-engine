/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.criteria.interval;

import java.text.Format;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.DateUtil;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;
import com.top_logic.reporting.report.util.DateConstants;

/**
 * The DateIntervalCriteria is a criteria that works on dates.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class DateIntervalCriteria implements IntervalCriteria {
    private Date   begin;
    private Date    end;
    private Long    granularity;
    private boolean useBusinessYear;
    private Map     additionalSettings;

    public DateIntervalCriteria( Date aBegin, Date anEnd, Long aGranularity, boolean useBusinessYear) {
        this(aBegin, anEnd, aGranularity, useBusinessYear, new HashMap());
    }
    
    public DateIntervalCriteria (Date aBegin, Date anEnd, Long aGranularity, boolean useBusinessYear, Map someSettings ) {
        this.begin = aBegin;
        this.end = anEnd;
        this.granularity = aGranularity;
        this.additionalSettings = someSettings;
        this.useBusinessYear = useBusinessYear;
    }

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getBegin()
	 */
	@Override
	public Object getBegin() {
		return this.begin;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getEnd()
	 */
	@Override
	public Object getEnd() {
		return this.end;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.Criteria#getCriteriaTyp()
	 */
	@Override
	public String getCriteriaTyp() {
		return "date-interval-criteria";
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getGranularity()
	 */
	@Override
	public Object getGranularity() {
		return this.granularity;
	}
	
	
	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getAdditionalSettings()
	 */
	@Override
	public Object getAdditionalSettings() {
		return this.additionalSettings;
	}
	
	/** 
	 * Used to name the partitions this {@link DateIntervalCriteria} defines.
	 * 
	 * @return a {@link String} representation of this {@link DateIntervalCriteria}
	 */
	@Override
	public String toString() {
		Format theFormat;
		if (TimeRangeFactory.YEAR_INT.equals(this.granularity)) {
			theFormat = DateConstants.getDateFormat(DateConstants.YEAR, this.useBusinessYear);
			if (DateUtil.sameYear(this.begin, this.end)) {
				return theFormat.format(this.begin);
			}
		}
		else if (TimeRangeFactory.HALFYEAR_INT.equals(this.granularity)) {
			theFormat = DateConstants.getDateFormat(DateConstants.HALFYEAR, this.useBusinessYear);
			if (DateUtil.sameHalfOfYear(this.begin, this.end)) {
				return theFormat.format(this.begin);
			}
        }
		else if (TimeRangeFactory.QUARTER_INT.equals(this.granularity)) {
		    theFormat = DateConstants.getDateFormat(DateConstants.QUARTER, this.useBusinessYear);
			if (DateUtil.sameQuarterOfYear(this.begin, this.end)) {
				return theFormat.format(this.begin);
			}
		}
		else if (TimeRangeFactory.MONTH_INT.equals(this.granularity)) {
			theFormat = DateConstants.getDateFormat(DateConstants.MONTH, this.useBusinessYear);
			if (DateUtil.sameMonthAndYear(this.begin, this.end)) {
				return theFormat.format(this.begin);
			}
		}
		else if (TimeRangeFactory.WEEK_INT.equals(this.granularity)) {
			theFormat = DateConstants.getDateFormat(DateConstants.WEEK, this.useBusinessYear);
			if (DateUtil.sameWeekOfYear(this.begin, this.end)) {
				return theFormat.format(this.begin);
			}
		}
		else if (TimeRangeFactory.DAY_INT.equals(this.granularity)) {
			theFormat = DateConstants.getDateFormat(DateConstants.DAY, this.useBusinessYear);
			if (DateUtil.sameDay(this.begin, this.end)) {
				return theFormat.format(this.begin);
			}
		}
		else if (TimeRangeFactory.HOUR_INT.equals(this.granularity)) {
			theFormat = DateConstants.getDateFormat(DateConstants.HOUR, this.useBusinessYear);
		}
		else {
		    throw new IllegalArgumentException("Granularity " + this.granularity + " not supported for DateIntervalCriteria");
		}
		
		return  "[ " + theFormat.format(this.begin) + " - " + theFormat.format(this.end) + " ]";
	}
}
