/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.regex.Pattern;

/**
 * How the {@link TextFilterState#pattern() pattern} of a {@link TextFilterState} is found in a
 * text.
 *
 * <p>
 * Except for {@link #REGEXP}, the pattern is taken literally: characters with a special meaning in
 * a regular expression stand for themselves. Word boundaries are Unicode word boundaries: a word
 * is a maximal run of Unicode letters, digits and connector characters.
 * </p>
 */
public enum TextMatchMode {

	/** The pattern occurs anywhere in the text. */
	CONTAINS,

	/** A word of the text starts with the pattern. */
	WORD_START,

	/** The pattern occurs in the text as a whole word. */
	WHOLE_WORD,

	/** The pattern is a regular expression that is found in the text. */
	REGEXP;

	/**
	 * The regular expression finding the given pattern in this mode.
	 *
	 * @param pattern
	 *        The pattern as the user entered it.
	 * @param caseSensitive
	 *        Whether upper and lower case letters are distinguished.
	 */
	public Pattern compile(String pattern, boolean caseSensitive) {
		int flags = Pattern.UNICODE_CHARACTER_CLASS;
		if (!caseSensitive) {
			flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}
		switch (this) {
			case WORD_START:
				return Pattern.compile(NO_WORD_BEFORE + Pattern.quote(pattern), flags);
			case WHOLE_WORD:
				return Pattern.compile(NO_WORD_BEFORE + Pattern.quote(pattern) + NO_WORD_AFTER, flags);
			case REGEXP:
				return Pattern.compile(pattern, flags);
			case CONTAINS:
			default:
				return Pattern.compile(Pattern.quote(pattern), flags);
		}
	}

	/**
	 * A position not preceded by a word character, i.e. the start of the text or a Unicode word
	 * boundary, also in front of a pattern that does not start with a word character.
	 */
	private static final String NO_WORD_BEFORE = "(?<!\\w)";

	/**
	 * A position not followed by a word character, i.e. the end of the text or a Unicode word
	 * boundary, also behind a pattern that does not end with a word character.
	 */
	private static final String NO_WORD_AFTER = "(?!\\w)";

}
