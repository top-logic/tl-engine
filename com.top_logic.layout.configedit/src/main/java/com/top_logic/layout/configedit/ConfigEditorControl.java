/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;

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
public class ConfigEditorControl extends ReactFormLayoutControl {

	/**
	 * Creates a {@link ConfigEditorControl} for all visible properties.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config) {
		this(context, config, Collections.emptySet(), false);
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
		this(context, config, hiddenProperties, false);
	}

	/**
	 * Creates a {@link ConfigEditorControl}, hiding the given properties and optionally skipping
	 * tree properties.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param hiddenProperties
	 *        Properties to exclude from the form.
	 * @param skipTreeProperties
	 *        If {@code true}, properties annotated with {@link TreeProperty} are skipped. Use
	 *        {@code true} for top-level tree node configurations, {@code false} for nested/inline
	 *        sub-configurations.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties) {
		super(context);

		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (hiddenProperties.contains(property)) {
				continue;
			}
			if (!isSupportedKind(property.kind())) {
				continue;
			}
			if (isHidden(property)) {
				continue;
			}
			if (skipTreeProperties && isTreeProperty(property)) {
				continue;
			}

			if (property.kind() == PropertyKind.ITEM) {
				if (PolymorphicConfiguration.class.isAssignableFrom(property.getType())) {
					String label = resolveLabel(property);
					PolymorphicItemControl polyGroup =
						createPolymorphicGroup(context, label, config, property);
					addChild(polyGroup);
				} else {
					ConfigurationAccess configAccess = property.getConfigurationAccess();
					ConfigurationItem nested = configAccess.getConfig(config.value(property));
					if (nested != null) {
						ConfigEditorControl nestedEditor = createNestedEditor(context, nested);
						String label = resolveLabel(property);
						ReactFormGroupControl group = new ReactFormGroupControl(
							context, label, true, false, "subtle", true,
							List.of(), List.of(nestedEditor));
						addChild(group);
					}
				}
				continue;
			}

			ConfigFieldModel model = new ConfigFieldModel(config, property);
			addCleanupAction(model::detach);

			ReactControl input = ConfigFieldDispatch.createPlainControl(context, model);

			String label = resolveLabel(property);
			String helpText = resolveHelpText(property);
			String labelPosition = (property.getType() == boolean.class || property.getType() == Boolean.class)
				? "after" : null;

			ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(
				context, label, model.isMandatory(), false, null, helpText, labelPosition,
				false, true, input);
			addChild(chrome);
		}
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

	/**
	 * Creates a {@link PolymorphicItemControl} for a polymorphic ITEM property.
	 *
	 * <p>
	 * Subclasses may override this to customize the polymorphic editor (e.g. for testing).
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param label
	 *        The group label.
	 * @param parentConfig
	 *        The parent configuration item.
	 * @param property
	 *        The polymorphic ITEM property.
	 * @return A new polymorphic item control.
	 */
	protected PolymorphicItemControl createPolymorphicGroup(ReactContext context, String label,
			ConfigurationItem parentConfig, PropertyDescriptor property) {
		return new PolymorphicItemControl(context, label, parentConfig, property, this::createNestedEditor);
	}

	private static boolean isSupportedKind(PropertyKind kind) {
		return kind == PropertyKind.PLAIN || kind == PropertyKind.REF || kind == PropertyKind.ITEM;
	}

	private static boolean isHidden(PropertyDescriptor property) {
		Hidden annotation = property.getAnnotation(Hidden.class);
		return annotation != null && annotation.value();
	}

	private static boolean isTreeProperty(PropertyDescriptor property) {
		TreeProperty annotation = property.getAnnotation(TreeProperty.class);
		return annotation != null && annotation.value();
	}

}
