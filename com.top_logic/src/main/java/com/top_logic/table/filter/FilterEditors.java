/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;

/**
 * Builds the {@link FilterEditor} for a concrete {@link ColumnFilter}, seeded from the
 * column's current {@link FilterState}.
 */
public final class FilterEditors {

	private FilterEditors() {
		// Utility class.
	}

	/**
	 * Creates the editor for the given filter and current state.
	 *
	 * @param filter
	 *        The column filter.
	 * @param current
	 *        The current filter state, or {@code null} if the column is not filtered.
	 * @param counts
	 *        Facet counts for option filters, or {@link MatchCounts#NONE}.
	 */
	public static FilterEditor create(ColumnFilter<?> filter, FilterState current, MatchCounts counts) {
		if (filter instanceof TextColumnFilter) {
			return new TextFilterEditor((TextFilterState) current);
		}
		if (filter instanceof BooleanColumnFilter) {
			return new BooleanFilterEditor((BooleanFilterState) current);
		}
		if (filter instanceof OptionsColumnFilter<?> options) {
			return new OptionsFilterEditor(options.options(), (OptionsFilterState) current, counts);
		}
		if (filter instanceof ComparableColumnFilter<?> comparable) {
			return comparableEditor(comparable, current);
		}
		throw new IllegalArgumentException("No filter editor for: " + filter.getClass().getName());
	}

	private static <V> FilterEditor comparableEditor(ComparableColumnFilter<V> filter, FilterState current) {
		@SuppressWarnings("unchecked")
		RangeFilterState<V> range = (RangeFilterState<V>) current;
		return new ComparableFilterEditor<>(filter, range);
	}

}
