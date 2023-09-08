/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.scripting;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.wysiwyg.ui.StructuredText;

/**
 * {@link ModelNamingScheme} of {@link StructuredText} values.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StructuredTextNamingScheme
		extends ModelNamingScheme<FormField, StructuredText, StructuredTextNamingScheme.Name> {

	/**
	 * {@link ModelName} of {@link StructuredTextNamingScheme}.
	 */
	public interface Name extends ContextDependent {

		/**
		 * The literal value.
		 */
		String getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(String value);

	}

	/**
	 * Creates a {@link StructuredTextNamingScheme}.
	 */
	public StructuredTextNamingScheme() {
		super(StructuredText.class, Name.class, FormField.class);
	}

	@Override
	public StructuredText locateModel(ActionContext context, FormField valueContext, Name name) {
		Object value = valueContext.getValue();
		if (value == null) {
			return new StructuredText(name.getValue());
		} else {
			StructuredText oldStructuredText = (StructuredText) value;
			return new StructuredText(name.getValue(), oldStructuredText.getImages());
		}
	}

	@Override
	protected Maybe<Name> buildName(FormField valueContext, StructuredText model) {
		Name name = createName();
		name.setValue(model.getSourceCode());
		return Maybe.some(name);
	}
}
