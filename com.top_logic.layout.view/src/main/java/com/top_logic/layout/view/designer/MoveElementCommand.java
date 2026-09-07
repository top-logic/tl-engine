/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.configedit.ConfigChildren;

/**
 * Reorders the element a {@link DesignTreeNode} represents within the container property holding it.
 *
 * <p>
 * The element is moved within the parent's {@link DesignTreeNode#getChildContainer() child
 * container}, so the change is part of the configuration and is written to the {@code .view.xml}
 * file on save. The caller is responsible for rebuilding the tree UI afterwards.
 * </p>
 */
public class MoveElementCommand {

	/**
	 * Direction constants for moving a node.
	 */
	public enum Direction {
		/** Move the node one position earlier in its container. */
		UP(-1),

		/** Move the node one position later in its container. */
		DOWN(1);

		private final int _delta;

		private Direction(int delta) {
			_delta = delta;
		}

		/**
		 * The offset to apply to the element's position.
		 */
		public int delta() {
			return _delta;
		}
	}

	/**
	 * Moves the given node in the specified direction within its container.
	 *
	 * @param node
	 *        The node to move.
	 * @param direction
	 *        The direction to move.
	 * @return {@code true} if the node was successfully moved.
	 */
	public static boolean execute(DesignTreeNode node, Direction direction) {
		if (!canExecute(node, direction)) {
			return false;
		}

		DesignTreeNode parent = node.getParent();
		ConfigurationItem config = DesignTreeNodes.configOf(node);
		if (!parent.getChildContainer().move(config, direction.delta())) {
			return false;
		}

		// Mirror the new order in the tree. The mirror may hold nodes that are not elements of the
		// container (a <view-ref> child), so the sibling order is taken from the container.
		List<DesignTreeNode> siblings = parent.getChildren();
		siblings.remove(node);
		siblings.add(mirrorIndex(parent, config), node);
		parent.markDirty();
		return true;
	}

	/**
	 * Whether the given node can be moved in the given direction.
	 *
	 * @param node
	 *        The candidate node (may be {@code null}).
	 * @param direction
	 *        The intended move direction.
	 * @return {@code true} if the node is an element of an ordered container and the target position
	 *         is within it.
	 */
	public static boolean canExecute(DesignTreeNode node, Direction direction) {
		ConfigChildren container = DesignTreeNodes.owningContainer(node);
		if (container == null || !container.isList()) {
			return false;
		}
		int target = container.indexOf(DesignTreeNodes.configOf(node)) + direction.delta();
		return target >= 0 && target < container.elements().size();
	}

	/**
	 * The position in the parent's mirror children list corresponding to the config's position in
	 * the container.
	 */
	private static int mirrorIndex(DesignTreeNode parent, ConfigurationItem config) {
		int configIndex = parent.getChildContainer().indexOf(config);
		int seen = 0;
		List<DesignTreeNode> siblings = parent.getChildren();
		for (int i = 0; i < siblings.size(); i++) {
			if (parent.getChildContainer().indexOf(DesignTreeNodes.configOf(siblings.get(i))) >= 0) {
				if (seen == configIndex) {
					return i;
				}
				seen++;
			}
		}
		return siblings.size();
	}
}
