/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.util.List;

/**
 * Pluggable backend for {@link WebSearchTool}.
 *
 * <p>
 * Implementations wrap a concrete search provider (Tavily, Brave, Google, ...) behind a single,
 * provider-neutral method so the tool and its configuration stay independent of the chosen
 * provider. Select the active implementation via {@link WebToolsService} configuration.
 * </p>
 */
public interface WebSearchEngine {

	/**
	 * Performs a web search.
	 *
	 * @param query
	 *        The search query; never {@code null} or blank.
	 * @param includeDomains
	 *        Restrict results to these domains. May be {@code null} or empty.
	 * @param excludeDomains
	 *        Exclude results from these domains. May be {@code null} or empty.
	 * @param maxResults
	 *        Maximum number of results to return.
	 * @return The provider-neutral results; never {@code null}.
	 * @throws IOException
	 *         On network or protocol errors, or a non-success response.
	 * @throws InterruptedException
	 *         If the calling thread is interrupted while waiting for the response.
	 */
	WebSearchResults search(String query, List<String> includeDomains, List<String> excludeDomains, int maxResults)
			throws IOException, InterruptedException;
}
