/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.func.Identity;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.gui.layout.upload.FileNameConstraint;
import com.top_logic.knowledge.gui.layout.upload.FileNameStrategy;
import com.top_logic.knowledge.gui.layout.upload.NoNameCheck;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;

/**
 * {@link Editor} allowing to upload files to declarative forms.
 * 
 * <p>
 * Must be annotated to a {@link BinaryData} property using {@link PropertyEditor}.
 * </p>
 * 
 * <p>
 * Note: The property must also be annotated {@link InstanceFormat} and {@link ItemDisplay}
 * {@link com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType#VALUE}.
 * </p>
 * 
 * @see AcceptedTypes
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BinaryDataEditor extends AbstractEditor {

	/**
	 * Singleton {@link BinaryDataEditor} instance.
	 */
	public static final BinaryDataEditor INSTANCE = new BinaryDataEditor();

	private BinaryDataEditor() {
		// Singleton constructor.
	}

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {

		DataField field = FormFactory.newDataField(fieldName, model.getProperty().isMultiple());

		PropertyDescriptor property = model.getProperty();
		AcceptedTypes acceptedTypes = property.getAnnotation(AcceptedTypes.class);
		if (acceptedTypes != null) {
			field.setAcceptedTypes(StringServices.toString(acceptedTypes.value(), ", "));

			Class<? extends FileNameStrategy> checkerClass = acceptedTypes.checker();
			if (checkerClass != NoNameCheck.class) {
				try {
					FileNameStrategy checker = ConfigUtil.getInstance(checkerClass);

					FileNameConstraint constraint = new FileNameConstraint(checker);
					field.setFileNameConstraint(constraint);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
			}
		}
		init(editorFactory, model, field, Identity.INSTANCE,
			model.getProperty().isMultiple() ? CollectionUtil::asList : CollectionUtil::getSingleValueFrom);

		container.addMember(field);
		return field;
	}

}
