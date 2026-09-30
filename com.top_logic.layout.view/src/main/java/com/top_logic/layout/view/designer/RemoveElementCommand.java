/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.layout.configedit.ConfigChildren;

/**
 * Removes the element a {@link DesignTreeNode} represents from the container property holding it.
 *
 * <p>
 * The element is removed from the parent's {@link DesignTreeNode#getChildContainer() child
 * container}, so the change is part of the configuration and is written to the {@code .view.xml}
 * file on save. The caller is responsible for clearing the selection and rebuilding the tree UI
 * afterwards.
 * </p>
 */
public class RemoveElementCommand {

	/**
	 * Removes the given node from its parent.
	 *
	 * @param node
	 *        The node to remove.
	 * @return {@code true} if the node was successfully removed.
	 */
	public static boolean execute(DesignTreeNode node) {
		if (!canExecute(node)) {
			return false;
		}

		DesignTreeNode parent = node.getParent();
		if (!parent.getChildContainer().remove(DesignTreeNodes.configOf(node))) {
			return false;
		}

		parent.getChildren().remove(node);
		node.setParent(null);
		node.cleanup();
		parent.markDirty();
		return true;
	}

	/**
	 * Whether the given node can be removed.
	 *
	 * @param node
	 *        The candidate node (may be {@code null}).
	 * @return {@code true} if the node is an element of its parent's child container. This excludes
	 *         the root, a view referenced through {@code <view-ref>} (which is edited by designing
	 *         that view), and a node standing in for a view that failed to load.
	 */
	public static boolean canExecute(DesignTreeNode node) {
		ConfigChildren container = DesignTreeNodes.owningContainer(node);
		return container != null;
	}
}
