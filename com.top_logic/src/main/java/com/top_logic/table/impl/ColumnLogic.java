/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;

import com.top_logic.table.Column;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;

/**
 * Shared logic deriving a row {@link Predicate} and {@link Comparator} from a
 * {@link FilterSpec} / {@link SortSpec} and the column definitions, used by the in-memory
 * {@link ListRowSource} and {@link TreeRowSource}.
 */
final class ColumnLogic {

	private ColumnLogic() {
		// Utility class.
	}

	/**
	 * The combined AND predicate over all active column filters, or {@code null} if no
	 * filter is active (accept all).
	 */
	static <R> Predicate<R> predicate(FilterSpec filter, Map<String, Column<R, ?>> byName) {
		return predicate(filter, byName, null);
	}

	/**
	 * The combined AND predicate over all active column filters except the one for
	 * {@code excludeColumn}, or {@code null} if none remains (accept all).
	 *
	 * <p>
	 * Excluding a column's own filter is what makes correct facet counts: each option's count
	 * reflects how many rows it would yield <em>given the other active filters</em>, independent of
	 * the current selection in this very column.
	 * </p>
	 *
	 * @param excludeColumn
	 *        The column whose own filter is left out, or {@code null} to include all.
	 */
	static <R> Predicate<R> predicate(FilterSpec filter, Map<String, Column<R, ?>> byName, String excludeColumn) {
		Predicate<R> result = null;
		for (Map.Entry<String, FilterState> entry : filter.byColumn().entrySet()) {
			if (entry.getKey().equals(excludeColumn)) {
				continue;
			}
			FilterState state = entry.getValue();
			if (state == null || state.isEmpty()) {
				continue;
			}
			Predicate<R> columnPredicate = columnPredicate(require(byName, entry.getKey()), state);
			result = result == null ? columnPredicate : result.and(columnPredicate);
		}
		return result;
	}

	private static <R, V> Predicate<R> columnPredicate(Column<R, V> column, FilterState state) {
		// Inversion is generic: unwrap it here so every concrete filter sees only its own state,
		// and negate the resulting value predicate.
		boolean inverted = false;
		if (state instanceof NegatedFilterState negated) {
			inverted = true;
			state = negated.inner();
		}
		Predicate<V> valuePredicate = column.filter()
			.orElseThrow(() -> new IllegalStateException("Column '" + column.name() + "' is not filterable."))
			.predicate(state);
		Predicate<V> effective = inverted ? valuePredicate.negate() : valuePredicate;
		return row -> effective.test(column.value(row));
	}

	/**
	 * The chained comparator for the given sort order, or {@code null} for no ordering.
	 */
	static <R> Comparator<R> comparator(SortSpec sort, Map<String, Column<R, ?>> byName) {
		Comparator<R> result = null;
		for (SortColumn sortColumn : sort.columns()) {
			Comparator<R> step = columnComparator(require(byName, sortColumn.column()), sortColumn.ascending());
			result = result == null ? step : result.thenComparing(step);
		}
		return result;
	}

	private static <R, V> Comparator<R> columnComparator(Column<R, V> column, boolean ascending) {
		Comparator<V> valueComparator = column.sort()
			.orElseThrow(() -> new IllegalStateException("Column '" + column.name() + "' is not sortable."))
			.comparator();
		Comparator<R> rowComparator =
			Comparator.comparing(column::value, Comparator.nullsLast(valueComparator));
		return ascending ? rowComparator : rowComparator.reversed();
	}

	private static <R> Column<R, ?> require(Map<String, Column<R, ?>> byName, String name) {
		Column<R, ?> column = byName.get(name);
		if (column == null) {
			throw new IllegalArgumentException("No such column: " + name);
		}
		return column;
	}

}
