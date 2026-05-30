/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service holding configuration for the web tools ({@link WebSearchTool}, {@link WebFetchTool}):
 * the search API key, the selected search provider, and fetch limits.
 */
public class WebToolsService extends ConfiguredManagedClass<WebToolsService.Config<?>> {

	/** Environment variable / system property consulted when no API key is configured. */
	public static final String API_KEY_ENV = "tl_websearch_api_key";

	/**
	 * Configuration of {@link WebToolsService}.
	 */
	public interface Config<I extends WebToolsService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for the search provider.
		 *
		 * @see #getProvider()
		 */
		String PROVIDER = "provider";

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
		 * Configuration property name for the default maximum result count.
		 *
		 * @see #getMaxResults()
		 */
		String MAX_RESULTS = "max-results";

		/**
		 * Configuration property name for whether to request a synthesized answer.
		 *
		 * @see #getIncludeAnswer()
		 */
		String INCLUDE_ANSWER = "include-answer";

		/**
		 * Configuration property name for the fetch timeout (seconds).
		 *
		 * @see #getFetchTimeoutSeconds()
		 */
		String FETCH_TIMEOUT_SECONDS = "fetch-timeout-seconds";

		/**
		 * Configuration property name for the fetch byte cap.
		 *
		 * @see #getFetchMaxBytes()
		 */
		String FETCH_MAX_BYTES = "fetch-max-bytes";

		/**
		 * Configuration property name for the fetch character cap.
		 *
		 * @see #getFetchMaxChars()
		 */
		String FETCH_MAX_CHARS = "fetch-max-chars";

		/**
		 * The search provider id. Currently only {@code tavily} is supported.
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@StringDefault("tavily")
		String getProvider();

		/**
		 * The search API key. If blank, the {@value #API_KEY_ENV} environment variable (then system
		 * property) is consulted as a fallback.
		 *
		 * @see #API_KEY
		 */
		@Name(API_KEY)
		@Encrypted
		@Nullable
		String getApiKey();

		/**
		 * The search API base URL.
		 *
		 * @see #BASE_URL
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.tavily.com")
		String getBaseUrl();

		/**
		 * Search depth: {@code basic} (faster) or {@code advanced} (more thorough).
		 *
		 * @see #SEARCH_DEPTH
		 */
		@Name(SEARCH_DEPTH)
		@StringDefault("basic")
		String getSearchDepth();

		/**
		 * Default maximum number of results returned by {@code web_search}.
		 *
		 * @see #MAX_RESULTS
		 */
		@Name(MAX_RESULTS)
		@IntDefault(5)
		int getMaxResults();

		/**
		 * Whether {@code web_search} requests a synthesized answer from the provider.
		 *
		 * @see #INCLUDE_ANSWER
		 */
		@Name(INCLUDE_ANSWER)
		@BooleanDefault(true)
		boolean getIncludeAnswer();

		/**
		 * Per-request timeout for {@code web_fetch}, in seconds.
		 *
		 * @see #FETCH_TIMEOUT_SECONDS
		 */
		@Name(FETCH_TIMEOUT_SECONDS)
		@IntDefault(30)
		int getFetchTimeoutSeconds();

		/**
		 * Maximum number of bytes {@code web_fetch} reads from a page before stopping.
		 *
		 * @see #FETCH_MAX_BYTES
		 */
		@Name(FETCH_MAX_BYTES)
		@IntDefault(5_000_000)
		int getFetchMaxBytes();

		/**
		 * Maximum number of characters of extracted text {@code web_fetch} returns.
		 *
		 * @see #FETCH_MAX_CHARS
		 */
		@Name(FETCH_MAX_CHARS)
		@IntDefault(50_000)
		int getFetchMaxChars();
	}

	private static volatile WebToolsService _instance;

	private WebSearchEngine _searchEngine;

	/**
	 * Creates a {@link WebToolsService} from configuration.
	 */
	public WebToolsService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance, or {@code null} if the service is not started.
	 */
	public static WebToolsService getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
		_searchEngine = createSearchEngine(getConfig());
	}

	@Override
	protected void shutDown() {
		_instance = null;
		_searchEngine = null;
		super.shutDown();
	}

	private static WebSearchEngine createSearchEngine(Config<?> config) {
		String apiKey = resolveApiKey(config);
		if (apiKey == null) {
			Logger.info("Web search API key not configured; web_search will be unavailable until set.",
				WebToolsService.class);
			return null;
		}
		String provider = config.getProvider() == null ? "" : config.getProvider().trim().toLowerCase();
		switch (provider) {
			case "tavily":
				return new TavilySearchEngine(apiKey, config.getBaseUrl(), config.getSearchDepth(),
					config.getIncludeAnswer(), false);
			default:
				Logger.error("Unknown web search provider '" + config.getProvider() + "'; web_search disabled.",
					WebToolsService.class);
				return null;
		}
	}

	private static String resolveApiKey(Config<?> config) {
		String key = config.getApiKey();
		if (key != null && !key.isBlank()) {
			return key;
		}
		key = System.getenv(API_KEY_ENV);
		if (key != null && !key.isBlank()) {
			return key;
		}
		key = System.getProperty(API_KEY_ENV);
		return (key != null && !key.isBlank()) ? key : null;
	}

	/**
	 * The configured search engine, or {@code null} if web search is not configured.
	 */
	public WebSearchEngine getSearchEngine() {
		return _searchEngine;
	}

	/**
	 * Default maximum number of search results.
	 */
	public int getMaxResults() {
		return getConfig().getMaxResults();
	}

	/**
	 * Per-request fetch timeout in seconds.
	 */
	public int getFetchTimeoutSeconds() {
		return getConfig().getFetchTimeoutSeconds();
	}

	/**
	 * Maximum number of bytes read per fetch.
	 */
	public int getFetchMaxBytes() {
		return getConfig().getFetchMaxBytes();
	}

	/**
	 * Maximum number of characters of extracted text returned per fetch.
	 */
	public int getFetchMaxChars() {
		return getConfig().getFetchMaxChars();
	}

	/**
	 * {@link TypedRuntimeModule} for {@link WebToolsService}.
	 */
	public static final class Module extends TypedRuntimeModule<WebToolsService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<WebToolsService> getImplementation() {
			return WebToolsService.class;
		}
	}
}
