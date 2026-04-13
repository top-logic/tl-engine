/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.ai.service.agents.background.BackgroundAgentConfig;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.ai.service.skills.SkillConfig;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProvider;
import com.top_logic.tl.ai.core.provider.mistral.MistralProvider;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;

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

	/**
	 * Index mapping model names to their provider config name. Built at startup from the
	 * {@link ProviderConfig#getModels()} lists.
	 */
	private Map<String, String> _modelProviderIndex;

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

		// Build model name index from provider configs
		_modelProviderIndex = buildModelProviderIndex(getConfig().getProviders());

		Logger.info("AgentRegistry started with " +
			getConfig().getProviders().size() + " provider(s). " +
			"Agent and skill definitions are resolved from the knowledge base at runtime.",
			AgentRegistry.class);
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			_modelProviderIndex = null;
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
	 * <p>
	 * Resolves the entry point from agent instances in the knowledge base via
	 * {@link AgentFactory#getEntryPointAgentName()}.
	 * </p>
	 *
	 * @return The entry point agent name, never {@code null}.
	 * @throws IllegalStateException
	 *         if no entry point agent is defined in the knowledge base.
	 */
	public String getEntryPointAgentName() {
		return AgentFactory.getInstance().getEntryPointAgentName();
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
		AgentFactory factory = AgentFactory.getInstance();
		TLObject agentObj = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agentObj == null) {
			throw new RuntimeException(
				"No agent found with name '" + name + "' in the knowledge base.");
		}

		com.top_logic.tl.ai.core.agent.AgentConfig coreConfig = factory.buildConfig(agentObj);
		LLMProvider provider = factory.buildProvider(coreConfig.model());
		com.top_logic.tl.ai.core.tool.ToolRegistry registry = factory.buildToolRegistry(agentObj);

		AgentLoop loop;
		if (conversationMemory != null) {
			loop = new AgentLoop(coreConfig, provider, registry, conversationMemory);
			loop.run(ToolContext.EMPTY, listener);
		} else {
			loop = new AgentLoop(coreConfig, provider, registry);
			loop.run(conversation, ToolContext.EMPTY, listener);
		}

		return extractResponseText(conversation, conversationMemory);
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
