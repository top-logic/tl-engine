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
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse;
import com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult;
import com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults;

/**
 * {@link WebSearchEngine} backed by the Brave Web Search API ({@code GET /res/v1/web/search}).
 *
 * <p>
 * Brave has no native include/exclude-domain request parameters, so domain filters are realised by
 * appending {@code site:}/{@code -site:} search operators to the query. The age of the results is
 * Brave's {@code freshness}, their language its {@code search_lang}.
 * </p>
 */
@InApp
@Label("Brave")
public final class BraveSearchEngine implements WebSearchEngine {

	/**
	 * The request header in which Brave expects the {@link Config#getApiKey() API key}.
	 */
	public static final String API_KEY_HEADER = "X-Subscription-Token";

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
		 * Configuration property name for the credentials.
		 *
		 * @see #getCredentials()
		 */
		String CREDENTIALS = "credentials";

		/**
		 * Configuration property name for the API base URL.
		 *
		 * @see #getBaseUrl()
		 */
		String BASE_URL = "base-url";

		/**
		 * The Brave API key.
		 *
		 * <p>
		 * Sent in the header {@link BraveSearchEngine#API_KEY_HEADER}. Recommended is a reference
		 * to the variable or file holding the key, so that the key is not part of the
		 * configuration:
		 * </p>
		 *
		 * <pre>
		 * &lt;api-key class="com.top_logic.basic.credential.EnvironmentSecretSource" name="tl_brave_api_key"/&gt;
		 * </pre>
		 *
		 * <p>
		 * An attribute value is an encrypted key, or a plain key with the prefix
		 * <code>unencrypted:</code>. Configuring both an API key and {@link #getCredentials()
		 * credentials} is an error.
		 * </p>
		 *
		 * @see #API_KEY
		 */
		@Name(API_KEY)
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		/**
		 * How the requests to Brave are authenticated, if not with an {@link #getApiKey() API key}.
		 *
		 * <p>
		 * E.g. no credentials at all, when a gateway in between adds them. Configuring both
		 * credentials and an API key is an error.
		 * </p>
		 *
		 * @see #CREDENTIALS
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		/**
		 * The Brave Search API endpoint URL.
		 *
		 * @see #BASE_URL
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.search.brave.com/res/v1/web/search")
		String getBaseUrl();
	}

	/** The name of the search engine, in messages. */
	private static final String LABEL = "Brave";

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(TIMEOUT)
		.build();

	private final CredentialProvider _credentials;

	private final String _baseUrl;

	private final SearchHttp _http;

	/**
	 * Creates a {@link BraveSearchEngine} from configuration.
	 */
	@CalledByReflection
	public BraveSearchEngine(InstantiationContext context, Config config) {
		this(context, config, SearchHttp.Sleeper.SYSTEM);
	}

	/**
	 * Creates a {@link BraveSearchEngine} that waits with {@code sleeper}. Package-visible for
	 * testing.
	 */
	BraveSearchEngine(InstantiationContext context, Config config, SearchHttp.Sleeper sleeper) {
		_credentials = CredentialProvider.create(context, LABEL, config.getCredentials(), config.getApiKey(),
			API_KEY_HEADER, null);
		_baseUrl = config.getBaseUrl();
		_http = new SearchHttp(LABEL, HTTP_CLIENT, _credentials, sleeper, Clock.systemUTC());
	}

	@Override
	public boolean supportsFreshness() {
		return true;
	}

	@Override
	public boolean supportsLanguage() {
		return true;
	}

	@Override
	public WebSearchResults search(WebSearchQuery query) throws IOException, InterruptedException {
		String effectiveQuery = applyDomainFilters(query.query(), query.includeDomains(), query.excludeDomains());
		int count = Math.max(1, Math.min(query.count(), WebSearchQuery.MAX_COUNT));

		StringBuilder url = new StringBuilder(_baseUrl)
			.append(_baseUrl.indexOf('?') < 0 ? '?' : '&')
			.append("q=").append(encode(effectiveQuery))
			.append("&count=").append(count);
		if (query.freshness() != null) {
			url.append("&freshness=").append(freshness(query.freshness()));
		}
		if (query.language() != null) {
			url.append("&search_lang=").append(encode(searchLang(query.language())));
		}

		HttpRequest.Builder httpRequest = HttpRequest.newBuilder(URI.create(url.toString()))
			.header("Accept", "application/json")
			.timeout(TIMEOUT)
			.GET();

		HttpResponse<String> response = _http.send(httpRequest);
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

	/** Brave's {@code freshness} value: past day, week, month or year. */
	private static String freshness(Freshness freshness) {
		switch (freshness) {
			case DAY:
				return "pd";
			case WEEK:
				return "pw";
			case MONTH:
				return "pm";
			case YEAR:
				return "py";
		}
		throw new IllegalArgumentException("No such freshness: " + freshness);
	}

	/**
	 * Brave's {@code search_lang} value for an ISO 639-1 code.
	 *
	 * <p>
	 * Brave uses ISO 639-1 codes except for a few languages, which it names by a code of its own.
	 * </p>
	 */
	private static String searchLang(String language) {
		String code = language.strip().toLowerCase(Locale.ROOT);
		switch (code) {
			case "ja":
				return "jp";
			case "zh":
				return "zh-hans";
			case "pt":
				return "pt-pt";
			case "no":
				return "nb";
			default:
				return code;
		}
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
