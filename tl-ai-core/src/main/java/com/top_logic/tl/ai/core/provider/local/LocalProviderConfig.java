/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.local;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;

/**
 * {@link LLMProviderConfig} for local <code>OpenAI</code>-compatible servers (Ollama, LM Studio, <code>vLLM</code>).
 *
 * <p>
 * The base URL is mandatory and no API key is required. Local servers speak the <code>OpenAI</code> Chat
 * Completions protocol ({@code /v1/chat/completions}), so this config produces a
 * {@link ChatCompletionsProvider}.
 * </p>
 */
@Label("Local (OpenAI-compatible)")
@InApp
public class LocalProviderConfig implements LLMProviderConfig {

	/**
	 * Configuration for {@link LocalProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		LLMProviderConfig.Config.BASE_URL,
		LLMProviderConfig.Config.CREDENTIALS,
		LLMProviderConfig.Config.MODELS,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		@Override
		@Mandatory
		String getBaseUrl();

		/** @see #getBaseUrl() */
		void setBaseUrl(String value);
	}

	private final Config _config;

	private final CredentialProvider _credentials;

	/** */
	@CalledByReflection
	public LocalProviderConfig(InstantiationContext context, Config config) {
		_config = config;
		_credentials = LLMProviderConfig.createCredentials(context, config);
	}

	@Override
	public LLMProvider createProvider() {
		Map<String, String> customHeaders = _config.getCustomHeaders();
		return new ChatCompletionsProvider(
			_credentials, resolveBaseUrl(),
			_config.getConnectTimeout(), _config.getRequestTimeout(), customHeaders);
	}

	@Override
	public List<String> discoverModels() {
		return ModelDiscoveryHelper.fetchModels(_credentials, resolveBaseUrl());
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
