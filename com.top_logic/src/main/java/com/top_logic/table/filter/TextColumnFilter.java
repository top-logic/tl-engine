/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;

/**
 * A {@link ColumnFilter} matching a textual representation of the cell value against a
 * {@link TextFilterState} (substring or regular expression, case-sensitive or not, partial
 * or whole-field).
 *
 * @param <V>
 *        The cell value type; converted to text via the supplied function.
 */
public class TextColumnFilter<V> implements ColumnFilter<V> {

	private final Function<? super V, String> _textOf;

	/**
	 * Creates a {@link TextColumnFilter} with the given value-to-text conversion.
	 */
	public TextColumnFilter(Function<? super V, String> textOf) {
		_textOf = textOf;
	}

	/**
	 * A {@link TextColumnFilter} for {@link String}-valued columns.
	 */
	public static TextColumnFilter<String> forStrings() {
		return new TextColumnFilter<>(Function.identity());
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Text();
	}

	@Override
	public Predicate<V> predicate(FilterState state) {
		TextFilterState text = (TextFilterState) state;
		Predicate<String> matcher = matcher(text);
		return value -> {
			String string = _textOf.apply(value);
			return string != null && matcher.test(string);
		};
	}

	private Predicate<String> matcher(TextFilterState text) {
		String pattern = text.pattern();
		if (text.regexp()) {
			int flags = text.caseSensitive() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
			Pattern compiled = Pattern.compile(pattern, flags);
			return text.wholeField()
				? string -> compiled.matcher(string).matches()
				: string -> compiled.matcher(string).find();
		}
		if (text.caseSensitive()) {
			return text.wholeField() ? string -> string.equals(pattern) : string -> string.contains(pattern);
		}
		String lower = pattern.toLowerCase();
		return text.wholeField()
			? string -> string.equalsIgnoreCase(pattern)
			: string -> string.toLowerCase().contains(lower);
	}

}
