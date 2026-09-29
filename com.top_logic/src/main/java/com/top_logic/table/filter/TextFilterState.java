/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import com.top_logic.table.FilterState;

/**
 * {@link FilterState} of a {@link TextColumnFilter}: a text pattern plus matching flags.
 *
 * @param pattern
 *        The text pattern to match.
 * @param caseSensitive
 *        Whether matching is case-sensitive.
 * @param regexp
 *        Whether the pattern is a regular expression (otherwise a substring).
 * @param wholeField
 *        Whether the whole cell text must match (otherwise a partial match suffices).
 */
public record TextFilterState(String pattern, boolean caseSensitive, boolean regexp, boolean wholeField)
		implements FilterState {

	@Override
	public boolean isEmpty() {
		return pattern == null || pattern.isEmpty();
	}

	/**
	 * A case-insensitive substring filter.
	 */
	public static TextFilterState contains(String pattern) {
		return new TextFilterState(pattern, false, false, false);
	}

}
