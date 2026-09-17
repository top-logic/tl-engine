/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.filter.TextFilterState;

/**
 * A filter criterion offered under a name: the column filters and the search term the user applies
 * in one step.
 *
 * <p>
 * The same value describes both origins of such a criterion - one declared by the table definition
 * and one the user saved themselves - so a UI offering them knows a single concept and tells the
 * two apart by the {@link #origin()} alone (only a {@link Origin#SAVED} one can be deleted).
 * </p>
 *
 * <p>
 * Which named filter is {@link TableView#activeNamedFilter() active} is derived by comparing its
 * criteria with the table's live ones, see {@link #matches(Map, TextFilterState)}: there is no
 * flag to maintain, and editing any column filter or the search term ends the match by itself.
 * Criteria are therefore normalized: a column whose state is {@link FilterState#isEmpty() empty}
 * is no criterion, and an empty search term is no search.
 * </p>
 *
 * @param id
 *        Stable identifier, unique among the named filters of one table;
 *        {@link TableView#applyNamedFilter(String)} and
 *        {@link TableView#deleteNamedFilter(String)} address a filter by it.
 * @param label
 *        The name to display.
 * @param origin
 *        Where this filter comes from.
 * @param filters
 *        The criterion per column, keyed by {@link Column#name() column name}.
 * @param search
 *        The cross-column search term, or {@code null} for no search.
 */
public record NamedFilter(String id, ResKey label, Origin origin, Map<String, FilterState> filters,
		TextFilterState search) {

	/**
	 * Where a {@link NamedFilter} comes from, which decides whether the user may delete it.
	 */
	public enum Origin {

		/** Declared by the table definition, offered to every user of the table. */
		DECLARED,

		/** Saved by the user, who may also delete it again. */
		SAVED;

	}

	/**
	 * Creates a {@link NamedFilter} with normalized criteria: empty column states and an empty
	 * search term are dropped, so that equal criteria compare equal.
	 */
	public NamedFilter {
		filters = criteria(filters);
		search = term(search);
	}

	/**
	 * A {@link Origin#DECLARED} {@link NamedFilter}.
	 *
	 * @see #NamedFilter(String, ResKey, Origin, Map, TextFilterState)
	 */
	public static NamedFilter declared(String id, ResKey label, Map<String, FilterState> filters,
			TextFilterState search) {
		return new NamedFilter(id, label, Origin.DECLARED, filters, search);
	}

	/**
	 * A {@link Origin#SAVED} {@link NamedFilter} labelled with the name the user typed.
	 *
	 * @param name
	 *        The free-text name, held as {@link ResKey#text(String) literal text} so that a saved
	 *        filter and a declared one carry their label the same way.
	 * @see #NamedFilter(String, ResKey, Origin, Map, TextFilterState)
	 */
	public static NamedFilter saved(String id, String name, Map<String, FilterState> filters,
			TextFilterState search) {
		return new NamedFilter(id, ResKey.text(name), Origin.SAVED, filters, search);
	}

	/**
	 * Whether the given criteria are exactly this filter's own, so that this filter is the active
	 * one.
	 *
	 * <p>
	 * The given criteria are normalized like this filter's, then compared by value: it makes no
	 * difference whether they were set by applying this filter or by the user reaching the same
	 * combination in the filter editors, and a column left with an empty state counts as
	 * unfiltered.
	 * </p>
	 *
	 * @param columnFilters
	 *        The live criterion per column, see {@link TableViewState#getFilters()}.
	 * @param searchTerm
	 *        The live search term, see {@link TableViewState#getSearch()}.
	 */
	public boolean matches(Map<String, FilterState> columnFilters, TextFilterState searchTerm) {
		return filters.equals(criteria(columnFilters)) && Objects.equals(search, term(searchTerm));
	}

	/**
	 * The given column criteria without the columns that are not filtered, as an immutable copy.
	 */
	private static Map<String, FilterState> criteria(Map<String, FilterState> columnFilters) {
		Map<String, FilterState> result = new LinkedHashMap<>();
		for (Map.Entry<String, FilterState> entry : columnFilters.entrySet()) {
			FilterState state = entry.getValue();
			if (state != null && !state.isEmpty()) {
				result.put(entry.getKey(), state);
			}
		}
		return Map.copyOf(result);
	}

	/**
	 * The given search term, or {@code null} if it searches for nothing.
	 */
	private static TextFilterState term(TextFilterState searchTerm) {
		return searchTerm == null || searchTerm.isEmpty() ? null : searchTerm;
	}

}
