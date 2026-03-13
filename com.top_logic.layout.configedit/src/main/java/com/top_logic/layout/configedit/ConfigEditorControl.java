/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;

/**
 * A {@link ReactControl} that renders a form for all PLAIN, REF, and ITEM properties of a
 * {@link ConfigurationItem}.
 *
 * <p>
 * Each PLAIN/REF property is wrapped in a {@link ReactFormFieldChromeControl} with label, mandatory
 * indicator, and help text. ITEM properties are rendered as collapsible
 * {@link ReactFormGroupControl} sections containing a nested {@link ConfigEditorControl}. LIST,
 * MAP, ARRAY, DERIVED, and COMPLEX properties are skipped.
 * </p>
 */
public class ConfigEditorControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormLayout";

	private static final String CHILDREN = "children";

	private final List<ConfigFieldModel> _fieldModels = new ArrayList<>();

	private final List<ReactControl> _childControls = new ArrayList<>();

	/**
	 * Creates a {@link ConfigEditorControl} for all visible properties.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config) {
		this(context, config, Collections.emptySet());
	}

	/**
	 * Creates a {@link ConfigEditorControl}, hiding the given properties.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param hiddenProperties
	 *        Properties to exclude from the form.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties) {
		super(context, config, REACT_MODULE);

		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (hiddenProperties.contains(property)) {
				continue;
			}
			if (!isSupportedKind(property.kind())) {
				continue;
			}

			if (property.kind() == PropertyKind.ITEM) {
				ConfigurationItem nested = (ConfigurationItem) config.value(property);
				if (nested != null) {
					ConfigEditorControl nestedEditor = createNestedEditor(context, nested);
					String label = resolveLabel(property);
					ReactFormGroupControl group = new ReactFormGroupControl(
						context, label, true, false, "subtle", true,
						List.of(), List.of(nestedEditor));
					_childControls.add(group);
				}
				continue;
			}

			ConfigFieldModel model = new ConfigFieldModel(config, property);
			_fieldModels.add(model);

			ReactControl input = ConfigFieldDispatch.createPlainControl(context, model);

			String label = resolveLabel(property);
			String helpText = resolveHelpText(property);
			String labelPosition = (property.getType() == boolean.class || property.getType() == Boolean.class)
				? "after" : null;

			ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(
				context, label, model.isMandatory(), false, null, helpText, labelPosition,
				false, true, input);
			_childControls.add(chrome);
		}

		putState(CHILDREN, _childControls);
	}

	/**
	 * Resolves the label for the given property.
	 *
	 * @param property
	 *        The property descriptor.
	 * @return The label text.
	 */
	protected String resolveLabel(PropertyDescriptor property) {
		return Labels.propertyLabel(property, false);
	}

	/**
	 * Resolves the help text (tooltip) for the given property.
	 *
	 * @param property
	 *        The property descriptor.
	 * @return The help text, or {@code null} if none.
	 */
	protected String resolveHelpText(PropertyDescriptor property) {
		return Labels.propertyLabel(property, "@tooltip", true);
	}

	/**
	 * Creates a nested {@link ConfigEditorControl} for an ITEM property value.
	 *
	 * <p>
	 * Subclasses may override this to customize the nested editor (e.g. for testing).
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param nested
	 *        The nested configuration item to edit.
	 * @return A new editor control for the nested item.
	 */
	protected ConfigEditorControl createNestedEditor(ReactContext context, ConfigurationItem nested) {
		return new ConfigEditorControl(context, nested);
	}

	private static boolean isSupportedKind(PropertyKind kind) {
		return kind == PropertyKind.PLAIN || kind == PropertyKind.REF || kind == PropertyKind.ITEM;
	}

	/**
	 * The field models created for the properties.
	 */
	public List<ConfigFieldModel> getFieldModels() {
		return Collections.unmodifiableList(_fieldModels);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl chrome : _childControls) {
			chrome.cleanupTree();
		}
	}

	@Override
	protected void onCleanup() {
		for (ConfigFieldModel model : _fieldModels) {
			model.detach();
		}
	}
}
