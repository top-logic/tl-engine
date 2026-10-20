/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest;
import com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse;
import com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult;

/**
 * {@link WebSearchEngine} backed by the Tavily Search API ({@code POST /search}).
 *
 * <p>
 * The age of the results is Tavily's {@code time_range}. Tavily cannot restrict results by
 * language.
 * </p>
 */
@InApp
@Label("Tavily")
public final class TavilySearchEngine implements WebSearchEngine {

	/**
	 * Configuration of {@link TavilySearchEngine}.
	 */
	public interface Config extends PolymorphicConfiguration<TavilySearchEngine> {

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
		 * Configuration property name for the search depth.
		 *
		 * @see #getSearchDepth()
		 */
		String SEARCH_DEPTH = "search-depth";

		/**
		 * Configuration property name for whether to request a synthesized answer.
		 *
		 * @see #getIncludeAnswer()
		 */
		String INCLUDE_ANSWER = "include-answer";

		/**
		 * Configuration property name for whether to request full raw page content.
		 *
		 * @see #getIncludeRawContent()
		 */
		String INCLUDE_RAW_CONTENT = "include-raw-content";

		/**
		 * The Tavily API key (stored encrypted).
		 *
		 * @see #API_KEY
		 */
		@Name(API_KEY)
		@Encrypted
		@Mandatory
		String getApiKey();

		/**
		 * The Tavily API base URL.
		 *
		 * @see #BASE_URL
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.tavily.com")
		String getBaseUrl();

		/**
		 * Search depth: {@link SearchDepth#BASIC} (faster) or {@link SearchDepth#ADVANCED} (more
		 * thorough).
		 *
		 * @see #SEARCH_DEPTH
		 */
		@Name(SEARCH_DEPTH)
		@FormattedDefault("BASIC")
		SearchDepth getSearchDepth();

		/**
		 * Whether to request a synthesized answer from Tavily.
		 *
		 * @see #INCLUDE_ANSWER
		 */
		@Name(INCLUDE_ANSWER)
		@BooleanDefault(true)
		boolean getIncludeAnswer();

		/**
		 * Whether to request the full raw page content per result.
		 *
		 * @see #INCLUDE_RAW_CONTENT
		 */
		@Name(INCLUDE_RAW_CONTENT)
		@BooleanDefault(false)
		boolean getIncludeRawContent();
	}

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(TIMEOUT)
		.build();

	private final String _apiKey;

	private final String _baseUrl;

	private final SearchDepth _searchDepth;

	private final boolean _includeAnswer;

	private final boolean _includeRawContent;

	private final SearchHttp _http;

	/**
	 * Creates a {@link TavilySearchEngine} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public TavilySearchEngine(InstantiationContext context, Config config) {
		this(config, SearchHttp.Sleeper.SYSTEM);
	}

	/**
	 * Creates a {@link TavilySearchEngine} that waits with {@code sleeper}. Package-visible for
	 * testing.
	 */
	TavilySearchEngine(Config config, SearchHttp.Sleeper sleeper) {
		_apiKey = config.getApiKey();
		_baseUrl = config.getBaseUrl();
		_searchDepth = config.getSearchDepth();
		_includeAnswer = config.getIncludeAnswer();
		_includeRawContent = config.getIncludeRawContent();
		_http = new SearchHttp("Tavily", HTTP_CLIENT, sleeper, Clock.systemUTC());
	}

	@Override
	public boolean supportsFreshness() {
		return true;
	}

	@Override
	public boolean supportsLanguage() {
		return false;
	}

	@Override
	public WebSearchResults search(WebSearchQuery query) throws IOException, InterruptedException {
		TavilySearchRequest request = TavilySearchRequest.create();
		request.setQuery(query.query());
		request.setMaxResults(Math.max(1, Math.min(query.count(), WebSearchQuery.MAX_COUNT)));
		request.setSearchDepth(_searchDepth.apiValue());
		request.setIncludeAnswer(_includeAnswer);
		request.setIncludeRawContent(_includeRawContent);
		if (!query.includeDomains().isEmpty()) {
			request.setIncludeDomains(query.includeDomains());
		}
		if (!query.excludeDomains().isEmpty()) {
			request.setExcludeDomains(query.excludeDomains());
		}
		if (query.freshness() != null) {
			request.setTimeRange(query.freshness().externalName());
		}

		String payload = DataClassSerializer.toJson(request, TavilySearchEngine.class, "search()");

		String base = _baseUrl.endsWith("/") ? _baseUrl.substring(0, _baseUrl.length() - 1) : _baseUrl;
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(base + "/search"))
			.header("Content-Type", "application/json")
			.header("Authorization", "Bearer " + _apiKey)
			.timeout(TIMEOUT)
			.POST(BodyPublishers.ofString(payload, UTF_8))
			.build();

		HttpResponse<String> response = _http.send(httpRequest);
		if (response.statusCode() / 100 != 2) {
			throw new IOException("Tavily search failed (HTTP " + response.statusCode() + "): " + response.body());
		}
		return parseResponse(response.body());
	}

	/**
	 * Parses a Tavily JSON response body into provider-neutral {@link WebSearchResults}.
	 *
	 * <p>
	 * Package-visible and side-effect free so it can be unit-tested without network access.
	 * </p>
	 */
	static WebSearchResults parseResponse(String json) {
		TavilySearchResponse parsed;
		try {
			parsed = TavilySearchResponse.readTavilySearchResponse(new JsonReader(new StringR(json)));
		} catch (IOException ex) {
			throw new IllegalArgumentException("Malformed Tavily response: " + ex.getMessage(), ex);
		}
		List<WebSearchHit> hits = new ArrayList<>();
		for (TavilySearchResult result : parsed.getResults()) {
			String rawContent = result.getRawContent();
			String content = (rawContent == null || rawContent.isEmpty()) ? null : rawContent;
			hits.add(new WebSearchHit(result.getTitle(), result.getUrl(), result.getContent(), content));
		}
		String answer = parsed.getAnswer();
		if (answer != null && answer.isEmpty()) {
			answer = null;
		}
		return new WebSearchResults(answer, hits);
	}
}
