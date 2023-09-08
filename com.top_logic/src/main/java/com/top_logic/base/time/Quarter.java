/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Calendar;

/**
 * A quarter defines a three month period of a year.
 * 
 * Whereas year can be different from the calendar year, e.g. a business year from 1.4. until 31.3.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class Quarter extends MonthlyTimePeriod {

	/**
	 * Calendaric Quarter
	 */
	public Quarter(Calendar aDate) {
		this(aDate, Calendar.JANUARY, 1);
    }
	
	public Quarter(Calendar aDate, int yearBeginMonth, int yearBeginDay) {
		super(aDate, yearBeginMonth, yearBeginDay, 3);
	}
}
