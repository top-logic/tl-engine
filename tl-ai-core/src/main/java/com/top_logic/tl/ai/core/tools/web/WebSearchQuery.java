/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

/**
 * What one web search asks a {@link WebSearchEngine} for.
 *
 * @param query
 *        The search query; never {@code null} or blank.
 * @param includeDomains
 *        Restrict results to these domains; empty for no restriction.
 * @param excludeDomains
 *        Keep results from these domains out; empty for none.
 * @param count
 *        How many results to return, at least 1 and at most {@link #MAX_COUNT}.
 * @param freshness
 *        How recent the results must be; {@code null} for any time. An engine that cannot filter by
 *        time says so in {@link WebSearchEngine#supportsFreshness()}.
 * @param language
 *        The language of the results as an ISO 639-1 code, e.g. {@code de}; {@code null} for any
 *        language. An engine that cannot filter by language says so in
 *        {@link WebSearchEngine#supportsLanguage()}.
 */
public record WebSearchQuery(String query, List<String> includeDomains, List<String> excludeDomains, int count,
		Freshness freshness, String language) {

	/** The most results one search may ask for; Brave and Tavily both stop at 20. */
	public static final int MAX_COUNT = 20;

	/** A query for {@code count} results of any time and language, without domain filters. */
	public static WebSearchQuery of(String query, int count) {
		return new WebSearchQuery(query, List.of(), List.of(), count, null, null);
	}
}
