/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.form.SelectFieldOptionsNaming.SelectFieldOptionsName;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;

/**
 * {@link FieldAssertionPlugin} to create an assertion for options of a {@link SelectField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FieldOptionsAssertionPlugin extends FieldAssertionPlugin<SelectField, SelectField> {

	/**
	 * Creates a new {@link FieldOptionsAssertionPlugin}.
	 */
	public FieldOptionsAssertionPlugin(SelectField model) {
		super(model, false, "fieldOptions");
		if (model.getOptionModel() instanceof TreeOptionModel<?>) {
			throw new IllegalArgumentException(
				"Assertion for options of tree-options field not supported: " + model.getName());
		}
	}

	@Override
	protected SelectField createValueField(String name) {
		SelectField valueField =
			FormFactory.newSelectField(name, getInitialValue(), FormFactory.MULTIPLE, !FormFactory.IMMUTABLE);
		valueField.setControlProvider(SelectionControlProvider.INSTANCE);
		valueField.setMandatory(true);
		return valueField;
	}

	@Override
	protected Collection<?> getInitialValue() {
		SelectField model = getModel();
		if (!model.hasValue()) {
			return Collections.emptyList();
		}
		return SelectFieldUtils.getOptionAndSelectionOuterJoinOrdered(model);
	}

	@Override
	protected GuiAssertion buildAssertion(ModelName fieldModelName) {
		SelectFieldOptionsName fieldModelOptionName = TypedConfiguration.newConfigItem(SelectFieldOptionsName.class);
		fieldModelOptionName.setFormMember(fieldModelName);
		if (!getExpectedValueField().hasValue()) {
			throw new ApplicationAssertion("Expected options can not be determined.");
		}
		List<?> expectedOptions = getExpectedValueField().getSelection();
		Maybe<? extends ModelName> optionsName = ModelResolver.buildModelNameIfAvailable(expectedOptions);
		if (!optionsName.hasValue()) {
			throw new ApplicationAssertion("Unable to get name for options: " + expectedOptions);
		}
		return ActionFactory.valueAssertion(optionsName.get(), fieldModelOptionName, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_OPTIONS;
	}

}