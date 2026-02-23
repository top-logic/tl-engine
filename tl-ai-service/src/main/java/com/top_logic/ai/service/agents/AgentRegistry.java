/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.ai.service.providers.AnthropicChatModelFactory;
import com.top_logic.ai.service.providers.MistralChatModelFactory;
import com.top_logic.ai.service.providers.OpenAIChatModelFactory;
import com.top_logic.ai.service.skills.Skill;
import com.top_logic.ai.service.skills.SkillRegistry;
import com.top_logic.ai.service.skills.SkillTool;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.help.TLScriptReferenceTool;
import com.top_logic.ai.service.tools.model.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.model.InstanceBrowserTool;
import com.top_logic.ai.service.tools.model.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.model.ModelBrowserTool;
import com.top_logic.ai.service.tools.model.ModelEditorTool;
import com.top_logic.ai.service.tools.model.ModelSearchTool;
import com.top_logic.ai.service.tools.model.TypeAnalysisTool;
import com.top_logic.ai.service.tools.registry.ToolImplementation;
import com.top_logic.ai.service.tools.script.TLScriptTool;
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
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderResult;

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

	private static final Map<String, Supplier<Object>> TOOL_REGISTRY;

	/**
	 * Unmodifiable list of well-known tool names selectable from the configuration UI.
	 */
	public static final List<String> TOOL_NAMES;

	static {
		Map<String, Supplier<Object>> registry = new LinkedHashMap<>();
		registry.put("model-browser", ModelBrowserTool::new);
		registry.put("instance-browser", InstanceBrowserTool::new);
		registry.put("type-analysis", TypeAnalysisTool::new);
		registry.put("model-search", ModelSearchTool::new);
		registry.put("annotation-browser", ModelAnnotationBrowserTool::new);
		registry.put("annotation-explorer", AnnotationDefinitionExplorerTool::new);
		registry.put("model-editor", ModelEditorTool::new);
		registry.put("tlscript-reference", TLScriptReferenceTool::new);
		registry.put("tlscript", TLScriptTool::new);
		TOOL_REGISTRY = Collections.unmodifiableMap(registry);
		TOOL_NAMES = List.copyOf(registry.keySet());
	}

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

		Logger.info("AgentRegistry started with " + getConfig().getAgents().size() + " agent(s) and " +
			skillConfigs.size() + " skill(s).",
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

		return aiService.getChatModel(config.getModel());
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
		String basePrompt = config.getSystemPrompt();

		List<String> skillNames = config.getSkills();
		String skillSection = "";
		if (!skillNames.isEmpty()) {
			List<Skill> skills = new ArrayList<>(skillNames.size());
			for (String skillName : skillNames) {
				try {
					skills.add(_skillRegistry.getSkill(skillName));
				} catch (IllegalArgumentException ex) {
					Logger.warn("Skipping unavailable skill '" + skillName + "': " + ex.getMessage(),
						AgentRegistry.class);
				}
			}
			skillSection = buildSkillSection(skills);
		}

		if (basePrompt.contains(SkillRegistry.SKILLS_PLACEHOLDER)) {
			basePrompt = basePrompt.replace(SkillRegistry.SKILLS_PLACEHOLDER, skillSection);
		} else if (!skillSection.isEmpty()) {
			basePrompt = basePrompt + "\n\n---\n\n" + skillSection;
		}

		return basePrompt;
	}

	/**
	 * Instantiates the standard (annotation-based) tool objects for the given agent configuration.
	 *
	 * <p>
	 * Tools from {@link AgentConfig#getTools()} are instantiated by name from
	 * {@link #TOOL_REGISTRY}. On-demand skill tools are added automatically when the agent has
	 * skills of that mode. Delegation tools are handled separately by
	 * {@link #buildDelegationTools(AgentConfig)}.
	 * </p>
	 *
	 * @param config
	 *        The agent configuration.
	 * @return A list of instantiated tool objects with {@code @Tool}-annotated methods.
	 */
	public List<Object> buildTools(AgentConfig config) {
		List<Object> tools = new ArrayList<>();

		for (String toolName : config.getTools()) {
			Supplier<Object> factory = TOOL_REGISTRY.get(toolName);
			if (factory != null) {
				tools.add(factory.get());
			} else {
				Logger.warn("Agent '" + config.getName() + "' references unknown tool '" + toolName +
					"'. Available: " + TOOL_NAMES, AgentRegistry.class);
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
				tools.add(new SkillTool(_skillRegistry, config.getName()));
			}
		}

		return tools;
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
			.addStringProperty("goal", "Short intent, e.g. 'Explore model structure'")
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

			String toolName = "delegate_to_" + targetName.replace('-', '_');

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
				GenericAgent agent = buildAgent(targetName, GenericAgent.class);
				String result = ThreadContextManager.inSystemInteraction(AgentRegistry.class,
					() -> agent.invoke(message));
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
		List<Object> tools = buildTools(config);
		List<ToolImplementation> delegationTools = buildDelegationTools(config);

		var builder = AgenticServices.agentBuilder(agentInterface)
			.chatModel(model)
			.systemMessageProvider(memoryId -> systemPrompt)
			.tools(tools.toArray());

		if (!delegationTools.isEmpty()) {
			Map<ToolSpecification, ToolExecutor> specMap = new LinkedHashMap<>();
			for (ToolImplementation impl : delegationTools) {
				specMap.put(impl.getSpecification(), impl.getExecutor());
			}
			builder.toolProvider(request -> new ToolProviderResult(specMap));
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

		if (config.getThinkingType() != null) {
			builder.thinkingType(config.getThinkingType().getValue());
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

		for (Skill skill : autoSkills) {
			sb.append("### ").append(skill.description()).append("\n\n");
			sb.append(skill.content()).append("\n\n");
		}

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
