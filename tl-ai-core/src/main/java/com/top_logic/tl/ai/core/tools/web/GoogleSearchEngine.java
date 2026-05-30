/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse;
import com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult;

/**
 * {@link WebSearchEngine} backed by the Google Custom Search JSON API
 * ({@code GET /customsearch/v1}).
 *
 * <p>
 * Both an API key ({@code key}) and a Programmable Search Engine ID ({@code cx}) are required. A
 * single include- or exclude-domain is realised with the native {@code siteSearch}/
 * {@code siteSearchFilter} parameters; multiple domains fall back to {@code site:}/{@code -site:}
 * query operators, since the API supports only one {@code siteSearch} value.
 * </p>
 */
@InApp
public final class GoogleSearchEngine implements WebSearchEngine {

	/**
	 * Configuration of {@link GoogleSearchEngine}.
	 */
	public interface Config extends PolymorphicConfiguration<GoogleSearchEngine> {

		/**
		 * Configuration property name for the API key.
		 *
		 * @see #getApiKey()
		 */
		String API_KEY = "api-key";

		/**
		 * Configuration property name for the Programmable Search Engine ID.
		 *
		 * @see #getSearchEngineId()
		 */
		String SEARCH_ENGINE_ID = "search-engine-id";

		/**
		 * Configuration property name for the API base URL.
		 *
		 * @see #getBaseUrl()
		 */
		String BASE_URL = "base-url";

		/**
		 * The Google API key (stored encrypted).
		 *
		 * @see #API_KEY
		 */
		@Name(API_KEY)
		@Encrypted
		@Mandatory
		String getApiKey();

		/**
		 * The Programmable Search Engine ID ({@code cx}).
		 *
		 * @see #SEARCH_ENGINE_ID
		 */
		@Name(SEARCH_ENGINE_ID)
		@Mandatory
		String getSearchEngineId();

		/**
		 * The Google Custom Search API endpoint URL.
		 *
		 * @see #BASE_URL
		 */
		@Name(BASE_URL)
		@StringDefault("https://www.googleapis.com/customsearch/v1")
		String getBaseUrl();
	}

	/** Google caps {@code num} at 10 results per request. */
	private static final int MAX_NUM = 10;

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(TIMEOUT)
		.build();

	private final String _apiKey;

	private final String _searchEngineId;

	private final String _baseUrl;

	/**
	 * Creates a {@link GoogleSearchEngine} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public GoogleSearchEngine(InstantiationContext context, Config config) {
		_apiKey = config.getApiKey();
		_searchEngineId = config.getSearchEngineId();
		_baseUrl = config.getBaseUrl();
	}

	@Override
	public WebSearchResults search(String query, List<String> includeDomains, List<String> excludeDomains,
			int maxResults) throws IOException, InterruptedException {
		int num = Math.max(1, Math.min(maxResults, MAX_NUM));

		StringBuilder url = new StringBuilder(_baseUrl);
		url.append(_baseUrl.indexOf('?') < 0 ? '?' : '&');
		url.append("key=").append(encode(_apiKey));
		url.append("&cx=").append(encode(_searchEngineId));
		url.append("&num=").append(num);
		url.append("&q=").append(encode(applySiteOperators(query, includeDomains, excludeDomains)));
		appendSiteSearch(url, includeDomains, excludeDomains);

		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url.toString()))
			.header("Accept", "application/json")
			.timeout(TIMEOUT)
			.GET()
			.build();

		HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, BodyHandlers.ofString());
		if (response.statusCode() / 100 != 2) {
			throw new IOException("Google search failed (HTTP " + response.statusCode() + "): " + response.body());
		}
		return parseResponse(response.body());
	}

	/**
	 * Determines whether the native single-domain {@code siteSearch} parameter applies.
	 */
	private static boolean isSingle(List<String> domains) {
		return domains != null && domains.size() == 1;
	}

	/**
	 * Appends the native {@code siteSearch}/{@code siteSearchFilter} parameters when exactly one
	 * include- or exclude-domain is requested.
	 */
	private void appendSiteSearch(StringBuilder url, List<String> includeDomains, List<String> excludeDomains) {
		if (isSingle(includeDomains)) {
			url.append("&siteSearch=").append(encode(includeDomains.get(0))).append("&siteSearchFilter=i");
			Logger.info("Google: restricting to domain " + includeDomains.get(0)
				+ " via siteSearch (filter=i).", GoogleSearchEngine.class);
		} else if (isSingle(excludeDomains)) {
			url.append("&siteSearch=").append(encode(excludeDomains.get(0))).append("&siteSearchFilter=e");
			Logger.info("Google: excluding domain " + excludeDomains.get(0)
				+ " via siteSearch (filter=e).", GoogleSearchEngine.class);
		}
	}

	/**
	 * Builds the effective query, falling back to {@code site:}/{@code -site:} operators for multiple
	 * domains (the API supports only a single native {@code siteSearch} value). A single domain is
	 * handled natively by {@link #appendSiteSearch} and left out of the query here.
	 */
	private static String applySiteOperators(String query, List<String> includeDomains,
			List<String> excludeDomains) {
		StringBuilder result = new StringBuilder(query);
		if (includeDomains != null && includeDomains.size() > 1) {
			Logger.info("Google supports only one native siteSearch domain; applying " + includeDomains
				+ " via site: query operators instead.", GoogleSearchEngine.class);
			result.append(" (");
			for (int i = 0; i < includeDomains.size(); i++) {
				if (i > 0) {
					result.append(" OR ");
				}
				result.append("site:").append(includeDomains.get(i));
			}
			result.append(')');
		}
		if (excludeDomains != null && excludeDomains.size() > 1) {
			Logger.info("Google supports only one native siteSearch domain; applying " + excludeDomains
				+ " via -site: query operators instead.", GoogleSearchEngine.class);
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
	 * Parses a Google Custom Search JSON response body into provider-neutral
	 * {@link WebSearchResults}.
	 *
	 * <p>
	 * Package-visible and side-effect free so it can be unit-tested without network access. Google
	 * supplies no synthesized answer, so {@link WebSearchResults#answer()} is always {@code null}.
	 * </p>
	 */
	static WebSearchResults parseResponse(String json) {
		GoogleSearchResponse parsed;
		try {
			parsed = GoogleSearchResponse.readGoogleSearchResponse(new JsonReader(new StringR(json)));
		} catch (IOException ex) {
			throw new IllegalArgumentException("Malformed Google response: " + ex.getMessage(), ex);
		}
		List<WebSearchHit> hits = new ArrayList<>();
		for (GoogleSearchResult result : parsed.getItems()) {
			String snippet = result.getSnippet();
			if (snippet != null && snippet.isEmpty()) {
				snippet = null;
			}
			hits.add(new WebSearchHit(result.getTitle(), result.getLink(), snippet, null));
		}
		return new WebSearchResults(null, hits);
	}
}
