/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.List;

/**
 * Moves a {@link DesignTreeNode} up or down within its parent's children list.
 *
 * <p>
 * The root node cannot be moved. The caller is responsible for rebuilding the tree UI after this
 * operation.
 * </p>
 */
public class MoveElementCommand {

	/**
	 * Direction constants for moving a node.
	 */
	public enum Direction {
		/** Move the node one position earlier in its parent's children list. */
		UP,

		/** Move the node one position later in its parent's children list. */
		DOWN
	}

	/**
	 * Moves the given node in the specified direction within its parent's children list.
	 *
	 * @param node
	 *        The node to move.
	 * @param direction
	 *        The direction to move.
	 * @return {@code true} if the node was successfully moved.
	 */
	public static boolean execute(DesignTreeNode node, Direction direction) {
		DesignTreeNode parent = node.getParent();
		if (parent == null) {
			return false;
		}

		List<DesignTreeNode> siblings = parent.getChildren();
		int index = siblings.indexOf(node);
		if (index < 0) {
			return false;
		}

		int targetIndex;
		switch (direction) {
			case UP:
				targetIndex = index - 1;
				break;
			case DOWN:
				targetIndex = index + 1;
				break;
			default:
				return false;
		}

		if (targetIndex < 0 || targetIndex >= siblings.size()) {
			return false;
		}

		siblings.remove(index);
		siblings.add(targetIndex, node);
		parent.markDirty();
		return true;
	}

	/**
	 * Whether the command can be executed for the given node and direction.
	 *
	 * @param node
	 *        The candidate node (may be {@code null}).
	 * @param direction
	 *        The intended move direction.
	 * @return {@code true} if the node can be moved in the given direction.
	 */
	public static boolean canExecute(DesignTreeNode node, Direction direction) {
		if (node == null || node.getParent() == null) {
			return false;
		}

		List<DesignTreeNode> siblings = node.getParent().getChildren();
		int index = siblings.indexOf(node);
		if (index < 0) {
			return false;
		}

		switch (direction) {
			case UP:
				return index > 0;
			case DOWN:
				return index < siblings.size() - 1;
			default:
				return false;
		}
	}
}
