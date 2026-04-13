/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.ai.service.agents.background.BackgroundAgentConfig;
import com.top_logic.ai.service.agents.background.scheduler.BackgroundAgentTask;
import com.top_logic.ai.service.agents.background.trigger.ScheduledTrigger;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.ai.service.skills.Skill;
import com.top_logic.ai.service.skills.SkillConfig;
import com.top_logic.ai.service.skills.SkillRegistry;
import com.top_logic.ai.service.skills.SkillTool;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.registry.ToolRegistry;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ToolSchema;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProvider;
import com.top_logic.tl.ai.core.provider.mistral.MistralProvider;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;
import com.top_logic.tl.ai.core.tool.AnnotatedTools;
import com.top_logic.tl.ai.core.tool.Tool;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * Registry of runtime-configurable agents, skills, and LLM provider connections.
 *
 * <p>
 * Each agent is declared in XML via {@link AgentConfig} and holds its LLM model name, optional
 * thinking parameters, system prompt, tools, and skills. Skills are declared via
 * {@link SkillConfig}. LLM provider connections are configured via {@link ProviderConfig}.
 * </p>
 *
 * <p>
 * This service validates all configurations at startup, builds provider connections, and provides
 * the {@link #runAgent(String, Conversation, ConversationMemory, AgentEventListener)} method
 * to run agents using the tl-ai-core {@link AgentLoop}.
 * </p>
 *
 * @see AgentConfig
 * @see SkillConfig
 * @see ProviderConfig
 * @see AgentLoop
 */
@ServiceDependencies(Scheduler.Module.class)
public class AgentRegistry extends ConfiguredManagedClass<AgentRegistry.Config<?>> {

	/**
	 * Placeholder marker for the tool catalog in system prompts. When present, the catalog of
	 * available tools replaces this marker. Otherwise, the catalog is appended at the end.
	 */
	public static final String TOOLS_PLACEHOLDER = "<<AVAILABLE_TOOLS>>";

	/**
	 * Placeholder marker for the delegation catalog in system prompts. When present, the catalog
	 * of delegation targets replaces this marker. Otherwise, the catalog is appended at the end.
	 */
	public static final String DELEGATES_PLACEHOLDER = "<<AVAILABLE_DELEGATES>>";

	/**
	 * Maximum number of messages retained in each sub-agent's conversation memory. Uses a sliding
	 * window so that repeated calls to the same sub-agent within (or across) user messages preserve
	 * recent conversation history.
	 */
	static final int SUB_AGENT_MEMORY_MAX_MESSAGES = 20;

	/**
	 * Configuration interface for {@link AgentRegistry}.
	 */
	public interface Config<I extends AgentRegistry> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for agents.
		 *
		 * @see #getAgents()
		 */
		String AGENTS = "agents";

		/**
		 * Configuration property name for skills.
		 *
		 * @see #getSkills()
		 */
		String SKILLS = "skills";

		/**
		 * Configuration property name for providers.
		 *
		 * @see #getProviders()
		 */
		String PROVIDERS = "providers";

		/**
		 * Configuration property name for default model.
		 *
		 * @see #getDefaultModel()
		 */
		String DEFAULT_MODEL = "default-model";

		/**
		 * The configured agents, keyed by {@link AgentConfig#getName() agent name}.
		 *
		 * <p>
		 * Each entry defines one agent with its model, prompt, tools, and skills.
		 * </p>
		 */
		@Name(AGENTS)
		@DefaultContainer
		@EntryTag("agent")
		@Key(AgentConfig.NAME_ATTRIBUTE)
		Map<String, AgentConfig> getAgents();

		/**
		 * The configured skills, keyed by {@link SkillConfig#getName() skill name}.
		 *
		 * <p>
		 * Each entry defines a reusable skill with its description and prompt content. Skills
		 * are referenced by name from {@link AgentConfig#getSkills()}.
		 * </p>
		 */
		@Name(SKILLS)
		@EntryTag("skill")
		@Key(SkillConfig.NAME_ATTRIBUTE)
		Map<String, SkillConfig> getSkills();

		/**
		 * The configured LLM providers, keyed by provider name.
		 *
		 * <p>
		 * Each provider entry specifies connection details (API key, base URL) and a
		 * comma-separated list of model names it serves. When building an agent, the registry
		 * matches the agent's model name against these lists to select the correct provider.
		 * </p>
		 */
		@Name(PROVIDERS)
		@EntryTag("provider")
		@Key(ProviderConfig.NAME_ATTRIBUTE)
		Map<String, ProviderConfig> getProviders();

		/**
		 * The default model name to use when no model is explicitly specified on an agent.
		 *
		 * <p>
		 * This should match a model listed in one of the {@link #getProviders() providers}.
		 * </p>
		 */
		@Name(DEFAULT_MODEL)
		@Nullable
		String getDefaultModel();
	}

	/**
	 * Configuration for a single LLM provider.
	 *
	 * <p>
	 * Each provider has a name (e.g. {@code "anthropic"}, {@code "openai"}, {@code "mistral"}),
	 * API credentials, and a comma-separated list of model names it serves.
	 * </p>
	 */
	public interface ProviderConfig extends NamedConfigMandatory {

		/** @see #getApiKey() */
		String API_KEY = "api-key";

		/** @see #getBaseUrl() */
		String BASE_URL = "base-url";

		/** @see #getModels() */
		String MODELS = "models";

		/** @see #getVersion() */
		String VERSION = "version";

		/**
		 * The API key for authentication with the provider.
		 */
		@Name(API_KEY)
		@Mandatory
		@Encrypted
		String getApiKey();

		/**
		 * The base URL for the provider's API.
		 *
		 * <p>
		 * Optional. When not set, the default URL for the provider type is used.
		 * </p>
		 */
		@Name(BASE_URL)
		@Nullable
		String getBaseUrl();

		/**
		 * Comma-separated list of model names served by this provider.
		 *
		 * <p>
		 * Used to match agent model references to the correct provider. May be empty if the
		 * provider name alone is sufficient for matching.
		 * </p>
		 */
		@Name(MODELS)
		@Format(CommaSeparatedStrings.class)
		List<String> getModels();

		/**
		 * Provider-specific API version string.
		 *
		 * <p>
		 * Used by Anthropic to set the {@code anthropic-version} header. Ignored by other
		 * providers.
		 * </p>
		 */
		@Name(VERSION)
		@Nullable
		String getVersion();
	}

	private static volatile AgentRegistry _instance;

	private SkillRegistry _skillRegistry;

	private Map<String, String> _agentNameIndex;

	/**
	 * Index mapping model names to their provider config name. Built at startup from the
	 * {@link ProviderConfig#getModels()} lists.
	 */
	private Map<String, String> _modelProviderIndex;

	private String _entryPointAgentName;

	private List<String> _scheduledTaskNames;

	/**
	 * Creates a new {@link AgentRegistry} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public AgentRegistry(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Returns the singleton instance of the {@link AgentRegistry}.
	 *
	 * @return The service instance, or {@code null} if not yet started.
	 */
	public static AgentRegistry getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
		_agentNameIndex = buildAgentNameIndex(getConfig().getAgents());

		// Build a shared SkillRegistry from XML-configured skills
		_skillRegistry = new SkillRegistry();

		Map<String, SkillConfig> skillConfigs = getConfig().getSkills();
		if (skillConfigs != null && !skillConfigs.isEmpty()) {
			_skillRegistry.registerSkillsFromConfig(skillConfigs);
		}

		// Build model name index from provider configs
		_modelProviderIndex = buildModelProviderIndex(getConfig().getProviders());

		// Single pass over all agents: validate models, register skills, collect entry points,
		// log background agents, and register scheduled tasks.
		Scheduler scheduler = Scheduler.Module.INSTANCE.getImplementationInstance();
		List<String> entryPoints = new ArrayList<>();
		_scheduledTaskNames = new ArrayList<>();

		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			String agentName = entry.getKey();
			AgentConfig agentConfig = entry.getValue();

			// Validate model reference
			String model = agentConfig.getModel();
			if (model == null || model.isEmpty()) {
				Logger.info(
					"Agent '" + agentName + "' has no model configured. " +
						"It will use the default model when invoked.",
					AgentRegistry.class);
			} else if (!_modelProviderIndex.containsKey(model)) {
				Logger.warn(
					"Agent '" + agentName + "' references model '" + model +
						"' which is not listed in any provider. Available: " + _modelProviderIndex.keySet(),
					AgentRegistry.class);
			}

			// Register skills and validate references
			List<String> skillNames = agentConfig.getSkills();
			if (!skillNames.isEmpty()) {
				_skillRegistry.registerAgentSkills(agentName, skillNames);
				for (String skillName : skillNames) {
					try {
						_skillRegistry.getSkill(skillName);
					} catch (IllegalArgumentException ex) {
						Logger.warn(
							"Agent '" + agentName + "' references unknown skill '" + skillName + "': " +
								ex.getMessage(),
							AgentRegistry.class);
					}
				}
			}

			// Collect entry points
			if (agentConfig instanceof ChatAgentConfig chatConfig && chatConfig.isEntryPoint()) {
				entryPoints.add(agentName);
			}

			// Handle background agents
			if (agentConfig instanceof BackgroundAgentConfig bgConfig) {
				Logger.info("Background agent '" + agentName + "' registered: scope=" + bgConfig.getScope(),
					AgentRegistry.class);

				// Register scheduled tasks
				TriggerConfig trigger = bgConfig.getTrigger();
				if (trigger instanceof ScheduledTrigger scheduled) {
					String taskName = "ai-bg-" + agentName;
					BackgroundAgentTask.Config taskConfig =
						TypedConfiguration.newConfigItem(BackgroundAgentTask.Config.class);
					taskConfig.setName(taskName);
					taskConfig.setAgentName(agentName);
					taskConfig.getSchedules().addAll(scheduled.getSchedules());

					DefaultInstantiationContext instCtx = new DefaultInstantiationContext(AgentRegistry.class);
					BackgroundAgentTask task = instCtx.getInstance(taskConfig);
					scheduler.addTask(task);
					_scheduledTaskNames.add(taskName);

					Logger.info("Registered scheduler task '" + taskName + "' for background agent '" +
						agentName + "' with " + scheduled.getSchedules().size() + " schedule(s).",
						AgentRegistry.class);
				}
			}
		}

		// Validate that exactly one agent is marked as entry point
		if (entryPoints.isEmpty()) {
			throw new RuntimeException(
				"No agent is marked as entry point. Exactly one agent must have entry-point=\"true\". " +
					"Available agents: " + getConfig().getAgents().keySet());
		}
		if (entryPoints.size() > 1) {
			throw new RuntimeException(
				"Multiple agents are marked as entry point: " + entryPoints +
					". Exactly one agent must have entry-point=\"true\".");
		}
		_entryPointAgentName = entryPoints.get(0);

		Logger.info("AgentRegistry started with " + getConfig().getAgents().size() + " agent(s), " +
			getConfig().getProviders().size() + " provider(s), and " +
			skillConfigs.size() + " skill(s).",
			AgentRegistry.class);
	}

	@Override
	protected void shutDown() {
		try {
			// Unregister scheduled tasks from TopLogic Scheduler
			if (_scheduledTaskNames != null && !_scheduledTaskNames.isEmpty()) {
				Scheduler scheduler = Scheduler.Module.INSTANCE.getImplementationInstance();
				for (String taskName : _scheduledTaskNames) {
					Task task = scheduler.getTaskByName(taskName);
					if (task != null) {
						scheduler.removeTask(task);
					}
				}
			}
			_scheduledTaskNames = null;
			_instance = null;
			_skillRegistry = null;
			_agentNameIndex = null;
			_modelProviderIndex = null;
			_entryPointAgentName = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Returns the {@link AgentConfig} for the given agent name.
	 *
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @return The agent configuration.
	 * @throws RuntimeException
	 *         if no agent with the given name is configured.
	 */
	public AgentConfig getAgentConfig(String name) {
		Map<String, AgentConfig> agents = getConfig().getAgents();
		AgentConfig config = agents.get(name);
		if (config == null && name != null) {
			String matchedName = _agentNameIndex.get(name.toLowerCase(Locale.ROOT));
			if (matchedName != null) {
				config = agents.get(matchedName);
			}
		}
		if (config == null) {
			throw new RuntimeException(
				"No agent configured with name '" + name + "'. Available: " +
					agents.keySet());
		}
		return config;
	}

	/**
	 * Returns the names of all background agents matching the given trigger type and scope.
	 *
	 * @param triggerType
	 *        The trigger configuration class to match (e.g. {@code PostTurnTrigger.class}).
	 * @param scope
	 *        The scope to match (e.g.
	 *        {@link com.top_logic.ai.service.agents.background.BackgroundAgentConfig.Scope#CHAT}).
	 * @return A list of agent names with matching background configuration; never null.
	 */
	public List<String> getBackgroundAgents(
			Class<? extends TriggerConfig> triggerType, BackgroundAgentConfig.Scope scope) {
		List<String> result = new ArrayList<>();
		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			if (!(entry.getValue() instanceof BackgroundAgentConfig bgConfig)) continue;
			if (bgConfig.getScope() != scope) continue;
			TriggerConfig triggerCfg = bgConfig.getTrigger();
			if (triggerCfg != null && triggerType.isInstance(triggerCfg)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Returns the name of the agent marked as the chat entry point.
	 *
	 * @return The entry point agent name, never {@code null}.
	 */
	public String getEntryPointAgentName() {
		return _entryPointAgentName;
	}

	private static Map<String, String> buildAgentNameIndex(Map<String, AgentConfig> agents) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String agentName : agents.keySet()) {
			String normalizedName = agentName.toLowerCase(Locale.ROOT);
			String previous = result.put(normalizedName, agentName);
			if (previous != null && !previous.equals(agentName)) {
				throw new RuntimeException(
					"Ambiguous agent configuration: '" + previous + "' and '" + agentName +
						"' differ only by case. Please keep unique names ignoring case.");
			}
		}
		return result;
	}

	/**
	 * Returns the names of all models listed across all configured providers.
	 *
	 * @return An immutable list of all model names.
	 */
	public List<String> getModelNames() {
		if (_modelProviderIndex == null) {
			return List.of();
		}
		return List.copyOf(_modelProviderIndex.keySet());
	}

	/**
	 * Builds an {@link LLMProvider} for the given model name.
	 *
	 * <p>
	 * Looks up the provider configuration whose {@link ProviderConfig#getModels() models} list
	 * contains the given model name, then creates the appropriate provider implementation.
	 * </p>
	 *
	 * @param modelName
	 *        The model name to find a provider for.
	 * @return A ready-to-use provider.
	 * @throws IllegalStateException
	 *         if no provider is configured for the given model.
	 */
	public LLMProvider buildProvider(String modelName) {
		String providerName = _modelProviderIndex.get(modelName);
		if (providerName == null) {
			throw new IllegalStateException(
				"No provider configured for model: " + modelName
					+ ". Available models: " + _modelProviderIndex.keySet());
		}

		ProviderConfig providerConfig = getConfig().getProviders().get(providerName);
		return createProvider(providerName, providerConfig);
	}

	/**
	 * Runs an agent by name using the tl-ai-core {@link AgentLoop}.
	 *
	 * <p>
	 * Resolves the agent configuration, builds the provider, tool registry, and core
	 * {@link com.top_logic.tl.ai.core.agent.AgentConfig}, creates an {@link AgentLoop}, and runs
	 * it. Returns the response text extracted from the last {@link AssistantMessage}.
	 * </p>
	 *
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @param conversation
	 *        The conversation to operate on.
	 * @param conversationMemory
	 *        Optional conversation memory for context management, or {@code null}.
	 * @param listener
	 *        Receives progress events during the run.
	 * @return The response text from the agent, or empty string if no response.
	 */
	public String runAgent(String name, Conversation conversation,
			ConversationMemory conversationMemory, AgentEventListener listener) {
		AgentConfig agentConfig = getAgentConfig(name);
		return runAgentWithConfig(agentConfig, conversation, conversationMemory, listener);
	}

	/**
	 * Runs an agent with the given configuration using the tl-ai-core {@link AgentLoop}.
	 */
	private String runAgentWithConfig(AgentConfig agentConfig, Conversation conversation,
			ConversationMemory conversationMemory, AgentEventListener listener) {
		String modelName = resolveModelName(agentConfig);

		LLMProvider provider = buildProvider(modelName);
		com.top_logic.tl.ai.core.tool.ToolRegistry coreRegistry = buildCoreToolRegistry(agentConfig);

		String systemPrompt = getSystemPrompt(agentConfig);

		// Build model options from agent config
		Map<String, Object> modelOptions = buildModelOptions(agentConfig);

		Integer maxTokens = agentConfig.getMaxTokens();
		int maxOutputTokens = maxTokens != null ? maxTokens
			: com.top_logic.tl.ai.core.agent.AgentConfig.DEFAULT_MAX_OUTPUT_TOKENS;

		com.top_logic.tl.ai.core.agent.AgentConfig coreConfig =
			new com.top_logic.tl.ai.core.agent.AgentConfig(
				modelName, systemPrompt,
				com.top_logic.tl.ai.core.agent.AgentConfig.DEFAULT_MAX_TURNS,
				maxOutputTokens, modelOptions);

		AgentLoop loop;
		if (conversationMemory != null) {
			loop = new AgentLoop(coreConfig, provider, coreRegistry, conversationMemory);
		} else {
			loop = new AgentLoop(coreConfig, provider, coreRegistry);
		}

		AgentResult result;
		if (conversationMemory != null) {
			result = loop.run(ToolContext.EMPTY, listener);
		} else {
			result = loop.run(conversation, ToolContext.EMPTY, listener);
		}

		return extractResponseText(conversation, conversationMemory);
	}

	/**
	 * Resolves the effective model name for an agent, falling back to the default model.
	 */
	private String resolveModelName(AgentConfig agentConfig) {
		String model = agentConfig.getModel();
		if (model != null && !model.isEmpty()) {
			return model;
		}
		String defaultModel = getConfig().getDefaultModel();
		if (defaultModel != null && !defaultModel.isEmpty()) {
			return defaultModel;
		}
		throw new RuntimeException(
			"Agent '" + agentConfig.getName() + "' has no model and no default model is configured.");
	}

	/**
	 * Builds model options from the XML agent configuration.
	 *
	 * <p>
	 * Maps thinking, temperature, topP, and other provider-specific settings into the generic
	 * {@code modelOptions} map consumed by the core providers.
	 * </p>
	 */
	private Map<String, Object> buildModelOptions(AgentConfig agentConfig) {
		Map<String, Object> options = new HashMap<>();

		if (agentConfig.getTemperature() != null) {
			options.put("temperature", agentConfig.getTemperature());
		}
		if (agentConfig.getTopP() != null) {
			options.put("topP", agentConfig.getTopP());
		}
		if (agentConfig.getTopK() != null) {
			options.put("topK", agentConfig.getTopK());
		}
		if (agentConfig.getThinkingType() != null) {
			options.put("thinkingType", agentConfig.getThinkingType().name().toLowerCase(Locale.ROOT));
			if (agentConfig.getThinkingBudgetTokens() != null) {
				options.put("thinkingBudgetTokens", agentConfig.getThinkingBudgetTokens());
			}
		}
		if (agentConfig.getReasoningEffort() != null) {
			options.put("reasoningEffort", agentConfig.getReasoningEffort().name().toLowerCase(Locale.ROOT));
		}
		if (agentConfig.getStopSequences() != null && !agentConfig.getStopSequences().isEmpty()) {
			options.put("stop", agentConfig.getStopSequences());
		}
		if (agentConfig.getPresencePenalty() != null) {
			options.put("presencePenalty", agentConfig.getPresencePenalty());
		}
		if (agentConfig.getFrequencyPenalty() != null) {
			options.put("frequencyPenalty", agentConfig.getFrequencyPenalty());
		}
		if (agentConfig.getSeed() != null) {
			options.put("seed", agentConfig.getSeed());
		}

		return options.isEmpty() ? Map.of() : Map.copyOf(options);
	}

	/**
	 * Builds a tl-ai-core {@link com.top_logic.tl.ai.core.tool.ToolRegistry} from the agent's
	 * configured tools, delegation tools, and skill tools.
	 */
	private com.top_logic.tl.ai.core.tool.ToolRegistry buildCoreToolRegistry(AgentConfig agentConfig) {
		com.top_logic.tl.ai.core.tool.ToolRegistry coreRegistry =
			new com.top_logic.tl.ai.core.tool.ToolRegistry();

		// Add standard tools from the service-level ToolRegistry
		ToolRegistry serviceRegistry = ToolRegistry.getInstance();
		for (String qualifiedName : agentConfig.getTools()) {
			Tool tool = serviceRegistry.getTool(qualifiedName);
			if (tool != null) {
				coreRegistry.register(tool);
			} else {
				Logger.warn("Agent '" + agentConfig.getName() + "' references unknown tool '" + qualifiedName +
					"'. Available: " + serviceRegistry.getToolNames(), AgentRegistry.class);
			}
		}

		// Add skill tools
		if (!agentConfig.getSkills().isEmpty() && _skillRegistry != null) {
			SkillTool skillTool = new SkillTool(_skillRegistry, agentConfig.getName());
			for (Tool tool : AnnotatedTools.from(skillTool)) {
				coreRegistry.register(tool);
			}
		}

		// Add delegation tools
		if (agentConfig instanceof ChatAgentConfig chatConfig) {
			List<String> delegations = chatConfig.getDelegations();
			if (!delegations.isEmpty()) {
				for (Tool delegationTool : buildDelegationTools(agentConfig)) {
					coreRegistry.register(delegationTool);
				}
			}
		}

		return coreRegistry.freeze();
	}

	/**
	 * Builds delegation tools as tl-ai-core {@link Tool} instances.
	 *
	 * <p>
	 * Each delegation target becomes a {@link Tool} that runs a nested {@link AgentLoop} with
	 * the target agent's configuration, its own conversation and tool registry.
	 * </p>
	 */
	private List<Tool> buildDelegationTools(AgentConfig config) {
		if (!(config instanceof ChatAgentConfig chatConfig)) {
			return List.of();
		}
		List<String> delegations = chatConfig.getDelegations();
		if (delegations.isEmpty()) {
			return List.of();
		}

		List<Tool> result = new ArrayList<>(delegations.size());
		String fromAgent = chatConfig.getName();

		for (String targetName : delegations) {
			AgentConfig targetConfig = getConfig().getAgents().get(targetName);
			if (targetConfig == null) {
				Logger.warn("Agent '" + fromAgent + "' delegates to unknown agent '" + targetName +
					"'. Available: " + getConfig().getAgents().keySet(), AgentRegistry.class);
				continue;
			}

			String desc = targetConfig.getDescription();
			if (desc == null || desc.isEmpty()) {
				desc = "Specialist agent";
			}

			String toolName = delegationToolName(targetName);
			String toolDescription = "Delegate to '" + targetName + "': " + desc;

			String inputSchemaJson = "{"
				+ "\"type\":\"object\","
				+ "\"properties\":{"
				+ "\"goal\":{\"type\":\"string\",\"description\":\"Short intent\"},"
				+ "\"task\":{\"type\":\"string\",\"description\":\"Concrete question or instruction\"},"
				+ "\"context\":{\"type\":\"string\",\"description\":\"Optional: relevant context or hints\"}"
				+ "},"
				+ "\"required\":[\"task\"]"
				+ "}";

			ToolSchema schema = new ToolSchema(toolName, toolDescription, inputSchemaJson);

			result.add(new DelegationTool(toolName, toolDescription, schema, fromAgent, targetName));
		}

		return result;
	}

	/**
	 * A delegation tool that runs a sub-agent via a nested {@link AgentLoop}.
	 */
	private class DelegationTool implements Tool {

		private final String _name;

		private final String _description;

		private final ToolSchema _schema;

		private final String _fromAgent;

		private final String _targetName;

		/** */
		DelegationTool(String name, String description, ToolSchema schema,
				String fromAgent, String targetName) {
			_name = name;
			_description = description;
			_schema = schema;
			_fromAgent = fromAgent;
			_targetName = targetName;
		}

		@Override
		public String name() {
			return _name;
		}

		@Override
		public String description() {
			return _description;
		}

		@Override
		public ToolSchema schema() {
			return _schema;
		}

		@Override
		public com.top_logic.tl.ai.core.tool.ToolMode mode() {
			return com.top_logic.tl.ai.core.tool.ToolMode.READ;
		}

		@Override
		public String execute(Map<String, Object> args, ToolContext context) {
			String goal = (String) args.get("goal");
			String task = (String) args.get("task");
			String contextHint = (String) args.get("context");

			StringBuilder prompt = new StringBuilder();
			if (goal != null && !goal.isEmpty()) {
				prompt.append("Goal: ").append(goal).append("\n");
			}
			if (task != null && !task.isEmpty()) {
				prompt.append("Task: ").append(task).append("\n");
			}
			if (contextHint != null && !contextHint.isEmpty()) {
				prompt.append("Context: ").append(contextHint).append("\n");
			}

			String message = prompt.length() > 0 ? prompt.toString() : task;

			ToolCallTracking.recordAgentCallStartIfTracking(_fromAgent, _targetName, message);
			try {
				// Create a fresh conversation for the sub-agent
				Conversation subConversation = new Conversation();
				subConversation.add(new UserMessage(
					java.util.UUID.randomUUID().toString(), java.time.Instant.now(),
					List.of(new TextContent(message))));

				AgentConfig targetConfig = getAgentConfig(_targetName);
				String subResult = ThreadContextManager.inSystemInteraction(AgentRegistry.class,
					() -> runAgentWithConfig(targetConfig, subConversation, null,
						event -> { /* discard sub-agent events */ }));

				ToolCallTracking.recordAgentCallCompleteIfTracking(truncate(subResult));
				return subResult;
			} catch (Exception e) {
				Throwable cause = e;
				while (cause.getCause() != null && cause.getCause() != cause) {
					cause = cause.getCause();
				}
				String errorMsg = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
				Logger.error("Delegation to '" + _targetName + "' failed: " + errorMsg, cause, AgentRegistry.class);
				ToolCallTracking.recordAgentCallCompleteIfTracking("Error: " + errorMsg);
				return "Error delegating to '" + _targetName + "': " + errorMsg;
			}
		}
	}

	/**
	 * Extracts the response text from the last {@link AssistantMessage} in the conversation.
	 */
	private static String extractResponseText(Conversation conversation, ConversationMemory memory) {
		Conversation effectiveConversation = memory != null ? memory.conversation() : conversation;
		List<Message> messages = effectiveConversation.messages();
		for (int i = messages.size() - 1; i >= 0; i--) {
			if (messages.get(i) instanceof AssistantMessage assistant) {
				StringBuilder sb = new StringBuilder();
				for (ContentPart part : assistant.content()) {
					if (part instanceof TextContent text) {
						if (sb.length() > 0) {
							sb.append("\n");
						}
						sb.append(text.text());
					}
				}
				return sb.toString();
			}
		}
		return "";
	}

	/**
	 * Builds the complete system prompt for the given agent configuration.
	 *
	 * <p>
	 * Reads the base prompt from {@link AgentConfig#getSystemPrompt()}, resolves the agent's
	 * {@link AgentConfig#getSkills() skills}, and injects them into the prompt. Skills appear as a
	 * catalog that the agent can load on demand via {@code loadSkill()}. Delegation targets appear
	 * as a catalog with their tool names and descriptions.
	 * </p>
	 *
	 * @param config
	 *        The agent configuration.
	 * @return The fully-resolved system prompt.
	 */
	public String getSystemPrompt(AgentConfig config) {
		String prompt = config.getSystemPrompt();

		// Build skill section
		String skillSection = buildSkillSectionForAgent(config);

		// Build tool section
		String toolSection = buildToolSection(config);

		// Build delegation section
		String delegationSection = buildDelegationSection(config);

		// Process all three placeholders using replacePlaceholderOrAppend
		prompt = replacePlaceholderOrAppend(prompt, SkillRegistry.SKILLS_PLACEHOLDER, skillSection);
		prompt = replacePlaceholderOrAppend(prompt, TOOLS_PLACEHOLDER, toolSection);
		prompt = replacePlaceholderOrAppend(prompt, DELEGATES_PLACEHOLDER, delegationSection);

		return prompt;
	}

	/**
	 * Resolves the skills for the given agent configuration and builds the skill section content.
	 *
	 * @param config
	 *        The agent configuration whose {@link AgentConfig#getSkills() skills} to resolve.
	 * @return The formatted skill section, or empty string if the agent has no skills.
	 */
	private String buildSkillSectionForAgent(AgentConfig config) {
		List<String> skillNames = config.getSkills();
		if (skillNames.isEmpty()) {
			return "";
		}

		List<Skill> skills = new ArrayList<>(skillNames.size());
		for (String skillName : skillNames) {
			try {
				skills.add(_skillRegistry.getSkill(skillName));
			} catch (IllegalArgumentException ex) {
				Logger.warn("Skipping unavailable skill '" + skillName + "': " + ex.getMessage(),
					AgentRegistry.class);
			}
		}
		return buildSkillSection(skills);
	}

	/**
	 * Returns the shared {@link SkillRegistry} managed by this service.
	 *
	 * @return The skill registry, or {@code null} if the service is not started.
	 */
	public SkillRegistry getSkillRegistry() {
		return _skillRegistry;
	}

	/**
	 * Computes the canonical tool name for delegating to the given target agent.
	 *
	 * @param targetName
	 *        The target agent name as declared in the configuration.
	 * @return The tool name in the format {@code delegate_to_<name>} with hyphens replaced by
	 *         underscores.
	 */
	static String delegationToolName(String targetName) {
		return "delegate_to_" + targetName.replace('-', '_');
	}

	private static final int RESULT_SUMMARY_MAX_LEN = 100_000;

	private static String truncate(String s) {
		if (s == null) {
			return "";
		}
		return s.length() <= RESULT_SUMMARY_MAX_LEN ? s : s.substring(0, RESULT_SUMMARY_MAX_LEN - 3) + "...";
	}

	/**
	 * Builds the model-to-provider index from the provider configurations.
	 */
	private static Map<String, String> buildModelProviderIndex(Map<String, ProviderConfig> providers) {
		Map<String, String> index = new LinkedHashMap<>();
		for (Map.Entry<String, ProviderConfig> entry : providers.entrySet()) {
			String providerName = entry.getKey();
			List<String> models = entry.getValue().getModels();
			if (models != null) {
				for (String model : models) {
					if (model != null && !model.isBlank()) {
						index.put(model.trim(), providerName);
					}
				}
			}
		}
		return index;
	}

	/**
	 * Creates an {@link LLMProvider} from the provider configuration.
	 *
	 * <p>
	 * The provider name determines which implementation to use: names containing "anthropic"
	 * produce an {@link AnthropicProvider}, "openai" produce an {@link OpenAIProvider}, and
	 * "mistral" produce a {@link MistralProvider}.
	 * </p>
	 */
	private LLMProvider createProvider(String providerName, ProviderConfig config) {
		String name = providerName.toLowerCase(Locale.ROOT);
		String apiKey = config.getApiKey();

		if (name.contains("anthropic")) {
			String baseUrl = config.getBaseUrl();
			if (baseUrl != null) {
				// Strip trailing /v1 if present — the provider appends it internally
				if (baseUrl.endsWith("/v1")) {
					baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
				}
			}
			String version = config.getVersion();
			if (version == null) {
				version = "2023-06-01";
			}
			return new AnthropicProvider(apiKey,
				baseUrl != null ? baseUrl : "https://api.anthropic.com",
				version);
		}

		if (name.contains("openai")) {
			String baseUrl = config.getBaseUrl();
			if (baseUrl != null) {
				if (baseUrl.endsWith("/v1")) {
					baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
				}
			}
			return new OpenAIProvider(apiKey,
				baseUrl != null ? baseUrl : "https://api.openai.com");
		}

		if (name.contains("mistral")) {
			String baseUrl = config.getBaseUrl();
			if (baseUrl != null) {
				if (baseUrl.endsWith("/v1")) {
					baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
				}
			}
			return new MistralProvider(apiKey,
				baseUrl != null ? baseUrl : "https://api.mistral.ai");
		}

		throw new IllegalStateException(
			"Unknown provider type: '" + providerName
				+ "'. Provider name must contain 'anthropic', 'openai', or 'mistral'.");
	}

	/**
	 * Checks if an API key is valid (not null, not empty, not a placeholder).
	 */
	static boolean isValidApiKey(String apiKey) {
		if (apiKey == null || apiKey.trim().isEmpty()) {
			return false;
		}
		if (apiKey.startsWith("%") && apiKey.endsWith("%")) {
			return false;
		}
		return true;
	}

	/**
	 * Replaces the given placeholder in the prompt with the section content, or appends the section
	 * at the end if the placeholder is not present. If the section is empty, the placeholder is
	 * removed without inserting anything.
	 *
	 * @param prompt
	 *        The base prompt text.
	 * @param placeholder
	 *        The placeholder marker to search for (e.g. {@code "<<AVAILABLE_TOOLS>>"}).
	 * @param section
	 *        The content to inject, or empty string if nothing to inject.
	 * @return The prompt with the placeholder replaced or section appended.
	 */
	static String replacePlaceholderOrAppend(String prompt, String placeholder, String section) {
		if (prompt.contains(placeholder)) {
			return prompt.replace(placeholder, section);
		}
		if (!section.isEmpty()) {
			return prompt + "\n\n---\n\n" + section;
		}
		return prompt;
	}

	/**
	 * Builds a markdown catalog of the tools available to the given agent.
	 *
	 * @param config
	 *        The agent configuration.
	 * @return A formatted tool catalog section, or empty string if the agent has no tools.
	 */
	private String buildToolSection(AgentConfig config) {
		List<String> toolNames = config.getTools();
		if (toolNames.isEmpty()) {
			return "";
		}

		ToolRegistry toolRegistry = ToolRegistry.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("# Available Tools\n\n");
		sb.append("You have the following tools available. Use them frequently to gather information ");
		sb.append("and perform actions. NEVER guess when a tool can give you the answer.\n\n");

		for (String qualifiedName : toolNames) {
			Tool tool = toolRegistry.getTool(qualifiedName);
			if (tool != null) {
				sb.append("- **").append(tool.name()).append("**: ").append(tool.description()).append("\n");
			} else {
				Logger.warn("buildToolSection: agent '" + config.getName() +
					"' references unknown tool '" + qualifiedName + "' — omitted from prompt catalog.",
					AgentRegistry.class);
			}
		}

		return sb.toString();
	}

	/**
	 * Builds an XML-style catalog of delegation targets for the given agent.
	 *
	 * @param config
	 *        The agent configuration.
	 * @return A formatted delegation catalog section, or empty string if the agent has no
	 *         delegations.
	 */
	private String buildDelegationSection(AgentConfig config) {
		if (!(config instanceof ChatAgentConfig chatConfig)) {
			return "";
		}
		List<String> delegations = chatConfig.getDelegations();
		if (delegations.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("# Available Sub-Agents\n\n");
		sb.append("You MUST delegate tasks to the sub-agent with the most relevant expertise.\n\n");
		sb.append("### Strategic Orchestration\n");
		sb.append("Your own context window is your most precious resource. When you delegate, the sub-agent's\n");
		sb.append("entire execution is consolidated into a single result in your history, keeping your main loop lean.\n\n");
		sb.append("<available_agents>\n");

		for (String targetName : delegations) {
			AgentConfig targetConfig = getConfig().getAgents().get(targetName);
			if (targetConfig == null) {
				continue;
			}
			String desc = targetConfig.getDescription();
			if (desc == null || desc.isEmpty()) {
				desc = "Specialist agent";
			}
			String toolName = delegationToolName(targetName);

			sb.append("  <agent>\n");
			sb.append("    <name>").append(targetName).append("</name>\n");
			sb.append("    <tool>").append(toolName).append("</tool>\n");
			sb.append("    <description>").append(escapeXml(desc)).append("</description>\n");
			sb.append("  </agent>\n");
		}

		sb.append("</available_agents>");
		return sb.toString();
	}

	/**
	 * Builds an XML-style skill catalog for injection into a system prompt. All skills are rendered
	 * as {@code <available_skills>} catalog entries with enforcement language for calling
	 * {@code loadSkill()}.
	 *
	 * @param skills
	 *        The resolved skills for the agent.
	 * @return The formatted skill section, or empty string if no skills.
	 */
	private String buildSkillSection(List<Skill> skills) {
		if (skills.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("# Available Skills\n\n");
		sb.append("You MUST call `loadSkill(\"skill-id\")` BEFORE performing any task that matches ");
		sb.append("a skill description. Loading the skill gives you specialized instructions ");
		sb.append("that dramatically improve your output quality.\n\n");

		sb.append("<available_skills>\n");
		for (Skill skill : skills) {
			sb.append("  <skill>\n");
			sb.append("    <id>").append(escapeXml(skill.id())).append("</id>\n");
			sb.append("    <description>").append(escapeXml(skill.description())).append("</description>\n");
			sb.append("  </skill>\n");
		}
		sb.append("</available_skills>\n\n");

		sb.append("**Rule:** If a task even partially matches a skill description, call `loadSkill()` first. ");
		sb.append("Do NOT attempt the task without loading the relevant skill.");

		return sb.toString();
	}

	/**
	 * Escapes XML special characters in text content that will be embedded in XML-style tags within
	 * system prompts.
	 */
	private static String escapeXml(String s) {
		return s.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;");
	}

	/**
	 * Module for {@link AgentRegistry}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentRegistry> {

		/**
		 * Singleton {@link AgentRegistry.Module} instance.
		 */
		public static final AgentRegistry.Module INSTANCE = new AgentRegistry.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<AgentRegistry> getImplementation() {
			return AgentRegistry.class;
		}
	}
}
