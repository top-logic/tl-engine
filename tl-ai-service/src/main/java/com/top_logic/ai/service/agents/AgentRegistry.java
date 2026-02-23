/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.ai.service.providers.AnthropicChatModelFactory;
import com.top_logic.ai.service.providers.MistralChatModelFactory;
import com.top_logic.ai.service.providers.OpenAIChatModelFactory;
import com.top_logic.ai.service.skills.Skill;
import com.top_logic.ai.service.skills.SkillRegistry;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Registry of runtime-configurable agents.
 *
 * <p>
 * Each agent is declared in XML via {@link AgentConfig} and holds its LLM model name, optional
 * thinking parameters, system prompt, tools, and skills. This service validates the configurations
 * at startup and provides factory methods to build fully-wired <i>LangChain4j</i> agents from them.
 * </p>
 *
 * <p>
 * Model building is provider-aware: if the referenced model is served by a known provider
 * ({@link AnthropicChatModelFactory}, {@link MistralChatModelFactory},
 * {@link OpenAIChatModelFactory}), the agent's parameters (thinking, temperature, max tokens) are
 * applied to a fresh, unpooled model instance. Otherwise, the pooled model from {@link AIService}
 * is returned as a fallback.
 * </p>
 *
 * @see AgentConfig
 * @see AIService
 */
public class AgentRegistry extends ConfiguredManagedClass<AgentRegistry.Config<?>> {

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
	}

	private static volatile AgentRegistry _instance;

	private SkillRegistry _skillRegistry;

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

		// Validate that all agent configs reference models known to AIService
		AIService aiService = AIService.getInstance();
		if (aiService != null) {
			List<String> availableModels = aiService.getModelNames();
			for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
				String agentName = entry.getKey();
				AgentConfig agentConfig = entry.getValue();
				String model = agentConfig.getModel();
				if (!availableModels.contains(model)) {
					Logger.warn(
						"Agent '" + agentName + "' references model '" + model +
							"' which is not registered in AIService. Available: " + availableModels,
						AgentRegistry.class);
				}
			}
		}

		// Build a shared SkillRegistry and pre-load skills for each agent
		_skillRegistry = new SkillRegistry();
		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			List<String> skillNames = entry.getValue().getSkills();
			if (!skillNames.isEmpty()) {
				// Pre-load all referenced skills so errors surface at startup
				for (String skillName : skillNames) {
					try {
						_skillRegistry.getSkill(skillName);
					} catch (IllegalArgumentException ex) {
						Logger.warn(
							"Agent '" + entry.getKey() + "' references unknown skill '" + skillName + "': " +
								ex.getMessage(),
							AgentRegistry.class);
					}
				}
			}
		}

		Logger.info("AgentRegistry started with " + getConfig().getAgents().size() + " agent(s).",
			AgentRegistry.class);
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			_skillRegistry = null;
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
		AgentConfig config = getConfig().getAgents().get(name);
		if (config == null) {
			throw new RuntimeException(
				"No agent configured with name '" + name + "'. Available: " +
					getConfig().getAgents().keySet());
		}
		return config;
	}

	/**
	 * Builds a provider-specific {@link ChatModel} from the given agent configuration.
	 *
	 * <p>
	 * If the model's factory is a known provider, a fresh unpooled model is created with the
	 * agent's parameters (thinking, temperature, max tokens). Otherwise, falls back to the pooled
	 * model from {@link AIService}.
	 * </p>
	 *
	 * @param config
	 *        The agent configuration.
	 * @return A chat model configured per the agent's settings.
	 */
	public ChatModel buildChatModel(AgentConfig config) {
		AIService aiService = AIService.getInstance();
		if (aiService == null) {
			throw new RuntimeException("AIService is not available.");
		}

		ChatModelFactory factory = aiService.getFactory(config.getModel());
		if (factory == null) {
			// No factory registered; fall back to pooled model
			return aiService.getChatModel(config.getModel());
		}

		if (factory instanceof AnthropicChatModelFactory) {
			return buildAnthropicModel((AnthropicChatModelFactory) factory, config);
		}
		if (factory instanceof MistralChatModelFactory) {
			return buildMistralModel((MistralChatModelFactory) factory, config);
		}
		if (factory instanceof OpenAIChatModelFactory) {
			return buildOpenAIModel((OpenAIChatModelFactory) factory, config);
		}

		// Unknown provider type; fall back to pooled model
		return aiService.getChatModel(config.getModel());
	}

	/**
	 * Builds the complete system prompt for the given agent configuration.
	 *
	 * <p>
	 * Reads the base prompt from {@link AgentConfig#getSystemPrompt()}, resolves the agent's
	 * {@link AgentConfig#getSkills() skills}, and injects skill sections: {@link Skill.Mode#AUTO
	 * AUTO} skills are injected in full, {@link Skill.Mode#ON_DEMAND ON_DEMAND} skills appear as a
	 * catalog. If the base prompt contains {@link SkillRegistry#SKILLS_PLACEHOLDER}, the skill
	 * section replaces it; otherwise, it is appended.
	 * </p>
	 *
	 * @param config
	 *        The agent configuration.
	 * @return The fully-resolved system prompt.
	 */
	public String getSystemPrompt(AgentConfig config) {
		String basePrompt = config.getSystemPrompt();
		List<String> skillNames = config.getSkills();

		if (skillNames.isEmpty()) {
			// No skills; just remove the placeholder if present
			return basePrompt.replace(SkillRegistry.SKILLS_PLACEHOLDER, "");
		}

		// Load all skills
		List<Skill> skills = new ArrayList<>(skillNames.size());
		for (String skillName : skillNames) {
			try {
				skills.add(_skillRegistry.getSkill(skillName));
			} catch (IllegalArgumentException ex) {
				Logger.warn("Skipping unavailable skill '" + skillName + "': " + ex.getMessage(),
					AgentRegistry.class);
			}
		}

		String skillSection = buildSkillSection(skills);

		if (basePrompt.contains(SkillRegistry.SKILLS_PLACEHOLDER)) {
			return basePrompt.replace(SkillRegistry.SKILLS_PLACEHOLDER, skillSection);
		}

		if (skills.isEmpty()) {
			return basePrompt;
		}
		return basePrompt + "\n\n---\n\n" + skillSection;
	}

	/**
	 * Instantiates the tool objects declared in the given agent configuration.
	 *
	 * <p>
	 * Each tool entry in {@link AgentConfig#getTools()} is a {@link PolymorphicConfiguration} that
	 * is instantiated via the TopLogic configuration framework.
	 * </p>
	 *
	 * @param config
	 *        The agent configuration.
	 * @return A list of instantiated tool objects.
	 */
	public List<Object> buildTools(AgentConfig config) {
		List<Object> tools = new ArrayList<>();
		InstantiationContext context = new DefaultInstantiationContext(AgentRegistry.class);
		for (PolymorphicConfiguration<?> toolConfig : config.getTools()) {
			Object tool = context.getInstance(toolConfig);
			tools.add(tool);
		}
		try {
			context.checkErrors();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to instantiate tools for agent '" +
				config.getName() + "': " + ex.getMessage(), ex);
		}

		// TODO: Wire SkillTool once name-based SkillRegistry support is added (Task 5).
		// For now, skills are only injected into the system prompt (AUTO) or listed as catalog
		// (ON_DEMAND). The SkillTool for on-demand loading will be added when the registry
		// supports agent-name-based lookups.

		return tools;
	}

	/**
	 * Builds a fully-wired <i>LangChain4j</i> agent from the named configuration without
	 * conversation memory.
	 *
	 * @param <T>
	 *        The agent interface type.
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @param agentInterface
	 *        The agent interface class.
	 * @return A fully configured agent instance.
	 *
	 * @see #buildAgent(String, Class, ChatMemory)
	 */
	public <T> T buildAgent(String name, Class<T> agentInterface) {
		return buildAgent(name, agentInterface, null);
	}

	/**
	 * Builds a fully-wired <i>LangChain4j</i> agent from the named configuration.
	 *
	 * <p>
	 * Resolves the agent's model, system prompt, and tools from the XML configuration and assembles
	 * them into a <i>LangChain4j</i> agent via {@link AgenticServices#agentBuilder(Class)}.
	 * </p>
	 *
	 * @param <T>
	 *        The agent interface type.
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @param agentInterface
	 *        The agent interface class.
	 * @param memory
	 *        Optional conversation memory, or {@code null} for stateless agents.
	 * @return A fully configured agent instance.
	 */
	public <T> T buildAgent(String name, Class<T> agentInterface, ChatMemory memory) {
		AgentConfig config = getAgentConfig(name);
		ChatModel model = buildChatModel(config);
		String systemPrompt = getSystemPrompt(config);
		List<Object> tools = buildTools(config);

		var builder = AgenticServices.agentBuilder(agentInterface)
			.chatModel(model)
			.systemMessageProvider(memoryId -> systemPrompt)
			.tools(tools.toArray());

		if (memory != null) {
			builder.chatMemory(memory);
		}

		return builder.build();
	}

	/**
	 * Returns the shared {@link SkillRegistry} managed by this service.
	 *
	 * @return The skill registry, or {@code null} if the service is not started.
	 */
	public SkillRegistry getSkillRegistry() {
		return _skillRegistry;
	}

	private ChatModel buildAnthropicModel(AnthropicChatModelFactory factory, AgentConfig config) {
		AnthropicChatModelFactory.Config fc = factory.getConfig();
		AnthropicChatModel.AnthropicChatModelBuilder builder = AnthropicChatModel.builder()
			.apiKey(fc.getApiKey())
			.baseUrl(fc.getBaseUrl())
			.modelName(config.getModel());

		if (config.getThinkingType() != null) {
			builder.thinkingType(config.getThinkingType());
			builder.returnThinking(config.getReturnThinking());
			builder.sendThinking(config.getSendThinking());
		}
		if (config.getThinkingBudgetTokens() != null) {
			builder.thinkingBudgetTokens(config.getThinkingBudgetTokens());
		}
		if (config.getMaxTokens() != null) {
			builder.maxTokens(config.getMaxTokens());
		}
		if (config.getTemperature() != null) {
			builder.temperature(config.getTemperature());
		}
		String version = fc.getVersion();
		if (version != null) {
			builder.version(version);
		}
		return builder.build();
	}

	private ChatModel buildMistralModel(MistralChatModelFactory factory, AgentConfig config) {
		MistralChatModelFactory.Config fc = factory.getConfig();
		MistralAiChatModel.MistralAiChatModelBuilder builder = MistralAiChatModel.builder()
			.apiKey(fc.getApiKey())
			.baseUrl(fc.getBaseUrl())
			.modelName(config.getModel());

		if (config.getMaxTokens() != null) {
			builder.maxTokens(config.getMaxTokens());
		}
		if (config.getTemperature() != null) {
			builder.temperature(config.getTemperature());
		}
		return builder.build();
	}

	private ChatModel buildOpenAIModel(OpenAIChatModelFactory factory, AgentConfig config) {
		OpenAIChatModelFactory.Config fc = factory.getConfig();
		OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
			.apiKey(fc.getApiKey())
			.baseUrl(fc.getBaseUrl())
			.modelName(config.getModel());

		if (config.getMaxTokens() != null) {
			builder.maxTokens(config.getMaxTokens());
		}
		if (config.getTemperature() != null) {
			builder.temperature(config.getTemperature());
		}
		String org = fc.getOrganization();
		if (org != null) {
			builder.organizationId(org);
		}
		return builder.build();
	}

	private String buildSkillSection(List<Skill> skills) {
		if (skills.isEmpty()) {
			return "";
		}

		List<Skill> autoSkills = skills.stream()
			.filter(s -> s.mode() == Skill.Mode.AUTO)
			.toList();
		List<Skill> onDemandSkills = skills.stream()
			.filter(s -> s.mode() == Skill.Mode.ON_DEMAND)
			.toList();

		StringBuilder sb = new StringBuilder();

		// Auto skills: inject full content directly
		for (Skill skill : autoSkills) {
			sb.append("### ").append(skill.description()).append("\n\n");
			sb.append(skill.content()).append("\n\n");
		}

		// On-demand skills: catalog with loadSkill instructions
		if (!onDemandSkills.isEmpty()) {
			sb.append("### Available Skills (IMPORTANT)\n\n");
			sb.append("You have specialized skills that improve your work. ");
			sb.append(
				"Load a skill BEFORE performing the related action by calling `loadSkill(\"skill-id\")`.\n\n");
			for (Skill skill : onDemandSkills) {
				sb.append("- `").append(skill.id()).append("`: ").append(skill.description()).append("\n");
			}
		}

		return sb.toString();
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
