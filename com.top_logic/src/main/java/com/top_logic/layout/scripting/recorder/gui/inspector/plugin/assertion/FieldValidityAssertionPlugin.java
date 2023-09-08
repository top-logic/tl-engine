/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.Arrays;
import java.util.Collections;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.FieldValidity;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.FieldAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Has the {@link FormField} the given {@link FieldValidity}?
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FieldValidityAssertionPlugin extends FieldAssertionPlugin<FormField, SelectField> {

	public FieldValidityAssertionPlugin(FormField model) {
		super(model, false, "fieldValidity");
	}

	@Override
	protected SelectField createValueField(String name) {
		SelectField fieldValidity = FormFactory.newSelectField(name, Arrays.asList(FieldValidity.values()));
		fieldValidity.setMandatory(true);
		return fieldValidity;
	}

	@Override
	protected Object getInitialValue() {
		return Collections.singletonList(FieldValidity.getValidity(getModel()));
	}

	@Override
	protected FieldAssertion buildAssertion(ModelName fieldModelName) {
		FieldValidity expectedValidity = (FieldValidity) getExpectedValueField().getSingleSelection();
		return ActionFactory.fieldValidityAssertion(fieldModelName, expectedValidity, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_VALIDITY;
	}

}