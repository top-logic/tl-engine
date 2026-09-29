/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigTagName;
import com.top_logic.util.Resources;

/**
 * A {@link DesignTreeNode} bound to a {@link ConfigurationItem}.
 */
public class ConfigDesignTreeNode extends DesignTreeNode {

	private static final String[] LABEL_PROPERTIES = { "name", "title-key", "attribute", "view", "id" };

	private final ConfigurationItem _config;

	private final List<ListenerRegistration> _listeners = new ArrayList<>();

	private record ListenerRegistration(PropertyDescriptor property, ConfigurationListener listener) {
	}

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
			ConfigurationListener listener = change -> markDirty();
			config.addConfigurationListener(property, listener);
			_listeners.add(new ListenerRegistration(property, listener));
		}
	}

	@Override
	protected void onCleanup() {
		for (ListenerRegistration reg : _listeners) {
			_config.removeConfigurationListener(reg.property(), reg.listener());
		}
		_listeners.clear();
	}

	/**
	 * The configuration this node represents.
	 */
	public ConfigurationItem getConfig() {
		return _config;
	}

	@Override
	public String getTagName() {
		return ConfigTagName.of(_config);
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

	@Override
	public String getTooltipHtml() {
		// For a UI element node, prefer the implementation class' JavaDoc (the element help),
		// falling back to the configuration interface if the implementation class is not set.
		Class<?> keyClass = null;
		if (_config instanceof PolymorphicConfiguration<?> pc) {
			keyClass = pc.getImplementationClass();
		}
		if (keyClass == null) {
			keyClass = _config.descriptor().getConfigurationInterface();
		}
		return Resources.getInstance().getString(ResKey.forClass(keyClass).tooltip(), null);
	}
}
