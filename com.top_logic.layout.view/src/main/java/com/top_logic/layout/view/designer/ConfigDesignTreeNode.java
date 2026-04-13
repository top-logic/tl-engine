/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;

/**
 * A {@link DesignTreeNode} bound to a {@link ConfigurationItem}.
 */
public class ConfigDesignTreeNode extends DesignTreeNode {

	private static final String[] LABEL_PROPERTIES = { "name", "title-key", "attribute", "view", "id" };

	private final ConfigurationItem _config;

	/**
	 * Creates a {@link ConfigDesignTreeNode}.
	 *
	 * @param config
	 *        The configuration this node represents.
	 * @param sourceFile
	 *        The .view.xml file this config originates from.
	 */
	public ConfigDesignTreeNode(ConfigurationItem config, String sourceFile) {
		super(sourceFile);
		_config = config;

		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			config.addConfigurationListener(property, change -> markDirty());
		}
	}

	/**
	 * The configuration this node represents.
	 */
	public ConfigurationItem getConfig() {
		return _config;
	}

	@Override
	public String getTagName() {
		Class<?> configInterface = _config.descriptor().getConfigurationInterface();
		TagName tagName = configInterface.getAnnotation(TagName.class);
		if (tagName != null) {
			return tagName.value();
		}
		String name = configInterface.getSimpleName();
		if (name.endsWith("Config")) {
			name = name.substring(0, name.length() - "Config".length());
		}
		return name;
	}

	@Override
	public String getLabel() {
		ConfigurationDescriptor descriptor = _config.descriptor();
		for (String propName : LABEL_PROPERTIES) {
			PropertyDescriptor property = descriptor.getProperty(propName);
			if (property != null) {
				Object value = _config.value(property);
				if (value != null) {
					return String.valueOf(value);
				}
			}
		}
		return null;
	}
}
