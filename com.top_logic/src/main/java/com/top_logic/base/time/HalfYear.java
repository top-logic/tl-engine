/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Calendar;

/**
 * Defines a half-year of a year.
 * 
 * Whereas year can be different from the calendar year, e.g. a business year from 1.4. until 31.3.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class HalfYear extends MonthlyTimePeriod {

	/**
	 * Calendaric half year, from 1. January to 30. Juni and 1. July to 31. December
	 */
	public HalfYear(Calendar aDate) {
		this(aDate, Calendar.JANUARY, 1);
	}

	public HalfYear(Calendar aDate, int yearBeginMonth, int yearBeginDay) {
		super(aDate, yearBeginMonth, yearBeginDay, 6);
	}
}
