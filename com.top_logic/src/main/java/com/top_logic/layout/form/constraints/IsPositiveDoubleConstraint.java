/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Check if the value is a positive Double. Otherwise throws a {@link CheckException}.
 * 
 * @author     <a href="mailto:jes@top-logic.com">jes</a>
 */
public class IsPositiveDoubleConstraint extends AbstractConstraint {
	
    public static final IsPositiveDoubleConstraint INSTANCE = new IsPositiveDoubleConstraint();

	@Override
	public boolean check(Object value) throws CheckException {
		if (value instanceof Number) {
			if (((Number) value).doubleValue() <  0) {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_POSITIVE_NUMBER));
	        }
		} else if(value == null){
			return true;
		} else {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_POSITIVE_NUMBER));
		}
		return true;
	}

}
