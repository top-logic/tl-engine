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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;

/**
 * {@link LLMProviderConfig} for third-party services that implement the OpenAI Chat Completions
 * protocol.
 *
 * <p>
 * Unlike {@link OpenAIProviderConfig}, the base URL is mandatory (there is no sensible default).
 * The API key is optional since some services use different authentication mechanisms.
 * Produces a {@link ChatCompletionsProvider} targeting {@code /v1/chat/completions}.
 * </p>
 */
@Label("OpenAI-compatible provider (Chat Completions)")
@InApp
public class OpenAICompatibleProviderConfig implements LLMProviderConfig {

	/**
	 * Configuration for {@link OpenAICompatibleProviderConfig}.
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
		 * The API key for authentication.
		 *
		 * <p>
		 * Optional — some services use different authentication mechanisms.
		 * </p>
		 */
		@Name(API_KEY)
		@Nullable
		@Encrypted
		String getApiKey();

		@Override
		@Mandatory
		String getBaseUrl();

		/** @see #getBaseUrl() */
		void setBaseUrl(String value);

		@Override
		@Options(fun = ModelOptions.class, args = { @Ref(API_KEY), @Ref(LLMProviderConfig.Config.BASE_URL) })
		List<String> getModels();
	}

	/**
	 * Options provider discovering available models from the <code>OpenAI</code>-compatible endpoint.
	 */
	public static class ModelOptions extends Function2<List<String>, String, String> {
		@Override
		public List<String> apply(String apiKey, String baseUrl) {
			if (baseUrl == null || baseUrl.isBlank()) {
				return List.of();
			}
			return ModelDiscoveryHelper.fetchModels(apiKey, ModelDiscoveryHelper.normalizeBaseUrl(baseUrl));
		}
	}

	private final Config _config;

	/** */
	@CalledByReflection
	public OpenAICompatibleProviderConfig(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public LLMProvider createProvider() {
		Map<String, String> customHeaders = _config.getCustomHeaders();
		return new ChatCompletionsProvider(
			_config.getApiKey(), resolveBaseUrl(),
			_config.getConnectTimeout(), _config.getRequestTimeout(), customHeaders);
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
