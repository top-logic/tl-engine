/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.form.FormField;

/**
 * {@link Mapping} of {@link FormField}s to their {@link FormField#getValue()}.
 * 
 * @see BusinessModelMapping
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormFieldValueMapping implements Mapping<FormField, Object> {

	/**
	 * Singleton {@link FormFieldValueMapping} instance.
	 */
	public static final FormFieldValueMapping INSTANCE = new FormFieldValueMapping();

	private FormFieldValueMapping() {
		// Singleton constructor.
	}

	@Override
	public Object map(FormField input) {
		if (input == null || !input.hasValue()) {
			return null;
		}

		return input.getValue();
	}

}
