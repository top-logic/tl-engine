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

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.mcp.McpServerRegistry;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderRegistry;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Builds {@link AgentLoop} instances from agent definitions stored in the knowledge base.
 *
 * <p>
 * This is the primary entry point for constructing agents from model-based definitions
 * ({@code tl.ai.core:Agent} instances). It delegates provider creation to
 * {@link LLMProviderRegistry#buildProvider(String)} and tool resolution to the service-level
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

	/** Singleton accessor. */
	public static AgentFactory getInstance() {
		return INSTANCE;
	}

	private AgentFactory() {
		// Singleton.
	}

	/**
	 * Returns the name of the entry point agent (the agent with {@code entryPoint=true}).
	 *
	 * @return the agent name
	 * @throws IllegalStateException if no entry point agent is defined
	 */
	public String getEntryPointAgentName() {
		for (Agent agent : AgentDefinitionQuery.getInstance().getAllAgents()) {
			if (agent.getEntryPoint()) {
				return agent.getName();
			}
		}
		throw new IllegalStateException("No entry point agent defined in the knowledge base.");
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
		Agent agentObj = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agentObj == null) {
			throw new RuntimeException(
				"No agent found with name '" + name + "' in the knowledge base.");
		}

		AgentParams coreConfig = buildConfig(agentObj);
		LLMProvider provider = buildProvider(coreConfig.model());
		com.top_logic.tl.ai.core.tools.ToolDispatcher registry = buildToolDispatcher(agentObj);

		int maxToolResultChars = resolveMaxToolResultChars(agentObj);

		AgentLoop loop;
		if (conversationMemory != null) {
			loop = new AgentLoop(coreConfig, provider, registry, conversationMemory, maxToolResultChars);
			loop.run(context, listener);
		} else {
			loop = new AgentLoop(coreConfig, provider, registry, maxToolResultChars);
			loop.run(conversation, context, listener);
		}

		return extractResponseText(conversation, conversationMemory);
	}

	/**
	 * Extracts the response text from the last {@link AssistantMessage} in the conversation.
	 */
	static String extractResponseText(Conversation conversation, ConversationMemory memory) {
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
		Agent agent = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agent == null) {
			throw new IllegalArgumentException("No agent found with name: " + name);
		}

		AgentParams config = buildConfig(agent);
		LLMProvider provider = buildProvider(config.model());
		com.top_logic.tl.ai.core.tools.ToolDispatcher registry = buildToolDispatcher(agent);

		int maxToolResultChars = resolveMaxToolResultChars(agent);

		return new AgentLoop(config, provider, registry, maxToolResultChars);
	}

	/**
	 * Builds the {@link AgentParams} record from a KB agent object.
	 */
	AgentParams buildConfig(Agent agent) {
		String model = agent.getModel();
		if (model == null || model.isBlank()) {
			LLMProviderRegistry providerRegistry = LLMProviderRegistry.getInstance();
			model = providerRegistry != null ? providerRegistry.getConfig().getDefaultModel() : null;
		}
		if (model == null) {
			throw new IllegalStateException(
				"Agent '" + agent.getName() + "' has no model and no default model is configured.");
		}

		String systemPrompt = agent.getSystemPrompt();
		systemPrompt = injectSkills(agent, systemPrompt);
		systemPrompt = injectDelegateDescriptions(agent, systemPrompt);

		Integer maxTokens = agent.getMaxTokens();
		int maxOutputTokens = maxTokens != null ? maxTokens : AgentParams.DEFAULT_MAX_OUTPUT_TOKENS;

		Map<String, Object> modelOptions = buildModelOptions(agent);

		return new AgentParams(model, systemPrompt, AgentParams.DEFAULT_MAX_TURNS, maxOutputTokens, modelOptions);
	}

	private static int resolveMaxToolResultChars(Agent agent) {
		Integer value = agent.getMaxToolResultChars();
		return value != null && value > 0 ? value : AgentLoop.DEFAULT_MAX_RESULT_CHARS;
	}

	/**
	 * Builds provider-specific model options from agent attributes.
	 */
	private Map<String, Object> buildModelOptions(Agent agent) {
		Map<String, Object> options = new HashMap<>();

		Double temperature = agent.getTemperature();
		if (temperature != null) {
			options.put("temperature", temperature);
		}

		Double topP = agent.getTopP();
		if (topP != null) {
			options.put("topP", topP);
		}

		return options.isEmpty() ? Map.of() : Map.copyOf(options);
	}

	/**
	 * Creates an {@link LLMProvider} for the given model name.
	 *
	 * <p>
	 * Delegates to {@link LLMProviderRegistry#buildProvider(String)}.
	 * </p>
	 */
	LLMProvider buildProvider(String modelName) {
		LLMProviderRegistry registry = LLMProviderRegistry.getInstance();
		if (registry == null) {
			throw new IllegalStateException("LLMProviderRegistry is not available.");
		}
		return registry.buildProvider(modelName);
	}

	/**
	 * Builds a {@link com.top_logic.tl.ai.core.tools.ToolDispatcher} containing the tools assigned
	 * to the agent.
	 *
	 * <p>
	 * Resolves tool names from the agent's {@code tools} attribute against the service-level
	 * {@link ToolRegistryService} and registers them in a core registry.
	 * </p>
	 */
	com.top_logic.tl.ai.core.tools.ToolDispatcher buildToolDispatcher(Agent agent) {
		com.top_logic.tl.ai.core.tools.ToolDispatcher registry =
			new com.top_logic.tl.ai.core.tools.ToolDispatcher();

		Set<String> toolNames = agent.getTools();
		if (toolNames != null && !toolNames.isEmpty()) {
			ToolRegistryService serviceRegistry = ToolRegistryService.getInstance();

			for (String toolName : toolNames) {
				Tool tool = serviceRegistry.getTool(toolName);
				if (tool == null) {
					Logger.warn("Tool not found in registry: " + toolName, AgentFactory.class);
					continue;
				}
				registry.register(tool);
			}
		}

		// Resolve MCP tools from McpServerRegistry
		Set<String> mcpServerNames = agent.getMcpServers();
		if (mcpServerNames != null && !mcpServerNames.isEmpty()
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

		// Inject delegation tools from the agent's delegations reference
		Set<? extends Agent> delegations = agent.getDelegations();
		if (delegations != null && !delegations.isEmpty()) {
			String parentName = agent.getName();
			for (Agent target : delegations) {
				String targetName = target.getName();
				String targetDesc = target.getDescription();
				if (targetName == null || targetName.isBlank()) {
					continue;
				}
				String toolName = "delegate_to_" + targetName.replace('-', '_');
				registry.register(new DelegationTool(toolName, targetDesc, targetName, parentName, this::buildAgentLoop));
			}
		}

		return registry.freeze();
	}

	/**
	 * Builds a {@link SkillTool} for the agent's KB skills, or {@code null} if the agent has no
	 * skills.
	 */
	private SkillTool buildSkillTool(Agent agent) {
		Set<? extends com.top_logic.tl.ai.core.model.Skill> skills = agent.getSkills();
		if (skills == null || skills.isEmpty()) {
			return null;
		}

		Map<String, Skill> skillMap = new LinkedHashMap<>();
		for (com.top_logic.tl.ai.core.model.Skill skill : skills) {
			String name = skill.getName();
			String description = skill.getDescription();
			String content = skill.getContent();
			if (name != null && content != null) {
				skillMap.put(name, new Skill(name, description, content));
			}
		}
		return skillMap.isEmpty() ? null : new SkillTool(skillMap);
	}

	/**
	 * Reads skill references from the agent and injects a catalog of available skill names and
	 * descriptions into the system prompt. Full skill content is loaded on demand via
	 * {@link SkillTool}.
	 */
	private String injectSkills(Agent agent, String systemPrompt) {
		Set<? extends com.top_logic.tl.ai.core.model.Skill> skills = agent.getSkills();
		String base = systemPrompt != null ? systemPrompt : "";

		if (skills == null || skills.isEmpty()) {
			// Remove placeholder if present, even when there are no skills.
			return base.replace(SKILLS_PLACEHOLDER, "");
		}

		StringBuilder catalog = new StringBuilder();
		catalog.append("### Available Skills\n\n");
		catalog.append("You have specialized skills. ");
		catalog.append("Load a skill BEFORE performing the related action by calling `loadSkill(\"skill-id\")`.\n\n");

		for (com.top_logic.tl.ai.core.model.Skill skill : skills) {
			String skillName = skill.getName();
			String skillDescription = skill.getDescription();
			if (skillName != null) {
				catalog.append("- `").append(skillName).append("`: ");
				catalog.append(skillDescription != null ? skillDescription : "No description");
				catalog.append("\n");
			}
		}

		return replacePlaceholderOrAppend(base, SKILLS_PLACEHOLDER, catalog.toString());
	}

	/**
	 * Injects descriptions of available delegate agents into the system prompt.
	 */
	private String injectDelegateDescriptions(Agent agent, String systemPrompt) {
		Set<? extends Agent> delegations = agent.getDelegations();
		if (delegations == null || delegations.isEmpty()) {
			return systemPrompt != null ? systemPrompt : "";
		}

		StringBuilder section = new StringBuilder();
		section.append("\n\n### Available Delegates\n\n");
		section.append("You can delegate tasks to these specialized agents:\n\n");

		for (Agent target : delegations) {
			String targetName = target.getName();
			String targetDesc = target.getDescription();
			if (targetName == null) continue;

			String toolName = "delegate_to_" + targetName.replace('-', '_');
			section.append("- **").append(targetName).append("** (`").append(toolName).append("`): ");
			section.append(targetDesc != null ? targetDesc : "No description").append("\n");
		}

		String base = systemPrompt != null ? systemPrompt : "";

		if (base.contains(DELEGATES_PLACEHOLDER)) {
			return base.replace(DELEGATES_PLACEHOLDER, section.toString());
		}
		return base + section;
	}
}
