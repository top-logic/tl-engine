/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * A RangeConstraint specialized for Dates.
 *
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DateRangeConstraint extends RangeConstraint {

    /** Dates are compared exactly in milliseconds. */
    public static final int GRANULARITY_TYPE_MILLISECOND = 0;

    /** Dates are compared only by day, month and year. */
    public static final int GRANULARITY_TYPE_DAY = 1;

    /** Dates are compared only by month and year. */
    public static final int GRANULARITY_TYPE_MONTH = 2;

    /** Dates are compared only by year. */
    public static final int GRANULARITY_TYPE_YEAR = 3;


    /** The granularity to use in comparing. */
    private int granularityType;

    /**
     * Creates a new RangeConstraint without ranges. Use the method
     * {@link #setLowerAndUpper(Comparable, Comparable)} to set the lower and
     * upper bound.
     */
    public DateRangeConstraint() {
        this(GRANULARITY_TYPE_MILLISECOND);
    }
    
    /**
     * Creates a new RangeConstraint without ranges. Use the method
     * {@link #setLowerAndUpper(Comparable, Comparable)} to set the lower and
     * upper bound.
     *
     * @param granularity the granularity type to use while comparing the Dates
     */
    public DateRangeConstraint(int granularity) {
        setGranularity(granularity);
		setErrorKeyToSmall(I18NConstants.DATE_STRING_TO_SMALL__VALUE_MINIMUM);
		setErrorKeyToBig(I18NConstants.DATE_STRING_TO_BIG__VALUE_MAXIMUM);
    }
    
    /** 
     * Create a new RangeConstraint with lower an upper bounds.
     * 
     * @param aLower null indicates no lower limit
     * @param anUpper null indicates no anUpper limit
     * 
     * @throws IllegalArgumentException when both are null or aLower &gt; anUpper.
     */
    public DateRangeConstraint(Date aLower, Date anUpper) {
        this(GRANULARITY_TYPE_MILLISECOND, aLower, anUpper);
    }

    /** 
     * Create a new RangeConstraint with lower an upper bounds.
     * 
     * @param aLower null indicates no lower limit
     * @param anUpper null indicates no anUpper limit
     * @param granularity the granularity type to use while comparing the Dates
     * 
     * @throws IllegalArgumentException when both are null or aLower &gt; anUpper.
     */
    public DateRangeConstraint(int granularity, Date aLower, Date anUpper) {
        super(aLower, anUpper);
        setGranularity(granularity);
    }
    
    public void setGranularity(int granularity) {
        granularityType = granularity;
        switch (granularityType) {
            case GRANULARITY_TYPE_MILLISECOND:
            case GRANULARITY_TYPE_DAY:
            case GRANULARITY_TYPE_MONTH:
            case GRANULARITY_TYPE_YEAR:
                break;
            default: {
                throw new IllegalArgumentException("Unknown granularity type.");
            }
        }
    }

    @Override
	public boolean check(Object aValue) throws CheckException {
        if (aValue instanceof Date) {
			if (getLower() != null && (compareDates((Date) getLower(), (Date) aValue) > 0))
                throw new CheckException(getValueToSmallMessage(aValue));
			if (getUpper() != null && (compareDates((Date) getUpper(), (Date) aValue) < 0))
                throw new CheckException(getValueToBigMessage(aValue));
            return true;
        }
        return super.check(aValue);
    }

    protected int compareDates(Date value, Date otherValue) {
        switch (granularityType) {
            case GRANULARITY_TYPE_DAY: {
                return DateUtil.compareDatesByDay(value, otherValue);
            }
            case GRANULARITY_TYPE_MONTH: {
                return DateUtil.compareDatesByMonth(value, otherValue);
            }
            case GRANULARITY_TYPE_YEAR: {
                return DateUtil.compareDatesByYear(value, otherValue);
            }
            default: {
                return DateUtil.compareDates(value, otherValue);
            }
        }
    }

	/**
	 * This method returns the value to big message for the
	 * {@link CheckException} and NEVER <code>null</code>. This method is a
	 * hook for subclasses to return detailed message.
	 * 
	 * @param aValue
	 *            A value that is to big for the declared range.
	 * @return Returns the value to big message for the {@link CheckException}
	 */
	@Override
	protected String getValueToBigMessage(Object aValue) {
		return Resources.getInstance().getString(getErrorKeyToBig().fill(HTMLFormatter.getInstance().formatDate((Date) aValue), HTMLFormatter.getInstance().formatDate((Date) getUpper())));
	}

	/**
	 * This method returns the value to small message for the
	 * {@link CheckException} and NEVER <code>null</code>. This method is a
	 * hook for subclasses to return detailed message.
	 * 
	 * @param aValue
	 *            A value that is to small for the declared range.
	 * @return Returns the value to small message for the {@link CheckException}
	 */
	@Override
	protected String getValueToSmallMessage(Object aValue) {
		return Resources.getInstance().getString(getErrorKeyToSmall().fill(HTMLFormatter.getInstance().formatDate((Date) aValue), HTMLFormatter.getInstance().formatDate((Date) getLower())));
	}
	
}
