/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
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
 *
 * <p>
 * Tools the run's principal may not use are withheld: they are neither added to the catalog nor
 * named in the load message. Announcing them would put an authoritative tool result in the
 * conversation that the next turn's schema filter immediately contradicts, and a model that took it
 * at its word would earn a denial. Which skills are offered here at all is decided one level up, in
 * {@code AgentFactory}.
 * </p>
 *
 * <p>
 * A skill that decision withheld is <em>explained</em> rather than merely refused - see
 * {@code AgentCapabilities.skillAbsenceReason}. "Not available for this agent" covered a withheld
 * skill, a skill this agent never declared and a misspelled id alike, and the model filled the gap
 * with a capability of its own choosing.
 * </p>
 */
public class SkillTool {

	private final Map<String, Skill> _skills;

	private final DynamicToolCatalog _dynamicCatalog;

	private final Function<String, Tool> _toolResolver;

	private final Predicate<Tool> _permitted;

	private final Function<String, String> _absenceReason;

	private final Set<String> _loadedSkills = new HashSet<>();

	/**
	 * Creates a skill tool with dynamic tool loading, refusing an absent skill without explaining it.
	 *
	 * @see #SkillTool(Map, DynamicToolCatalog, Function, Predicate, Set, Function)
	 */
	public SkillTool(Map<String, Skill> skills, DynamicToolCatalog dynamicCatalog,
			Function<String, Tool> toolResolver, Predicate<Tool> permitted,
			Set<String> preloadedSkillIds) {
		this(skills, dynamicCatalog, toolResolver, permitted, preloadedSkillIds, null);
	}

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
	 * @param permitted
	 *        whether the run's principal may use a given tool; tools it rejects are neither
	 *        registered nor announced. May be {@code null}, which permits everything.
	 * @param preloadedSkillIds
	 *        skill IDs to restore from a previous session; their tools are added to the catalog
	 *        immediately. May be {@code null} or empty.
	 * @param absenceReason
	 *        why a skill that is not in {@code skills} is not there, for an id the caller can explain -
	 *        typically {@code AgentCapabilities.skillAbsenceReason}. May be {@code null} or answer
	 *        {@code null}, which leaves the plain "no such skill" message.
	 */
	public SkillTool(Map<String, Skill> skills, DynamicToolCatalog dynamicCatalog,
			Function<String, Tool> toolResolver, Predicate<Tool> permitted,
			Set<String> preloadedSkillIds, Function<String, String> absenceReason) {
		_skills = skills;
		_dynamicCatalog = dynamicCatalog;
		_toolResolver = toolResolver;
		_permitted = permitted != null ? permitted : tool -> true;
		_absenceReason = absenceReason != null ? absenceReason : skillId -> null;

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
		this(skills, dynamicCatalog, toolResolver, null, null);
	}

	/**
	 * Legacy constructor without dynamic tool loading.
	 */
	public SkillTool(Map<String, Skill> skills) {
		this(skills, null, null);
	}

	/**
	 * Every tool name any of these skills declares, loaded or not.
	 *
	 * <p>
	 * What the agent could reach by loading a skill. Handed to the {@link DynamicToolCatalog} so that
	 * a call for an absent tool can say "load the skill that has it" rather than "no such tool".
	 * </p>
	 */
	public Set<String> declaredToolNames() {
		Set<String> names = new HashSet<>();
		for (Skill skill : _skills.values()) {
			for (String toolName : skill.tools()) {
				names.add(toolName);
				Tool tool = _toolResolver == null ? null : _toolResolver.apply(toolName);
				if (tool != null) {
					// The model calls a tool by its bare name, not by the qualified name a skill
					// lists it under.
					names.add(tool.name());
				}
			}
		}
		return names;
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
		truncationHint = Integer.MAX_VALUE, group = ToolGroups.INTERACTION)
	public String loadSkill(@P("Skill id from the catalog") String skillId) {
		Skill skill = _skills.get(skillId);
		if (skill == null) {
			return absenceMessage(skillId);
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

	/**
	 * The answer to a {@code loadSkill} for a skill this agent cannot hand over.
	 *
	 * <p>
	 * The list is the skills this agent really can load, so that following it cannot walk into a second
	 * refusal - the same rule the unknown-tool message follows. An id nobody can explain keeps the
	 * plain sentence it always had: a typo must disclose nothing about capabilities.
	 * </p>
	 */
	private String absenceMessage(String skillId) {
		String reason = _absenceReason.apply(skillId);
		if (reason == null) {
			return "Skill '" + skillId + "' is not available for this agent. Available: "
				+ _skills.keySet();
		}
		return reason + " Available: " + _skills.keySet();
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
		List<String> withheld = new ArrayList<>();

		for (String toolName : skill.tools()) {
			Tool tool = _toolResolver.apply(toolName);
			if (tool == null) {
				Logger.warn("Skill '" + skill.id() + "' references unknown tool: " + toolName,
					SkillTool.class);
				continue;
			}
			// A tool the principal may not use is neither registered nor reported as available:
			// the per-turn schema filter would remove it again anyway, and an authoritative tool
			// result naming a tool the model cannot call is worse than not mentioning it.
			if (!_permitted.test(tool)) {
				withheld.add(tool.name());
				continue;
			}
			// Skip if already in the catalog (from another skill)
			if (_dynamicCatalog.getTool(tool.name()) != null) {
				continue;
			}
			overlay.register(tool);
			added.add(tool.name());
		}

		if (!withheld.isEmpty()) {
			Logger.info("Skill '" + skill.id() + "' loaded without " + withheld.size()
				+ " tools the current user may not use: " + withheld, SkillTool.class);
		}

		if (!overlay.tools().isEmpty()) {
			overlay.freeze();
			_dynamicCatalog.addOverlay(overlay);
		}

		return added;
	}
}
