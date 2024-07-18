/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;

/**
 * Common base class for {@link FieldProvider} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFieldProvider implements FieldProvider {

	@Override
	public final FormMember getFormField(EditContext editContext, String fieldName) {
		FormMember field = createFormField(editContext, fieldName);
		initValue(editContext, field);
		return field;
	}

	/**
	 * Sets the given field's value to the value from the {@link EditContext}.
	 */
	protected void initValue(EditContext editContext, FormMember field) {
		if (field instanceof FormField) {
			AttributeFormFactory.initFieldValue(editContext, (FormField) field);
		}
	}

	/**
	 * Creates a new {@link FormField} with the given name suitable to represent a value from the
	 * given {@link EditContext}.
	 */
	protected abstract FormMember createFormField(EditContext editContext, String fieldName);

	@Override
	public final Unimplementable unimplementable() {
		return null;
	}

}
