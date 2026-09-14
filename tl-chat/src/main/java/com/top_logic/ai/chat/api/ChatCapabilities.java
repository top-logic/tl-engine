/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.ai.chat.api.messages.AgentOptionDto;
import com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse;
import com.top_logic.ai.chat.api.messages.McpServerOptionDto;
import com.top_logic.ai.chat.api.messages.ModelOptionDto;
import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.mcp.McpServerRegistry;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.EffortLevels;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * Builds the catalog the chat UI needs to pick an agent, a model, and MCP servers.
 */
final class ChatCapabilities {

	private ChatCapabilities() {
		// Static helper.
	}

	/**
	 * Snapshot of agents, models and MCP servers visible to the current user.
	 *
	 * @param hiddenAgent
	 *        An agent the chat runs for itself rather than on the user's behalf - the prompt
	 *        improver - which therefore must not be offered as a conversation partner; may be
	 *        <code>null</code>.
	 */
	static ChatCapabilitiesResponse build(String hiddenAgent) {
		ChatCapabilitiesResponse response = ChatCapabilitiesResponse.create();
		LLMService llm = LLMService.Module.INSTANCE.isActive() ? LLMService.getInstance() : null;
		if (llm != null) {
			response.setEntryPointAgent(llm.getEntryPointAgentName());
		}

		for (AgentDefinition agent : listAgents()) {
			if (hiddenAgent != null && hiddenAgent.equals(agent.name())) {
				continue;
			}
			response.addAgent(toAgentDto(agent, llm));
		}

		if (llm != null) {
			for (String modelId : llm.getConfiguredModelNames()) {
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
			for (String name : names) {
				response.addMcpServer(McpServerOptionDto.create()
					.setName(name)
					.setStatus(mcp.isConnected(name) ? "connected" : "disconnected"));
			}
		}
		return response;
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

	private static AgentOptionDto toAgentDto(AgentDefinition agent, LLMService llm) {
		AgentOptionDto dto = AgentOptionDto.create()
			.setName(agent.name())
			.setDescription(agent.description() != null ? agent.description() : "");
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
