/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.PasswordInputControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link DisplayProvider} for password fields.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PasswordTagProvider implements DisplayProvider, ControlProvider {

	/**
	 * Singleton {@link PasswordTagProvider} instance.
	 */
	public static final PasswordTagProvider INSTANCE = new PasswordTagProvider();

	private PasswordTagProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		int columns = DisplayAnnotations.inputSize(editContext, 0);
		return PasswordInputControlProvider.createPasswordControl((FormField) member, 0, 0, columns);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		int columns = DisplayAnnotations.inputSize(editContext, 0);
		return PasswordInputControlProvider.createPasswordControl((FormField) member, 0, 0, columns);
	}

	@Override
	public Control createControl(Object model, String style) {
		return new PasswordInputControlProvider(0, 0, 0).createControl(model, style);
	}

	@Override
	public HTMLFragment createFragment(Object model, String style) {
		return new PasswordInputControlProvider(0, 0, 0).createFragment(model, style);
	}

}
