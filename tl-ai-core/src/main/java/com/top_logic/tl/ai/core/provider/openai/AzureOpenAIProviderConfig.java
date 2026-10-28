/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.EnumDefaultValue;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.tl.ai.core.provider.AuthScheme;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;
import com.top_logic.tl.ai.core.provider.chatcompletions.TokenLimitParam;

/**
 * {@link LLMProviderConfig} preset for Azure <code>OpenAI</code> Chat Completions deployment endpoints.
 *
 * <p>
 * Configures a {@link ChatCompletionsProvider} with the Azure-specific deployment path
 * {@code /openai/deployments/{model}/chat/completions}, an {@code api-version} query parameter, and
 * a selectable authentication scheme ({@link AuthScheme#BEARER} for gateways such as Nexus GPT /
 * Carina chat; {@link AuthScheme#API_KEY} for real Azure <code>OpenAI</code> subscription-key auth). No
 * model-listing endpoint is available on Azure, so models must be added to the
 * {@link com.top_logic.tl.ai.core.provider.LLMProviderConfig.Config#getModels() models} list
 * manually.
 * </p>
 */
@Label("Azure OpenAI")
@InApp
public class AzureOpenAIProviderConfig implements LLMProviderConfig {

	/**
	 * {@link EnumDefaultValue} providing {@link AuthScheme#BEARER} as the default auth scheme.
	 */
	public static final class BearerDefault extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return AuthScheme.BEARER;
		}
	}

	/**
	 * Configuration for {@link AzureOpenAIProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		LLMProviderConfig.Config.BASE_URL,
		Config.API_KEY,
		LLMProviderConfig.Config.CREDENTIALS,
		Config.API_VERSION,
		Config.AUTH_SCHEME,
		LLMProviderConfig.Config.MODELS,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration property name for {@link #getApiVersion()}. */
		String API_VERSION = "api-version";

		/** Configuration property name for {@link #getAuthScheme()}. */
		String AUTH_SCHEME = "auth-scheme";

		/**
		 * The API key for authentication.
		 *
		 * <p>For Azure <code>OpenAI</code> with {@link AuthScheme#API_KEY} this is the Azure subscription key.
		 * For gateway deployments using {@link AuthScheme#BEARER} this is the bearer token.</p>
		 */
		@Name(API_KEY)
		@Nullable
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		/** @see #getApiKey() */
		void setApiKey(PolymorphicConfiguration<? extends SecretSource> value);

		@Override
		@Mandatory
		String getBaseUrl();

		/** @see #getBaseUrl() */
		void setBaseUrl(String value);

		/**
		 * The {@code api-version} query parameter sent on every request.
		 *
		 * <p>Defaults to {@code 2024-10-21}, which is the version required by Nexus GPT and
		 * Carina chat gateways as well as production Azure <code>OpenAI</code> deployments.</p>
		 */
		@Name(API_VERSION)
		@StringDefault("2024-10-21")
		String getApiVersion();

		/** @see #getApiVersion() */
		void setApiVersion(String value);

		/**
		 * The HTTP authentication scheme used to send the {@link #getApiKey() API key}.
		 *
		 * <p>{@link AuthScheme#BEARER} (the default) places the key in the {@code Authorization}
		 * header, which is what Nexus and Carina gateways expect. {@link AuthScheme#API_KEY}
		 * places it in the {@code api-key} header, which is required by real Azure
		 * <code>OpenAI</code> resource endpoints.</p>
		 *
		 * <p>Not used for {@link #getCredentials() credentials}, which define their header
		 * themselves.</p>
		 */
		@Name(AUTH_SCHEME)
		@ComplexDefault(BearerDefault.class)
		AuthScheme getAuthScheme();

		/** @see #getAuthScheme() */
		void setAuthScheme(AuthScheme value);
	}

	private final Config _config;

	private final CredentialProvider _credentials;

	/** */
	@CalledByReflection
	public AzureOpenAIProviderConfig(InstantiationContext context, Config config) {
		_config = config;
		_credentials = LLMProviderConfig.createCredentials(context, config, config.getApiKey(), config.getAuthScheme().getHeader(), config.getAuthScheme().getScheme());
	}

	@Override
	public CredentialProvider getCredentials() {
		return _credentials;
	}

	@Override
	public LLMProvider createProvider() {
		String baseUrl = ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
		int connectTimeout = _config.getConnectTimeout();
		int requestTimeout = _config.getRequestTimeout();
		Map<String, String> customHeaders = _config.getCustomHeaders();
		String apiVersion = _config.getApiVersion();

		return new ChatCompletionsProvider(
			_credentials, baseUrl, connectTimeout, requestTimeout, customHeaders,
			"/openai/deployments/{model}/chat/completions",
			Map.of("api-version", apiVersion),
			TokenLimitParam.MAX_COMPLETION_TOKENS);
	}

	@Override
	public List<String> discoverModels() {
		// Azure OpenAI has no model-listing endpoint; models must be configured manually.
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
}
