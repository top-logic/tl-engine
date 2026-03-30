/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;

/**
 * Builds a {@link DesignTreeNode} tree from a root view's
 * {@link com.top_logic.layout.view.UIElement.Config} hierarchy.
 *
 * <p>
 * Generically traverses all properties of each config: LIST properties containing
 * {@link com.top_logic.layout.view.UIElement.Config}s and ITEM properties containing a single
 * {@link com.top_logic.layout.view.UIElement.Config} are discovered via
 * {@link PropertyDescriptor#getInstanceType()}. If a config has exactly one such container property,
 * its children are inlined directly. Multiple container properties each get their own virtual group
 * node (e.g. "[header]", "[content]", "[footer]").
 * </p>
 *
 * <p>
 * Eagerly resolves {@link com.top_logic.layout.view.ReferenceElement.Config} by loading the
 * referenced view's config via {@link ViewLoader#getOrLoadConfig(String)}.
 * </p>
 */
public class DesignTreeBuilder {

	/**
	 * Builds the design tree starting from the given root view path.
	 *
	 * @param rootViewPath
	 *        Full path to the root view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @return The root {@link DesignTreeNode}.
	 */
	public DesignTreeNode build(String rootViewPath) throws ConfigurationException {
		ViewElement.Config rootConfig = ViewLoader.getOrLoadConfig(rootViewPath);
		return buildNode(rootConfig, rootViewPath);
	}

	@SuppressWarnings("unchecked")
	private DesignTreeNode buildNode(PolymorphicConfiguration<? extends UIElement> config,
			String sourceFile) throws ConfigurationException {
		DesignTreeNode node = new DesignTreeNode(config, sourceFile);
		ConfigurationItem configItem = (ConfigurationItem) config;

		// Count container properties to decide between inline and grouped display.
		int containerPropertyCount = countContainerProperties(configItem);

		// Traverse all properties generically.
		for (PropertyDescriptor property : configItem.descriptor().getProperties()) {
			if (property.kind() == PropertyKind.LIST && isUIElementProperty(property)) {
				List<PolymorphicConfiguration<? extends UIElement>> children =
					(List<PolymorphicConfiguration<? extends UIElement>>) configItem.value(property);
				if (children != null && !children.isEmpty()) {
					if (containerPropertyCount == 1) {
						for (var childConfig : children) {
							addChild(node, childConfig, sourceFile);
						}
					} else {
						DesignTreeNode group = new DesignTreeNode(property.getPropertyName(), sourceFile);
						group.setParent(node);
						node.getChildren().add(group);
						for (var childConfig : children) {
							addChild(group, childConfig, sourceFile);
						}
					}
				}
			} else if (property.kind() == PropertyKind.ITEM && isUIElementProperty(property)) {
				PolymorphicConfiguration<? extends UIElement> childConfig =
					(PolymorphicConfiguration<? extends UIElement>) configItem.value(property);
				if (childConfig != null) {
					if (containerPropertyCount == 1) {
						addChild(node, childConfig, sourceFile);
					} else {
						DesignTreeNode group = new DesignTreeNode(property.getPropertyName(), sourceFile);
						group.setParent(node);
						node.getChildren().add(group);
						addChild(group, childConfig, sourceFile);
					}
				}
			}
		}

		// For ReferenceElement, also load referenced view as a child.
		if (config instanceof ReferenceElement.Config refConfig) {
			String refPath = ViewLoader.VIEW_BASE_PATH + refConfig.getView();
			ViewElement.Config refViewConfig = ViewLoader.getOrLoadConfig(refPath);
			addChild(node, refViewConfig, refPath);
		}

		return node;
	}

	private int countContainerProperties(ConfigurationItem configItem) {
		int count = 0;
		for (PropertyDescriptor property : configItem.descriptor().getProperties()) {
			if (isUIElementProperty(property)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Whether the given property contains UIElement configurations (either as a LIST of
	 * {@link PolymorphicConfiguration}s or a single ITEM).
	 */
	private boolean isUIElementProperty(PropertyDescriptor property) {
		PropertyKind kind = property.kind();
		if (kind != PropertyKind.LIST && kind != PropertyKind.ITEM) {
			return false;
		}
		Class<?> instanceType = property.getInstanceType();
		return instanceType != null && UIElement.class.isAssignableFrom(instanceType);
	}

	private void addChild(DesignTreeNode parent,
			PolymorphicConfiguration<? extends UIElement> childConfig,
			String sourceFile) throws ConfigurationException {
		DesignTreeNode child = buildNode(childConfig, sourceFile);
		child.setParent(parent);
		parent.getChildren().add(child);
	}
}
