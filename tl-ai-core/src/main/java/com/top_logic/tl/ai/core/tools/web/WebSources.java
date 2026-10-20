/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.time.Instant;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.PageArchive;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.util.TLContext;

/**
 * Puts a web page a tool read on the run's reading list, and keeps a copy of it.
 *
 * <p>
 * The one place both routes to a page - {@code webFetch} and the browser - say what they read, so a
 * page is a source the same way whichever of them read it. Called on the tool's own thread, where
 * the run's reading list is bound; outside an agent run (a TL-Script call of the tool) nothing is
 * recorded and nothing archived.
 * </p>
 */
final class WebSources {

	/**
	 * The longest title a page is listed under.
	 *
	 * <p>
	 * A page chooses its own title, and the title travels further than the page's text: into the
	 * footnotes of an answer, the findings a worker delivers and the kept research run. One bounded
	 * line names a page; it is no channel for a paragraph of instructions.
	 * </p>
	 */
	static final int MAX_TITLE = 200;

	private WebSources() {
		// Static utility.
	}

	/**
	 * Records that the whole page at {@code finalUrl} was read.
	 *
	 * @param requestedUrl
	 *        The address the model asked for; the answer may cite the page by it when a redirect led
	 *        to {@code finalUrl}.
	 * @param finalUrl
	 *        The address the page was read from.
	 * @param title
	 *        Its title, or empty.
	 * @param text
	 *        What was extracted of it, as Markdown.
	 */
	static void read(String requestedUrl, String finalUrl, String title, String text) {
		read(requestedUrl, finalUrl, title, text, 0, text == null ? 0 : text.length());
	}

	/**
	 * Records that a part of the document at {@code finalUrl} was read.
	 *
	 * <p>
	 * A long document is read in windows. What the run's reading list and the archived copy hold of
	 * it is every window the run read, in document order - so a passage from the last page of a long
	 * report is citable when the run read that page, and a passage it did not read is not. The copy
	 * the run already has of the document is extended rather than stored again per window.
	 * </p>
	 *
	 * @param requestedUrl
	 *        The address the model asked for.
	 * @param finalUrl
	 *        The address the document was read from.
	 * @param title
	 *        Its title, or empty.
	 * @param document
	 *        The whole extracted text of the document, as Markdown.
	 * @param start
	 *        Where the part the run read starts.
	 * @param end
	 *        Where it ends, exclusive.
	 */
	static void read(String requestedUrl, String finalUrl, String title, String document, int start, int end) {
		ReadingList list = ReadingListBinding.current();
		if (list == null || finalUrl == null || finalUrl.isBlank() || document == null) {
			return;
		}
		String text = list.readPart(finalUrl, document, start, end);
		String name = title(title, finalUrl);
		ReadSource known = list.find(finalUrl);
		String archiveUrl = known == null ? "" : known.archiveUrl();
		Instant readAt = known == null || known.archiveUrl().isEmpty() ? Instant.now() : known.readAt();
		if (PageArchive.Module.INSTANCE.isActive()) {
			PageArchive.Copy copy =
				PageArchive.getInstance().archive(finalUrl, name, text, currentPerson(), handleOf(archiveUrl));
			if (copy != null) {
				archiveUrl = copy.viewUrl();
				readAt = copy.fetchedAt();
			}
		}
		ReadingListBinding.read(
			new ReadSource(CitationKinds.WEB, finalUrl, name, finalUrl, text, archiveUrl, readAt),
			requestedUrl == null ? "" : requestedUrl);
	}

	/**
	 * The name a page is listed under: its title as one line of at most {@link #MAX_TITLE}
	 * characters, or its address when it has no title.
	 */
	private static String title(String title, String url) {
		String line = title == null ? "" : title.replaceAll("(?U)\\s+", " ").strip();
		if (line.isEmpty()) {
			return url;
		}
		if (line.length() <= MAX_TITLE) {
			return line;
		}
		int end = MAX_TITLE - 3;
		if (Character.isHighSurrogate(line.charAt(end - 1))) {
			end--;
		}
		return line.substring(0, end).stripTrailing() + "...";
	}

	/** The handle of the copy an archive address names, or <code>null</code> for none. */
	private static String handleOf(String archiveUrl) {
		int at = archiveUrl == null ? -1 : archiveUrl.indexOf(PageArchive.URL_PREFIX);
		return at < 0 ? null : archiveUrl.substring(at + PageArchive.URL_PREFIX.length());
	}

	/** The account the run acts for, or <code>null</code> when the thread names none. */
	private static Person currentPerson() {
		try {
			TLContext context = TLContext.getContext();
			return context == null ? null : context.getPerson();
		} catch (RuntimeException ex) {
			Logger.info("No account for the archived copy of a page; only administrators can open it.",
				WebSources.class);
			return null;
		}
	}
}
