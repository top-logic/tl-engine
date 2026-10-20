/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * Summary of the text of a shape in a presentation template.
 *
 * @param style
 *        The effective style of the first text in the shape.
 * @param paragraphs
 *        The number of paragraphs.
 * @param content
 *        The plain text, shortened to {@link #MAX_CONTENT_LENGTH} characters.
 */
public record TextInfo(TextStyle style, int paragraphs, String content) {

	/**
	 * The maximum number of characters kept in {@link #content()}.
	 */
	public static final int MAX_CONTENT_LENGTH = 200;

	/**
	 * Creates a {@link TextInfo} shortening the given text.
	 */
	public static TextInfo create(TextStyle style, int paragraphs, String text) {
		return new TextInfo(style, paragraphs, shorten(text));
	}

	/**
	 * Shortens the given text to at most {@link #MAX_CONTENT_LENGTH} characters.
	 */
	public static String shorten(String text) {
		if (text == null || text.length() <= MAX_CONTENT_LENGTH) {
			return text;
		}
		return text.substring(0, MAX_CONTENT_LENGTH - 1) + "…";
	}

	/**
	 * The data representation: the entries of the {@link #style()} plus the keys
	 * {@link PresentationData#PARAGRAPHS} and {@link PresentationData#CONTENT}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = style == null ? PresentationData.map() : style.toData();
		result.put(PresentationData.PARAGRAPHS, Double.valueOf(paragraphs));
		PresentationData.put(result, PresentationData.CONTENT, content);
		return result;
	}

}
