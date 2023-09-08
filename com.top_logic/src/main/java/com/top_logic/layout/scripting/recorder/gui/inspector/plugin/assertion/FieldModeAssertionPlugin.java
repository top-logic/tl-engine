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
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.FieldMode;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.FormAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Is the {@link FormField} in the given {@link FieldMode}?
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FieldModeAssertionPlugin extends FieldAssertionPlugin<FormMember, SelectField> {

	public FieldModeAssertionPlugin(FormMember model) {
		super(model, false, "fieldMode");
	}

	@Override
	protected SelectField createValueField(String name) {
		SelectField fieldMode = FormFactory.newSelectField(name, Arrays.asList(FieldMode.values()));
		fieldMode.setMandatory(true);
		return fieldMode;
	}

	@Override
	protected Object getInitialValue() {
		return Collections.singletonList(FieldMode.getMode(getModel()));
	}

	@Override
	protected FormAssertion buildAssertion(ModelName fieldModelName) {
		FieldMode expectedMode = (FieldMode) getExpectedValueField().getSingleSelection();
		return ActionFactory.fieldModeAssertion(fieldModelName, expectedMode, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_MODE;
	}
}