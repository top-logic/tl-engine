/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.ai.chat.api.messages.AgentOptionDto;
import com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse;
import com.top_logic.ai.chat.api.messages.McpServerOptionDto;
import com.top_logic.ai.chat.api.messages.ModelOptionDto;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.mcp.McpServerRegistry;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.EffortLevels;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.util.Resources;

/**
 * Builds the catalog the chat UI needs to pick an agent, a model, and MCP servers.
 */
final class ChatCapabilities {

	private ChatCapabilities() {
		// Static helper.
	}

	/**
	 * Snapshot of the agents, models and MCP servers the given user may use.
	 *
	 * <p>
	 * Filtered by the agent security policy, so that a client is never offered something a run would
	 * refuse. The refusal itself stays where it belongs - {@code AgentFactory} decides, and this list
	 * only keeps the menu honest.
	 * </p>
	 *
	 * @param person
	 *        The account asking, may be <code>null</code> - who then holds no role.
	 * @param hiddenAgent
	 *        An agent the chat runs for itself rather than on the user's behalf - the prompt
	 *        improver - which therefore must not be offered as a conversation partner; may be
	 *        <code>null</code>.
	 */
	static ChatCapabilitiesResponse build(Person person, String hiddenAgent) {
		ChatCapabilitiesResponse response = ChatCapabilitiesResponse.create();
		LLMService llm = LLMService.Module.INSTANCE.isActive() ? LLMService.getInstance() : null;
		if (llm != null) {
			response.setEntryPointAgent(llm.getEntryPointAgentName());
		}
		RunAuthorization access = runAccess(person);

		// The models and servers the visible agents use themselves are not a choice, so they stay
		// offered whatever the policy hands out beyond them - see RoleAccessPolicy.
		Set<String> assignedModels = new LinkedHashSet<>();
		Set<String> declaredServers = new LinkedHashSet<>();
		for (AgentDefinition agent : listAgents()) {
			if (hiddenAgent != null && hiddenAgent.equals(agent.name())) {
				continue;
			}
			if (access != null && !access.allowsAgent(agent.name())) {
				continue;
			}
			AgentOptionDto dto = toAgentDto(agent, llm);
			if (!dto.getModel().isEmpty()) {
				assignedModels.add(dto.getModel());
			}
			declaredServers.addAll(dto.getMcpServers());
			response.addAgent(dto);
		}

		if (llm != null) {
			for (String modelId : allowed(access, llm.getConfiguredModelNames(), assignedModels, true)) {
				ModelOptionDto dto = ModelOptionDto.create().setId(modelId);
				String provider = llm.getProviderId(modelId);
				dto.setProvider(provider != null ? provider : "");
				ModelInfo info = llm.getModel(modelId);
				if (info != null && info.limits() != null) {
					dto.setContextWindow(info.limits().contextWindow());
					dto.setMaxOutput(info.limits().maxOutput());
				}
				// Empty for a model that cannot reason, or whose provider has no such setting: a
				// UI must not offer a level the provider would reject.
				List<String> levels = EffortLevels.forModel(provider, info);
				dto.getEffortLevels().addAll(levels);
				if (!levels.isEmpty()) {
					String assigned = assignedEffort(llm, modelId);
					dto.setDefaultEffort(assigned != null && levels.contains(assigned)
						? assigned
						: levels.get(levels.size() / 2));
				}
				response.addModel(dto);
			}
		}

		if (McpServerRegistry.Module.INSTANCE.isActive()) {
			McpServerRegistry mcp = McpServerRegistry.getInstance();
			List<String> names = new ArrayList<>(mcp.getConfiguredServerNames());
			names.sort(String::compareTo);
			for (String name : allowed(access, names, declaredServers, false)) {
				response.addMcpServer(McpServerOptionDto.create()
					.setName(name)
					.setStatus(mcp.isConnected(name) ? "connected" : "disconnected"));
			}
		}
		return response;
	}

	/**
	 * What the given account may run, or <code>null</code> while the security service is not running.
	 *
	 * <p>
	 * A menu that shows everything is the right answer for an application whose policy service is
	 * switched off: nothing is being enforced either, and a client offered nothing would look broken.
	 * A run still refuses - {@code AgentFactory} asks the security context, which has no unchecked
	 * mode.
	 * </p>
	 */
	private static RunAuthorization runAccess(Person person) {
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			return null;
		}
		return AiToolAccessService.effectiveRunAccessFor(person);
	}

	/** The candidates the account may pick from, or all of them while nothing is enforced. */
	private static List<String> allowed(RunAuthorization access, Collection<String> candidates,
			Collection<String> agentsOwn, boolean models) {
		if (access == null) {
			return new ArrayList<>(candidates);
		}
		return models
			? access.allowedModels(candidates, agentsOwn)
			: access.allowedMcpServers(candidates, agentsOwn);
	}

	/**
	 * The effort the entry-point agent's assignment already carries for this model, if any.
	 *
	 * <p>
	 * Shown as the default so the menu opens on what a turn would actually use, rather than on a
	 * level the UI invented.
	 * </p>
	 */
	private static String assignedEffort(LLMService llm, String modelId) {
		try {
			AgentModelAssignment assignment =
				llm.resolveModelAssignment(llm.getEntryPointAgentName()).withModel(modelId);
			return assignment.effectiveEffort();
		} catch (RuntimeException ex) {
			Logger.debug("No assignment to read a default effort from for '" + modelId + "'.",
				ChatCapabilities.class);
			return null;
		}
	}

	private static Collection<AgentDefinition> listAgents() {
		try {
			return AgentDefinitionQuery.getInstance().getAllAgents();
		} catch (RuntimeException ex) {
			Logger.warn("Listing knowledge-base agents failed; using configured agents only.",
				ex, ChatCapabilities.class);
			if (TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
				return new ArrayList<>(TopLogicAgentDefinitions.getInstance().getAllConfigAgents().values());
			}
			return List.of();
		}
	}

	/**
	 * What to say about this agent to the person choosing it, in their language.
	 *
	 * <p>
	 * Deliberately not {@link AgentDefinition#description()}: that is a delegation instruction
	 * addressed to a model - it names the arguments to pass and tells the caller not to paste the
	 * user's message - and rendering it as help text is what filled the agent picker with English
	 * model instructions. An agent with nothing to say here says nothing, and is offered by its name
	 * alone.
	 * </p>
	 */
	/*
	 * Read from the definition rather than from a table of the five agents this product ships, which
	 * is what this was until #392. That table left every other agent - a customer's own record, a
	 * consulting installation's - with an empty line under its name, and there was nowhere to write
	 * one. The text now lives where the agent does: a <summary> in the agent's configuration, or the
	 * summary attribute of its record.
	 */
	/**
	 * What to call this agent in front of the user, or nothing if it has no name of its own.
	 *
	 * <p>
	 * Empty rather than the identifier, so that the client - and not this method - decides what to
	 * show when an agent is named only by its identifier.
	 * </p>
	 */
	private static String labelOf(AgentDefinition agent) {
		ResKey displayName = agent.displayName();
		return displayName == null ? "" : Resources.getInstance().getString(displayName, "");
	}

	private static String summaryOf(AgentDefinition agent) {
		ResKey summary = agent.summary();
		return summary == null ? "" : Resources.getInstance().getString(summary, "");
	}

	private static AgentOptionDto toAgentDto(AgentDefinition agent, LLMService llm) {
		AgentOptionDto dto = AgentOptionDto.create()
			.setName(agent.name())
			.setLabel(labelOf(agent))
			.setSummary(summaryOf(agent));
		if (agent.mcpServers() != null) {
			dto.getMcpServers().addAll(agent.mcpServers());
		}
		if (llm != null) {
			try {
				dto.setModel(llm.resolveModel(agent.name()));
			} catch (RuntimeException ignored) {
				dto.setModel("");
			}
		}
		return dto;
	}
}
