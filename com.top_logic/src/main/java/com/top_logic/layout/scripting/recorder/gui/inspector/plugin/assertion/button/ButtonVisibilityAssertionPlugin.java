/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.button.ButtonVisibilityNaming.ButtonVisibilityName;

/**
 * {@link SingleValueAssertionPlugin} for the label of a button.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ButtonVisibilityAssertionPlugin extends LabeledButtonAssertionPlugin<BooleanField> {

	/**
	 * Creates a {@link ButtonVisibilityAssertionPlugin}.
	 */
	public ButtonVisibilityAssertionPlugin(CommandModel model, ModelName modelName, boolean assertByDefault) {
		super(model, modelName, assertByDefault, "buttonVisibility");
	}

	@Override
	protected BooleanField createValueField(String name) {
		return FormFactory.newBooleanField(name);
	}

	@Override
	protected Object getInitialValue() {
		return getModel().isVisible();
	}

	@Override
	protected GuiAssertion buildAssertion() {
		Boolean expectedValue = getExpectedValueField().getAsBoolean();
		return ActionFactory.buttonAspectAssertion(ButtonVisibilityName.class, buttonName(), expectedValue,
			getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.BUTTON_VISIBILITY;
	}

}
