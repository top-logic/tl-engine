/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.PasswordInputControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * {@link Editor} creating the UI for a read-only derived property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ValueDisplay implements Editor {

	/**
	 * Singleton {@link ValueDisplay} instance.
	 */
	public static final ValueDisplay INSTANCE = new ValueDisplay();

	private ValueDisplay() {
		// Singleton constructor.
	}

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel valueModel) {
		PropertyDescriptor property = valueModel.getProperty();
		FormField field = immutable(field(container, property.getPropertyName()));
		bindValue(field, configurationValue(valueModel.getModel(), property));

		if (editorFactory.getAnnotation(property, Encrypted.class) != null) {
			field.setControlProvider(PasswordInputControlProvider.INSTANCE);
		}

		editorFactory.processControlProviderAnnotation(valueModel.getProperty(), field);
		return field;
	}

}
