/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Abstract {@link Constraint} for {@link FormField} implementations that use
 * {@link Float} as their {@link FormField#getValue() values}, and which do
 * not accept the empty input.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFloatPrimitiveConstraint extends AbstractFloatConstraint {
    
    boolean allowNull;
    
    public AbstractFloatPrimitiveConstraint(boolean anAllowNull) {
        this.allowNull = anAllowNull;
    }

	@Override
	protected final boolean checkFloat(Number value) throws CheckException {
		if (value == null) {
            if (allowNull) {
                return true;
            } else {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.NOT_EMPTY));
            }
		}
		return checkFloatPrimitive(value.floatValue());
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
	protected abstract boolean checkFloatPrimitive(float value) throws CheckException;

}
