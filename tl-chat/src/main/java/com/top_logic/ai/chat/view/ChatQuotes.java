/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.util.Resources;

/**
 * What the {@code quote} channel of {@code <chat-conversation/>} carries: a passage the reader
 * marked in the host's own viewer, as the composer's own token.
 *
 * <p>
 * The sibling of {@link ChatTurnChannels}, for the composer's text rather than for the turn's
 * settings. {@code reference} takes an <em>object</em>, whose quoted text is everything the element
 * knows about it; a quote takes <em>a piece of one</em>, which only the host that showed it can
 * name. So this channel carries the token itself - {@code kind}, {@code id}, {@code label},
 * {@code location} and {@code text} - as a map, which is what a TL-Script expression writes as
 * <code>{"id": $section, "location": …, "text": …}</code>.
 * </p>
 *
 * <p>
 * Only {@code id} and {@code text} have to be there: there is nothing to quote without the passage,
 * and nothing for a host tool to resolve without the object it is from. Everything else is
 * defaulted from the object under {@code id} - which is why {@code id} is normally written as the
 * model object itself and not as a printed identifier: an application has no way of spelling the
 * stable id its own {@code @} menu uses, and writing the object lets a nested {@code <mention>} say
 * how this application labels it.
 * </p>
 *
 * @see ChatConversationElement.Config#getQuote()
 * @see ChatConversationControl
 */
public final class ChatQuotes {

	/** The object the passage is from: the model object itself, or its stable id. */
	public static final String ID = "id";

	/** What decides the token's icon; the object's type name when the map leaves it out. */
	public static final String KIND = "kind";

	/** What the reader sees on the token; the object's label when the map leaves it out. */
	public static final String LABEL = "label";

	/** Where in the source the passage stands - a section title, {@code L87-93} - or nothing. */
	public static final String LOCATION = "location";

	/** The marked passage itself: what the model is meant to read. */
	public static final String TEXT = "text";

	/** No instances: this class is the rule, not a thing that has one. */
	private ChatQuotes() {
		// Static utility.
	}

	/**
	 * One write of the {@code quote} channel as the token the composer inserts, or {@code null}
	 * when the channel says nothing insertable.
	 *
	 * <p>
	 * {@code null} is the answer for anything that is not a map of the five fields, for a map with
	 * no passage, and for one whose object has no stable id. A bare model object is {@code null}
	 * too: an object with no passage is what the {@code reference} channel is for, and inserting the
	 * whole of it here would quote back exactly what the agent's instructions say not to.
	 * </p>
	 *
	 * @param written
	 *        What the host put on the channel; a one-element collection is read as its element, the
	 *        way a single-row selection writes one.
	 * @param tokenOf
	 *        How this element turns the object under {@code id} into a token - its kind, its stable
	 *        id and its label, with a nested {@code <mention>}'s say in the label. It is asked for
	 *        every {@code id} that is not written as text, and answers an empty map for anything it
	 *        cannot make a token of.
	 * @return The token, ready for the browser to insert, or {@code null} for nothing insertable.
	 */
	public static Map<String, Object> token(Object written, Function<Object, Map<String, Object>> tokenOf) {
		Map<?, ?> fields = fields(written);
		if (fields == null) {
			return null;
		}
		String passage = text(fields.get(TEXT));
		if (passage.isBlank()) {
			return null;
		}

		Object source = fields.get(ID);
		Map<String, Object> base;
		String id;
		if (source instanceof CharSequence chars) {
			base = Map.of();
			id = chars.toString().trim();
		} else if (source == null) {
			return null;
		} else {
			base = tokenOf.apply(source);
			id = field(base, ID);
		}
		if (id.isEmpty()) {
			return null;
		}
		// What the write says wins over what the object says: the host showed the passage and knows
		// how to name it. The id is the exception - it is the object's, or it is nothing.
		String kind = either(field(fields, KIND), field(base, KIND));
		String label = either(field(fields, LABEL), either(field(base, LABEL), id));

		Map<String, Object> token = new LinkedHashMap<>();
		token.put(KIND, kind);
		token.put(ID, id);
		token.put(LABEL, label);
		token.put(LOCATION, field(fields, LOCATION));
		// Verbatim, unlike the fields above: this is what the reader marked, not a name for it.
		token.put(TEXT, passage);
		return token;
	}

	/**
	 * One naming field of a written map, or of the token the element built for its object.
	 *
	 * @param fields
	 *        The map to read.
	 * @param key
	 *        Which field.
	 * @return Its text, trimmed; the empty string when it says nothing.
	 */
	private static String field(Map<?, ?> fields, String key) {
		return text(fields.get(key)).trim();
	}

	/**
	 * The map a host wrote, also when a selection channel handed over its single row.
	 *
	 * @param written
	 *        The channel's value.
	 * @return The map, or {@code null} when the value is not one.
	 */
	private static Map<?, ?> fields(Object written) {
		Object value = written;
		if (value instanceof Collection<?> many) {
			if (many.size() != 1) {
				return null;
			}
			value = many.iterator().next();
		}
		return value instanceof Map<?, ?> map ? map : null;
	}

	/**
	 * A value written onto a chat channel as the text it stands for.
	 *
	 * <p>
	 * A {@link ResKey} is resolved rather than printed, because the obvious way to write a sentence
	 * from TL-Script is {@code {"en": …, "de": …}.internationalize()} and the user is to read their
	 * own language, not a resource key. Anything else is labelled the way the transcript labels it.
	 * </p>
	 *
	 * @param value
	 *        What the channel holds.
	 * @return The text, never {@code null}; the empty string for nothing.
	 */
	public static String text(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof ResKey key) {
			return Resources.getInstance().getString(key);
		}
		if (value instanceof CharSequence chars) {
			return chars.toString();
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}

	/**
	 * What the write said, or what to say when it said nothing.
	 *
	 * @param value
	 *        What the write said.
	 * @param fallback
	 *        What to say instead.
	 * @return The first of the two that is not blank.
	 */
	private static String either(String value, String fallback) {
		return value.isBlank() ? fallback : value;
	}
}
