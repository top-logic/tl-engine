/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

	@Override
	public boolean supportsInversion() {
		return true;
	}

	/**
	 * Serializes the selection as the indices of the selected options. Option values can be
	 * arbitrary business objects (classifiers, enums); their position in the stable option list is a
	 * type-agnostic identity that round-trips without an object-serialization strategy.
	 */
	@Override
	public Object toJson(FilterState state) {
		Set<Object> selected = ((OptionsFilterState) state).selected();
		List<Object> indices = new ArrayList<>();
		for (int n = 0; n < _options.size(); n++) {
			if (selected.contains(_options.get(n).value())) {
				indices.add(Integer.valueOf(n));
			}
		}
		return indices;
	}

	@Override
	public FilterState fromJson(Object json) {
		if (!(json instanceof List<?> indices)) {
			return null;
		}
		Set<Object> selected = new LinkedHashSet<>();
		for (Object index : indices) {
			if (index instanceof Number number) {
				int n = number.intValue();
				if (n >= 0 && n < _options.size()) {
					selected.add(_options.get(n).value());
				}
			}
		}
		return new OptionsFilterState(selected);
	}

}
