/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.codeedit.editor;

import java.text.Format;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.func.Identity;
import com.top_logic.layout.Control;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.AbstractEditor;
import com.top_logic.layout.form.values.edit.editor.Editor;

/**
 * {@link Editor} for a {@link PropertyDescriptor} contains a {@link ConfigurationItem} displayed as
 * XML.
 * 
 * <p>
 * To use this editor, you must annotate the property's getter with:
 * </p>
 * 
 * <pre>
 * &#64;{@link PropertyEditor}({@link ConfigXMLEditor}.class)
 * &#64;{@link ControlProvider}({@link ConfigXMLEditor}.{@link CP}.class)
 * &#64;{@link ItemDisplay}({@link ItemDisplayType}.VALUE)
 * </pre>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigXMLEditor extends AbstractEditor {

	/**
	 * {@link ControlProvider} for displaying an XML code editor.
	 */
	public static final class CP extends AbstractFormFieldControlProvider {
		@Override
		protected Control createInput(FormMember member) {
			return new CodeEditorControl((FormField) member, CodeEditorControl.MODE_XML);
		}
	}

	/**
	 * Singleton {@link ConfigXMLEditor} instance.
	 */
	public static final ConfigXMLEditor INSTANCE = new ConfigXMLEditor();

	private ConfigXMLEditor() {
		// Singleton constructor.
	}

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {

		PropertyDescriptor property = model.getProperty();
		Format format = new ConfigXMLFormat(property);
		FormField field = FormFactory.newComplexField(fieldName, format);
		container.addMember(field);
		init(editorFactory, model, field, Identity.getInstance(), Identity.getInstance());

		return field;
	}
}
