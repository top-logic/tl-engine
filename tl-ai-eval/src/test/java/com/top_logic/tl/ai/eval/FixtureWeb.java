/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.tl.ai.core.tools.web.WebSearchHit;

/**
 * The web as a scripted eval run sees it: search results and pages a test writes.
 *
 * <p>
 * Read by {@link FixtureSearchEngine} and {@link FixtureWebFetchTool}, which stand in for the real
 * search engine and the real {@code webFetch} in the eval's configuration, so that a run of the
 * research skill reads pages without a network. A page lives on a host under {@code .test}, a
 * top-level domain that never resolves (RFC 2606): an address of this web cannot be mistaken for a
 * real one, and every other address goes to the real tool.
 * </p>
 */
public final class FixtureWeb {

	/** The top-level domain the pages of this web live under. */
	public static final String HOST_SUFFIX = ".test";

	private static final Map<String, List<WebSearchHit>> SEARCHES = new ConcurrentHashMap<>();

	private static final Map<String, Page> PAGES = new ConcurrentHashMap<>();

	/**
	 * One page of this web.
	 *
	 * @param title
	 *        Its title.
	 * @param text
	 *        Its content, as {@code webFetch} would extract it.
	 */
	public record Page(String title, String text) {
		// Sole record.
	}

	private FixtureWeb() {
		// Static utility.
	}

	/** Answers the given query with the given hits. */
	public static void search(String query, WebSearchHit... hits) {
		SEARCHES.put(query, List.of(hits));
	}

	/** Serves the given page at the given address, which must be on a host under {@link #HOST_SUFFIX}. */
	public static void page(String url, String title, String text) {
		if (!isFixture(url)) {
			throw new IllegalArgumentException("Not an address of the fixture web: " + url);
		}
		PAGES.put(url, new Page(title, text));
	}

	/** Forgets every search and page. */
	public static void clear() {
		SEARCHES.clear();
		PAGES.clear();
	}

	/** The hits for the query, or {@code null} if no test wrote any. */
	static List<WebSearchHit> hits(String query) {
		return SEARCHES.get(query);
	}

	/** The page at the address, or {@code null} if there is none. */
	static Page get(String url) {
		return PAGES.get(url);
	}

	/** Whether the address belongs to this web rather than to the real one. */
	static boolean isFixture(String url) {
		try {
			String host = java.net.URI.create(url.trim()).getHost();
			return host != null && host.endsWith(HOST_SUFFIX);
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}
}
