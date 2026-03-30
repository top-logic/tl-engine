/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.view.element.StackElement;

/**
 * Adds a new child {@link DesignTreeNode} to the given parent node.
 *
 * <p>
 * Creates a default {@link com.top_logic.layout.view.element.StackElement.Config} (a generic
 * container) and wraps it in a new
 * {@link DesignTreeNode} that is appended to the parent's children list. The caller is responsible
 * for rebuilding the tree UI after this operation.
 * </p>
 */
public class AddChildCommand {

	/**
	 * Adds a new default child node to the given parent.
	 *
	 * @param parent
	 *        The parent node to add a child to.
	 * @return The newly created child node.
	 */
	public static DesignTreeNode execute(DesignTreeNode parent) {
		StackElement.Config childConfig = TypedConfiguration.newConfigItem(StackElement.Config.class);

		DesignTreeNode child = new DesignTreeNode(childConfig, parent.getSourceFile());
		child.setParent(parent);
		parent.getChildren().add(child);

		return child;
	}

	/**
	 * Whether the command can be executed for the given node.
	 *
	 * @param node
	 *        The candidate parent node (may be {@code null}).
	 * @return {@code true} if a child can be added.
	 */
	public static boolean canExecute(DesignTreeNode node) {
		return node != null;
	}
}
