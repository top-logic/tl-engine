/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.AgentTuning;
import com.top_logic.tl.ai.core.provider.catalog.ModelCatalogEntry;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelsDevFetcher;
import com.top_logic.tl.ai.core.provider.health.FailoverConfig;
import com.top_logic.tl.ai.core.provider.health.ModelAvailability;
import com.top_logic.tl.ai.core.provider.health.ModelTarget;

/**
 * The LLM providers of this installation: which models exist and how to reach them, what is known
 * about each model, and where a run goes when its model cannot answer.
 *
 * <p>
 * The service configuration is organized into these sections:
 * </p>
 * <ol>
 * <li><b>Providers</b> - connection details for each LLM provider (API keys, base URLs, model
 * allowlists). Each provider entry defines how to reach a specific LLM API endpoint and which
 * models are enabled.</li>
 * <li><b>Failover</b> - where a run goes when the model it was configured with cannot answer.</li>
 * <li><b>Model Catalog</b> - read-only metadata describing each model's capabilities (tool
 * calling, reasoning, temperature support) and limits (context window, max output tokens).
 * Taken from the catalog bundled with the application at startup and from the
 * <a href="https://models.dev">models.dev</a> remote catalog in the background after it, with
 * optional manual overrides for local or unlisted models.</li>
 * </ol>
 *
 * <p>
 * Which agent runs on which of these models is configured on {@link AgentRuntimeService}. This
 * service was called {@code LLMService} before; a configuration that still names that service stops
 * the application from starting, with a message that says where each of its sections now belongs -
 * see {@link LLMService}.
 * </p>
 *
 * @see LLMProviderConfig
 * @see AgentFactory
 */
@Label("LLM providers")
public class LLMProviderService extends ConfiguredManagedClass<LLMProviderService.Config<?>> {

	/**
	 * Provides model names from the running {@link LLMProviderService} instance.
	 *
	 * <p>
	 * Unlike {@link ConfiguredModelOptions}, this does not require a {@link Container @Container}
	 * chain to resolve the provider map. This makes it suitable for contexts where the container
	 * reference is not yet set, such as during creation of a new {@link AgentRuntimeService.AgentOverride} entry in a
	 * keyed map.
	 * </p>
	 */
	public static class ServiceModelOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!LLMProviderService.Module.INSTANCE.isActive()) {
				return List.of();
			}
			return LLMProviderService.getInstance().getConfiguredModelNames();
		}
	}

	/**
	 * Provides provider names from the running {@link LLMProviderService} instance.
	 *
	 * <p>
	 * The {@link Container @Container}-free counterpart of {@link ConfiguredProviderOptions}, for
	 * contexts where the container reference is not yet set - such as creating a new
	 * {@link AgentRuntimeService.AgentOverride} entry in a keyed map.
	 * </p>
	 */
	public static class ServiceProviderOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!LLMProviderService.Module.INSTANCE.isActive()) {
				return List.of();
			}
			return LLMProviderService.getInstance().getConfiguredProviderNames();
		}
	}

	/**
	 * Configuration interface for {@link LLMProviderService}.
	 */
	@DisplayOrder({
		Config.PROVIDERS,
		Config.FAILOVER,
		Config.MODEL_CATALOG,
	})
	public interface Config<I extends LLMProviderService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for providers.
		 *
		 * @see #getProviders()
		 */
		String PROVIDERS = "providers";

		/**
		 * Configuration property name for the model catalog.
		 *
		 * @see #getModelCatalog()
		 */
		String MODEL_CATALOG = "model-catalog";

		/**
		 * Configuration property name for the failover section.
		 *
		 * @see #getFailover()
		 */
		String FAILOVER = "failover";

		/**
		 * The configured LLM providers, keyed by provider name.
		 *
		 * <p>
		 * Each entry carries provider-specific properties.
		 * </p>
		 */
		@Name(PROVIDERS)
		@EntryTag("provider")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, LLMProviderConfig.Config> getProviders();

		/**
		 * Model metadata catalog describing each model's capabilities and limits.
		 *
		 * <p>
		 * Populated automatically from the remote <a href="https://models.dev">models.dev</a>
		 * catalog on startup. Manual overrides can be added for models not listed in the remote
		 * catalog (e.g. local or custom-hosted models).
		 * </p>
		 *
		 * @see #MODEL_CATALOG
		 */
		@Name(MODEL_CATALOG)
		ModelCatalogConfig getModelCatalog();

		/**
		 * Where a run goes when the model it was configured with cannot answer, and how long this
		 * installation remembers that a model could not.
		 *
		 * <p>
		 * Empty by default, and an empty section changes nothing.
		 * </p>
		 *
		 * @see #FAILOVER
		 */
		@Name(FAILOVER)
		FailoverConfig getFailover();

	}

	/**
	 * Configuration for the model metadata catalog.
	 *
	 * <p>
	 * Provides read-only metadata about each model's capabilities (tool calling, reasoning,
	 * temperature support) and limits (context window, max output tokens) for all models
	 * configured in the provider section. The service starts with the copy of the
	 * <a href="https://models.dev">models.dev</a> catalog bundled with the application and loads
	 * the current catalog in the background after its start. Models not found in the catalog
	 * (e.g. locally hosted models) can be defined manually in the
	 * {@link #getModelOverrides() model overrides}.
	 * </p>
	 */
	public interface ModelCatalogConfig extends ConfigurationItem {

		/** Default catalog URL. */
		String DEFAULT_CATALOG_URL = "https://models.dev/api.json";

		/** @see #getCatalogUrl() */
		String CATALOG_URL = "catalog-url";

		/** @see #getFetchRemoteCatalog() */
		String FETCH_REMOTE_CATALOG = "fetch-remote-catalog";

		/** @see #getModelConfiguration() */
		String MODEL_CONFIGURATION = "llm-configuration";

		/** @see #getModelOverrides() */
		String MODEL_OVERRIDES = "model-overrides";

		/**
		 * The URL of the remote model catalog.
		 *
		 * @see #CATALOG_URL
		 */
		@Name(CATALOG_URL)
		@StringDefault(DEFAULT_CATALOG_URL)
		@ReadOnly
		String getCatalogUrl();

		/**
		 * Whether the current catalog is loaded from the catalog URL.
		 *
		 * <p>
		 * The service never waits for it: it starts with the catalog bundled with the application
		 * and loads the current one in the background. Switched off, no request is made at all and
		 * the bundled catalog is used alone - for an installation without access to the internet.
		 * </p>
		 *
		 * @see #FETCH_REMOTE_CATALOG
		 */
		@Name(FETCH_REMOTE_CATALOG)
		@BooleanDefault(true)
		boolean getFetchRemoteCatalog();

		/**
		 * Per-model metadata (limits, capabilities).
		 *
		 * <p>
		 * Populated automatically from the bundled catalog on startup and from the remote catalog
		 * when it has been loaded. Read-only - do not modify manually.
		 * </p>
		 *
		 * @see #MODEL_CONFIGURATION
		 */
		@Name(MODEL_CONFIGURATION)
		@Label("Model metadata")
		@ReadOnly
		@EntryTag("model")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ModelCatalogEntry.Config> getModelConfiguration();

		/**
		 * Manually defined model metadata for models not in the remote catalog.
		 *
		 * <p>
		 * Use this for local models or <code>OpenAI</code>-compatible endpoints whose models are not listed
		 * on models.dev. Entries here take precedence over catalog entries with the same name.
		 * </p>
		 *
		 * <p>
		 * An entry may also give a model its prices, in US dollars per million tokens. A model
		 * without a price costs nothing in a money budget.
		 * </p>
		 *
		 * @see #MODEL_OVERRIDES
		 */
		@Name(MODEL_OVERRIDES)
		@Label("Model overrides")
		@EntryTag("model")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ModelCatalogEntry.OverrideConfig> getModelOverrides();
	}

	/**
	 * The time the remote catalog may take to load, from name resolution to the last byte.
	 *
	 * <p>
	 * Nobody waits for it, so it only bounds how long a stalled server holds a connection. The
	 * catalog is about 6 MB, which a link of 1 Mbit/s still transfers within this time.
	 * </p>
	 */
	private static final Duration CATALOG_DEADLINE = Duration.ofSeconds(60);

	private static volatile LLMProviderService _instance;

	private final InstantiationContext _context;

	/**
	 * Index mapping each model name to the names of the providers exposing it, in configuration
	 * order.
	 *
	 * <p>
	 * A model exposed by two provider entries has both of them here - neither is dropped. Which of
	 * them serves a given agent is decided by that agent's {@link AgentRuntimeService.AgentOverride#getProvider()
	 * provider}, never by iteration order; an unqualified reference to such a model is refused at
	 * {@link #startUp()}.
	 * </p>
	 */
	private Map<String, List<String>> _modelProviders;

	/**
	 * Named provider configs, keyed by the provider config name.
	 */
	private Map<String, LLMProviderConfig> _providerConfigs;

	/**
	 * Index mapping model IDs to their resolved {@link ModelInfo} metadata.
	 */
	private volatile Map<String, ModelInfo> _models;

	/**
	 * The thread loading the remote catalog, or <code>null</code> when none is loading for this
	 * start. A loader that is no longer this thread installs nothing.
	 */
	private Thread _catalogLoader;

	/**
	 * What every model is currently known to be good for.
	 *
	 * <p>
	 * A component rather than a service of its own - see {@link ModelAvailability} for why. It is
	 * replaced on every start, so a restart of this service is also the blunt way to clear every
	 * mark.
	 * </p>
	 */
	private volatile ModelAvailability _health = new ModelAvailability(null, Clock.systemUTC());

	/**
	 * Creates a new {@link LLMProviderService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public LLMProviderService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	/**
	 * Returns the singleton instance of the {@link LLMProviderService}.
	 *
	 * @return The service instance, or {@code null} if not yet started.
	 */
	public static LLMProviderService getInstance() {
		return _instance;
	}

	/**
	 * What every model of this installation is currently known to be good for.
	 *
	 * <p>
	 * Never <code>null</code>, and usable before {@link #startUp()} has read the configuration: an
	 * installation that configured no lifetimes gets the shipped ones.
	 * </p>
	 *
	 * @see ModelAvailability
	 */
	public ModelAvailability modelHealth() {
		return _health;
	}

	/**
	 * The models a run configured with the given model reference may use, in order.
	 *
	 * <p>
	 * The first entry is always the reference itself, so a model with no configured chain yields a
	 * list of one and nothing about that run changes. The rest is the {@code <failover>} section's
	 * chain for that model name, whichever provider was asked to serve it.
	 * </p>
	 *
	 * @param model
	 *        The model the run was configured with; may be <code>null</code>, which yields an
	 *        empty list.
	 * @param provider
	 *        The provider qualifying it, or <code>null</code>.
	 * @return An immutable list, the configured reference first; never <code>null</code>.
	 */
	public List<ModelTarget> failoverChain(String model, String provider) {
		if (model == null || model.isBlank()) {
			return List.of();
		}
		ModelTarget configured = new ModelTarget(model, blankToNull(provider));
		FailoverConfig failover = getConfig().getFailover();
		FailoverConfig.Chain chain = failover == null ? null : failover.getChains().get(model);
		if (chain == null || chain.getFallbacks().isEmpty()) {
			return List.of(configured);
		}
		List<ModelTarget> result = new ArrayList<>();
		result.add(configured);
		for (FailoverConfig.Target fallback : chain.getFallbacks()) {
			ModelTarget target = new ModelTarget(fallback.getModel(), blankToNull(fallback.getProvider()));
			// A chain that names the model it is the way out of, or names one twice, would spend a
			// turn re-trying what just failed. Dropped here rather than refused at start-up: the
			// duplicate may be the same model served by a second provider, which is legitimate.
			if (result.stream().noneMatch(target::sameAs)) {
				result.add(target);
			}
		}
		return List.copyOf(result);
	}

	@Override
	protected void startUp() {
		super.startUp();
		// Before anything else: a configuration of the old service is not read by anybody, so an
		// application that still has one would run on whatever tl-ai ships - other providers, no keys,
		// other limits - without a word. The engine knows no alias for a renamed service.
		LLMService.refuseOldConfiguration(_context);
		_instance = this;
		ModelDiscoveryHelper.clearCache();

		Map<String, LLMProviderConfig> providerConfigs = new LinkedHashMap<>();
		Map<String, List<String>> modelProviders = new LinkedHashMap<>();

		for (Map.Entry<String, LLMProviderConfig.Config> entry : getConfig().getProviders().entrySet()) {
			String providerName = entry.getKey();
			LLMProviderConfig providerConfig = _context.getInstance(entry.getValue());
			providerConfigs.put(providerName, providerConfig);

			for (String model : providerConfig.getConfiguredModels()) {
				List<String> exposedBy = modelProviders.computeIfAbsent(model, name -> new ArrayList<>());
				if (!exposedBy.contains(providerName)) {
					exposedBy.add(providerName);
				}
			}
		}

		_providerConfigs = Map.copyOf(providerConfigs);
		Map<String, List<String>> index = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : modelProviders.entrySet()) {
			index.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		_modelProviders = Map.copyOf(index);

		refuseUnroutableReferences();

		// Built here rather than in the constructor so that a restart of this service - the blunt
		// way to clear every mark - also picks up changed lifetimes.
		_health = new ModelAvailability(getConfig().getFailover(), Clock.systemUTC());

		// --- Catalog phase ---
		ModelCatalogConfig catalogConfig = getConfig().getModelCatalog();
		Map<String, Set<String>> catalogRequested = buildCatalogProviderModels();

		Map<String, ModelInfo> bundled;
		if (catalogConfig == null || catalogRequested.isEmpty()) {
			bundled = Map.of();
		} else {
			bundled = ModelsDevFetcher.loadSnapshot(catalogRequested);
		}
		installCatalog(bundled);

		int overrideCount = catalogConfig == null ? 0 : catalogConfig.getModelOverrides().size();
		Logger.info("LLMProviderService started with " +
			getConfig().getProviders().size() + " provider(s), " +
			bundled.size() + " model(s) from the bundled catalog" +
			(overrideCount > 0 ? ", " + overrideCount + " model override(s)" : "") + ".",
			LLMProviderService.class);

		if (catalogConfig != null && !catalogRequested.isEmpty() && catalogConfig.getFetchRemoteCatalog()) {
			String url = catalogConfig.getCatalogUrl();
			Thread loader = new Thread(() -> loadRemoteCatalog(url, catalogRequested, bundled), "Model catalog");
			loader.setDaemon(true);
			synchronized (this) {
				_catalogLoader = loader;
			}
			loader.start();
		}
	}

	/**
	 * Loads the remote catalog and installs it over the bundled one, on the
	 * {@link #_catalogLoader} thread.
	 */
	private void loadRemoteCatalog(String url, Map<String, Set<String>> requested, Map<String, ModelInfo> bundled) {
		Map<String, ModelInfo> remote;
		try {
			remote = ModelsDevFetcher.fetch(url, requested, CATALOG_DEADLINE);
		} catch (InterruptedException ex) {
			// The service stopped.
			return;
		} catch (IOException ex) {
			Logger.warn("Could not load the model catalog from " + url + " (" + ex.getMessage()
				+ "); the bundled catalog stays in use.", LLMProviderService.class);
			return;
		}

		// A model the remote catalog does not list keeps what the bundled one knows about it.
		Map<String, ModelInfo> combined = new LinkedHashMap<>(bundled);
		combined.putAll(remote);
		synchronized (this) {
			if (_catalogLoader != Thread.currentThread()) {
				// Stopped or restarted meanwhile.
				return;
			}
			_catalogLoader = null;
			installCatalog(combined);
		}
		Logger.info("Loaded " + remote.size() + " model(s) from the model catalog at " + url + ".",
			LLMProviderService.class);
	}

	/**
	 * Makes the given catalog the one {@link #getModel(String)} answers from, with the manual
	 * overrides applied over it.
	 */
	private synchronized void installCatalog(Map<String, ModelInfo> catalog) {
		ModelCatalogConfig catalogConfig = getConfig().getModelCatalog();
		Map<String, ModelCatalogEntry.OverrideConfig> overrides;
		if (catalogConfig == null) {
			overrides = Map.of();
		} else {
			// Populate read-only config entries for admin UI visibility
			Map<String, ModelCatalogEntry.Config> catalogEntries = catalogConfig.getModelConfiguration();
			catalogEntries.clear();
			for (Map.Entry<String, ModelInfo> entry : catalog.entrySet()) {
				catalogEntries.put(entry.getKey(), ModelCatalogEntry.toConfig(entry.getKey(), entry.getValue()));
			}
			overrides = catalogConfig.getModelOverrides();
		}

		// Merge manual overrides (take precedence over catalog)
		Map<String, ModelInfo> merged = new LinkedHashMap<>(catalog);
		for (Map.Entry<String, ModelCatalogEntry.OverrideConfig> entry : overrides.entrySet()) {
			merged.put(entry.getKey(),
				ModelCatalogEntry.overrideToModelInfo(entry.getValue(), catalog.get(entry.getKey())));
		}

		_models = Map.copyOf(merged);
	}

	@Override
	protected void shutDown() {
		try {
			Thread loader;
			synchronized (this) {
				loader = _catalogLoader;
				_catalogLoader = null;
			}
			if (loader != null) {
				loader.interrupt();
			}
			_instance = null;
			_modelProviders = null;
			_providerConfigs = null;
			_models = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Refuses the start when a configured model reference cannot be routed to exactly one
	 * provider.
	 *
	 * <p>
	 * Reported to the {@link InstantiationContext} so that a full application boot lists every
	 * problem at once, and thrown so that this start fails whoever asked for it. The context alone
	 * is not enough: {@link TypedRuntimeModule} tests it for errors <em>before</em> this method
	 * runs, so at boot the refusal would only surface as the next service's failure, and an
	 * in-app restart of this service alone would report nothing and leave an unroutable
	 * configuration running.
	 * </p>
	 */
	private void refuseUnroutableReferences() {
		List<String> refusals = refusals(getConfig().getFailover(), _modelProviders, _providerConfigs.keySet());
		if (refusals.isEmpty()) {
			return;
		}
		for (String refusal : refusals) {
			_context.error(refusal);
		}
		throw new ConfigurationError(ResKey.text("The failover configuration cannot be routed to"
			+ " providers, so this service is not running: " + String.join(" ", refusals)));
	}

	/**
	 * Everything about the given failover configuration that must stop the service from starting.
	 *
	 * <p>
	 * Two things do: a reference to a provider that is not configured, and an unqualified reference
	 * to a model that two providers expose. The second is a refusal rather than a warning because
	 * there is no answer to give - the operator listed the model twice to keep the two endpoints
	 * apart, and picking either one by configuration order would be guessing which account, key or
	 * rate limit the run was meant to use.
	 * </p>
	 *
	 * <p>
	 * The model references of the agents - the default model and the agent overrides - are checked
	 * by {@link AgentRuntimeService} at its own start, through {@link #routingRefusal}.
	 * </p>
	 *
	 * @param failover
	 *        The failover section, whose fallbacks are model references; may be <code>null</code>.
	 * @param modelProviders
	 *        Model name to the names of the providers exposing it.
	 * @param providerNames
	 *        The names of all configured providers.
	 * @return One sentence per problem, in configuration order; empty for a routable
	 *         configuration.
	 */
	static List<String> refusals(FailoverConfig failover, Map<String, List<String>> modelProviders,
			Set<String> providerNames) {
		List<String> result = new ArrayList<>();
		// A fallback is a model reference like any other, and one that cannot be routed is worse
		// than a missing fallback: it is only ever reached on the day the primary model is already
		// down, which is the worst moment to find out.
		if (failover != null) {
			for (Map.Entry<String, FailoverConfig.Chain> entry : failover.getChains().entrySet()) {
				for (FailoverConfig.Target fallback : entry.getValue().getFallbacks()) {
					addRefusal(result, "The fallback for model '" + entry.getKey() + "'",
						new ModelRef(fallback.getModel(), blankToNull(fallback.getProvider())),
						modelProviders, providerNames);
				}
			}
		}
		return result;
	}

	/**
	 * Why the given model reference cannot be routed to exactly one provider of this service, or
	 * <code>null</code> when it can.
	 *
	 * <p>
	 * Refused are a model that no provider exposes, a provider that is not configured or does not
	 * expose the model, and an unqualified reference to a model that two providers expose.
	 * </p>
	 *
	 * @param subject
	 *        Who makes the reference, the beginning of the sentence, e.g. {@code "Agent 'X'"}.
	 * @param model
	 *        The model name; a blank one is not a reference and answers <code>null</code>.
	 * @param provider
	 *        The provider named for it, or <code>null</code>.
	 */
	public String routingRefusal(String subject, String model, String provider) {
		if (model == null || model.isBlank()) {
			return null;
		}
		Map<String, List<String>> modelProviders = _modelProviders == null ? Map.of() : _modelProviders;
		Set<String> providerNames = _providerConfigs == null ? Set.of() : _providerConfigs.keySet();
		if (provider == null || provider.isBlank()) {
			if (modelProviders.getOrDefault(model, List.of()).isEmpty()) {
				return subject + ": no provider exposes model '" + model + "'; the configured models are "
					+ modelProviders.keySet() + ".";
			}
		}
		List<String> refusals = new ArrayList<>();
		addRefusal(refusals, subject, new ModelRef(model, blankToNull(provider)), modelProviders, providerNames);
		return refusals.isEmpty() ? null : refusals.get(0);
	}

	private static void addRefusal(List<String> refusals, String subject, ModelRef ref,
			Map<String, List<String>> modelProviders, Set<String> providerNames) {
		String model = ref.model();
		if (model == null || model.isBlank()) {
			// No model to route; reported by the resolution itself when the agent runs.
			return;
		}
		List<String> exposedBy = modelProviders.getOrDefault(model, List.of());
		String provider = ref.provider();
		if (provider != null) {
			if (!providerNames.contains(provider)) {
				refusals.add(subject + ": the provider '" + provider + "' named for model '" + model
					+ "' is not configured; configured providers are " + providerNames + ".");
			} else if (!exposedBy.contains(provider)) {
				refusals.add(subject + ": the provider '" + provider + "' does not expose model '" + model
					+ "'; it exposes " + modelsOf(provider, modelProviders) + ".");
			}
		} else if (exposedBy.size() > 1) {
			refusals.add(subject + ": model '" + model + "' is exposed by " + exposedBy
				+ ", so one of them must be named as the provider.");
		}
	}

	/**
	 * The models the given provider exposes, for a refusal message.
	 */
	private static List<String> modelsOf(String providerName, Map<String, List<String>> modelProviders) {
		List<String> result = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : modelProviders.entrySet()) {
			if (entry.getValue().contains(providerName)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Returns only the explicitly configured model names from all providers.
	 *
	 * <p>
	 * Returns only models that an admin has explicitly added to a provider's {@code models}
	 * allowlist. Models that were merely discovered via provider API are excluded.
	 * </p>
	 *
	 * @return An immutable list of explicitly configured model names.
	 */
	public List<String> getConfiguredModelNames() {
		return modelNamesOf(getConfig().getProviders());
	}

	/**
	 * Collects the explicitly configured model names from the given provider map.
	 *
	 * @param providers
	 *        The provider configurations keyed by name, or {@code null}.
	 * @return An immutable list of the model names listed in each provider's
	 *         {@link LLMProviderConfig.Config#getModels() allowlist}, de-duplicated in iteration
	 *         order.
	 */
	static List<String> modelNamesOf(Map<String, LLMProviderConfig.Config> providers) {
		if (providers == null) {
			return List.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (LLMProviderConfig.Config config : providers.values()) {
			List<String> models = config.getModels();
			if (models != null) {
				result.addAll(models);
			}
		}
		return List.copyOf(result);
	}

	/**
	 * The names of all configured providers, in configuration order.
	 */
	public List<String> getConfiguredProviderNames() {
		return List.copyOf(getConfig().getProviders().keySet());
	}

	/**
	 * The names of the providers exposing the given model, in configuration order.
	 *
	 * <p>
	 * More than one entry means the model must be referenced with a provider - see
	 * {@link AgentRuntimeService.AgentOverride#getProvider()}.
	 * </p>
	 *
	 * @param modelName
	 *        The model to look up.
	 * @return The provider names; empty if no configured provider exposes the model.
	 */
	public List<String> getProvidersOf(String modelName) {
		if (_modelProviders == null) {
			return List.of();
		}
		return _modelProviders.getOrDefault(modelName, List.of());
	}

	/**
	 * Builds an {@link LLMProvider} for an unqualified model reference.
	 *
	 * @see #buildProvider(String, String)
	 */
	public LLMProvider buildProvider(String modelName) {
		return buildProvider(modelName, null);
	}

	/**
	 * Builds an {@link LLMProvider} for the given model, served by the named provider.
	 *
	 * @param modelName
	 *        The model name to find a provider for.
	 * @param providerName
	 *        The configured provider that must serve the model, or {@code null} to let the
	 *        model's only provider serve it.
	 * @return A ready-to-use provider.
	 * @throws IllegalStateException
	 *         if the model cannot be routed to exactly one configured provider.
	 */
	public LLMProvider buildProvider(String modelName, String providerName) {
		LLMProviderConfig providerConfig = lookupProvider(modelName, providerName);
		if (providerConfig == null) {
			throw new IllegalStateException(unroutable(modelName, providerName));
		}
		return providerConfig.createProvider();
	}

	/**
	 * The provider configuration serving the given model reference, or {@code null} if the
	 * reference does not resolve to exactly one provider.
	 *
	 * @param modelName
	 *        The model name.
	 * @param providerName
	 *        The provider qualifying the model, or {@code null} for an unqualified reference.
	 */
	private LLMProviderConfig lookupProvider(String modelName, String providerName) {
		if (_providerConfigs == null || modelName == null) {
			return null;
		}
		List<String> exposedBy = getProvidersOf(modelName);
		if (providerName != null && !providerName.isBlank()) {
			// An allowlist that is bypassed is not an allowlist: a provider only serves the models
			// it exposes, however it was named.
			return exposedBy.contains(providerName) ? _providerConfigs.get(providerName) : null;
		}
		if (exposedBy.size() != 1) {
			return null;
		}
		return _providerConfigs.get(exposedBy.get(0));
	}

	/**
	 * Why the given model reference does not resolve to exactly one provider.
	 */
	private String unroutable(String modelName, String providerName) {
		List<String> exposedBy = getProvidersOf(modelName);
		if (providerName != null && !providerName.isBlank()) {
			if (_providerConfigs == null || !_providerConfigs.containsKey(providerName)) {
				return "No provider named '" + providerName + "' is configured for model '" + modelName
					+ "'. Configured providers: " + getConfiguredProviderNames();
			}
			return "Provider '" + providerName + "' does not expose model '" + modelName
				+ "'. It exposes: " + modelsOf(providerName, _modelProviders) + ".";
		}
		if (exposedBy.isEmpty()) {
			return "No provider configured for model: " + modelName
				+ ". Available models: " + (_modelProviders == null ? List.of() : _modelProviders.keySet());
		}
		return "Model '" + modelName + "' is exposed by the providers " + exposedBy
			+ ". Name one of them to select the provider.";
	}

	/**
	 * Returns metadata for the given model, or {@code null} if unknown.
	 */
	public ModelInfo getModel(String modelId) {
		Map<String, ModelInfo> models = _models;
		if (models == null) {
			return null;
		}
		return models.get(modelId);
	}

	/**
	 * Returns the catalog provider ID for an unqualified model reference, or {@code null} if it
	 * cannot be determined.
	 *
	 * <p>
	 * Two provider entries for the same vendor - the case this exists for, two Anthropic accounts
	 * exposing the same model - agree on their
	 * {@link LLMProviderConfig#getCatalogProviderId() catalog provider ID}, so the ID is
	 * unambiguous even where the provider <em>instance</em> is not. Only providers that disagree
	 * make the answer {@code null}.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to look up.
	 * @return The catalog provider ID (e.g. "anthropic", "openai"), or {@code null}.
	 */
	public String getProviderId(String modelName) {
		if (_providerConfigs == null) {
			return null;
		}
		String result = null;
		boolean first = true;
		for (String providerName : getProvidersOf(modelName)) {
			String catalogId = _providerConfigs.get(providerName).getCatalogProviderId();
			if (first) {
				result = catalogId;
				first = false;
			} else if (!Objects.equals(result, catalogId)) {
				return null;
			}
		}
		return result;
	}

	/**
	 * Returns the catalog provider ID of the given model reference, or {@code null} if the
	 * reference does not resolve to exactly one provider.
	 *
	 * @param modelName
	 *        The model name to look up.
	 * @param providerName
	 *        The provider qualifying the model, or {@code null} for an unqualified reference.
	 * @return The catalog provider ID (e.g. "anthropic", "openai"), or {@code null}.
	 */
	public String getProviderId(String modelName, String providerName) {
		if (providerName == null || providerName.isBlank()) {
			return getProviderId(modelName);
		}
		LLMProviderConfig providerConfig = lookupProvider(modelName, providerName);
		return providerConfig == null ? null : providerConfig.getCatalogProviderId();
	}

	/**
	 * How long, in seconds, a run on its last model waits in total for a model that is rate limited.
	 *
	 * @see FailoverConfig#getRateLimitWaitSeconds()
	 */
	public int rateLimitWaitSeconds() {
		FailoverConfig failover = getConfig().getFailover();
		return failover == null ? AgentTuning.DEFAULT_RATE_LIMIT_WAIT_SECONDS : failover.getRateLimitWaitSeconds();
	}

	/**
	 * A model together with the provider that serves it.
	 *
	 * @param model
	 *        The model name, or {@code null} if none is configured.
	 * @param provider
	 *        The name of the provider serving the model, or {@code null} for an unqualified
	 *        reference that the model's only provider serves.
	 */
	record ModelRef(String model, String provider) {
		// A pair, nothing more.
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	/**
	 * Derives the catalog-provider-ID to model-ID mapping from configured providers.
	 */
	private Map<String, Set<String>> buildCatalogProviderModels() {
		Map<String, Set<String>> result = new LinkedHashMap<>();
		for (LLMProviderConfig providerConfig : _providerConfigs.values()) {
			String catalogId = providerConfig.getCatalogProviderId();
			if (catalogId == null || catalogId.isBlank()) {
				continue;
			}
			// Every provider contributes its own allowlist: a model two providers expose is
			// requested once per catalog ID, and neither provider's models go unrequested.
			result.computeIfAbsent(catalogId, id -> new LinkedHashSet<>())
				.addAll(providerConfig.getConfiguredModels());
		}
		return result;
	}

	/**
	 * Options provider that collects all model names configured across all provider entries.
	 *
	 * <p>
	 * Used by UI components to display available models for selection.
	 * </p>
	 */
	public static class AllowedModelOptions extends Function1<List<String>, Map<String, LLMProviderConfig.Config>> {
		@Override
		public List<String> apply(Map<String, LLMProviderConfig.Config> providers) {
			if (providers == null || providers.isEmpty()) {
				return List.of();
			}
			Set<String> allModels = new LinkedHashSet<>();
			for (LLMProviderConfig.Config config : providers.values()) {
				List<String> models = config.getModels();
				if (models != null) {
					allModels.addAll(models);
				}
			}
			return new ArrayList<>(allModels);
		}
	}

	/**
	 * Module for {@link LLMProviderService}.
	 */
	public static final class Module extends TypedRuntimeModule<LLMProviderService> {

		/**
		 * Singleton {@link LLMProviderService.Module} instance.
		 */
		public static final LLMProviderService.Module INSTANCE = new LLMProviderService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<LLMProviderService> getImplementation() {
			return LLMProviderService.class;
		}
	}
}
