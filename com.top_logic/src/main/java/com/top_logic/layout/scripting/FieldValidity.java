/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting;

import com.top_logic.layout.form.FormField;

/**
 * Represents the three validity states of a {@link FormField}:
 * <ul>
 * <li>{@link #VALID}</li>
 * <li>{@link #WARNING}</li>
 * <li>{@link #ERROR}</li>
 * </ul>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public enum FieldValidity {

	/**
	 * A {@link FormField} is {@link #VALID}, if and only if it has neither
	 * {@link FormField#hasError() errors} nor {@link FormField#hasWarnings() warnings}.
	 */
	VALID,

	/**
	 * A {@link FormField} is {@link #WARNING}, if and only if it has no
	 * {@link FormField#hasError() errors} but {@link FormField#hasWarnings() warnings}.
	 */
	WARNING,

	/**
	 * A {@link FormField} is {@link #ERROR}, if and only if it {@link FormField#hasError() has
	 * errors}.
	 */
	ERROR;

	public static FieldValidity getValidity(FormField field) {
		assert field.hasError() != field.isValid();
		if (field.hasError()) {
			return FieldValidity.ERROR;
		} else if (field.hasWarnings()) {
			return FieldValidity.WARNING;
		} else {
			return FieldValidity.VALID;
		}
	}

}
