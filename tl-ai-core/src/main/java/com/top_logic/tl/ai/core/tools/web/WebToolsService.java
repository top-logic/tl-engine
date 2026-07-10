/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service holding configuration for the web tools ({@link WebSearchTool}, {@link WebFetchTool}):
 * the pluggable search engine and the fetch limits.
 */
public class WebToolsService extends ConfiguredManagedClass<WebToolsService.Config<?>> {

	/**
	 * Configuration of {@link WebToolsService}.
	 */
	public interface Config<I extends WebToolsService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for the search engine.
		 *
		 * @see #getSearchEngine()
		 */
		String SEARCH_ENGINE = "search-engine";

		/**
		 * Configuration property name for the default maximum result count.
		 *
		 * @see #getMaxResults()
		 */
		String MAX_RESULTS = "max-results";

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
		 * The search engine backing {@code webSearch}. If unset, web search is unavailable.
		 *
		 * @see #SEARCH_ENGINE
		 */
		@Name(SEARCH_ENGINE)
		@Nullable
		PolymorphicConfiguration<? extends WebSearchEngine> getSearchEngine();

		/**
		 * Default maximum number of results returned by {@code webSearch}.
		 *
		 * @see #MAX_RESULTS
		 */
		@Name(MAX_RESULTS)
		@IntDefault(5)
		int getMaxResults();

		/**
		 * Per-request timeout for {@code webFetch}, in seconds.
		 *
		 * @see #FETCH_TIMEOUT_SECONDS
		 */
		@Name(FETCH_TIMEOUT_SECONDS)
		@IntDefault(30)
		int getFetchTimeoutSeconds();

		/**
		 * Maximum number of bytes {@code webFetch} reads from a page before stopping.
		 *
		 * @see #FETCH_MAX_BYTES
		 */
		@Name(FETCH_MAX_BYTES)
		@IntDefault(5_000_000)
		int getFetchMaxBytes();

		/**
		 * Maximum number of characters of extracted text {@code webFetch} returns.
		 *
		 * @see #FETCH_MAX_CHARS
		 */
		@Name(FETCH_MAX_CHARS)
		@IntDefault(50_000)
		int getFetchMaxChars();
	}

	private static volatile WebToolsService _instance;

	private final InstantiationContext _context;

	private WebSearchEngine _searchEngine;

	/**
	 * Creates a {@link WebToolsService} from configuration.
	 */
	public WebToolsService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
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
		PolymorphicConfiguration<? extends WebSearchEngine> engineConfig = getConfig().getSearchEngine();
		_searchEngine = engineConfig == null ? null : _context.getInstance(engineConfig);
		if (_searchEngine == null) {
			Logger.info("No web search engine configured; webSearch will be unavailable until one is set.",
				WebToolsService.class);
		}
	}

	@Override
	protected void shutDown() {
		_instance = null;
		_searchEngine = null;
		super.shutDown();
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
