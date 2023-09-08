/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Check if the string is a parseable Long.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class IsLongConstraint extends AbstractStringConstraint {

    public static final IsLongConstraint INSTANCE = new IsLongConstraint();

    @Override
	protected boolean checkString(String aValue) throws CheckException {
        // This is no mandatory constraint
        if (StringServices.isEmpty(aValue)) return true;
        try {
            checkLong(Long.parseLong(aValue));
            return true;
        }
        catch (CheckException e) {
            throw e;
        }
        catch (Exception e) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_LONG_VALUE));
        }
    }


    protected void checkLong(long aValue) throws CheckException {
        // nothing to check here - for subclasses only
    }

}
