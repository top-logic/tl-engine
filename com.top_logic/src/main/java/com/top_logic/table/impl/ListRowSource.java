/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.table.Column;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowSource;
import com.top_logic.table.RowSourceListener;
import com.top_logic.table.SortColumn;
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
 * Grouping is not yet implemented and is added in a follow-up step; passing a non-empty
 * {@link GroupSpec} currently throws.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 */
public class ListRowSource<R> implements RowSource<R> {

	private final List<? extends R> _elements;

	private final List<Column<R, ?>> _columns;

	private final Function<? super R, ?> _keyOf;

	private final List<RowSourceListener> _listeners = new ArrayList<>();

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
		_columns = List.copyOf(columns);
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
		if (!grouping.columns().isEmpty()) {
			throw new UnsupportedOperationException("Grouping is not yet implemented in ListRowSource.");
		}
		_grouping = grouping;
		return this;
	}

	@Override
	public void setExpanded(Object rowKey, boolean expanded) {
		// Flat source: no expandable rows.
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
		List<R> rows = new ArrayList<>();
		Predicate<R> predicate = rowPredicate();
		for (R element : _elements) {
			if (predicate.test(element)) {
				rows.add(element);
			}
		}
		Comparator<R> order = rowComparator();
		if (order != null) {
			rows.sort(order);
		}
		List<Row<R>> displayed = new ArrayList<>(rows.size());
		for (R row : rows) {
			displayed.add(new DataRow<>(_keyOf.apply(row), row));
		}
		_displayed = displayed;
	}

	/**
	 * The combined AND predicate over all active column filters, or an accept-all
	 * predicate.
	 */
	private Predicate<R> rowPredicate() {
		Predicate<R> result = null;
		for (var entry : _filter.byColumn().entrySet()) {
			FilterState state = entry.getValue();
			if (state == null || state.isEmpty()) {
				continue;
			}
			Predicate<R> columnPredicate = columnPredicate(column(entry.getKey()), state);
			result = result == null ? columnPredicate : result.and(columnPredicate);
		}
		return result == null ? row -> true : result;
	}

	private <V> Predicate<R> columnPredicate(Column<R, V> column, FilterState state) {
		Predicate<V> valuePredicate = column.filter()
			.orElseThrow(() -> new IllegalStateException("Column '" + column.name() + "' is not filterable."))
			.predicate(state);
		return row -> valuePredicate.test(column.value(row));
	}

	/**
	 * The chained comparator for the active sort order, or {@code null} for no ordering.
	 */
	private Comparator<R> rowComparator() {
		Comparator<R> result = null;
		for (SortColumn sortColumn : _sort.columns()) {
			Comparator<R> step = columnComparator(column(sortColumn.column()), sortColumn.ascending());
			result = result == null ? step : result.thenComparing(step);
		}
		return result;
	}

	private <V> Comparator<R> columnComparator(Column<R, V> column, boolean ascending) {
		Comparator<V> valueComparator = column.sort()
			.orElseThrow(() -> new IllegalStateException("Column '" + column.name() + "' is not sortable."))
			.comparator();
		Comparator<R> rowComparator =
			Comparator.comparing(column::value, Comparator.nullsLast(valueComparator));
		return ascending ? rowComparator : rowComparator.reversed();
	}

	private Column<R, ?> column(String name) {
		for (Column<R, ?> column : _columns) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new IllegalArgumentException("No such column: " + name);
	}

	private void fireInvalidated() {
		for (RowSourceListener listener : List.copyOf(_listeners)) {
			listener.rowsInvalidated(0, Integer.MAX_VALUE);
		}
	}

}
