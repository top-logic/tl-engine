/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion.Comparision;
import com.top_logic.layout.scripting.recorder.gui.FieldCopier;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * A {@link FieldAssertionPlugin} for recording a {@link ValueAssertion} for the placeholder of a
 * form field.
 */
public class FieldPlaceholderAssertionPlugin extends FieldAssertionPlugin<FormField, FormField> {

	/**
	 * Creates a {@link FieldPlaceholderAssertionPlugin}.
	 * 
	 * @param model
	 *        The {@link FormField} to be inspected.
	 */
	public FieldPlaceholderAssertionPlugin(FormField model) {
		super(model, false, "fieldPlaceholder");
	}

	@Override
	protected FormField createValueField(String name) {
		FormField fieldValue = FieldCopier.copyField(getModel(), name);
		if (fieldValue == null) {
			throw new RuntimeException("Unable to copy such a field: "
				+ getModel().getClass().getCanonicalName());
		}

		fieldValue.initializeField(getModel().getPlaceholder());
		fieldValue.setPlaceholder(null);

		return fieldValue;
	}

	@Override
	protected Object getInitialValue() {
		return getModel().getPlaceholder();
	}

	@Override
	protected GuiAssertion buildAssertion(ModelName fieldModelName) {
		FormField field = getExpectedValueField();

		ModelName expectedValue = expectedValue(field);
		Comparision compareMode = FieldValueAssertionPlugin.compareMode(field);
		ValueAssertion valueAssertion =
			ActionFactory.fieldPlaceholderAssertion(fieldModelName, expectedValue, getComment());
		valueAssertion.setComparision(compareMode);
		return valueAssertion;
	}

	private ModelName expectedValue(FormField field) {
		Object expectedValue = field.getValue();
		return ModelResolver.buildModelName(field, expectedValue);
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_PLACEHOLDER;
	}

}