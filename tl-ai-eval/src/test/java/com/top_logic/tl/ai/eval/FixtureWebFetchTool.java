/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.time.Instant;

import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.tools.web.UrlProvenance;
import com.top_logic.tl.ai.core.tools.web.WebFetchTool;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;

/**
 * {@code webFetch} as the eval has it: a page of {@link FixtureWeb} is read from there, every other
 * address by the real tool.
 *
 * <p>
 * A fixture page is recorded on the run's reading list as the real tool records a page - kind
 * {@link CitationKinds#WEB}, the address as id and link - and answered in the same envelope, so a
 * research run that cites it goes through the real citation path, and an address the run was not
 * given is refused as the real tool refuses it ({@link UrlProvenance}). A fixture page is short and comes
 * in one part, whatever the offset. The archived copy is left out: nobody opens it in an eval.
 * </p>
 *
 * <p>
 * Registered as the factory {@code web-fetch} in the eval's configuration, under the name and in the
 * capability group of the real tool, so that an agent configured with {@code web-fetch.webFetch}
 * gets this one and the background allowlist decides about it as about the real one.
 * </p>
 */
public class FixtureWebFetchTool extends WebFetchTool {

	/**
	 * Creates a {@link FixtureWebFetchTool}.
	 */
	public FixtureWebFetchTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	@Override
	@ToolMethod(
		description = "Fetch a single web page or PDF by its URL and return the main content as Markdown, "
			+ "preserving headings, links, lists and tables. "
			+ "Use after webSearch to read a specific result, or to read a URL the user provided. "
			+ "A long document is returned in parts: the answer then ends with the offset of the next "
			+ "part, and you read on by calling webFetch again with the same url and that offset. "
			+ "Only public http(s) documents are supported.",
		mode = ToolMode.READ, group = ToolGroups.EGRESS,
		marking = AnswerMarking.TOOL,
		truncationHint = Integer.MAX_VALUE)
	public String webFetch(
			@P("The absolute http or https URL to fetch.") String url,
			@P(value = "Optional: the character offset to start reading at. Leave it out for the start "
				+ "of the document; to read on, give the offset that the previous answer for the same url "
				+ "names.", required = false) Integer offset) {
		if (url == null || !FixtureWeb.isFixture(url)) {
			return super.webFetch(url, offset);
		}
		String address = url.trim();
		String unseen = UrlProvenance.refusal(address);
		if (unseen != null) {
			return unseen;
		}
		FixtureWeb.Page page = FixtureWeb.get(address);
		if (page == null) {
			return "Error: fetch failed (HTTP 404) for " + address;
		}
		ReadingListBinding.read(
			new ReadSource(CitationKinds.WEB, address, page.title(), address, page.text(), "", Instant.now()),
			address);
		UrlProvenance.recordAllIn(page.text());
		return UntrustedEnvelope.PAGE_CONTENT.wrap(address, page.text(), 50_000);
	}
}
