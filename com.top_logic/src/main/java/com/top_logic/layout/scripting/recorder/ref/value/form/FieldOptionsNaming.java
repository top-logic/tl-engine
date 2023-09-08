/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.form;

import java.util.List;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.AspectNaming;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for resolving the options of a given form field.
 * 
 * @see FieldOptionLabelsNaming
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldOptionsNaming extends AspectNaming<List<?>, FieldOptionsNaming.Name, FormField> {

	/**
	 * Reference to the option list of a given field.
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

	@Override
	protected Class<FormField> baseType() {
		return FormField.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<List<?>> getModelClass() {
		return (Class) List.class;
	}

	@Override
	protected List<?> localteModel(ActionContext context, Name name, FormField field) {
		return SelectFieldUtils.getOptions(field);
	}

}
