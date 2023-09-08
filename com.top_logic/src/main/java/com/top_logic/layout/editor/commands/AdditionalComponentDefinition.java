/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Definition of an additional {@link LayoutComponent} to create.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AdditionalComponentDefinition {

	private final String _layoutKey;

	private final DynamicComponentDefinition _definition;

	private final String _configuredLayoutKey;

	private final ConfigurationItem _arguments;

	/**
	 * Creates a new {@link AdditionalComponentDefinition}.
	 * 
	 * @param layoutKey
	 *        See {@link #getLayoutKey()}.
	 * @param configuredLayoutKey
	 *        See {@link #getConfiguredNameScope()}.
	 * @param definition
	 *        See {@link #getDefinition()}.
	 * @param arguments
	 *        See {@link #getArguments()}.
	 */
	public AdditionalComponentDefinition(String layoutKey, String configuredLayoutKey,
			DynamicComponentDefinition definition, ConfigurationItem arguments) {
		_layoutKey = StringServices.requireNotEmpty(layoutKey, "layoutKey must not be empty.");
		_configuredLayoutKey = StringServices.nonEmpty(configuredLayoutKey);
		_definition = definition;
		_arguments = arguments;
	}

	/**
	 * The definition of the template of the component to create.
	 */
	public DynamicComponentDefinition getDefinition() {
		return _definition;
	}

	/**
	 * Arguments for the template in {@link #getDefinition()}.
	 */
	public ConfigurationItem getArguments() {
		return _arguments;
	}

	/**
	 * The layout key to use for the new {@link LayoutComponent}.
	 */
	public String getLayoutKey() {
		return _layoutKey;
	}

	/**
	 * The layout key configured in the template. The usage of this key must be replaced by
	 * {@link #getLayoutKey()}.
	 * 
	 * @return May be <code>null</code> when the represented component is not referenced anywhere.
	 */
	public String getConfiguredNameScope() {
		return _configuredLayoutKey;
	}

}

