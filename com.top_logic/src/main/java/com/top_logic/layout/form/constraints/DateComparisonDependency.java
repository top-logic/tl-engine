/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Comparator;
import java.util.Date;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormField;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * A ComparisonDependency specialized for Dates. The Dates gets compared in the desired
 * granularity.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 * 
 */
public class DateComparisonDependency extends ComparisonDependency {

    /** Dates are compared exactly in milliseconds. */
    public static final int GRANULARITY_TYPE_MILLISECOND = 0;

    /** Dates are compared only by day, month and year. */
    public static final int GRANULARITY_TYPE_DAY = 1;

    /** Dates are compared only by month and year. */
    public static final int GRANULARITY_TYPE_MONTH = 2;

    /** Dates are compared only by year. */
    public static final int GRANULARITY_TYPE_YEAR = 3;


    /**
	 * @see #DateComparisonDependency(int, int, FormField, ResKey)
	 */
    public DateComparisonDependency(int aType, FormField aDependency) {
        this(GRANULARITY_TYPE_MILLISECOND, aType, aDependency, null);
    }

    /**
	 * @see #DateComparisonDependency(int, int, FormField, ResKey)
	 */
	public DateComparisonDependency(int aType, FormField aDependency, ResKey aCustomErrorMessageKey) {
        this(GRANULARITY_TYPE_MILLISECOND, aType, aDependency, aCustomErrorMessageKey);
    }

    /**
	 * @see #DateComparisonDependency(int, int, FormField, ResKey)
	 */
    public DateComparisonDependency(int aGranularityType, int aType, FormField aDependency) {
        this(aGranularityType, aType, aDependency, null);
    }

    /**
	 * Creates a new instance of this class. See
	 * {@link ComparisonDependency#ComparisonDependency(int, FormField, ResKey)} for a description.
	 *
	 * @param aGranularityType
	 *        the granularity type to use while comparing the Dates
	 */
	public DateComparisonDependency(int aGranularityType, int aType, FormField aDependency,
			ResKey aCustomErrorMessageKey) {
		super(aType, aDependency, getComparator(aGranularityType), aCustomErrorMessageKey);
    }

	private static Comparator getComparator(int aGranularityType) {
		switch (aGranularityType) {
			case GRANULARITY_TYPE_MILLISECOND:
				return DateComparator.INSTANCE;
			case GRANULARITY_TYPE_DAY:
				return DateComparator.INSTANCE_DAY;
			case GRANULARITY_TYPE_MONTH:
				return DateComparator.INSTANCE_MONTH;
			case GRANULARITY_TYPE_YEAR:
				return DateComparator.INSTANCE_YEAR;
			default: {
				throw new IllegalArgumentException("Unknown granularity type.");
			}
		}
	}

    @Override
	protected Object formatForMessage(Object aValue) {
		return HTMLFormatter.getInstance().formatDate((Date) aValue);
    }



    /**
     * Binds two date fields with a comparison dependency.
     *
     * Start field < date > End field
     *
     * @param aStartField
     *        The start field (which has the lower value).
     * @param aEndField
     *        The end field (which has the higher value).
     */
    public static void buildStartEndDependency(FormField aStartField, FormField aEndField) {
        aStartField.addConstraint(new DateComparisonDependency(ComparisonDependency.LOWER_TYPE, aEndField));
        aEndField.addConstraint(new DateComparisonDependency(ComparisonDependency.GREATER_TYPE, aStartField));
    }

    /**
     * Binds two date fields with a comparison dependency.
     *
     * Start field <= date => End field
     *
     * @param aStartField
     *        The start field (which has the lower or equal value).
     * @param aEndField
     *        The end field (which has the higher or equal value).
     */
    public static void buildStartEndWithEqualDependency(FormField aStartField, FormField aEndField) {
        aStartField.addConstraint(new DateComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, aEndField));
        aEndField.addConstraint(new DateComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, aStartField));
    }

	/**
	 * Binds two date fields with a comparison dependency.
	 * 
	 * Start field < date > End field
	 * 
	 * @param aStartField
	 *        The start field (which has the lower value).
	 * @param aEndField
	 *        The end field (which has the higher value).
	 */
	public static void buildStartEndDependency(int granularityType, FormField aStartField, FormField aEndField) {
		aStartField.addConstraint(new DateComparisonDependency(granularityType, ComparisonDependency.LOWER_TYPE, aEndField));
		aEndField.addConstraint(new DateComparisonDependency(granularityType, ComparisonDependency.GREATER_TYPE, aStartField));
	}

	/**
	 * Binds two date fields with a comparison dependency.
	 * 
	 * Start field <= date => End field
	 * 
	 * @param aStartField
	 *        The start field (which has the lower or equal value).
	 * @param aEndField
	 *        The end field (which has the higher or equal value).
	 */
	public static void buildStartEndWithEqualDependency(int granularityType, FormField aStartField, FormField aEndField) {
		aStartField.addConstraint(new DateComparisonDependency(granularityType, ComparisonDependency.LOWER_OR_EQUALS_TYPE, aEndField));
		aEndField.addConstraint(new DateComparisonDependency(granularityType, ComparisonDependency.GREATER_OR_EQUALS_TYPE, aStartField));
	}

}
