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
 * Tool that allows agents to discover and load on-demand skills at runtime.
 *
 * <p>
 * Each instance is scoped to a specific agent class, exposing only the skills registered for that
 * agent.
 * </p>
 */
public class SkillTool {

	private final SkillRegistry _registry;

	private final Class<?> _agentClass;

	/**
	 * Creates a skill tool scoped to the given agent.
	 *
	 * @param registry
	 *        The skill registry.
	 * @param agentClass
	 *        The agent class whose on-demand skills are exposed.
	 */
	public SkillTool(SkillRegistry registry, Class<?> agentClass) {
		_registry = registry;
		_agentClass = agentClass;
	}

	/**
	 * Lists all on-demand skills available for this agent.
	 *
	 * <p>
	 * This method is used internally by the system prompt builder to generate the skill catalog. It
	 * is not exposed as a tool because the catalog is already present in the system prompt.
	 * </p>
	 *
	 * @return Formatted skill catalog with names and descriptions.
	 */
	public String listAvailableSkills() {
		List<Skill> skills = _registry.getSkillsFor(_agentClass);
		if (skills.isEmpty()) {
			String result = "No on-demand skills available for this agent.";
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
	 *        The skill id from the catalog (e.g. {@code "shared/prompting-model-explorer"}).
	 * @return The full skill content, or an error message if not available.
	 */
	@Tool("Loads a skill by its id. Returns specialized instructions you MUST follow for the related task. Check the Available Skills section in your system instructions for skill ids and when to use them.")
	public String loadSkill(@P("Skill id from the catalog, e.g. 'shared/prompting-model-explorer'") String skillId) {
		Map<String, Object> args = Map.of("skillId", skillId);
		List<Skill> skills = _registry.getSkillsFor(_agentClass);
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
