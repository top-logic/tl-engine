/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.recorder.gui.FieldCopier;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.FieldChangeInfo;
import com.top_logic.layout.scripting.recorder.ref.value.FieldCompareValue;

/**
 * {@link CompareValueAssertionPlugin} for comparison in {@link FormField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareValueFormPlugin extends CompareValueAssertionPlugin<FormField> {

	private final FormField _compareModel;

	/**
	 * Creates a new {@link CompareValueFormPlugin}.
	 * 
	 * @param model
	 *        see {@link #getModel()}
	 * @param compareModel
	 *        The current comparison field.
	 * @param compareInfo
	 *        see
	 *        {@link CompareValueAssertionPlugin#CompareValueAssertionPlugin(Object, CompareInfo)}
	 */
	public CompareValueFormPlugin(FormField model, FormField compareModel, CompareInfo compareInfo) {
		super(model, compareInfo);
		_compareModel = compareModel;
	}

	@Override
	protected FormField createCompareValueField(String fieldCompareName) {
		return FieldCopier.copyField(_compareModel, fieldCompareName);
	}

	@Override
	protected ValueAssertion assertSameChangeInfo() {
		ModelName actualValue = ReferenceInstantiator.fieldAspect(FieldChangeInfo.class, getModel());
		return ActionFactory.valueAssertion(ModelResolver.buildModelName(expectedChangeInfo()), actualValue,
			getComment());
	}

	@Override
	protected ValueAssertion assertSameCompareObject() {
		ModelName actualValue = ReferenceInstantiator.fieldAspect(FieldCompareValue.class, getModel());
		return ActionFactory.valueAssertion(ModelResolver.buildModelName(_compareField.getValue()), actualValue,
			getComment());
	}

}

