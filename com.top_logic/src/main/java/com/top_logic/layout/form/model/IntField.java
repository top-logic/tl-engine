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
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * {@link FormField} that represents {@link Integer}-typed application values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IntField extends AbstractSingleValueField {

	/**
	 * @see AbstractSingleValueField#AbstractSingleValueField(String, boolean, boolean, Constraint)
	 */
	protected IntField(String name, boolean mandatory, boolean immutable,
			Constraint constraint) {
		super(name, mandatory, immutable, constraint);
	}
	
	@Override
	protected Object parseString(String aRawValue) throws CheckException {
		if (aRawValue.trim().length() == 0) {
			// No value.
			return null;
		}
		
		try {
			return Integer.valueOf(aRawValue);
		} catch (NumberFormatException ex) {
			throw new CheckException(
				Resources.getInstance().getString(I18NConstants.FORMAT_INVALID__VALUE_EXAMPLE.fill(aRawValue, Integer.valueOf(42))));
		}
	}
	
	@Override
	protected Object narrowValue(Object aValue) {
		// The casted value must be assigned to a local variable to a void an
		// "unnecessary cast" warning.
		Integer integerValue = (Integer) aValue;
		
		return integerValue;
	}
	
	@Override
	protected String unparseString(Object aValue) {
		if (aValue == null) {
			// No value.
			return "";
		}
		
		return ((Integer) aValue).toString();
	}
	
	/**
	 * Checks, whether the value of this field is not the
	 * {@link FormField#EMPTY_INPUT}.
	 */
	public boolean hasInt() {
		return getValue() != null;
	}
	
	/**
	 * Convenience shortcut for {@link #getValue()}.
	 */
	public Integer getAsInteger() {
		return (Integer) getValue();
	}

	/**
	 * Convenience shortcut for {@link #getValue()}. Assumes that
	 * {@link #hasInt()} returns <code>true</code>.
	 */
	public int getAsInt() {
		assert hasInt();
		return getAsInteger().intValue();
	}

	/**
	 * Convenience shortcut for {@link #setValue(Object)}.
	 */
	public void setAsInt(int aValue) {
		setValue(Integer.valueOf(aValue));
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitIntField(this, arg);
	}
}
