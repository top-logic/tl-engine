/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.button.ButtonTooltipCaptionNaming.ButtonTooltipCaptionName;

/**
 * {@link SingleValueAssertionPlugin} for the tooltip caption of a button.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ButtonTooltipCaptionAssertionPlugin extends LabeledButtonAssertionPlugin<StringField> {

	/**
	 * Creates a {@link ButtonTooltipCaptionAssertionPlugin}.
	 */
	public ButtonTooltipCaptionAssertionPlugin(CommandModel model, ModelName modelName, boolean assertByDefault) {
		super(model, modelName, assertByDefault, "buttonTooltipCaption");
	}

	@Override
	protected StringField createValueField(String name) {
		return FormFactory.newStringField(name);
	}

	@Override
	protected Object getInitialValue() {
		return getModel().getTooltipCaption();
	}

	@Override
	protected GuiAssertion buildAssertion() {
		String expectedValue = getExpectedValueField().getAsString();
		return ActionFactory.buttonAspectAssertion(ButtonTooltipCaptionName.class, buttonName(), expectedValue,
			getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.BUTTON_TOOLTIP_CAPTION;
	}

}
