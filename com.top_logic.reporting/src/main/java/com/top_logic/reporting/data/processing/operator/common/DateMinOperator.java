/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.operator.common;

import java.util.Date;

import com.top_logic.reporting.data.processing.operator.AbstractDateOperator;

/** 
 * The minimum operation on a date.
 * 
 * This instance inspect the list of given dates 
 * {@link com.top_logic.reporting.data.base.value.common.DateValue}s
 * and return the date, which is the first in that array.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class DateMinOperator extends AbstractDateOperator {

    /**
     * Constructor for DateMinOperator.
     * 
     * @param    aName    The unique name of this operation.
     * @throws   IllegalArgumentException    If the given name is <code>null</code>.
     */
    public DateMinOperator(String aName) throws IllegalArgumentException {
        super(aName);
    }

    /**
     * Find the date, which is before the other.
     * 
     * @param    aCurrent    The currently active date.
     * @param    aNew        The date to be compared.
     * @return   The date, which is before the other.
     */
    @Override
	protected Date process(Date aCurrent, Date aNew) {
        if (aCurrent.compareTo(aNew) < 0) {
            return (aCurrent);
        }
        else {
            return (aNew);
        }
    }

    /**
     * Return the neutral date for this operation.
     * 
     * This date (which is new Date(Long.MAX_VALUE) will be used, if 
     * there is a <code>null</code> value in the set of values to be 
     * processed.
     * 
     * @return    The neutral date for this operation.
     */
    @Override
	protected Date getNeutralDate() {
        return (new Date(Long.MAX_VALUE));
    }
}
