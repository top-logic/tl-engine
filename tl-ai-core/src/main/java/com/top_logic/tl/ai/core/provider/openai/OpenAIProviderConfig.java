/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;

/**
 * {@link LLMProviderConfig} for the <code>OpenAI</code> cloud API.
 */
@Label("OpenAI")
@InApp
public class OpenAIProviderConfig implements LLMProviderConfig {


	/**
	 * Configuration for {@link OpenAIProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		LLMProviderConfig.Config.BASE_URL,
		Config.API_KEY,
		LLMProviderConfig.Config.MODELS,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/**
		 * The API key for <code>OpenAI</code> authentication.
		 */
		@Name(API_KEY)
		@Encrypted
		String getApiKey();

		@Override
		@StringDefault("https://api.openai.com")
		String getBaseUrl();

		@Override
		@StringDefault("openai")
		String getCatalogProviderId();
	}

	private final Config _config;

	/** */
	@CalledByReflection
	public OpenAIProviderConfig(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public LLMProvider createProvider() {
		return new OpenAIResponsesProvider(
			_config.getApiKey(), resolveBaseUrl(),
			_config.getConnectTimeout(), _config.getRequestTimeout(), _config.getCustomHeaders());
	}

	@Override
	public List<String> discoverModels() {
		return ModelDiscoveryHelper.fetchModels(_config.getApiKey(), resolveBaseUrl());
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

	private String resolveBaseUrl() {
		return ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
	}
}
