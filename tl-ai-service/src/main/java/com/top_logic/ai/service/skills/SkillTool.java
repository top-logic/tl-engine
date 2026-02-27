/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.tools.ToolCallTracking;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool that allows agents to discover and load skills at runtime.
 *
 * <p>
 * Each instance is scoped to a specific agent name, exposing only the skills registered for that
 * agent.
 * </p>
 */
public class SkillTool {

	private final SkillRegistry _registry;

	private final String _agentName;

	/**
	 * Creates a skill tool scoped to the given agent name.
	 *
	 * @param registry
	 *        The skill registry.
	 * @param agentName
	 *        The agent name as declared in the agent configuration.
	 */
	public SkillTool(SkillRegistry registry, String agentName) {
		_registry = registry;
		_agentName = agentName;
	}

	/**
	 * Lists all skills available for this agent.
	 *
	 * <p>
	 * This method is used internally by the system prompt builder to generate the skill catalog. It
	 * is not exposed as a tool because the catalog is already present in the system prompt.
	 * </p>
	 *
	 * @return Formatted skill catalog with names and descriptions.
	 */
	public String listAvailableSkills() {
		List<Skill> skills = _registry.getSkillsFor(_agentName);
		if (skills.isEmpty()) {
			String result = "No skills available for this agent.";
			ToolCallTracking.recordIfTracking("listAvailableSkills", Collections.emptyMap(), result);
			return result;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Available skills (use loadSkill with the skill id to load one):\n");
		for (Skill skill : skills) {
			sb.append("- `").append(skill.id()).append("`: ").append(skill.description()).append("\n");
		}
		String result = sb.toString();
		ToolCallTracking.recordIfTracking("listAvailableSkills", Collections.emptyMap(), result);
		return result;
	}

	/**
	 * Loads a skill by its id, returning its full content.
	 *
	 * @param skillId
	 *        The skill id from the catalog (e.g. {@code "shared/prompting-ModelExplorer"}).
	 * @return The full skill content, or an error message if not available.
	 */
	@Tool("Loads a skill by its id. Returns specialized instructions you MUST follow for the related task. Check the Available Skills section in your system instructions for skill ids and when to use them.")
	public String loadSkill(@P("Skill id from the catalog, e.g. 'shared/prompting-ModelExplorer'") String skillId) {
		Map<String, Object> args = Map.of("skillId", skillId);
		List<Skill> skills = _registry.getSkillsFor(_agentName);
		for (Skill skill : skills) {
			if (skill.id().equals(skillId)) {
				String result = skill.content();
				ToolCallTracking.recordIfTracking("loadSkill", args, result);
				return result;
			}
		}
		String result = "Skill '" + skillId + "' is not available for this agent.";
		ToolCallTracking.recordIfTracking("loadSkill", args, result);
		return result;
	}
}
