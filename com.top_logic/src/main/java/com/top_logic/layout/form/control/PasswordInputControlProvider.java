/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.PasswordField;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ControlProvider} for {@link PasswordInputControl}
 * 
 * @see PasswordField
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class PasswordInputControlProvider extends AbstractFormFieldControlProvider {
	
	/**
	 * Singleton {@link PasswordInputControlProvider} instance.
	 */
	public static final PasswordInputControlProvider INSTANCE = new PasswordInputControlProvider();

	private PasswordInputControlProvider() {
		// Singleton constructor.
	}

	private int _columns;

	private int _minCharLength;

	private int _criteriaCount;
	
	/**
	 * Create a new PasswordInputControlProvider
	 */
	public PasswordInputControlProvider(int columns, int minCharLength, int criteriaCount) {
		_columns = columns;
		_minCharLength = minCharLength;
		_criteriaCount = criteriaCount;
	}
	
	@Override
	protected Control createInput(FormMember member) {
		return createPasswordControl((FormField) member, _minCharLength, _criteriaCount, _columns);
	}

	/**
	 * Creates a {@link Control} for the given password field.
	 */
	public static Control createPasswordControl(FormField field, int minLength, int criteriaCount, int columns) {
		if (minLength == 0 && criteriaCount == 0) {
			TextInputControl result = new TextInputControl(field);
			result.setColumns(columns);
			result.setType(HTMLConstants.PASSWORD_TYPE_VALUE);
			return result;
		}
		PasswordInputControl passwordInputControl = new PasswordInputControl(field, minLength, criteriaCount);
		passwordInputControl.setColumns(columns);
		return passwordInputControl;
	}
}
