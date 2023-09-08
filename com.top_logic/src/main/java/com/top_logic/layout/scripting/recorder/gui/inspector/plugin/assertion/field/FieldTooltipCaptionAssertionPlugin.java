/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.field;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.FieldAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.form.FormMemberTooltipCaptionNaming.FormMemberTooltipCaptionName;

/**
 * {@link FieldAssertionPlugin} for the tooltip caption.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FieldTooltipCaptionAssertionPlugin extends FieldAssertionPlugin<FormMember, StringField> {

	/**
	 * Creates a {@link FieldTooltipCaptionAssertionPlugin}.
	 */
	public FieldTooltipCaptionAssertionPlugin(FormMember model) {
		super(model, false, "fieldTooltipCaption");
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
	protected GuiAssertion buildAssertion(ModelName fieldModelName) {
		String expectedValue = (String) getExpectedValueField().getValue();
		return ActionFactory.formMemberAspectAssertion(
			FormMemberTooltipCaptionName.class, expectedValue, fieldModelName, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_TOOLTIP_CAPTION;
	}

}
