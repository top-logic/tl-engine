/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.configedit.ConfigChildren;

/**
 * Adds a new child element to the container property a {@link DesignTreeNode} represents.
 *
 * <p>
 * The element is inserted into the {@link DesignTreeNode#getChildContainer() child container} of the
 * given node, so the change is part of the configuration and is written to the {@code .view.xml}
 * file on save. The caller is responsible for rebuilding the tree UI afterwards.
 * </p>
 */
public class AddChildCommand {

	/**
	 * Adds a new child element of the given type to the given parent.
	 *
	 * @param parent
	 *        The node whose child container receives the new element.
	 * @param typeOption
	 *        The element type to create, one of the options of
	 *        {@link ConfigChildren#allowedTypes()}, or {@code null} for the container's default
	 *        type.
	 * @return The newly created child node, or {@code null} if no element could be added.
	 */
	public static DesignTreeNode execute(DesignTreeNode parent, Object typeOption) {
		ConfigChildren container = parent == null ? null : parent.getChildContainer();
		if (container == null || !container.canAdd()) {
			return null;
		}

		ConfigurationItem childConfig = container.newElement(typeOption);
		if (!container.add(childConfig)) {
			return null;
		}

		DesignTreeNode child = new ConfigDesignTreeNode(childConfig, parent.getSourceFile());
		child.setParent(parent);
		parent.getChildren().add(child);
		parent.markDirty();

		return child;
	}

	/**
	 * Whether a child can be added to the given node.
	 *
	 * @param node
	 *        The candidate parent node (may be {@code null}).
	 * @return {@code true} if the node has a child container that accepts another element.
	 */
	public static boolean canExecute(DesignTreeNode node) {
		ConfigChildren container = node == null ? null : node.getChildContainer();
		return container != null && container.canAdd();
	}
}
