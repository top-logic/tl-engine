/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.container.ConfigPart;
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
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentConfig;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentSettings;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryConfig;
import com.top_logic.tl.ai.core.agent.environment.UserMemorySettings;
import com.top_logic.tl.ai.core.security.DefaultSecurityContextFactory;
import com.top_logic.tl.ai.core.security.SecurityContextFactory;
import com.top_logic.tl.ai.core.provider.catalog.ModelCatalogEntry;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelsDevFetcher;
import com.top_logic.tl.ai.core.tools.model.ObjectIdConfig;
import com.top_logic.tl.ai.core.tools.model.ObjectIdSettings;

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
		AgentOverride.PROVIDER,
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
	public interface AgentOverride extends NamedConfigMandatory, ConfigPart {
		/** @see #getModel() */
		String MODEL = "model";

		/** @see #getProvider() */
		String PROVIDER = "provider";

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
		@Options(fun = ServiceModelOptions.class)
		@Constraint(value = ModelToolCallConstraint.class, args = @Ref(NAME_ATTRIBUTE))
		String getModel();

		/**
		 * The provider serving this agent's model, or empty when only one provider exposes it.
		 *
		 * <p>
		 * Name a provider when two provider entries expose the same model - separate accounts, or
		 * an official endpoint next to a gateway. Left empty for a model exposed twice, the
		 * service refuses to start. An agent inheriting the default model inherits the default
		 * provider.
		 * </p>
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@Nullable
		@Options(fun = ServiceProviderOptions.class)
		@Constraint(value = ProviderExposesModelConstraint.class, args = @Ref(MODEL))
		String getProvider();

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
		@DynamicMode(fun = ProviderFieldMode.AnthropicFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
		String getThinkingEffort();

		/**
		 * Anthropic top-k parameter, or {@code null}.
		 *
		 * @see #TOP_K
		 */
		@Label("Top-K")
		@Name(TOP_K)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.AnthropicFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
		Integer getTopK();

		/**
		 * <code>OpenAI</code>/<code>xAI</code> reasoning effort (low/medium/high), or {@code null}.
		 *
		 * @see #REASONING_EFFORT
		 */
		@Name(REASONING_EFFORT)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.OpenAIFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
		String getReasoningEffort();

		/**
		 * <code>OpenAI</code>/<code>xAI</code> reasoning summary (auto/detailed), or {@code null}.
		 *
		 * @see #REASONING_SUMMARY
		 */
		@Name(REASONING_SUMMARY)
		@Nullable
		@DynamicMode(fun = ProviderFieldMode.OpenAIFieldMode.class, args = { @Ref(MODEL), @Ref(PROVIDER) })
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
	 *
	 * <p>
	 * Derives the options from the {@link Config#getProviders() provider section} of the
	 * configuration currently being edited, so a model added to a provider becomes selectable
	 * immediately — without saving and restarting the service first.
	 * </p>
	 */
	public static class ConfiguredModelOptions
			extends Function1<List<String>, Map<String, LLMProviderConfig.Config>> {
		@Override
		public List<String> apply(Map<String, LLMProviderConfig.Config> providers) {
			return modelNamesOf(providers);
		}
	}

	/**
	 * Provides model names from the running {@link LLMService} instance.
	 *
	 * <p>
	 * Unlike {@link ConfiguredModelOptions}, this does not require a {@link Container @Container}
	 * chain to resolve the provider map. This makes it suitable for contexts where the container
	 * reference is not yet set, such as during creation of a new {@link AgentOverride} entry in a
	 * keyed map.
	 * </p>
	 */
	public static class ServiceModelOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!LLMService.Module.INSTANCE.isActive()) {
				return List.of();
			}
			return LLMService.getInstance().getConfiguredModelNames();
		}
	}

	/**
	 * Provides all configured provider names for selection in the admin UI.
	 *
	 * <p>
	 * Derives the options from the {@link Config#getProviders() provider section} of the
	 * configuration currently being edited, so a newly added provider becomes selectable without
	 * saving and restarting the service first.
	 * </p>
	 */
	public static class ConfiguredProviderOptions
			extends Function1<List<String>, Map<String, LLMProviderConfig.Config>> {
		@Override
		public List<String> apply(Map<String, LLMProviderConfig.Config> providers) {
			if (providers == null) {
				return List.of();
			}
			return List.copyOf(providers.keySet());
		}
	}

	/**
	 * Provides provider names from the running {@link LLMService} instance.
	 *
	 * <p>
	 * The {@link Container @Container}-free counterpart of {@link ConfiguredProviderOptions}, for
	 * contexts where the container reference is not yet set - such as creating a new
	 * {@link AgentOverride} entry in a keyed map.
	 * </p>
	 */
	public static class ServiceProviderOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!LLMService.Module.INSTANCE.isActive()) {
				return List.of();
			}
			return LLMService.getInstance().getConfiguredProviderNames();
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
		AgentExecutionConfig.DEFAULT_PROVIDER,
		AgentExecutionConfig.AGENT_OVERRIDES,
	})
	public interface AgentExecutionConfig extends ConfigPart {

		/** Configuration property name for {@link #getChatEntryPoint()}. */
		String CHAT_ENTRY_POINT = "chat-entry-point";

		/** Configuration property name for {@link #getDefaultModel()}. */
		String DEFAULT_MODEL = "default-model";

		/** Configuration property name for {@link #getDefaultProvider()}. */
		String DEFAULT_PROVIDER = "default-provider";

		/** Configuration property name for {@link #getAgentOverrides()}. */
		String AGENT_OVERRIDES = "agent-overrides";

		/** @see #getOwner() */
		String OWNER = "owner";

		/**
		 * The enclosing service {@link Config}, used to resolve configured models from its
		 * {@link Config#getProviders() provider section} within the same edit form.
		 *
		 * @see #OWNER
		 */
		@Name(OWNER)
		@Hidden
		@Container
		Config<?> getOwner();

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
		@StringDefault("TL_SingleAgent")
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
		@Options(fun = ConfiguredModelOptions.class, args = @Ref(steps = {
			@Step(OWNER),
			@Step(Config.PROVIDERS),
		}))
		String getDefaultModel();

		/**
		 * The provider serving the default model, or empty when only one provider exposes it.
		 *
		 * <p>
		 * Applies to every agent that has no provider of its own. Left empty for a default model
		 * exposed by two providers, the service refuses to start rather than pick one by
		 * configuration order.
		 * </p>
		 *
		 * @see #DEFAULT_PROVIDER
		 */
		@Name(DEFAULT_PROVIDER)
		@Nullable
		@Options(fun = ConfiguredProviderOptions.class, args = @Ref(steps = {
			@Step(OWNER),
			@Step(Config.PROVIDERS),
		}))
		@Constraint(value = ProviderExposesModelConstraint.class, args = @Ref(DEFAULT_MODEL))
		String getDefaultProvider();

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
		Config.ENVIRONMENT,
		Config.SECURITY_CONTEXT,
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
		 * Configuration property name for the environment section.
		 *
		 * @see #getEnvironment()
		 */
		String ENVIRONMENT = "environment";

		/**
		 * Configuration property name for the user-memory section.
		 *
		 * @see #getUserMemory()
		 */
		String USER_MEMORY = "user-memory";

		/**
		 * Configuration property name for the object-ids section.
		 *
		 * @see #getObjectIds()
		 */
		String OBJECT_IDS = "object-ids";

		/**
		 * Configuration property name for the security-context factory.
		 *
		 * @see #getSecurityContext()
		 */
		String SECURITY_CONTEXT = "security-context";

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
		 * What every agent run is told about the situation it runs in: the wording of the environment
		 * block, the application name the model is given, and whether the accounts' own preference
		 * texts are part of it.
		 *
		 * @see #ENVIRONMENT
		 */
		@Name(ENVIRONMENT)
		EnvironmentConfig getEnvironment();

		/**
		 * What an agent may remember about the accounts of this installation: whether it may at
		 * all, how much of it reaches a model, and the words the agreement check looks for.
		 *
		 * @see #USER_MEMORY
		 */
		@Name(USER_MEMORY)
		UserMemoryConfig getUserMemory();

		/**
		 * What this application knows about the identifiers a model passes to a tool: the codes its
		 * own users speak, and what to tell a model that passed one instead of an object id.
		 *
		 * @see #OBJECT_IDS
		 */
		@Name(OBJECT_IDS)
		ObjectIdConfig getObjectIds();

		/**
		 * What decides, for every agent run, which tool calls need a person to confirm them.
		 *
		 * <p>
		 * The shipped default asks the tool's mode: a read runs, everything that changes something is
		 * confirmed. An application whose answer depends on what is being changed - overwriting an
		 * empty section is not overwriting what a colleague wrote - names its own implementation
		 * here.
		 * </p>
		 *
		 * @see #SECURITY_CONTEXT
		 */
		@Name(SECURITY_CONTEXT)
		@ItemDefault
		@ImplementationClassDefault(DefaultSecurityContextFactory.class)
		PolymorphicConfiguration<SecurityContextFactory> getSecurityContext();

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
	 * Index mapping each model name to the names of the providers exposing it, in configuration
	 * order.
	 *
	 * <p>
	 * A model exposed by two provider entries has both of them here - neither is dropped. Which of
	 * them serves a given agent is decided by that agent's {@link AgentOverride#getProvider()
	 * provider}, never by iteration order; an unqualified reference to such a model is refused at
	 * {@link #startUp()}.
	 * </p>
	 */
	private Map<String, List<String>> _modelProviders;

	/**
	 * Named provider configs, keyed by the provider config name.
	 */
	private Map<String, LLMProviderConfig> _providerConfigs;

	/**
	 * Index mapping model IDs to their resolved {@link ModelInfo} metadata.
	 */
	private Map<String, ModelInfo> _models;

	/**
	 * What builds the security context of every run started while this service is up.
	 */
	private SecurityContextFactory _securityContextFactory;

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

	/**
	 * What every agent run is told about the situation it runs in.
	 *
	 * @see EnvironmentSettings
	 */
	public EnvironmentConfig getEnvironmentConfig() {
		return getConfig().getEnvironment();
	}

	/**
	 * What an agent may remember about the accounts of this installation.
	 *
	 * @see UserMemorySettings
	 */
	public UserMemoryConfig getUserMemoryConfig() {
		return getConfig().getUserMemory();
	}

	/**
	 * What this application knows about the identifiers a model passes to a tool.
	 *
	 * @see ObjectIdSettings
	 */
	public ObjectIdConfig getObjectIdConfig() {
		return getConfig().getObjectIds();
	}

	/**
	 * What builds the {@link com.top_logic.tl.ai.core.agent.SecurityContext} of an agent run.
	 *
	 * <p>
	 * Asked by every entry point that starts one, through
	 * {@link com.top_logic.tl.ai.core.security.RunSecurity#contextFor}.
	 * </p>
	 *
	 * @return The configured factory, never <code>null</code>.
	 */
	public SecurityContextFactory getSecurityContextFactory() {
		return _securityContextFactory;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
		ModelDiscoveryHelper.clearCache();

		// Resolved once: a run must not pay for instantiating it, and an application that configured a
		// broken one must learn about it when the service starts rather than at the first tool call.
		SecurityContextFactory securityContextFactory = _context.getInstance(getConfig().getSecurityContext());
		_securityContextFactory = securityContextFactory == null
			? DefaultSecurityContextFactory.INSTANCE : securityContextFactory;

		Map<String, LLMProviderConfig> providerConfigs = new LinkedHashMap<>();
		Map<String, List<String>> modelProviders = new LinkedHashMap<>();

		for (Map.Entry<String, LLMProviderConfig.Config> entry : getConfig().getProviders().entrySet()) {
			String providerName = entry.getKey();
			LLMProviderConfig providerConfig = _context.getInstance(entry.getValue());
			providerConfigs.put(providerName, providerConfig);

			for (String model : providerConfig.getConfiguredModels()) {
				List<String> exposedBy = modelProviders.computeIfAbsent(model, name -> new ArrayList<>());
				if (!exposedBy.contains(providerName)) {
					exposedBy.add(providerName);
				}
			}
		}

		_providerConfigs = Map.copyOf(providerConfigs);
		Map<String, List<String>> index = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : modelProviders.entrySet()) {
			index.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		_modelProviders = Map.copyOf(index);

		refuseUnroutableReferences();

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
			_modelProviders = null;
			_providerConfigs = null;
			_models = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Refuses the start when a configured model reference cannot be routed to exactly one
	 * provider.
	 *
	 * <p>
	 * Reported to the {@link InstantiationContext} so that a full application boot lists every
	 * problem at once, and thrown so that this start fails whoever asked for it. The context alone
	 * is not enough: {@link TypedRuntimeModule} tests it for errors <em>before</em> this method
	 * runs, so at boot the refusal would only surface as the next service's failure, and an
	 * in-app restart of this service alone would report nothing and leave an unroutable
	 * configuration running.
	 * </p>
	 */
	private void refuseUnroutableReferences() {
		List<String> refusals =
			refusals(getConfig().getAgentExecution(), _modelProviders, _providerConfigs.keySet());
		if (refusals.isEmpty()) {
			return;
		}
		for (String refusal : refusals) {
			_context.error(refusal);
		}
		throw new ConfigurationError(ResKey.text("The agent model configuration cannot be routed to"
			+ " providers, so this service is not running: " + String.join(" ", refusals)));
	}

	/**
	 * Everything about the given agent execution configuration that must stop the service from
	 * starting.
	 *
	 * <p>
	 * Two things do: a reference to a provider that is not configured, and an unqualified
	 * reference to a model that two providers expose. The second is a refusal rather than a
	 * warning because there is no answer to give - the operator listed the model twice to keep the
	 * two endpoints apart, and picking either one by configuration order would be guessing which
	 * account, key or rate limit the agent was meant to use.
	 * </p>
	 *
	 * <p>
	 * A model that <em>no</em> provider exposes is deliberately not refused here: that stays the
	 * runtime error of {@link #buildProvider(String, String)}, because the model catalog can be
	 * completed after the start and an agent nobody runs should not stop the application.
	 * </p>
	 *
	 * @param execution
	 *        The agent execution section to inspect.
	 * @param modelProviders
	 *        Model name to the names of the providers exposing it.
	 * @param providerNames
	 *        The names of all configured providers.
	 * @return One sentence per problem, in configuration order; empty for a routable
	 *         configuration.
	 */
	static List<String> refusals(AgentExecutionConfig execution, Map<String, List<String>> modelProviders,
			Set<String> providerNames) {
		List<String> result = new ArrayList<>();
		String defaultModel = execution.getDefaultModel();
		String defaultProvider = execution.getDefaultProvider();

		addRefusal(result, "The default model", modelRefOf(null, defaultModel, defaultProvider),
			modelProviders, providerNames);
		for (Map.Entry<String, AgentOverride> entry : execution.getAgentOverrides().entrySet()) {
			addRefusal(result, "Agent '" + entry.getKey() + "'",
				modelRefOf(entry.getValue(), defaultModel, defaultProvider), modelProviders, providerNames);
		}
		return result;
	}

	private static void addRefusal(List<String> refusals, String subject, ModelRef ref,
			Map<String, List<String>> modelProviders, Set<String> providerNames) {
		String model = ref.model();
		if (model == null || model.isBlank()) {
			// No model to route; reported by the resolution itself when the agent runs.
			return;
		}
		List<String> exposedBy = modelProviders.getOrDefault(model, List.of());
		String provider = ref.provider();
		if (provider != null) {
			if (!providerNames.contains(provider)) {
				refusals.add(subject + ": the provider '" + provider + "' named for model '" + model
					+ "' is not configured; configured providers are " + providerNames + ".");
			} else if (!exposedBy.contains(provider)) {
				refusals.add(subject + ": the provider '" + provider + "' does not expose model '" + model
					+ "'; it exposes " + modelsOf(provider, modelProviders) + ".");
			}
		} else if (exposedBy.size() > 1) {
			refusals.add(subject + ": model '" + model + "' is exposed by " + exposedBy
				+ ", so one of them must be named as the provider.");
		}
	}

	/**
	 * The models the given provider exposes, for a refusal message.
	 */
	private static List<String> modelsOf(String providerName, Map<String, List<String>> modelProviders) {
		List<String> result = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : modelProviders.entrySet()) {
			if (entry.getValue().contains(providerName)) {
				result.add(entry.getKey());
			}
		}
		return result;
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
		return modelNamesOf(getConfig().getProviders());
	}

	/**
	 * Collects the explicitly configured model names from the given provider map.
	 *
	 * @param providers
	 *        The provider configurations keyed by name, or {@code null}.
	 * @return An immutable list of the model names listed in each provider's
	 *         {@link LLMProviderConfig.Config#getModels() allowlist}, de-duplicated in iteration
	 *         order.
	 */
	static List<String> modelNamesOf(Map<String, LLMProviderConfig.Config> providers) {
		if (providers == null) {
			return List.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (LLMProviderConfig.Config config : providers.values()) {
			List<String> models = config.getModels();
			if (models != null) {
				result.addAll(models);
			}
		}
		return List.copyOf(result);
	}

	/**
	 * The names of all configured providers, in configuration order.
	 */
	public List<String> getConfiguredProviderNames() {
		return List.copyOf(getConfig().getProviders().keySet());
	}

	/**
	 * The names of the providers exposing the given model, in configuration order.
	 *
	 * <p>
	 * More than one entry means the model must be referenced with a provider - see
	 * {@link AgentOverride#getProvider()}.
	 * </p>
	 *
	 * @param modelName
	 *        The model to look up.
	 * @return The provider names; empty if no configured provider exposes the model.
	 */
	public List<String> getProvidersOf(String modelName) {
		if (_modelProviders == null) {
			return List.of();
		}
		return _modelProviders.getOrDefault(modelName, List.of());
	}

	/**
	 * Builds an {@link LLMProvider} for an unqualified model reference.
	 *
	 * @see #buildProvider(String, String)
	 */
	public LLMProvider buildProvider(String modelName) {
		return buildProvider(modelName, null);
	}

	/**
	 * Builds an {@link LLMProvider} for the given model, served by the named provider.
	 *
	 * @param modelName
	 *        The model name to find a provider for.
	 * @param providerName
	 *        The configured provider that must serve the model, or {@code null} to let the
	 *        model's only provider serve it.
	 * @return A ready-to-use provider.
	 * @throws IllegalStateException
	 *         if the model cannot be routed to exactly one configured provider.
	 */
	public LLMProvider buildProvider(String modelName, String providerName) {
		LLMProviderConfig providerConfig = lookupProvider(modelName, providerName);
		if (providerConfig == null) {
			throw new IllegalStateException(unroutable(modelName, providerName));
		}
		return providerConfig.createProvider();
	}

	/**
	 * The provider configuration serving the given model reference, or {@code null} if the
	 * reference does not resolve to exactly one provider.
	 *
	 * @param modelName
	 *        The model name.
	 * @param providerName
	 *        The provider qualifying the model, or {@code null} for an unqualified reference.
	 */
	private LLMProviderConfig lookupProvider(String modelName, String providerName) {
		if (_providerConfigs == null || modelName == null) {
			return null;
		}
		List<String> exposedBy = getProvidersOf(modelName);
		if (providerName != null && !providerName.isBlank()) {
			// An allowlist that is bypassed is not an allowlist: a provider only serves the models
			// it exposes, however it was named.
			return exposedBy.contains(providerName) ? _providerConfigs.get(providerName) : null;
		}
		if (exposedBy.size() != 1) {
			return null;
		}
		return _providerConfigs.get(exposedBy.get(0));
	}

	/**
	 * Why the given model reference does not resolve to exactly one provider.
	 */
	private String unroutable(String modelName, String providerName) {
		List<String> exposedBy = getProvidersOf(modelName);
		if (providerName != null && !providerName.isBlank()) {
			if (_providerConfigs == null || !_providerConfigs.containsKey(providerName)) {
				return "No provider named '" + providerName + "' is configured for model '" + modelName
					+ "'. Configured providers: " + getConfiguredProviderNames();
			}
			return "Provider '" + providerName + "' does not expose model '" + modelName
				+ "'. It exposes: " + modelsOf(providerName, _modelProviders) + ".";
		}
		if (exposedBy.isEmpty()) {
			return "No provider configured for model: " + modelName
				+ ". Available models: " + (_modelProviders == null ? List.of() : _modelProviders.keySet());
		}
		return "Model '" + modelName + "' is exposed by the providers " + exposedBy
			+ ". Name one of them to select the provider.";
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
	 * Returns the catalog provider ID for an unqualified model reference, or {@code null} if it
	 * cannot be determined.
	 *
	 * <p>
	 * Two provider entries for the same vendor - the case this exists for, two Anthropic accounts
	 * exposing the same model - agree on their
	 * {@link LLMProviderConfig#getCatalogProviderId() catalog provider ID}, so the ID is
	 * unambiguous even where the provider <em>instance</em> is not. Only providers that disagree
	 * make the answer {@code null}.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to look up.
	 * @return The catalog provider ID (e.g. "anthropic", "openai"), or {@code null}.
	 */
	public String getProviderId(String modelName) {
		if (_providerConfigs == null) {
			return null;
		}
		String result = null;
		boolean first = true;
		for (String providerName : getProvidersOf(modelName)) {
			String catalogId = _providerConfigs.get(providerName).getCatalogProviderId();
			if (first) {
				result = catalogId;
				first = false;
			} else if (!Objects.equals(result, catalogId)) {
				return null;
			}
		}
		return result;
	}

	/**
	 * Returns the catalog provider ID of the given model reference, or {@code null} if the
	 * reference does not resolve to exactly one provider.
	 *
	 * @param modelName
	 *        The model name to look up.
	 * @param providerName
	 *        The provider qualifying the model, or {@code null} for an unqualified reference.
	 * @return The catalog provider ID (e.g. "anthropic", "openai"), or {@code null}.
	 */
	public String getProviderId(String modelName, String providerName) {
		if (providerName == null || providerName.isBlank()) {
			return getProviderId(modelName);
		}
		LLMProviderConfig providerConfig = lookupProvider(modelName, providerName);
		return providerConfig == null ? null : providerConfig.getCatalogProviderId();
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
	 * Returns the provider serving the {@link #getDefaultModel() default model}, or {@code null}
	 * if none is named.
	 */
	public String getDefaultProvider() {
		return blankToNull(getConfig().getAgentExecution().getDefaultProvider());
	}

	/**
	 * A model together with the provider that serves it.
	 *
	 * @param model
	 *        The model name, or {@code null} if none is configured.
	 * @param provider
	 *        The name of the provider serving the model, or {@code null} for an unqualified
	 *        reference that the model's only provider serves.
	 */
	record ModelRef(String model, String provider) {
		// A pair, nothing more.
	}

	/**
	 * The model and provider the given override effectively uses.
	 *
	 * <p>
	 * Both fields fall back to the defaults independently, with one tie: an override that names no
	 * model of its own inherits the default provider along with the default model, since a
	 * provider for a model the override does not mention would be meaningless.
	 * </p>
	 *
	 * @param override
	 *        The agent's override, or {@code null} for an agent that has none.
	 * @param defaultModel
	 *        The configured default model.
	 * @param defaultProvider
	 *        The configured default provider, or {@code null}.
	 */
	static ModelRef modelRefOf(AgentOverride override, String defaultModel, String defaultProvider) {
		if (override == null) {
			return new ModelRef(defaultModel, blankToNull(defaultProvider));
		}
		String model = blankToNull(override.getModel());
		String provider = blankToNull(override.getProvider());
		if (model == null) {
			return new ModelRef(defaultModel, provider != null ? provider : blankToNull(defaultProvider));
		}
		return new ModelRef(model, provider);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
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
		return resolveModelAssignment(agentName).model();
	}

	/**
	 * Resolves the {@link ModelRef} of the given override, without checking that it names a model.
	 */
	private ModelRef modelRefOf(AgentOverride override) {
		AgentExecutionConfig execution = getConfig().getAgentExecution();
		return modelRefOf(override, execution.getDefaultModel(), execution.getDefaultProvider());
	}

	private static ModelRef requireModel(String agentName, ModelRef ref) {
		if (ref.model() == null || ref.model().isBlank()) {
			throw new IllegalStateException(
				"No model assigned to agent '" + agentName + "' and no default model configured.");
		}
		return ref;
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
		ModelRef ref = requireModel(agentName, modelRefOf(perAgent));
		if (perAgent != null) {
			return new AgentModelAssignment(ref.model(), ref.provider(),
				perAgent.getMaxTokens(), perAgent.getContextWindow(), perAgent.getMaxTurns(),
				perAgent.getTemperature(), perAgent.getTopP(),
				perAgent.getThinkingEffort(), perAgent.getTopK(),
				perAgent.getReasoningEffort(), perAgent.getReasoningSummary(),
				perAgent.getToolChoice(),
				perAgent.getTimeoutSeconds(), perAgent.getMaxRetries(),
				perAgent.getMaxToolResultChars());
		}
		return new AgentModelAssignment(ref.model(), ref.provider(),
			null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Derives the catalog-provider-ID to model-ID mapping from configured providers.
	 */
	private Map<String, Set<String>> buildCatalogProviderModels() {
		Map<String, Set<String>> result = new LinkedHashMap<>();
		for (LLMProviderConfig providerConfig : _providerConfigs.values()) {
			String catalogId = providerConfig.getCatalogProviderId();
			if (catalogId == null || catalogId.isBlank()) {
				continue;
			}
			// Every provider contributes its own allowlist: a model two providers expose is
			// requested once per catalog ID, and neither provider's models go unrequested.
			result.computeIfAbsent(catalogId, id -> new LinkedHashSet<>())
				.addAll(providerConfig.getConfiguredModels());
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
