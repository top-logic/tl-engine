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
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.view.UIElement;

/**
 * A node in the design tree representing a single {@link com.top_logic.layout.view.UIElement.Config}.
 *
 * <p>
 * Carries the configuration, source file origin, parent reference, and mutable children list for
 * structural editing in the View Designer.
 * </p>
 */
public class DesignTreeNode {

	private static final String[] LABEL_PROPERTIES = { "name", "title-key", "attribute", "view" };

	private final PolymorphicConfiguration<? extends UIElement> _config;

	private final String _sourceFile;

	private final List<DesignTreeNode> _children;

	private DesignTreeNode _parent;

	/**
	 * Creates a new {@link DesignTreeNode}.
	 *
	 * @param config
	 *        The UIElement configuration this node represents.
	 * @param sourceFile
	 *        The .view.xml file this config originates from.
	 */
	public DesignTreeNode(PolymorphicConfiguration<? extends UIElement> config, String sourceFile) {
		_config = config;
		_sourceFile = sourceFile;
		_children = new ArrayList<>();
	}

	/**
	 * The UIElement configuration.
	 */
	public PolymorphicConfiguration<? extends UIElement> getConfig() {
		return _config;
	}

	/**
	 * The configuration as {@link ConfigurationItem} for the config editor.
	 */
	public ConfigurationItem getConfigItem() {
		return (ConfigurationItem) _config;
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
	 * interface, or the simple interface name as fallback.
	 */
	public String getTagName() {
		Class<?> configInterface = getConfigItem().descriptor().getConfigurationInterface();
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
	 * "title-key", "attribute", "view".
	 *
	 * @return The first non-null property value found, or {@code null} if none match.
	 */
	public String getLabel() {
		ConfigurationDescriptor descriptor = getConfigItem().descriptor();
		for (String propName : LABEL_PROPERTIES) {
			PropertyDescriptor property = descriptor.getProperty(propName);
			if (property != null) {
				Object value = getConfigItem().value(property);
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
