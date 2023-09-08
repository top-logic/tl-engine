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
 * single {@link String} objects as their {@link FormField#getValue() values}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStringConstraint extends AbstractConstraint {

    /** Redirects to {@link #checkString(String)} with cast. */
	@Override
	public final boolean check(Object value) throws CheckException {
        if (value instanceof String || value == null) {
            return checkString((String)value);
        }
        else {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_VALUE));
        }
	}

	/**
	 * Type-safe convenience method that replaces
	 * {@link Constraint#check(Object)}.
	 *
	 * @see Constraint#check(Object) for parameters and result.
	 */
	protected abstract boolean checkString(String value) throws CheckException;
}
