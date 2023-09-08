/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * The {@link AtMostOneFilledFieldDependency} creates constraints which check that at most one of
 * the given fields contain a value.
 * 
 * <br />
 * 
 * E.g. The user must either select a file or upload a new one, but not both:
 * 
 * <pre>
 * FormField[] fields = new FormField[] { selectFile, uploadFile };
 * AtMostOneFilledFieldDependency dependency = new AtMostOneFilledFieldDependency(fields);
 * dependency.attach();
 * </pre>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AtMostOneFilledFieldDependency extends AbstractDependency {

    /**
	 * Creates a new {@link AtMostOneFilledFieldDependency} with the given parameters.
	 * 
	 * @param someFields
	 *        Some dependent {@link FormField}s.
	 */
	public AtMostOneFilledFieldDependency(FormField... someFields) {
		super(someFields);
    }

    @Override
	public boolean check(int checkedFieldIndex, Object aValue) throws CheckException {
		if (isAllowedValue(aValue)) {
			return true;
		}

		StringBuilder labelBuffer = null;
    	
    	// If the current field's value is not allowed, check if one of the
		// dependency fields contains a value.
    	for (int i = 0, cnt = size(); i < cnt; i++) {
			if (i == checkedFieldIndex) {
				continue;
			}
    		FormField dependency = get(i);
                                    
    		// Check the values of the dependency fields.
			if (!dependency.hasValue()) {
				// Can not check value. Abort.
				return false;
			}
			Object theVal = dependency.getValue();
			if (!isAllowedValue(theVal)) {
				if (labelBuffer == null) {
					labelBuffer = new StringBuilder();
				} else {
					labelBuffer.append(", ");
				}
				labelBuffer.append(get(i).getLabel());
    		}
    	}
    	
    	if (labelBuffer == null) {
			// No other field with not allowed value
			return true;
    	}

		// This field and dependent fields contain a value.
    	// Throw a CheckException.
    	throw new CheckException(createErrorMessage(labelBuffer.toString()));
    }

    /**
     * Create the error message for the check exception.
     *
     * @param labels comma separated list of i18n-ed labels of the dependent fields.
     */
    protected String createErrorMessage(String labels) {
		return Resources.getInstance().getString(I18NConstants.MAX_ONE_FIELD_NOT_EMPTY__NAMES.fill(labels));
    }

    protected boolean isAllowedValue(Object aValue) {
    	if (aValue == null) {
			return true;
    	}
        	
    	if (aValue instanceof Collection<?>) {
			return ((Collection<?>) aValue).isEmpty();
    	}
    	
    	if (aValue instanceof String) {
			return ((String) aValue).isEmpty();
    	}

		if (aValue instanceof Boolean) {
			return !((Boolean) aValue).booleanValue();
		}
        	
		return false; // Allow no non-null values
    }
    
}
