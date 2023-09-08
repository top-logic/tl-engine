/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;

/**
 * Abstract {@link Constraint} for {@link FormField} implementations that use
 * {@link Integer} as their {@link FormField#getValue() values}, and which do
 * not accept the empty input.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractIntConstraint extends AbstractIntegerConstraint {

	@Override
	protected final boolean checkInteger(Integer value) throws CheckException {
		if (value == null) {
			// Note: An int constraint must not imply a mandatory constraint.
			return (true); 
		}
		return checkInt(value.intValue());
	}

	/**
	 * Convenience method that replaces
	 * {@link com.top_logic.layout.form.Constraint#check(Object)}.
	 * 
	 * If this method is called, this constraint has already ensured that the
	 * input is not empty.
	 * 
	 * @see com.top_logic.layout.form.Constraint#check(Object) for parameters
	 *      and result.
	 */
	protected abstract boolean checkInt(int value) throws CheckException;

}
