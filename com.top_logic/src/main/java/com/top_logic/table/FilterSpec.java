/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.Map;

/**
 * The combined filter of a table: the active {@link FilterState} per column, combined with
 * AND semantics.
 *
 * @param byColumn
 *        Active filter state keyed by {@link Column#name() column name}.
 */
public record FilterSpec(Map<String, FilterState> byColumn) {

	/** The empty filter (no column filtered). */
	public static final FilterSpec NONE = new FilterSpec(Map.of());

	/**
	 * Creates a {@link FilterSpec} with a defensive, immutable copy of the map.
	 */
	public FilterSpec {
		byColumn = Map.copyOf(byColumn);
	}

}
