/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.top_logic.base.time.BusinessYearConfiguration;
import com.top_logic.base.time.HalfYear;
import com.top_logic.base.time.Quarter;
import com.top_logic.base.time.Year;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.model.aggregation.SupportsType;
import com.top_logic.reporting.report.model.filter.DateIntervalProvider;
import com.top_logic.reporting.report.model.filter.Interval;
import com.top_logic.reporting.report.model.filter.ObjectFilter;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;
import com.top_logic.reporting.report.model.partition.criteria.interval.DateIntervalCriteria;

/**
 * The DatePartitionFunction creates partitions based on {@link DateIntervalCriteria}s. They can be
 * based on an absolute date (e.g. 25.07.2007) or on relative definitions (e.g. "currentDay" and/or
 * "weekly").
 *
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@SupportsType(value = {LegacyTypeCodes.TYPE_DATE})
@Deprecated
public class DatePartitionFunction extends IntervalPartitionFunction {

    /**
     * date range constant representing one day (from 00:00:00 until 23:59:00)
     */
    public static final String              DATE_RANGE_DAY       = "dateRangeDay";

    /**
     * date range constant representing one week (from Monday to Sunday)
     */
    public static final String              DATE_RANGE_WEEK      = "dateRangeWeek";

    /**
     * date range constant representing one month (from 1. until 27.-31.)
     */
    public static final String              DATE_RANGE_MONTH     = "dateRangeMonth";

    /**
     * date range constant representing one quarter of a year
     */
    public static final String              DATE_RANGE_QUARTER   = "dateRangeQuarter";

    /**
     * date range constant representing one half of a year
     */
    public static final String              DATE_RANGE_HALFYEAR  = "dateRangeHalfyear";

    /**
     * date range constant representing one year
     */
    public static final String              DATE_RANGE_YEAR      = "dateRangeYear";

    /**
     * date range constant representing a manual selected date range,
     * from {@link DatePartitionConfiguration#getIntervalStart()}
     * until {@link DatePartitionConfiguration#getIntervalEnd()}
     *
     * If from and until dates are null, the minimum and maximum are used
     * @see #DATE_RANGE_AUTOMATIC
     */
    public static final String              DATE_RANGE_MANUAL    = "dateRangeManual";

    /**
	 * date range constant representing an automatic calculated date range, using the minimum and
	 * maximum of all date values (not null) of {@link #getDatePartitionConfiguration()}
	 */
    public static final String              DATE_RANGE_AUTOMATIC = "dateRangeAutomatic";

    private DateIntervalCriteria intervalCriteria;

	/**
	 * If this constructor is used a call to init is mandatory!
	 */
	public DatePartitionFunction(InstantiationContext aContext, DatePartitionConfiguration aConfig) {
        super(aContext, aConfig);
		this.setPartitionCriterias(new ArrayList());
		this.setPartitionFilters(new ArrayList());
	}

	@Deprecated
	public DatePartitionFunction(String anAttributeName, String aLanguage, boolean ignoreNullValues,
			boolean ignoreEmptyCategories, DateIntervalProvider anIntervalProvider, Object aBegin,
			Object anEnd, Object aGranularity) {
		super(anAttributeName, aLanguage, ignoreNullValues, ignoreEmptyCategories);
		this.setPartitionCriterias(new ArrayList());
		this.setPartitionFilters(new ArrayList());
	}

	protected DatePartitionConfiguration getDatePartitionConfiguration() {
	    return (DatePartitionConfiguration) super.getConfiguration();
	}

	protected Date[] getFixedStartEndDate() {

	    Date[] theDates = new Date[2];

	    DatePartitionConfiguration theConfig    = this.getDatePartitionConfiguration();
        String                     theDateRange = theConfig.getDateRange();
        boolean                    useBusinessYear = theConfig.shouldUseBusinessYear();
        
        int beginDay = 1;
        int beginMonth = Calendar.JANUARY;
        
        if (useBusinessYear) {
            beginDay = BusinessYearConfiguration.getBeginDay();
            beginMonth = BusinessYearConfiguration.getBeginMonth();
        }
        
		Calendar cal = CalendarUtil.createCalendar();

        if (DATE_RANGE_DAY.equals(theDateRange)) {
			theDates[0] = DateUtil.adjustToDayBegin(cal);
			theDates[1] = DateUtil.adjustToDayEnd(cal);
        }
        else if (DATE_RANGE_WEEK.equals(theDateRange)) {
			theDates[0] = DateUtil.adjustToWeekBegin(cal);
			theDates[1] = DateUtil.adjustToWeekEnd(cal);
        }
        else if (DATE_RANGE_MONTH.equals(theDateRange)) {
			theDates[0] = DateUtil.adjustDateToMonthBegin(cal);
			theDates[1] = DateUtil.adjustDateToMonthEnd(cal);
        }
        else if (DATE_RANGE_QUARTER.equals(theDateRange)) {
			Quarter theQ = new Quarter(cal, beginMonth, beginDay);
            theDates[0] = theQ.getBegin();
            theDates[1] = theQ.getEnd();
        }
        else if (DATE_RANGE_HALFYEAR.equals(theDateRange)) {
			HalfYear theH = new HalfYear(cal, beginMonth, beginDay);
            theDates[0] = theH.getBegin();
            theDates[1] = theH.getEnd();
        }
        else if (DATE_RANGE_YEAR.equals(theDateRange)) {
			Year theH = new Year(cal, beginMonth, beginDay);
            theDates[0] = theH.getBegin();
            theDates[1] = theH.getEnd();
        }
        else {
            theDates[0] = theConfig.getIntervalStart();
            theDates[1] = theConfig.getIntervalEnd();
        }

        return theDates;
	}

	@Override
	public List processObjects(Collection someObjects) {
		
		// if no objects are present the following calculations are useless and might produce
		// unwanted warnings/errors
		if (CollectionUtil.isEmptyOrNull(someObjects)) {
			return Collections.emptyList();
		}

	    Date[] theDates = this.getFixedStartEndDate();

		DatePartitionConfiguration theConfig    = this.getDatePartitionConfiguration();
        String                     theDateRange = theConfig.getDateRange();

        // init automatic interval end points
        if (DATE_RANGE_MANUAL.equals(theDateRange) || DATE_RANGE_AUTOMATIC.equals(theDateRange)) {

            theDates[0] = theConfig.getIntervalStart();
            theDates[1] = theConfig.getIntervalEnd();

            if (DATE_RANGE_AUTOMATIC.equals(theDateRange)) {
                theDates[0] = null;
                theDates[1] = null;
            }

            if (theDates[0] == null || theDates[1] == null) {
                Date[] theMinMax = this.getMinMaxDate(someObjects);

                if (theDates[0] == null) {
                    theDates[0] = theMinMax[0];
                }
                if (theDates[1] == null) {
                    theDates[1] = theMinMax[1];
                }
            }
        }

        if (theDates[0] == null || theDates[1] == null) {
        	if (DATE_RANGE_AUTOMATIC.equals(theDateRange)) {
        		throw new ReportingException(DatePartitionFunction.class, "automaticRange.null");
        	}
        	else {
        		throw new ReportingException(DatePartitionFunction.class, "invalidStartEnd");
        	}
        }

        Long granularity = theConfig.getSubIntervalLength();
        boolean useBusinessYear = theConfig.shouldUseBusinessYear();
        this.intervalCriteria = new DateIntervalCriteria(theDates[0], theDates[1], granularity, useBusinessYear, null);

		List theIntervals = new DateIntervalProvider(granularity, useBusinessYear).getIntervals(theDates[0], theDates[1]);
		for (int i = 0; i < theIntervals.size(); i++) {
			Interval theInter  = (Interval) theIntervals.get(i);
			Date theInterBegin = (Date) theInter.getBegin();
			Date theInterEnd   = (Date) theInter.getEnd();
			Filter   theFilter   = new ObjectFilter(theInterBegin, theInterEnd);
            Criteria theCriteria = new DateIntervalCriteria(theInterBegin, theInterEnd, granularity, useBusinessYear, null);
			this.getPartitionFilters().add(theFilter);
			this.getPartitionCriterias().add(theCriteria);
		}
		return createPartitions(someObjects, false);
	}

	protected Date[] getMinMaxDate(Collection someObjects) {

	    Date theMin = null;
	    Date theMax = null;

	    Iterator theIter = someObjects.iterator();
        while(theIter.hasNext()) {

			Date theDate = (Date) this.getAttribute(theIter.next());

            if (theDate == null) {
                continue;
            }

            if (theMax == null || theDate.after(theMax)) {
                theMax = theDate;
            }

            if(theMin == null || theDate.before(theMin)) {
                theMin = theDate;
            }
        }

        return new Date[] { theMin, theMax };
	}

	@Override
	public String getType() {
		return PartitionFunctionFactory.DATE;
	}

	@Override
	public Criteria getCriteria() {
		return this.intervalCriteria;
	}

	public interface DatePartitionConfiguration extends PartitionFunctionConfiguration {

		@Override
		@ClassDefault(DatePartitionFunction.class)
		Class getImplementationClass();

	    /**
	     * Flag to tell the function how to treat the {@link #getDateRange()}-Property.
	     *
	     * If useRelativeRange is true, {@link #getIntervalStart()} and {@link #getIntervalEnd()} are ignored
	     * and the start and end point is calculated from the current date using the {@link DateUtil}-adjustTo...
	     * methods.
	     */
	    @BooleanDefault(false)
	    boolean shouldUseRelativeRange();
	    void setUseRelativeRange(boolean useRelativeRange);

	    /**
         * Flag to tell the function how to treat some special date interval aggregations.
         *
         * If useBusinessYear is true, the start and end point is calculated related to the current business year.
         */
        String USE_BUSINESS_YEAR_NAME = "useBusinessYear";
        @BooleanDefault(false)
        @Name(USE_BUSINESS_YEAR_NAME)
        boolean shouldUseBusinessYear();
        void setUseBusinessYear(boolean useBusinessYear);
	    
	    /**
	     * One of the #DATE_RANGE_* constants
	     */
        String DATE_RANGE_NAME = "dateRange";
	    @Name(DATE_RANGE_NAME)
	    String getDateRange();
	    void   setDateRange(String aString);

	    /**
		 * Set the start date of the partition interval.
		 */
	    String INTERVAL_START_NAME = "intervalStart";
	    @Name(INTERVAL_START_NAME)
        Date getIntervalStart();
        void setIntervalStart(Date aDate);

        /**
         * Set the end date of the partition interval
         */
        String INTERVAL_END_NAME = "intervalEnd";
        @Name(INTERVAL_END_NAME)
        Date getIntervalEnd();
        void setIntervalEnd(Date aDate);

        /**
         * One of the #*_INT constants.
         */
        public static final String SUB_INTERVAL_LENGTH_NAME = "subIntervalLength";
        @Name(SUB_INTERVAL_LENGTH_NAME)
        Long getSubIntervalLength();
        void setSubIntervalLength(Long aLength);
	}
}
