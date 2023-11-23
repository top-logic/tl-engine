/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;

/**
 * Abstract base class for all {@link FormField}s, which consume only a single
 * raw value from its client-side representation (with a single input view, as
 * {@link IntField}, and {@link ComplexField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSingleValueField extends AbstractFormField implements SingleValueField {

	/**
	 * @see AbstractFormField#AbstractFormField(String, boolean, boolean, boolean, Constraint)
	 */
	protected AbstractSingleValueField(String name, boolean mandatory,
			boolean immutable, Constraint constraint) {
		super(name, mandatory, immutable, NORMALIZE, constraint);
	}

	/**
	 * Processes the input under the assumption that at most a single value was input to this field.
	 * 
	 * <p>
	 * If there is some input, the processing is forwarded to
	 * {@link #parseString(String)}. Otherwise, {@link FormField#EMPTY_INPUT}
	 * is returned.
	 * </p>
	 * 
	 * @see AbstractFormField#parseRawValue(Object)
	 */
	@Override
	protected Object parseRawValue(Object rawValue) throws CheckException {
		if (rawValue == NO_RAW_VALUE) {
			return EMPTY_INPUT;
		} else {
			return parseString((String) rawValue);
		}
	}

	/**
	 * Processes the input under the assumption that the given application value can be unparsed to
	 * a single string.
	 * 
	 * <p>
	 * The processing is forwarded to {@link #unparseString(Object)}.
	 * </p>
	 * 
	 * @see AbstractFormField#parseRawValue(Object)
	 */
	@Override
	protected final Object unparseValue(Object value) {
		return unparseString(value);
	}
	
	@Override
	public final void setAsString(String aValue) {
		try {
			setValue(parseString(aValue));
		} catch (CheckException ex) {
			throw (IllegalArgumentException) new IllegalArgumentException().initCause(ex);
		}
	}
	
	@Override
	public final String getAsString() {
		return unparseString(getValue());
	}
	
    @Override
	public final String getRawString() {
    	Object rawValue = getRawValue();
    	
		if (rawValue == NO_RAW_VALUE) return "";
    	
        return (String) rawValue;
    }

    /**
	 * Equivalent to {@link AbstractFormField#parseRawValue(Object)} for
	 * fields that only consume a single raw input value.
	 */
	protected abstract Object parseString(String aRawValue) throws CheckException;

	/**
	 * Equivalent to {@link AbstractFormField#unparseValue(Object)} for fields
	 * that only consume a single raw input value.
	 */
	protected abstract String unparseString(Object aValue);

}
