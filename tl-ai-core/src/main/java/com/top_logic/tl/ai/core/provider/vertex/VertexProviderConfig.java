/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.vertex;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.tl.ai.core.provider.ApiFamilies;
import com.top_logic.tl.ai.core.provider.I18NConstants;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ToolChoice;

/**
 * {@link LLMProviderConfig} for models on Google Vertex AI, reached directly or through a gateway.
 *
 * <p>
 * Vertex AI serves the models of several publishers. A model is addressed as
 * {@code {base}/{api-version}/projects/{project}/locations/{location}/publishers/{publisher}/models/{model-id}:streamRawPredict}
 * and answers in the dialect of its publisher. The publisher of a model is configured in its
 * {@link Config#getModelSettings() model settings}; a model whose name starts with
 * {@link #CLAUDE_PREFIX} is of the publisher {@link #PUBLISHER_ANTHROPIC} without that. A model
 * whose publisher is unknown or not {@link #SUPPORTED_PUBLISHERS supported} prevents the start.
 * </p>
 *
 * <p>
 * Without a {@link Config#getBaseUrl() base URL}, the host is derived from the location: the
 * location {@link #DEFAULT_LOCATION} is served by {@link #GLOBAL_HOST}, the multi-regions
 * {@code us} and {@code eu} by {@code aiplatform.{location}.rep.googleapis.com}, every other
 * region by {@code {location}-aiplatform.googleapis.com}.
 * </p>
 *
 * <p>
 * Requests are authenticated with the {@link Config#getApiKey() API key} as bearer token, or with
 * the configured {@link Config#getCredentials() credentials}.
 * </p>
 */
@Label("Google Vertex AI")
@InApp
public class VertexProviderConfig implements LLMProviderConfig {

	/** Default of {@link Config#getCatalogProviderId()}: the models.dev ID of Google Vertex AI. */
	public static final String DEFAULT_CATALOG_PROVIDER_ID = "google-vertex";

	/** Default of {@link Config#getLocation()}: the global endpoint. */
	public static final String DEFAULT_LOCATION = "global";

	/** Default of {@link Config#getApiVersion()}. */
	public static final String DEFAULT_API_VERSION = "v1";

	/** Default of {@link Config#getAnthropicVersion()}. */
	public static final String DEFAULT_ANTHROPIC_VERSION = "vertex-2023-10-16";

	/** Default of {@link Config#getToolChoice()}: every tool choice. */
	public static final String DEFAULT_TOOL_CHOICE = "auto,required,none";

	/** The publisher of the Claude models. */
	public static final String PUBLISHER_ANTHROPIC = "anthropic";

	/** The publishers whose models this provider can address. */
	public static final Set<String> SUPPORTED_PUBLISHERS = Set.of(PUBLISHER_ANTHROPIC);

	/** Start of the name of a model of the publisher {@link #PUBLISHER_ANTHROPIC}. */
	public static final String CLAUDE_PREFIX = "claude-";

	/** Host of the location {@link #DEFAULT_LOCATION}. */
	public static final String GLOBAL_HOST = "aiplatform.googleapis.com";

	/** The multi-region locations, served by {@code aiplatform.{location}.rep.googleapis.com}. */
	private static final Set<String> MULTI_REGIONS = Set.of("us", "eu");

	/** Method of the streaming endpoint of a model that answers in its publisher's dialect. */
	private static final String STREAM_RAW_PREDICT = ":streamRawPredict";

	private static final String HTTPS = "https://";

	/**
	 * Configuration for {@link VertexProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		Config.PROJECT,
		Config.LOCATION,
		LLMProviderConfig.Config.BASE_URL,
		Config.API_VERSION,
		Config.API_KEY,
		LLMProviderConfig.Config.CREDENTIALS,
		LLMProviderConfig.Config.MODELS,
		Config.MODEL_SETTINGS,
		Config.TOOL_CHOICE,
		Config.ANTHROPIC_VERSION,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getProject()}. */
		String PROJECT = "project";

		/** Configuration property name for {@link #getLocation()}. */
		String LOCATION = "location";

		/** Configuration property name for {@link #getApiVersion()}. */
		String API_VERSION = "api-version";

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration property name for {@link #getAnthropicVersion()}. */
		String ANTHROPIC_VERSION = "anthropic-version";

		/** Configuration property name for {@link #getToolChoice()}. */
		String TOOL_CHOICE = "tool-choice";

		/** Configuration property name for {@link #getModelSettings()}. */
		String MODEL_SETTINGS = "model-settings";

		/**
		 * The Google Cloud project whose quota the models are used with.
		 */
		@Name(PROJECT)
		@Mandatory
		String getProject();

		/**
		 * The location of the models: {@code global}, a multi-region such as {@code eu}, or a
		 * region such as {@code europe-west4}.
		 *
		 * <p>
		 * Without a {@link #getBaseUrl() base URL}, it also decides the host. A model may be served
		 * at another location, see {@link ModelSettings#getLocation()}.
		 * </p>
		 */
		@Name(LOCATION)
		@StringDefault(DEFAULT_LOCATION)
		String getLocation();

		/**
		 * The URL of a gateway in front of Vertex AI, e.g. {@code https://gateway.example.com/v1}.
		 *
		 * <p>
		 * A trailing {@link #getApiVersion() API version} may be included. If not set, Vertex AI is
		 * called directly, on the host of the {@link #getLocation() location}.
		 * </p>
		 */
		@Override
		String getBaseUrl();

		/**
		 * The version of the Vertex AI API, the first segment of the path of a model.
		 */
		@Name(API_VERSION)
		@StringDefault(DEFAULT_API_VERSION)
		String getApiVersion();

		/**
		 * The API key forwarded as {@code Authorization: Bearer}.
		 *
		 * <p>
		 * May be {@code null} for endpoints that use a different authentication mechanism, see
		 * {@link #getCredentials()}.
		 * </p>
		 */
		@Name(API_KEY)
		@Nullable
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		/** @see #getApiKey() */
		void setApiKey(PolymorphicConfiguration<? extends SecretSource> value);

		@Override
		@StringDefault(DEFAULT_CATALOG_PROVIDER_ID)
		String getCatalogProviderId();

		/**
		 * The version of the Anthropic Messages API sent in the body of a request to a model of the
		 * publisher {@link VertexProviderConfig#PUBLISHER_ANTHROPIC}.
		 */
		@Name(ANTHROPIC_VERSION)
		@StringDefault(DEFAULT_ANTHROPIC_VERSION)
		String getAnthropicVersion();

		/**
		 * The tool choices the endpoint accepts.
		 *
		 * <p>
		 * Some endpoints accept only {@code auto}. On such an endpoint, a request that forces a
		 * tool call ({@code required}) fails before it is sent, and a request that forbids tool
		 * calls ({@code none}) is sent with {@code auto}, keeping its tools.
		 * </p>
		 */
		@Name(TOOL_CHOICE)
		@Format(ToolChoice.ListFormat.class)
		@FormattedDefault(DEFAULT_TOOL_CHOICE)
		List<ToolChoice> getToolChoice();

		/**
		 * Settings of individual {@link #getModels() models}, by model name.
		 *
		 * <p>
		 * A model without settings is addressed under its name, at the {@link #getLocation()
		 * location} of the provider, with the publisher derived from its name.
		 * </p>
		 */
		@Name(MODEL_SETTINGS)
		@EntryTag("model")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ModelSettings> getModelSettings();

		/** @see #getModels() */
		void setModels(List<String> value);
	}

	/**
	 * Settings of a model of a {@link VertexProviderConfig}, named by the model.
	 */
	public interface ModelSettings extends NamedConfigMandatory {

		/** Configuration property name for {@link #getPublisher()}. */
		String PUBLISHER = "publisher";

		/** Configuration property name for {@link #getModelId()}. */
		String MODEL_ID = "model-id";

		/** Configuration property name for {@link #getLocation()}. */
		String LOCATION = "location";

		/** Configuration property name for {@link #getCatalogModel()}. */
		String CATALOG_MODEL = "catalog-model";

		/**
		 * The publisher of the model, e.g. {@value VertexProviderConfig#PUBLISHER_ANTHROPIC}.
		 *
		 * <p>
		 * If not set, a model whose name starts with {@value VertexProviderConfig#CLAUDE_PREFIX} is
		 * of the publisher {@value VertexProviderConfig#PUBLISHER_ANTHROPIC}.
		 * </p>
		 */
		@Name(PUBLISHER)
		@Nullable
		String getPublisher();

		/**
		 * The ID of the model in the path of its endpoint, if it differs from the model name, e.g.
		 * {@code claude-sonnet-5-5@20260101}.
		 */
		@Name(MODEL_ID)
		@Nullable
		String getModelId();

		/**
		 * The location the model is served at, if it differs from the
		 * {@link Config#getLocation() location} of the provider.
		 */
		@Name(LOCATION)
		@Nullable
		String getLocation();

		/**
		 * The key of the model in the model catalog, if it differs from the model name.
		 *
		 * <p>
		 * The catalog is searched under this key, then under the key with the suffix
		 * {@code @default}.
		 * </p>
		 */
		@Name(CATALOG_MODEL)
		@Nullable
		String getCatalogModel();
	}

	/**
	 * How one model of this provider is addressed.
	 *
	 * @param publisher
	 *        The publisher of the model.
	 * @param baseUrl
	 *        The URL the {@link #path()} is appended to, without trailing slash.
	 * @param path
	 *        The path of the streaming endpoint of the model.
	 */
	public record ModelEndpoint(String publisher, String baseUrl, String path) {
		/** The URL of the streaming endpoint of the model. */
		public String url() {
			return baseUrl + path;
		}
	}

	private final Config _config;

	private final CredentialProvider _credentials;

	private final Map<String, ModelEndpoint> _endpoints;

	/** */
	@CalledByReflection
	public VertexProviderConfig(InstantiationContext context, Config config) {
		_config = config;
		_credentials = LLMProviderConfig.createCredentials(context, config, config.getApiKey(),
			CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER);
		_endpoints = endpoints(config);
	}

	private static Map<String, ModelEndpoint> endpoints(Config config) {
		Map<String, ModelEndpoint> result = new LinkedHashMap<>();
		List<String> models = config.getModels();
		if (models != null) {
			for (String model : models) {
				result.put(model, endpoint(config, model));
			}
		}
		return result;
	}

	private static ModelEndpoint endpoint(Config config, String model) {
		ModelSettings settings = config.getModelSettings().get(model);
		String publisher = publisher(model, settings);
		if (publisher == null) {
			throw new ConfigurationError(I18NConstants.ERROR_VERTEX_NO_PUBLISHER__PROVIDER_MODEL
				.fill(config.getName(), model));
		}
		if (!SUPPORTED_PUBLISHERS.contains(publisher)) {
			throw new ConfigurationError(
				I18NConstants.ERROR_VERTEX_UNSUPPORTED_PUBLISHER__PROVIDER_MODEL_PUBLISHER_SUPPORTED
					.fill(config.getName(), model, publisher, String.join(", ", SUPPORTED_PUBLISHERS)));
		}
		String location = nonBlank(settings == null ? null : settings.getLocation(), config.getLocation());
		String modelId = nonBlank(settings == null ? null : settings.getModelId(), model);
		String apiVersion = config.getApiVersion();

		String baseUrl = isBlank(config.getBaseUrl())
			? HTTPS + host(location)
			: gatewayUrl(config.getBaseUrl(), apiVersion);
		String path = "/" + apiVersion + "/projects/" + config.getProject() + "/locations/" + location
			+ "/publishers/" + publisher + "/models/" + modelId + STREAM_RAW_PREDICT;
		return new ModelEndpoint(publisher, baseUrl, path);
	}

	/**
	 * The publisher of the given model, <code>null</code> if it is unknown.
	 */
	private static String publisher(String model, ModelSettings settings) {
		String configured = settings == null ? null : settings.getPublisher();
		if (!isBlank(configured)) {
			return configured.trim();
		}
		if (model.startsWith(CLAUDE_PREFIX)) {
			return PUBLISHER_ANTHROPIC;
		}
		return null;
	}

	/**
	 * The host of Vertex AI for the given location.
	 */
	public static String host(String location) {
		if (DEFAULT_LOCATION.equals(location)) {
			return GLOBAL_HOST;
		}
		if (MULTI_REGIONS.contains(location)) {
			return "aiplatform." + location + ".rep.googleapis.com";
		}
		return location + "-aiplatform.googleapis.com";
	}

	/**
	 * The configured gateway URL without trailing slash and without a trailing API version, which
	 * is part of the path of a model.
	 */
	private static String gatewayUrl(String baseUrl, String apiVersion) {
		String result = stripTrailingSlashes(baseUrl.trim());
		String versionSuffix = "/" + apiVersion;
		if (result.endsWith(versionSuffix)) {
			result = stripTrailingSlashes(result.substring(0, result.length() - versionSuffix.length()));
		}
		return result;
	}

	private static String stripTrailingSlashes(String url) {
		String result = url;
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	private static String nonBlank(String value, String fallback) {
		return isBlank(value) ? fallback : value.trim();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	/**
	 * The configuration of this provider.
	 */
	public Config getConfig() {
		return _config;
	}

	/**
	 * How the given model of this provider is addressed.
	 *
	 * @return The endpoint, <code>null</code> if the model is not one of this provider.
	 */
	public ModelEndpoint getEndpoint(String model) {
		return _endpoints.get(model);
	}

	@Override
	public CredentialProvider getCredentials() {
		return _credentials;
	}

	@Override
	public LLMProvider createProvider() {
		Map<String, LLMProvider> models = new LinkedHashMap<>();
		for (Map.Entry<String, ModelEndpoint> entry : _endpoints.entrySet()) {
			ModelEndpoint endpoint = entry.getValue();
			// All endpoints are of a supported publisher, see endpoint(Config, String).
			models.put(entry.getKey(), new VertexAnthropicProvider(_credentials, endpoint.baseUrl(),
				endpoint.path(), _config.getAnthropicVersion(), _config.getConnectTimeout(),
				_config.getRequestTimeout(), _config.getCustomHeaders(), _config.getCatalogProviderId()));
		}
		return new VertexProvider(_config.getName(), toolChoices(), models, _credentials);
	}

	/**
	 * The configured {@link Config#getToolChoice() tool choices}, the same for every model.
	 */
	@Override
	public Set<ToolChoice> getToolChoices(String model) {
		return toolChoices();
	}

	private Set<ToolChoice> toolChoices() {
		List<ToolChoice> configured = _config.getToolChoice();
		return configured.isEmpty() ? EnumSet.allOf(ToolChoice.class) : EnumSet.copyOf(configured);
	}

	@Override
	public List<String> discoverModels() {
		// The models of a project are configured, not listed by Vertex AI.
		return List.of();
	}

	@Override
	public List<String> getConfiguredModels() {
		List<String> models = _config.getModels();
		return models != null ? models : List.of();
	}

	@Override
	public String getCatalogProviderId() {
		return _config.getCatalogProviderId();
	}

	/**
	 * {@link ApiFamilies#ANTHROPIC} for a model of the publisher {@link #PUBLISHER_ANTHROPIC},
	 * otherwise the catalog provider ID.
	 */
	@Override
	public String getApiFamily(String model) {
		ModelEndpoint endpoint = _endpoints.get(model);
		if (endpoint != null && PUBLISHER_ANTHROPIC.equals(endpoint.publisher())) {
			return ApiFamilies.ANTHROPIC;
		}
		return getCatalogProviderId();
	}

	@Override
	public String getCatalogModel(String model) {
		ModelSettings settings = _config.getModelSettings().get(model);
		return nonBlank(settings == null ? null : settings.getCatalogModel(), model);
	}
}
