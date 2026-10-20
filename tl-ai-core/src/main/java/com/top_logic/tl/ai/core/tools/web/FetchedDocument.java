/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

/**
 * The text {@link WebFetchTool} extracted from one document, before a window of it is shown.
 *
 * <p>
 * The whole text is kept, not only the part one answer shows: the model reads a long document in
 * windows, and every window must be cut from the same text. A document fetched again between two
 * windows may have changed, and then an offset would point into a different text.
 * </p>
 *
 * @param url
 *        The address the document was read from, after any redirects.
 * @param title
 *        Its title, or empty.
 * @param text
 *        The extracted text: Markdown for an HTML page, the page texts with page markers for a PDF,
 *        the body for plain text.
 * @param pageStarts
 *        For a PDF, the offset in {@code text} at which each page starts, in page order, for the pages
 *        whose text was extracted; empty for every other document.
 * @param links
 *        For an HTML page, the absolute address of every link its text keeps; empty for every other
 *        document.
 * @param pageCount
 *        For a PDF, its number of pages, also those whose text was not extracted; {@code 0} for
 *        every other document.
 */
record FetchedDocument(String url, String title, String text, List<Integer> pageStarts, List<String> links,
		int pageCount) {

	/** A document without pages and links. */
	FetchedDocument(String url, String title, String text) {
		this(url, title, text, List.of(), List.of());
	}

	/** A document whose every page has its text in {@code text}. */
	FetchedDocument(String url, String title, String text, List<Integer> pageStarts, List<String> links) {
		this(url, title, text, pageStarts, links, pageStarts.size());
	}

	/**
	 * This document with its text cut to at most {@code maxChars} characters, or this document when it
	 * fits.
	 *
	 * <p>
	 * The text ends with a note that says where it was cut, so the reader knows the document goes on.
	 * The cut is put at the last paragraph break shortly before the limit, where there is one.
	 * </p>
	 */
	FetchedDocument cutTo(int maxChars) {
		if (text.length() <= maxChars) {
			return this;
		}
		String note = "\n\n[The text of this document is cut here: this application reads at most " + maxChars
			+ " characters of one document.]";
		int end = Math.max(0, maxChars - note.length());
		int paragraph = text.lastIndexOf("\n\n", end);
		if (paragraph > end - 2_000 && paragraph > 0) {
			end = paragraph;
		} else if (end > 0 && Character.isHighSurrogate(text.charAt(end - 1))) {
			end--;
		}
		int cut = end;
		List<Integer> starts = pageStarts.stream().filter(start -> start.intValue() < cut).toList();
		return new FetchedDocument(url, title, text.substring(0, cut) + note, starts, links, pageCount);
	}

	/**
	 * The 1-based number of the page that holds the character at {@code offset}, or {@code 0} for a
	 * document without pages.
	 */
	int pageAt(int offset) {
		int page = 0;
		for (int i = 0; i < pageStarts.size() && pageStarts.get(i) <= offset; i++) {
			page = i + 1;
		}
		return page;
	}

}
