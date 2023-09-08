/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * The {@link OneFilledFieldDependency} creates constraints which check if one of the given fields
 * contains a value.
 * 
 * <p>
 * E.g. check if one of the three e-mail address fields contain a value:
 * 
 * <pre>
 * FormField[] fields = new FormField[] { mailAddress1, mailAddress2, mailAddress3 };
 * OneFilledFieldDependency dependency = new OneFilledFieldDependency(fields);
 * dependency.attach();
 * </pre>
 * </p>
 * 
 * @see AtMostOneFilledFieldDependency
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OneFilledFieldDependency extends AbstractDependency {

    /** If true false will be interpreted as a filled value for booleans. */
    private boolean allowFalseForBoolean;

    /**
	 * Creates a new {@link OneFilledFieldDependency} with the given parameters.
	 * 
	 * @param someFields
	 *        Some dependent {@link FormField}s.
	 */
	public OneFilledFieldDependency(FormField... someFields) {
        this(someFields, true);
    }
    
    /**
	 * Creates a {@link OneFilledFieldDependency} with the given parameters.
	 * 
	 * @param someFields
	 *        Some dependent {@link FormField}s.
	 */
    public OneFilledFieldDependency(FormField[] someFields, boolean anAllowFalseForBoolean) {
        super(someFields, false);
        
        this.allowFalseForBoolean = anAllowFalseForBoolean;
    }
        
    /**
     * This method checks if the field or one of the dependent fields
     * contain a value.
     * 
     * @param  aValue A value.
     * @return Returns true of the field or one of the dependent fields
     *         contain a value or throw a {@link CheckException}.
     */
    @Override
	public boolean check(int checkedFieldIndex, Object aValue) throws CheckException {
    	if (isAllowedValue(aValue)) {
    		return true;
    	}
    	
    	// If the current field's value is not allowed, check if one of the
		// dependency fields contains a value.
    	for (int i = 0, cnt = size(); i < cnt; i++) {
    		FormField dependency = get(i);
                                    
    		// Check the values of the dependency fields.
    		if (dependency.hasValue()) {
    			Object theVal = dependency.getValue();
    			if (isAllowedValue(theVal)) {
    				return true;
    			}
    		}
    	}

    	// Compute the error message.
    	StringBuffer labelBuffer = new StringBuffer();
    	for (int i = 0, cnt = size(); i < cnt; i++) {
    		// Do not include the checked field in the list of labels.
    		if (i == checkedFieldIndex) continue;
    		
    		if (i > 0) {
    			labelBuffer.append(", ");
    		}
    		labelBuffer.append(get(i).getLabel());
    	}                                    

    	// Neither this field nor the dependent fields contain a value.
    	// Throw a CheckException.
    	throw new CheckException(createErrorMessage(labelBuffer.toString()));
    }

    /**
     * Create the error message for the check exception.
     *
     * @param labels comma separated list of i18n-ed labels of the dependent fields.
     */
    protected String createErrorMessage(String labels) {
		return Resources.getInstance().getString(I18NConstants.MIN_ONE_FIELD_NOT_EMPTY__NAMES.fill(labels));
    }

    protected boolean isAllowedValue(Object aValue) {
    	if (aValue == null) {
    		return false;
    	}
        	
    	if (aValue instanceof Collection<?>) {
    		return !((Collection<?>) aValue).isEmpty();
    	}

		if (aValue instanceof String) {
			return !((String) aValue).isEmpty();
		}
        	
    	if (aValue instanceof Boolean) {
    		return this.allowFalseForBoolean || ((Boolean) aValue).booleanValue();
    	}
        	
    	return true;	// Allow all other non-null values
    }
    
}
