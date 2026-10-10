/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.regex.Pattern;

/**
 * One thing a research run found out, with the words of the source it rests on.
 *
 * <p>
 * A finding is recorded by the run that read the source and is kept on that run's
 * {@link ReadingList}, so it travels with the sources: when a subagent's result is delivered, its
 * findings join its lead's list. The quote is not the model's account of the source but the
 * source's own words - it is refused unless it occurs in the text the run read of that source
 * ({@link #occursIn(String, String)}) - so a lead can rest a statement on a finding without reading
 * the page again.
 * </p>
 *
 * @param id
 *        The identifier a report and the lead refer to the finding by, unique in the run tree.
 * @param claim
 *        What was found, in the recording run's words.
 * @param source
 *        The reference of the source on the reading list - its address for a web page.
 * @param sourceTitle
 *        The source's title, or its reference when it has none.
 * @param quote
 *        The words of the source the claim rests on.
 * @param recordedBy
 *        The agent whose run recorded the finding, or empty.
 */
public record Finding(String id, String claim, String source, String sourceTitle, String quote, String recordedBy) {

	/** Runs of white space, which a quote may render differently from the stored text. */
	private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

	/** A backslash escaping a punctuation character, which the stored Markdown has and a quote may not. */
	private static final Pattern MARKDOWN_ESCAPE = Pattern.compile("\\\\(\\p{Punct})");

	/**
	 * Whether {@code quote} occurs in {@code text}.
	 *
	 * <p>
	 * Word for word, but not white space for white space: a line break in a PDF's text is a space in a
	 * sentence quoted from it, so every run of white space counts as one space on both sides. The
	 * Markdown escapes of the stored text ({@code 2\.5}) are not required in the quote either.
	 * </p>
	 *
	 * @param text
	 *        The text read of a source.
	 * @param quote
	 *        The words claimed to be in it.
	 */
	public static boolean occursIn(String text, String quote) {
		if (text == null || quote == null) {
			return false;
		}
		String words = squeeze(quote);
		if (words.isEmpty()) {
			return false;
		}
		String squeezed = squeeze(text);
		return squeezed.contains(words) || squeeze(MARKDOWN_ESCAPE.matcher(text).replaceAll("$1")).contains(words);
	}

	private static String squeeze(String text) {
		return WHITE_SPACE.matcher(text).replaceAll(" ").strip();
	}
}
