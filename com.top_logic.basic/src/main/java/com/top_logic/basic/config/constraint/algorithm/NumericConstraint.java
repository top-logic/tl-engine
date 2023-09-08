/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;

import com.top_logic.basic.util.ResKey;

/**
 * {@link ConstraintAlgorithm} that acts on single numeric values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class NumericConstraint extends ValueConstraint<Number> {

	/**
	 * Creates a {@link NumericConstraint}.
	 */
	public NumericConstraint() {
		super(Number.class);
	}

	@Override
	protected void checkValue(PropertyModel<Number> propertyModel) {
		Number value = propertyModel.getValue();
		if (value == null) {
			return;
		}
		if (matches(value)) {
			return;
		}

		propertyModel.setProblemDescription(problemDescription());
	}


	/**
	 * Whether the given value matches this {@link NumericConstraint}.
	 * 
	 * @param value
	 *        The value to check.
	 * @return Whether the given value conforms to this {@link NumericConstraint}.
	 */
	protected abstract boolean matches(Number value);

	/**
	 * The problem description to report, if {@link #matches(Number)} reports <code>false</code>.
	 */
	protected abstract ResKey problemDescription();

}
