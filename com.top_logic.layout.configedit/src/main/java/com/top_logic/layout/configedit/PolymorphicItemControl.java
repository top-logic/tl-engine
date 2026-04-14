/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.List;
import java.util.function.BiFunction;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.copy.ConfigCopier;
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
 * the user selects a different type, the control creates a new configuration instance (through
 * {@link PolymorphicOptions}) and rebuilds the nested editor.
 * </p>
 */
public class PolymorphicItemControl extends ReactFormGroupControl {

	private final ConfigurationItem _parentConfig;

	private final PropertyDescriptor _property;

	private final BiFunction<ReactContext, ConfigurationItem, ConfigEditorControl> _editorFactory;

	private final ReactContext _context;

	private final PolymorphicOptions.Choices _choices;

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
		_choices = PolymorphicOptions.compute(parentConfig, property);

		ConfigurationItem currentValue = (ConfigurationItem) parentConfig.value(property);
		Class<?> currentClass = PolymorphicOptions.fromConfig(_choices.mapping(), _choices.options(), currentValue);
		List<String> options = _choices.options().stream().map(Class::getName).toList();
		String currentFqcn = currentClass != null ? currentClass.getName() : null;
		_typeModel = new SimpleSelectFieldModel(currentFqcn, options, false);
		_typeModel.setMandatory(property.isMandatory());
		boolean nullable = property.isNullable();
		_typeModel.setNullable(nullable);

		LabelProvider labelProvider = new ConfigListEditorControl.FqcnClassLabelProvider(_choices.options());

		boolean showSelector = _choices.hasOptions() && (_choices.options().size() > 1 || nullable);
		if (showSelector) {
			ReactSelectFormFieldControl typeSelect =
				new ReactSelectFormFieldControl(context, _typeModel, labelProvider);
			ReactFormFieldChromeControl typeChrome =
				new ReactFormFieldChromeControl(context, "Type", typeSelect);
			addChild(typeChrome);
		}

		if (currentValue != null) {
			_nestedEditor = editorFactory.apply(context, currentValue);
			addChild(_nestedEditor);
		}

		_typeChangeListener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				onTypeChanged(ConfigListEditorControl.resolveClass(_choices.options(), (String) newValue));
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

	private void onTypeChanged(Class<?> selected) {
		ConfigurationItem oldConfig = (ConfigurationItem) _parentConfig.value(_property);

		if (_nestedEditor != null) {
			_nestedEditor.cleanupTree();
			getChildren().remove(_nestedEditor);
			_nestedEditor = null;
		}

		if (selected == null) {
			_parentConfig.update(_property, null);
			putState("children", getChildren());
			return;
		}

		ConfigurationItem newConfig = PolymorphicOptions.toConfig(_choices.mapping(), selected);
		if (oldConfig != null) {
			ConfigCopier.copyContent(new DefaultInstantiationContext(PolymorphicItemControl.class),
				oldConfig, newConfig, true);
		}
		_parentConfig.update(_property, newConfig);

		_nestedEditor = _editorFactory.apply(_context, newConfig);
		addChild(_nestedEditor);
		putState("children", getChildren());
	}

	@Override
	protected void cleanupChildren() {
		_typeModel.removeListener(_typeChangeListener);
		super.cleanupChildren();
	}
}
