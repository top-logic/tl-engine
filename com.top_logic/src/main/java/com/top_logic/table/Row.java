/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * A single row as currently displayed by a {@link RowSource}.
 *
 * <p>
 * This is the windowed unit of the table model. A row carries a {@link #kind()}, a stable
 * {@link #key() identity} (for selection, expansion and incremental client patching), and
 * tree/group nesting via {@link #depth()} / {@link #expandable()} / {@link #expanded()}.
 * The same abstraction represents flat data rows, tree nodes, group headers and
 * aggregation rows.
 * </p>
 *
 * @param <R>
 *        The business object type of a {@link RowKind#DATA} row.
 */
public interface Row<R> {

	/**
	 * The kind of this row.
	 */
	RowKind kind();

	/**
	 * Stable identity of this row, used for selection, expansion state and incremental
	 * updates. Must be stable across re-windowing as long as the row logically exists.
	 */
	Object key();

	/**
	 * The business object for a {@link RowKind#DATA} row, or {@code null} for synthetic
	 * rows.
	 */
	R data();

	/**
	 * The group descriptor for a {@link RowKind#GROUP_HEADER} or {@link RowKind#AGGREGATE}
	 * row, or {@code null} for a {@link RowKind#DATA} row.
	 */
	GroupKey group();

	/**
	 * Nesting depth in a tree or grouping hierarchy. Top-level flat data rows are at depth
	 * {@code 0}.
	 */
	int depth();

	/**
	 * Whether this row can be expanded/collapsed (a non-leaf tree node or a collapsible
	 * group header).
	 */
	boolean expandable();

	/**
	 * Whether an {@link #expandable()} row is currently expanded.
	 */
	boolean expanded();

}
