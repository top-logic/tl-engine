/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function4;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProvider;
import com.top_logic.tl.ai.core.provider.mistral.MistralProvider;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;
import com.top_logic.tl.ai.core.provider.xai.XAIProvider;

/**
 * Registry of LLM provider connections.
 *
 * <p>
 * Manages provider configurations (API keys, base URLs, model lists) and creates
 * {@link LLMProvider} instances for given model names. Agent execution is handled by
 * {@link AgentFactory}.
 * </p>
 *
 * @see ProviderConfig
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
		 * Each provider entry specifies connection details (API key, base URL) and a
		 * comma-separated list of model names it serves. When building an agent, the registry
		 * matches the agent's model name against these lists to select the correct provider.
		 * </p>
		 */
		@Name(PROVIDERS)
		@EntryTag("provider")
		@Key(ProviderConfig.NAME_ATTRIBUTE)
		Map<String, ProviderConfig> getProviders();

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

	/**
	 * Configuration for a single LLM provider.
	 *
	 * <p>
	 * Each provider has a name (e.g. {@code "anthropic"}, {@code "openai"}, {@code "mistral"}),
	 * API credentials, and a comma-separated list of model names it serves.
	 * </p>
	 */
	public interface ProviderConfig extends NamedConfigMandatory {

		/** @see #getApiKey() */
		String API_KEY = "api-key";

		/** @see #getBaseUrl() */
		String BASE_URL = "base-url";

		/** @see #getModels() */
		String MODELS = "models";

		/** @see #getVersion() */
		String VERSION = "version";

		/**
		 * The API key for authentication with the provider.
		 */
		@Name(API_KEY)
		@Mandatory
		@Encrypted
		String getApiKey();

		/**
		 * The base URL for the provider's API.
		 *
		 * <p>
		 * Optional. When not set, the default URL for the provider type is used.
		 * </p>
		 */
		@Name(BASE_URL)
		@Nullable
		String getBaseUrl();

		/**
		 * Allowed model names for this provider.
		 *
		 * <p>
		 * If non-empty, only these models are exposed by this provider to the application.
		 * If empty, all models discovered from the provider's {@code /v1/models} endpoint are
		 * exposed.
		 * </p>
		 */
		@Name(MODELS)
		@Format(CommaSeparatedStrings.class)
		@Options(fun = ProviderModelOptions.class, args = {
			@Ref(NAME_ATTRIBUTE), @Ref(API_KEY), @Ref(BASE_URL), @Ref(VERSION)
		})
		List<String> getModels();

		/**
		 * Provider-specific API version string.
		 *
		 * <p>
		 * Used by Anthropic to set the {@code anthropic-version} header. Ignored by other
		 * providers.
		 * </p>
		 */
		@Name(VERSION)
		@Nullable
		String getVersion();
	}

	private static volatile LLMProviderRegistry _instance;

	/**
	 * Index mapping model names to their provider config name. Built at startup from the
	 * {@link ProviderConfig#getModels()} lists.
	 */
	private Map<String, String> _modelProviderIndex;
	private Map<String, List<String>> _providerModelOptions;

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
		ProviderModelDiscovery.clearCache();

		// Build model name index from provider configs
		ModelCatalog modelCatalog = buildModelCatalog(getConfig().getProviders());
		_modelProviderIndex = modelCatalog._modelProviderIndex;
		_providerModelOptions = modelCatalog._providerModelOptions;

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
			_providerModelOptions = null;
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
	 * Returns all discoverable model names for the given provider.
	 *
	 * <p>
	 * This list can be used by configuration UIs to present a provider-specific model selection.
	 * </p>
	 */
	public List<String> getProviderModelOptions(String providerName) {
		if (_providerModelOptions == null) {
			return List.of();
		}
		List<String> options = _providerModelOptions.get(providerName);
		return options != null ? options : List.of();
	}

	/**
	 * Builds an {@link LLMProvider} for the given model name.
	 *
	 * <p>
	 * Looks up the provider configuration whose {@link ProviderConfig#getModels() models} list
	 * contains the given model name, then creates the appropriate provider implementation.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to find a provider for.
	 * @return A ready-to-use provider.
	 * @throws IllegalStateException
	 *         if no provider is configured for the given model.
	 */
	public LLMProvider buildProvider(String modelName) {
		String providerName = _modelProviderIndex.get(modelName);
		if (providerName == null) {
			throw new IllegalStateException(
				"No provider configured for model: " + modelName
					+ ". Available models: " + _modelProviderIndex.keySet());
		}

		ProviderConfig providerConfig = getConfig().getProviders().get(providerName);
		return createProvider(providerName, providerConfig);
	}

	/**
	 * Builds effective model mappings and provider model option lists from configuration.
	 *
	 * <p>
	 * If {@link ProviderConfig#getModels()} is non-empty, it acts as an explicit allowlist.
	 * If empty, all discovered provider models are allowed.
	 * </p>
	 */
	private static ModelCatalog buildModelCatalog(Map<String, ProviderConfig> providers) {
		Map<String, String> index = new LinkedHashMap<>();
		Map<String, List<String>> providerModelOptions = new LinkedHashMap<>();

		for (Map.Entry<String, ProviderConfig> entry : providers.entrySet()) {
			String providerName = entry.getKey();
			ProviderModelDiscovery.DiscoveryResult discovery =
				ProviderModelDiscovery.discoverModels(providerName, entry.getValue());
			List<String> discovered = discovery.models();
			List<String> configured = normalizeModels(entry.getValue().getModels());

			Set<String> options = new LinkedHashSet<>(discovered);
			options.addAll(configured);
			providerModelOptions.put(providerName, List.copyOf(options));

			List<String> effectiveModels = configured.isEmpty() ? discovered : configured;
			for (String model : effectiveModels) {
				registerModel(index, model, providerName);
			}
			if (!discovered.isEmpty() && !discovery.fromCache()) {
				Logger.info("Discovered " + discovered.size() + " model(s) from provider '"
					+ providerName + "'.", LLMProviderRegistry.class);
			}
		}

		return new ModelCatalog(index, providerModelOptions);
	}

	private static List<String> normalizeModels(List<String> rawModels) {
		if (rawModels == null || rawModels.isEmpty()) {
			return List.of();
		}
		Set<String> normalized = new LinkedHashSet<>();
		for (String model : rawModels) {
			if (model != null && !model.isBlank()) {
				normalized.add(model.trim());
			}
		}
		return List.copyOf(normalized);
	}

	/**
	 * Options provider for {@link Config#getDefaultModel()}.
	 */
	public static class AllowedModelOptions extends Function1<List<String>, Map<String, ProviderConfig>> {
		@Override
		public List<String> apply(Map<String, ProviderConfig> providers) {
			if (providers == null || providers.isEmpty()) {
				return List.of();
			}
			return new ArrayList<>(buildModelCatalog(providers)._modelProviderIndex.keySet());
		}
	}

	/**
	 * Options provider for {@link ProviderConfig#getModels()}.
	 */
	public static class ProviderModelOptions extends Function4<List<String>, String, String, String, String> {
		@Override
		public List<String> apply(String providerName, String apiKey, String baseUrl, String version) {
			if (providerName == null || providerName.isBlank()) {
				return List.of();
			}
			return ProviderModelDiscovery.discoverModels(providerName, apiKey, baseUrl, version).models();
		}
	}

	private static void registerModel(Map<String, String> index, String modelName, String providerName) {
		String existingProvider = index.putIfAbsent(modelName, providerName);
		if (existingProvider != null && !existingProvider.equals(providerName)) {
			Logger.warn("Model '" + modelName + "' is exposed by multiple providers: '"
				+ existingProvider + "' and '" + providerName + "'. Keeping the first mapping.",
				LLMProviderRegistry.class);
		}
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

	private static final class ModelCatalog {
		final Map<String, String> _modelProviderIndex;
		final Map<String, List<String>> _providerModelOptions;

		ModelCatalog(Map<String, String> modelProviderIndex, Map<String, List<String>> providerModelOptions) {
			_modelProviderIndex = Collections.unmodifiableMap(new LinkedHashMap<>(modelProviderIndex));
			_providerModelOptions = Collections.unmodifiableMap(new LinkedHashMap<>(providerModelOptions));
		}
	}


	/**
	 * Creates an {@link LLMProvider} from the provider configuration.
	 *
	 * <p>
	 * The provider name determines which implementation to use: names containing "anthropic"
	 * produce an {@link AnthropicProvider}, "openai" produce an {@link OpenAIProvider}, and
	 * "mistral" produce a {@link MistralProvider}, and "xai" produce an
	 * {@link XAIProvider}.
	 * </p>
	 */
	private LLMProvider createProvider(String providerName, ProviderConfig config) {
		String name = providerName.toLowerCase(Locale.ROOT);
		String apiKey = config.getApiKey();

		if (name.contains("anthropic")) {
			String baseUrl = config.getBaseUrl();
			if (baseUrl != null) {
				// Strip trailing /v1 if present — the provider appends it internally
				if (baseUrl.endsWith("/v1")) {
					baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
				}
			}
			String version = config.getVersion();
			if (version == null) {
				version = "2023-06-01";
			}
			return new AnthropicProvider(apiKey,
				baseUrl != null ? baseUrl : "https://api.anthropic.com",
				version);
		}

		if (name.contains("openai")) {
			String baseUrl = config.getBaseUrl();
			if (baseUrl != null) {
				if (baseUrl.endsWith("/v1")) {
					baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
				}
			}
			return new OpenAIProvider(apiKey,
				baseUrl != null ? baseUrl : "https://api.openai.com");
		}

		if (name.contains("mistral")) {
			String baseUrl = config.getBaseUrl();
			if (baseUrl != null) {
				if (baseUrl.endsWith("/v1")) {
					baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
				}
			}
			return new MistralProvider(apiKey,
				baseUrl != null ? baseUrl : "https://api.mistral.ai");
		}

		if (name.contains("xai")) {
			String baseUrl = config.getBaseUrl();
			if (baseUrl != null) {
				if (baseUrl.endsWith("/v1")) {
					baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
				}
			}
			return new XAIProvider(apiKey,
				baseUrl != null ? baseUrl : "https://api.x.ai");
		}

		throw new IllegalStateException(
			"Unknown provider type: '" + providerName
				+ "'. Provider name must contain 'anthropic', 'openai', 'mistral', or 'xai'.");
	}

	/**
	 * Checks if an API key is valid (not null, not empty, not a placeholder).
	 */
	static boolean isValidApiKey(String apiKey) {
		if (apiKey == null || apiKey.trim().isEmpty()) {
			return false;
		}
		if (apiKey.startsWith("%") && apiKey.endsWith("%")) {
			return false;
		}
		return true;
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
