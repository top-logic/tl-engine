/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.suggest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.layout.react.state.SuggestionEntry;

/**
 * A suggestion offered for a text being typed: a replacement for a range of that text.
 *
 * <p>
 * Picking the suggestion replaces the characters {@link #getFrom()} (inclusive) to
 * {@link #getTo()} (exclusive) of the text it was computed for by {@link #getText()}. With equal
 * positions, the suggestion inserts its text there.
 * </p>
 *
 * @see SuggestionSource
 */
public final class Suggestion {

	private final String _label;

	private final String _text;

	private final int _from;

	private final int _to;

	/**
	 * Creates a {@link Suggestion}.
	 *
	 * @param label
	 *        What the list of suggestions shows, {@code null} for the text itself.
	 * @param text
	 *        The text replacing the range, never {@code null}.
	 * @param from
	 *        The position of the first character replaced.
	 * @param to
	 *        The position behind the last character replaced, not before {@code from}.
	 * @throws IllegalArgumentException
	 *         If the positions do not describe a range.
	 */
	public Suggestion(String label, String text, int from, int to) {
		if (from < 0 || to < from) {
			throw new IllegalArgumentException("Not a range of a text: " + from + ".." + to);
		}
		_text = Objects.requireNonNull(text, "text");
		_label = label == null ? text : label;
		_from = from;
		_to = to;
	}

	/**
	 * A {@link Suggestion} inserting the given text at the given position, labelled by the text
	 * itself.
	 */
	public static Suggestion insert(String text, int position) {
		return new Suggestion(null, text, position, position);
	}

	/**
	 * What the list of suggestions shows for this suggestion.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * The text replacing the range {@link #getFrom()} to {@link #getTo()}.
	 */
	public String getText() {
		return _text;
	}

	/**
	 * The position of the first character replaced.
	 */
	public int getFrom() {
		return _from;
	}

	/**
	 * The position behind the last character replaced.
	 */
	public int getTo() {
		return _to;
	}

	/**
	 * Whether this suggestion replaces a range of the given text, i.e. its range ends within it.
	 */
	public boolean fits(String text) {
		return _to <= text.length();
	}

	/**
	 * This suggestion in the form the client receives it, a {@link SuggestionEntry}.
	 */
	public Map<String, Object> toState() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(SuggestionEntry.LABEL__PROP, _label);
		result.put(SuggestionEntry.TEXT__PROP, _text);
		result.put(SuggestionEntry.FROM__PROP, _from);
		result.put(SuggestionEntry.TO__PROP, _to);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Suggestion other)) {
			return false;
		}
		return _from == other._from && _to == other._to && _text.equals(other._text) && _label.equals(other._label);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_label, _text, _from, _to);
	}

	@Override
	public String toString() {
		return "Suggestion(" + _label + ": '" + _text + "' at " + _from + ".." + _to + ")";
	}

}
