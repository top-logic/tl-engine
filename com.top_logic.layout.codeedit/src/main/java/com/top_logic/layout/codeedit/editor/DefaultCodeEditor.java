/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.codeedit.editor;

import java.text.Format;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.func.Identity;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.codeedit.control.EditorControlConfig;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.AbstractEditor;
import com.top_logic.layout.form.values.edit.editor.ConfigurationFormatAdapter;

/**
 * {@link AbstractEditor} to display a {@link String} valued property with a
 * {@link CodeEditorControl}.
 * 
 * @see EditorControlConfig Configuration of the {@link CodeEditorControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultCodeEditor extends AbstractEditor {

	/**
	 * Singleton {@link DefaultCodeEditor} instance.
	 */
	public static final DefaultCodeEditor INSTANCE = new DefaultCodeEditor();

	private DefaultCodeEditor() {
		// Singleton constructor.
	}

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		PropertyDescriptor property = model.getProperty();
		EditorControlConfig controlConfig = editorFactory.getAnnotation(property, EditorControlConfig.class);
		ControlProvider editorCP;
		if (controlConfig == null) {
			editorCP = CodeEditorControl.CP.INSTANCE;
		} else {
			editorCP = new CodeEditorControl.CP(controlConfig.language()).warnLevel(controlConfig.warnLevel());
		}

		FormField field;
		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<Object> valueProvider = property.getValueProvider();
		if (isString(valueProvider)) {
			field = Fields.line(container, fieldName);
		} else {
			Format format = new ConfigurationFormatAdapter(valueProvider);
			field = Fields.complex(container, fieldName, format);
		}

		init(editorFactory, model, field, Identity.getInstance(), Identity.getInstance());

		// Note: The control provider must be set last to override a control provider associated
		// with the field by default.
		field.setControlProvider(editorCP);

		return field;
	}

	private boolean isString(ConfigurationValueProvider<?> valueProvider) {
		return valueProvider == StringValueProvider.INSTANCE;
	}

}

