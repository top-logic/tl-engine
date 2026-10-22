/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.suggest.Suggestion;
import com.top_logic.layout.react.control.suggest.SuggestionSource;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link SuggestionSource} computing the suggestions for a text field by a TL-Script function.
 *
 * <p>
 * The function has the signature {@code self -> text -> caret -> entries}: {@code self} is the
 * object the field edits an attribute of (the {@link AttributeFieldModel#getObject() object of an
 * attribute field}, read anew for every computation), or {@code null} for a field editing no
 * attribute; {@code text} is the text typed so far; {@code caret} is the caret position in it.
 * </p>
 *
 * <p>
 * Each entry of the result is either a dictionary with the keys {@link #LABEL}, {@link #TEXT},
 * {@link #FROM} and {@link #TO} - the text replacing the characters from {@link #FROM} (inclusive)
 * to {@link #TO} (exclusive), shown by its label, which defaults to the text itself; the positions
 * default to the caret - or a plain string, inserted at the caret. An entry of another kind, one
 * without a text, or one whose positions do not describe a range of the typed text is skipped and
 * logged, so that one malformed entry does not cost the user the others.
 * </p>
 */
public class ScriptSuggestionSource implements SuggestionSource {

	/** Key of the label in a dictionary entry: what the list of suggestions shows. */
	public static final String LABEL = "label";

	/** Key of the text in a dictionary entry: the text replacing the range. */
	public static final String TEXT = "text";

	/** Key of the start of the replaced range in a dictionary entry, a character offset. */
	public static final String FROM = "from";

	/** Key of the end of the replaced range in a dictionary entry, an exclusive character offset. */
	public static final String TO = "to";

	private final QueryExecutor _function;

	private final FieldModel _model;

	/**
	 * Creates a {@link ScriptSuggestionSource}.
	 *
	 * @param function
	 *        The compiled function computing the suggestions.
	 * @param model
	 *        The model of the field the suggestions are offered for.
	 */
	public ScriptSuggestionSource(QueryExecutor function, FieldModel model) {
		_function = function;
		_model = model;
	}

	@Override
	public List<Suggestion> suggest(String text, int caret) {
		Object self = _model instanceof AttributeFieldModel attributeModel ? attributeModel.getObject() : null;
		List<?> entries = SearchExpression.asList(_function.execute(self, text, Integer.valueOf(caret)));

		List<Suggestion> result = new ArrayList<>(entries.size());
		for (Object entry : entries) {
			Suggestion suggestion = toSuggestion(entry, text, caret);
			if (suggestion == null) {
				Logger.warn("Skipping a malformed suggestion for the text '" + text + "': " + entry, this);
				continue;
			}
			result.add(suggestion);
		}
		return result;
	}

	/**
	 * The {@link Suggestion} described by the given entry of the script result, or {@code null} if
	 * the entry describes none.
	 */
	private static Suggestion toSuggestion(Object entry, String text, int caret) {
		if (entry instanceof CharSequence insertion) {
			return Suggestion.insert(insertion.toString(), caret);
		}
		if (!(entry instanceof Map<?, ?> dictionary)) {
			return null;
		}
		if (!(dictionary.get(TEXT) instanceof CharSequence replacement)) {
			return null;
		}
		Integer from = position(dictionary.get(FROM), caret);
		Integer to = position(dictionary.get(TO), caret);
		if (from == null || to == null || from.intValue() < 0 || to.intValue() < from.intValue()
			|| to.intValue() > text.length()) {
			return null;
		}
		return new Suggestion(label(dictionary.get(LABEL)), replacement.toString(), from.intValue(), to.intValue());
	}

	/**
	 * The position given by an entry, the caret if it gives none, or {@code null} if the given
	 * value is no whole number.
	 */
	private static Integer position(Object value, int caret) {
		if (value == null) {
			return Integer.valueOf(caret);
		}
		if (!(value instanceof Number number)) {
			return null;
		}
		double position = number.doubleValue();
		if (position != Math.rint(position) || Math.abs(position) > Integer.MAX_VALUE) {
			return null;
		}
		return Integer.valueOf((int) position);
	}

	/**
	 * The label given by an entry, {@code null} for none.
	 */
	private static String label(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof CharSequence label) {
			return label.toString();
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}

}
