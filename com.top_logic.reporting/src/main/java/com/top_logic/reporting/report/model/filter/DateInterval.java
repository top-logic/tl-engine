/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.util.Date;

import com.top_logic.mig.html.HTMLFormatter;

/**
 * This class wraps a date interval.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class DateInterval implements Interval{
	/** The start date */
	private Date start;
	/** The end date */
	private Date end;

	/** 
	 * Creates a {@link DateInterval}.
	 * 
	 */
	public DateInterval(Date aStartDate, Date anEndDate) {
		if(aStartDate.after(anEndDate)) {
			throw new IllegalArgumentException("EndDate is before StartDate");
		}
		this.start = aStartDate;
		this.end = anEndDate;
	}

	/**
	 * This method returns the start.
	 * 
	 * @return    Returns the start.
	 */
	@Override
	public Object getBegin() {
		return (this.start);
	}

	/**
	 * This method returns the end.
	 * 
	 * @return    Returns the end.
	 */
	@Override
	public Object getEnd() {
		return (this.end);
	}
	
	@Override
	public String toString() {
		return HTMLFormatter.getInstance().formatDate((Date)getBegin()) + " - " + 
				HTMLFormatter.getInstance().formatDate((Date)getEnd());
	}

}

