/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.help;

import java.util.UUID;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.AbstractEditor;

/**
 * {@link AbstractEditor} that displays a {@link String} valued {@link PropertyDescriptor} as
 * boolean field.
 * 
 * <p>
 * When the field is set to <code>true</code> a {@link UUID random UUID} is stored, if the field is
 * set to <code>false</code>, <code>null</code> is stored.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UseHelpEditor extends AbstractEditor {

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		BooleanField field = Fields.checkbox(container, fieldName);
		
		init(editorFactory, model, field,
			input -> !StringServices.isEmpty(input),
			new StorageConversion(model.getValue()));

		return field;
	}

	private static class StorageConversion implements Mapping<Object, Object> {

		private Object _initialValue;

		public StorageConversion(Object value) {
			_initialValue = value;
		}

		@Override
		public Object map(Object input) {
			if (Utils.isTrue((Boolean) input)) {
				if (Utils.isEmpty(_initialValue)) {
					// Store UUID to ensure it is not created twice.
					_initialValue = UUID.randomUUID().toString();
				}
				return _initialValue;
			}
			return null;
		}

	}

}

