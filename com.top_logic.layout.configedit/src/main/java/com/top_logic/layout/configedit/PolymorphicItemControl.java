/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;

/**
 * A control that renders a type selector dropdown and a nested {@link ConfigEditorControl} for
 * polymorphic {@link PolymorphicConfiguration} ITEM properties.
 *
 * <p>
 * Extends {@link ReactFormGroupControl} to render as a collapsible form group. The type selector is
 * the first child, and the nested config editor (if a type is selected) is the second child. When
 * the user selects a different configuration type, the control creates a new configuration instance
 * and rebuilds the nested editor.
 * </p>
 */
public class PolymorphicItemControl extends ReactFormGroupControl {

	private final ConfigurationItem _parentConfig;

	private final PropertyDescriptor _property;

	private final BiFunction<ReactContext, ConfigurationItem, ConfigEditorControl> _editorFactory;

	private final ReactContext _context;

	private final SimpleSelectFieldModel _typeModel;

	private ConfigEditorControl _nestedEditor;

	private final FieldModelListener _typeChangeListener;

	/**
	 * Creates a {@link PolymorphicItemControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param label
	 *        The group header label.
	 * @param parentConfig
	 *        The parent configuration item owning the property.
	 * @param property
	 *        The polymorphic ITEM property descriptor.
	 * @param editorFactory
	 *        Factory for creating nested editors (allows test subclass propagation).
	 */
	public PolymorphicItemControl(ReactContext context, String label, ConfigurationItem parentConfig,
			PropertyDescriptor property,
			BiFunction<ReactContext, ConfigurationItem, ConfigEditorControl> editorFactory) {
		super(context, label, true, false, "subtle", true, List.of(), List.of());
		_context = context;
		_parentConfig = parentConfig;
		_property = property;
		_editorFactory = editorFactory;

		List<String> typeOptions = resolveTypeOptions(property);

		String currentTypeName = resolveCurrentTypeName(parentConfig, property);
		_typeModel = new SimpleSelectFieldModel(currentTypeName, typeOptions, false);

		LabelProvider labelProvider = fqcn -> {
			if (fqcn instanceof String) {
				String name = (String) fqcn;
				int dot = name.lastIndexOf('.');
				return dot >= 0 ? name.substring(dot + 1) : name;
			}
			return fqcn != null ? fqcn.toString() : "";
		};

		ReactSelectFormFieldControl typeSelect =
			new ReactSelectFormFieldControl(context, _typeModel, labelProvider);
		ReactFormFieldChromeControl typeChrome =
			new ReactFormFieldChromeControl(context, "Type", typeSelect);
		addChild(typeChrome);

		ConfigurationItem currentValue = (ConfigurationItem) parentConfig.value(property);
		if (currentValue != null) {
			_nestedEditor = editorFactory.apply(context, currentValue);
			addChild(_nestedEditor);
		}

		_typeChangeListener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				onTypeChanged((String) newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		};
		_typeModel.addListener(_typeChangeListener);
	}

	/**
	 * The type selector model.
	 */
	public SimpleSelectFieldModel getTypeModel() {
		return _typeModel;
	}

	/**
	 * The nested editor, or {@code null} if no type is selected.
	 */
	public ConfigEditorControl getNestedEditor() {
		return _nestedEditor;
	}

	private void onTypeChanged(String newTypeFqcn) {
		// Clean up old nested editor.
		if (_nestedEditor != null) {
			_nestedEditor.cleanupTree();
			getChildren().remove(_nestedEditor);
			_nestedEditor = null;
		}

		if (newTypeFqcn == null || newTypeFqcn.isEmpty()) {
			_parentConfig.update(_property, null);
			putState("children", getChildren());
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			Class<? extends ConfigurationItem> configClass =
				(Class<? extends ConfigurationItem>) Class.forName(newTypeFqcn);
			ConfigurationItem newConfig = TypedConfiguration.newConfigItem(configClass);
			_parentConfig.update(_property, newConfig);

			_nestedEditor = _editorFactory.apply(_context, newConfig);
			addChild(_nestedEditor);
			putState("children", getChildren());
		} catch (ClassNotFoundException ex) {
			Logger.error("Unknown configuration class: " + newTypeFqcn, ex, PolymorphicItemControl.class);
		}
	}

	/**
	 * Resolves the available configuration type options for the given property.
	 */
	public static List<String> resolveTypeOptions(PropertyDescriptor property) {
		return resolveTypeOptions(property.getType());
	}

	/**
	 * Resolves the available configuration type options for the given base type.
	 */
	public static List<String> resolveTypeOptions(Class<?> baseType) {
		Collection<Class<?>> specializations =
			TypeIndex.getInstance().getSpecializations(baseType, true, true, false);
		List<String> result = new ArrayList<>();
		for (Class<?> cls : specializations) {
			result.add(cls.getName());
		}
		result.sort(Comparator.comparing(fqcn -> {
			int dot = fqcn.lastIndexOf('.');
			return dot >= 0 ? fqcn.substring(dot + 1) : fqcn;
		}));
		return result;
	}

	private static String resolveCurrentTypeName(ConfigurationItem parentConfig, PropertyDescriptor property) {
		Object currentValue = parentConfig.value(property);
		if (currentValue instanceof ConfigurationItem) {
			return ((ConfigurationItem) currentValue).descriptor().getConfigurationInterface().getName();
		}
		return null;
	}

	@Override
	protected void cleanupChildren() {
		_typeModel.removeListener(_typeChangeListener);
		super.cleanupChildren();
	}
}
