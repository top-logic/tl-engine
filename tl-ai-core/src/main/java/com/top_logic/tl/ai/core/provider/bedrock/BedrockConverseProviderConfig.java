/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;

/**
 * {@link LLMProviderConfig} preset for the AWS Bedrock Converse streaming API.
 *
 * <p>
 * Configures a {@link BedrockConverseProvider} against any Bedrock-compatible endpoint, including
 * Nexus-Claude gateways and direct regional AWS Bedrock endpoints
 * ({@code https://bedrock-runtime.{region}.amazonaws.com}). An API key is sent as bearer token.
 * </p>
 *
 * <p>
 * No model-listing endpoint exists on Bedrock, so models must be added to the
 * {@link com.top_logic.tl.ai.core.provider.LLMProviderConfig.Config#getModels() models} list
 * manually and {@code discoverModels()} always returns an empty list.
 * </p>
 */
@Label("AWS Bedrock")
@InApp
public class BedrockConverseProviderConfig implements LLMProviderConfig {

	/**
	 * Configuration for {@link BedrockConverseProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		LLMProviderConfig.Config.BASE_URL,
		Config.API_KEY,
		LLMProviderConfig.Config.CREDENTIALS,
		LLMProviderConfig.Config.MODELS,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/**
		 * The API key forwarded as {@code Authorization: Bearer}.
		 *
		 * <p>May be {@code null} for endpoints that use a different authentication mechanism,
		 * see {@link #getCredentials()}.</p>
		 *
		 * @see #API_KEY
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

		/** @see #getModels() */
		void setModels(List<String> value);
	}

	private final Config _config;

	private final CredentialProvider _credentials;

	/** */
	@CalledByReflection
	public BedrockConverseProviderConfig(InstantiationContext context, Config config) {
		_config = config;
		_credentials = LLMProviderConfig.createCredentials(context, config, config.getApiKey(), CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER);
	}

	@Override
	public LLMProvider createProvider() {
		// Do NOT run through normalizeBaseUrl — that strips /v1 which may be valid for some
		// Bedrock gateway paths. AbstractStreamingProvider trims trailing slashes internally.
		String baseUrl = _config.getBaseUrl();
		int connectTimeout = _config.getConnectTimeout();
		int requestTimeout = _config.getRequestTimeout();
		Map<String, String> customHeaders = _config.getCustomHeaders();

		return new BedrockConverseProvider(_credentials, baseUrl, connectTimeout, requestTimeout, customHeaders);
	}

	@Override
	public List<String> discoverModels() {
		// AWS Bedrock has no model-listing endpoint; models must be configured manually.
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
