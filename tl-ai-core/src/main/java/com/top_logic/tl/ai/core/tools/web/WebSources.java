/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.time.Instant;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.PageArchive;
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

	private WebSources() {
		// Static utility.
	}

	/**
	 * Records that the page at {@code finalUrl} was read.
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
		if (ReadingListBinding.current() == null || finalUrl == null || finalUrl.isBlank()) {
			return;
		}
		String name = title == null || title.isBlank() ? finalUrl : title.strip();
		String archiveUrl = "";
		Instant readAt = Instant.now();
		if (PageArchive.Module.INSTANCE.isActive()) {
			PageArchive.Copy copy = PageArchive.getInstance().archive(finalUrl, name, text, currentPerson());
			if (copy != null) {
				archiveUrl = copy.viewUrl();
				readAt = copy.fetchedAt();
			}
		}
		ReadingListBinding.read(
			new ReadSource(CitationKinds.WEB, finalUrl, name, finalUrl, text, archiveUrl, readAt),
			requestedUrl == null ? "" : requestedUrl);
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
