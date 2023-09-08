/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Calendar;

/**
 * A calendar or business year.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class Year extends MonthlyTimePeriod {

	/**
	 * Calendaric Year
	 */
	public Year(Calendar aDate) {
		this(aDate, Calendar.JANUARY, 1);
	}

	public Year(Calendar aDate, int yearBeginMonth, int yearBeginDay) {
		super(aDate, yearBeginMonth, yearBeginDay, 12);
	}
}
