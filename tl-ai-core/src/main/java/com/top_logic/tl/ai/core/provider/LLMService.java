/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.provider.catalog.ModelCatalogEntry;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelsDevFetcher;

/**
 * Central service for LLM provider management, model resolution, and agent execution
 * configuration.
 *
 * <p>
 * The service configuration is organized into three sections:
 * </p>
 * <ol>
 * <li><b>Providers</b> — connection details for each LLM provider (API keys, base URLs, model
 * allowlists). Each provider entry defines how to reach a specific LLM API endpoint and which
 * models are enabled.</li>
 * <li><b>Agent Execution</b> — configures the chat entry-point agent, the default model used by
 * all agents, and optional per-agent overrides for model selection and execution parameters
 * (temperature, max tokens, timeouts, reasoning effort, etc.).</li>
 * <li><b>Model Catalog</b> — read-only metadata describing each model's capabilities (tool
 * calling, reasoning, temperature support) and limits (context window, max output tokens).
 * Populated automatically from the <a href="https://models.dev">models.dev</a> remote catalog on
 * startup, with optional manual overrides for local or unlisted models.</li>
 * </ol>
 *
 * @see LLMProviderConfig
 * @see AgentFactory
 */
public class LLMService extends ConfiguredManagedClass<LLMService.Config<?>> {

	/**
	 * Per-agent override for model selection and execution parameters.
	 *
	 * <p>
	 * Each entry overrides the {@link AgentExecutionConfig#getDefaultModel() default model} for a
	 * specific agent and optionally configures execution parameters (max turns, timeouts, tool
	 * choice) and model-specific settings (temperature, reasoning effort). Parameters left
	 * {@code null} inherit defaults from the model catalog or system defaults.
	 * </p>
	 */
	@DisplayOrder({
		NamedConfigMandatory.NAME_ATTRIBUTE,
		AgentOverride.MODEL,
		AgentOverride.CONTEXT_WINDOW,
		AgentOverride.MAX_TOKENS,
		AgentOverride.MAX_TURNS,
		AgentOverride.MAX_TOOL_RESULT_CHARS,
		AgentOverride.TOOL_CHOICE,
		AgentOverride.TIMEOUT_SECONDS,
		AgentOverride.MAX_RETRIES,
		AgentOverride.TEMPERATURE,
		AgentOverride.TOP_P,
		AgentOverride.THINKING_EFFORT,
		AgentOverride.TOP_K,
		AgentOverride.REASONING_EFFORT,
		AgentOverride.REASONING_SUMMARY,
	})
	public interface AgentOverride extends NamedConfigMandatory {
		/** @see #getModel() */
		String MODEL = "model";

		/** @see #getMaxTokens() */
		String MAX_TOKENS = "max-tokens";

		/** @see #getContextWindow() */
		String CONTEXT_WINDOW = "context-window";

		/** @see #getMaxTurns() */
		String MAX_TURNS = "max-turns";

		/** @see #getTemperature() */
		String TEMPERATURE = "temperature";

		/** @see #getTopP() */
		String TOP_P = "top-p";

		/** @see #getThinkingEffort() */
		String THINKING_EFFORT = "thinking-effort";

		/** @see #getTopK() */
		String TOP_K = "top-k";

		/** @see #getReasoningEffort() */
		String REASONING_EFFORT = "reasoning-effort";

		/** @see #getReasoningSummary() */
		String REASONING_SUMMARY = "reasoning-summary";

		/** @see #getToolChoice() */
		String TOOL_CHOICE = "tool-choice";

		/** @see #getTimeoutSeconds() */
		String TIMEOUT_SECONDS = "timeout-seconds";

		/** @see #getMaxRetries() */
		String MAX_RETRIES = "max-retries";

		/** @see #getMaxToolResultChars() */
		String MAX_TOOL_RESULT_CHARS = "max-tool-result-chars";

		@Override
		@Options(fun = AllAgentNameOptions.class)
		String getName();

		/**
		 * The model to use for this agent.
		 *
		 * @see #MODEL
		 */
		@Name(MODEL)
		@Options(fun = AllConfiguredModelOptions.class)
		@Constraint(value = ModelToolCallConstraint.class, args = @Ref(NAME_ATTRIBUTE))
		String getModel();

		/**
		 * Maximum output tokens per LLM call, or {@code null} for catalog default.
		 *
		 * @see #MAX_TOKENS
		 */
		@Name(MAX_TOKENS)
		@Nullable
		Integer getMaxTokens();

		/**
		 * Context window size override, or {@code null} for catalog default.
		 *
		 * @see #CONTEXT_WINDOW
		 */
		@Name(CONTEXT_WINDOW)
		@Nullable
		Integer getContextWindow();

		/**
		 * Maximum agent loop turns, or {@code null} for default.
		 *
		 * @see #MAX_TURNS
		 */
		@Name(MAX_TURNS)
		@Nullable
		Integer getMaxTurns();

		/**
		 * Sampling temperature, or {@code null} for default.
		 *
		 * @see #TEMPERATURE
		 */
		@Name(TEMPERATURE)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.TemperatureFieldMode.class, args = @Ref(MODEL))
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 0.0)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = 1.0)
		Double getTemperature();

		/**
		 * Top-p sampling parameter, or {@code null} for default.
		 *
		 * @see #TOP_P
		 */
		@Label("Top-P")
		@Name(TOP_P)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.TemperatureFieldMode.class, args = @Ref(MODEL))
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 0.0)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = 1.0)
		Double getTopP();

		/**
		 * Anthropic thinking effort (low/medium/high/max), or {@code null}.
		 *
		 * @see #THINKING_EFFORT
		 */
		@Name(THINKING_EFFORT)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.AnthropicFieldMode.class, args = @Ref(MODEL))
		String getThinkingEffort();

		/**
		 * Anthropic top-k parameter, or {@code null}.
		 *
		 * @see #TOP_K
		 */
		@Label("Top-K")
		@Name(TOP_K)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.AnthropicFieldMode.class, args = @Ref(MODEL))
		Integer getTopK();

		/**
		 * <code>OpenAI</code>/<code>xAI</code> reasoning effort (low/medium/high), or {@code null}.
		 *
		 * @see #REASONING_EFFORT
		 */
		@Name(REASONING_EFFORT)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.OpenAIFieldMode.class, args = @Ref(MODEL))
		String getReasoningEffort();

		/**
		 * <code>OpenAI</code>/<code>xAI</code> reasoning summary (auto/detailed), or {@code null}.
		 *
		 * @see #REASONING_SUMMARY
		 */
		@Name(REASONING_SUMMARY)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.OpenAIFieldMode.class, args = @Ref(MODEL))
		String getReasoningSummary();

		/**
		 * Tool choice (AUTO/REQUIRED/NONE), or {@code null} for provider default.
		 *
		 * @see #TOOL_CHOICE
		 */
		@Name(TOOL_CHOICE)
		@Nullable
		String getToolChoice();

		/**
		 * Request timeout in seconds, or {@code null} for default.
		 *
		 * @see #TIMEOUT_SECONDS
		 */
		@Name(TIMEOUT_SECONDS)
		@Nullable
		Integer getTimeoutSeconds();

		/**
		 * Maximum retries on transient errors, or {@code null} for default.
		 *
		 * @see #MAX_RETRIES
		 */
		@Name(MAX_RETRIES)
		@Nullable
		Integer getMaxRetries();

		/**
		 * Maximum characters per tool result, or {@code null} for default.
		 *
		 * @see #MAX_TOOL_RESULT_CHARS
		 */
		@Name(MAX_TOOL_RESULT_CHARS)
		@Nullable
		Integer getMaxToolResultChars();
	}

	/**
	 * Provides all available agent names for selection in the admin UI.
	 */
	public static class AllAgentNameOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
				return List.of();
			}
			List<String> names = new ArrayList<>();
			for (AgentDefinition ad : AgentDefinitionQuery.getInstance().getAllAgents()) {
				names.add(ad.name());
			}
			return names;
		}
	}

	/**
	 * Provides all configured model names for selection in the admin UI.
	 */
	public static class AllConfiguredModelOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			LLMService service = getInstance();
			if (service == null) {
				return List.of();
			}
			return service.getConfiguredModelNames();
		}
	}

	/**
	 * Agent execution configuration: chat entry point, default model, and per-agent overrides.
	 *
	 * <p>
	 * Agent definitions (identity, system prompt, tools, delegations) are managed by
	 * {@link TopLogicAgentDefinitions} and the knowledge base. This section controls the operational
	 * side: which agent serves as the chat entry point, which model to use by default, and
	 * per-agent overrides for model selection and execution parameters.
	 * </p>
	 */
	@DisplayOrder({
		AgentExecutionConfig.CHAT_ENTRY_POINT,
		AgentExecutionConfig.DEFAULT_MODEL,
		AgentExecutionConfig.AGENT_OVERRIDES,
	})
	public interface AgentExecutionConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getChatEntryPoint()}. */
		String CHAT_ENTRY_POINT = "chat-entry-point";

		/** Configuration property name for {@link #getDefaultModel()}. */
		String DEFAULT_MODEL = "default-model";

		/** Configuration property name for {@link #getAgentOverrides()}. */
		String AGENT_OVERRIDES = "agent-overrides";

		/**
		 * The agent that serves as the main entry point when a user opens a new chat session.
		 *
		 * <p>
		 * This agent receives the initial user message and orchestrates the conversation. It may
		 * delegate to other agents via its delegations list.
		 * </p>
		 *
		 * @see #CHAT_ENTRY_POINT
		 */
		@Name(CHAT_ENTRY_POINT)
		@StringDefault("TL_Orchestrator")
		@Options(fun = AllAgentNameOptions.class)
		String getChatEntryPoint();

		/**
		 * The fallback model used by agents that have no explicit override in
		 * {@link #getAgentOverrides()}.
		 *
		 * <p>
		 * To assign a specific model (and optional tuning parameters) to an individual agent, add
		 * an entry to the {@link #getAgentOverrides() agent overrides} map instead.
		 * </p>
		 *
		 * @see #DEFAULT_MODEL
		 */
		@Name(DEFAULT_MODEL)
		@Mandatory
		@Options(fun = AllConfiguredModelOptions.class)
		String getDefaultModel();

		/**
		 * Per-agent overrides. Agents not listed here use the {@link #getDefaultModel()
		 * default model}. Applies to all agents regardless of source (config-defined or KB).
		 *
		 * @see #AGENT_OVERRIDES
		 */
		@Name(AGENT_OVERRIDES)
		@EntryTag("agent")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, AgentOverride> getAgentOverrides();
	}

	/**
	 * Configuration interface for {@link LLMService}.
	 *
	 * <p>
	 * Organized into three sections: provider connections, agent execution (chat entry point,
	 * default model, per-agent overrides), and model catalog (read-only model metadata).
	 * </p>
	 */
	@DisplayOrder({
		Config.PROVIDERS,
		Config.AGENT_EXECUTION,
		Config.MODEL_CATALOG,
	})
	public interface Config<I extends LLMService> extends ConfiguredManagedClass.Config<I> {

		// --- Provider configuration ---

		/**
		 * Configuration property name for providers.
		 *
		 * @see #getProviders()
		 */
		String PROVIDERS = "providers";

		/**
		 * Configuration property name for agent execution.
		 *
		 * @see #getAgentExecution()
		 */
		String AGENT_EXECUTION = "agent-execution";

		/**
		 * Configuration property name for the model catalog.
		 *
		 * @see #getModelCatalog()
		 */
		String MODEL_CATALOG = "model-catalog";

		/**
		 * The configured LLM providers, keyed by provider name.
		 *
		 * <p>
		 * Each entry carries provider-specific properties.
		 * </p>
		 */
		@Name(PROVIDERS)
		@EntryTag("provider")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, LLMProviderConfig.Config> getProviders();

		/**
		 * Agent execution configuration: chat entry point, default model, and per-agent overrides
		 * for model selection and execution parameters.
		 *
		 * @see #AGENT_EXECUTION
		 */
		@Name(AGENT_EXECUTION)
		AgentExecutionConfig getAgentExecution();

		/**
		 * Model metadata catalog describing each model's capabilities and limits.
		 *
		 * <p>
		 * Populated automatically from the remote <a href="https://models.dev">models.dev</a>
		 * catalog on startup. Manual overrides can be added for models not listed in the remote
		 * catalog (e.g. local or custom-hosted models).
		 * </p>
		 *
		 * @see #MODEL_CATALOG
		 */
		@Name(MODEL_CATALOG)
		ModelCatalogConfig getModelCatalog();

	}

	/**
	 * Configuration for the model metadata catalog.
	 *
	 * <p>
	 * Provides read-only metadata about each model's capabilities (tool calling, reasoning,
	 * temperature support) and limits (context window, max output tokens). The catalog is fetched
	 * from <a href="https://models.dev">models.dev</a> on startup for all models configured in
	 * the provider section. Models not found in the remote catalog (e.g. locally hosted models)
	 * can be defined manually in the {@link #getModelOverrides() model overrides}.
	 * </p>
	 */
	public interface ModelCatalogConfig extends ConfigurationItem {

		/** Default catalog URL. */
		String DEFAULT_CATALOG_URL = "https://models.dev/api.json";

		/** @see #getCatalogUrl() */
		String CATALOG_URL = "catalog-url";

		/** @see #getModelConfiguration() */
		String MODEL_CONFIGURATION = "llm-configuration";

		/** @see #getModelOverrides() */
		String MODEL_OVERRIDES = "model-overrides";

		/**
		 * The URL of the remote model catalog.
		 *
		 * @see #CATALOG_URL
		 */
		@Name(CATALOG_URL)
		@StringDefault(DEFAULT_CATALOG_URL)
		@ReadOnly
		String getCatalogUrl();

		/**
		 * Per-model metadata (limits, capabilities).
		 *
		 * <p>
		 * Populated automatically from the remote catalog on startup. Read-only — do not
		 * modify manually.
		 * </p>
		 *
		 * @see #MODEL_CONFIGURATION
		 */
		@Name(MODEL_CONFIGURATION)
		@Label("Model metadata")
		@ReadOnly
		@EntryTag("model")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ModelCatalogEntry.Config> getModelConfiguration();

		/**
		 * Manually defined model metadata for models not in the remote catalog.
		 *
		 * <p>
		 * Use this for local models or <code>OpenAI</code>-compatible endpoints whose models are not listed
		 * on models.dev. Entries here take precedence over catalog entries with the same name.
		 * </p>
		 *
		 * @see #MODEL_OVERRIDES
		 */
		@Name(MODEL_OVERRIDES)
		@Label("Model overrides")
		@EntryTag("model")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ModelCatalogEntry.OverrideConfig> getModelOverrides();
	}

	private static volatile LLMService _instance;

	private final InstantiationContext _context;

	/**
	 * Index mapping model names to their resolved {@link LLMProviderConfig} instance.
	 */
	private Map<String, LLMProviderConfig> _modelProviderIndex;

	/**
	 * Named provider configs, keyed by the provider config name.
	 */
	private Map<String, LLMProviderConfig> _providerConfigs;

	/**
	 * Index mapping model IDs to their resolved {@link ModelInfo} metadata.
	 */
	private Map<String, ModelInfo> _models;

	private Map<String, String> _agentModels;

	/**
	 * Creates a new {@link LLMService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public LLMService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	/**
	 * Returns the singleton instance of the {@link LLMService}.
	 *
	 * @return The service instance, or {@code null} if not yet started.
	 */
	public static LLMService getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
		ModelDiscoveryHelper.clearCache();

		Map<String, LLMProviderConfig> providerConfigs = new LinkedHashMap<>();
		Map<String, LLMProviderConfig> modelIndex = new LinkedHashMap<>();

		for (Map.Entry<String, LLMProviderConfig.Config> entry : getConfig().getProviders().entrySet()) {
			String providerName = entry.getKey();
			LLMProviderConfig providerConfig = _context.getInstance(entry.getValue());
			providerConfigs.put(providerName, providerConfig);

			List<String> configured = providerConfig.getConfiguredModels();
			for (String model : configured) {
				LLMProviderConfig existing = modelIndex.putIfAbsent(model, providerConfig);
				if (existing != null && existing != providerConfig) {
					Logger.warn("Model '" + model + "' is exposed by multiple providers. Keeping the first mapping.",
						LLMService.class);
				}
			}
		}

		_providerConfigs = Map.copyOf(providerConfigs);
		_modelProviderIndex = Map.copyOf(modelIndex);

		// --- Catalog phase ---
		ModelCatalogConfig catalogConfig = getConfig().getModelCatalog();
		Map<String, Set<String>> catalogRequested = buildCatalogProviderModels();

		Map<String, ModelInfo> fetched;
		if (catalogConfig == null || catalogRequested.isEmpty()) {
			fetched = Map.of();
		} else {
			fetched = ModelsDevFetcher.fetch(catalogConfig.getCatalogUrl(), catalogRequested);
		}

		Map<String, ModelCatalogEntry.OverrideConfig> overrides;
		if (catalogConfig == null) {
			overrides = Map.of();
		} else {
			// Populate read-only config entries for admin UI visibility
			Map<String, ModelCatalogEntry.Config> catalogEntries = catalogConfig.getModelConfiguration();
			catalogEntries.clear();
			for (Map.Entry<String, ModelInfo> entry : fetched.entrySet()) {
				catalogEntries.put(entry.getKey(), ModelCatalogEntry.toConfig(entry.getKey(), entry.getValue()));
			}
			overrides = catalogConfig.getModelOverrides();
		}

		// Merge manual overrides (take precedence over catalog)
		Map<String, ModelInfo> merged = new LinkedHashMap<>(fetched);
		for (Map.Entry<String, ModelCatalogEntry.OverrideConfig> entry : overrides.entrySet()) {
			merged.put(entry.getKey(), ModelCatalogEntry.overrideToModelInfo(entry.getValue()));
		}

		_models = Map.copyOf(merged);

		AgentExecutionConfig tuningConfig = getConfig().getAgentExecution();
		Map<String, String> agentModelMap = new LinkedHashMap<>();
		for (Map.Entry<String, AgentOverride> entry : tuningConfig.getAgentOverrides().entrySet()) {
			agentModelMap.put(entry.getKey(), entry.getValue().getModel());
		}
		_agentModels = Map.copyOf(agentModelMap);

		int overrideCount = overrides.size();
		Logger.info("LLMService started with " +
			getConfig().getProviders().size() + " provider(s), " +
			fetched.size() + " model(s) from catalog" +
			(overrideCount > 0 ? ", " + overrideCount + " model override(s)" : "") + ".",
			LLMService.class);
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			_modelProviderIndex = null;
			_providerConfigs = null;
			_models = null;
			_agentModels = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Returns only the explicitly configured model names from all providers.
	 *
	 * <p>
	 * Returns only models that an admin has explicitly added to a provider's {@code models}
	 * allowlist. Models that were merely discovered via provider API are excluded.
	 * </p>
	 *
	 * @return An immutable list of explicitly configured model names.
	 */
	public List<String> getConfiguredModelNames() {
		Set<String> result = new LinkedHashSet<>();
		for (LLMProviderConfig.Config config : getConfig().getProviders().values()) {
			List<String> models = config.getModels();
			if (models != null) {
				result.addAll(models);
			}
		}
		return List.copyOf(result);
	}

	/**
	 * Builds an {@link LLMProvider} for the given model name.
	 *
	 * <p>
	 * Looks up the {@link LLMProviderConfig} whose model list contains the given model name,
	 * then delegates to {@link LLMProviderConfig#createProvider()}.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to find a provider for.
	 * @return A ready-to-use provider.
	 * @throws IllegalStateException
	 *         if no provider is configured for the given model.
	 */
	public LLMProvider buildProvider(String modelName) {
		LLMProviderConfig providerConfig = _modelProviderIndex.get(modelName);
		if (providerConfig == null) {
			throw new IllegalStateException(
				"No provider configured for model: " + modelName
					+ ". Available models: " + _modelProviderIndex.keySet());
		}
		return providerConfig.createProvider();
	}

	/**
	 * Returns metadata for the given model, or {@code null} if unknown.
	 */
	public ModelInfo getModel(String modelId) {
		if (_models == null) {
			return null;
		}
		return _models.get(modelId);
	}

	/**
	 * Returns the catalog provider ID for the given model name, or {@code null} if unknown.
	 *
	 * <p>
	 * Looks up which configured provider owns the given model and returns that provider's
	 * {@link LLMProviderConfig#getCatalogProviderId() catalog provider ID}.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to look up.
	 * @return The catalog provider ID (e.g. "anthropic", "openai"), or {@code null}.
	 */
	public String getProviderId(String modelName) {
		if (_modelProviderIndex == null) {
			return null;
		}
		LLMProviderConfig providerConfig = _modelProviderIndex.get(modelName);
		if (providerConfig == null) {
			return null;
		}
		return providerConfig.getCatalogProviderId();
	}

	/**
	 * Returns the name of the chat entry-point agent.
	 */
	public String getEntryPointAgentName() {
		return getConfig().getAgentExecution().getChatEntryPoint();
	}

	/**
	 * Returns the configured default model name, or {@code null} if not set.
	 */
	public String getDefaultModel() {
		return getConfig().getAgentExecution().getDefaultModel();
	}

	/**
	 * Resolves the model for the given agent.
	 *
	 * <p>
	 * Resolution order: per-agent assignment from the {@link AgentExecutionConfig#getAgentOverrides()
	 * agent execution config}, then {@link #getDefaultModel() default model}.
	 * </p>
	 *
	 * @param agentName
	 *        the agent name
	 * @return the resolved model identifier
	 * @throws IllegalStateException
	 *         if no model can be resolved
	 */
	public String resolveModel(String agentName) {
		if (_agentModels != null) {
			String perAgent = _agentModels.get(agentName);
			if (perAgent != null && !perAgent.isBlank()) {
				return perAgent;
			}
		}
		String defaultModel = getDefaultModel();
		if (defaultModel != null && !defaultModel.isBlank()) {
			return defaultModel;
		}
		throw new IllegalStateException(
			"No model assigned to agent '" + agentName + "' and no default model configured.");
	}

	/**
	 * Resolves the full model assignment for the given agent, including all tuning parameters.
	 *
	 * <p>
	 * Resolution order for the model name: per-agent assignment from the
	 * {@link AgentExecutionConfig#getAgentOverrides() agent execution config}, then
	 * {@link #getDefaultModel() default model}. Tuning parameters are taken from the per-agent
	 * assignment if present; otherwise all are {@code null} (meaning "use defaults").
	 * </p>
	 *
	 * @param agentName
	 *        the agent name
	 * @return the full assignment including model name and all optional tuning parameters
	 * @throws IllegalStateException
	 *         if no model can be resolved
	 *
	 * @see AgentModelAssignment
	 */
	public AgentModelAssignment resolveModelAssignment(String agentName) {
		AgentOverride perAgent = getConfig().getAgentExecution().getAgentOverrides().get(agentName);
		String model = (perAgent != null && perAgent.getModel() != null && !perAgent.getModel().isBlank())
			? perAgent.getModel() : getDefaultModel();
		if (model == null || model.isBlank()) {
			throw new IllegalStateException(
				"No model assigned to agent '" + agentName + "' and no default model configured.");
		}
		if (perAgent != null) {
			return new AgentModelAssignment(model,
				perAgent.getMaxTokens(), perAgent.getContextWindow(), perAgent.getMaxTurns(),
				perAgent.getTemperature(), perAgent.getTopP(),
				perAgent.getThinkingEffort(), perAgent.getTopK(),
				perAgent.getReasoningEffort(), perAgent.getReasoningSummary(),
				perAgent.getToolChoice(),
				perAgent.getTimeoutSeconds(), perAgent.getMaxRetries(),
				perAgent.getMaxToolResultChars());
		}
		return new AgentModelAssignment(model,
			null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Derives the catalog-provider-ID to model-ID mapping from configured providers.
	 */
	private Map<String, Set<String>> buildCatalogProviderModels() {
		Map<String, Set<String>> result = new LinkedHashMap<>();
		for (Map.Entry<String, LLMProviderConfig> entry : _providerConfigs.entrySet()) {
			LLMProviderConfig providerConfig = entry.getValue();
			String catalogId = providerConfig.getCatalogProviderId();
			if (catalogId == null || catalogId.isBlank()) {
				continue;
			}
			Set<String> models = result.computeIfAbsent(catalogId, k -> new LinkedHashSet<>());
			for (Map.Entry<String, LLMProviderConfig> modelEntry : _modelProviderIndex.entrySet()) {
				if (modelEntry.getValue() == providerConfig) {
					models.add(modelEntry.getKey());
				}
			}
		}
		return result;
	}

	/**
	 * Options provider that collects all model names configured across all provider entries.
	 *
	 * <p>
	 * Used by UI components to display available models for selection.
	 * </p>
	 */
	public static class AllowedModelOptions extends Function1<List<String>, Map<String, LLMProviderConfig.Config>> {
		@Override
		public List<String> apply(Map<String, LLMProviderConfig.Config> providers) {
			if (providers == null || providers.isEmpty()) {
				return List.of();
			}
			Set<String> allModels = new LinkedHashSet<>();
			for (LLMProviderConfig.Config config : providers.values()) {
				List<String> models = config.getModels();
				if (models != null) {
					allModels.addAll(models);
				}
			}
			return new ArrayList<>(allModels);
		}
	}

	/**
	 * Module for {@link LLMService}.
	 */
	public static final class Module extends TypedRuntimeModule<LLMService> {

		/**
		 * Singleton {@link LLMService.Module} instance.
		 */
		public static final LLMService.Module INSTANCE = new LLMService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<LLMService> getImplementation() {
			return LLMService.class;
		}
	}
}
