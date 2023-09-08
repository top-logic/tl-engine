/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.util.Resources;

/**
 * {@link Constraint} that checks for a value not being in a given set of values.
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class UniqueValueConstraint extends AbstractConstraint {

	Collection existingValues;
	
	public UniqueValueConstraint(Collection existingValues) {
		this.existingValues = existingValues;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		if(existingValues.contains(value)){
			throw new CheckException(Resources.getInstance().getString(I18NConstants.ERROR_VALUES_NOT_UNIQUE));
		}
		return true;
	}

}
