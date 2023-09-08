/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.FieldAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * {@link FieldAssertionPlugin} to create an assertion for the error of a {@link FormField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FieldErrorAssertionPlugin extends FieldAssertionPlugin<FormField, StringField> {

	/**
	 * Creates a new {@link FieldErrorAssertionPlugin}.
	 */
	public FieldErrorAssertionPlugin(FormField model) {
		super(model, false, "fieldError");
	}

	@Override
	protected StringField createValueField(String name) {
		StringField errorField = FormFactory.newStringField(name);
		errorField.setMandatory(true);
		return errorField;
	}

	@Override
	protected Object getInitialValue() {
		FormField model = getModel();
		if (!model.hasError()) {
			return StringServices.EMPTY_STRING;
		}
		return model.getError();
	}

	@Override
	protected FieldAssertion buildAssertion(ModelName fieldModelName) {
		String error = getExpectedValueField().getAsString();
		return ActionFactory.fieldErrorAssertion(fieldModelName, error, null, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_ERROR;
	}

}