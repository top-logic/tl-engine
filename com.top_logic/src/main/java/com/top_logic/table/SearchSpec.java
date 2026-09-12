/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

import com.top_logic.table.filter.TextFilterState;

/**
 * A free-text search over several columns: a row matches when the pattern is found in the
 * {@link Column#searchText(Object) searchable text} of at least one of the searched
 * columns (OR over the columns).
 *
 * <p>
 * Which columns are searched is decided by the caller, so a table can search exactly the
 * columns the user currently sees. A name that does not denote a column of the table is
 * skipped.
 * </p>
 *
 * @param pattern
 *        The text pattern together with its matching flags, or {@code null} for no search.
 * @param columns
 *        The {@link Column#name() names} of the columns to search.
 */
public record SearchSpec(TextFilterState pattern, List<String> columns) {

	/** The empty search (no pattern, no column). */
	public static final SearchSpec NONE = new SearchSpec(null, List.of());

	/**
	 * Creates a {@link SearchSpec} with a defensive, immutable copy of the column names.
	 */
	public SearchSpec {
		columns = List.copyOf(columns);
	}

	/**
	 * Whether this search selects nothing to match, either because no pattern is entered or
	 * because no column is searched.
	 */
	public boolean isEmpty() {
		return pattern == null || pattern.isEmpty() || columns.isEmpty();
	}

	/**
	 * A case-insensitive substring search over the given columns.
	 */
	public static SearchSpec contains(String pattern, List<String> columns) {
		return new SearchSpec(TextFilterState.contains(pattern), columns);
	}

	/**
	 * A case-insensitive substring search over the given columns.
	 */
	public static SearchSpec contains(String pattern, String... columns) {
		return contains(pattern, List.of(columns));
	}

}
