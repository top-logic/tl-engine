/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import com.top_logic.layout.tree.TreeData;

/**
 * Behavior that implements reactions on a drag operation over a tree.
 * 
 * @see TreeData#getDragSource()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeDragSource {

	/**
	 * Whether dragging from the given tree is enabled.
	 * 
	 * @param data
	 *        The {@link TreeData} that potentially might operate as drag source.
	 */
	boolean dragEnabled(TreeData data);

	/**
	 * Whether the given node can be dragged in the given tree.
	 * 
	 * <p>
	 * Note: If this method returns <code>true</code> for any node of the given tree,
	 * {@link #dragEnabled(TreeData)} must also return <code>true</code> for the same tree.
	 * </p>
	 * 
	 * @param data
	 *        The {@link TreeData} that potentially might operate as drag source.
	 * @param node
	 *        The node that might serve as potential drag source.
	 * 
	 * @return Whether the given node can be dragged.
	 */
	boolean canDrag(TreeData data, Object node);

}
