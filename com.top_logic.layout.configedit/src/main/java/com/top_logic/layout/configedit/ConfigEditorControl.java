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

/**
 * A {@link ReactControl} that renders a form for all PLAIN/REF properties of a
 * {@link ConfigurationItem}.
 *
 * <p>
 * Each supported property is wrapped in a {@link ReactFormFieldChromeControl} with label, mandatory
 * indicator, and help text. ITEM, LIST, MAP, ARRAY, DERIVED, and COMPLEX properties are skipped in
 * this phase.
 * </p>
 */
public class ConfigEditorControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormLayout";

	private static final String CHILDREN = "children";

	private final List<ConfigFieldModel> _fieldModels = new ArrayList<>();

	private final List<ReactControl> _chromeControls = new ArrayList<>();

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
			_chromeControls.add(chrome);
		}

		putState(CHILDREN, _chromeControls);
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

	private static boolean isSupportedKind(PropertyKind kind) {
		return kind == PropertyKind.PLAIN || kind == PropertyKind.REF;
	}

	/**
	 * The field models created for the properties.
	 */
	public List<ConfigFieldModel> getFieldModels() {
		return Collections.unmodifiableList(_fieldModels);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl chrome : _chromeControls) {
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
