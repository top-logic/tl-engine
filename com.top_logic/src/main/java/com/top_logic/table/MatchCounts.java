/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

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
	 * Whether counts have been computed and are meaningful (counting can be disabled for
	 * performance, e.g. on large pushdown sources).
	 */
	boolean isAvailable();

}
