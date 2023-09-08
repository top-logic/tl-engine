/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;

/**
 * A constraint that checks, whether a given Integer value satisfies a lower and
 * upper bound.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IntRangeConstraint extends RangeConstraint {

	/**
	 * Creates a constraint that checks a lower and upper bound, if specified.
	 * 
	 * @param min
	 *        The lower bound, or null of none exists.
	 * @param max
	 *        The upper bound, or null, if none exists.
	 */
	public IntRangeConstraint(Integer min, Integer max) {
		super(min, max);
	}

	/**
     * Convert aValue to Integer in case of a Number, then call super.
     */
	@Override
	public boolean check(Object aValue) throws CheckException {
        if (aValue instanceof Integer) {
            return super.check(aValue);
        }
        else if (aValue instanceof Number) {
			return super.check(Integer.valueOf(((Number) aValue).intValue()));
        }
        else if (aValue == null) {
        	// Note: An specialized constraint must not imply a mandatory constraint.
            return (true);
        }
        else {
            // calling super wont do any better ...
            throw new ClassCastException("Need at least a Number");
        }
	}
}
