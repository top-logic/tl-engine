/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.Map;

/**
 * The combined filter of a table: the active {@link FilterState} per column and a
 * cross-column {@link SearchSpec}, combined with AND semantics.
 *
 * <p>
 * A row is displayed when it passes every active column filter <em>and</em> the search. The
 * search itself is an OR over the columns it examines, so it is a criterion of its own
 * instead of a filter on a single column.
 * </p>
 *
 * @param byColumn
 *        Active filter state keyed by {@link Column#name() column name}.
 * @param search
 *        The active free-text search, {@link SearchSpec#NONE} for none ({@code null} is
 *        normalized to it).
 */
public record FilterSpec(Map<String, FilterState> byColumn, SearchSpec search) {

	/** The empty filter (no column filtered, no search). */
	public static final FilterSpec NONE = new FilterSpec(Map.of(), SearchSpec.NONE);

	/**
	 * Creates a {@link FilterSpec} with a defensive, immutable copy of the map.
	 */
	public FilterSpec {
		byColumn = Map.copyOf(byColumn);
		search = search == null ? SearchSpec.NONE : search;
	}

	/**
	 * Creates a {@link FilterSpec} with column filters only.
	 *
	 * @param byColumn
	 *        Active filter state keyed by {@link Column#name() column name}.
	 */
	public FilterSpec(Map<String, FilterState> byColumn) {
		this(byColumn, SearchSpec.NONE);
	}

}
