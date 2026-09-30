/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * A multi-column sort order: an ordered list of {@link SortColumn}s, primary first.
 *
 * @param columns
 *        The sort steps, primary sort column first.
 */
public record SortSpec(List<SortColumn> columns) {

	/** The empty sort order (no explicit ordering). */
	public static final SortSpec NONE = new SortSpec(List.of());

	/**
	 * Creates a {@link SortSpec} with a defensive, immutable copy of the steps.
	 */
	public SortSpec {
		columns = List.copyOf(columns);
	}

	/**
	 * A single-column ascending sort.
	 */
	public static SortSpec ascending(String column) {
		return new SortSpec(List.of(new SortColumn(column, true)));
	}

}
