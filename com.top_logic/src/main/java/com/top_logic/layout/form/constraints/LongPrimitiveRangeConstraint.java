/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * A constraint that checks, whether a given integer value satisfies a lower and
 * upper bound.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LongPrimitiveRangeConstraint extends AbstractLongPrimitiveConstraint {
	boolean hasMin;
	boolean hasMax;
	long min;
	long max;

	/**
	 * Creates a constraint that checks a lower and upper bound.  
	 *  
	 * @param min
	 *     The lower bound.
	 * @param max
	 *     The upper bound.
	 */
	public LongPrimitiveRangeConstraint(long min, long max, boolean anAllowNull) {
		this(true, min, true, max, anAllowNull);
	}

	/**
	 * Creates a new constraint that only checks a lower bound.
	 * 
	 * To create a constraint that only checks an upper bound, use the
	 * {@link #LongPrimitiveRangeConstraint(boolean, long, boolean, long, boolean)} constructor.
	 * 
	 * @param min
	 *     The lower bound.
	 */
	public LongPrimitiveRangeConstraint(long min, boolean anAllowNull) {
		this(true, min, false, 0, anAllowNull);
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
	public LongPrimitiveRangeConstraint(boolean hasMin, long min, boolean hasMax, long max, boolean anAllowNull) {
        super(anAllowNull);
		this.hasMin = hasMin;
		this.min = min;
		this.hasMax = hasMax;
		this.max = max;
	}

	@Override
	protected boolean checkLongPrimitive(long value) throws CheckException {
		if (hasMin && (value < min)) {
			throw new CheckException(
				Resources.getInstance().getString(I18NConstants.VALUE_TO_SMALL__VALUE_MINIMUM.fill(Long.valueOf(value), Long.valueOf(min))));
		}
		if (hasMax && (value > max)) {
			throw new CheckException(
				Resources.getInstance().getString(I18NConstants.VALUE_TO_BIG__VALUE_MAXIMUM.fill(Long.valueOf(value), Long.valueOf(max))));
		}
		return true;
	}
	
}
