/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

/**
 * The structure of the items of a control displaying a tree, as far as a {@link TreeDropPlace}
 * needs it to resolve a drop: which node holds a node, the children of a node in display order,
 * and whether a node is expanded.
 *
 * <p>
 * A control displaying its items as a tree - the nodes of a tree, the rows of a tree table -
 * implements this navigation over the nodes it displays, and leaves the rules of where a drop at a
 * place of it applies to {@link TreeDropPlace}.
 * </p>
 *
 * @param <N>
 *        The type of the nodes.
 */
public interface DropTreeNavigation<N> {

	/**
	 * The top-level nodes, in display order.
	 */
	List<? extends N> topLevel();

	/**
	 * The business object the {@link #topLevel() top-level nodes} are the children of, {@code null}
	 * if they are no children of any object.
	 */
	Object topLevelParent();

	/**
	 * The node holding the given one among its children, {@code null} for a top-level node.
	 */
	N parent(N node);

	/**
	 * The children of the given node in display order, computed for a collapsed node, too.
	 *
	 * @return The children, empty for a leaf.
	 */
	List<? extends N> children(N node);

	/**
	 * Whether the given node is expanded, displaying its children below it.
	 */
	boolean isExpanded(N node);

	/**
	 * The business object the given node stands for.
	 */
	Object businessObject(N node);

}
