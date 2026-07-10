/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.xai;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EnumDefaultValue;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ModelDiscoveryHelper;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsProvider;
import com.top_logic.tl.ai.core.provider.openai.OpenAIResponsesProvider;

/**
 * {@link LLMProviderConfig} for the <code>xAI</code> (Grok) API.
 *
 * <p>
 * The <code>xAI</code> API supports both the <code>OpenAI</code> Chat Completions protocol
 * ({@code /v1/chat/completions}) and the <code>OpenAI</code> Responses protocol ({@code /v1/responses}).
 * Use {@link Config#getProtocol()} to select the wire format.
 * The default is {@link Protocol#CHAT_COMPLETIONS}.
 * </p>
 */
@Label("xAI")
@InApp
public class XAIProviderConfig implements LLMProviderConfig {

	/**
	 * Wire protocol to use when talking to the <code>xAI</code> endpoint.
	 */
	public enum Protocol {

		/** <code>OpenAI</code> Chat Completions protocol ({@code /v1/chat/completions}, SSE with {@code [DONE]}). */
		CHAT_COMPLETIONS,

		/** <code>OpenAI</code> Responses protocol ({@code /v1/responses}, event-typed SSE). */
		RESPONSES,
	}

	/**
	 * {@link EnumDefaultValue} providing {@link Protocol#CHAT_COMPLETIONS} as the default.
	 */
	public static final class ChatCompletionsDefault extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return Protocol.CHAT_COMPLETIONS;
		}
	}

	/**
	 * Configuration for {@link XAIProviderConfig}.
	 */
	@DisplayOrder({
		LLMProviderConfig.Config.CATALOG_PROVIDER_ID,
		LLMProviderConfig.Config.BASE_URL,
		Config.API_KEY,
		Config.PROTOCOL,
		LLMProviderConfig.Config.MODELS,
		LLMProviderConfig.Config.CONNECT_TIMEOUT,
		LLMProviderConfig.Config.REQUEST_TIMEOUT,
		LLMProviderConfig.Config.CUSTOM_HEADERS,
	})
	public interface Config extends LLMProviderConfig.Config {

		/** Configuration property name for {@link #getApiKey()}. */
		String API_KEY = "api-key";

		/** Configuration property name for {@link #getProtocol()}. */
		String PROTOCOL = "protocol";

		/**
		 * The API key for <code>xAI</code> authentication.
		 */
		@Name(API_KEY)
		@Encrypted
		String getApiKey();

		/** @see #getApiKey() */
		void setApiKey(String value);

		@Override
		@StringDefault("https://api.x.ai")
		String getBaseUrl();

		/**
		 * The wire protocol to use when communicating with the <code>xAI</code> endpoint.
		 *
		 * <p>
		 * {@link Protocol#CHAT_COMPLETIONS} produces a {@link ChatCompletionsProvider};
		 * {@link Protocol#RESPONSES} produces an {@link OpenAIResponsesProvider}.
		 * </p>
		 */
		@Name(PROTOCOL)
		@ComplexDefault(ChatCompletionsDefault.class)
		Protocol getProtocol();

		/** @see #getProtocol() */
		void setProtocol(Protocol value);

		@Override
		@StringDefault("xai")
		String getCatalogProviderId();
	}

	private final Config _config;

	/** */
	@CalledByReflection
	public XAIProviderConfig(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public LLMProvider createProvider() {
		String apiKey = _config.getApiKey();
		String baseUrl = ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl());
		int connectTimeout = _config.getConnectTimeout();
		int requestTimeout = _config.getRequestTimeout();
		Map<String, String> customHeaders = _config.getCustomHeaders();

		return switch (_config.getProtocol()) {
			case RESPONSES -> new OpenAIResponsesProvider(apiKey, baseUrl, connectTimeout, requestTimeout, customHeaders);
			case CHAT_COMPLETIONS -> new ChatCompletionsProvider(apiKey, baseUrl, connectTimeout, requestTimeout, customHeaders);
		};
	}

	@Override
	public List<String> discoverModels() {
		return ModelDiscoveryHelper.fetchModels(_config.getApiKey(),
			ModelDiscoveryHelper.normalizeBaseUrl(_config.getBaseUrl()));
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
