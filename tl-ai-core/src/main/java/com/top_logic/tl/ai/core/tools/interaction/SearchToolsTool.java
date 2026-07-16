/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

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
				// Skills reference tools in config notation ({factory}.{tool}), while the registry
				// keys them in provider-safe notation ({factory}__{tool}). Normalize before lookup.
				String registryName = ToolRegistryService.toProviderName(toolName);
				ToolSchema schema = _allTools.get(registryName);
				if (schema != null && matchesQuery(registryName, schema.description(), queryWords)) {
					matched.add(registryName);
					matchedToolNames.add(registryName);
				}
			}

			// Also check if the skill name/description itself matches
			if (matched.isEmpty() && matchesQuery(skillId, skill.description(), queryWords)) {
				for (String toolName : skill.tools()) {
					String registryName = ToolRegistryService.toProviderName(toolName);
					if (_allTools.containsKey(registryName)) {
						matched.add(registryName);
						matchedToolNames.add(registryName);
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
			for (String toolName : skill.tools()) {
				allSkillTools.add(ToolRegistryService.toProviderName(toolName));
			}
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

		// Tools assigned to no skill cannot be loaded via loadSkill, so surfacing them
		// to the agent only sends it chasing tools it can never acquire. Keep them out
		// of the result and instead warn the developer about the misconfiguration.
		if (!ungroupedMatches.isEmpty()) {
			Logger.warn("searchTools query '" + query + "' matched " + ungroupedMatches.size()
				+ " tool(s) assigned to no skill: " + ungroupedMatches
				+ ". Assign them to a skill or they are unreachable for agents.",
				SearchToolsTool.class);
		}

		if (skillMatches.isEmpty()) {
			return "No tools found matching '" + query + "'. "
				+ "Note: searchTools searches tool names and descriptions, not domain-specific "
				+ "functions or APIs. If you're looking for a specific function (e.g. a TLScript "
				+ "function), load the relevant skill first and use its discovery tools instead. "
				+ "This search also does NOT cover runtime-authored TL-Script tools: if the query "
				+ "is a specific tool name (especially one shaped like {factory}__{name} or "
				+ "{factory}.{name}), load TL_agenticAuthoring and check with listScriptToolFactories "
				+ "/ listScriptTools / getScriptTool before concluding the tool does not exist. "
				+ "Do NOT call searchTools again with the same or similar query.";
		}

		return formatResults(skillMatches);
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

	private String formatResults(Map<String, List<String>> skillMatches) {
		StringBuilder sb = new StringBuilder();
		sb.append("Found ").append(skillMatches.size()).append(" relevant skill(s):\n\n");

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

		return sb.toString().trim();
	}
}
