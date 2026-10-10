/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
 *        For a PDF, the offset in {@code text} at which each page starts, in page order; empty for
 *        every other document.
 * @param links
 *        For an HTML page, the absolute address of every link its text keeps; empty for every other
 *        document.
 */
record FetchedDocument(String url, String title, String text, List<Integer> pageStarts, List<String> links) {

	/** A document without pages and links. */
	FetchedDocument(String url, String title, String text) {
		this(url, title, text, List.of(), List.of());
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

	/** The number of pages, {@code 0} for a document without pages. */
	int pageCount() {
		return pageStarts.size();
	}
}
