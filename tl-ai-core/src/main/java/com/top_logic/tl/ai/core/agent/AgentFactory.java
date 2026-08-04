/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
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
import com.top_logic.tl.ai.core.tools.interaction.LoadedSkillsBinding;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.core.tools.interaction.WaitForSubagentTool;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

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
public class AgentFactory implements DelegationTool.AgentLoopFactory {

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

		AgentCapabilities capabilities = AgentCapabilities.forRun(context.security());

		AgentParams coreConfig = buildConfig(agentDef, assignment, capabilities);
		LLMProvider provider = buildProvider(coreConfig.model());
		boolean hasDynamicLoading = !agentDef.skills().isEmpty();
		ToolRegistry registry = sessionRegistry != null && hasDynamicLoading
			? sessionRegistry
			: buildToolCatalog(agentDef, capabilities);

		validateToolCapability(name, coreConfig.model(), registry);

		int maxToolResultChars = resolveMaxToolResultChars(assignment);

		Conversation effectiveConversation =
			conversationMemory != null ? conversationMemory.conversation() : conversation;
		int messageCountBefore = effectiveConversation.messages().size();

		AgentLoop loop;
		if (conversationMemory != null) {
			// Session memory is created before the agent is known; align its compaction
			// budget with the effective context window (assignment override > catalog).
			conversationMemory.setContextWindowTokens(coreConfig.contextWindowTokens());
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
	 * @param security
	 *        The security context of the run the loop will serve. Its principal decides which skills
	 *        the agent is offered and which capabilities its prompt reports as missing; the child run
	 *        carries the parent's context unchanged, so it can never widen the caller's authority.
	 * @return A ready-to-run agent loop.
	 * @throws IllegalArgumentException
	 *         if no agent with the given name exists
	 */
	@Override
	public AgentLoop buildAgentLoop(String name, SecurityContext security) {
		AgentDefinition agentDef = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agentDef == null) {
			throw new IllegalArgumentException("No agent found with name: " + name);
		}

		AgentModelAssignment assignment =
			LLMService.getInstance().resolveModelAssignment(agentDef.name());

		AgentCapabilities capabilities = AgentCapabilities.forRun(security);
		AgentParams config = buildConfig(agentDef, assignment, capabilities);
		LLMProvider provider = buildProvider(config.model());
		ToolRegistry registry = buildToolCatalog(agentDef, capabilities);

		int maxToolResultChars = resolveMaxToolResultChars(assignment);

		return new AgentLoop(config, provider, registry, maxToolResultChars);
	}

	/**
	 * Builds an {@link AgentLoop} for an asynchronous subagent: a faithful clone of the named
	 * agent — same provider/config and the same full tool catalog (skills, {@code loadSkill}, and
	 * the agent's complete read <em>and</em> write tool set) — with the single exception that the
	 * subagent may not spawn further subagents.
	 *
	 * <p>
	 * Concretely this builds the same catalog as {@link #buildAgentLoop} but with
	 * {@code includeDelegation=false}, so the {@code delegate_to_*} / {@code wait_for_subagent}
	 * tools are omitted (delegation depth stays bounded — a subagent is depth-1). Everything else
	 * is at full parity with the parent agent, so a subagent has the very same capabilities the
	 * user would get by talking to that agent directly — including skill-provided tools such as
	 * web access ({@code TL_webAccess}).
	 * </p>
	 */
	@Override
	public AgentLoop buildSubagentLoop(String name, SecurityContext security) {
		AgentDefinition agentDef = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agentDef == null) {
			throw new IllegalArgumentException("No agent found with name: " + name);
		}
		AgentModelAssignment assignment =
			LLMService.getInstance().resolveModelAssignment(agentDef.name());
		AgentCapabilities capabilities = AgentCapabilities.forRun(security);
		AgentParams config = buildConfig(agentDef, assignment, capabilities);
		LLMProvider provider = buildProvider(config.model());
		// Full parity with the parent agent's toolset, minus the ability to spawn subagents.
		ToolRegistry registry = agentDef.skills().isEmpty()
			? buildStaticToolCatalog(agentDef, false, false, capabilities)
			: buildDynamicToolCatalog(agentDef, false, false, capabilities);
		int maxToolResultChars = resolveMaxToolResultChars(assignment);
		return new AgentLoop(config, provider, registry, maxToolResultChars);
	}

	/**
	 * Returns {@code true} if the tool has a read-only mode ({@code READ} or {@code USER_SCOPED}).
	 */
	static boolean isReadOnlyTool(Tool tool) {
		ToolMode mode = tool.mode();
		return mode == ToolMode.READ
			|| mode == ToolMode.USER_SCOPED;
	}

	/**
	 * Builds the {@link AgentParams} record from an {@link AgentDefinition} and its
	 * {@link AgentModelAssignment}.
	 */
	AgentParams buildConfig(AgentDefinition agent, AgentModelAssignment assignment,
			AgentCapabilities capabilities) {
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
		systemPrompt = injectSkills(agent, systemPrompt, capabilities);
		systemPrompt = injectDelegateDescriptions(agent, systemPrompt, capabilities);

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
	ToolRegistry buildToolCatalog(AgentDefinition agent, AgentCapabilities capabilities) {
		if (!agent.skills().isEmpty()) {
			return buildDynamicToolCatalog(agent, true, false, capabilities);
		}
		return buildStaticToolCatalog(agent, true, false, capabilities);
	}

	private ToolCatalog buildStaticToolCatalog(AgentDefinition agent, boolean includeDelegation, boolean readOnly,
			AgentCapabilities capabilities) {
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
				if (!readOnly || isReadOnlyTool(tool)) {
					try {
						registry.register(tool);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(
							"Agent '" + agent.name() + "': " + e.getMessage()
							+ " (qualified source: '" + toolName + "')", e);
					}
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
					if (!readOnly || isReadOnlyTool(mcpTool)) {
						registry.register(mcpTool);
					}
				}
			}
		}

		// Inject skill tool if the agent has skills (but not for read-only subagents to avoid runtime capability escalation)
		if (!readOnly) {
			SkillTool skillTool = buildSkillTool(agent, capabilities);
			if (skillTool != null) {
				for (Tool tool : AnnotatedTools.from(skillTool)) {
					registry.register(tool);
				}
			}
		}

		// Inject delegation tools from the agent's delegations list
		if (includeDelegation) {
			registerDelegationTools(registry, agent, capabilities);
		}

		return registry.freeze();
	}

	private ToolRegistry buildDynamicToolCatalog(AgentDefinition agent, boolean includeDelegation, boolean readOnly,
			AgentCapabilities capabilities) {
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
				if (!readOnly || isReadOnlyTool(tool)) {
					try {
						baseCatalog.register(tool);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(
							"Agent '" + agent.name() + "': " + e.getMessage()
							+ " (qualified source: '" + toolName + "')", e);
					}
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
					if (!readOnly || isReadOnlyTool(mcpTool)) {
						baseCatalog.register(mcpTool);
					}
				}
			}
		}

		// Delegation tools go in the base (architectural)
		if (includeDelegation) {
			registerDelegationTools(baseCatalog, agent, capabilities);
		}

		DynamicToolCatalog dynamicCatalog = new DynamicToolCatalog(baseCatalog.freeze());

		// Build SkillTool with dynamic catalog and tool resolver
		Set<String> preloadedSkills = LoadedSkillsBinding.getSkills();
		SkillTool skillTool = buildDynamicSkillTool(agent, dynamicCatalog, preloadedSkills, capabilities);
		if (skillTool != null) {
			ToolCatalog skillToolOverlay = new ToolCatalog();
			for (Tool tool : AnnotatedTools.from(skillTool)) {
				skillToolOverlay.register(tool);
			}
			dynamicCatalog.addOverlay(skillToolOverlay.freeze());

			// Not for lookup - so that a call for one of these before its skill is loaded is answered
			// with "load that skill" rather than with "no such tool".
			dynamicCatalog.setReachableBySkill(skillTool.declaredToolNames());
		}

		return dynamicCatalog;
	}

	private SkillTool buildDynamicSkillTool(AgentDefinition agent, DynamicToolCatalog dynamicCatalog,
			Set<String> preloadedSkillIds, AgentCapabilities capabilities) {
		List<String> skillNames = agent.skills();
		if (skillNames.isEmpty()) {
			return null;
		}

		Map<String, Skill> skillMap = buildSkillMap(skillNames, skillLookup(), capabilities);
		if (skillMap.isEmpty()) {
			return null;
		}

		Function<String, Tool> resolver = name -> ToolRegistryService.getInstance().getTool(name);
		return new SkillTool(skillMap, dynamicCatalog, resolver, capabilities.permitted(), preloadedSkillIds);
	}

	/**
	 * Builds a {@link SkillTool} for the agent's skills, or {@code null} if the agent has no
	 * skills.
	 */
	private SkillTool buildSkillTool(AgentDefinition agent, AgentCapabilities capabilities) {
		List<String> skillNames = agent.skills();
		if (skillNames.isEmpty()) {
			return null;
		}

		Map<String, Skill> skillMap = buildSkillMap(skillNames, skillLookup(), capabilities);
		return skillMap.isEmpty() ? null : new SkillTool(skillMap);
	}

	/** Resolves a skill name against the merged config and knowledge-base definitions. */
	private static Function<String, SkillDefinition> skillLookup() {
		AgentDefinitionQuery query = AgentDefinitionQuery.getInstance();
		return query::findSkillByName;
	}

	/**
	 * Resolves the named skills to the {@link Skill}s this run's principal can actually use.
	 *
	 * <p>
	 * Applied to the loadable skill map as well as to the prompt catalog, and for the same reason:
	 * {@code loadSkill} called on a name recovered from an older transcript has to refuse what the
	 * catalog no longer offers, or the two disagree.
	 * </p>
	 *
	 * @param skills
	 *        Resolves a skill name to its definition, or <code>null</code> if there is none.
	 */
	static Map<String, Skill> buildSkillMap(List<String> skillNames,
			Function<String, SkillDefinition> skills, AgentCapabilities capabilities) {
		Map<String, Skill> skillMap = new LinkedHashMap<>();
		for (String skillName : skillNames) {
			SkillDefinition sd = skills.apply(skillName);
			if (sd != null && sd.content() != null) {
				if (capabilities.unusableReason(sd) != null) {
					// Logged once per run where the catalog is generated - see skillCatalog.
					continue;
				}
				skillMap.put(sd.name(), new Skill(sd.name(), sd.description(), sd.content(), sd.tools()));
			} else {
				Logger.warn("Skill not found: " + skillName, AgentFactory.class);
			}
		}
		return skillMap;
	}

	/**
	 * Reads skill references from the agent and injects a catalog of available skill names and
	 * descriptions into the system prompt. Full skill content is loaded on demand via
	 * {@link SkillTool}.
	 *
	 * <p>
	 * Skills that can do nothing for the run's principal are left out, and where the run enforces
	 * tool access the catalog is followed by the capabilities that principal lacks - see
	 * {@link AgentCapabilities}. This is the single place every agent's prompt passes through,
	 * config-defined and knowledge-base-defined alike.
	 * </p>
	 */
	private String injectSkills(AgentDefinition agent, String systemPrompt, AgentCapabilities capabilities) {
		String base = systemPrompt != null ? systemPrompt : "";
		String catalog = skillCatalog(agent.name(), agent.skills(), agent.tools(), skillLookup(), capabilities);
		return replacePlaceholderOrAppend(base, SKILLS_PLACEHOLDER, catalog);
	}

	/**
	 * The generated {@code ### Available Skills} section, empty if the agent declares no skills.
	 *
	 * @param skills
	 *        Resolves a skill name to its definition, or <code>null</code> if there is none.
	 */
	static String skillCatalog(String agentName, List<String> skillNames, List<String> agentToolNames,
			Function<String, SkillDefinition> skills, AgentCapabilities capabilities) {
		if (skillNames.isEmpty()) {
			return "";
		}

		StringBuilder catalog = new StringBuilder();
		catalog.append("### Available Skills\n\n");
		catalog.append("You have specialized skills. ");
		catalog.append("Load a skill BEFORE performing the related action by calling `loadSkill(\"skill-id\")`.\n\n");

		List<SkillDefinition> declared = new ArrayList<>();
		for (String skillName : skillNames) {
			SkillDefinition sd = skills.apply(skillName);
			if (sd == null) {
				continue;
			}
			declared.add(sd);
			String unusable = capabilities.unusableReason(sd);
			if (unusable != null) {
				// Silent capability loss is the failure mode this filter must not have: an
				// operator asked why the agent "forgot how to do X" must find the answer here.
				Logger.info("Agent '" + agentName + "': skill '" + sd.name()
					+ "' is not offered to the current user - " + unusable, AgentFactory.class);
				continue;
			}
			catalog.append("- `").append(sd.name()).append("`: ");
			catalog.append(sd.description() != null ? sd.description() : "No description");
			catalog.append("\n");
		}

		// Every declared skill, not only the offered ones: the capabilities behind a skill that was
		// dropped entirely are exactly the ones worth naming.
		catalog.append(capabilities.notice(agentToolNames, declared));

		return catalog.toString();
	}

	/**
	 * Injects descriptions of available delegate agents into the system prompt.
	 *
	 * <p>
	 * Delegates that can do nothing for the run's principal are left out, and one that can do only
	 * part of what it declares is offered with the capabilities it cannot use named - see
	 * {@link #delegateCatalog}.
	 * </p>
	 */
	private String injectDelegateDescriptions(AgentDefinition agent, String systemPrompt,
			AgentCapabilities capabilities) {
		String base = systemPrompt != null ? systemPrompt : "";

		if (agent.delegations().isEmpty()) {
			return base.replace(DELEGATES_PLACEHOLDER, "");
		}

		return replacePlaceholderOrAppend(base, DELEGATES_PLACEHOLDER,
			delegateCatalog(agent.name(), delegateOffers(agent, capabilities)));
	}

	/**
	 * The generated {@code ### Available Delegates} section, empty if the agent has no delegate the
	 * run's principal can use.
	 *
	 * <p>
	 * The single place a dropped delegate is logged, as {@link #skillCatalog} is for a dropped skill:
	 * every surface filters, and logging in each of them would say the same thing three times.
	 * </p>
	 *
	 * @param agentName
	 *        The delegating agent, for the log line.
	 * @param offers
	 *        Its delegates, already judged.
	 */
	static String delegateCatalog(String agentName, List<DelegateOffer> offers) {
		List<DelegateOffer> offered = new ArrayList<>();
		for (DelegateOffer offer : offers) {
			if (offer.offered()) {
				offered.add(offer);
				continue;
			}
			// Silent capability loss is the failure mode this filter must not have: an operator asked
			// why the agent "stopped handing work to X" must find the answer here.
			Logger.info("Agent '" + agentName + "': delegate '" + offer.targetName()
				+ "' is not offered to the current user - " + offer.unusableReason(), AgentFactory.class);
		}
		if (offered.isEmpty()) {
			return "";
		}

		StringBuilder section = new StringBuilder();
		section.append("\n\n### Available Delegates\n\n");
		section.append("You can delegate tasks to these specialized agents:\n\n");

		boolean anyLimited = false;
		for (DelegateOffer offer : offered) {
			section.append("- **").append(offer.targetName()).append("** (`")
				.append(offer.toolName()).append("`): ");
			section.append(offer.description() != null ? offer.description() : "No description")
				.append("\n");
			if (offer.limited()) {
				anyLimited = true;
				section.append("  Limited for this user - it cannot use:");
				for (String capability : offer.unreachableCapabilities()) {
					section.append("\n    - ").append(AgentCapabilities.describeCapability(capability));
				}
				section.append("\n");
			}
		}
		if (anyLimited) {
			// A delegate inherits this run's permissions unchanged, so the parent finding out by
			// sending work that comes back refused is a wasted child run and a wasted turn.
			section.append("\nA delegate marked limited still works, for whatever the capabilities it"
				+ " keeps allow. Do not send it work that needs one of the capabilities named under"
				+ " it: it acts with this user's permissions and would come back refused.\n");
		}

		return section.toString();
	}

	/**
	 * Registers a {@code delegate_to_*} tool for every delegate the run's principal can use.
	 *
	 * <p>
	 * Shared by both catalog paths, because a delegate the prompt does not offer and a
	 * {@code delegate_to_*} tool the model can still call would contradict each other: filtering only
	 * the prompt text would be cosmetic.
	 * </p>
	 */
	void registerDelegationTools(ToolCatalog catalog, AgentDefinition agent,
			AgentCapabilities capabilities) {
		boolean anyOffered = false;
		for (DelegateOffer offer : delegateOffers(agent, capabilities)) {
			if (!offer.offered()) {
				// Logged once per run where the catalog is generated - see delegateCatalog.
				continue;
			}
			catalog.register(new DelegationTool(offer.toolName(), offer.description(),
				offer.targetName(), agent.name(), this));
			anyOffered = true;
		}
		if (anyOffered && catalog.getTool(WaitForSubagentTool.NAME) == null) {
			// delegate_to_* runs asynchronously, so the await tool is wired in alongside it, unless
			// the agent already selected it explicitly (subagent.wait_for_subagent). With nothing to
			// delegate to there is nothing to await either.
			catalog.register(new WaitForSubagentTool());
		}
	}

	/**
	 * The delegates the given agent declares, each judged for the run's principal.
	 *
	 * <p>
	 * Computed once per surface rather than per surface differently: the prompt catalog and the
	 * {@code delegate_to_*} registration both read this list, so they cannot drift apart.
	 * </p>
	 */
	static List<DelegateOffer> delegateOffers(AgentDefinition agent, AgentCapabilities capabilities) {
		List<DelegateOffer> offers = new ArrayList<>();
		for (String targetName : agent.delegations()) {
			AgentDefinition target = capabilities.graph().agent(targetName);
			offers.add(new DelegateOffer(targetName, delegationToolName(targetName),
				target != null ? target.description() : null,
				capabilities.unusableReason(target),
				capabilities.unreachableCapabilities(target)));
		}
		return offers;
	}

	/**
	 * The delegates of the given agent that are offered to the run's principal, in declaration order.
	 */
	public static List<String> offeredDelegates(AgentDefinition agent, AgentCapabilities capabilities) {
		List<String> offered = new ArrayList<>();
		for (DelegateOffer offer : delegateOffers(agent, capabilities)) {
			if (offer.offered()) {
				offered.add(offer.targetName());
			}
		}
		return offered;
	}

	/** The name of the tool that delegates to the agent of the given name. */
	public static String delegationToolName(String targetAgentName) {
		return "delegate_to_" + targetAgentName.replace('-', '_');
	}

	/**
	 * One delegation edge of an agent, judged for the principal of one run.
	 *
	 * @param targetName
	 *        The delegate's agent name.
	 * @param toolName
	 *        The name of the {@code delegate_to_*} tool that reaches it.
	 * @param description
	 *        The delegate's own description, or <code>null</code> if it declares none - or if no agent
	 *        of that name exists.
	 * @param unusableReason
	 *        Why the delegate can do nothing for the principal, or <code>null</code> if it can do
	 *        something.
	 * @param unreachableCapabilities
	 *        The capabilities some of the delegate's tools need and the principal does not hold.
	 */
	record DelegateOffer(String targetName, String toolName, String description,
			String unusableReason, Set<String> unreachableCapabilities) {

		/** Whether the delegate is offered to the run at all. */
		boolean offered() {
			return unusableReason == null;
		}

		/** Whether the delegate is offered but cannot do everything it declares. */
		boolean limited() {
			return offered() && !unreachableCapabilities.isEmpty();
		}
	}
}
