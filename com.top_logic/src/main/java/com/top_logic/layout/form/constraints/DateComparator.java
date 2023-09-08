/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Comparator;
import java.util.Date;

/**
 * Compares dates by the given granularity type.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public abstract class DateComparator implements Comparator<Date> {

	/**
	 * Dates are compared exactly in milliseconds.
	 */
	public static final DateComparator INSTANCE = new DateByDateComparator();

	/**
	 * Dates are compared only by day, month and year.
	 */
	public static final DateComparator INSTANCE_DAY = new DateByDayComparator();

	/**
	 * Dates are compared only by month and year.
	 */
	public static final DateComparator INSTANCE_MONTH = new DateByMonthComparator();

	/**
	 * Dates are compared only by year.
	 */
	public static final DateComparator INSTANCE_YEAR = new DateByYearCompararator();

	/**
	 * Creates a new instance of this class.
	 */
	public DateComparator() {
	}

}
