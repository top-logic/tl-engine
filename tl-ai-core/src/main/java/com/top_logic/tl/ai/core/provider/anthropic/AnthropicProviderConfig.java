/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function3;
import com.top_logic.layout.form.values.edit.annotation.Options;
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
@InApp
public class AnthropicProviderConfig implements LLMProviderConfig {


	/**
	 * Configuration for {@link AnthropicProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		Config.BASE_URL,
		Config.API_KEY,
		LLMProviderConfig.Config.MODELS,
		Config.VERSION,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration property name for {@link #getBaseUrl()}. */
		String BASE_URL = "base-url";

		/** Configuration property name for {@link #getVersion()}. */
		String VERSION = "version";

		/**
		 * The API key for Anthropic authentication.
		 */
		@Name(API_KEY)
		@Encrypted
		String getApiKey();

		/**
		 * The base URL for the Anthropic API.
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.anthropic.com")
		String getBaseUrl();

		/**
		 * The Anthropic API version string sent as {@code anthropic-version} header.
		 */
		@Name(VERSION)
		@StringDefault("2023-06-01")
		String getVersion();

		@Override
		@Options(fun = ModelOptions.class, args = { @Ref(API_KEY), @Ref(BASE_URL), @Ref(VERSION) })
		List<String> getModels();

		@Override
		@StringDefault("anthropic")
		String getCatalogProviderId();
	}

	/**
	 * Options provider discovering available models from the Anthropic API.
	 */
	public static class ModelOptions extends Function3<List<String>, String, String, String> {
		@Override
		public List<String> apply(String apiKey, String baseUrl, String version) {
			String url = ModelDiscoveryHelper.normalizeBaseUrl(baseUrl);
			Map<String, String> headers = new LinkedHashMap<>();
			headers.put("x-api-key", apiKey);
			headers.put("anthropic-version", version);
			return ModelDiscoveryHelper.fetchModels(url, headers);
		}
	}

	private final Config _config;

	/** */
	@CalledByReflection
	public AnthropicProviderConfig(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public LLMProvider createProvider() {
		String baseUrl = ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
		return new AnthropicProvider(
			_config.getApiKey(), baseUrl, _config.getVersion(),
			_config.getConnectTimeout(), _config.getRequestTimeout(), _config.getCustomHeaders());
	}

	@Override
	public List<String> discoverModels() {
		String baseUrl = ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("x-api-key", _config.getApiKey());
		headers.put("anthropic-version", _config.getVersion());
		return ModelDiscoveryHelper.fetchModels(baseUrl, headers);
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
