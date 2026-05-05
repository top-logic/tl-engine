/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.local;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;

/**
 * {@link LLMProviderConfig} for local OpenAI-compatible servers (Ollama, LM Studio, vLLM).
 *
 * <p>
 * The base URL is mandatory and no API key is required. Produces an {@link OpenAIProvider}
 * since local servers speak the OpenAI protocol.
 * </p>
 */
@InApp
public class LocalProviderConfig implements LLMProviderConfig {

	/**
	 * Configuration for {@link LocalProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		Config.BASE_URL,
		LLMProviderConfig.Config.MODELS,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getBaseUrl()}. */
		String BASE_URL = "base-url";

		/**
		 * The base URL of the local server (e.g. {@code http://localhost:11434}).
		 *
		 * <p>
		 * Mandatory — the server address must be explicitly specified.
		 * </p>
		 */
		@Name(BASE_URL)
		@Mandatory
		String getBaseUrl();

		@Override
		@Options(fun = ModelOptions.class, args = @Ref(BASE_URL))
		List<String> getModels();
	}

	/**
	 * Options provider discovering available models from a local server.
	 */
	public static class ModelOptions extends Function1<List<String>, String> {
		@Override
		public List<String> apply(String baseUrl) {
			if (baseUrl == null || baseUrl.isBlank()) {
				return List.of();
			}
			return ModelDiscoveryHelper.fetchModels(null, ModelDiscoveryHelper.normalizeBaseUrl(baseUrl));
		}
	}

	private final Config _config;

	/** */
	@CalledByReflection
	public LocalProviderConfig(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public LLMProvider createProvider() {
		return new OpenAIProvider(
			null, resolveBaseUrl(),
			_config.getConnectTimeout(), _config.getRequestTimeout(), _config.getCustomHeaders());
	}

	@Override
	public List<String> discoverModels() {
		return ModelDiscoveryHelper.fetchModels(null, resolveBaseUrl());
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
