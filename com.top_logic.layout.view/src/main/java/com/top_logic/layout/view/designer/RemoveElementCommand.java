/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

/**
 * Removes a {@link DesignTreeNode} from its parent's children list.
 *
 * <p>
 * The root node cannot be removed. The caller is responsible for clearing the selection and
 * rebuilding the tree UI after this operation.
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
		DesignTreeNode parent = node.getParent();
		if (parent == null) {
			// Cannot remove the root node.
			return false;
		}
		parent.getChildren().remove(node);
		node.setParent(null);
		return true;
	}

	/**
	 * Whether the command can be executed for the given node.
	 *
	 * @param node
	 *        The candidate node (may be {@code null}).
	 * @return {@code true} if the node can be removed (i.e., it is not the root).
	 */
	public static boolean canExecute(DesignTreeNode node) {
		return node != null && node.getParent() != null;
	}
}
