/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;
import java.util.function.Function;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Option;

/**
 * Builds the {@link FilterEditor} for a {@link ColumnFilter}, seeded from the column's current
 * {@link FilterState}.
 *
 * <p>
 * The editor is chosen from the filter's {@link FilterInput} <em>descriptor</em>, not from its
 * concrete class - so any filter declaring an {@link com.top_logic.table.FilterInput.Options options
 * input} reuses the checkbox editor, a custom {@link com.top_logic.table.FilterInput.Bool boolean}
 * filter the boolean editor, and so on.
 * A new filter plugs into the dialog by describing its input shape, without a new editor or a
 * change here. Two inputs still need a filter-specific detail: the boolean editor takes the option
 * labels from a {@link BooleanColumnFilter}, and the range editor the value parser from a
 * {@link ComparableColumnFilter}.
 * </p>
 */
public final class FilterEditors {

	private FilterEditors() {
		// Utility class.
	}

	/**
	 * Creates the editor for a column with the given only filter and current state.
	 *
	 * @param filter
	 *        The column filter.
	 * @param current
	 *        The current filter state, or {@code null} if the column is not filtered.
	 * @param counts
	 *        Facet counts for option filters, or {@link MatchCounts#NONE}.
	 *
	 * @see #create(List, FilterState, Function)
	 */
	public static ColumnFilterEditor create(ColumnFilter<?> filter, FilterState current, MatchCounts counts) {
		return create(List.of(filter), current, kind -> counts);
	}

	/**
	 * Creates the editor for a column with the given kinds of filter and current state.
	 *
	 * @param kinds
	 *        The {@link com.top_logic.table.Column#filters() kinds of filter} of the column.
	 * @param current
	 *        The current filter state, or {@code null} if the column is not filtered.
	 * @param counts
	 *        Facet counts per {@link ColumnFilter#kind()}, or {@link MatchCounts#NONE}.
	 */
	public static ColumnFilterEditor create(List<? extends ColumnFilter<?>> kinds, FilterState current,
			Function<String, MatchCounts> counts) {
		return new ColumnFilterEditor(kinds, current, counts);
	}

	/**
	 * Creates the editor of a single kind of filter, without the column's kind and inversion.
	 *
	 * @param filter
	 *        The kind of filter.
	 * @param current
	 *        The state of that filter, or {@code null} for an empty editor.
	 * @param counts
	 *        Facet counts for option filters, or {@link MatchCounts#NONE}.
	 */
	public static FilterEditor kindEditor(ColumnFilter<?> filter, FilterState current, MatchCounts counts) {
		FilterInput input = filter.input();
		if (input instanceof FilterInput.Options options) {
			OptionsFilterState selection = (OptionsFilterState) current;
			List<Option> offered = filter instanceof OptionsColumnFilter<?> optionsFilter
				? optionsFilter.options(counts, selection)
				: options.values();
			return new OptionsFilterEditor(offered, selection, counts);
		}
		if (input instanceof FilterInput.Bool bool) {
			return booleanEditor(filter, (BooleanFilterState) current, bool.nullable());
		}
		if (input instanceof FilterInput.Range) {
			return rangeEditor(filter, current);
		}
		if (input instanceof FilterInput.Text) {
			return new TextFilterEditor((TextFilterState) current);
		}
		throw new IllegalArgumentException("No filter editor for input: " + input);
	}

	private static FilterEditor booleanEditor(ColumnFilter<?> filter, BooleanFilterState current, boolean nullable) {
		if (filter instanceof BooleanColumnFilter bool) {
			return new BooleanFilterEditor(current, bool.trueLabel(), bool.falseLabel(), nullable);
		}
		return new BooleanFilterEditor(current, I18NConstants.VALUE_TRUE, I18NConstants.VALUE_FALSE, nullable);
	}

	private static FilterEditor rangeEditor(ColumnFilter<?> filter, FilterState current) {
		if (filter instanceof ComparableColumnFilter<?> comparable) {
			return comparableEditor(comparable, current);
		}
		throw new IllegalArgumentException(
			"A range-input filter must be a ComparableColumnFilter: " + filter.getClass().getName());
	}

	private static <V> FilterEditor comparableEditor(ComparableColumnFilter<V> filter, FilterState current) {
		@SuppressWarnings("unchecked")
		RangeFilterState<V> range = (RangeFilterState<V>) current;
		return new ComparableFilterEditor<>(filter, range);
	}

}
