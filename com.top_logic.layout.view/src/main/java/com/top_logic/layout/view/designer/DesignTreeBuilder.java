/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;

/**
 * Builds a {@link DesignTreeNode} tree from a root view's
 * {@link com.top_logic.layout.view.UIElement.Config} hierarchy.
 *
 * <p>
 * Eagerly resolves {@link com.top_logic.layout.view.ReferenceElement.Config} by loading the
 * referenced view's config via
 * {@link ViewLoader#getOrLoadConfig(String)}.
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

	private DesignTreeNode buildNode(PolymorphicConfiguration<? extends UIElement> config,
			String sourceFile) throws ConfigurationException {
		DesignTreeNode node = new DesignTreeNode(config, sourceFile);

		// Add direct children based on config type
		if (config instanceof ViewElement.Config viewConfig) {
			for (var childConfig : viewConfig.getContent()) {
				addChild(node, childConfig, sourceFile);
			}
		} else if (config instanceof ContainerElement.Config containerConfig) {
			for (var childConfig : containerConfig.getChildren()) {
				addChild(node, childConfig, sourceFile);
			}
		}

		// For ReferenceElement, also load referenced view as a child
		if (config instanceof ReferenceElement.Config refConfig) {
			String refPath = ViewLoader.VIEW_BASE_PATH + refConfig.getView();
			ViewElement.Config refViewConfig = ViewLoader.getOrLoadConfig(refPath);
			addChild(node, refViewConfig, refPath);
		}

		return node;
	}

	private void addChild(DesignTreeNode parent,
			PolymorphicConfiguration<? extends UIElement> childConfig,
			String sourceFile) throws ConfigurationException {
		DesignTreeNode child = buildNode(childConfig, sourceFile);
		child.setParent(parent);
		parent.getChildren().add(child);
	}
}
