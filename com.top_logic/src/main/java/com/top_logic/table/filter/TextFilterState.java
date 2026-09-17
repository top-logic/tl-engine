/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.function.Predicate;
import java.util.regex.Pattern;

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
	 * The predicate deciding whether a piece of text matches this state, according to
	 * {@link #regexp()}, {@link #caseSensitive()} and {@link #wholeField()}.
	 *
	 * <p>
	 * This is the single definition of the text-matching semantics, shared by the
	 * {@link TextColumnFilter} and the cross-column search, so both find the same texts for
	 * the same {@link #pattern()}.
	 * </p>
	 *
	 * <p>
	 * Requires a {@link #pattern()}, i.e. a state that is not {@link #isEmpty()}.
	 * </p>
	 */
	public Predicate<String> matcher() {
		if (regexp()) {
			int flags = caseSensitive() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
			Pattern compiled = Pattern.compile(pattern(), flags);
			return wholeField()
				? string -> compiled.matcher(string).matches()
				: string -> compiled.matcher(string).find();
		}
		if (caseSensitive()) {
			return wholeField() ? string -> string.equals(pattern()) : string -> string.contains(pattern());
		}
		String lower = pattern().toLowerCase();
		return wholeField()
			? string -> string.equalsIgnoreCase(pattern())
			: string -> string.toLowerCase().contains(lower);
	}

	/**
	 * A case-insensitive substring filter.
	 */
	public static TextFilterState contains(String pattern) {
		return new TextFilterState(pattern, false, false, false);
	}

}
