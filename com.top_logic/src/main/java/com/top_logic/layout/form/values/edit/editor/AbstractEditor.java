/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.ModifiableValue;
import com.top_logic.layout.form.values.edit.ActivateImmutable;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * Base-class for simplified {@link Editor} implementations for atomic values.
 * 
 * @see #addField(EditorFactory, FormContainer, ValueModel, String)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractEditor implements Editor {

	private static final ValueListener IMMUTABLE_ON_CHANGE = new ValueListener() {
		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			if (!isEmpty(newValue)) {
				field.setImmutable(true);
			}
		}

		private boolean isEmpty(Object newValue) {
			return newValue == null || ((newValue instanceof Collection<?>) && ((Collection<?>) newValue).isEmpty());
		}
	};

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model) {
		return addField(editorFactory, container, model, model.getProperty().getPropertyName());
	}

	/**
	 * Create the field for the primitive value.
	 * 
	 * <p>
	 * Implementations must do the following:
	 * </p>
	 * 
	 * <ul>
	 * <li>Create the field with the given field name.</li>
	 * <li>Add the field to the given container.</li>
	 * <li>Call {@link #init(EditorFactory, ValueModel, FormField, Mapping, Mapping)} with the
	 * created field to bind it to its model.</li>
	 * <li>Return the newly created field.</li>
	 * </ul>
	 * 
	 * @param editorFactory
	 *        See {@link #createUI(EditorFactory, FormContainer, ValueModel)}.
	 * @param container
	 *        See {@link #createUI(EditorFactory, FormContainer, ValueModel)}.
	 * @param model
	 *        See {@link #createUI(EditorFactory, FormContainer, ValueModel)}.
	 * @param fieldName
	 *        The name of the field to create.
	 * @return The newly created field.
	 */
	protected abstract FormField addField(EditorFactory editorFactory, FormContainer container,
			ValueModel model, String fieldName);

	/**
	 * Binds the given field to its model.
	 *
	 * @param editorFactory
	 *        See {@link #createUI(EditorFactory, FormContainer, ValueModel)}.
	 * @param model
	 *        See {@link #createUI(EditorFactory, FormContainer, ValueModel)}.
	 * @param field
	 *        The newly created field.
	 * @param uiConversion
	 *        The conversion to apply when displaying a new model value in the given field.
	 * @param storageConversion
	 *        The conversion to apply to the value entered in the field before storing it to its
	 *        model.
	 */
	protected void init(EditorFactory editorFactory, ValueModel model, final FormField field,
			Mapping<Object, Object> uiConversion, Mapping<Object, Object> storageConversion) {
		if (isMandatory(editorFactory, model.getProperty())) {
			field.setMandatory(true);
			if (editorFactory.getDisplayFieldsAsText() && isDisplayedImmutableWhenMandatory(field)) {
				field.addValueListener(IMMUTABLE_ON_CHANGE);
				field.setControlProvider(ActivateImmutable.INSTANCE);
			}
		} else {
			DerivedProperty<Boolean> mandatoryProperty =
				Fields.mandatoryProperty(editorFactory.formOptions(model.getProperty()));
			if (mandatoryProperty != null) {
				bindMandatory(field, mandatoryProperty.getValue(model.getModel()));
			}
		}

		ModifiableValue<Object> input = fieldValue(field);
		ModifiableValue<Object> modelValue = configurationValue(model.getModel(), model.getProperty());
		bindValue(field, modelValue, uiConversion);
		if (model.getProperty().canHaveSetter()) {
			linkStorage(input, modelValue, storageConversion);
		}

		editorFactory.processControlProviderAnnotation(model.getProperty(), field);
	}

	private boolean isMandatory(AnnotationCustomizations customizations, PropertyDescriptor property) {
		return property.isMandatory() || isTechnicallyMandatory(property)
			|| customizations.getAnnotation(property, Mandatory.class) != null;
	}

	private boolean isTechnicallyMandatory(final PropertyDescriptor property) {
		switch (property.kind()) {
			case ARRAY:
			case LIST:
			case MAP:
				// Lists and maps are not nullable but may be empty.
				return false;

			default:
				// A property that is non-nullable must have a value entered. Special cases are
				// String and boolean properties, because the empty string is entered as
				// empty input and booleans cannot be entered empty.
				Class<?> type = property.getType();

				return !property.isNullable() && type != String.class && type != boolean.class && type != List.class
					&& type != Map.class && type != Set.class;
		}
	}

	private static boolean isDisplayedImmutableWhenMandatory(FormMember field) {
		return (field instanceof SelectField) || (field instanceof StringField);
	}

}
