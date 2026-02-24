/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.providers;

import java.util.List;

import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiModelCatalog;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Factory for creating <i>OpenAI</i> chat model instances.
 *
 * <p>
 * This factory creates {@link ChatModel} instances using the <i>OpenAI</i> API (or compatible APIs
 * like Azure <i>OpenAI</i>).
 * </p>
 */
public class OpenAIChatModelFactory extends ChatModelFactory {

	/**
	 * Configuration interface for {@link OpenAIChatModelFactory}.
	 */
	public interface Config extends ChatModelFactory.Config<OpenAIChatModelFactory> {

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
		 * Configuration property name for organization.
		 *
		 * @see #getOrganization()
		 */
		String ORGANIZATION = "organization";

		/**
		 * The <i>OpenAI</i> API key for authentication.
		 */
		@Name(API_KEY)
		@Encrypted
		String getApiKey();

		/**
		 * The base URL for the <i>OpenAI</i> API.
		 *
		 * <p>
		 * Default: https://api.openai.com/v1
		 * </p>
		 * <p>
		 * You can override this to use a different endpoint, such as an Azure <i>OpenAI</i>
		 * deployment or a local proxy server.
		 * </p>
		 */
		@Name(BASE_URL)
		@StringDefault("https://api.openai.com/v1")
		String getBaseUrl();

		/**
		 * The <i>OpenAI</i> organization ID.
		 *
		 * <p>
		 * Optional. For users who belong to multiple organizations, you can specify which
		 * organization is used for an API request.
		 * </p>
		 */
		@Name(ORGANIZATION)
		@Nullable
		String getOrganization();
	}

	/**
	 * Creates a new {@link OpenAIChatModelFactory} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The factory configuration.
	 */
	public OpenAIChatModelFactory(InstantiationContext context, Config config) {
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

		// Create LangChain4j OpenAI chat model
		OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
			.apiKey(config.getApiKey())
			.baseUrl(config.getBaseUrl())
			.modelName(modelName);

		// Add optional organization if configured
		String organization = config.getOrganization();
		if (organization != null) {
			builder.organizationId(organization);
		}

		return builder.build();
	}

	@Override
	protected List<String> getAvailableModels() throws Exception {
		Config config = getConfig();

		OpenAiModelCatalog.Builder builder = OpenAiModelCatalog.builder()
			.apiKey(config.getApiKey())
			.baseUrl(config.getBaseUrl());

		String organization = config.getOrganization();
		if (organization != null) {
			builder.organizationId(organization);
		}

		return extractModelNames(builder.build().listModels());
	}
}
