/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link FormGroupBuilder} creating the form for {@link Map} entries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class MapFormGroupBuilder extends FormGroupBuilder {

	private Optional<ConfigurationValueProvider<?>> _keyProvider;

	private Optional<ConfigurationValueProvider<?>> _valueProvider;

	private Class<?> _valueType;

	private Class<?> _keyType;

	/**
	 * Creates a {@link FormGroupBuilder} for {@link Map} entries.
	 */
	public MapFormGroupBuilder(EditorFactory editorFactory, ValueModel valueModel, FormGroup content) {
		super(editorFactory, valueModel, content);

		PropertyDescriptor property = valueModel.getProperty();

		initBinding(editorFactory, property);
		initEntryTypes(property);
	}

	@Override
	public void add(Object value) {
		Map.Entry<?, ?> entry = (Entry<?, ?>) value;

		int id = getId();
		FormGroup elementGroup = group(getParent(), EditorUtils.LIST_ELEMENT_PREFIX + id);
		setId(id + 1);

		Object key = entry.getKey();
		elementGroup.setStableIdSpecialCaseMarker(key);

		FormGroup contentGroup = group(elementGroup, EditorUtils.LIST_ITEM_GROUP);

		FormField keyField = createKeyField(key, contentGroup);
		createValueField(entry, contentGroup);

		addRemoveButton(elementGroup, keyField);
	}

	private void createValueField(Map.Entry<?, ?> entry, FormGroup contentGroup) {
		FormField valueField = createValueFieldInternal(contentGroup);
		valueField.addValueListener(createValueListener(entry.getKey()));

		valueField.setMandatory(true);
		valueField.initializeField(entry.getValue());
	}

	private ValueListener createValueListener(Object key) {
		return new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				Map<Object, Object> newMap = new LinkedHashMap<>((Map<?, ?>) _valueModel.getValue());
				newMap.put(key, newValue);
				_valueModel.setValue(newMap);
			}
		};
	}

	private FormField createKeyField(Object key, FormGroup contentGroup) {
		FormField keyField = createKeyFieldInternal(contentGroup);

		keyField.setImmutable(true);
		keyField.initializeField(key);

		return keyField;
	}

	/**
	 * Creates the {@link FormGroup} for the dialog to add a new entry which is moved to the
	 * {@link FormGroup} displaying all entries by success.
	 * 
	 * @see #moveContentGroup(FormGroup)
	 */
	protected FormGroup createDialogContentGroup() {
		int id = getId();
		FormGroup elementGroup = new FormGroup(EditorUtils.LIST_ELEMENT_PREFIX + id, getParent().getResources());
		setId(id + 1);

		FormGroup contentGroup = group(elementGroup, EditorUtils.LIST_ITEM_GROUP);

		createKeyField(contentGroup);
		createValueField(contentGroup);

		return elementGroup;
	}

	private void createValueField(FormGroup contentGroup) {
		FormField valueField = createValueFieldInternal(contentGroup);

		valueField.setMandatory(true);
	}

	private FormField createKeyFieldInternal(FormGroup contentGroup) {
		String name = EditorUtils.MAP_KEY_MEMBER_NAME;
		FormField field = EditorUtils.createPrimitiveField(contentGroup, name, _keyType, _keyProvider);

		setResources(field, name, I18NConstants.MAP_KEY_LABEL);

		return field;
	}

	private void setResources(FormField field, String name, ResKey defaultLabelKey) {
		Resources resources = Resources.getInstance();

		ResKey baseKey = Labels.propertyLabelKey(getValueModel().getProperty()).suffix(name);

		field.setLabel(resources.getStringWithDefaultKey(baseKey, defaultLabelKey));
		field.setTooltip(resources.getString(baseKey.tooltipOptional()));
	}

	private FormField createValueFieldInternal(FormGroup contentGroup) {
		String name = EditorUtils.MAP_VALUE_MEMBER_NAME;
		FormField field = EditorUtils.createPrimitiveField(contentGroup, name, _valueType, _valueProvider);

		setResources(field, name, I18NConstants.MAP_VALUE_LABEL);

		return field;
	}

	private void createKeyField(FormGroup contentGroup) {
		FormField keyField = createKeyFieldInternal(contentGroup);

		keyField.setMandatory(true);
		keyField.addConstraint(new Constraint() {
			@Override
			public boolean check(Object key) throws CheckException {
				Map<?,?> map = (Map<?, ?>) getValueModel().getValue();
				
				if (map.containsKey(key)) {
					ResKey notUniqueKey = I18NConstants.KEY_NOT_UNIQUE__KEY.fill(key);

					throw new CheckException(Resources.getInstance().getString(notUniqueKey));
				}

				return true;
			}
		});
	}

	void moveContentGroup(FormGroup contentGroup) {
		contentGroup.setStableIdSpecialCaseMarker(getParent().size() - 1);
		setId(getId() + 1);

		FormField keyField = (FormField) contentGroup.getFirstMemberRecursively(EditorUtils.MAP_KEY_MEMBER_NAME);
		keyField.setImmutable(true);

		moveContentGroupFromDialogToForm(contentGroup);
		updateValueModel(contentGroup, keyField.getValue());

		addRemoveButton(contentGroup, keyField);
	}

	private void moveContentGroupFromDialogToForm(FormGroup contentGroup) {
		contentGroup.getParent().removeMember(contentGroup);
		getParent().addMember(contentGroup);
	}

	private void updateValueModel(FormGroup contentGroup, Object key) {
		Map<Object, Object> newValue = new LinkedHashMap<>((Map<?, ?>) getValueModel().getValue());
		newValue.put(key, getMapValue(contentGroup));
		getValueModel().setValue(newValue);
	}

	private Object getMapValue(FormGroup contentGroup) {
		return ((FormField) contentGroup.getFirstMemberRecursively(EditorUtils.MAP_VALUE_MEMBER_NAME)).getValue();
	}

	private void initEntryTypes(PropertyDescriptor property) {
		ParameterizedType mapType = (ParameterizedType) property.getGenericType();
		Type[] actualTypeArguments = mapType.getActualTypeArguments();

		_keyType = getTypeClass(actualTypeArguments[0]);
		_valueType = getTypeClass(actualTypeArguments[1]);
	}

	private Class<?> getTypeClass(Type type) {
		Type rawType = type;

		if (type instanceof ParameterizedType) {
			rawType = ((ParameterizedType) type).getRawType();
		}

		if (rawType instanceof Class) {
			return (Class<?>) rawType;
		}

		return null;
	}

	private void initBinding(EditorFactory editorFactory, PropertyDescriptor property) {
		MapBinding binding = editorFactory.getAnnotation(property, MapBinding.class);

		_keyProvider = EditorUtils.createValueProvider(binding.keyFormat());
		_valueProvider = EditorUtils.createValueProvider(binding.valueFormat());
	}

	@Override
	protected void addAddEntryCommand() {
		MapFormGroupBuilder builder = this;

		Command addCommand = new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				ResKey labelKey = Labels.propertyLabelKey(getValueModel().getProperty());

				return new MapEntryBuilderDialog(builder, labelKey).open(context);
			}
		};

		addCommand((FormGroup) getParent().getParent(), isReadOnly(), getValueModel(), addCommand);
	}

}
