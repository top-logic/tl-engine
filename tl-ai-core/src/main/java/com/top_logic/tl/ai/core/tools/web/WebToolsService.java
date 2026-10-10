/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.Collection;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Service holding configuration for the web tools ({@link WebSearchTool}, {@link WebFetchTool}):
 * the pluggable search engine, the fetch limits and the application's browsing governance.
 *
 * <p>
 * The {@link Config#getBrowsing() browsing policy} configured here is the application's answer to
 * "which origins may this user's agent reach", and it binds every route out of the application: the
 * browser tool, {@code webFetch} and {@code webSearch}. It sits <em>above</em> {@link UrlSafety},
 * which answers the different question of whether an address may be connected to at all and is
 * enforced independently, unconditionally and unconfigurably.
 * </p>
 *
 * @see BrowsingPolicyConfig
 * @see BrowsingPolicies
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
		 * Configuration property name for the browsing policy.
		 *
		 * @see #getBrowsing()
		 */
		String BROWSING = "browsing";

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
		 * Maximum number of bytes {@code webFetch} reads from a document before stopping.
		 *
		 * <p>
		 * Of an HTML page or a text, the part up to this size is read. A PDF that is larger is not
		 * read at all, because a PDF cannot be read without its end.
		 * </p>
		 *
		 * @see #FETCH_MAX_BYTES
		 */
		@Name(FETCH_MAX_BYTES)
		@IntDefault(WebFetchTool.DEFAULT_MAX_BYTES)
		int getFetchMaxBytes();

		/**
		 * Maximum number of characters of extracted text one {@code webFetch} answer shows.
		 *
		 * <p>
		 * A longer document is shown in parts of this size. Each answer tells the agent the offset at
		 * which the next part starts, and the agent reads on with a call that gives this offset. This
		 * value is the only cap on a {@code webFetch} answer: the agent's own limit on the size of a
		 * tool answer does not apply to it, so that the agent loop does not cut a part in the middle.
		 * </p>
		 *
		 * @see #FETCH_MAX_CHARS
		 */
		@Name(FETCH_MAX_CHARS)
		@IntDefault(WebFetchTool.DEFAULT_MAX_CHARS)
		int getFetchMaxChars();

		/**
		 * Which origins the agents of this application may browse, per security role.
		 *
		 * <p>
		 * Left empty - which is what ships - nothing changes: every origin that passes the SSRF
		 * check stays reachable. It is the ceiling for all three routes out of the application, so
		 * a domain list an agent passes to {@code webSearch} can only narrow it, never widen it.
		 * </p>
		 *
		 * @see #BROWSING
		 */
		@Name(BROWSING)
		@ItemDefault
		BrowsingPolicyConfig getBrowsing();
	}

	private static volatile WebToolsService _instance;

	private final InstantiationContext _context;

	private WebSearchEngine _searchEngine;

	private BrowsingPolicies _browsingPolicies = BrowsingPolicies.unrestricted();

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
		// Resolve the policy BEFORE the instance becomes visible: a half-started service whose
		// browsing policy is still the unrestricted placeholder would answer one tool call with the
		// opposite of what an administrator wrote.
		try {
			_browsingPolicies = BrowsingPolicies.of(getConfig().getBrowsing());
		} catch (IllegalArgumentException ex) {
			// A policy name nobody declared is an administrator's mistake in a security setting, so
			// it is reported to them as one rather than logged and worked around.
			throw new ConfigurationError(ResKey.text(ex.getMessage()), ex);
		}
		_instance = this;
		PolymorphicConfiguration<? extends WebSearchEngine> engineConfig = getConfig().getSearchEngine();
		_searchEngine = engineConfig == null ? null : _context.getInstance(engineConfig);
		if (_searchEngine == null) {
			Logger.info("No web search engine configured; webSearch will be unavailable until one is set.",
				WebToolsService.class);
		}
		Logger.info("Browsing governance: " + _browsingPolicies.describe() + ".", WebToolsService.class);
	}

	@Override
	protected void shutDown() {
		_instance = null;
		_searchEngine = null;
		_browsingPolicies = BrowsingPolicies.unrestricted();
		super.shutDown();
	}

	/**
	 * The configured browsing policies; never {@code null}.
	 */
	public BrowsingPolicies browsingPolicies() {
		return _browsingPolicies;
	}

	/**
	 * The browsing policy binding a run of the given account.
	 *
	 * <p>
	 * Static and null-safe on purpose: every caller is a tool or a browser session that must keep
	 * working when this service is not started, and "not started" means "nothing configured", which
	 * means unrestricted.
	 * </p>
	 *
	 * @param account
	 *        The acting account, may be {@code null}.
	 * @return The policy; never {@code null}.
	 */
	public static BrowsingPolicy policyFor(Person account) {
		WebToolsService service = getInstance();
		return service == null ? BrowsingPolicy.unrestricted() : service.browsingPolicies().forAccount(account);
	}

	/**
	 * The browsing policy binding a run whose account holds the given roles.
	 *
	 * @param roleNames
	 *        The roles held on the security root.
	 * @return The policy; never {@code null}.
	 */
	public static BrowsingPolicy policyForRoles(Collection<String> roleNames) {
		WebToolsService service = getInstance();
		return service == null ? BrowsingPolicy.unrestricted()
			: service.browsingPolicies().forRoles(roleNames);
	}

	/**
	 * The browsing policy binding the account of the running thread.
	 *
	 * @return The policy; never {@code null}.
	 */
	public static BrowsingPolicy currentPolicy() {
		return policyFor(BrowsingPolicies.currentAccountQuietly());
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
	 * Maximum number of characters of extracted text one fetch answer shows.
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
