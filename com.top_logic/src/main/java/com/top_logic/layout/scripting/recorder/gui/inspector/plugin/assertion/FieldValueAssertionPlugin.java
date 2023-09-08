/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion.Comparision;
import com.top_logic.layout.scripting.recorder.gui.FieldCopier;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * A {@link FieldAssertionPlugin} for recording a {@link ValueAssertion} for the field value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FieldValueAssertionPlugin extends FieldAssertionPlugin<FormField, FormField> {

	/**
	 * Creates a {@link FieldValueAssertionPlugin}.
	 * 
	 * @param model
	 *        The {@link FormField} to be inspected. Is not allowed to be null.
	 */
	public FieldValueAssertionPlugin(FormField model) {
		super(model, true, "fieldValue");
	}

	@Override
	protected FormField createValueField(String name) {
		FormField fieldValue = FieldCopier.copyField(getModel(), name);
		if (fieldValue == null) {
			throw new RuntimeException("Unable to copy such a field: "
				+ getModel().getClass().getCanonicalName());
		}
		return fieldValue;
	}

	@Override
	protected Object getInitialValue() {
		return getModel().getValue();
	}

	@Override
	protected GuiAssertion buildAssertion(ModelName fieldModelName) {
		FormField field = getExpectedValueField();

		// Note: The field's raw value is at least for SelectField instances not useful for
		// identifying the user input.
		boolean useRawValue = !(field instanceof SelectField);

		ModelName expectedValue = expectedValue(field, useRawValue);
		Comparision compareMode = compareMode(field);
		ValueAssertion valueAssertion =
			ActionFactory.fieldValueAssertion(fieldModelName, expectedValue, useRawValue, getComment());
		valueAssertion.setComparision(compareMode);
		return valueAssertion;
	}

	private Comparision compareMode(FormField field) {
		Comparision compareMode = Comparision.EQUALS;
		if (field instanceof SelectField && !((SelectField) field).hasCustomOrder()) {
			compareMode = Comparision.MULTI_SET_EQUALS;
		}
		return compareMode;
	}

	private ModelName expectedValue(FormField field, boolean useRawValue) {
		Object expectedValue;
		if (useRawValue) {
			expectedValue = field.getRawValue();
		} else {
			expectedValue = field.getValue();
		}
		return ModelResolver.buildModelName(field, expectedValue);
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_VALUE;
	}

}