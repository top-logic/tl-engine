/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.values.Fields.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * Creates the edit UI for {@link PropertyDescriptor} of kind {@link PropertyKind#COMPLEX} with a
 * {@link MapBinding} or {@link ListBinding}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ComplexEditor implements Editor {

	private static final String LIST_CONTENT_GROUP = "content";

	/**
	 * Singleton {@link ComplexEditor} instance.
	 */
	public static final ComplexEditor INSTANCE = new ComplexEditor();

	private ComplexEditor() {
		// Singleton constructor.
	}

	@Override
	public FormMember createUI(EditorFactory factory, FormContainer container, final ValueModel valueModel) {
		FormGroup rootGroup = group(container, factory, valueModel.getProperty());
		FormGroup contentGroup = group(rootGroup, LIST_CONTENT_GROUP);

		Object value = valueModel.getValue();

		boolean minimized = Fields.displayMinimized(factory, valueModel.getProperty());
		if (value instanceof Map) {
			template(rootGroup, EditorUtils.mapTemplate(rootGroup, minimized));

			createFormGroup(new MapFormGroupBuilder(factory, valueModel, contentGroup), ((Map<?, ?>) value).entrySet());
		} else if (value instanceof List) {
			template(rootGroup, EditorUtils.listTemplate(rootGroup, minimized));

			createFormGroup(new ListFormGroupBuilder(factory, valueModel, contentGroup), (List<?>) value);
		}
		EditorUtils.addAdditionalChangedField(rootGroup, contentGroup, factory, valueModel);
		return rootGroup;
	}

	private void createFormGroup(FormGroupBuilder builder, Collection<?> values) {
		for (Object value : values) {
			builder.add(value);
		}

		builder.addAddEntryCommand();
	}

}
