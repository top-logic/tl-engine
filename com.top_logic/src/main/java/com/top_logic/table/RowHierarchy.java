/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * The tree the {@link RowKind#DATA data rows} of a tree-backed {@link RowSource} form: which row a
 * row is the child of, and which rows are the children of a row - whether that row is expanded or
 * not.
 *
 * <p>
 * The {@link RowSource#window(int, int) displayed rows} are the flattened expanded part of this
 * tree. Siblings are listed in the order they are displayed in, under the sort order and the filter
 * currently applied.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 *
 * @see RowSource#hierarchy()
 */
public interface RowHierarchy<R> {

	/**
	 * The top-level rows, in display order.
	 */
	List<Row<R>> roots();

	/**
	 * The business object the {@link #roots() top-level rows} are the children of, {@code null} if
	 * they are no children of any object.
	 *
	 * @see TreeStructure#rootParent()
	 */
	Object rootParent();

	/**
	 * The row holding the given one among its children, {@code null} for a top-level row.
	 *
	 * @param row
	 *        A row of this hierarchy.
	 */
	Row<R> parent(Row<R> row);

	/**
	 * The children of the given row, in display order - computed when the row is collapsed, too.
	 *
	 * @param row
	 *        A row of this hierarchy.
	 * @return The child rows, empty for a leaf.
	 */
	List<Row<R>> children(Row<R> row);

}
