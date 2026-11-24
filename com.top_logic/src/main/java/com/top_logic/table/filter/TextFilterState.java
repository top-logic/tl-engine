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
 * @param mode
 *        How the pattern is found in a text; {@code null} is normalized to
 *        {@link TextMatchMode#CONTAINS}.
 * @param wholeField
 *        Whether the whole cell text must match: in {@link TextMatchMode#REGEXP} mode the regular
 *        expression must match the whole text, in every other mode the text must equal the
 *        pattern. Not offered in a filter editor; set by a caller asking for exactly one text.
 */
public record TextFilterState(String pattern, boolean caseSensitive, TextMatchMode mode, boolean wholeField)
		implements FilterState {

	/**
	 * Creates a {@link TextFilterState}, normalizing a missing {@link #mode()}.
	 */
	public TextFilterState {
		mode = mode == null ? TextMatchMode.CONTAINS : mode;
	}

	/**
	 * Creates a {@link TextFilterState} that does not require the {@link #wholeField() whole
	 * field} to match.
	 */
	public TextFilterState(String pattern, boolean caseSensitive, TextMatchMode mode) {
		this(pattern, caseSensitive, mode, false);
	}

	@Override
	public boolean isEmpty() {
		return pattern == null || pattern.isEmpty();
	}

	/**
	 * The predicate deciding whether a piece of text matches this state, according to
	 * {@link #mode()}, {@link #caseSensitive()} and {@link #wholeField()}.
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
		if (mode() == TextMatchMode.REGEXP) {
			Pattern compiled = mode().compile(pattern(), caseSensitive());
			return wholeField()
				? string -> compiled.matcher(string).matches()
				: string -> compiled.matcher(string).find();
		}
		if (wholeField()) {
			return caseSensitive() ? string -> string.equals(pattern()) : string -> string.equalsIgnoreCase(pattern());
		}
		if (mode() == TextMatchMode.CONTAINS) {
			if (caseSensitive()) {
				return string -> string.contains(pattern());
			}
			String lower = pattern().toLowerCase();
			return string -> string.toLowerCase().contains(lower);
		}
		Pattern compiled = mode().compile(pattern(), caseSensitive());
		return string -> compiled.matcher(string).find();
	}

	/**
	 * A case-insensitive substring filter.
	 */
	public static TextFilterState contains(String pattern) {
		return new TextFilterState(pattern, false, TextMatchMode.CONTAINS);
	}

}
