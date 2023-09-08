/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.button.ButtonTooltipElseLabelNaming.ButtonTooltipElseLabelName;

/**
 * {@link SingleValueAssertionPlugin} for the tooltip of a button, using the label as a fallback.
 * 
 * <p>
 * This action is necessary, as {@link AbstractButtonRenderer} uses the label as fallback for the
 * tooltip.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ButtonTooltipElseLabelAssertionPlugin extends LabeledButtonAssertionPlugin<StringField> {

	/**
	 * Creates a {@link ButtonTooltipElseLabelAssertionPlugin}.
	 */
	public ButtonTooltipElseLabelAssertionPlugin(CommandModel model, ModelName modelName, boolean assertByDefault) {
		super(model, modelName, assertByDefault, "buttonTooltipElseLabel");
	}

	@Override
	protected StringField createValueField(String name) {
		return FormFactory.newStringField(name);
	}

	@Override
	protected Object getInitialValue() {
		String tooltip = getModel().getTooltip();
		if (isEmpty(tooltip)) {
			return nonNull(getModel().getLabel());
		}
		return nonNull(tooltip);
	}

	@Override
	protected GuiAssertion buildAssertion() {
		String expectedValue = getExpectedValueField().getAsString();
		return ActionFactory.buttonAspectAssertion(ButtonTooltipElseLabelName.class, buttonName(), expectedValue,
			getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.BUTTON_TOOLTIP_ELSE_LABEL;
	}

}
