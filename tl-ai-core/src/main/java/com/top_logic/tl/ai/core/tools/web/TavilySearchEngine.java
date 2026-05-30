/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest;
import com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse;
import com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult;

/**
 * {@link WebSearchEngine} backed by the Tavily Search API ({@code POST /search}).
 */
public final class TavilySearchEngine implements WebSearchEngine {

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(TIMEOUT)
		.build();

	private final String _apiKey;

	private final String _baseUrl;

	private final String _searchDepth;

	private final boolean _includeAnswer;

	private final boolean _includeRawContent;

	/**
	 * Creates a {@link TavilySearchEngine}.
	 *
	 * @param apiKey
	 *        The Tavily API key; never {@code null} or blank.
	 * @param baseUrl
	 *        The API base URL, e.g. {@code https://api.tavily.com}.
	 * @param searchDepth
	 *        {@code basic} or {@code advanced}.
	 * @param includeAnswer
	 *        Whether to request a synthesized answer.
	 * @param includeRawContent
	 *        Whether to request full raw page content per result.
	 */
	public TavilySearchEngine(String apiKey, String baseUrl, String searchDepth, boolean includeAnswer,
			boolean includeRawContent) {
		_apiKey = apiKey;
		_baseUrl = baseUrl;
		_searchDepth = searchDepth;
		_includeAnswer = includeAnswer;
		_includeRawContent = includeRawContent;
	}

	@Override
	public WebSearchResults search(String query, List<String> includeDomains, List<String> excludeDomains,
			int maxResults) throws IOException, InterruptedException {
		TavilySearchRequest request = TavilySearchRequest.create();
		request.setQuery(query);
		request.setMaxResults(maxResults);
		request.setSearchDepth(_searchDepth);
		request.setIncludeAnswer(_includeAnswer);
		request.setIncludeRawContent(_includeRawContent);
		if (includeDomains != null && !includeDomains.isEmpty()) {
			request.setIncludeDomains(includeDomains);
		}
		if (excludeDomains != null && !excludeDomains.isEmpty()) {
			request.setExcludeDomains(excludeDomains);
		}

		String payload = DataClassSerializer.toJson(request, TavilySearchEngine.class, "search()");

		String base = _baseUrl.endsWith("/") ? _baseUrl.substring(0, _baseUrl.length() - 1) : _baseUrl;
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(base + "/search"))
			.header("Content-Type", "application/json")
			.header("Authorization", "Bearer " + _apiKey)
			.timeout(TIMEOUT)
			.POST(BodyPublishers.ofString(payload, UTF_8))
			.build();

		HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, BodyHandlers.ofString());
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
