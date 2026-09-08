/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.tools.util.UntrustedEnvelope;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool that searches the web via the configured {@link WebSearchEngine}.
 *
 * <p>
 * A ranked result list is the cheapest prompt-injection channel there is: the attacker needs no
 * access to anything, only a page that ranks for a query the agent is likely to make. So what the
 * engine returned - its answer, and every title, URL and excerpt - comes back inside the
 * {@link UntrustedEnvelope#SEARCH_RESULTS untrusted-content envelope}.
 * </p>
 *
 * <p>
 * The tool's own two sentences stay <em>outside</em> it: the line naming the query, and the standing
 * instruction to cite sources as markdown links. That instruction is the application talking to the
 * model, and putting it inside a block the model is told to treat as data would tell the model to
 * ignore it.
 * </p>
 *
 * <p>
 * {@link ToolMode#READ} is deliberate and says only that the search changes nothing in this
 * application. That it sends the query to a third party is the group's statement, not the mode's -
 * see {@link ToolGroups#EGRESS}, which is where an administrator gates outbound access.
 * </p>
 */
public class WebSearchTool {

	/**
	 * Creates a {@link WebSearchTool}.
	 */
	public WebSearchTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Searches the web and returns ranked results.
	 *
	 * @param query
	 *        The search query.
	 * @param allowedDomains
	 *        Optional domain allow-list. May be {@code null}.
	 * @param blockedDomains
	 *        Optional domain block-list. May be {@code null}.
	 * @return Formatted results, or an error string.
	 */
	@ToolMethod(
		description = "Search the web and return ranked results (title, URL, snippet). "
			+ "Use this to find current information or sources on the public internet. "
			+ "Cite any sources you rely on as markdown links.",
		mode = ToolMode.READ, group = ToolGroups.EGRESS)
	public String webSearch(
			@P("The search query.") String query,
			@P(value = "Optional: restrict results to these domains, e.g. [\"docs.oracle.com\"].",
				required = false) List<String> allowedDomains,
			@P(value = "Optional: exclude results from these domains. Do not combine with allowedDomains.",
				required = false) List<String> blockedDomains) {

		if (query == null || query.isBlank()) {
			return "Error: query is required.";
		}
		boolean hasAllow = allowedDomains != null && !allowedDomains.isEmpty();
		boolean hasBlock = blockedDomains != null && !blockedDomains.isEmpty();
		if (hasAllow && hasBlock) {
			return "Error: provide either allowedDomains or blockedDomains, not both.";
		}

		WebToolsService service = WebToolsService.getInstance();
		if (service == null) {
			return "Error: web tools service is not running.";
		}
		WebSearchEngine engine = service.getSearchEngine();
		if (engine == null) {
			return "Error: web search is not configured. Configure a search engine on the WebToolsService "
				+ "(e.g. a TavilySearchEngine with an api-key).";
		}

		try {
			WebSearchResults results =
				engine.search(query, allowedDomains, blockedDomains, service.getMaxResults());
			return format(query, results);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return "Error: web search was interrupted.";
		} catch (Exception ex) {
			Logger.error("Web search failed: " + ex.getMessage(), ex, WebSearchTool.class);
			return "Error: web search failed: " + ex.getMessage();
		}
	}

	/**
	 * Renders what the engine returned, with everything that came from the engine inside the
	 * untrusted-content envelope. Package-visible for testing.
	 *
	 * @see UntrustedEnvelope#SEARCH_RESULTS
	 */
	static String format(String query, WebSearchResults results) {
		boolean noAnswer = results.answer() == null || results.answer().isBlank();
		if (results.hits().isEmpty() && noAnswer) {
			// Nothing came back, so there is nothing to mark - and an empty envelope would only tell
			// the model to distrust a sentence this application wrote.
			return "No results found for query: " + query;
		}
		StringBuilder body = new StringBuilder();
		if (!noAnswer) {
			body.append("Answer: ").append(results.answer().strip()).append("\n");
		}
		int index = 1;
		for (WebSearchHit hit : results.hits()) {
			body.append("\n").append(index++).append(". ").append(nullToEmpty(hit.title())).append("\n");
			body.append("   URL: ").append(nullToEmpty(hit.url())).append("\n");
			String text = hit.content() != null ? hit.content() : hit.snippet();
			if (text != null && !text.isBlank()) {
				body.append("   ").append(text.strip()).append("\n");
			}
		}

		StringBuilder out = new StringBuilder();
		out.append("Web search results for: \"").append(query).append("\"\n\n");
		out.append(UntrustedEnvelope.SEARCH_RESULTS.wrap(query, body.toString().strip()));
		out.append("\n\nCite any sources you use as markdown links.");
		return out.toString();
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
