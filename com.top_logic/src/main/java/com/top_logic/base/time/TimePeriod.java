/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Date;

import com.top_logic.basic.DateUtil;

/**
 * TODO #5554 This is basically a part of the Semantics implemented by {@link TimeRangeIterator}.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public abstract class TimePeriod {
	protected Date begin;
	protected Date end;

	/**
	 * Takes a {@link Date}, calculates the TimePeriod this date resides in and set
	 * the begin and end dates repectivly.
	 */
	protected abstract void adjustBeginEnd(Date aDate);

	/**
	 * Checks weather a given {@link Date} is inside this Time Period or not.
	 * 
	 * @param aDate
	 *        a {@link Date} to check.
	 * @return <code>true</code> if the given {@link Date} is inside the {@link TimePeriod},
	 *         <code>false</code> otherwise.
	 */
	public boolean contains(Date aDate) {
		return DateUtil.inInterval(aDate, this.begin, this.end);
	}
	
	/**
	 * Updates this {@link TimePeriod} to represent the new {@link Date}
	 */
	public void update(Date aDate) {
		this.adjustBeginEnd(aDate);
    }
	
	public Date getBegin() {
	    return begin;
    }

	public Date getEnd() {
	    return end;
    }
	
	/**
	 * the next {@link TimePeriod} of the same type.
	 */
	public abstract TimePeriod getNextPeriod();
}
