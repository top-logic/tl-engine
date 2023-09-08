/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;

/**
 * A constraint that checks, whether a given Double value satisfies a lower and
 * upper bound.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class DoubleRangeConstraint extends RangeConstraint {

	/**
	 * Creates a constraint that checks a lower and upper bound.  
	 *  
	 * @param min
	 *     The lower bound.
	 * @param max
	 *     The upper bound.
	 */
	public DoubleRangeConstraint(double min, double max) {
		super(Double.valueOf(min), Double.valueOf(max));
	}

	/**
	 * Creates a new constraint that only checks a lower bound.
	 * 
	 * To create a constraint that only checks an upper bound, use the
	 * {@link #DoubleRangeConstraint(boolean, double, boolean, double)} constructor.
	 * 
	 * @param min
	 *     The lower bound.
	 */
	public DoubleRangeConstraint(double min) {
		super(Double.valueOf(min), null);
	}
	
	/**
	 * Creates a new constraint with either only a lower, only an upper, or a
	 * lower and upper bound.
	 * 
	 * @param hasMin
	 *     Whether, the new constraint should check a lower bound. 
	 * @param min
	 *     The optional lower bound. 
	 * @param hasMax
	 *     Whether the new constraint should check an upper bound.
	 * @param max
	 *     The optional upper bound.
	 */
	public DoubleRangeConstraint(boolean hasMin, double min, boolean hasMax, double max) {
		super(hasMin ? null : Double.valueOf(min), hasMax ? null : Double.valueOf(max));
	}

    /**
     * Convert aValue to double in case of a Number, then call super.
     */
	@Override
	public boolean check(Object aValue) throws CheckException {
        if (aValue instanceof Double) {
            return super.check(aValue);
        }
        else if (aValue instanceof Number) {
            return super.check(Double.valueOf(((Number)aValue).doubleValue()));
        }
        else if (aValue == null) {
        	// Note: An specialized constraint must not imply a mandatory constraint.
            return true;
        }
        else {
            // calling super wont do any better ...
            throw new ClassCastException("Need at least a Number");
        }
	}
}
