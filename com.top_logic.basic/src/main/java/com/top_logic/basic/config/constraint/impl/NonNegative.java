/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;
import com.top_logic.basic.config.constraint.algorithm.NumericConstraint;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ConstraintAlgorithm} that accepts only non-negative numbers (including zero).
 * 
 * <p>
 * A more generic was to specify value bounds is the {@link Bound} annotation.
 * </p>
 * 
 * @see Bound
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NonNegative extends NumericConstraint {

	/**
	 * Singleton {@link NonNegative} instance.
	 */
	public static final NonNegative INSTANCE = new NonNegative();

	private NonNegative() {
		super();
	}

	@Override
	protected boolean matches(Number value) {
		return value.doubleValue() >= 0.0;
	}

	@Override
	protected ResKey problemDescription() {
		return I18NConstants.NON_NEGATIVE_VALUE_EXPECTED;
	}

}
