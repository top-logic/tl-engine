/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Check if the string is a parseable positive Integer.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class IsPositiveIntegerConstraint extends IsIntegerConstraint {

    public static final IsPositiveIntegerConstraint INSTANCE = new IsPositiveIntegerConstraint();

    protected boolean includeZero;

    public IsPositiveIntegerConstraint() {
        includeZero = true;
    }

    public IsPositiveIntegerConstraint(boolean includeZero) {
        this.includeZero = includeZero;
    }

    @Override
	protected void checkInt(int aValue) throws CheckException {
        if (aValue < (includeZero ? 0 : 1)) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_POSITIVE_NUMBER));
        }
    }

}
