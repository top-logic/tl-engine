/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.mcp.McpServerRegistry;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SearchToolsTool;
import com.top_logic.tl.ai.core.tools.interaction.LoadedSkillsBinding;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.model.ModelService;

/**
 * Builds {@link AgentLoop} instances from agent definitions stored in the knowledge base.
 *
 * <p>
 * This is the primary entry point for constructing agents from model-based definitions
 * ({@code tl.ai.core:Agent} instances). It delegates provider creation to
 * {@link LLMService#buildProvider(String)} and tool resolution to the service-level
 * {@link ToolRegistryService}.
 * </p>
 *
 * @see AgentDefinitionQuery
 * @see AgentLoop
 */
public class AgentFactory {

	private static final AgentFactory INSTANCE = new AgentFactory();

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
	 * Placeholder marker in system prompts for the skill catalog. When present, the catalog replaces
	 * this marker. Otherwise, the catalog is appended at the end.
	 */
	static final String SKILLS_PLACEHOLDER = "<<AVAILABLE_SKILLS>>";

	/**
	 * Placeholder marker in system prompts for the application model digest. When present, a compact
	 * TypeScript-style model overview replaces this marker. The digest gives agents structural
	 * knowledge without requiring exploration tool calls.
	 *
	 * @see ModelDigestGenerator
	 */
	public static final String MODEL_PLACEHOLDER = "<<APPLICATION_MODEL>>";

	/** Singleton accessor. */
	public static AgentFactory getInstance() {
		return INSTANCE;
	}

	private AgentFactory() {
		// Singleton.
	}

	/**
	 * Returns the name of the entry-point agent as configured in
	 * {@link com.top_logic.tl.ai.core.provider.LLMService.AgentExecutionConfig#getChatEntryPoint()}.
	 *
	 * @return the agent name
	 */
	public String getEntryPointAgentName() {
		return LLMService.getInstance().getEntryPointAgentName();
	}

	/**
	 * Runs an agent by name with an unrestricted {@link ToolContext}.
	 *
	 * <p>
	 * Convenience overload for trusted internal use (scripts, background agents). Uses
	 * {@link ToolContext#EMPTY} which auto-approves all tools. For interactive use with
	 * human-in-the-loop approval, call
	 * {@link #runAgent(String, Conversation, ConversationMemory, ToolContext, AgentEventListener)}.
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
		return runAgent(name, conversation, conversationMemory, ToolContext.EMPTY, listener);
	}

	/**
	 * Runs an agent by name using the tl-ai-core {@link AgentLoop}.
	 *
	 * <p>
	 * Resolves the agent configuration, builds the provider, tool registry, and core
	 * {@link AgentParams}, creates an {@link AgentLoop}, and runs
	 * it. Returns the response text extracted from the last {@link AssistantMessage}.
	 * </p>
	 *
	 * @param name
	 *        The agent name as declared in the configuration.
	 * @param conversation
	 *        The conversation to operate on.
	 * @param conversationMemory
	 *        Optional conversation memory for context management, or {@code null}.
	 * @param context
	 *        Execution context with security policy, approval callback, and transaction scope.
	 * @param listener
	 *        Receives progress events during the run.
	 * @return The response text from the agent, or empty string if no response.
	 */
	public String runAgent(String name, Conversation conversation,
			ConversationMemory conversationMemory, ToolContext context,
			AgentEventListener listener) {
		return runAgent(name, conversation, conversationMemory, context, listener, null).response();
	}

	/**
	 * Runs the named agent with an optional session-scoped tool registry.
	 *
	 * <p>When {@code sessionRegistry} is non-null and the agent has skills,
	 * the existing registry is reused — tools loaded by previous runs in the same session
	 * remain available without re-loading skills.</p>
	 *
	 * @return a {@link RunResult} carrying the response text and the tool registry used
	 */
	public RunResult runAgent(String name, Conversation conversation,
			ConversationMemory conversationMemory, ToolContext context,
			AgentEventListener listener, ToolRegistry sessionRegistry) {
		AgentDefinition agentDef = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agentDef == null) {
			throw new RuntimeException(
				"No agent found with name '" + name + "'.");
		}

		AgentModelAssignment assignment =
			LLMService.getInstance().resolveModelAssignment(agentDef.name());

		AgentParams coreConfig = buildConfig(agentDef, assignment);
		LLMProvider provider = buildProvider(coreConfig.model());
		boolean hasDynamicLoading = !agentDef.skills().isEmpty();
		ToolRegistry registry = sessionRegistry != null && hasDynamicLoading
			? sessionRegistry
			: buildToolCatalog(agentDef);

		validateToolCapability(name, coreConfig.model(), registry);

		int maxToolResultChars = resolveMaxToolResultChars(assignment);

		Conversation effectiveConversation =
			conversationMemory != null ? conversationMemory.conversation() : conversation;
		int messageCountBefore = effectiveConversation.messages().size();

		AgentLoop loop;
		if (conversationMemory != null) {
			loop = new AgentLoop(coreConfig, provider, registry, conversationMemory, maxToolResultChars);
			loop.run(context, listener);
		} else {
			loop = new AgentLoop(coreConfig, provider, registry, maxToolResultChars);
			loop.run(conversation, context, listener);
		}

		return new RunResult(extractResponseText(effectiveConversation, messageCountBefore), registry);
	}

	/**
	 * Result of {@link #runAgent}, carrying both the response text and the tool registry used.
	 *
	 * @param response The final assistant response text.
	 * @param registry The tool registry used during the run.
	 */
	public record RunResult(String response, ToolRegistry registry) {}

	/**
	 * Extracts the response text from the last {@link AssistantMessage} appended at or after
	 * {@code fromIndex}. Only text that appears <em>after</em> the last {@link ToolCall} in the
	 * message is included; pre-tool narration (e.g. "Let me look that up...") is stripped.
	 * When the message contains no tool calls, all text is returned.
	 *
	 * <p>Returns an empty string when the current run added no assistant message, so a prior
	 * assistant turn restored from history is not mistaken for a fresh reply.</p>
	 */
	public static String extractResponseText(Conversation conversation, int fromIndex) {
		List<Message> messages = conversation.messages();
		for (int i = messages.size() - 1; i >= fromIndex; i--) {
			if (messages.get(i) instanceof AssistantMessage assistant) {
				List<ContentPart> content = assistant.content();

				int lastToolIndex = -1;
				for (int j = content.size() - 1; j >= 0; j--) {
					if (content.get(j) instanceof ToolCall) {
						lastToolIndex = j;
						break;
					}
				}

				StringBuilder sb = new StringBuilder();
				for (int j = lastToolIndex + 1; j < content.size(); j++) {
					if (content.get(j) instanceof TextContent text) {
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
	public static String replacePlaceholderOrAppend(String prompt, String placeholder, String section) {
		if (prompt.contains(placeholder)) {
			return prompt.replace(placeholder, section);
		}
		if (!section.isEmpty()) {
			return prompt + "\n\n---\n\n" + section;
		}
		return prompt;
	}

	/**
	 * Builds an {@link AgentLoop} for the agent with the given name.
	 *
	 * @param name
	 *        The agent name as stored in the KB.
	 * @return A ready-to-run agent loop.
	 * @throws IllegalArgumentException
	 *         if no agent with the given name exists
	 */
	public AgentLoop buildAgentLoop(String name) {
		AgentDefinition agentDef = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agentDef == null) {
			throw new IllegalArgumentException("No agent found with name: " + name);
		}

		AgentModelAssignment assignment =
			LLMService.getInstance().resolveModelAssignment(agentDef.name());

		AgentParams config = buildConfig(agentDef, assignment);
		LLMProvider provider = buildProvider(config.model());
		ToolRegistry registry = buildToolCatalog(agentDef);

		int maxToolResultChars = resolveMaxToolResultChars(assignment);

		return new AgentLoop(config, provider, registry, maxToolResultChars);
	}

	/**
	 * Builds the {@link AgentParams} record from an {@link AgentDefinition} and its
	 * {@link AgentModelAssignment}.
	 */
	AgentParams buildConfig(AgentDefinition agent, AgentModelAssignment assignment) {
		LLMService llmRegistry = LLMService.getInstance();
		String model = assignment.model();

		ModelInfo modelInfo = llmRegistry.getModel(model);
		if (modelInfo == null) {
			Logger.warn("No catalog metadata for model '" + model + "'. "
				+ "Using defaults — define the model in model-overrides for accurate limits.",
				AgentFactory.class);
			modelInfo = defaultModelInfo(model);
		}

		String providerId = llmRegistry.getProviderId(model);

		String systemPrompt = agent.systemPrompt();
		systemPrompt = injectSkills(agent, systemPrompt);
		systemPrompt = injectDelegateDescriptions(agent, systemPrompt);
		systemPrompt = injectModelDigest(systemPrompt);

		// maxOutputTokens: assignment override > catalog
		Integer assignedMaxTokens = assignment.maxTokens();
		int maxOutputTokens = assignedMaxTokens != null ? assignedMaxTokens : modelInfo.limits().maxOutput();

		// contextWindowTokens: assignment override > catalog
		Integer assignedContextWindow = assignment.contextWindow();
		int contextWindowTokens = assignedContextWindow != null
			? assignedContextWindow : modelInfo.limits().contextWindow();

		// maxTurns: assignment override > default
		Integer assignedMaxTurns = assignment.maxTurns();
		int maxTurns = assignedMaxTurns != null ? assignedMaxTurns : AgentParams.DEFAULT_MAX_TURNS;

		// maxRetries: assignment override > default
		Integer assignedMaxRetries = assignment.maxRetries();
		int maxRetries = assignedMaxRetries != null ? assignedMaxRetries : AgentParams.DEFAULT_MAX_RETRIES;

		// timeoutSeconds: assignment override > default
		Integer assignedTimeoutSeconds = assignment.timeoutSeconds();
		int timeoutSeconds = assignedTimeoutSeconds != null
			? assignedTimeoutSeconds : AgentParams.DEFAULT_TIMEOUT_SECONDS;

		// toolChoice: assignment override > null (provider default)
		ToolChoice toolChoice = resolveToolChoice(assignment.toolChoice());

		Map<String, Object> modelOptions = buildModelOptions(assignment, modelInfo, providerId);

		return new AgentParams(model, systemPrompt, maxTurns,
			maxOutputTokens, modelOptions, contextWindowTokens,
			maxRetries, timeoutSeconds, toolChoice, msg -> true);
	}

	private static int resolveMaxToolResultChars(AgentModelAssignment assignment) {
		Integer value = assignment.maxToolResultChars();
		return value != null && value > 0 ? value : AgentLoop.DEFAULT_MAX_RESULT_CHARS;
	}

	/**
	 * Validates that the model supports tool calls if the agent has tools assigned.
	 */
	private void validateToolCapability(String agentName, String model, ToolRegistry registry) {
		if (registry.toolSchemas(t -> true).isEmpty()) {
			return;
		}
		LLMService llmRegistry = LLMService.getInstance();
		if (llmRegistry == null) {
			return;
		}
		ModelInfo info = llmRegistry.getModel(model);
		if (info != null && !info.capabilities().toolCall()) {
			throw new IllegalStateException(
				"Agent '" + agentName + "' has tools assigned but model '" + model
					+ "' does not support tool calls.");
		}
	}

	/**
	 * Builds provider-specific model options from the model assignment, gated on model capabilities.
	 */
	private Map<String, Object> buildModelOptions(AgentModelAssignment assignment,
			ModelInfo modelInfo, String providerId) {
		Map<String, Object> options = new HashMap<>();

		if (modelInfo.capabilities().temperature()) {
			Double temperature = assignment.temperature();
			if (temperature != null) {
				options.put("temperature", temperature);
			}

			Double topP = assignment.topP();
			if (topP != null) {
				options.put("topP", topP);
			}
		}

		// Anthropic-specific options
		if ("anthropic".equals(providerId)) {
			if (modelInfo.capabilities().reasoning()) {
				String thinkingEffort = assignment.thinkingEffort();
				if (thinkingEffort != null) {
					options.put("thinking_effort", thinkingEffort.toLowerCase());
				}
			}
			Integer topK = assignment.topK();
			if (topK != null) {
				options.put("top_k", topK);
			}
		}

		// OpenAI/xAI-specific options
		if ("openai".equals(providerId) || "xai".equals(providerId)) {
			if (modelInfo.capabilities().reasoning()) {
				String reasoningEffort = assignment.reasoningEffort();
				if (reasoningEffort != null) {
					options.put("reasoning_effort", reasoningEffort.toLowerCase());
				}
				String reasoningSummary = assignment.reasoningSummary();
				if (reasoningSummary != null) {
					options.put("reasoning_summary", reasoningSummary.toLowerCase());
				}
			}
		}

		return options.isEmpty() ? Map.of() : Map.copyOf(options);
	}

	/**
	 * Creates a fallback {@link ModelInfo} for models without a catalog entry.
	 *
	 * <p>
	 * Assumes conservative defaults: 128k context, 16k output, tool calls and temperature
	 * enabled, reasoning disabled.
	 * </p>
	 */
	private static ModelInfo defaultModelInfo(String model) {
		return new ModelInfo(
			model,
			null,
			new ModelInfo.ModelLimits(128_000, 16_384, 0),
			new ModelInfo.ModelCapabilities(false, true, true, false, false, null, null),
			null, null, false, null);
	}

	/**
	 * Maps the tool choice string to the Java {@link ToolChoice} enum.
	 */
	private static ToolChoice resolveToolChoice(String choice) {
		if (choice == null) {
			return null;
		}
		return switch (choice) {
			case "AUTO" -> ToolChoice.AUTO;
			case "REQUIRED" -> ToolChoice.REQUIRED;
			case "NONE" -> ToolChoice.NONE;
			default -> null;
		};
	}

	/**
	 * Creates an {@link LLMProvider} for the given model name.
	 *
	 * <p>
	 * Delegates to {@link LLMService#buildProvider(String)}.
	 * </p>
	 */
	LLMProvider buildProvider(String modelName) {
		LLMService registry = LLMService.getInstance();
		if (registry == null) {
			throw new IllegalStateException("LLMService is not available.");
		}
		return registry.buildProvider(modelName);
	}

	/**
	 * Builds a {@link ToolRegistry} containing the tools assigned to the agent.
	 *
	 * <p>
	 * When the agent has skills, returns a {@link DynamicToolCatalog} with only bootstrap
	 * tools; skill tools are loaded on demand via {@code loadSkill}. Otherwise returns a
	 * frozen {@link ToolCatalog} with all tools pre-registered.
	 * </p>
	 */
	ToolRegistry buildToolCatalog(AgentDefinition agent) {
		if (!agent.skills().isEmpty()) {
			return buildDynamicToolCatalog(agent);
		}
		return buildStaticToolCatalog(agent);
	}

	private ToolCatalog buildStaticToolCatalog(AgentDefinition agent) {
		ToolCatalog registry =
			new ToolCatalog();

		List<String> toolNames = agent.tools();
		if (!toolNames.isEmpty()) {
			ToolRegistryService serviceRegistry = ToolRegistryService.getInstance();

			for (String toolName : toolNames) {
				Tool tool = serviceRegistry.getTool(toolName);
				if (tool == null) {
					Logger.warn("Tool not found in registry: " + toolName, AgentFactory.class);
					continue;
				}
				try {
					registry.register(tool);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(
						"Agent '" + agent.name() + "': " + e.getMessage()
						+ " (qualified source: '" + toolName + "')", e);
				}
			}
		}

		// Resolve MCP tools from McpServerRegistry
		List<String> mcpServerNames = agent.mcpServers();
		if (!mcpServerNames.isEmpty()
				&& McpServerRegistry.Module.INSTANCE.isActive()) {
			McpServerRegistry mcpRegistry = McpServerRegistry.getInstance();
			for (String serverName : mcpServerNames) {
				List<Tool> mcpTools = mcpRegistry.getTools(serverName);
				if (mcpTools == null) {
					Logger.warn("MCP server not found or not connected: " + serverName,
						AgentFactory.class);
					continue;
				}
				for (Tool mcpTool : mcpTools) {
					registry.register(mcpTool);
				}
			}
		}

		// Inject skill tool if the agent has skills
		SkillTool skillTool = buildSkillTool(agent);
		if (skillTool != null) {
			for (Tool tool : AnnotatedTools.from(skillTool)) {
				registry.register(tool);
			}
		}

		// Inject delegation tools from the agent's delegations list
		List<String> delegationNames = agent.delegations();
		if (!delegationNames.isEmpty()) {
			String parentName = agent.name();
			AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
			for (String targetName : delegationNames) {
				AgentDefinition target = query.findAgentByName(targetName);
				String targetDesc = target != null ? target.description() : "No description";
				String toolName = "delegate_to_" + targetName.replace('-', '_');
				registry.register(new DelegationTool(toolName, targetDesc, targetName, parentName, this::buildAgentLoop));
			}
		}

		return registry.freeze();
	}

	private ToolRegistry buildDynamicToolCatalog(AgentDefinition agent) {
		ToolCatalog baseCatalog = new ToolCatalog();

		// Register only bootstrap tools from the agent's tools list
		List<String> toolNames = agent.tools();
		if (!toolNames.isEmpty()) {
			ToolRegistryService serviceRegistry = ToolRegistryService.getInstance();
			for (String toolName : toolNames) {
				Tool tool = serviceRegistry.getTool(toolName);
				if (tool == null) {
					Logger.warn("Tool not found in registry: " + toolName, AgentFactory.class);
					continue;
				}
				try {
					baseCatalog.register(tool);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(
						"Agent '" + agent.name() + "': " + e.getMessage()
						+ " (qualified source: '" + toolName + "')", e);
				}
			}
		}

		// MCP tools go in the base (architectural, not skill-driven)
		List<String> mcpServerNames = agent.mcpServers();
		if (!mcpServerNames.isEmpty()
				&& McpServerRegistry.Module.INSTANCE.isActive()) {
			McpServerRegistry mcpRegistry = McpServerRegistry.getInstance();
			for (String serverName : mcpServerNames) {
				List<Tool> mcpTools = mcpRegistry.getTools(serverName);
				if (mcpTools == null) {
					Logger.warn("MCP server not found or not connected: " + serverName,
						AgentFactory.class);
					continue;
				}
				for (Tool mcpTool : mcpTools) {
					baseCatalog.register(mcpTool);
				}
			}
		}

		// Delegation tools go in the base (architectural)
		List<String> delegationNames = agent.delegations();
		if (!delegationNames.isEmpty()) {
			String parentName = agent.name();
			AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
			for (String targetName : delegationNames) {
				AgentDefinition target = query.findAgentByName(targetName);
				String targetDesc = target != null ? target.description() : "No description";
				String toolName = "delegate_to_" + targetName.replace('-', '_');
				baseCatalog.register(
					new DelegationTool(toolName, targetDesc, targetName, parentName, this::buildAgentLoop));
			}
		}

		DynamicToolCatalog dynamicCatalog = new DynamicToolCatalog(baseCatalog.freeze());

		// Build SkillTool with dynamic catalog and tool resolver
		Set<String> preloadedSkills = LoadedSkillsBinding.getSkills();
		SkillTool skillTool = buildDynamicSkillTool(agent, dynamicCatalog, preloadedSkills);
		if (skillTool != null) {
			ToolCatalog skillToolOverlay = new ToolCatalog();
			for (Tool tool : AnnotatedTools.from(skillTool)) {
				skillToolOverlay.register(tool);
			}
			dynamicCatalog.addOverlay(skillToolOverlay.freeze());
		}

		// Build SearchToolsTool
		SearchToolsTool searchTool = buildSearchTool(agent);
		if (searchTool != null) {
			ToolCatalog searchOverlay = new ToolCatalog();
			for (Tool tool : AnnotatedTools.from(searchTool)) {
				searchOverlay.register(tool);
			}
			dynamicCatalog.addOverlay(searchOverlay.freeze());
		}

		return dynamicCatalog;
	}

	private SkillTool buildDynamicSkillTool(AgentDefinition agent, DynamicToolCatalog dynamicCatalog,
			Set<String> preloadedSkillIds) {
		List<String> skillNames = agent.skills();
		if (skillNames.isEmpty()) {
			return null;
		}

		Map<String, Skill> skillMap = new LinkedHashMap<>();
		AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
		for (String skillName : skillNames) {
			SkillDefinition sd = query.findSkillByName(skillName);
			if (sd != null && sd.content() != null) {
				skillMap.put(sd.name(), new Skill(sd.name(), sd.description(), sd.content(), sd.tools()));
			} else {
				Logger.warn("Skill not found: " + skillName, AgentFactory.class);
			}
		}

		if (skillMap.isEmpty()) {
			return null;
		}

		Function<String, Tool> resolver = name -> ToolRegistryService.getInstance().getTool(name);
		return new SkillTool(skillMap, dynamicCatalog, resolver, preloadedSkillIds);
	}

	private SearchToolsTool buildSearchTool(AgentDefinition agent) {
		if (agent.skills().isEmpty()) {
			return null;
		}

		// Collect all tool schemas from the global registry
		ToolRegistryService registry = ToolRegistryService.getInstance();
		Map<String, ToolSchema> allTools = new LinkedHashMap<>();
		for (String toolName : registry.getToolNames()) {
			Tool tool = registry.getTool(toolName);
			if (tool != null) {
				allTools.put(toolName, tool.schema());
			}
		}

		// Collect skill definitions with their tools
		Map<String, Skill> skills = new LinkedHashMap<>();
		AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
		for (String skillName : agent.skills()) {
			SkillDefinition sd = query.findSkillByName(skillName);
			if (sd != null) {
				skills.put(sd.name(), new Skill(sd.name(), sd.description(),
					sd.content(), sd.tools()));
			}
		}

		return new SearchToolsTool(allTools, skills);
	}

	/**
	 * Builds a {@link SkillTool} for the agent's skills, or {@code null} if the agent has no
	 * skills.
	 */
	private SkillTool buildSkillTool(AgentDefinition agent) {
		List<String> skillNames = agent.skills();
		if (skillNames.isEmpty()) {
			return null;
		}

		Map<String, Skill> skillMap = new LinkedHashMap<>();
		AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
		for (String skillName : skillNames) {
			SkillDefinition sd = query.findSkillByName(skillName);
			if (sd != null && sd.content() != null) {
				skillMap.put(sd.name(), new Skill(sd.name(), sd.description(), sd.content(), sd.tools()));
			} else {
				Logger.warn("Skill not found: " + skillName, AgentFactory.class);
			}
		}
		return skillMap.isEmpty() ? null : new SkillTool(skillMap);
	}

	/**
	 * Reads skill references from the agent and injects a catalog of available skill names and
	 * descriptions into the system prompt. Full skill content is loaded on demand via
	 * {@link SkillTool}.
	 */
	private String injectSkills(AgentDefinition agent, String systemPrompt) {
		List<String> skillNames = agent.skills();
		String base = systemPrompt != null ? systemPrompt : "";

		if (skillNames.isEmpty()) {
			// Remove placeholder if present, even when there are no skills.
			return base.replace(SKILLS_PLACEHOLDER, "");
		}

		StringBuilder catalog = new StringBuilder();
		catalog.append("### Available Skills\n\n");
		catalog.append("You have specialized skills. ");
		catalog.append("Load a skill BEFORE performing the related action by calling `loadSkill(\"skill-id\")`.\n\n");

		AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
		for (String skillName : skillNames) {
			SkillDefinition sd = query.findSkillByName(skillName);
			if (sd != null) {
				catalog.append("- `").append(sd.name()).append("`: ");
				catalog.append(sd.description() != null ? sd.description() : "No description");
				catalog.append("\n");
			}
		}

		return replacePlaceholderOrAppend(base, SKILLS_PLACEHOLDER, catalog.toString());
	}

	/**
	 * Injects descriptions of available delegate agents into the system prompt.
	 */
	private String injectDelegateDescriptions(AgentDefinition agent, String systemPrompt) {
		List<String> delegationNames = agent.delegations();
		String base = systemPrompt != null ? systemPrompt : "";

		if (delegationNames.isEmpty()) {
			return base.replace(DELEGATES_PLACEHOLDER, "");
		}

		StringBuilder section = new StringBuilder();
		section.append("\n\n### Available Delegates\n\n");
		section.append("You can delegate tasks to these specialized agents:\n\n");

		AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
		for (String targetName : delegationNames) {
			AgentDefinition target = query.findAgentByName(targetName);
			String targetDesc = target != null ? target.description() : "No description";
			String toolName = "delegate_to_" + targetName.replace('-', '_');
			section.append("- **").append(targetName).append("** (`").append(toolName).append("`): ");
			section.append(targetDesc != null ? targetDesc : "No description").append("\n");
		}

		return replacePlaceholderOrAppend(base, DELEGATES_PLACEHOLDER, section.toString());
	}

	/**
	 * Generates a compact model digest and injects it into the system prompt at the
	 * {@link #MODEL_PLACEHOLDER} position. If the placeholder is absent, the digest is not
	 * injected (opt-in per agent).
	 */
	private String injectModelDigest(String systemPrompt) {
		String base = systemPrompt != null ? systemPrompt : "";

		if (!base.contains(MODEL_PLACEHOLDER)) {
			return base;
		}

		com.top_logic.model.TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		Logger.info("Model digest injected (" + digest.length() + " chars):\n" + digest,
			AgentFactory.class);

		return base.replace(MODEL_PLACEHOLDER, digest);
	}
}
