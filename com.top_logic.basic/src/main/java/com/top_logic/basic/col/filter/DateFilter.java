/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.Filter;

/**
 * The DateFilter filters dates. The DateFilter contains a start date and a end date which can be <code>null</code>.
 * <ul>
 *  <li>start date != null, end date != null (only <code>true</code> start date <= check date <= end date)</li>
 *  <li>start date != null, end date == null (only <code>true</code> start date <= check date            )</li>
 *  <li>start date == null, end date != null (only <code>true</code>               check date <= end date)</li>
 *  <li>start date == null, end date == null (always <code>true</code> )</li>
 * </ul> 
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DateFilter implements Filter {

    /** A start date. */
    private Date startDate;
    /** A end date. */
    private Date endDate;

    /**
     * Creates a {@link DateFilter} and adjusts the given dates to
     * the day begin/end.
     * 
     * @param aStartDate A start date.
     * @param aEndDate   A end date.
     */
    public DateFilter(Date aStartDate, Date aEndDate) {
        this(aStartDate, aEndDate, true);
    }
    
    /**
     * Creates a {@link DateFilter} with the given parameters.
     * 
     * @param aStartDate A start date.
     * @param aEndDate   A end date.
     * @param adjustDate Indicates whether the hours, minutes, seconds and milliseconds set to min/max.
     */
    public DateFilter(Date aStartDate, Date aEndDate, boolean adjustDate) {
        if (adjustDate) {
            this.startDate = aStartDate != null ? DateUtil.adjustToDayBegin(aStartDate)  : null;
            this.endDate   = aEndDate   != null ? DateUtil.adjustToDayEnd  (aEndDate)    : null;
        } else {
            this.startDate = aStartDate;
            this.endDate   = aEndDate;
        }
    }

    /** 
     * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
     */
    @Override
	public boolean accept(Object anObject) {
        Date theDate = this.parseDate(anObject);
        
        if (theDate == null) {
            return (this.startDate == null) || (this.endDate == null);
        }
        else {
            boolean afterEqualsStart = this.startDate != null ? theDate.equals(this.startDate) || theDate.after(this.startDate) : true;
            boolean beforeEqualsEnd  = this.endDate   != null ? theDate.equals(this.endDate)   || theDate.before(this.endDate)  : true;

            return afterEqualsStart && beforeEqualsEnd;
        }
    }

    /**
     * This method parses a date from the given object and returns it.
     */
    public Date parseDate (Object anObject) {
        return (Date)anObject;
    }

    /** Returns the end date. */
    public Date getStartDate() {
        return this.startDate;
    }
    
    /** Sets the start date. */
    public void setStartDate(Date aStartDate) {
        this.startDate = aStartDate;
    }
    
    /** Returns the start date. */
    public Date getEndDate() {
        return this.endDate;
    }
    
    /** Sets the end date. */
    public void setEndDate(Date aEndDate) {
        this.endDate = aEndDate;
    }
    
}

