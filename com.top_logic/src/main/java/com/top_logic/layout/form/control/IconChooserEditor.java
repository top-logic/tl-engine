/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.func.Identity;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.format.ThemeImageFormat;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.values.ModifiableValue;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.Editor;

/**
 * {@link Editor} for displaying properties of type {@link ThemeImage}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">iwi</a>
 */
public class IconChooserEditor implements Editor {

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model) {
		final PropertyDescriptor property = model.getProperty();
		String fieldName = fieldName(property);
		ComplexField field = FormFactory.newComplexField(fieldName, ThemeImageFormat.INSTANCE);
		container.addMember(field);

		ModifiableValue<Object> input = fieldValue(field);
		ModifiableValue<Object> modelValue = configurationValue(model.getModel(), property);
		bindValue(field, modelValue, Identity.INSTANCE);
		if (property.canHaveSetter()) {
			linkStorage(input, modelValue, Identity.INSTANCE);
		}

		editorFactory.processControlProviderAnnotation(model.getProperty(), field);
		return field;
	}

}
