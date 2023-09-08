/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import static com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator.*;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.form.FormFieldMandatoryNaming.FormFieldMandatoryName;

/**
 * {@link FieldAssertionPlugin} to assert {@link FormField#isMandatory()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FieldMandatoryAssertionPlugin extends FieldAssertionPlugin<FormField, BooleanField> {

	/**
	 * Creates a new {@link FieldMandatoryAssertionPlugin}.
	 */
	public FieldMandatoryAssertionPlugin(FormField model, boolean assertByDefault) {
		super(model, assertByDefault, "field-mandatory");
	}

	@Override
	protected GuiAssertion buildAssertion(ModelName formMemberName) {
		boolean expectedValue = getExpectedValueField().getAsBoolean();
		ModelName actualValueRef = formMemberAspectName(FormFieldMandatoryName.class, formMemberName);

		return ActionFactory.equalsCheck(expectedValue, actualValueRef, getComment());
	}

	@Override
	protected BooleanField createValueField(String name) {
		return FormFactory.newBooleanField(name);
	}

	@Override
	protected Object getInitialValue() {
		return getModel().isMandatory();
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants.FIELD_MANDATORY;
	}

}

