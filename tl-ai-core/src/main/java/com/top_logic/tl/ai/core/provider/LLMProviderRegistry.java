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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.agent.AgentFactory;

/**
 * Registry of LLM provider connections.
 *
 * <p>
 * Manages provider configurations and creates {@link LLMProvider} instances for given model names.
 * Each provider is a polymorphic {@link LLMProviderConfig} that knows how to create its own
 * provider instance and discover available models.
 * </p>
 *
 * @see LLMProviderConfig
 * @see AgentFactory
 */
public class LLMProviderRegistry extends ConfiguredManagedClass<LLMProviderRegistry.Config<?>> {

	/**
	 * Configuration interface for {@link LLMProviderRegistry}.
	 */
	public interface Config<I extends LLMProviderRegistry> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for providers.
		 *
		 * @see #getProviders()
		 */
		String PROVIDERS = "providers";

		/**
		 * Configuration property name for default model.
		 *
		 * @see #getDefaultModel()
		 */
		String DEFAULT_MODEL = "default-model";

		/**
		 * The configured LLM providers, keyed by provider name.
		 *
		 * <p>
		 * Each provider entry is a polymorphic {@link LLMProviderConfig.Config} that carries
		 * provider-specific properties (API key, base URL, etc.) and knows how to create the
		 * appropriate {@link LLMProvider} implementation.
		 * </p>
		 */
		@Name(PROVIDERS)
		@EntryTag("provider")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, LLMProviderConfig.Config> getProviders();

		/**
		 * The default model name to use when no model is explicitly specified on an agent.
		 *
		 * <p>
		 * Must match one of the effective model names exposed by the configured providers.
		 * </p>
		 */
		@Name(DEFAULT_MODEL)
		@Nullable
		@Options(fun = AllowedModelOptions.class, args = @Ref(PROVIDERS))
		String getDefaultModel();
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
			List<String> discovered = providerConfig.discoverModels();

			List<String> effectiveModels = configured.isEmpty() ? discovered : configured;
			for (String model : effectiveModels) {
				LLMProviderConfig existing = modelIndex.putIfAbsent(model, providerConfig);
				if (existing != null && existing != providerConfig) {
					Logger.warn("Model '" + model + "' is exposed by multiple providers. Keeping the first mapping.",
						LLMProviderRegistry.class);
				}
			}

			if (!discovered.isEmpty()) {
				Logger.info("Discovered " + discovered.size() + " model(s) from provider '"
					+ providerName + "'.", LLMProviderRegistry.class);
			}
		}

		_providerConfigs = Map.copyOf(providerConfigs);
		_modelProviderIndex = Map.copyOf(modelIndex);

		validateDefaultModel(getConfig().getDefaultModel());

		Logger.info("LLMProviderRegistry started with " +
			getConfig().getProviders().size() + " provider(s). " +
			"Agent and skill definitions are resolved from the knowledge base at runtime.",
			LLMProviderRegistry.class);
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			_modelProviderIndex = null;
			_providerConfigs = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Returns the names of all models listed across all configured providers.
	 *
	 * @return An immutable list of all model names.
	 */
	public List<String> getModelNames() {
		if (_modelProviderIndex == null) {
			return List.of();
		}
		return List.copyOf(_modelProviderIndex.keySet());
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

	private void validateDefaultModel(String defaultModel) {
		if (defaultModel == null || defaultModel.isBlank()) {
			return;
		}
		if (_modelProviderIndex.containsKey(defaultModel)) {
			return;
		}
		throw new RuntimeException("Invalid default model '" + defaultModel
			+ "'. The model must be one of the allowed provider models. Available models: "
			+ _modelProviderIndex.keySet());
	}

	/**
	 * Options provider for {@link Config#getDefaultModel()}.
	 *
	 * <p>
	 * Collects all model names configured across all provider entries.
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
