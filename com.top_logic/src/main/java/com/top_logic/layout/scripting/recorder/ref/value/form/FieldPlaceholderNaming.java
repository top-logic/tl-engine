/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.form;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.AspectNaming;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for resolving the placeholder of a given form field.
 * 
 * @see FormField#getPlaceholder()
 * @see FieldValueNaming
 */
public class FieldPlaceholderNaming extends AspectNaming<Object, FieldPlaceholderNaming.Name, FormField> {

	/**
	 * Reference to the placeholder values of a given field.
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
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	protected Class<FormField> baseType() {
		return FormField.class;
	}

	@Override
	protected Object localteModel(ActionContext context, Name name, FormField field) {
		return field.getPlaceholder();
	}

}
