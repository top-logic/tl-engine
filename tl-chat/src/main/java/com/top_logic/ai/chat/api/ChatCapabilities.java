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
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * Builds the catalog the chat UI needs to pick an agent, a model, and MCP servers.
 */
final class ChatCapabilities {

	private ChatCapabilities() {
		// Static helper.
	}

	/** Snapshot of agents, models and MCP servers visible to the current user. */
	static ChatCapabilitiesResponse build() {
		ChatCapabilitiesResponse response = ChatCapabilitiesResponse.create();
		LLMService llm = LLMService.Module.INSTANCE.isActive() ? LLMService.getInstance() : null;
		if (llm != null) {
			response.setEntryPointAgent(llm.getEntryPointAgentName());
		}

		for (AgentDefinition agent : listAgents()) {
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
