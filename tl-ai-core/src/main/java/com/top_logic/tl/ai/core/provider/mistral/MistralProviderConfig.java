/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.mistral;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;

/**
 * {@link LLMProviderConfig} for the Mistral AI API.
 */
@InApp
public class MistralProviderConfig implements LLMProviderConfig {


	/**
	 * Configuration for {@link MistralProviderConfig}.
	 */
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration property name for {@link #getBaseUrl()}. */
		String BASE_URL = "base-url";

		/**
		 * The API key for Mistral authentication.
		 */
		@Name(API_KEY)
		@Encrypted
		String getApiKey();

		/**
		 * The base URL for the Mistral API.
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.mistral.ai")
		String getBaseUrl();

		@Override
		@Options(fun = ModelOptions.class, args = { @Ref(API_KEY), @Ref(BASE_URL) })
		List<String> getModels();
	}

	/**
	 * Options provider discovering available models from the Mistral API.
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
	public MistralProviderConfig(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public LLMProvider createProvider() {
		return new MistralProvider(_config.getApiKey(), ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl()));
	}

	@Override
	public List<String> discoverModels() {
		String baseUrl = ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
		return ModelDiscoveryHelper.fetchModels(_config.getApiKey(), baseUrl);
	}

	@Override
	public List<String> getConfiguredModels() {
		List<String> models = _config.getModels();
		return models != null ? models : List.of();
	}
}
