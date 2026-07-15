/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * The hierarchical structure consumed by a tree-backed {@link RowSource}, separated from
 * display/expansion concerns.
 *
 * <p>
 * A tree-backed {@code RowSource} flattens the expanded, filtered subtree of this
 * structure into the windowed row sequence, so that flat and tree tables share one
 * {@link RowSource} API. {@link #children(Object)} may load lazily; an infinite/lazy tree
 * reports {@link #isFinite()} {@code false} so that descendant counts are not assumed.
 * </p>
 *
 * @param <N>
 *        The tree node type.
 * @param <R>
 *        The business object type exposed as the row data for cell value extraction.
 */
public interface TreeStructure<N, R> {

	/**
	 * The root nodes of the tree.
	 */
	List<N> roots();

	/**
	 * The child nodes of the given node. May be loaded lazily on first access.
	 */
	List<N> children(N node);

	/**
	 * Whether the given node is a leaf (has no children).
	 */
	boolean isLeaf(N node);

	/**
	 * Whether the tree is finite, i.e. descendant counts may be computed eagerly.
	 * Infinite or lazily unbounded trees return {@code false}.
	 */
	default boolean isFinite() {
		return true;
	}

	/**
	 * The business object wrapped by the given node, used for cell value extraction.
	 */
	R businessObject(N node);

}
