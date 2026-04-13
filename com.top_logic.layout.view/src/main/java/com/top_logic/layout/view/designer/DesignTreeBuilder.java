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
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;

/**
 * Builds a {@link DesignTreeNode} tree from a root view's
 * {@link com.top_logic.layout.view.UIElement.Config} hierarchy.
 *
 * <p>
 * Traverses only {@link TreeProperty}-annotated LIST and ITEM properties of each config. This
 * allows configuration authors to explicitly control which structural properties appear as tree
 * nodes vs. editable form fields.
 * </p>
 *
 * <p>
 * If a config has exactly one {@link TreeProperty}, its children are inlined directly into the
 * parent node (no virtual group node). Multiple {@link TreeProperty} properties each get their own
 * virtual group node with an internationalized label.
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
		DesignTreeNode node = new ConfigDesignTreeNode(config, sourceFile);

		// Collect tree properties.
		List<PropertyDescriptor> treeProperties = new ArrayList<>();
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (isTreeProperty(property)) {
				treeProperties.add(property);
			}
		}

		boolean inline = treeProperties.size() == 1;

		for (PropertyDescriptor property : treeProperties) {
			DesignTreeNode target = inline ? node : createGroupNode(node, property, sourceFile);
			if (property.kind() == PropertyKind.LIST) {
				List<?> children = (List<?>) config.value(property);
				if (children != null) {
					for (Object childConfig : children) {
						addChild(target, (ConfigurationItem) childConfig, sourceFile);
					}
				}
			} else if (property.kind() == PropertyKind.ITEM) {
				Object childConfig = config.value(property);
				if (childConfig instanceof ConfigurationItem childItem) {
					addChild(target, childItem, sourceFile);
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

	private DesignTreeNode createGroupNode(DesignTreeNode parent, PropertyDescriptor property, String sourceFile) {
		DesignTreeNode group = new VirtualDesignTreeNode(property, sourceFile);
		group.setParent(parent);
		parent.getChildren().add(group);
		return group;
	}

	/**
	 * Whether the given property is annotated with {@link TreeProperty} and should be traversed.
	 */
	private boolean isTreeProperty(PropertyDescriptor property) {
		TreeProperty annotation = property.getAnnotation(TreeProperty.class);
		return annotation != null && annotation.value();
	}

	private void addChild(DesignTreeNode parent, ConfigurationItem childConfig,
			String sourceFile) throws ConfigurationException {
		DesignTreeNode child = buildNode(childConfig, sourceFile);
		child.setParent(parent);
		parent.getChildren().add(child);
	}
}
