/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tool that searches the full tool registry by keyword, grouping results by skill.
 */
public class SearchToolsTool {

	private final Map<String, ToolSchema> _allTools;

	private final Map<String, Skill> _skills;

	/**
	 * Creates a search tool.
	 *
	 * @param allTools
	 *        all tools in the registry, keyed by qualified name
	 * @param skills
	 *        all skills available to the agent, keyed by skill id
	 */
	public SearchToolsTool(Map<String, ToolSchema> allTools, Map<String, Skill> skills) {
		_allTools = allTools;
		_skills = skills;
	}

	/**
	 * Search for tools by keyword query.
	 *
	 * @param query
	 *        description of what you need (e.g. "modify data model", "execute scripts")
	 * @return matching tools grouped by skill, with instructions on how to load them
	 */
	@ToolMethod(
		description = "Search for available tools by keyword. Returns matching tools grouped by skill. Use ONLY when you don't know which skill to load. This searches tool names and descriptions, NOT domain-specific functions like TLScript functions. For those, load the relevant skill and use its discovery tools. Do not call repeatedly with similar queries.",
		mode = ToolMode.READ,
		concurrencySafe = true)
	public String searchTools(
			@P("Description of what you need, e.g. 'modify data model' or 'execute scripts'") String query) {

		String[] queryWords = query.toLowerCase(Locale.ROOT).split("\\s+");

		// Build skill->matched tools mapping
		Map<String, List<String>> skillMatches = new LinkedHashMap<>();
		Set<String> matchedToolNames = new LinkedHashSet<>();

		for (Map.Entry<String, Skill> skillEntry : _skills.entrySet()) {
			String skillId = skillEntry.getKey();
			Skill skill = skillEntry.getValue();
			List<String> matched = new ArrayList<>();

			for (String toolName : skill.tools()) {
				ToolSchema schema = _allTools.get(toolName);
				if (schema != null && matchesQuery(toolName, schema.description(), queryWords)) {
					matched.add(toolName);
					matchedToolNames.add(toolName);
				}
			}

			// Also check if the skill name/description itself matches
			if (matched.isEmpty() && matchesQuery(skillId, skill.description(), queryWords)) {
				for (String toolName : skill.tools()) {
					if (_allTools.containsKey(toolName)) {
						matched.add(toolName);
						matchedToolNames.add(toolName);
					}
				}
			}

			if (!matched.isEmpty()) {
				skillMatches.put(skillId, matched);
			}
		}

		// Find ungrouped matches (tools not assigned to any skill)
		Set<String> allSkillTools = new LinkedHashSet<>();
		for (Skill skill : _skills.values()) {
			allSkillTools.addAll(skill.tools());
		}

		List<String> ungroupedMatches = new ArrayList<>();
		for (Map.Entry<String, ToolSchema> entry : _allTools.entrySet()) {
			String toolName = entry.getKey();
			if (!allSkillTools.contains(toolName) && !matchedToolNames.contains(toolName)) {
				ToolSchema schema = entry.getValue();
				if (matchesQuery(toolName, schema.description(), queryWords)) {
					ungroupedMatches.add(toolName);
				}
			}
		}

		if (skillMatches.isEmpty() && ungroupedMatches.isEmpty()) {
			return "No tools found matching '" + query + "'. "
				+ "Note: searchTools searches tool names and descriptions, not domain-specific "
				+ "functions or APIs. If you're looking for a specific function (e.g. a TLScript "
				+ "function), load the relevant skill first and use its discovery tools instead. "
				+ "Do NOT call searchTools again with the same or similar query.";
		}

		return formatResults(skillMatches, ungroupedMatches);
	}

	private boolean matchesQuery(String name, String description, String[] queryWords) {
		String searchable = (name + " " + description).toLowerCase(Locale.ROOT);
		for (String word : queryWords) {
			if (searchable.contains(word)) {
				return true;
			}
		}
		return false;
	}

	private String formatResults(Map<String, List<String>> skillMatches,
			List<String> ungroupedMatches) {
		StringBuilder sb = new StringBuilder();
		int totalSkills = skillMatches.size() + (ungroupedMatches.isEmpty() ? 0 : 1);
		sb.append("Found ").append(totalSkills).append(" relevant skill(s):\n\n");

		for (Map.Entry<String, List<String>> entry : skillMatches.entrySet()) {
			String skillId = entry.getKey();
			Skill skill = _skills.get(skillId);
			List<String> tools = entry.getValue();

			sb.append("## ").append(skillId)
				.append(" (loadSkill(\"").append(skillId).append("\"))\n");
			sb.append(skill.description()).append("\n");
			sb.append("Matching tools: ");
			if (tools.size() <= 3) {
				sb.append(String.join(", ", tools));
			} else {
				sb.append(String.join(", ", tools.subList(0, 3)));
				sb.append(" (+ ").append(tools.size() - 3).append(" more)");
			}
			sb.append("\n\n");
		}

		if (!ungroupedMatches.isEmpty()) {
			sb.append("## Ungrouped tools\n");
			sb.append("These tools are not part of any skill:\n");
			for (String toolName : ungroupedMatches) {
				ToolSchema schema = _allTools.get(toolName);
				sb.append("- ").append(toolName).append(": ").append(schema.description()).append("\n");
			}
		}

		return sb.toString().trim();
	}
}
