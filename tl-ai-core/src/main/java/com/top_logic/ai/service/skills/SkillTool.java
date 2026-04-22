/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.util.Map;

import com.top_logic.tl.ai.core.tool.P;
import com.top_logic.tl.ai.core.tool.ToolMethod;
import com.top_logic.tl.ai.core.tool.ToolMode;

/**
 * Tool that allows agents to load skills at runtime.
 *
 * <p>
 * Each instance is scoped to a specific agent's skill set. The agent sees a catalog of skill names
 * and descriptions in its system prompt and calls {@code loadSkill} to retrieve the full content.
 * </p>
 */
public class SkillTool {

	private final Map<String, Skill> _skills;

	/**
	 * Creates a skill tool with the given skills.
	 *
	 * @param skills
	 *        Map of skill id to {@link Skill}, representing all skills available to this agent.
	 */
	public SkillTool(Map<String, Skill> skills) {
		_skills = skills;
	}

	/**
	 * Loads a skill by its id, returning its full content.
	 *
	 * @param skillId
	 *        The skill id from the catalog.
	 * @return The full skill content, or an error message if not available.
	 */
	@ToolMethod(
		description = "Loads a skill by its id. Returns specialized instructions you MUST follow for the related task. Check the Available Skills section in your system instructions for skill ids and when to use them.",
		mode = ToolMode.READ,
		concurrencySafe = true)
	public String loadSkill(@P("Skill id from the catalog") String skillId) {
		Skill skill = _skills.get(skillId);
		if (skill == null) {
			return "Skill '" + skillId + "' is not available for this agent. Available: " + _skills.keySet();
		}
		return skill.content();
	}
}
