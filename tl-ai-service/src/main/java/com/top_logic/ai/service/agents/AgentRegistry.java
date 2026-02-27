/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.ai.service.providers.AnthropicChatModelFactory;
import com.top_logic.ai.service.providers.MistralChatModelFactory;
import com.top_logic.ai.service.providers.ModelCapability;
import com.top_logic.ai.service.providers.OpenAIChatModelFactory;
import com.top_logic.ai.service.skills.Skill;
import com.top_logic.ai.service.skills.SkillConfig;
import com.top_logic.ai.service.skills.SkillRegistry;
import com.top_logic.ai.service.skills.SkillTool;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.registry.ToolImplementation;
import com.top_logic.ai.service.tools.registry.ToolRegistry;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * Registry of runtime-configurable agents and skills.
 *
 * <p>
 * Each agent is declared in XML via {@link AgentConfig} and holds its LLM model name, optional
 * thinking parameters, system prompt, tools, and skills. Skills are declared via
 * {@link SkillConfig}. This service validates the configurations at startup and provides factory
 * methods to build fully-wired <i>LangChain4j</i> agents from them.
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
 * @see SkillConfig
 * @see AIService
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
		 * Each entry defines a reusable skill with its description, mode, and prompt content. Skills
		 * are referenced by name from {@link AgentConfig#getSkills()}.
		 * </p>
		 */
		@Name(SKILLS)
		@EntryTag("skill")
		@Key(SkillConfig.NAME_ATTRIBUTE)
		Map<String, SkillConfig> getSkills();
	}

	private static volatile AgentRegistry _instance;

	private SkillRegistry _skillRegistry;

	private Map<String, String> _agentNameIndex;

	private String _entryPointAgentName;

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

		// Validate agent model references against AIService (informational only)
		AIService aiService = AIService.getInstance();
		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			String agentName = entry.getKey();
			String model = entry.getValue().getModel();
			if (model == null || model.isEmpty()) {
				Logger.info(
					"Agent '" + agentName + "' has no model configured. " +
						"It will use the AIService default model when invoked.",
					AgentRegistry.class);
			} else if (aiService != null && !aiService.getModelNames().contains(model)) {
				Logger.warn(
					"Agent '" + agentName + "' references model '" + model +
						"' which is not registered in AIService. Available: " + aiService.getModelNames(),
					AgentRegistry.class);
			}
		}

		// Build a shared SkillRegistry from XML-configured skills
		_skillRegistry = new SkillRegistry();

		Map<String, SkillConfig> skillConfigs = getConfig().getSkills();
		if (skillConfigs != null && !skillConfigs.isEmpty()) {
			_skillRegistry.registerSkillsFromConfig(skillConfigs);
		}

		// Register agent-to-skill mappings and validate references
		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			List<String> skillNames = entry.getValue().getSkills();
			if (!skillNames.isEmpty()) {
				_skillRegistry.registerAgentSkills(entry.getKey(), skillNames);
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

		// Validate that exactly one agent is marked as entry point
		List<String> entryPoints = new ArrayList<>();
		for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
			if (entry.getValue().isEntryPoint()) {
				entryPoints.add(entry.getKey());
			}
		}
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

		Logger.info("AgentRegistry started with " + getConfig().getAgents().size() + " agent(s) and " +
			skillConfigs.size() + " skill(s).",
			AgentRegistry.class);
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
			_skillRegistry = null;
			_agentNameIndex = null;
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

		String modelName = config.getModel();

		// Fall back to AIService default model when agent has no model configured
		if (modelName == null || modelName.isEmpty()) {
			return aiService.getChatModel(null);
		}

		ChatModelFactory factory = aiService.getFactory(modelName);
		if (factory == null) {
			return aiService.getChatModel(modelName);
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

		return aiService.getChatModel(modelName);
	}

	/**
	 * Builds the complete system prompt for the given agent configuration.
	 *
	 * <p>
	 * Reads the base prompt from {@link AgentConfig#getSystemPrompt()}, resolves the agent's
	 * {@link AgentConfig#getSkills() skills}, and injects them into the prompt. Auto skills are
	 * injected in full, on-demand skills appear as a catalog. Delegation targets are not mentioned
	 * in the system prompt because each delegation tool carries its own self-describing
	 * {@link ToolSpecification}.
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
	 * Resolves tools for the given agent configuration from the {@link ToolRegistry}.
	 *
	 * <p>
	 * Each qualified name in {@link AgentConfig#getTools()} is looked up in the
	 * {@link ToolRegistry}. On-demand skill tools are added when the agent has skills of
	 * that mode. Delegation tools are handled separately by
	 * {@link #buildDelegationTools(AgentConfig)}.
	 * </p>
	 *
	 * @param config
	 *        The agent configuration.
	 * @return A list of resolved tool implementations.
	 */
	public List<ToolImplementation> buildTools(AgentConfig config) {
		List<ToolImplementation> tools = new ArrayList<>();

		ToolRegistry toolRegistry = ToolRegistry.getInstance();
		for (String qualifiedName : config.getTools()) {
			ToolImplementation tool = toolRegistry.getTool(qualifiedName);
			if (tool != null) {
				tools.add(tool);
			} else {
				Logger.warn("Agent '" + config.getName() + "' references unknown tool '" + qualifiedName +
					"'. Available: " + toolRegistry.getToolNames(), AgentRegistry.class);
			}
		}

		if (!config.getSkills().isEmpty() && _skillRegistry != null) {
			boolean hasOnDemand = config.getSkills().stream()
				.anyMatch(name -> {
					try {
						return _skillRegistry.getSkill(name).mode() == Skill.Mode.ON_DEMAND;
					} catch (IllegalArgumentException ex) {
						return false;
					}
				});
			if (hasOnDemand) {
				SkillTool skillTool = new SkillTool(_skillRegistry, config.getName());
				for (ToolSpecification spec : ToolSpecifications.toolSpecificationsFrom(skillTool)) {
					Method method = findMethodForToolSpec(SkillTool.class, spec.name());
					if (method != null) {
						ToolExecutor executor = new DefaultToolExecutor(skillTool, method);
						tools.add(new ToolImplementation(spec, executor));
					}
				}
			}
		}

		return tools;
	}

	private static Method findMethodForToolSpec(Class<?> clazz, String toolName) {
		for (Method method : clazz.getDeclaredMethods()) {
			dev.langchain4j.agent.tool.Tool toolAnnotation =
				method.getAnnotation(dev.langchain4j.agent.tool.Tool.class);
			if (toolAnnotation != null) {
				String name = toolAnnotation.name().isEmpty() ? method.getName() : toolAnnotation.name();
				if (name.equals(toolName)) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * Builds programmatic delegation tools for the given agent configuration.
	 *
	 * <p>
	 * For each name in {@link AgentConfig#getDelegations()}, a dedicated
	 * {@link ToolSpecification} is created whose description includes the target agent's
	 * {@link AgentConfig#getDescription() description}. This makes each delegation target
	 * self-describing in the tool signature — the LLM does not need a system prompt section to
	 * know what each delegation target does.
	 * </p>
	 *
	 * @param config
	 *        The source agent configuration.
	 * @return A list of {@link ToolImplementation}s, one per delegation target. Empty if the agent
	 *         has no delegations.
	 */
	public List<ToolImplementation> buildDelegationTools(AgentConfig config) {
		List<String> delegations = config.getDelegations();
		if (delegations.isEmpty()) {
			return List.of();
		}

		List<ToolImplementation> result = new ArrayList<>(delegations.size());
		String fromAgent = config.getName();

		JsonObjectSchema parameters = JsonObjectSchema.builder()
			.addStringProperty("goal", "Short intent")
			.addStringProperty("task", "Concrete question or instruction")
			.addStringProperty("context", "Optional: relevant context or hints")
			.required("task")
			.build();

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

			ToolSpecification spec = ToolSpecification.builder()
				.name(toolName)
				.description("Delegate to '" + targetName + "': " + desc)
				.parameters(parameters)
				.build();

			ToolExecutor executor = createDelegationExecutor(fromAgent, targetName);
			result.add(new ToolImplementation(spec, executor));
		}

		return result;
	}

	/**
	 * Builds an {@link UntypedAgent} from the named configuration.
	 *
	 * <p>
	 * Uses the no-arg {@link AgenticServices#agentBuilder()} to create an agent purely from
	 * configuration data — no Java interface is required. The agent accepts a single
	 * {@code "message"} input and returns a {@link String}.
	 * </p>
	 *
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @return A fully configured untyped agent instance.
	 */
	public UntypedAgent buildUntypedAgent(String name) {
		return buildUntypedAgent(name, null);
	}

	/**
	 * Builds an {@link UntypedAgent} from the named configuration.
	 *
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @param memory
	 *        Optional conversation memory, or {@code null} for stateless agents.
	 * @return A fully configured untyped agent instance.
	 */
	public UntypedAgent buildUntypedAgent(String name, ChatMemory memory) {
		return buildUntypedAgent(name, memory, null);
	}

	/**
	 * Builds an {@link UntypedAgent} from the named configuration with an optional
	 * {@link ChatModel} decorator.
	 *
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @param memory
	 *        Optional conversation memory, or {@code null} for stateless agents.
	 * @param chatModelDecorator
	 *        Optional decorator applied to the {@link ChatModel} before building. Useful for
	 *        injecting multimodal content into LLM requests. May be {@code null}.
	 * @return A fully configured untyped agent instance.
	 */
	public UntypedAgent buildUntypedAgent(String name, ChatMemory memory,
			java.util.function.UnaryOperator<ChatModel> chatModelDecorator) {
		AgentConfig config = getAgentConfig(name);
		ChatModel model = buildChatModel(config);
		if (chatModelDecorator != null) {
			model = chatModelDecorator.apply(model);
		}
		String systemPrompt = getSystemPrompt(config);
		List<ToolImplementation> tools = buildTools(config);
		List<ToolImplementation> delegationTools = buildDelegationTools(config);

		Map<ToolSpecification, ToolExecutor> allTools = new LinkedHashMap<>();
		for (ToolImplementation impl : tools) {
			allTools.put(impl.getSpecification(), impl.getExecutor());
		}
		for (ToolImplementation impl : delegationTools) {
			allTools.put(impl.getSpecification(), impl.getExecutor());
		}

		var builder = AgenticServices.agentBuilder()
			.chatModel(model)
			.systemMessage(systemPrompt)
			.userMessage("{{message}}")
			.inputKey(String.class, "message")
			.returnType(String.class);

		if (!allTools.isEmpty()) {
			builder.tools(allTools);
		}

		if (memory != null) {
			builder.chatMemory(memory);
		}

		return builder.build();
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

	private ToolExecutor createDelegationExecutor(String fromAgent, String targetName) {
		return (request, memoryId) -> {
			Map<String, Object> args;
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> parsed = (Map<String, Object>) JSON.fromString(request.arguments());
				args = parsed;
			} catch (Exception e) {
				return "Error parsing delegation arguments: " + e.getMessage();
			}

			String goal = (String) args.get("goal");
			String task = (String) args.get("task");
			String context = (String) args.get("context");

			StringBuilder prompt = new StringBuilder();
			if (goal != null && !goal.isEmpty()) {
				prompt.append("Goal: ").append(goal).append("\n");
			}
			if (task != null && !task.isEmpty()) {
				prompt.append("Task: ").append(task).append("\n");
			}
			if (context != null && !context.isEmpty()) {
				prompt.append("Context: ").append(context).append("\n");
			}

			String message = prompt.length() > 0 ? prompt.toString() : task;

			ToolCallTracking.recordAgentCallStartIfTracking(fromAgent, targetName, message);
			try {
				UntypedAgent agent = buildUntypedAgent(targetName);
				String result = (String) ThreadContextManager.inSystemInteraction(AgentRegistry.class,
					() -> agent.invoke(Map.of("message", message)));
				ToolCallTracking.recordAgentCallCompleteIfTracking(truncate(result));
				return result;
			} catch (Exception e) {
				ToolCallTracking.recordAgentCallCompleteIfTracking("Error: " + e.getMessage());
				throw e;
			}
		};
	}

	private static final int RESULT_SUMMARY_MAX_LEN = 100_000;

	private static String truncate(String s) {
		if (s == null) {
			return "";
		}
		return s.length() <= RESULT_SUMMARY_MAX_LEN ? s : s.substring(0, RESULT_SUMMARY_MAX_LEN - 3) + "...";
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
		List<ToolImplementation> tools = buildTools(config);
		List<ToolImplementation> delegationTools = buildDelegationTools(config);

		Map<ToolSpecification, ToolExecutor> allTools = new LinkedHashMap<>();
		for (ToolImplementation impl : tools) {
			allTools.put(impl.getSpecification(), impl.getExecutor());
		}
		for (ToolImplementation impl : delegationTools) {
			allTools.put(impl.getSpecification(), impl.getExecutor());
		}

		var builder = AgenticServices.agentBuilder(agentInterface)
			.chatModel(model)
			.systemMessageProvider(memoryId -> systemPrompt);

		if (!allTools.isEmpty()) {
			builder.tools(allTools);
		}

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

		if (config.getThinkingType() != null && config.isThinkingCapable()) {
			ThinkingType thinkingType = config.getThinkingType();
			Set<ModelCapability> caps = factory.getModelCapabilities(config.getModel());
			boolean supported = (thinkingType == ThinkingType.ADAPTIVE)
				? caps.contains(ModelCapability.THINKING_ADAPTIVE)
				: caps.contains(ModelCapability.THINKING_ENABLED);
			if (supported) {
				builder.thinkingType(thinkingType.getValue());
				if (config.getThinkingBudgetTokens() != null) {
					builder.thinkingBudgetTokens(config.getThinkingBudgetTokens());
				}
				builder.returnThinking(config.getReturnThinking());
				builder.sendThinking(config.getSendThinking());
			}
		}
		if (config.getMaxTokens() != null) {
			builder.maxTokens(config.getMaxTokens());
		}
		if (config.getTemperature() != null) {
			builder.temperature(config.getTemperature());
		}
		if (config.getTopP() != null) {
			builder.topP(config.getTopP());
		}
		if (config.getTopK() != null) {
			builder.topK(config.getTopK());
		}
		if (config.getStopSequences() != null && !config.getStopSequences().isEmpty()) {
			builder.stopSequences(config.getStopSequences());
		}
		if (config.getDisableParallelToolUse()) {
			builder.disableParallelToolUse(Boolean.TRUE);
		}
		if (config.getTimeoutSeconds() != null) {
			builder.timeout(toTimeoutDuration(config.getTimeoutSeconds()));
		}
		if (config.getMaxRetries() != null) {
			builder.maxRetries(config.getMaxRetries());
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
		if (config.getTopP() != null) {
			builder.topP(config.getTopP());
		}
		if (config.getStopSequences() != null && !config.getStopSequences().isEmpty()) {
			builder.stopSequences(config.getStopSequences());
		}
		ResponseFormat responseFormat = mapResponseFormat(config.getResponseFormat());
		if (responseFormat != null) {
			builder.responseFormat(responseFormat);
		}
		if (config.getTimeoutSeconds() != null) {
			builder.timeout(toTimeoutDuration(config.getTimeoutSeconds()));
		}
		if (config.getMaxRetries() != null) {
			builder.maxRetries(config.getMaxRetries());
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
		if (config.getTopP() != null) {
			builder.topP(config.getTopP());
		}
		if (config.getStopSequences() != null && !config.getStopSequences().isEmpty()) {
			builder.stop(config.getStopSequences());
		}
		if (config.getPresencePenalty() != null) {
			builder.presencePenalty(config.getPresencePenalty());
		}
		if (config.getFrequencyPenalty() != null) {
			builder.frequencyPenalty(config.getFrequencyPenalty());
		}
		if (config.getSeed() != null) {
			builder.seed(config.getSeed());
		}
		if (config.getReasoningEffort() != null
			&& factory.getModelCapabilities(config.getModel()).contains(ModelCapability.REASONING_EFFORT)) {
			builder.reasoningEffort(config.getReasoningEffort().getValue());
		}
		ResponseFormat responseFormat = mapResponseFormat(config.getResponseFormat());
		if (responseFormat != null) {
			builder.responseFormat(responseFormat);
		}
		if (config.getTimeoutSeconds() != null) {
			builder.timeout(toTimeoutDuration(config.getTimeoutSeconds()));
		}
		if (config.getMaxRetries() != null) {
			builder.maxRetries(config.getMaxRetries());
		}
		String org = fc.getOrganization();
		if (org != null) {
			builder.organizationId(org);
		}
		return builder.build();
	}

	static ResponseFormat mapResponseFormat(AgentConfig.ResponseFormatOption option) {
		if (option == null || option == AgentConfig.ResponseFormatOption.DEFAULT) {
			return null;
		}
		if (option == AgentConfig.ResponseFormatOption.JSON) {
			return ResponseFormat.JSON;
		}
		return ResponseFormat.TEXT;
	}

	static Duration toTimeoutDuration(Integer timeoutSeconds) {
		return timeoutSeconds == null ? null : Duration.ofSeconds(timeoutSeconds);
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
			ToolImplementation tool = toolRegistry.getTool(qualifiedName);
			if (tool != null) {
				ToolSpecification spec = tool.getSpecification();
				sb.append("- **").append(spec.name()).append("**: ").append(spec.description()).append("\n");
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
		List<String> delegations = config.getDelegations();
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
	 * Builds the skill section for injection into a system prompt. Auto skills are injected inline
	 * with their full content. On demand skills are rendered as an XML-style
	 * {@code <available_skills>} catalog with enforcement language for calling {@code loadSkill()}.
	 *
	 * @param skills
	 *        The resolved skills for the agent.
	 * @return The formatted skill section, or empty string if no skills.
	 */
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

		// AUTO skills: inject inline (unchanged)
		for (Skill skill : autoSkills) {
			sb.append("### ").append(skill.description()).append("\n\n");
			sb.append(skill.content()).append("\n\n");
		}

		// ON_DEMAND skills: XML-style catalog with strong enforcement
		if (!onDemandSkills.isEmpty()) {
			sb.append("# Available Skills\n\n");
			sb.append("You MUST call `loadSkill(\"skill-id\")` BEFORE performing any task that matches ");
			sb.append("a skill description. Loading the skill gives you specialized instructions ");
			sb.append("that dramatically improve your output quality.\n\n");

			sb.append("<available_skills>\n");
			for (Skill skill : onDemandSkills) {
				sb.append("  <skill>\n");
				sb.append("    <id>").append(escapeXml(skill.id())).append("</id>\n");
				sb.append("    <description>").append(escapeXml(skill.description())).append("</description>\n");
				sb.append("  </skill>\n");
			}
			sb.append("</available_skills>\n\n");

			sb.append("**Rule:** If a task even partially matches a skill description, call `loadSkill()` first. ");
			sb.append("Do NOT attempt the task without loading the relevant skill.");
		}

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
