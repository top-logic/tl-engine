/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;
import java.util.function.Predicate;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.Option;

/**
 * A {@link ColumnFilter} accepting cell values contained in a user-selected set of
 * {@link Option}s. Reports {@link #countsMatches()} so a view can show facet counts.
 *
 * @param <V>
 *        The cell value type.
 */
public class OptionsColumnFilter<V> implements ColumnFilter<V> {

	private final List<Option> _options;

	/**
	 * Creates an {@link OptionsColumnFilter} over the given options.
	 */
	public OptionsColumnFilter(List<Option> options) {
		_options = List.copyOf(options);
	}

	/**
	 * The options offered by this filter.
	 */
	public List<Option> options() {
		return _options;
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Options(_options);
	}

	@Override
	public Predicate<V> predicate(FilterState state) {
		OptionsFilterState options = (OptionsFilterState) state;
		return value -> options.selected().contains(value);
	}

	@Override
	public boolean countsMatches() {
		return true;
	}

}
