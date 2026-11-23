/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider.vertex;

import java.util.EnumSet;
import java.util.HashMap;
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
import com.top_logic.ai.core.provider.ApiFamilies;
import com.top_logic.ai.core.provider.I18NConstants;
import com.top_logic.ai.core.provider.LLMProvider;
import com.top_logic.ai.core.provider.LLMProviderConfig;
import com.top_logic.ai.core.provider.ToolChoice;

/**
 * {@link LLMProviderConfig} for models on Google Vertex AI, reached directly or through a gateway.
 *
 * <p>
 * Vertex AI serves the models of several publishers. A model is addressed as
 * {@code {base-url}/projects/{project}/locations/{location}/publishers/{publisher}/models/{model}:streamRawPredict}
 * and answers in the dialect of its publisher. The publisher of a model is configured in its
 * {@link Config#getModelSettings() model settings}; a model whose name starts with
 * {@link #CLAUDE_PREFIX} is of the publisher {@link #PUBLISHER_ANTHROPIC} without that. A model
 * whose publisher is unknown or not {@link #SUPPORTED_PUBLISHERS supported} prevents the start.
 * </p>
 *
 * <p>
 * Without a {@link Config#getBaseUrl() base URL}, it is derived from the location as the host
 * followed by {@link #API_VERSION_PATH}: the location {@link #DEFAULT_LOCATION} is served by
 * {@link #GLOBAL_HOST}, the multi-regions {@code us} and {@code eu} by
 * {@code aiplatform.{location}.rep.googleapis.com}, every other region by
 * {@code {location}-aiplatform.googleapis.com}.
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

	/** The version of the Vertex AI API, the end of a base URL derived from the location. */
	public static final String API_VERSION_PATH = "/v1";

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

	private static final String HTTPS = "https://";

	/**
	 * Configuration for {@link VertexProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		Config.PROJECT,
		Config.LOCATION,
		LLMProviderConfig.Config.BASE_URL,
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
		 * Without a {@link #getBaseUrl() base URL}, it also decides the host.
		 * </p>
		 */
		@Name(LOCATION)
		@StringDefault(DEFAULT_LOCATION)
		String getLocation();

		/**
		 * The URL in front of the path {@code /projects/...} of a model, including the version of
		 * the API, e.g. {@code https://gateway.example.com/v1} for a gateway in front of Vertex AI.
		 *
		 * <p>
		 * If not set, Vertex AI is called directly, on the host of the {@link #getLocation()
		 * location} with the version {@link VertexProviderConfig#API_VERSION_PATH}.
		 * </p>
		 */
		@Override
		String getBaseUrl();

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
		 * A model without settings has the publisher derived from its name.
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
	}

	private final Config _config;

	private final CredentialProvider _credentials;

	/** The publisher of each configured model. */
	private final Map<String, String> _publishers;

	/** */
	@CalledByReflection
	public VertexProviderConfig(InstantiationContext context, Config config) {
		_config = config;
		_credentials = LLMProviderConfig.createCredentials(context, config, config.getApiKey(),
			CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER);
		_publishers = publishers(config);
	}

	/**
	 * The publisher of each configured model, checked to be {@link #SUPPORTED_PUBLISHERS
	 * supported}.
	 */
	private static Map<String, String> publishers(Config config) {
		Map<String, String> result = new HashMap<>();
		for (String model : configuredModels(config)) {
			String publisher = publisher(model, config.getModelSettings().get(model));
			if (publisher == null) {
				throw new ConfigurationError(I18NConstants.ERROR_VERTEX_NO_PUBLISHER__PROVIDER_MODEL
					.fill(config.getName(), model));
			}
			if (!SUPPORTED_PUBLISHERS.contains(publisher)) {
				throw new ConfigurationError(
					I18NConstants.ERROR_VERTEX_UNSUPPORTED_PUBLISHER__PROVIDER_MODEL_PUBLISHER_SUPPORTED
						.fill(config.getName(), model, publisher, String.join(", ", SUPPORTED_PUBLISHERS)));
			}
			result.put(model, publisher);
		}
		return result;
	}

	private static List<String> configuredModels(Config config) {
		List<String> models = config.getModels();
		return models != null ? models : List.of();
	}

	/**
	 * The URL the paths of the models are appended to: the configured one without trailing slash,
	 * or the one {@link #defaultBaseUrl(String) derived} from the {@link Config#getLocation()
	 * location}.
	 */
	private String baseUrl() {
		return isBlank(_config.getBaseUrl())
			? defaultBaseUrl(_config.getLocation())
			: stripTrailingSlashes(_config.getBaseUrl().trim());
	}

	/**
	 * The path of the models of the given publisher, to which the model name is appended.
	 */
	private String modelsPath(String publisher) {
		return "/projects/" + _config.getProject() + "/locations/"
			+ _config.getLocation() + "/publishers/" + publisher + "/models/";
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
	 * The base URL of Vertex AI for the given location: its host with the version
	 * {@link #API_VERSION_PATH}.
	 */
	public static String defaultBaseUrl(String location) {
		return HTTPS + host(location) + API_VERSION_PATH;
	}

	/**
	 * The host of Vertex AI for the given location.
	 */
	private static String host(String location) {
		if (DEFAULT_LOCATION.equals(location)) {
			return GLOBAL_HOST;
		}
		if (MULTI_REGIONS.contains(location)) {
			return "aiplatform." + location + ".rep.googleapis.com";
		}
		return location + "-aiplatform.googleapis.com";
	}

	private static String stripTrailingSlashes(String url) {
		String result = url;
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
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

	@Override
	public CredentialProvider getCredentials() {
		return _credentials;
	}

	@Override
	public LLMProvider createProvider() {
		// All configured models are of the only supported publisher, see publishers(Config).
		return new VertexAnthropicProvider(_config.getName(), _credentials, baseUrl(),
			modelsPath(PUBLISHER_ANTHROPIC), _config.getAnthropicVersion(), toolChoices(),
			_config.getConnectTimeout(), _config.getRequestTimeout(), _config.getCustomHeaders(),
			_config.getCatalogProviderId());
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
		return configuredModels(_config);
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
		if (PUBLISHER_ANTHROPIC.equals(_publishers.get(model))) {
			return ApiFamilies.ANTHROPIC;
		}
		return getCatalogProviderId();
	}
}
