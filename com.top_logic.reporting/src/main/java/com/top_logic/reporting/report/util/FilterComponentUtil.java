/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.reporting.common.period.HalfYear;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.view.component.AbstractFilterComponent;
import com.top_logic.util.Resources;

/**
 * The FilterComponentUtil contains useful static methods for the {@link AbstractFilterComponent}
 * and its subclasses.
 *
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class FilterComponentUtil {

    public static final Class[] TIME_PERIOD_MONTH_QUARTER_HALFYEAR_YEAR = new Class[] {Month.class, Quarter.class, HalfYear.class, Year.class};
    public static final Class[] TIME_PERIOD_MONTH_QUARTER_YEAR          = new Class[] {Month.class, Quarter.class,  Year.class};
    public static final Class[] TIME_PERIOD_MONTH_HALFYEAR_YEAR         = new Class[] {Month.class, HalfYear.class, Year.class};

    public static final String FIELDNAME_DATE_START  = "startDate";
    public static final String FIELDNAME_DATE_END    = "endDate";
    public static final String FIELDNAME_TIME_PERIOD = "timePeriod";

    private FilterComponentUtil() {
        // Use the static methods of this class.
    }

    public static void addStartEndDateFields(FormContext aFormContext, Date aStartDate, Date anEndDate) {
        ComplexField startDateField = FormFactory.newDateField(FIELDNAME_DATE_START, null, false);
        ComplexField endDateField   = FormFactory.newDateField(FIELDNAME_DATE_END, null, false);

        // Init the start date
        startDateField.setMandatory(true);
        startDateField.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, endDateField));
        if (aStartDate != null) {
            startDateField.setValue(aStartDate);
        }
        aFormContext.addMember(startDateField);

        // Init the end date
        endDateField.setMandatory(true);
        endDateField.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, startDateField));
		if (anEndDate != null) {
            endDateField.setValue(anEndDate);
        }
        aFormContext.addMember(endDateField);
    }

    public static Date getDate(FormContext aFormContext, String aDateFieldName) {
        return (Date) aFormContext.getField(aDateFieldName).getValue();
    }

    public static void copyStartEndDateFromTo(FormContext aFormContext, ChartContext aFilterVO) {
        aFilterVO.setValue(FIELDNAME_DATE_START, getDate(aFormContext, FIELDNAME_DATE_START));
        aFilterVO.setValue(FIELDNAME_DATE_END,   getDate(aFormContext, FIELDNAME_DATE_END));
    }

    public static void addTimePeriodField(FormContext aFormContext, Class[] someRegularTimePeriods) {
        SelectField timePeriodField = FormFactory.newSelectField(FIELDNAME_TIME_PERIOD, Arrays.asList(someRegularTimePeriods), /* multiple */ false, /* immutable */ false);
        timePeriodField.setMandatory(true);
        timePeriodField.addConstraint(new SelectionSizeConstraint(1, 1));
        timePeriodField.setOptionLabelProvider(RegularTimePeriodLabelProvider.INSTANCE);
        timePeriodField.setAsSingleSelection(someRegularTimePeriods[someRegularTimePeriods.length - 1]);
        aFormContext.addMember(timePeriodField);
    }

    public static Class getTimePeriodValue(FormContext aFormContext) {
        SelectField timePeriodField = (SelectField) aFormContext.getField(FIELDNAME_TIME_PERIOD);

        return (Class) timePeriodField.getSingleSelection();
    }

    public static void copyTimePeriodValueFromTo(FormContext aFormContext, ChartContext aFilterVO) {
        aFilterVO.setValue(FIELDNAME_TIME_PERIOD, getTimePeriodValue(aFormContext));
    }

    public static void copyFieldValueFromTo(FormContext aFormContext, ChartContext aFilterVO, String aFieldName) {
        aFilterVO.setValue(aFieldName, aFormContext.getField(aFieldName).getValue());
    }

    /**
     * This method returns the minimum and maximum date of the extracted dates
     * from the given wrapper and attribute names. The attribute values must be
     * a {@link Date} or <code>null</code>. If no maximum or minimum date is
     * found the default dates are used.
     *
     * The returned array contains two values the
     *  - first  = minimum date
     *  - second = maximum date
     *
     * @param ofWrappers
     *        A collection of {@link Wrapper}s. Must not be <code>null</code>.
     * @param ofDateAttributeNames
     *        An array of attributenames (value must be a {@link Date}). Must
     *        not be <code>null</code>.
     * @param aMinDefault
     *        The minimum date is used if no date was found.
     * @param aMaxDefault
     *        The maximum date is used if only one date was found.
     */
    public static Date[] getMinMaxDate(Collection ofWrappers, String[] ofDateAttributeNames, Date aMinDefault, Date aMaxDefault) {
        // Get all dates from the wrappers and store it into a list.
        // Sort the list and the min date is the first element and the
        // max date is the last date of the list.
        List dateList = new ArrayList();
        for (Iterator projectIter = ofWrappers.iterator(); projectIter.hasNext();) {
            Wrapper project = (Wrapper) projectIter.next();

            for (int i = 0; i < ofDateAttributeNames.length; i++) {
                addDate(dateList, (Date) project.getValue(ofDateAttributeNames[i]));
            }
        }
        Collections.sort(dateList);

        // Return the min/max date array.
        int size = dateList.size();
        if (size == 0) {
            return new Date[] {aMinDefault, aMaxDefault};
        } else if (size == 1) {
            return new Date[] {(Date) dateList.get(0), aMaxDefault};
        } else {
            return new Date[] {(Date) dateList.get(0), (Date) dateList.get(size - 1)};
        }
    }

    private static void addDate(List ofDates, Date aDate) {
        if (aDate != null) {
            ofDates.add(aDate);
        }
    }

    private static class RegularTimePeriodLabelProvider implements LabelProvider {

        public static final RegularTimePeriodLabelProvider INSTANCE = new RegularTimePeriodLabelProvider();

        private RegularTimePeriodLabelProvider() {
            // Use the single instance of this class.
        }

        @Override
		public String getLabel(Object anObject) {
            Class     timePeriod = (Class) anObject;
            Resources resource   = Resources.getInstance();
            if (timePeriod.equals(Month.class)) {
				return resource.getString(I18NConstants.GRANULARITY_MONTHS);
            }
            else if (timePeriod.equals(Year.class)) {
				return resource.getString(I18NConstants.GRANULARITY_YEARS);
            }
            else if (timePeriod.equals(Quarter.class)) {
				return resource.getString(I18NConstants.GRANULARITY_QUARTERS);
            }
            else if (timePeriod.equals(HalfYear.class)) {
				return resource.getString(I18NConstants.GRANULARITY_HALF_YEARS);
            }
            else if (timePeriod.equals(Week.class)) {
				return resource.getString(I18NConstants.GRANULARITY_WEEKS);
            }
            else if (timePeriod.equals(Day.class)) {
				return resource.getString(I18NConstants.GRANULARITY_DAYS);
            }

            throw new IllegalArgumentException("The regular time period ('" + timePeriod + "') is not supported.");
        }

    }

}

