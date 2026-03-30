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
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;

/**
 * A node in the design tree representing either a {@link ConfigurationItem} or a virtual property
 * group.
 *
 * <p>
 * Config nodes carry the actual configuration and source file origin. Virtual nodes represent a
 * container property (e.g. "header", "content", "footer") and group the child elements that belong
 * to that property.
 * </p>
 */
public class DesignTreeNode {

	private static final String[] LABEL_PROPERTIES = { "name", "title-key", "attribute", "view", "id" };

	private final ConfigurationItem _config;

	private final String _sourceFile;

	private final String _propertyName;

	private final List<DesignTreeNode> _children;

	private DesignTreeNode _parent;

	/**
	 * Creates a config node.
	 *
	 * @param config
	 *        The configuration this node represents.
	 * @param sourceFile
	 *        The .view.xml file this config originates from.
	 */
	public DesignTreeNode(ConfigurationItem config, String sourceFile) {
		_config = config;
		_sourceFile = sourceFile;
		_propertyName = null;
		_children = new ArrayList<>();
	}

	/**
	 * Creates a virtual property group node.
	 *
	 * @param propertyName
	 *        The property name this group represents (e.g. "header", "content").
	 * @param sourceFile
	 *        The .view.xml file this group belongs to.
	 */
	public DesignTreeNode(String propertyName, String sourceFile) {
		_config = null;
		_sourceFile = sourceFile;
		_propertyName = propertyName;
		_children = new ArrayList<>();
	}

	/**
	 * Whether this is a virtual property group node (no config of its own).
	 */
	public boolean isVirtual() {
		return _config == null;
	}

	/**
	 * The property name for virtual group nodes, or {@code null} for config nodes.
	 */
	public String getPropertyName() {
		return _propertyName;
	}

	/**
	 * The configuration, or {@code null} for virtual nodes.
	 */
	public ConfigurationItem getConfig() {
		return _config;
	}

	/**
	 * Alias for {@link #getConfig()} for the config editor.
	 */
	public ConfigurationItem getConfigItem() {
		return _config;
	}

	/**
	 * The source .view.xml file path.
	 */
	public String getSourceFile() {
		return _sourceFile;
	}

	/**
	 * The mutable child nodes list.
	 */
	public List<DesignTreeNode> getChildren() {
		return _children;
	}

	/**
	 * The parent node, or {@code null} for the root.
	 */
	public DesignTreeNode getParent() {
		return _parent;
	}

	/**
	 * Sets the parent node.
	 *
	 * @see DesignTreeBuilder
	 */
	void setParent(DesignTreeNode parent) {
		_parent = parent;
	}

	/**
	 * The tag name for display, derived from the {@link TagName} annotation on the config
	 * interface, or the simple interface name as fallback. For virtual nodes, returns the property
	 * name in brackets.
	 */
	public String getTagName() {
		if (isVirtual()) {
			return "[" + _propertyName + "]";
		}
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

	/**
	 * An identifying label for display. Checks common identifying properties in order: "name",
	 * "title-key", "attribute", "view", "id".
	 *
	 * @return The first non-null property value found, or {@code null} if none match. Always
	 *         {@code null} for virtual nodes.
	 */
	public String getLabel() {
		if (isVirtual()) {
			return null;
		}
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

	/**
	 * Display string: tagName + optional label in quotes.
	 */
	@Override
	public String toString() {
		String label = getLabel();
		if (label != null) {
			return getTagName() + " \"" + label + "\"";
		}
		return getTagName();
	}
}
