/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.environment.AgentFact;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.tl.ai.core.agent.environment.RunEnvironment;
import com.top_logic.tl.ai.core.agent.environment.RunFactsBinding;
import com.top_logic.tl.ai.core.agent.environment.UserMemorySettings;
import com.top_logic.tl.ai.core.tools.memory.UserMemoryTool;
import com.top_logic.tl.ai.core.agent.environment.TurnContext;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.limits.RunLimits;
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
import com.top_logic.tl.ai.core.provider.EffortLevels;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.tl.ai.core.security.ToolSecurityException;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.LoadedSkillsBinding;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.core.tools.interaction.WaitForSubagentTool;
import com.top_logic.tl.ai.core.tools.introspection.ListToolsTool;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Builds {@link AgentLoop} instances from agent definitions stored in the knowledge base.
 *
 * <p>
 * This is the primary entry point for constructing agents from model-based definitions
 * ({@code tl.ai.core:Agent} instances). It delegates provider creation to
 * {@link LLMService#buildProvider(String, String)} and tool resolution to the service-level
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
	 * an {@link ToolContext#unrestricted()} context which auto-approves all tools. For interactive use with
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
		return runAgent(name, conversation, conversationMemory, ToolContext.unrestricted(), listener);
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
		return runAgent(name, conversation, conversationMemory, context, listener, sessionRegistry,
			RunOverrides.NONE);
	}

	/**
	 * Runs the named agent, applying optional per-run model and MCP overrides from the chat UI.
	 */
	public RunResult runAgent(String name, Conversation conversation,
			ConversationMemory conversationMemory, ToolContext context,
			AgentEventListener listener, ToolRegistry sessionRegistry, RunOverrides overrides) {
		// What this installation carries and what this account may spend, decided before anything of
		// the run is built. The same choke point as authorizeRun below, and for the same reason: the
		// chat, AgentRunner, the scheduler and TL-Script all arrive here, so a ceiling put here is a
		// ceiling on every run rather than on one surface. Released when the loop returns.
		try (RunLimits.Admission admission = RunLimits.admit(context)) {
			return runAdmitted(name, conversation, conversationMemory,
				context.withTokenAllowance(admission.allowance()), listener, sessionRegistry, overrides);
		}
	}

	/**
	 * {@link #runAgent(String, Conversation, ConversationMemory, ToolContext, AgentEventListener, ToolRegistry, RunOverrides)}
	 * for a run that has been let through, with the budget it may spend already on its
	 * {@code context}.
	 */
	private RunResult runAdmitted(String name, Conversation conversation,
			ConversationMemory conversationMemory, ToolContext context,
			AgentEventListener listener, ToolRegistry sessionRegistry, RunOverrides overrides) {
		AgentDefinition agentDef = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agentDef == null) {
			throw new RuntimeException(
				"No agent found with name '" + name + "'.");
		}

		AgentModelAssignment assignment =
			LLMService.getInstance().resolveModelAssignment(agentDef.name());

		// Before anything of the request is applied: what a client asked for is not yet what this run
		// is. The agent's own assignment and its own servers are not a choice and are not checked.
		authorizeRun(context.security(), agentDef, assignment.model(), overrides);

		if (overrides != null && overrides.mcpServers() != null) {
			agentDef = agentDef.withMcpServers(overrides.mcpServers());
		}
		if (overrides != null && overrides.hasModel()) {
			assignment = assignment.withModel(overrides.model());
		}
		if (overrides != null && overrides.hasEffort()) {
			// The effort travels with the model: it is a property of the model the turn runs on, and a
			// model the principal may not choose was already refused above.
			assignment = applyEffort(assignment, overrides.effort());
		}

		AgentCapabilities capabilities = AgentCapabilities.forRun(context.security());

		AgentParams coreConfig = buildConfig(agentDef, assignment, capabilities);
		LLMProvider provider = buildProvider(coreConfig.model(), assignment.provider());

		// Tell the run's tools which agent they are serving, as plain data. The name is resolved
		// here and nowhere else, and publishing it here (rather than from the execution recorder)
		// means an unrecorded run answers the question too - see ExecutionHandle#setAgentName.
		context.executionHandle().setAgentName(agentDef.name());

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
		AgentResult result;
		if (conversationMemory != null) {
			// Session memory is created before the agent is known; align its compaction
			// budget with the effective context window (assignment override > catalog).
			conversationMemory.setContextWindowTokens(coreConfig.contextWindowTokens());
			loop = new AgentLoop(coreConfig, provider, registry, conversationMemory, maxToolResultChars);
			result = loop.run(context, listener);
		} else {
			loop = new AgentLoop(coreConfig, provider, registry, maxToolResultChars);
			result = loop.run(conversation, context, listener);
		}

		return new RunResult(extractResponseText(effectiveConversation, messageCountBefore), registry,
			result);
	}

	/**
	 * Result of {@link #runAgent}, carrying the response text, the tool registry used and why the
	 * loop stopped.
	 *
	 * @param response The final assistant response text.
	 * @param registry The tool registry used during the run.
	 * @param result   Why the loop stopped, and what the provider said if it failed. A caller that
	 *                 has no event listener reads the failure here - the response text of a failed
	 *                 run is the empty string, which is indistinguishable from an empty answer.
	 */
	public record RunResult(String response, ToolRegistry registry, AgentResult result) {}

	/**
	 * Optional per-run overrides supplied by the chat UI.
	 *
	 * <p>
	 * {@code mcpServers == null} keeps the agent's configured servers; a non-null list (including
	 * empty) replaces them for this run only.
	 * </p>
	 *
	 * @param model
	 *        Model override, or {@code null} to keep the agent's assignment
	 * @param mcpServers
	 *        MCP server names for this run, or {@code null} to keep the agent's configured list
	 * @param effort
	 *        How hard the model may think, one of the levels the model offers, or {@code null} to
	 *        keep the agent's assignment
	 */
	public record RunOverrides(String model, List<String> mcpServers, String effort) {

		/** No overrides — use the agent definition as stored. */
		public static final RunOverrides NONE = new RunOverrides(null, null, null);

		/** Overrides without an effort level, for callers that do not offer the setting. */
		public RunOverrides(String model, List<String> mcpServers) {
			this(model, mcpServers, null);
		}

		/** Whether a model override is present. */
		public boolean hasModel() {
			return model != null && !model.isBlank();
		}

		/** Whether an effort override is present. */
		public boolean hasEffort() {
			return effort != null && !effort.isBlank();
		}
	}

	/**
	 * Refuses a run that is not the acting principal's to start this way.
	 *
	 * <p>
	 * Three questions, all of them about what a <em>client</em> asked for rather than about what an
	 * administrator configured: may this principal run this agent, choose this model, attach these MCP
	 * servers. The agent's own assignment and its own server list are passed in and therefore permitted
	 * - a request that echoes back what the agent already uses is not choosing anything, and every
	 * client of {@code /chat/send} does exactly that.
	 * </p>
	 *
	 * <p>
	 * The choke point for every entry point: the chat, {@code AgentRunner}, the scheduler and
	 * TL-Script all arrive here. Refusing before the provider is built means no round trip is spent on
	 * a run that may not happen.
	 * </p>
	 *
	 * @param security
	 *        The security context of the run, carrying the principal's authorization.
	 * @param agentDef
	 *        The agent as stored, before any override is applied.
	 * @param assignedModel
	 *        The model that agent is assigned.
	 * @param overrides
	 *        What the client asked for, may be <code>null</code>.
	 * @throws ToolSecurityException
	 *         If the principal may not run the agent, choose the model or attach one of the servers.
	 */
	private static void authorizeRun(SecurityContext security, AgentDefinition agentDef,
			String assignedModel, RunOverrides overrides) {
		refuse(refusalFor(security.runAuthorization(), agentDef, assignedModel, overrides));
	}

	/**
	 * The agent a request that names none runs as, for the given principal.
	 *
	 * <p>
	 * The configured chat entry point when that principal may run it, and otherwise the first agent
	 * they may run. Nobody chose the entry point: a client that names no agent is not asking for it,
	 * and refusing such a request by its name tells the user about an agent they cannot have and
	 * cannot avoid. A restricted role gets the agent it does have instead.
	 * </p>
	 *
	 * <p>
	 * Resolved in one place because two callers have to agree: the menu reports this as its entry
	 * point, and the run has to be this agent. A client that is told one name and runs another is
	 * the defect this method exists to prevent.
	 * </p>
	 *
	 * <p>
	 * An agent marked {@link AgentDefinition#infrastructure()} is never the answer: the application
	 * runs it <em>for</em> the user, so falling back to it would start a conversation with something
	 * that does not answer as a persona. That is read from the definition rather than taken from a
	 * list the caller keeps, so every caller skips the same agents.
	 * </p>
	 *
	 * @param authorization
	 *        What the acting principal may run.
	 * @return The agent name, or <code>null</code> if the principal may run none at all - which is a
	 *         refusal of its own and a different sentence from "you may not run this one".
	 */
	public static String defaultAgentFor(RunAuthorization authorization) {
		String configured = LLMService.Module.INSTANCE.isActive()
			? LLMService.getInstance().getEntryPointAgentName()
			: null;
		if (canRun(authorization, configured)) {
			return configured;
		}
		for (AgentDefinition agent : agents()) {
			if (canRun(authorization, agent.name())) {
				return agent.name();
			}
		}
		return null;
	}

	/**
	 * Whether a run of this agent would start at all for the given principal.
	 *
	 * <p>
	 * More than the allowlist: an agent whose own model or whose own server a role denies cannot run
	 * for that account either, and falling back to one would answer a client that named no agent with
	 * a refusal it can do nothing about. An {@link AgentDefinition#infrastructure()} agent fails the
	 * same question, because a run the user did not ask for may not be one.
	 * </p>
	 */
	private static boolean canRun(RunAuthorization authorization, String agentName) {
		if (agentName == null || agentName.isBlank()) {
			return false;
		}
		AgentDefinition agentDef;
		try {
			agentDef = AgentDefinitionQuery.getInstance().findAgentByName(agentName);
		} catch (RuntimeException ex) {
			// Cannot look it up, so cannot rule it out - the run itself decides, as it always does.
			Logger.debug("Cannot read '" + agentName + "' to check what it uses.", AgentFactory.class);
			return authorization.allowsAgent(agentName, false);
		}
		if (agentDef == null) {
			return authorization.allowsAgent(agentName, false);
		}
		return authorization.allowsAgent(agentDef.name(), agentDef.infrastructure())
			&& authorization.allowsAgentsOwn(assignedModelOf(agentDef.name()), agentDef.mcpServers());
	}

	/**
	 * Why this run may not use what its agent itself uses, or <code>null</code>.
	 *
	 * <p>
	 * Asked for every run and not only for one that names a model or a server: what an agent is
	 * assigned is not a choice and is therefore permitted - but a {@code <deny>} still forbids it, and
	 * while this went unasked the refusal could be escaped by staying silent. A client that named the
	 * agent's own model was refused, and the identical request without the field ran on exactly that
	 * model.
	 * </p>
	 *
	 * <p>
	 * Audited, unlike {@link RunAuthorization#agentsOwnRefusal}: this is the gate, and a run that is
	 * stopped here belongs in the log.
	 * </p>
	 */
	private static Decision.Denied agentsOwnRefusal(RunAuthorization authorization, String assignedModel,
			Collection<String> declaredServers) {
		Decision.Denied ownModel = authorization.assignedModelRefusal(assignedModel);
		if (ownModel != null) {
			return ownModel;
		}
		if (declaredServers != null) {
			for (String declared : declaredServers) {
				Decision.Denied ownServer = authorization.declaredServerRefusal(declared, declaredServers);
				if (ownServer != null) {
					return ownServer;
				}
			}
		}
		return null;
	}

	/** The model the given agent is assigned, or <code>null</code> while that cannot be resolved. */
	static String assignedModelOf(String agentName) {
		try {
			return LLMService.getInstance().resolveModelAssignment(agentName).model();
		} catch (RuntimeException ex) {
			Logger.debug("No model assignment to check '" + agentName + "' against.", AgentFactory.class);
			return null;
		}
	}

	/** The agents of this application, in the order a client lists them; empty when none can answer. */
	private static Collection<AgentDefinition> agents() {
		try {
			return AgentDefinitionQuery.getInstance().getAllAgents();
		} catch (RuntimeException ex) {
			Logger.warn("Listing the agents of this application failed; no default agent can be"
				+ " resolved.", ex, AgentFactory.class);
			return List.of();
		}
	}

	/**
	 * Why the given principal may not start this run, without starting anything.
	 *
	 * <p>
	 * The same three questions {@link #authorizeRun} asks, for a caller that wants to answer them
	 * before the run rather than through the exception it throws: {@code /chat/send} pre-checks here
	 * so that a refused request comes back as a sentence the user can read instead of as a failed
	 * turn. The run is checked again where it starts, because this one is a courtesy and not the
	 * gate.
	 * </p>
	 *
	 * @param authorization
	 *        What the acting principal may run, from
	 *        {@code AiToolAccessService.effectiveRunAccessFor(Person)}.
	 * @param agentName
	 *        The agent the request names, or the entry point it would fall back to.
	 * @param overrides
	 *        What the request asks for beyond the agent, may be <code>null</code>.
	 * @return The refusal, or <code>null</code> if the run is permitted. A name no agent has is not
	 *         refused here - that is not a denial, and it fails where it always did.
	 */
	public static Decision.Denied runRefusal(RunAuthorization authorization, String agentName,
			RunOverrides overrides) {
		AgentDefinition agentDef = AgentDefinitionQuery.getInstance().findAgentByName(agentName);
		if (agentDef == null) {
			return authorization.agentRefusal(agentName, false);
		}
		return refusalFor(authorization, agentDef, assignedModelOf(agentDef.name()), overrides);
	}

	/** The refusal of one run, or <code>null</code>; the one place the three questions are asked. */
	private static Decision.Denied refusalFor(RunAuthorization authorization, AgentDefinition agentDef,
			String assignedModel, RunOverrides overrides) {
		Decision.Denied agentRefusal =
			authorization.agentRefusal(agentDef.name(), agentDef.infrastructure());
		if (agentRefusal != null) {
			return agentRefusal;
		}

		Decision.Denied ownRefusal =
			agentsOwnRefusal(authorization, assignedModel, agentDef.mcpServers());
		if (ownRefusal != null) {
			return ownRefusal;
		}

		if (overrides == null) {
			return null;
		}
		if (overrides.hasModel()) {
			Decision.Denied modelRefusal = authorization.modelRefusal(overrides.model(), assignedModel);
			if (modelRefusal != null) {
				return modelRefusal;
			}
		}
		if (overrides.mcpServers() != null) {
			for (String serverName : overrides.mcpServers()) {
				Decision.Denied serverRefusal =
					authorization.mcpServerRefusal(serverName, agentDef.mcpServers());
				if (serverRefusal != null) {
					return serverRefusal;
				}
			}
		}
		return null;
	}

	/** Turns a refusal into the exception every entry point reports, or does nothing. */
	private static void refuse(Decision.Denied denied) {
		if (denied != null) {
			throw new ToolSecurityException(denied.reason(), denied.message());
		}
	}

	/**
	 * Puts an effort level into whichever field the model's provider reads it from.
	 *
	 * <p>
	 * A level the model does not offer is dropped with a warning rather than passed on: the two
	 * providers reject an unknown value, and failing the whole run over a UI setting the user can
	 * see but the model cannot use would be the worse answer.
	 * </p>
	 */
	private static AgentModelAssignment applyEffort(AgentModelAssignment assignment, String effort) {
		String model = assignment.model();
		LLMService llm = LLMService.getInstance();
		String providerId = llm.getProviderId(model, assignment.provider());
		ModelInfo info = llm.getModel(model);
		String level = effort.trim().toLowerCase(Locale.ROOT);

		if (!EffortLevels.isValid(providerId, info, level)) {
			Logger.warn("Ignoring effort '" + effort + "' for model '" + model
				+ "': it offers " + EffortLevels.forModel(providerId, info) + ".", AgentFactory.class);
			return assignment;
		}
		return switch (EffortLevels.kindOf(providerId, info)) {
			case THINKING_EFFORT -> assignment.withThinkingEffort(level);
			case REASONING_EFFORT -> assignment.withReasoningEffort(level);
			case NONE -> assignment;
		};
	}

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
		// Delegation is bounded by the acting principal too, not only by the parent agent's delegation
		// list: a child runs on the user's behalf, so an agent the user may not run is not reachable by
		// asking another agent for it. AgentCapabilities drops such an agent from the delegate catalog
		// and from list_agents, so the model normally never asks; this is the gate behind that filter.
		// The same call refuses an infrastructure agent - one the application runs for the user -
		// unless the context says the product, and not the user, named it (#397).
		refuse(security.runAuthorization().agentRefusal(name, agentDef.infrastructure()));

		AgentModelAssignment assignment =
			LLMService.getInstance().resolveModelAssignment(agentDef.name());
		// And what that agent itself uses, for the same reason: a child of an agent whose own model a
		// role denies must not start either, and the catalog filter in front of this is a filter.
		refuse(agentsOwnRefusal(security.runAuthorization(), assignment.model(), agentDef.mcpServers()));

		AgentCapabilities capabilities = AgentCapabilities.forRun(security);
		AgentParams config = buildConfig(agentDef, assignment, capabilities);
		LLMProvider provider = buildProvider(config.model(), assignment.provider());
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
		// See buildAgentLoop: a subagent is the same run under another name.
		refuse(security.runAuthorization().agentRefusal(name, agentDef.infrastructure()));

		AgentModelAssignment assignment =
			LLMService.getInstance().resolveModelAssignment(agentDef.name());
		refuse(agentsOwnRefusal(security.runAuthorization(), assignment.model(), agentDef.mcpServers()));
		AgentCapabilities capabilities = AgentCapabilities.forRun(security);
		AgentParams config = buildConfig(agentDef, assignment, capabilities);
		LLMProvider provider = buildProvider(config.model(), assignment.provider());
		// Full parity with the parent agent's toolset, minus the ability to spawn subagents.
		ToolRegistry registry = agentDef.skills().isEmpty()
			? buildStaticToolCatalog(agentDef, false, capabilities)
			: buildDynamicToolCatalog(agentDef, false, capabilities);
		int maxToolResultChars = resolveMaxToolResultChars(assignment);
		return new AgentLoop(config, provider, registry, maxToolResultChars);
	}

	/**
	 * Whether this installation offers the tool at all.
	 *
	 * <p>
	 * Not a security gate - that is {@link ToolPermissionFilter} and the invocation gate in
	 * {@link AgentLoop}. This answers the narrower question of whether a feature an administrator
	 * can switch off is switched on, so that an agent whose definition still names its tools does
	 * not offer the model something the installation has retired. A refused feature also refuses at
	 * the tool itself, so a catalog built another way is covered too.
	 * </p>
	 */
	static boolean isOffered(Tool tool) {
		if (UserMemoryTool.MEMORY_TOOLS.contains(tool.name())) {
			return UserMemorySettings.enabled();
		}
		return true;
	}

	/**
	 * Builds the {@link AgentParams} record from an {@link AgentDefinition} and its
	 * {@link AgentModelAssignment}.
	 *
	 * <p>
	 * Assembles the agent's own system prompt and the sections generated for the run - the skill
	 * catalog, the capability notice, the delegate catalog. The framework's standing rule about
	 * {@code <untrusted_*>} content is <em>not</em> among them: it is appended by {@link AgentLoop},
	 * which is the one point every run passes through, including the runs that never come through here.
	 * </p>
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

		String providerId = llmRegistry.getProviderId(model, assignment.provider());

		String systemPrompt = agent.systemPrompt();
		systemPrompt = injectSkills(agent, systemPrompt, capabilities);
		systemPrompt = injectDelegateDescriptions(agent, systemPrompt, capabilities);
		systemPrompt = appendContributions(agent, assignment, systemPrompt,
			LLMService.promptContributors());

		// The environment block is composed last, so that a placeholder written into a skill or a
		// delegate description is filled too, and so that the block is the last thing the model reads
		// when the prompt does not say where it goes.
		List<EnvironmentFact> facts = runFacts(agent);
		RunEnvironment environment =
			RunEnvironment.compose(agent.displayName(), agent.name(), model, facts);
		systemPrompt = environment.apply(systemPrompt);

		// The same facts, split by the one question the host answered on each: what holds for the
		// conversation went into the prompt above, what changes with the next click goes into the
		// turn. AgentLoop puts this block beside the user's text; it must never reach the prompt,
		// which has to stay byte-identical across the turns of one conversation.
		String turnContext = TurnContext.compose(facts, RunFactsBinding.turnNumber());

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
			maxRetries, timeoutSeconds, toolChoice, msg -> true, environment.block(), turnContext);
	}

	/**
	 * Appends what the installation's {@link SystemPromptContributor}s say about this run.
	 *
	 * <p>
	 * After the generated sections, so a contributor reads as the installation speaking after the
	 * agent rather than as part of the agent's own instructions, and before the {@code <environment>}
	 * block is composed, so a placeholder in the contributed text is filled like one in the agent's
	 * prompt.
	 * </p>
	 *
	 * <p>
	 * A contributor that throws is skipped with a log line rather than failing the run: losing a
	 * section of standing instructions is a better answer than no answer at all, which is how a
	 * failed {@link com.top_logic.tl.ai.core.agent.environment.AgentFact} is treated too.
	 * </p>
	 */
	static String appendContributions(AgentDefinition agent, AgentModelAssignment assignment,
			String systemPrompt, List<SystemPromptContributor> contributors) {
		String result = systemPrompt == null ? "" : systemPrompt;
		for (SystemPromptContributor contributor : contributors) {
			String text;
			try {
				text = contributor.contribute(agent, assignment);
			} catch (RuntimeException ex) {
				Logger.error("System prompt contributor " + contributor.getClass().getName()
					+ " failed for agent '" + agent.name() + "': " + ex.getMessage(), ex,
					AgentFactory.class);
				continue;
			}
			if (text == null || text.isBlank()) {
				continue;
			}
			String trimmed = result.stripTrailing();
			result = trimmed.isEmpty() ? text.strip() : trimmed + "\n\n" + text.strip();
		}
		return result;
	}

	/**
	 * Everything the host stated about this run: what the agent definition itself says, plus what the
	 * entry point bound for the run - the chat's {@code <context>}, evaluated where the page was.
	 *
	 * <p>
	 * The agent's own facts come first because they hold for every run of that agent, whereas the
	 * page's facts hold for this conversation only. A fact that cannot be evaluated is dropped with a
	 * log line; see {@link AgentFact#evaluate()}.
	 * </p>
	 */
	static List<EnvironmentFact> runFacts(AgentDefinition agent) {
		List<EnvironmentFact> facts = new ArrayList<>();
		for (AgentFact declared : agent.facts()) {
			EnvironmentFact fact = declared.evaluate();
			if (fact != null) {
				facts.add(fact);
			}
		}
		facts.addAll(RunFactsBinding.facts());
		return facts;
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
	 * Creates an {@link LLMProvider} for the given model, served by the named provider.
	 *
	 * <p>
	 * Delegates to {@link LLMService#buildProvider(String, String)}.
	 * </p>
	 *
	 * @param providerName
	 *        The provider the agent's assignment names, or {@code null} to let the model's only
	 *        provider serve it.
	 */
	LLMProvider buildProvider(String modelName, String providerName) {
		LLMService registry = LLMService.getInstance();
		if (registry == null) {
			throw new IllegalStateException("LLMService is not available.");
		}
		return registry.buildProvider(modelName, providerName);
	}

	/**
	 * Builds a {@link ToolRegistry} containing the tools assigned to the agent.
	 *
	 * <p>
	 * When the agent has skills, returns a {@link DynamicToolCatalog} with only bootstrap
	 * tools; skill tools are loaded on demand via {@code loadSkill}. Otherwise returns a
	 * frozen {@link ToolCatalog} with all tools pre-registered.
	 * </p>
	 *
	 * <p>
	 * Public because it is the one place an agent's reachable tool set exists as an object: what a
	 * given user's run can call, what {@code loadSkill} will hand over and what each of the messages
	 * for an absent name says are all answerable from it, and a test that rebuilt an approximation of
	 * it by hand would be asserting about the approximation.
	 * </p>
	 */
	public ToolRegistry buildToolCatalog(AgentDefinition agent, AgentCapabilities capabilities) {
		if (!agent.skills().isEmpty()) {
			return buildDynamicToolCatalog(agent, true, capabilities);
		}
		return buildStaticToolCatalog(agent, true, capabilities);
	}

	private ToolCatalog buildStaticToolCatalog(AgentDefinition agent, boolean includeDelegation,
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
				if (!isOffered(tool)) {
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

		// Inject delegation tools from the agent's delegations list
		if (includeDelegation) {
			registerDelegationTools(registry, agent, capabilities);
		}

		// Last, so that it sees every tool the agent ended up with - delegation tools included.
		// No loadSkill tool: this catalog is built exactly for an agent that declares no skills.
		registry.register(listToolsTool(agent, registry.tools(), null));

		return registry.freeze();
	}

	private ToolRegistry buildDynamicToolCatalog(AgentDefinition agent, boolean includeDelegation,
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
				if (!isOffered(tool)) {
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
		if (includeDelegation) {
			registerDelegationTools(baseCatalog, agent, capabilities);
		}

		DynamicToolCatalog dynamicCatalog = new DynamicToolCatalog(baseCatalog.freeze());

		// What is callable without loading anything, for the self-inspection tool below. The base plus
		// loadSkill itself, which is a tool of this agent like any other.
		List<Tool> ownTools = new ArrayList<>(baseCatalog.tools());

		// Build SkillTool with dynamic catalog and tool resolver
		Set<String> preloadedSkills = LoadedSkillsBinding.getSkills();
		SkillTool skillTool = buildDynamicSkillTool(agent, dynamicCatalog, preloadedSkills, capabilities);
		if (skillTool != null) {
			ToolCatalog skillToolOverlay = new ToolCatalog();
			for (Tool tool : AnnotatedTools.from(skillTool)) {
				skillToolOverlay.register(tool);
				ownTools.add(tool);
			}
			dynamicCatalog.addOverlay(skillToolOverlay.freeze());

			// Not for lookup - so that a call for one of these before its skill is loaded is answered
			// with "load that skill" rather than with "no such tool".
			dynamicCatalog.setReachableBySkill(skillTool.declaredToolNames());
		}

		// Its own overlay rather than the base, because it has to be built after the SkillTool whose
		// state it reports, and the base is frozen by then.
		ToolCatalog selfInspection = new ToolCatalog();
		selfInspection.register(listToolsTool(agent, ownTools, skillTool));
		dynamicCatalog.addOverlay(selfInspection.freeze());

		return dynamicCatalog;
	}

	/**
	 * The agent's {@code listTools} tool: what this agent carries, filtered for whoever calls it.
	 *
	 * <p>
	 * Injected into every agent's catalog rather than declared in {@code <tools>}, for the same reason
	 * {@code loadSkill} is: it is a property of being an agent, not a capability an application decides
	 * to hand out. An agent that could be configured without it would answer a question about a named
	 * tool by guessing - see {@link ListToolsTool} and {@code tl-ai-demo/doc/secdemo/testbed.md} rows C3/C4.
	 * </p>
	 *
	 * @param ownTools
	 *        The tools already in the agent's catalog, which are exactly the ones callable without a
	 *        {@code loadSkill}.
	 * @param skillTool
	 *        The agent's {@code loadSkill} tool, or <code>null</code> if it has none.
	 */
	private static Tool listToolsTool(AgentDefinition agent, List<Tool> ownTools, SkillTool skillTool) {
		List<SkillDefinition> skills = new ArrayList<>();
		Function<String, SkillDefinition> lookup = skillLookup();
		for (String skillName : agent.skills()) {
			SkillDefinition skill = lookup.apply(skillName);
			if (skill != null) {
				skills.add(skill);
			}
		}
		return new ListToolsTool(agent.name(), ownTools, skills, skillTool);
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
		return new SkillTool(skillMap, dynamicCatalog, resolver, capabilities.permitted(),
			preloadedSkillIds, absenceReason(agent, capabilities));
	}

	/**
	 * Why a skill the agent's {@link SkillTool} cannot hand over is not there.
	 *
	 * <p>
	 * The skill map is already filtered, so a withheld skill and a misspelled id both arrive as "no
	 * such entry". Handing the reason down keeps the decision in one place - {@link AgentCapabilities}
	 * - and stops the refusal from being the one message the model has to guess about.
	 * </p>
	 */
	static Function<String, String> absenceReason(AgentDefinition agent,
			AgentCapabilities capabilities) {
		List<String> declared = agent.skills();
		return skillId -> capabilities.skillAbsenceReason(skillId, declared);
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
	 * The generated {@code ### Available Skills} section, followed by the run's capability notice.
	 *
	 * <p>
	 * Either part can be absent. An agent that declares no skills gets no skill section - and still gets
	 * the notice, because what it cannot do is not a property of its skills: it is the one thing its user
	 * has to be told, and an agent whose run is unattended learns that here too. An unenforced run gets
	 * no notice, so an application that has configured no {@code <tool-access>} policy keeps the section
	 * byte for byte.
	 * </p>
	 *
	 * @param skills
	 *        Resolves a skill name to its definition, or <code>null</code> if there is none.
	 */
	static String skillCatalog(String agentName, List<String> skillNames, List<String> agentToolNames,
			Function<String, SkillDefinition> skills, AgentCapabilities capabilities) {
		StringBuilder catalog = new StringBuilder();
		List<SkillDefinition> declared = new ArrayList<>();

		if (!skillNames.isEmpty()) {
			catalog.append("### Available Skills\n\n");
			catalog.append("You have specialized skills. ");
			catalog.append(
				"Load a skill BEFORE performing the related action by calling `loadSkill(\"skill-id\")`.\n\n");

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
		}

		// Every declared skill, not only the offered ones: the capabilities behind a skill that was
		// dropped entirely are exactly the ones worth naming.
		String notice = capabilities.notice(agentToolNames, declared);
		if (catalog.isEmpty()) {
			// The notice separates itself from the skill list above it with a leading newline. With no
			// list, that newline would be the section's first character, and land as a blank line after
			// the rule replacePlaceholderOrAppend writes.
			return notice.stripLeading();
		}
		catalog.append(notice);

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
				+ "' is not offered to the current user - " + offer.unusableReason().message(),
				AgentFactory.class);
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
			AgentCapabilities.Unusable unusable = capabilities.unusableReason(target);
			// Only an offered delegate can carry a "limited for this user" line, and computing the set
			// walks the delegate's whole transitive reachable-tool graph - so a dropped one does not
			// pay for an answer nothing reads. Its own refusal sentence is on the Unusable (#517).
			Set<String> unreachable =
				unusable == null ? capabilities.unreachableCapabilities(target) : Set.of();
			offers.add(new DelegateOffer(targetName, delegationToolName(targetName),
				target != null ? target.description() : null, unusable, unreachable));
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
	 *        The capabilities some of the delegate's tools need and the principal does not hold. Empty
	 *        for a delegate that is not {@link #offered()}: only {@link #limited()} reads it, and
	 *        computing it walks the delegate's whole transitive reachable-tool graph.
	 */
	record DelegateOffer(String targetName, String toolName, String description,
			AgentCapabilities.Unusable unusableReason, Set<String> unreachableCapabilities) {

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
