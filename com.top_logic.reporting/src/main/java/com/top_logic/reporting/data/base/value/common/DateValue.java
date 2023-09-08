/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.value.common;

import java.text.DateFormat;
import java.util.Date;

import com.top_logic.reporting.data.base.value.simple.SimpleValue;

/** 
 * A value representing a simple date.
 *
 * If the methods for a string representation are called, this instance
 * uses the {com.top_logic.mig.html.HTMLFormatter} for layouting the
 * value in the correct manner.
 * 
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class DateValue extends SimpleValue {

    /**
     * Constructor for DateValue.
     * 
     * @param    aValue    The represented value.
     * @throws   IllegalArgumentException    If the given value is <code>null</code>.
     */
    public DateValue(Date aValue) {
        super(aValue);
    }

    /**
     * @see com.top_logic.reporting.data.base.value.Value#getEntryAsString(int)
     */
    @Override
	public String getEntryAsString(int anIndex) {
        return ((anIndex == 0) ? DateFormat.getInstance().format(((Date)this.getValue()))
                               : "");
    }

    /**
     * @see com.top_logic.reporting.data.base.value.Value#setEntryAt(int, Object)
     */
    @Override
	public void setEntryAt(int anIndex, Object anEntry) {
        if ((anIndex == 0) && (anEntry instanceof Date)) {
            super.setEntryAt(anIndex,anEntry);
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object anObject) {
		if (anObject == null) {
			return false;
		}
		if (anObject == this) {
			return true;
		}
		if (anObject.getClass() == DateValue.class) {
            long theValue = ((Date)(((DateValue) anObject).getValue())).getTime();
            long theOrig  = ((Date)(this.getValue())).getTime();

            return (theOrig == theValue);
        }
        else {
            return (false);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" + 
                        "value: " + this.getEntryAsString(0) +
                        ']');
    }
    



    
}
