/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.Logger;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * {@link FormField} that represents {@link Boolean}-typed application values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanField extends AbstractSingleValueField implements Comparable {

	/**
	 * The raw value representing {@link Boolean#FALSE} (only for compatibility
	 * with plain HTML input fields).
	 */
	private static final String COMPATIBILITY_FALSE_RAW_VALUE = "";

	/**
	 * The raw value representing {@link Boolean#TRUE} (only for compatibility
	 * with plain HTML input fields).
	 */
	private static final String COMPATIBILITY_TRUE_RAW_VALUE = "checked";
	

//	private boolean tooltip = false;

	/**
	 * The raw value representing the undecided input of a {@link BooleanField}.
	 * 
	 * <p>
	 * The application value corresponding to the undecided state is
	 * {@link FormField#EMPTY_INPUT}.
	 * </p>
	 * 
	 * <p>
	 * It depends on the view, whether it supports the "undecided state" in
	 * addition to the <code>true</code>, and <code>false</code> states. If
	 * a view does not support the undecided state, it must interpret is as
	 * {@link Boolean#FALSE}.
	 * </p>
	 */
	public static final String NONE_RAW_VALUE = "none";
	
	/**
	 * The raw value representing the {@link Boolean#FALSE} application value.
	 */
	public static final String FALSE_RAW_VALUE = "false";
	
	/**
	 * The raw value representing the {@link Boolean#TRUE} application value.
	 */
	public static final String TRUE_RAW_VALUE = "true";

	/**
	 * @see AbstractFormField#AbstractFormField(String, boolean, boolean, boolean, Constraint)
	 */
	protected BooleanField(String name, boolean mandatory, boolean immutable,
			Constraint constraint) {
		super(name, mandatory, immutable, constraint);
	}
	
	@Override
	protected Object parseRawValue(Object aRawValue) throws CheckException {
		if (aRawValue instanceof Boolean) {
			// For convenience, a boolean field can also be updated with a
			// native JS boolean value.
			aRawValue = aRawValue.toString();
		}

		return super.parseRawValue(aRawValue);
	}

	@Override
	protected Object parseString(String aRawValue) throws CheckException {
		if (TRUE_RAW_VALUE.equals(aRawValue) || COMPATIBILITY_TRUE_RAW_VALUE.equals(aRawValue)) 
			return Boolean.TRUE;

		if (FALSE_RAW_VALUE.equals(aRawValue) || COMPATIBILITY_FALSE_RAW_VALUE.equals(aRawValue)) 
			return Boolean.FALSE;

		if (NONE_RAW_VALUE.equals(aRawValue)) 
			return null;

		// Should not happen:
		throw new CheckException(
			Resources.getInstance().getString(I18NConstants.FORMAT_INVALID__VALUE_EXAMPLE.fill(aRawValue, TRUE_RAW_VALUE + ", " + FALSE_RAW_VALUE)));
	}

	@Override
	protected String unparseString(Object aValue) {
		return getRawConstant((Boolean) aValue);
	}

	@Override
	protected Object narrowValue(Object aValue) {
		// Local temporary variable prevents the "unnecessary" cast warning.
		Boolean result = (Boolean) aValue;
		if (result == null) {
			return null;
		}

		if (result.booleanValue()) {
			if (result != Boolean.TRUE) {
				// Hurt greenhorns.
				Logger.warn("Useless Boolean instantiation.", new IllegalArgumentException("Value must be one of null, Boolean.TRUE, Boolean.FALSE."), BooleanField.class);
			}
			return Boolean.TRUE;
		} else {
			if (result != Boolean.FALSE) {
				// Hurt greenhorns.
				Logger.warn("Useless Boolean instantiation.", new IllegalArgumentException("Value must be one of null, Boolean.TRUE, Boolean.FALSE."), BooleanField.class);
			}
			return Boolean.FALSE;
		}
	}
	
	/**
	 * Convenience shortcut for {@link #setValue(Object)}.
	 */
	public void setAsBoolean(boolean aValue) {
		setValue(Boolean.valueOf(aValue));
	}

	/**
	 * Convenience shortcut for {@link #setValue(Object)}.
	 */
	public void setAsBoolean(Boolean aValue) {
		setValue(aValue);
	}

	/**
	 * Whether this field is not in the "undecided state" (see
	 *     {@link #NONE_RAW_VALUE}).
	 */
	public boolean hasBoolean() {
		return getValue() != null;
	}

	/**
	 * Shortcut for {@link #getValue()} in the case that this field
	 * {@link #hasBoolean() has a decided value}.
	 */
	public boolean getAsBoolean() {
		return ((Boolean) getValue()).booleanValue();
	}
	
	/**
	 * Translates a {@link Boolean} application value into its corresponding raw
	 * value.
	 */
	private static String getRawConstant(Boolean value) {
		if (value == null) return NONE_RAW_VALUE;
		if (value.booleanValue()) {
			return TRUE_RAW_VALUE;
		} else { 
			return FALSE_RAW_VALUE;
		}
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitBooleanField(this, arg);
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object aSecond) {
	    BooleanField theSecond = (BooleanField) aSecond;
	    Boolean theValue = (Boolean) this.getValue();
	    Boolean theSecondValue = theSecond == null ? null : (Boolean) theSecond.getValue();
	    if (theValue == null) {
	        theValue = Boolean.FALSE;
	    }
	    if (theSecondValue == null) {
	        theSecondValue = Boolean.FALSE;
	    }
	    if (theValue.equals(theSecondValue)) {
	        return 0;
	    }
	    if (theValue.booleanValue()) {
	        return 1;
	    } else {
	        return -1;
	    }
	}
}
