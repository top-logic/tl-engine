/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool.ObjectPool;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.thread.UnboundListener;

import dev.langchain4j.model.chat.ChatModel;

/**
 * TopLogic service that provides access to AI models via <i>LangChain4j</i> with thread-safe client
 * pooling.
 *
 * <p>
 * This service manages multiple {@link ChatModelFactory} instances, each configured for a specific
 * AI model. Models are pooled and automatically bound to the current {@link InteractionContext},
 * then returned to the pool when the context is destroyed.
 * </p>
 *
 * @see #getInstance()
 * @see #getChatModel()
 */
public class AIService extends ConfiguredManagedClass<AIService.Config<?>> {

	/**
	 * ThreadContext property key for storing the borrowed chat models by model name.
	 */
	private static final Property<Map<String, ChatModel>> CONTEXT_MODELS_KEY =
		TypedAnnotatable.propertyMap(AIService.class.getName() + ".models");

	/**
	 * Configuration interface for {@link AIService}.
	 */
	@DisplayOrder({
		Config.FACTORIES,
		Config.DEFAULT_MODEL,
	})
	@DisplayInherited(DisplayStrategy.PREPEND)
	public interface Config<I extends AIService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for model factories.
		 *
		 * @see #getFactories()
		 */
		String FACTORIES = "factories";

		/**
		 * Configuration property name for default model.
		 *
		 * @see #getDefaultModel()
		 */
		String DEFAULT_MODEL = "default-model";

		/**
		 * The configured model factories.
		 *
		 * <p>
		 * Each factory is identified by the model name it produces and manages a pool
		 * of chat model instances for that specific model.
		 * </p>
		 */
		@Name(FACTORIES)
		@DefaultContainer
		@EntryTag("factory")
		List<ChatModelFactory.Config<?>> getFactories();

		/**
		 * The default model name to use when no model is explicitly specified.
		 *
		 * <p>
		 * This should match one of the model names configured in {@link #getFactories()}.
		 * If not specified or null, the first factory's model will be used as default.
		 * </p>
		 */
		@Name(DEFAULT_MODEL)
		@Nullable
		String getDefaultModel();
	}

	private static volatile AIService _instance;

	private Map<String, ObjectPool<ChatModel>> _modelPools = new HashMap<>();

	private Map<String, ChatModelFactory> _factories = new HashMap<>();

	private List<String> _modelNames;

	private String _defaultModel;

	/**
	 * Creates a new {@link AIService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public AIService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Returns the singleton instance of the {@link AIService}.
	 *
	 * @return The service instance, or {@code null} if not yet started.
	 */
	public static AIService getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();

		try {
			// Register singleton instance
			_instance = this;

			Config<?> config = getConfig();

			// Validate that at least one factory is configured
			List<ChatModelFactory.Config<?>> factoryConfigs = config.getFactories();
			if (factoryConfigs == null || factoryConfigs.isEmpty()) {
				throw new RuntimeException(
					"At least one model factory must be configured. Please add factories to the '" +
						Config.FACTORIES + "' property in the service configuration.");
			}

			// Create a pool for each model from each configured factory
			InstantiationContext context = new DefaultInstantiationContext(AIService.class);
			for (ChatModelFactory.Config<?> factoryConfig : factoryConfigs) {
				List<String> modelNames = factoryConfig.getModels();

				// Create the factory
				ChatModelFactory factory = context.getInstance(factoryConfig);

				// Skip factories without valid configuration (e.g., missing API keys)
				if (!factory.hasValidConfiguration()) {
					continue;
				}

				// If no models are configured, query the provider for available models
				if (modelNames.isEmpty()) {
					try {
						modelNames = factory.getAvailableModels();
						if (modelNames.isEmpty()) {
							// Provider does not support model listing - skip this factory
							continue;
						}
					} catch (Exception ex) {
						throw new RuntimeException(
							"Failed to retrieve available models from provider: " + ex.getMessage(), ex);
					}
				}

				// Create a pool for each model using this factory
				for (String modelName : modelNames) {
					if (modelName == null || modelName.trim().isEmpty()) {
						throw new RuntimeException("Model name must not be empty.");
					}

					// Create the pool for this specific model
					ObjectPool<ChatModel> pool = factory.createPool(modelName);
					_modelPools.put(modelName, pool);
					_factories.put(modelName, factory);
				}
			}

			// Ensure at least one factory was successfully configured
			if (_modelPools.isEmpty()) {
				Logger.warn(
					"No valid model factories available. Please check that API keys are properly configured.",
					AIService.class);
				_modelNames = List.of();
			} else {
				// Create immutable list of model names in consistent order
				_modelNames = List.copyOf(_modelPools.keySet());

				// Set default model
				_defaultModel = config.getDefaultModel();
				if (_defaultModel == null || !_modelPools.containsKey(_defaultModel)) {
					// Use first available model as default
					_defaultModel = _modelNames.get(0);

					Logger.info("Selecting default AI model: " + _defaultModel, AIService.class);
				}
			}
			context.checkErrors();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to start AI service: " + ex.getMessage(), ex);
		}
	}

	@Override
	protected void shutDown() {
		try {
			// Unregister singleton instance
			_instance = null;

			// Close all model pools
			for (Map.Entry<String, ObjectPool<ChatModel>> entry : _modelPools.entrySet()) {
				try {
					entry.getValue().close();
				} catch (Exception ex) {
					// Log but don't fail shutdown
					System.err.println("Error closing chat model pool for model '" + entry.getKey() +
						"': " + ex.getMessage());
				}
			}
			_modelPools.clear();
			_factories.clear();
			_defaultModel = null;

		} finally {
			super.shutDown();
		}
	}

	/**
	 * Returns a chat model for the default model name, bound to the current {@link InteractionContext}.
	 *
	 * @return The chat model bound to the current interaction context.
	 * @throws RuntimeException
	 *         If no model is available or the service is not started.
	 * @see #getChatModel(String)
	 */
	public ChatModel getChatModel() {
		return getChatModel(null);
	}

	/**
	 * Returns a chat model for the specified model name, bound to the current {@link InteractionContext}.
	 *
	 * <p>
	 * The model is borrowed from the pool and automatically returned when the current
	 * {@link InteractionContext} is destroyed. Multiple calls to this method within the same
	 * interaction context with the same model name will return the same model instance.
	 * </p>
	 *
	 * @param modelName
	 *        The name of the model to use, or {@code null} to use the default model.
	 * @return The chat model bound to the current interaction context.
	 * @throws RuntimeException
	 *         If no model is available or the service is not started.
	 */
	public ChatModel getChatModel(String modelName) {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			throw new RuntimeException(
				"No InteractionContext available. Chat models must be used within an InteractionContext.");
		}

		// Use default model if none specified
		String effectiveModelName = modelName != null ? modelName : _defaultModel;

		// Get or create the models map for this interaction
		Map<String, ChatModel> contextModels = interaction.mkMap(CONTEXT_MODELS_KEY);
		if (contextModels == null) {
			contextModels = new HashMap<>();
			interaction.set(CONTEXT_MODELS_KEY, contextModels);

			// Register cleanup callback to return all models to their pools when context is
			// destroyed
			// Note: We only register one listener that returns all models
			interaction.addUnboundListener(new UnboundListener() {
				@Override
				public void threadUnbound(InteractionContext context) throws Throwable {
					Map<String, ChatModel> models = context.get(CONTEXT_MODELS_KEY);
					if (models != null) {
						for (Map.Entry<String, ChatModel> entry : models.entrySet()) {
							ObjectPool<ChatModel> modelPool = _modelPools.get(entry.getKey());
							if (modelPool != null) {
								try {
									modelPool.returnObject(entry.getValue());
								} catch (Exception ex) {
									// Log but don't fail cleanup
									System.err.println("Error returning chat model to pool for model '" +
										entry.getKey() + "': " + ex.getMessage());
								}
							}
						}
						models.clear();
					}
				}
			});
		}

		// Check if we already have this model for this interaction context
		ChatModel model = contextModels.get(effectiveModelName);
		if (model != null) {
			return model;
		}

		// Get the pool for this model
		ObjectPool<ChatModel> pool = _modelPools.get(effectiveModelName);
		if (pool == null) {
			throw new RuntimeException(
				"No model factory configured for model: " + effectiveModelName +
					". Available models: " + _modelPools.keySet());
		}

		// Borrow a new model from the pool
		final ChatModel newModel;
		try {
			newModel = pool.borrowObject();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to borrow chat model from pool for model '" +
				effectiveModelName + "': " + ex.getMessage(), ex);
		}

		// Store in interaction context
		contextModels.put(effectiveModelName, newModel);

		return newModel;
	}

	/**
	 * Returns the {@link ChatModelFactory} responsible for the given model name.
	 *
	 * <p>
	 * This allows callers to access the factory configuration (e.g. API keys, base URL) for
	 * building customized model instances outside the pool.
	 * </p>
	 *
	 * @param modelName
	 *        The model name.
	 * @return The factory, or {@code null} if no factory is registered for the given model.
	 */
	public ChatModelFactory getFactory(String modelName) {
		return _factories.get(modelName);
	}

	/**
	 * Returns the names of all available models.
	 *
	 * <p>
	 * These are the model names that can be passed to {@link #getChatModel(String)}. The returned
	 * list is immutable and maintains a consistent order across calls.
	 * </p>
	 *
	 * @return An immutable list of all configured model names.
	 */
	public List<String> getModelNames() {
		return _modelNames;
	}

	/**
	 * Module for {@link AIService}.
	 */
	public static final class Module extends TypedRuntimeModule<AIService> {

		/**
		 * Singleton {@link AIService.Module} instance.
		 */
		public static final AIService.Module INSTANCE = new AIService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<AIService> getImplementation() {
			return AIService.class;
		}
	}
}
