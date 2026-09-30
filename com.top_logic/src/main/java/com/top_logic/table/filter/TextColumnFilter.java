/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.StringServices;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;

/**
 * A {@link ColumnFilter} matching a textual representation of the cell value against a
 * {@link TextFilterState} (substring or regular expression, case-sensitive or not, partial
 * or whole-field) using {@link TextFilterState#matcher()}.
 *
 * @param <V>
 *        The cell value type; converted to text via the supplied function.
 */
public class TextColumnFilter<V> implements ColumnFilter<V> {

	/** JSON key of {@link TextFilterState#pattern()}. */
	public static final String PATTERN = "pattern";

	/** JSON key of {@link TextFilterState#caseSensitive()}. */
	public static final String CASE_SENSITIVE = "caseSensitive";

	/** JSON key of {@link TextFilterState#regexp()}. */
	public static final String REGEXP = "regexp";

	/** JSON key of {@link TextFilterState#wholeField()}. */
	public static final String WHOLE_FIELD = "wholeField";

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
	public boolean supportsInversion() {
		return true;
	}

	@Override
	public Object toJson(FilterState state) {
		return textToJson((TextFilterState) state);
	}

	@Override
	public FilterState fromJson(Object json) {
		return textFromJson(json);
	}

	/**
	 * Serializes a {@link TextFilterState} into a JSON value map with the keys
	 * {@link #PATTERN}, {@link #CASE_SENSITIVE}, {@link #REGEXP} and {@link #WHOLE_FIELD}.
	 *
	 * <p>
	 * This is the single definition of the JSON shape of a text pattern, shared by the column
	 * filter and the cross-column search of a table, so both are stored the same way.
	 * </p>
	 */
	public static Map<String, Object> textToJson(TextFilterState state) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put(PATTERN, state.pattern());
		json.put(CASE_SENSITIVE, Boolean.valueOf(state.caseSensitive()));
		json.put(REGEXP, Boolean.valueOf(state.regexp()));
		json.put(WHOLE_FIELD, Boolean.valueOf(state.wholeField()));
		return json;
	}

	/**
	 * The {@link TextFilterState} of a {@link #textToJson(TextFilterState) serialized} JSON
	 * value, or {@code null} if the value is no such serialization (anything but a map, or a
	 * map holding no {@link #PATTERN}).
	 */
	public static TextFilterState textFromJson(Object json) {
		if (!(json instanceof Map<?, ?> map)) {
			return null;
		}
		Object pattern = map.get(PATTERN);
		if (pattern == null) {
			return null;
		}
		return new TextFilterState(String.valueOf(pattern), bool(map.get(CASE_SENSITIVE)), bool(map.get(REGEXP)),
			bool(map.get(WHOLE_FIELD)));
	}

	private static boolean bool(Object value) {
		return Boolean.TRUE.equals(value) || "true".equals(value);
	}

	/**
	 * A case-insensitive substring match of the text of the given value.
	 *
	 * <p>
	 * Accepts any single value. A value of the filtered column's own type is turned into text the
	 * same way a cell holding it is - so a criterion over a column showing business objects names
	 * them exactly as that column displays them, and the pattern matches the rows the criterion
	 * means. Anything else contributes its own {@link Object#toString() text}.
	 * </p>
	 *
	 * <p>
	 * A collection is rejected: a text pattern matches one text, so a set of alternatives is not
	 * expressible. So is a value with no text, which as a pattern would match every row.
	 * </p>
	 */
	@Override
	public FilterState stateFor(Object value) {
		if (value == null || value instanceof Collection<?> || value.getClass().isArray()) {
			return null;
		}
		String text = text(value);
		return StringServices.isEmpty(text) ? null : TextFilterState.contains(text);
	}

	/**
	 * The text a cell holding the given value is matched by, falling back to the value's own text
	 * for a value of some other type than the filtered column's.
	 */
	private String text(Object value) {
		try {
			@SuppressWarnings("unchecked")
			V typed = (V) value;
			String text = _textOf.apply(typed);
			if (!StringServices.isEmpty(text)) {
				return text;
			}
		} catch (ClassCastException ex) {
			// No value of the filtered column, so the conversion does not apply to it.
		}
		return value.toString();
	}

	@Override
	public Predicate<V> predicate(FilterState state) {
		Predicate<String> matcher = ((TextFilterState) state).matcher();
		return value -> {
			String string = _textOf.apply(value);
			return string != null && matcher.test(string);
		};
	}

}
