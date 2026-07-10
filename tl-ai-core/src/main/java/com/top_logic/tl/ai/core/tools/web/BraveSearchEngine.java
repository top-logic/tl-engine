/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse;
import com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult;
import com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults;

/**
 * {@link WebSearchEngine} backed by the Brave Web Search API ({@code GET /res/v1/web/search}).
 *
 * <p>
 * Brave has no native include/exclude-domain request parameters, so domain filters are realised by
 * appending {@code site:}/{@code -site:} search operators to the query.
 * </p>
 */
@InApp
@Label("Brave")
public final class BraveSearchEngine implements WebSearchEngine {

	/**
	 * Configuration of {@link BraveSearchEngine}.
	 */
	public interface Config extends PolymorphicConfiguration<BraveSearchEngine> {

		/**
		 * Configuration property name for the API key.
		 *
		 * @see #getApiKey()
		 */
		String API_KEY = "api-key";

		/**
		 * Configuration property name for the API base URL.
		 *
		 * @see #getBaseUrl()
		 */
		String BASE_URL = "base-url";

		/**
		 * The Brave Search API subscription token (stored encrypted).
		 *
		 * @see #API_KEY
		 */
		@Name(API_KEY)
		@Encrypted
		@Mandatory
		String getApiKey();

		/**
		 * The Brave Search API endpoint URL.
		 *
		 * @see #BASE_URL
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.search.brave.com/res/v1/web/search")
		String getBaseUrl();
	}

	/** Brave caps {@code count} at 20 results per page. */
	private static final int MAX_COUNT = 20;

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(TIMEOUT)
		.build();

	private final String _apiKey;

	private final String _baseUrl;

	/**
	 * Creates a {@link BraveSearchEngine} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public BraveSearchEngine(InstantiationContext context, Config config) {
		_apiKey = config.getApiKey();
		_baseUrl = config.getBaseUrl();
	}

	@Override
	public WebSearchResults search(String query, List<String> includeDomains, List<String> excludeDomains,
			int maxResults) throws IOException, InterruptedException {
		String effectiveQuery = applyDomainFilters(query, includeDomains, excludeDomains);
		int count = Math.max(1, Math.min(maxResults, MAX_COUNT));

		String url = _baseUrl
			+ (_baseUrl.indexOf('?') < 0 ? '?' : '&')
			+ "q=" + encode(effectiveQuery)
			+ "&count=" + count;

		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
			.header("Accept", "application/json")
			.header("X-Subscription-Token", _apiKey)
			.timeout(TIMEOUT)
			.GET()
			.build();

		HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, BodyHandlers.ofString());
		if (response.statusCode() / 100 != 2) {
			throw new IOException("Brave search failed (HTTP " + response.statusCode() + "): " + response.body());
		}
		return parseResponse(response.body());
	}

	/**
	 * Appends {@code site:}/{@code -site:} search operators to the query for domain filtering, since
	 * Brave exposes no native domain parameters.
	 */
	private static String applyDomainFilters(String query, List<String> includeDomains,
			List<String> excludeDomains) {
		StringBuilder result = new StringBuilder(query);
		if (includeDomains != null && !includeDomains.isEmpty()) {
			Logger.info("Brave has no native include-domain parameter; applying " + includeDomains
				+ " via site: query operators.", BraveSearchEngine.class);
			result.append(" (");
			for (int i = 0; i < includeDomains.size(); i++) {
				if (i > 0) {
					result.append(" OR ");
				}
				result.append("site:").append(includeDomains.get(i));
			}
			result.append(')');
		}
		if (excludeDomains != null && !excludeDomains.isEmpty()) {
			Logger.info("Brave has no native exclude-domain parameter; applying " + excludeDomains
				+ " via -site: query operators.", BraveSearchEngine.class);
			for (String domain : excludeDomains) {
				result.append(" -site:").append(domain);
			}
		}
		return result.toString();
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	/**
	 * Parses a Brave JSON response body into provider-neutral {@link WebSearchResults}.
	 *
	 * <p>
	 * Package-visible and side-effect free so it can be unit-tested without network access. Brave
	 * supplies no synthesized answer, so {@link WebSearchResults#answer()} is always {@code null}.
	 * </p>
	 */
	static WebSearchResults parseResponse(String json) {
		BraveSearchResponse parsed;
		try {
			parsed = BraveSearchResponse.readBraveSearchResponse(new JsonReader(new StringR(json)));
		} catch (IOException ex) {
			throw new IllegalArgumentException("Malformed Brave response: " + ex.getMessage(), ex);
		}
		List<WebSearchHit> hits = new ArrayList<>();
		BraveWebResults web = parsed.getWeb();
		if (web != null) {
			for (BraveSearchResult result : web.getResults()) {
				String description = result.getDescription();
				String snippet = (description == null || description.isEmpty()) ? null : description;
				hits.add(new WebSearchHit(result.getTitle(), result.getUrl(), snippet, null));
			}
		}
		return new WebSearchResults(null, hits);
	}
}
