/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * A windowed sequence of displayed {@link Row}s - the green-field replacement for
 * {@code TableModel} / {@code ObjectTableModel} / {@code TreeTableModel}.
 *
 * <p>
 * Unlike the legacy model, a {@link RowSource} never requires full materialization: it
 * exposes a {@link #size() count} and a {@link #window(int, int) window} so that data can
 * be served lazily and sort/filter/grouping can be pushed down to the data tier.
 * Sorting, filtering and grouping derive new, independent views via
 * {@link #withOrder}/{@link #withFilter}/{@link #withGrouping}.
 * </p>
 *
 * <p>
 * Three intended implementations: an in-memory list source, a query-backed source
 * (count + windowed fetch with pushdown), and a tree source (flattening a
 * {@link TreeStructure}). All three expose the same windowed API, so the UI tier never
 * branches on flat vs. tree vs. grouped.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 */
public interface RowSource<R> {

	/**
	 * The number of currently displayed rows (data plus synthetic group/aggregate rows)
	 * after sort, filter, grouping and expansion.
	 */
	int size();

	/**
	 * The displayed rows in the half-open index range {@code [from, to)}, clamped to
	 * {@code [0, size())}.
	 */
	List<Row<R>> window(int from, int to);

	/**
	 * A view of this source with the given sort order applied.
	 */
	RowSource<R> withOrder(SortSpec sort);

	/**
	 * A view of this source with the given filter applied.
	 */
	RowSource<R> withFilter(FilterSpec filter);

	/**
	 * A view of this source with the given grouping applied.
	 */
	RowSource<R> withGrouping(GroupSpec grouping);

	/**
	 * Per-option facet counts for the given option-style filtered column, or
	 * {@link MatchCounts#NONE} if counting is unavailable.
	 */
	default MatchCounts matchCounts(String column) {
		return MatchCounts.NONE;
	}

	/**
	 * Expands or collapses the row with the given {@link Row#key() key} (a tree node or a
	 * collapsible group header).
	 */
	void setExpanded(Object rowKey, boolean expanded);

	/**
	 * Registers a listener notified when the displayed rows change.
	 */
	void addListener(RowSourceListener listener);

	/**
	 * Unregisters a previously {@link #addListener(RowSourceListener) registered} listener.
	 */
	void removeListener(RowSourceListener listener);

}
