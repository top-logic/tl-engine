/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;

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
	 * <p>
	 * A rate limit of the provider is waited out within bounds before this fails - see
	 * {@link SearchHttp}.
	 * </p>
	 *
	 * @param query
	 *        What to search for, and how; never {@code null}. A filter the engine does not
	 *        {@link #supportsFreshness() support} is ignored.
	 * @return The provider-neutral results; never {@code null}.
	 * @throws IOException
	 *         On network or protocol errors, or a non-success response.
	 * @throws InterruptedException
	 *         If the calling thread is interrupted while waiting for the response.
	 */
	WebSearchResults search(WebSearchQuery query) throws IOException, InterruptedException;

	/**
	 * Whether this engine can restrict results by {@link WebSearchQuery#freshness() age}.
	 */
	boolean supportsFreshness();

	/**
	 * Whether this engine can restrict results by {@link WebSearchQuery#language() language}.
	 */
	boolean supportsLanguage();
}
