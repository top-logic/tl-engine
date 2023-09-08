/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.AspectNaming;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} resolving the list of option labels of a given form field.
 * 
 * @see FieldOptionsNaming
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldOptionLabelsNaming extends AspectNaming<List<String>, FieldOptionLabelsNaming.Name, FormField> {

	/**
	 * Reference to the option labels of a given field.
	 * 
	 * @see #getModel()
	 */
	public interface Name extends AspectNaming.Name {
		// Pure marker interface.
	}

	@Override
	public Class<? extends Name> getNameClass() {
		return Name.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<List<String>> getModelClass() {
		return (Class) List.class;
	}

	@Override
	protected Class<FormField> baseType() {
		return FormField.class;
	}

	@Override
	protected List<String> localteModel(ActionContext context, Name name, FormField field) {
		List<?> options = SelectFieldUtils.getOptions(field);

		LabelProvider labelProvider = SelectFieldUtils.getOptionLabelProvider(field);

		ArrayList<String> result = new ArrayList<>(options.size());
		for (Object option : options) {
			result.add(labelProvider.getLabel(option));
		}
		return result;
	}

}
