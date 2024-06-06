/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link FormGroupBuilder} creating the form for {@link List} entries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ListFormGroupBuilder extends FormGroupBuilder {

	private Optional<ConfigurationValueProvider<?>> _valueProvider;

	/**
	 * Creates a {@link FormGroupBuilder} for {@link List} entries.
	 */
	public ListFormGroupBuilder(EditorFactory editorFactory, ValueModel valueModel, FormGroup content) {
		super(editorFactory, valueModel, content);

		initBinding(editorFactory, valueModel);
	}

	private void initBinding(EditorFactory editorFactory, ValueModel valueModel) {
		ListBinding binding = editorFactory.getAnnotation(valueModel.getProperty(), ListBinding.class);

		_valueProvider = EditorUtils.createValueProvider(binding.format());
	}

	@Override
	public void add(Object value) {
		int id = getId();
		final FormGroup elementGroup = group(getParent(), EditorUtils.LIST_ELEMENT_PREFIX + id);
		setId(id + 1);

		elementGroup.setStableIdSpecialCaseMarker(getParent().size() - 1);

		FormGroup contentGroup = group(elementGroup, EditorUtils.LIST_ITEM_GROUP);
		createEntryField(value, elementGroup, contentGroup);

		addRemoveButton(elementGroup, null);
	}

	private FormField createEntryField(Object value, final FormGroup elementGroup, FormGroup contentGroup) {
		FormField valueField = createListEntryField(contentGroup);

		valueField.setMandatory(true);
		valueField.initializeField(value);
		valueField.addValueListener(createValueListener(elementGroup));

		return valueField;
	}

	private ValueListener createValueListener(final FormGroup elementGroup) {
		return new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List<Object> list = new ArrayList<>((List<?>) getValueModel().getValue());
				Integer stableIdSpecialCaseMarker = (Integer) elementGroup.getStableIdSpecialCaseMarker();
				list.set(stableIdSpecialCaseMarker, newValue);
				getValueModel().setValue(list);
			}

		};
	}

	private FormField createListEntryField(FormGroup contentGroup) {
		Class<?> entryType = getValueModel().getProperty().getElementType();
		String entryName = EditorUtils.LIST_VALUE_MEMBER_NAME;

		return EditorUtils.createPrimitiveField(contentGroup, entryName, entryType, _valueProvider);
	}

	@Override
	protected void addAddEntryCommand() {
		Command addCommand = new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				List<Object> list = new LinkedList<>((List<?>) getValueModel().getValue());
				list.add(null);

				getValueModel().setValue(list);

				add(null);

				return HandlerResult.DEFAULT_RESULT;
			}
		};

		addCommand((FormGroup) getParent().getParent(), isReadOnly(), getValueModel(), addCommand);
	}

}
