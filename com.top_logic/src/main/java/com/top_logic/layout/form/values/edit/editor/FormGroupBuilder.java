/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.Values;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Icons;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * General builder creating {@link FormMember}s for a given input.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class FormGroupBuilder {

	private Value<Boolean> _readOnly;

	private FormGroup _parent;

	private int _id = 1;

	ValueModel _valueModel;

	/**
	 * Creates a {@link FormGroupBuilder}.
	 */
	public FormGroupBuilder(EditorFactory editorFactory, ValueModel valueModel, FormGroup content) {
		_readOnly = isReadOnly(editorFactory, valueModel, valueModel.getProperty());
		_parent = content;
		_valueModel = valueModel;
	}

	private Value<Boolean> isReadOnly(EditorFactory factory, final ValueModel valueModel, PropertyDescriptor property) {
		return Values.map(factory.fieldMode(property, valueModel.getModel()), IsLocallyImmutable.INSTANCE);
	}

	/**
	 * Adds a button to remove an entry.
	 */
	protected void addRemoveButton(FormGroup elementGroup, FormField field) {
		EditorUtils.addRemoveButton(elementGroup, newRemoveCommand(elementGroup, field), _readOnly);
	}

	private Command newRemoveCommand(final FormGroup toRemove, FormField keyField) {
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				updateValueModel();

				FormContainer parent = toRemove.getParent();
				parent.removeMember(toRemove);

				updateFormMemberIDs(parent);

				return HandlerResult.DEFAULT_RESULT;
			}

			private void updateValueModel() {
				Object object = _valueModel.getValue();

				if (object instanceof Map) {
					HashMap<?, ?> newValue = new HashMap<>((Map<?, ?>) object);
					newValue.remove(keyField.getValue());
					_valueModel.setValue(newValue);
				} else if (object instanceof List) {
					List<?> newValue = new ArrayList<>((List<?>) object);
					newValue.remove((int) toRemove.getStableIdSpecialCaseMarker());
					_valueModel.setValue(newValue);
				}
			}

			private void updateFormMemberIDs(FormContainer parent) {
				int elementId = 0;

				for (Iterator<? extends FormMember> it = parent.getMembers(); it.hasNext();) {
					it.next().setStableIdSpecialCaseMarker(elementId++);
				}
			}
		};
	}

	/**
	 * Adds the given {@link Command} to the form context.
	 */
	protected void addCommand(FormGroup list, Value<Boolean> readOnly, ValueModel model, Command command) {
		CommandField addButton = button(list, EditorUtils.LIST_ADD, Icons.ADD_ICON, command);

		addButton.setControlProvider(Buttons.ADD_BUTTON);
		addButton.setLabel(createAddEntryLabel(model));

		bindVisible(addButton, not(or(readOnly, Fields.isImmutable(list))));
	}

	private ResKey createAddEntryLabel(ValueModel valueModel) {
		String propertyLabel = Labels.propertyLabel(valueModel);

		return I18NConstants.ADD_ELEMENT__PROPERTY.fill(propertyLabel);
	}

	/**
	 * True if the property is read only, otherwise false.
	 */
	public Value<Boolean> isReadOnly() {
		return _readOnly;
	}

	/**
	 * @see #isReadOnly()
	 */
	public void setReadOnly(Value<Boolean> readOnly) {
		_readOnly = readOnly;
	}

	/**
	 * Container {@link FormGroup}.
	 */
	public FormGroup getParent() {
		return _parent;
	}

	/**
	 * @see #getParent()
	 */
	public void setParent(FormGroup parent) {
		_parent = parent;
	}

	/**
	 * Counter to identify each {@link FormGroup} containing an entry.
	 */
	public int getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public void setId(int id) {
		_id = id;
	}

	/**
	 * Abstraction of the model being edited.
	 */
	public ValueModel getValueModel() {
		return _valueModel;
	}

	/**
	 * @see #getValueModel()
	 */
	public void setValueModel(ValueModel valueModel) {
		_valueModel = valueModel;
	}

	/**
	 * Creates the form context for the given value and adds it to the parent {@link FormGroup}.
	 */
	public abstract void add(Object value);

	/**
	 * Adds a command to add a new entry.
	 */
	protected abstract void addAddEntryCommand();
}
