/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FuzzyFormMemberNaming;
import com.top_logic.layout.form.FuzzyFormMemberNaming.Name;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.ModelNotExistsAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The given {@link FormField} must not exist!
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldNotExistsAssertionPlugin extends FieldAssertionPlugin<FormMember, StringField> {

	/**
	 * Creates a {@link FieldNotExistsAssertionPlugin}.
	 */
	public FieldNotExistsAssertionPlugin(FormMember model) {
		super(model, false, "fieldExistance");
	}

	@Override
	protected StringField createValueField(String name) {
		StringField field = FormFactory.newStringField(name);
		field.setImmutable(true);
		return field;
	}

	@Override
	protected Object getInitialValue() {
		return "";
	}

	@Override
	protected ModelNotExistsAssertion buildAssertion(ModelName fieldModelName) {
		String failurePattern;
		if (fieldModelName instanceof FuzzyFormMemberNaming.Name) {
			failurePattern = FuzzyFormMemberNaming.getFailedMessagePrefix((Name) fieldModelName);
		} else {
			failurePattern = "There is no member named '" + getModel().getName() + "' in this group";
		}
		return ActionFactory.modelNotExistsAssertion(fieldModelName, failurePattern, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.FIELD_EXISTANCE;
	}

}