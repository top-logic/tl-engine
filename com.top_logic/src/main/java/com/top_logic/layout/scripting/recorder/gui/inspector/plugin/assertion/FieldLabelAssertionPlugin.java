/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.FormAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Has the {@link FormField} the given label?
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FieldLabelAssertionPlugin extends FieldAssertionPlugin<FormMember, StringField> {

	public FieldLabelAssertionPlugin(FormMember model) {
		super(model, false, "fieldLabel");
	}

	@Override
	protected StringField createValueField(String name) {
		StringField fieldLabel = FormFactory.newStringField(name);
		return fieldLabel;
	}

	@Override
	protected Object getInitialValue() {
		return getModel().getLabel();
	}

	@Override
	protected FormAssertion buildAssertion(ModelName fieldModelName) {
		String expectedLabel = (String) getExpectedValueField().getValue();
		return ActionFactory.fieldLabelAssertion(fieldModelName, expectedLabel, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_LABEL;
	}
}