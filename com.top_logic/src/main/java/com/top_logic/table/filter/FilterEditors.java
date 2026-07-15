/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.NegatedFilterState;

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
		// Inversion is a generic wrapper around any inner state; unwrap it to seed the inner editor
		// and re-offer the invert checkbox in its previous state.
		boolean inverted = current instanceof NegatedFilterState;
		FilterState inner = inverted ? ((NegatedFilterState) current).inner() : current;
		FilterEditor editor = innerEditor(filter, inner, counts);
		if (filter.supportsInversion()) {
			return new InvertingFilterEditor(editor, inverted);
		}
		return editor;
	}

	private static FilterEditor innerEditor(ColumnFilter<?> filter, FilterState current, MatchCounts counts) {
		FilterInput input = filter.input();
		if (input instanceof FilterInput.Options options) {
			return new OptionsFilterEditor(options.values(), (OptionsFilterState) current, counts);
		}
		if (input instanceof FilterInput.Bool) {
			return booleanEditor(filter, (BooleanFilterState) current);
		}
		if (input instanceof FilterInput.Range) {
			return rangeEditor(filter, current);
		}
		if (input instanceof FilterInput.Text) {
			return new TextFilterEditor((TextFilterState) current);
		}
		throw new IllegalArgumentException("No filter editor for input: " + input);
	}

	private static FilterEditor booleanEditor(ColumnFilter<?> filter, BooleanFilterState current) {
		if (filter instanceof BooleanColumnFilter bool) {
			return new BooleanFilterEditor(current, bool.trueLabel(), bool.falseLabel());
		}
		return new BooleanFilterEditor(current, I18NConstants.VALUE_TRUE, I18NConstants.VALUE_FALSE);
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
