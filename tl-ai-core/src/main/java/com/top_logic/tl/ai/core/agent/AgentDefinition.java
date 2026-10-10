/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.environment.AgentFact;

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
 * <b>Infrastructure agents.</b> {@link #infrastructure()} marks an agent the product runs on a
 * user's behalf - the chat's prompt improver rewrites a draft, it never answers as a persona. Such
 * an agent is not a conversation partner anybody may pick: no menu offers it, and naming it is
 * refused wherever a user's own run would be built, whatever {@code <agent-access>} grants. The
 * exemption is the product's own call path, which says so by building its run with
 * {@link com.top_logic.tl.ai.core.agent.SecurityContext#withoutAgentChoice()}.
 * </p>
 *
 * <p>
 * Contains only identity and capabilities. All tuning parameters (temperature, max tokens, etc.)
 * are resolved from the per-agent model assignment in
 * {@link AgentRuntimeService}.
 * </p>
 *
 * <p>
 * The {@link #name()} is the identifier: the chat entry point names it, skills name it, persisted
 * sessions hold it, and renaming one is a migration. What a user reads is {@link #displayName()},
 * which an installation may change whenever it likes.
 * </p>
 *
 * <p>
 * Two texts describe an agent and they have different readers, which is why they are two fields. The
 * {@link #description()} is a delegation instruction addressed to a model - it names the arguments to
 * pass and tells the caller not to paste the user's message - and rendering it to a person is what
 * once filled the agent picker with English model instructions. The {@link #summary()} is the line
 * written for the person choosing an agent, and it is the only one a user interface shows.
 * </p>
 *
 * @param name                  unique agent name - the identifier, referenced by configuration,
 *                              by skills and by persisted sessions, and never renamed lightly
 * @param displayName           what to call this agent in front of a user, localized;
 *                              {@code null} for an agent that is named by its identifier
 * @param description           what to tell a <em>model</em> that is deciding whether to delegate
 *                              here; never shown to a person - see {@link #summary()}
 * @param summary               one localized line saying what this agent is for, written for the
 *                              person choosing it; {@code null} for an agent that says nothing
 * @param systemPrompt          the agent's system prompt text
 * @param tools                 qualified tool names (e.g. "model-browser.listModules")
 * @param mcpServers            MCP server names
 * @param skills                skill names (resolved via layered lookup)
 * @param delegations           agent names this agent can delegate to
 * @param source                where this definition originated
 * @param facts                 what this agent tells every model about the situation it runs in,
 *                              as TL-Script evaluated in the acting user's context at run start
 * @param infrastructure        whether the application runs this agent <em>for</em> a user rather
 *                              than letting them talk to it - see below
 */
public record AgentDefinition(
		String name,
		ResKey displayName,
		String description,
		ResKey summary,
		String systemPrompt,
		List<String> tools,
		List<String> mcpServers,
		List<String> skills,
		List<String> delegations,
		AgentSource source,
		List<AgentFact> facts,
		boolean infrastructure) {

	/** */
	public AgentDefinition {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Agent name must not be blank.");
		}
		tools = tools != null ? List.copyOf(tools) : List.of();
		mcpServers = mcpServers != null ? List.copyOf(mcpServers) : List.of();
		skills = skills != null ? List.copyOf(skills) : List.of();
		delegations = delegations != null ? List.copyOf(delegations) : List.of();
		facts = facts != null ? List.copyOf(facts) : List.of();
	}

	/**
	 * A definition that states no facts of its own and is an ordinary persona.
	 */
	public AgentDefinition(String name, ResKey displayName, String description, ResKey summary,
			String systemPrompt, List<String> tools, List<String> mcpServers, List<String> skills,
			List<String> delegations, AgentSource source) {
		this(name, displayName, description, summary, systemPrompt, tools, mcpServers, skills,
			delegations, source, List.of(), false);
	}

	/**
	 * A definition that states no facts of its own - every source but the configuration.
	 */
	public AgentDefinition(String name, ResKey displayName, String description, ResKey summary,
			String systemPrompt, List<String> tools, List<String> mcpServers, List<String> skills,
			List<String> delegations, AgentSource source, boolean infrastructure) {
		this(name, displayName, description, summary, systemPrompt, tools, mcpServers, skills,
			delegations, source, List.of(), infrastructure);
	}

	/**
	 * Returns a copy of this definition using a different MCP server list.
	 */
	public AgentDefinition withMcpServers(List<String> newMcpServers) {
		return new AgentDefinition(name, displayName, description, summary, systemPrompt, tools,
			newMcpServers, skills, delegations, source, facts, infrastructure);
	}
}
