/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * {@link FormField} implementation for editing plain text values.
 * 
 * <p>
 * Note: For entering formatted values, use {@link ComplexField}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringField extends AbstractFormField implements SingleValueField {
	/**
	 * Value that represents the empty input of a {@link StringField}.
	 * 
	 * @see FormField#EMPTY_INPUT
	 */
	public static final String EMPTY_STRING_VALUE = "";

	/**
	 * Creates a {@link #getNormalizeInput() normalizing} {@link StringField}
	 * 
	 * @see AbstractFormField#AbstractFormField(String, boolean, boolean, boolean, Constraint)
	 */
	protected StringField(String name, boolean mandatory, boolean immutable,
			Constraint constraint) {
		super(name, mandatory, immutable, NORMALIZE, constraint);
	}

	@Override
	protected Object narrowValue(Object aValue) {
		// Local temporary variable prevents the "unnecessary" cast warning.
		String result = (String) aValue;
		
		if (aValue == null) { 
			// throw new IllegalArgumentException("The value of a string field may not be null. Use the empty string instead.");
			result = EMPTY_STRING_VALUE;
		}
		
		return result;
	}

	@Override
	protected Object parseRawValue(Object aRawValue) throws CheckException {
		if (aRawValue == NO_RAW_VALUE) {
			return EMPTY_STRING_VALUE;
		} else {
			// Note: Cast to string to assert that the provided raw value is a string. 
			// To avoid warnings about unnecessary cast, assign casted value to
			// local variable.
			String stringValue = (String) aRawValue;
			return stringValue;
		}
	}

	@Override
	protected Object unparseValue(Object aValue) {
		String stringValue = (String) aValue;
		if (stringValue == null) {
			return NO_RAW_VALUE;
		}
		return stringValue;
	}
	
	@Override
	public String getAsString() {
		String result = (String) getValue();
		if (result == null) {
			return EMPTY_STRING_VALUE;
		} else {
			return result;
		}
	}

	@Override
	public void setAsString(String stringValue) {
		setValue(stringValue);
	}

	@Override
	public String getRawString() {
		Object rawValue = getRawValue();
		if (rawValue == NO_RAW_VALUE) {
			return EMPTY_STRING_VALUE;
		} else {
			return (String) rawValue;
		}
	}
	
    @Override
	public Object visit(FormMemberVisitor v, Object arg) {
        return v.visitStringField(this, arg);
    }

}
