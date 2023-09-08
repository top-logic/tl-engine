/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.base.security.util.Password;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * {@link FormField} implementation for entering passwords.
 * 
 * <p>
 * Note: The password value is never shown at the UI. The field's value is encapsulated in a
 * {@link Password} object.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see FormFactory#newPasswordField(String, boolean, boolean, Constraint)
 * @see Password
 */
public class PasswordField extends AbstractFormField {

	/**
	 * Creates a {@link #getNormalizeInput() normalizing} {@link PasswordField}
	 * 
	 * @see AbstractFormField#AbstractFormField(String, boolean, boolean, boolean, Constraint)
	 * 
	 * @see FormFactory#newPasswordField(String, boolean, boolean, Constraint)
	 */
	protected PasswordField(String name, boolean mandatory, boolean immutable, Constraint constraint) {
		super(name, mandatory, immutable, !NORMALIZE, constraint);
	}

	@Override
	protected Object narrowValue(Object aValue) {
		// Local temporary variable prevents the "unnecessary" cast warning.
		Password result = (Password) aValue;
		return result;
	}

	@Override
	protected Object parseRawValue(Object aRawValue) throws CheckException {
		if (aRawValue == NO_RAW_VALUE) {
			return null;
		} else {
			String plainText = (String) aRawValue;
			return Password.fromPlainText(plainText);
		}
	}

	@Override
	protected Object unparseValue(Object aValue) {
		if (aValue == null) {
			return NO_RAW_VALUE;
		}
		return Password.PASSWORD_REPLACEMENT;
	}

	@Override
	protected boolean canRecordRawValue() {
		return true;
	}

	/**
	 * Visits this {@link PasswordField} with the given {@link FormMemberVisitor}.
	 */
    @Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitPasswordField(this, arg);
    }

}
