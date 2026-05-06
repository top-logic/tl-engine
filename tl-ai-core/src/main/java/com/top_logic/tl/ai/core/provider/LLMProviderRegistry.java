/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.provider.catalog.ModelCatalogEntry;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelsDevFetcher;

/**
 * Registry of LLM provider connections.
 *
 * <p>
 * Manages provider configurations and creates {@link LLMProvider} instances for given model names.
 * </p>
 *
 * @see LLMProviderConfig
 * @see AgentFactory
 */
public class LLMProviderRegistry extends ConfiguredManagedClass<LLMProviderRegistry.Config<?>> {

	/**
	 * Configuration interface for {@link LLMProviderRegistry}.
	 */
	@DisplayOrder({
		Config.PROVIDERS,
		Config.DEFAULT_MODEL,
		Config.MODEL_CATALOG,
	})
	public interface Config<I extends LLMProviderRegistry> extends ConfiguredManagedClass.Config<I> {

		// --- Provider configuration ---

		/**
		 * Configuration property name for providers.
		 *
		 * @see #getProviders()
		 */
		String PROVIDERS = "providers";

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

		// --- Model catalog configuration ---

		/** Configuration property name for {@link #getDefaultModel()}. */
		String DEFAULT_MODEL = "default-model";

		/** Configuration property name for {@link #getModelCatalog()}. */
		String MODEL_CATALOG = "model-catalog";

		/**
		 * The default model name used when no model is explicitly specified on an agent.
		 *
		 * @see #DEFAULT_MODEL
		 */
		@Name(DEFAULT_MODEL)
		@Nullable
		@Options(fun = AllowedModelOptions.class, args = @Ref(PROVIDERS))
		String getDefaultModel();

		/**
		 * Model catalog configuration: remote catalog URL and per-model metadata overrides.
		 *
		 * @see #MODEL_CATALOG
		 */
		@Name(MODEL_CATALOG)
		ModelCatalogConfig getModelCatalog();

	}

	/**
	 * Configuration for the model metadata catalog.
	 */
	public interface ModelCatalogConfig extends ConfigurationItem {

		/** Default catalog URL. */
		String DEFAULT_CATALOG_URL = "https://models.dev/api.json";

		/** @see #getCatalogUrl() */
		String CATALOG_URL = "catalog-url";

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
		String getCatalogUrl();

		/**
		 * Per-model metadata (limits, capabilities).
		 *
		 * <p>
		 * Populated automatically from the remote catalog on startup.
		 * </p>
		 *
		 * @see #MODEL_CONFIGURATION
		 */
		@Name(MODEL_CONFIGURATION)
		@Label("Model metadata")
		@EntryTag("model")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ModelCatalogEntry.Config> getModelConfiguration();

		/**
		 * Manually defined model metadata for models not in the remote catalog.
		 *
		 * <p>
		 * Use this for local models or OpenAI-compatible endpoints whose models are not listed
		 * on models.dev. Entries here take precedence over catalog entries with the same name.
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

	private static volatile LLMProviderRegistry _instance;

	private final InstantiationContext _context;

	/**
	 * Index mapping model names to their resolved {@link LLMProviderConfig} instance.
	 */
	private Map<String, LLMProviderConfig> _modelProviderIndex;

	/**
	 * Named provider configs, keyed by the provider config name.
	 */
	private Map<String, LLMProviderConfig> _providerConfigs;

	/**
	 * Index mapping model IDs to their resolved {@link ModelInfo} metadata.
	 */
	private Map<String, ModelInfo> _models;

	/**
	 * Creates a new {@link LLMProviderRegistry} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public LLMProviderRegistry(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	/**
	 * Returns the singleton instance of the {@link LLMProviderRegistry}.
	 *
	 * @return The service instance, or {@code null} if not yet started.
	 */
	public static LLMProviderRegistry getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
		ModelDiscoveryHelper.clearCache();

		Map<String, LLMProviderConfig> providerConfigs = new LinkedHashMap<>();
		Map<String, LLMProviderConfig> modelIndex = new LinkedHashMap<>();

		for (Map.Entry<String, LLMProviderConfig.Config> entry : getConfig().getProviders().entrySet()) {
			String providerName = entry.getKey();
			LLMProviderConfig providerConfig = _context.getInstance(entry.getValue());
			providerConfigs.put(providerName, providerConfig);

			List<String> configured = providerConfig.getConfiguredModels();
			for (String model : configured) {
				LLMProviderConfig existing = modelIndex.putIfAbsent(model, providerConfig);
				if (existing != null && existing != providerConfig) {
					Logger.warn("Model '" + model + "' is exposed by multiple providers. Keeping the first mapping.",
						LLMProviderRegistry.class);
				}
			}
		}

		_providerConfigs = Map.copyOf(providerConfigs);
		_modelProviderIndex = Map.copyOf(modelIndex);

		// --- Catalog phase ---
		Map<String, Set<String>> catalogRequested = buildCatalogProviderModels();

		Map<String, ModelInfo> fetched;
		if (catalogRequested.isEmpty()) {
			fetched = Map.of();
		} else {
			ModelCatalogConfig catalogConfig = getConfig().getModelCatalog();
			fetched = ModelsDevFetcher.fetch(catalogConfig.getCatalogUrl(), catalogRequested);
		}

		// Populate read-only config entries for admin UI visibility
		ModelCatalogConfig catalogConfig = getConfig().getModelCatalog();
		Map<String, ModelCatalogEntry.Config> catalogEntries = catalogConfig.getModelConfiguration();
		catalogEntries.clear();
		for (Map.Entry<String, ModelInfo> entry : fetched.entrySet()) {
			catalogEntries.put(entry.getKey(), ModelCatalogEntry.toConfig(entry.getKey(), entry.getValue()));
		}

		// Merge manual overrides (take precedence over catalog)
		Map<String, ModelInfo> merged = new LinkedHashMap<>(fetched);
		Map<String, ModelCatalogEntry.OverrideConfig> overrides = catalogConfig.getModelOverrides();
		for (Map.Entry<String, ModelCatalogEntry.OverrideConfig> entry : overrides.entrySet()) {
			merged.put(entry.getKey(), ModelCatalogEntry.overrideToModelInfo(entry.getValue()));
		}

		_models = Map.copyOf(merged);

		int overrideCount = overrides.size();
		Logger.info("LLMProviderRegistry started with " +
			getConfig().getProviders().size() + " provider(s), " +
			fetched.size() + " model(s) from catalog" +
			(overrideCount > 0 ? ", " + overrideCount + " model override(s)" : "") + ".",
			LLMProviderRegistry.class);
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			_modelProviderIndex = null;
			_providerConfigs = null;
			_models = null;
		} finally {
			super.shutDown();
		}
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
		Set<String> result = new LinkedHashSet<>();
		for (LLMProviderConfig.Config config : getConfig().getProviders().values()) {
			List<String> models = config.getModels();
			if (models != null) {
				result.addAll(models);
			}
		}
		return List.copyOf(result);
	}

	/**
	 * Builds an {@link LLMProvider} for the given model name.
	 *
	 * <p>
	 * Looks up the {@link LLMProviderConfig} whose model list contains the given model name,
	 * then delegates to {@link LLMProviderConfig#createProvider()}.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to find a provider for.
	 * @return A ready-to-use provider.
	 * @throws IllegalStateException
	 *         if no provider is configured for the given model.
	 */
	public LLMProvider buildProvider(String modelName) {
		LLMProviderConfig providerConfig = _modelProviderIndex.get(modelName);
		if (providerConfig == null) {
			throw new IllegalStateException(
				"No provider configured for model: " + modelName
					+ ". Available models: " + _modelProviderIndex.keySet());
		}
		return providerConfig.createProvider();
	}

	/**
	 * Returns metadata for the given model, or {@code null} if unknown.
	 */
	public ModelInfo getModel(String modelId) {
		if (_models == null) {
			return null;
		}
		return _models.get(modelId);
	}

	/**
	 * Returns the catalog provider ID for the given model name, or {@code null} if unknown.
	 *
	 * <p>
	 * Looks up which configured provider owns the given model and returns that provider's
	 * {@link LLMProviderConfig#getCatalogProviderId() catalog provider ID}.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to look up.
	 * @return The catalog provider ID (e.g. "anthropic", "openai"), or {@code null}.
	 */
	public String getProviderId(String modelName) {
		if (_modelProviderIndex == null) {
			return null;
		}
		LLMProviderConfig providerConfig = _modelProviderIndex.get(modelName);
		if (providerConfig == null) {
			return null;
		}
		return providerConfig.getCatalogProviderId();
	}

	/**
	 * Returns the configured default model name, or {@code null} if not set.
	 */
	public String getDefaultModel() {
		return getConfig().getDefaultModel();
	}

	/**
	 * Derives the catalog-provider-ID to model-ID mapping from configured providers.
	 */
	private Map<String, Set<String>> buildCatalogProviderModels() {
		Map<String, Set<String>> result = new LinkedHashMap<>();
		for (Map.Entry<String, LLMProviderConfig> entry : _providerConfigs.entrySet()) {
			LLMProviderConfig providerConfig = entry.getValue();
			String catalogId = providerConfig.getCatalogProviderId();
			if (catalogId == null || catalogId.isBlank()) {
				continue;
			}
			Set<String> models = result.computeIfAbsent(catalogId, k -> new LinkedHashSet<>());
			for (Map.Entry<String, LLMProviderConfig> modelEntry : _modelProviderIndex.entrySet()) {
				if (modelEntry.getValue() == providerConfig) {
					models.add(modelEntry.getKey());
				}
			}
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
	 * Module for {@link LLMProviderRegistry}.
	 */
	public static final class Module extends TypedRuntimeModule<LLMProviderRegistry> {

		/**
		 * Singleton {@link LLMProviderRegistry.Module} instance.
		 */
		public static final LLMProviderRegistry.Module INSTANCE = new LLMProviderRegistry.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<LLMProviderRegistry> getImplementation() {
			return LLMProviderRegistry.class;
		}
	}
}
