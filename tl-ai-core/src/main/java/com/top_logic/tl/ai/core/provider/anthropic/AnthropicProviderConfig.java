/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;

/**
 * {@link LLMProviderConfig} for the Anthropic Messages API.
 *
 * <p>
 * Provides the {@link Config#getVersion() version} property specific to Anthropic's
 * {@code anthropic-version} header requirement.
 * </p>
 */
@Label("Anthropic")
@InApp
public class AnthropicProviderConfig implements LLMProviderConfig {


	/**
	 * Configuration for {@link AnthropicProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		LLMProviderConfig.Config.BASE_URL,
		Config.API_KEY,
		LLMProviderConfig.Config.CREDENTIALS,
		LLMProviderConfig.Config.MODELS,
		Config.VERSION,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration property name for {@link #getVersion()}. */
		String VERSION = "version";

		/**
		 * The API key for Anthropic authentication.
		 */
		@Name(API_KEY)
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		@Override
		@StringDefault("https://api.anthropic.com")
		String getBaseUrl();

		/**
		 * The Anthropic API version string sent as {@code anthropic-version} header.
		 */
		@Name(VERSION)
		@StringDefault("2023-06-01")
		String getVersion();

		@Override
		@StringDefault("anthropic")
		String getCatalogProviderId();
	}

	private final Config _config;

	private final CredentialProvider _credentials;

	/** */
	@CalledByReflection
	public AnthropicProviderConfig(InstantiationContext context, Config config) {
		_config = config;
		_credentials = LLMProviderConfig.createCredentials(context, config, config.getApiKey(), AnthropicMessagesProvider.API_KEY_HEADER, null);
	}

	/**
	 * The configuration of this provider.
	 */
	public Config getConfig() {
		return _config;
	}

	/**
	 * The credentials authenticating the requests to the API of this provider.
	 */
	public CredentialProvider getCredentials() {
		return _credentials;
	}

	@Override
	public LLMProvider createProvider() {
		String baseUrl = ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
		return new AnthropicMessagesProvider(
			_credentials, baseUrl, _config.getVersion(),
			_config.getConnectTimeout(), _config.getRequestTimeout(), _config.getCustomHeaders());
	}

	@Override
	public List<String> discoverModels() {
		String baseUrl = ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
		return ModelDiscoveryHelper.fetchModels(_credentials, baseUrl,
			Map.of(AnthropicMessagesProvider.VERSION_HEADER, _config.getVersion()));
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
