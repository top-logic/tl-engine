/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import com.top_logic.tl.ai.core.security.AiSecurityMode;

/**
 * Source-agnostic representation of an agent definition.
 *
 * <p>
 * Produced by {@link AgentDefinitionQuery} from either service configuration or KB. All
 * downstream consumers ({@link AgentFactory}, chat UI) operate on this record — never on raw KB
 * objects or config items directly.
 * </p>
 *
 * <p>
 * Contains only identity and capabilities. All tuning parameters (temperature, max tokens, etc.)
 * are resolved from the per-agent model assignment in
 * {@link com.top_logic.tl.ai.core.provider.LLMService LLMService}.
 * </p>
 *
 * @param name                  unique agent name
 * @param description           human-readable description (shown in delegation catalogs)
 * @param systemPrompt          the agent's system prompt text
 * @param tools                 qualified tool names (e.g. "model-browser.listModules")
 * @param mcpServers            MCP server names
 * @param skills                skill names (resolved via layered lookup)
 * @param delegations           agent names this agent can delegate to
 * @param securityFloor         the security mode this agent declares for itself, or {@code null} if
 *                              it declares none. A <em>floor</em>: a run of this agent is the
 *                              stricter of the deployment's mode and this one, so an agent can
 *                              demand enforcement the deployment does not require but can never
 *                              relax what it does require. Named a floor here and
 *                              {@code security-mode} in the configuration an administrator writes,
 *                              where it stands next to the deployment's property of that name.
 * @param source                where this definition originated
 */
public record AgentDefinition(
		String name,
		String description,
		String systemPrompt,
		List<String> tools,
		List<String> mcpServers,
		List<String> skills,
		List<String> delegations,
		AiSecurityMode securityFloor,
		AgentSource source) {

	/** */
	public AgentDefinition {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Agent name must not be blank.");
		}
		tools = tools != null ? List.copyOf(tools) : List.of();
		mcpServers = mcpServers != null ? List.copyOf(mcpServers) : List.of();
		skills = skills != null ? List.copyOf(skills) : List.of();
		delegations = delegations != null ? List.copyOf(delegations) : List.of();
	}
}
