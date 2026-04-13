/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.top_logic.ai.service.providers.AnthropicChatModelFactory;
import com.top_logic.ai.service.providers.ModelCapability;
import com.top_logic.ai.service.providers.MistralChatModelFactory;
import com.top_logic.ai.service.providers.OpenAIChatModelFactory;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.catalog.ModelDescription;

/**
 * Abstract factory for creating and managing pooled {@link ChatModel} instances.
 *
 * <p>
 * This factory creates object pools for <i>LangChain4j</i> chat model instances. Each factory is
 * configured for a specific AI provider and can create pools for multiple models from that
 * provider.
 * </p>
 *
 * <p>
 * Concrete implementations exist for different AI providers:
 * </p>
 * <ul>
 * <li>{@link OpenAIChatModelFactory}: For OpenAI models (GPT-4o, GPT-3.5-turbo, etc.)</li>
 * <li>{@link AnthropicChatModelFactory}: For Anthropic models (Claude, etc.)</li>
 * <li>{@link MistralChatModelFactory}: For Mistral AI models</li>
 * </ul>
 */
public abstract class ChatModelFactory implements ConfiguredInstance<ChatModelFactory.Config<?>> {

	/**
	 * Configuration interface for {@link ChatModelFactory}.
	 */
	public interface Config<I extends ChatModelFactory> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration property name for models.
		 *
		 * @see #getModels()
		 */
		String MODELS = "models";

		/**
		 * Configuration property name for maximum pool size.
		 *
		 * @see #getMaxPoolSize()
		 */
		String MAX_POOL_SIZE = "max-pool-size";

		/**
		 * Configuration property name for maximum idle models.
		 *
		 * @see #getMaxIdleModels()
		 */
		String MAX_IDLE_MODELS = "max-idle-models";

		/**
		 * The model names (e.g., "gpt-4o, gpt-3.5-turbo" or "claude-3-opus, claude-3-sonnet").
		 *
		 * <p>
		 * Comma-separated list of model names. Each model name serves as a unique identifier and
		 * determines which models can be requested. All listed models will use the same API
		 * credentials from this factory.
		 * </p>
		 *
		 * @see AIService#getChatModel(String)
		 */
		@Name(MODELS)
		@Format(CommaSeparatedStrings.class)
		List<String> getModels();

		/**
		 * The maximum number of chat models in the pool.
		 *
		 * <p>
		 * Default: 10
		 * </p>
		 */
		@Name(MAX_POOL_SIZE)
		@IntDefault(10)
		int getMaxPoolSize();

		/**
		 * The maximum number of idle chat models kept in the pool.
		 *
		 * <p>
		 * Default: 5
		 * </p>
		 */
		@Name(MAX_IDLE_MODELS)
		@IntDefault(5)
		int getMaxIdleModels();
	}

	private final Config<?> _config;

	/**
	 * Creates a new {@link ChatModelFactory} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The factory configuration.
	 */
	protected ChatModelFactory(InstantiationContext context, Config<?> config) {
		_config = config;
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	/**
	 * Checks if this factory has a valid configuration.
	 *
	 * <p>
	 * This method is used during service startup to filter out factories that cannot be used
	 * because required configuration (like API keys) is missing or invalid.
	 * </p>
	 *
	 * @return {@code true} if the factory is properly configured and can create models,
	 *         {@code false} otherwise.
	 */
	public abstract boolean hasValidConfiguration();

	/**
	 * Checks if an API key is valid (not null, not empty, not a placeholder).
	 *
	 * @param apiKey
	 *        The API key to check.
	 * @return {@code true} if the API key is valid, {@code false} otherwise.
	 */
	protected static boolean isValidApiKey(String apiKey) {
		if (apiKey == null || apiKey.trim().isEmpty()) {
			return false;
		}
		// Check if it's a placeholder like %OPENAI_API_KEY%
		if (apiKey.startsWith("%") && apiKey.endsWith("%")) {
			return false;
		}
		return true;
	}

	/**
	 * Creates an object pool for a specific model.
	 *
	 * @param modelName
	 *        The name of the model to create a pool for.
	 * @return A configured object pool for the specified model.
	 */
	public ObjectPool<ChatModel> createPool(String modelName) {
		Config<?> config = getConfig();

		// Create an anonymous factory for this specific model
		GenericObjectPool<ChatModel> pool = new GenericObjectPool<>(
			new org.apache.commons.pool.BasePoolableObjectFactory<ChatModel>() {
				@Override
				public ChatModel makeObject() throws Exception {
					return createModel(modelName);
				}

				@Override
				public void destroyObject(ChatModel model) throws Exception {
					// ChatModel doesn't have an explicit close method in LangChain4j
					// The underlying HTTP client will be cleaned up by garbage collection
				}

				@Override
				public boolean validateObject(ChatModel model) {
					// Basic validation - ensure model is not null
					return model != null;
				}
			});

		pool.setMaxActive(config.getMaxPoolSize());
		pool.setMaxIdle(config.getMaxIdleModels());
		pool.setTestOnBorrow(true);
		pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);

		return pool;
	}

	/**
	 * Creates a chat model instance for the specified model name.
	 *
	 * <p>
	 * This method is called by the object pool to create new model instances.
	 * </p>
	 *
	 * @param modelName
	 *        The name of the model to create.
	 * @return A new chat model instance.
	 * @throws Exception
	 *         If the model cannot be created.
	 */
	protected abstract ChatModel createModel(String modelName) throws Exception;

	/**
	 * Extracts stable model names from model catalog entries.
	 */
	protected static List<String> extractModelNames(List<ModelDescription> models) {
		return models.stream()
			.map(ModelDescription::name)
			.filter(Objects::nonNull)
			.filter(modelName -> !modelName.isBlank())
			.distinct()
			.sorted()
			.collect(Collectors.toList());
	}

	/**
	 * Returns the list of available models from the provider.
	 *
	 * <p>
	 * This method queries the AI provider's API to get the list of models that can be used.
	 * It is called when the configuration does not specify any models (empty list).
	 * </p>
	 *
	 * @return A list of available model names, or an empty list if the provider does not
	 *         support model listing.
	 * @throws Exception
	 *         If the model list cannot be retrieved.
	 */
	protected abstract List<String> getAvailableModels() throws Exception;

	/**
	 * Returns the set of capabilities supported by the given model.
	 *
	 * <p>
	 * Override in provider-specific subclasses to report model-level capabilities such as extended
	 * thinking or reasoning effort. The default implementation returns an empty set.
	 * </p>
	 *
	 * @param modelName
	 *        The name of the model to query.
	 * @return The set of capabilities the model supports, never {@code null}.
	 *
	 * @see ModelCapability
	 */
	public Set<ModelCapability> getModelCapabilities(String modelName) {
		return EnumSet.noneOf(ModelCapability.class);
	}
}
