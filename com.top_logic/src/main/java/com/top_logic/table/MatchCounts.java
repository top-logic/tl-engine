/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * Facet counts for an option-style column filter: how many rows currently match each
 * option value.
 *
 * @see RowSource#matchCounts(String)
 */
public interface MatchCounts {

	/**
	 * An empty {@link MatchCounts} that is {@link #isAvailable() unavailable} and reports
	 * {@code 0} for every value.
	 */
	MatchCounts NONE = new MatchCounts() {
		@Override
		public int count(Object optionValue) {
			return 0;
		}

		@Override
		public boolean isAvailable() {
			return false;
		}
	};

	/**
	 * The number of currently displayed rows matching the given option value.
	 */
	int count(Object optionValue);

	/**
	 * The distinct facet values of the column over all rows of the table, regardless of any
	 * filter, in the order the column sorts its values (or in the order of their first occurrence
	 * for a column that cannot be sorted).
	 *
	 * <p>
	 * These are the options of a filter offering the {@link FilterInput.Options#occurring()
	 * occurring values}. The default offers none.
	 * </p>
	 */
	default List<Object> values() {
		return List.of();
	}

	/**
	 * Whether counts have been computed and are meaningful (counting can be disabled for
	 * performance, e.g. on large pushdown sources).
	 */
	boolean isAvailable();

}
