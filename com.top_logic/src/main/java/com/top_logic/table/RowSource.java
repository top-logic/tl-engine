/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
	 * Value of {@link #matchCount()} and {@link #dataCount()} for a source that cannot tell its
	 * count.
	 */
	int UNKNOWN_COUNT = -1;

	/**
	 * The number of data rows the current filter lets pass.
	 *
	 * <p>
	 * Counts the business objects, not the {@link #size() displayed rows}: a group header or an
	 * aggregate row is not counted, a data row inside a collapsed group or tree node is.
	 * </p>
	 *
	 * @return The count, or {@link #UNKNOWN_COUNT} for a source that would have to load what it
	 *         displays lazily to tell it.
	 */
	default int matchCount() {
		return UNKNOWN_COUNT;
	}

	/**
	 * The number of data rows regardless of the current filter.
	 *
	 * @return The count, or {@link #UNKNOWN_COUNT} for a source that would have to load what it
	 *         displays lazily to tell it.
	 *
	 * @see #matchCount()
	 */
	default int dataCount() {
		return UNKNOWN_COUNT;
	}

	/**
	 * The displayed rows in the half-open index range {@code [from, to)}, clamped to
	 * {@code [0, size())}.
	 */
	List<Row<R>> window(int from, int to);

	/**
	 * The given row keys that belong to a data row of this source, in the order they are given.
	 *
	 * <p>
	 * This is about the data, not about what is {@link #window(int, int) displayed}: a row the
	 * filter hides, or one inside a collapsed group or tree node, still belongs to the source. A key
	 * that is not returned names an object that is gone from the data.
	 * </p>
	 *
	 * @param keys
	 *        The {@link Row#key() row keys} to check.
	 * @return The keys among the given ones this source has a data row for.
	 */
	Set<Object> containedKeys(Collection<?> keys);

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
