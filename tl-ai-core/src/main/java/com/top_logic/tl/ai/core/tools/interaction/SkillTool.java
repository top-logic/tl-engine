/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool that allows agents to load skills at runtime.
 *
 * <p>
 * Each instance is scoped to a specific agent's skill set. The agent sees a catalog of skill names
 * and descriptions in its system prompt and calls {@code loadSkill} to retrieve the full content.
 * When a skill declares tools, they are dynamically added to the agent's tool catalog.
 * </p>
 */
public class SkillTool {

	private final Map<String, Skill> _skills;

	private final DynamicToolCatalog _dynamicCatalog;

	private final Function<String, Tool> _toolResolver;

	private final Set<String> _loadedSkills = new HashSet<>();

	/**
	 * Creates a skill tool with dynamic tool loading.
	 *
	 * @param skills
	 *        Map of skill id to {@link Skill}.
	 * @param dynamicCatalog
	 *        the dynamic catalog to add tool overlays to, or {@code null} for legacy behavior
	 * @param toolResolver
	 *        resolves qualified tool names to {@link Tool} instances, or {@code null} for legacy
	 *        behavior
	 * @param preloadedSkillIds
	 *        skill IDs to restore from a previous session; their tools are added to the catalog
	 *        immediately. May be {@code null} or empty.
	 */
	public SkillTool(Map<String, Skill> skills, DynamicToolCatalog dynamicCatalog,
			Function<String, Tool> toolResolver, Set<String> preloadedSkillIds) {
		_skills = skills;
		_dynamicCatalog = dynamicCatalog;
		_toolResolver = toolResolver;

		if (preloadedSkillIds != null) {
			for (String skillId : preloadedSkillIds) {
				Skill skill = _skills.get(skillId);
				if (skill != null) {
					addSkillTools(skill);
				} else {
					Logger.info("Skipping restore of removed skill: " + skillId, SkillTool.class);
				}
			}
		}
	}

	/**
	 * Creates a skill tool with dynamic tool loading and no preloaded skills.
	 */
	public SkillTool(Map<String, Skill> skills, DynamicToolCatalog dynamicCatalog,
			Function<String, Tool> toolResolver) {
		this(skills, dynamicCatalog, toolResolver, null);
	}

	/**
	 * Legacy constructor without dynamic tool loading.
	 */
	public SkillTool(Map<String, Skill> skills) {
		this(skills, null, null);
	}

	/**
	 * Loads a skill by its id, returning its full content.
	 * If the skill declares tools and this is the first load, those tools are added
	 * to the agent's active tool catalog.
	 *
	 * @param skillId
	 *        The skill id from the catalog.
	 * @return The full skill content with a summary of added tools, or an error message.
	 */
	@ToolMethod(
		description = "Loads a skill by its id. Returns specialized instructions you MUST follow for the related task. When a skill provides tools, they become available immediately. Check the Available Skills section in your system instructions for skill ids and when to use them.",
		mode = ToolMode.READ,
		concurrencySafe = true,
		truncationHint = Integer.MAX_VALUE)
	public String loadSkill(@P("Skill id from the catalog") String skillId) {
		Skill skill = _skills.get(skillId);
		if (skill == null) {
			return "Skill '" + skillId + "' is not available for this agent. Available: "
				+ _skills.keySet();
		}

		// Return short message if skill was already loaded (avoid dumping content twice)
		if (_loadedSkills.contains(skill.id())) {
			return "Skill \"" + skillId + "\" is already loaded. Its tools are available. "
				+ "Do not call loadSkill for this skill again.";
		}

		List<String> addedTools = addSkillTools(skill);

		StringBuilder result = new StringBuilder();
		result.append("Skill \"").append(skillId).append("\" loaded.\n\n");

		if (!addedTools.isEmpty()) {
			result.append("The following tools are now available:\n");
			for (String toolName : addedTools) {
				Tool tool = _dynamicCatalog.getTool(toolName);
				if (tool != null) {
					result.append("- ").append(toolName).append(": ")
						.append(tool.description()).append("\n");
				}
			}
			result.append("\n");
		}

		result.append(skill.content());
		return result.toString();
	}

	private List<String> addSkillTools(Skill skill) {
		if (_dynamicCatalog == null || _toolResolver == null) {
			return List.of();
		}
		if (skill.tools().isEmpty()) {
			return List.of();
		}
		if (_loadedSkills.contains(skill.id())) {
			return List.of();
		}
		_loadedSkills.add(skill.id());
		LoadedSkillsBinding.addSkill(skill.id());

		ToolCatalog overlay = new ToolCatalog();
		List<String> added = new ArrayList<>();

		for (String toolName : skill.tools()) {
			Tool tool = _toolResolver.apply(toolName);
			if (tool == null) {
				Logger.warn("Skill '" + skill.id() + "' references unknown tool: " + toolName,
					SkillTool.class);
				continue;
			}
			// Skip if already in the catalog (from another skill)
			if (_dynamicCatalog.getTool(tool.name()) != null) {
				continue;
			}
			overlay.register(tool);
			added.add(tool.name());
		}

		if (!overlay.tools().isEmpty()) {
			overlay.freeze();
			_dynamicCatalog.addOverlay(overlay);
		}

		return added;
	}
}
