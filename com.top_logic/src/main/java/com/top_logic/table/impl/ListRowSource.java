/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.table.Column;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.Group;
import com.top_logic.table.GroupKey;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowSource;
import com.top_logic.table.RowSourceListener;
import com.top_logic.table.SortSpec;

/**
 * In-memory {@link RowSource} over a fixed {@link List} of business objects, applying sort
 * and filter using the columns' {@link Column#sort() comparators} and
 * {@link Column#filter() predicates}.
 *
 * <p>
 * This implementation is a single, mutable live source: {@link #withOrder},
 * {@link #withFilter} and {@link #withGrouping} update the active specification in place,
 * recompute the displayed rows and notify listeners, returning {@code this}. (The
 * pure-derivation variant from the specification is only required for the query-backed
 * source and can be introduced there.)
 * </p>
 *
 * <p>
 * Single-level grouping is supported: a non-empty {@link GroupSpec} buckets the
 * filtered/sorted rows by the first grouping column's value and emits an expandable
 * {@link GroupRow group header} per bucket (which doubles as the subtotal row) followed by
 * the group's data rows. Multi-column grouping is a follow-up step.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 */
public class ListRowSource<R> implements RowSource<R> {

	private final List<? extends R> _elements;

	private final Map<String, Column<R, ?>> _byName = new LinkedHashMap<>();

	private final Function<? super R, ?> _keyOf;

	private final List<RowSourceListener> _listeners = new ArrayList<>();

	private final Set<Object> _collapsed = new HashSet<>();

	private SortSpec _sort = SortSpec.NONE;

	private FilterSpec _filter = FilterSpec.NONE;

	private GroupSpec _grouping = GroupSpec.NONE;

	private List<Row<R>> _displayed;

	/**
	 * Creates a {@link ListRowSource} using object identity as the row key.
	 *
	 * @param elements
	 *        The backing business objects (not copied).
	 * @param columns
	 *        The column definitions used for sorting and filtering.
	 */
	public ListRowSource(List<? extends R> elements, List<Column<R, ?>> columns) {
		this(elements, columns, Function.identity());
	}

	/**
	 * Creates a {@link ListRowSource}.
	 *
	 * @param elements
	 *        The backing business objects (not copied).
	 * @param columns
	 *        The column definitions used for sorting and filtering.
	 * @param keyOf
	 *        Extracts a stable {@link Row#key() row key} from a business object.
	 */
	public ListRowSource(List<? extends R> elements, List<Column<R, ?>> columns,
			Function<? super R, ?> keyOf) {
		_elements = elements;
		for (Column<R, ?> column : columns) {
			_byName.put(column.name(), column);
		}
		_keyOf = keyOf;
		recompute();
	}

	@Override
	public int size() {
		return _displayed.size();
	}

	@Override
	public List<Row<R>> window(int from, int to) {
		int lo = Math.max(0, from);
		int hi = Math.min(_displayed.size(), to);
		if (lo >= hi) {
			return List.of();
		}
		return List.copyOf(_displayed.subList(lo, hi));
	}

	@Override
	public RowSource<R> withOrder(SortSpec sort) {
		_sort = sort;
		recompute();
		fireInvalidated();
		return this;
	}

	@Override
	public RowSource<R> withFilter(FilterSpec filter) {
		_filter = filter;
		recompute();
		fireInvalidated();
		return this;
	}

	@Override
	public RowSource<R> withGrouping(GroupSpec grouping) {
		if (grouping.columns().size() > 1) {
			throw new UnsupportedOperationException("Multi-column grouping is not yet implemented in ListRowSource.");
		}
		_grouping = grouping;
		recompute();
		fireInvalidated();
		return this;
	}

	@Override
	public void setExpanded(Object rowKey, boolean expanded) {
		if (_grouping.columns().isEmpty()) {
			return;
		}
		boolean changed = expanded ? _collapsed.remove(rowKey) : _collapsed.add(rowKey);
		if (changed) {
			recompute();
			fireInvalidated();
		}
	}

	@Override
	public void addListener(RowSourceListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(RowSourceListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Recomputes the displayed rows by filtering then sorting the backing elements.
	 */
	private void recompute() {
		Predicate<R> predicate = ColumnLogic.predicate(_filter, _byName);
		List<R> rows = new ArrayList<>();
		for (R element : _elements) {
			if (predicate == null || predicate.test(element)) {
				rows.add(element);
			}
		}
		Comparator<R> order = ColumnLogic.comparator(_sort, _byName);
		if (order != null) {
			rows.sort(order);
		}
		_displayed = _grouping.columns().isEmpty() ? flatRows(rows) : groupedRows(rows);
	}

	private List<Row<R>> flatRows(List<R> rows) {
		List<Row<R>> displayed = new ArrayList<>(rows.size());
		for (R row : rows) {
			displayed.add(new DataRow<>(_keyOf.apply(row), row));
		}
		return displayed;
	}

	/**
	 * Buckets the (already filtered and sorted) rows by the first grouping column's value,
	 * in first-appearance order, and emits an expandable group header per bucket followed
	 * by its data rows when expanded.
	 */
	private List<Row<R>> groupedRows(List<R> rows) {
		Column<R, ?> groupColumn = _byName.get(_grouping.columns().get(0));
		Map<Object, List<R>> buckets = new LinkedHashMap<>();
		for (R row : rows) {
			buckets.computeIfAbsent(groupColumn.value(row), key -> new ArrayList<>()).add(row);
		}
		List<Row<R>> displayed = new ArrayList<>();
		for (Map.Entry<Object, List<R>> bucket : buckets.entrySet()) {
			GroupKey key = new GroupKey(Collections.singletonList(bucket.getKey()));
			List<R> members = bucket.getValue();
			Group<R> group = new SimpleGroup<>(key, members);
			boolean expanded = !_collapsed.contains(key);
			displayed.add(new GroupRow<>(group, 0, expanded));
			if (expanded) {
				for (R member : members) {
					displayed.add(new DataRow<>(_keyOf.apply(member), member, 1));
				}
			}
		}
		return displayed;
	}

	private void fireInvalidated() {
		for (RowSourceListener listener : List.copyOf(_listeners)) {
			listener.rowsInvalidated(0, Integer.MAX_VALUE);
		}
	}

}
