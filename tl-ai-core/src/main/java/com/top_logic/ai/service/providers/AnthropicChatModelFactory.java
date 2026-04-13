/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.providers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicModelCatalog;
import dev.langchain4j.model.chat.ChatModel;

/**
 * Factory for creating Anthropic chat model instances.
 *
 * <p>
 * This factory creates {@link ChatModel} instances using the Anthropic API
 * for Claude models.
 * </p>
 */
public class AnthropicChatModelFactory extends ChatModelFactory {

	/**
	 * Configuration interface for {@link AnthropicChatModelFactory}.
	 */
	public interface Config extends ChatModelFactory.Config<AnthropicChatModelFactory> {

		/**
		 * Configuration property name for API key.
		 *
		 * @see #getApiKey()
		 */
		String API_KEY = "api-key";

		/**
		 * Configuration property name for base URL.
		 *
		 * @see #getBaseUrl()
		 */
		String BASE_URL = "base-url";

		/**
		 * Configuration property name for version.
		 *
		 * @see #getVersion()
		 */
		String VERSION = "version";

		/**
		 * The Anthropic API key for authentication.
		 */
		@Name(API_KEY)
		@Encrypted
		String getApiKey();

		/**
		 * The base URL for the Anthropic API.
		 *
		 * <p>
		 * Default: https://api.anthropic.com/v1
		 * </p>
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.anthropic.com/v1")
		String getBaseUrl();

		/**
		 * The API version to use.
		 *
		 * <p>
		 * Optional. Anthropic uses versioned APIs. If not specified, the <i>LangChain4j</i> default
		 * is used.
		 * </p>
		 */
		@Name(VERSION)
		@Nullable
		String getVersion();
	}

	/**
	 * Creates a new {@link AnthropicChatModelFactory} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The factory configuration.
	 */
	public AnthropicChatModelFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public boolean hasValidConfiguration() {
		return isValidApiKey(getConfig().getApiKey());
	}

	@Override
	protected ChatModel createModel(String modelName) throws Exception {
		Config config = getConfig();

		// Create LangChain4j Anthropic chat model
		AnthropicChatModel.AnthropicChatModelBuilder builder = AnthropicChatModel.builder()
			.apiKey(config.getApiKey())
			.baseUrl(config.getBaseUrl())
			.modelName(modelName);

		// Add optional version if configured
		String version = config.getVersion();
		if (version != null) {
			builder.version(version);
		}

		return builder.build();
	}

	@Override
	protected List<String> getAvailableModels() throws Exception {
		Config config = getConfig();

		AnthropicModelCatalog.Builder builder = AnthropicModelCatalog.builder()
			.apiKey(config.getApiKey())
			.baseUrl(config.getBaseUrl());

		String version = config.getVersion();
		if (version != null) {
			builder.version(version);
		}

		return extractModelNames(builder.build().listModels());
	}

	@Override
	public Set<ModelCapability> getModelCapabilities(String modelName) {
		return anthropicModelCapabilities(modelName);
	}

	/**
	 * Determines the set of {@link ModelCapability capabilities} for the given Anthropic model name.
	 *
	 * <p>
	 * Package-private for testability.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to check (e.g. {@code "claude-opus-4-6-20260401"}).
	 * @return The capabilities supported by the model, never {@code null}.
	 */
	static Set<ModelCapability> anthropicModelCapabilities(String modelName) {
		String name = modelName.toLowerCase();

		// Opus 4.6 / Sonnet 4.6 — adaptive thinking (must be checked before opus-4 / sonnet-4)
		if (name.contains("opus-4-6") || name.contains("sonnet-4-6")) {
			return EnumSet.of(ModelCapability.THINKING_ENABLED, ModelCapability.THINKING_ADAPTIVE);
		}

		// Opus 4.x / Sonnet 4.x (including 4-5, 4-0, etc.)
		if (name.contains("opus-4") || name.contains("sonnet-4")) {
			return EnumSet.of(ModelCapability.THINKING_ENABLED);
		}

		// Haiku 4.5
		if (name.contains("haiku-4-5")) {
			return EnumSet.of(ModelCapability.THINKING_ENABLED);
		}

		// Claude 3.7 Sonnet
		if (name.contains("claude-3-7-sonnet")) {
			return EnumSet.of(ModelCapability.THINKING_ENABLED);
		}

		// Claude 3.5 and older — no thinking support
		return EnumSet.noneOf(ModelCapability.class);
	}
}
