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
 * Check if the string is a parseable Integer.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class IsIntegerConstraint extends AbstractStringConstraint {

    public static final IsIntegerConstraint INSTANCE = new IsIntegerConstraint();

    @Override
	protected boolean checkString(String aValue) throws CheckException {
        // This is no mandatory constraint
        if (StringServices.isEmpty(aValue)) return true;
        try {
            checkInt(Integer.parseInt(aValue));
            return true;
        }
        catch (CheckException e) {
            throw e;
        }
        catch (Exception e) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_INT_VALUE));
        }
    }

    protected void checkInt(int aValue) throws CheckException {
        // nothing to check here - for subclasses only
    }

}
