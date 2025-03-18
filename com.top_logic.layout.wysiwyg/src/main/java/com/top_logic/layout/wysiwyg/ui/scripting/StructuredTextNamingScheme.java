/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.scripting;

import com.top_logic.basic.StringServices;
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
		 * 
		 * @deprecated For compatibility with existing tests. Use {@link #getText()} instead
		 */
		@Deprecated
		String getValue();

		/**
		 * The represented {@link StructuredText}.
		 */
		StructuredText getText();

		/**
		 * Setter for {@link #getText()}.
		 */
		void setText(StructuredText text);
	}

	/**
	 * Creates a {@link StructuredTextNamingScheme}.
	 */
	public StructuredTextNamingScheme() {
		super(StructuredText.class, Name.class, FormField.class);
	}

	@Override
	public StructuredText locateModel(ActionContext context, FormField valueContext, Name name) {
		String nameValue = name.getValue();
		if (!StringServices.isEmpty(nameValue)) {
			Object value = valueContext.getValue();
			if (value == null) {
				return new StructuredText(nameValue);
			} else {
				StructuredText oldStructuredText = (StructuredText) value;
				return new StructuredText(nameValue, oldStructuredText.getImages());
			}
		}
		StructuredText text = name.getText();
		return text == null ? new StructuredText() : text.copy();
	}

	@Override
	protected Maybe<Name> buildName(FormField valueContext, StructuredText model) {
		Name name = createName();
		name.setText(model);
		return Maybe.some(name);
	}
}
