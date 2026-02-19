/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import com.top_logic.ai.service.tools.ToolCallTracking;

/**
 * LangChain4j tool that allows agents to discover and load on-demand skills at runtime.
 *
 * <p>
 * Each instance is scoped to a specific agent class, exposing only the skills
 * registered for that agent.
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
	 * @return Formatted skill catalog with names and descriptions.
	 */
	@Tool("Lists all on-demand skills available. Returns skill names with descriptions. Call to discover specialized knowledge you can load for the current task.")
	public String listAvailableSkills() {
		List<Skill> skills = _registry.getSkillsFor(_agentClass);
		if (skills.isEmpty()) {
			String result = "No on-demand skills available for this agent.";
			ToolCallTracking.recordIfTracking("listAvailableSkills", Collections.emptyMap(), result);
			return result;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Available skills (use loadSkill to load one):\n");
		for (Skill skill : skills) {
			sb.append("- ").append(skill.name()).append(": ").append(skill.description()).append("\n");
		}
		String result = sb.toString();
		ToolCallTracking.recordIfTracking("listAvailableSkills", Collections.emptyMap(), result);
		return result;
	}

	/**
	 * Loads a skill by name, returning its full content.
	 *
	 * @param skillName
	 *        The skill name from the catalog (e.g. {@code "tlscript/enum-comparison"}).
	 * @return The full skill content, or an error message if not available.
	 */
	@Tool("Loads a skill by name, returning its full content. Call after listAvailableSkills to load instructions you need for the current task.")
	public String loadSkill(@P("Skill name from the catalog, e.g. 'tlscript/enum-comparison'") String skillName) {
		Map<String, Object> args = Map.of("skillName", skillName);
		List<Skill> skills = _registry.getSkillsFor(_agentClass);
		for (Skill skill : skills) {
			if (skill.name().equals(skillName)) {
				String result = skill.content();
				ToolCallTracking.recordIfTracking("loadSkill", args, result);
				return result;
			}
		}
		String result = "Skill '" + skillName + "' is not available for this agent.";
		ToolCallTracking.recordIfTracking("loadSkill", args, result);
		return result;
	}
}
