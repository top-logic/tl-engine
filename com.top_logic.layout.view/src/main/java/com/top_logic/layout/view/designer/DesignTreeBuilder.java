/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
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
 * Generically traverses all {@link PolymorphicConfiguration}-typed LIST and ITEM properties of each
 * config. This includes not only {@link com.top_logic.layout.view.UIElement.Config} but also
 * intermediate config types (e.g. {@code NavItemConfig}, {@code BottomBarItemConfig}) that contain
 * UIElement children in their own properties.
 * </p>
 *
 * <p>
 * If a config has exactly one container property, its children are inlined directly. Multiple
 * container properties each get their own virtual group node (e.g. "[header]", "[content]").
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
	private DesignTreeNode buildNode(ConfigurationItem config, String sourceFile) throws ConfigurationException {
		DesignTreeNode node = new DesignTreeNode(config, sourceFile);

		// Traverse all container properties (LIST/ITEM of PolymorphicConfiguration).
		// Each container property gets its own virtual group node — no inlining, so the tree
		// always shows the full structure without losing intermediate levels.
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (!isContainerProperty(property)) {
				continue;
			}
			if (property.kind() == PropertyKind.LIST) {
				List<?> children = (List<?>) config.value(property);
				if (children != null && !children.isEmpty()) {
					DesignTreeNode group = new DesignTreeNode(property.getPropertyName(), sourceFile);
					group.setParent(node);
					node.getChildren().add(group);
					for (Object childConfig : children) {
						addChild(group, (ConfigurationItem) childConfig, sourceFile);
					}
				}
			} else if (property.kind() == PropertyKind.ITEM) {
				Object childConfig = config.value(property);
				if (childConfig instanceof ConfigurationItem childItem) {
					DesignTreeNode group = new DesignTreeNode(property.getPropertyName(), sourceFile);
					group.setParent(node);
					node.getChildren().add(group);
					addChild(group, childItem, sourceFile);
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

	/**
	 * Whether the given property holds structured values that should be traversed.
	 */
	private boolean isContainerProperty(PropertyDescriptor property) {
		PropertyKind kind = property.kind();
		return kind == PropertyKind.LIST || kind == PropertyKind.ITEM;
	}

	private void addChild(DesignTreeNode parent, ConfigurationItem childConfig,
			String sourceFile) throws ConfigurationException {
		DesignTreeNode child = buildNode(childConfig, sourceFile);
		child.setParent(parent);
		parent.getChildren().add(child);
	}
}
