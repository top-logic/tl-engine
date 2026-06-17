/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.Comparator;
import java.util.function.Predicate;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;

/**
 * A {@link ColumnFilter} comparing the cell value against a {@link RangeFilterState} using
 * a {@link Comparator}. A {@code null} cell value never matches.
 *
 * @param <V>
 *        The compared value type.
 */
public class ComparableColumnFilter<V> implements ColumnFilter<V> {

	private final Comparator<? super V> _comparator;

	/**
	 * Creates a {@link ComparableColumnFilter} with the given comparator.
	 */
	public ComparableColumnFilter(Comparator<? super V> comparator) {
		_comparator = comparator;
	}

	/**
	 * A {@link ComparableColumnFilter} using the natural ordering of the value type.
	 */
	public static <V extends Comparable<? super V>> ComparableColumnFilter<V> natural() {
		return new ComparableColumnFilter<>(Comparator.naturalOrder());
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Range();
	}

	@Override
	public Predicate<V> predicate(FilterState state) {
		@SuppressWarnings("unchecked")
		RangeFilterState<V> range = (RangeFilterState<V>) state;
		ComparisonOperator operator = range.operator();
		V primary = range.primary();
		V secondary = range.secondary();
		return value -> {
			if (value == null) {
				return false;
			}
			int toPrimary = _comparator.compare(value, primary);
			switch (operator) {
				case EQ:
					return toPrimary == 0;
				case NE:
					return toPrimary != 0;
				case LT:
					return toPrimary < 0;
				case LE:
					return toPrimary <= 0;
				case GT:
					return toPrimary > 0;
				case GE:
					return toPrimary >= 0;
				case BETWEEN:
					return toPrimary >= 0 && _comparator.compare(value, secondary) <= 0;
				default:
					return false;
			}
		};
	}

}
