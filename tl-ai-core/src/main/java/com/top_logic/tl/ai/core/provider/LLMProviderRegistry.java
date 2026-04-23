/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProvider;
import com.top_logic.tl.ai.core.provider.mistral.MistralProvider;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

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
		 * This should match a model listed in one of the {@link #getProviders() providers}.
		 * </p>
		 */
		@Name(DEFAULT_MODEL)
		@Nullable
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
		 * Comma-separated list of model names served by this provider.
		 *
		 * <p>
		 * Used to match agent model references to the correct provider. May be empty if the
		 * provider name alone is sufficient for matching.
		 * </p>
		 */
		@Name(MODELS)
		@Format(CommaSeparatedStrings.class)
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

		// Build model name index from provider configs
		_modelProviderIndex = buildModelProviderIndex(getConfig().getProviders());

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
	 * Builds the model-to-provider index from the provider configurations.
	 *
	 * <p>
	 * First adds any statically configured model names from {@link ProviderConfig#getModels()},
	 * then queries each provider's API to discover additional models.
	 * </p>
	 */
	private static Map<String, String> buildModelProviderIndex(Map<String, ProviderConfig> providers) {
		Map<String, String> index = new LinkedHashMap<>();

		for (Map.Entry<String, ProviderConfig> entry : providers.entrySet()) {
			String providerName = entry.getKey();
			List<String> models = entry.getValue().getModels();
			if (models != null) {
				for (String model : models) {
					if (model != null && !model.isBlank()) {
						index.put(model.trim(), providerName);
					}
				}
			}
		}

		for (Map.Entry<String, ProviderConfig> entry : providers.entrySet()) {
			String providerName = entry.getKey();
			List<String> discovered = discoverModels(providerName, entry.getValue());
			for (String model : discovered) {
				index.putIfAbsent(model, providerName);
			}
			if (!discovered.isEmpty()) {
				Logger.info("Discovered " + discovered.size() + " model(s) from provider '"
					+ providerName + "'.", LLMProviderRegistry.class);
			}
		}

		return index;
	}

	/**
	 * Queries a provider's models API endpoint and returns all discovered model IDs.
	 *
	 * <p>
	 * Calls {@code GET {baseUrl}/v1/models} using the provider's authentication scheme.
	 * Anthropic uses the {@code x-api-key} header; OpenAI and Mistral use
	 * {@code Authorization: Bearer}.
	 * </p>
	 *
	 * @param providerName
	 *        The provider name (e.g. "anthropic", "openai", "mistral").
	 * @param config
	 *        The provider configuration with API key and optional base URL.
	 * @return A list of model ID strings discovered from the API; empty list on any error.
	 */
	private static List<String> discoverModels(String providerName, ProviderConfig config) {
		String apiKey = config.getApiKey();
		if (!isValidApiKey(apiKey)) {
			return List.of();
		}

		String baseUrl = resolveBaseUrl(providerName, config);
		String endpoint = baseUrl + "/v1/models";

		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create(endpoint))
			.GET()
			.timeout(Duration.ofSeconds(30));

		String name = providerName.toLowerCase(Locale.ROOT);
		if (name.contains("anthropic")) {
			requestBuilder.header("x-api-key", apiKey);
			String version = config.getVersion();
			requestBuilder.header("anthropic-version", version != null ? version : "2023-06-01");
		} else {
			requestBuilder.header("Authorization", "Bearer " + apiKey);
		}

		try {
			HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();

			HttpResponse<String> response = client.send(
				requestBuilder.build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			if (response.statusCode() != 200) {
				Logger.warn(
					"Model discovery failed for provider '" + providerName
						+ "': HTTP " + response.statusCode(),
					LLMProviderRegistry.class);
				return List.of();
			}

			return parseModelIds(response.body());
		} catch (Exception ex) {
			Logger.warn(
				"Model discovery failed for provider '" + providerName + "': " + ex.getMessage(),
				LLMProviderRegistry.class);
			return List.of();
		}
	}

	/**
	 * Resolves the base URL for a provider, using defaults when not configured.
	 */
	private static String resolveBaseUrl(String providerName, ProviderConfig config) {
		String baseUrl = config.getBaseUrl();
		if (baseUrl != null) {
			if (baseUrl.endsWith("/v1")) {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
			}
			if (baseUrl.endsWith("/")) {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
			}
			return baseUrl;
		}

		String name = providerName.toLowerCase(Locale.ROOT);
		if (name.contains("anthropic")) {
			return "https://api.anthropic.com";
		}
		if (name.contains("openai")) {
			return "https://api.openai.com";
		}
		if (name.contains("mistral")) {
			return "https://api.mistral.ai";
		}
		return "https://api." + name + ".com";
	}

	/**
	 * Parses model IDs from the JSON response of a {@code /v1/models} endpoint.
	 *
	 * <p>
	 * Expects the standard format: {@code {"data": [{"id": "model-name", ...}, ...]}}.
	 * </p>
	 */
	private static List<String> parseModelIds(String json) {
		List<String> models = new ArrayList<>();
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			reader.beginObject();
			while (reader.hasNext()) {
				String field = reader.nextName();
				if ("data".equals(field)) {
					reader.beginArray();
					while (reader.hasNext()) {
						String modelId = parseModelEntry(reader);
						if (modelId != null && !modelId.isBlank()) {
							models.add(modelId);
						}
					}
					reader.endArray();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (Exception ex) {
			Logger.warn("Failed to parse model list JSON: " + ex.getMessage(),
				LLMProviderRegistry.class);
		}
		return models;
	}

	/**
	 * Parses a single model entry object and extracts its {@code id} field.
	 */
	private static String parseModelEntry(JsonReader reader) throws Exception {
		String id = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String field = reader.nextName();
			if ("id".equals(field)) {
				id = reader.nextString();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return id;
	}

	/**
	 * Creates an {@link LLMProvider} from the provider configuration.
	 *
	 * <p>
	 * The provider name determines which implementation to use: names containing "anthropic"
	 * produce an {@link AnthropicProvider}, "openai" produce an {@link OpenAIProvider}, and
	 * "mistral" produce a {@link MistralProvider}.
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

		throw new IllegalStateException(
			"Unknown provider type: '" + providerName
				+ "'. Provider name must contain 'anthropic', 'openai', or 'mistral'.");
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
