/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.xai;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;

/**
 * {@link LLMProviderConfig} for the xAI (Grok) API.
 *
 * <p>
 * The xAI inference API is OpenAI-compatible. This config produces an {@link XAIProvider}
 * (which extends {@link com.top_logic.tl.ai.core.provider.openai.OpenAIProvider}).
 * </p>
 */
@InApp
public class XAIProviderConfig implements LLMProviderConfig {


	/**
	 * Configuration for {@link XAIProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		Config.BASE_URL,
		Config.API_KEY,
		LLMProviderConfig.Config.MODELS,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration property name for {@link #getBaseUrl()}. */
		String BASE_URL = "base-url";

		/**
		 * The API key for xAI authentication.
		 */
		@Name(API_KEY)
		@Encrypted
		String getApiKey();

		/**
		 * The base URL for the xAI API.
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.x.ai")
		String getBaseUrl();

		@Override
		@Options(fun = ModelOptions.class, args = { @Ref(API_KEY), @Ref(BASE_URL) })
		List<String> getModels();

		@Override
		@StringDefault("xai")
		String getCatalogProviderId();
	}

	/**
	 * Options provider discovering available models from the xAI API.
	 */
	public static class ModelOptions extends Function2<List<String>, String, String> {
		@Override
		public List<String> apply(String apiKey, String baseUrl) {
			return ModelDiscoveryHelper.fetchModels(apiKey, ModelDiscoveryHelper.normalizeBaseUrl(baseUrl));
		}
	}

	private final Config _config;

	/** */
	@CalledByReflection
	public XAIProviderConfig(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public LLMProvider createProvider() {
		return new XAIProvider(
			_config.getApiKey(), ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl()),
			_config.getConnectTimeout(), _config.getRequestTimeout(), _config.getCustomHeaders());
	}

	@Override
	public List<String> discoverModels() {
		return ModelDiscoveryHelper.fetchModels(_config.getApiKey(), ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl()));
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
