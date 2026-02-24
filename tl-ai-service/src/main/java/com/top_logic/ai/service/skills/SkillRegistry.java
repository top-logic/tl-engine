/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.service.agents.SkillConfig;

/**
 * Registry that loads skill prompt fragments from classpath resources and makes them available to
 * agents via {@link SkillTool}.
 *
 * <p>
 * Skills are loaded from {@code com/top_logic/ai/service/skills/<name>.md} on the classpath. Each
 * agent is mapped by name to an ordered list of skill names. Skills are cached after first load.
 * </p>
 */
public class SkillRegistry {

	private static final String RESOURCE_BASE = "com/top_logic/ai/service/skills/";

	private final Map<String, Skill> _skills = new LinkedHashMap<>();

	private final Map<String, List<String>> _namedAgentSkills = new LinkedHashMap<>();

	/**
	 * Loads and returns the skill with the given name.
	 *
	 * <p>
	 * The skill is loaded from {@code com/top_logic/ai/service/skills/<name>.md} on the classpath.
	 * Skills are cached after first load.
	 * </p>
	 *
	 * @param name
	 *        The skill name (e.g. {@code "shared/prompting-ModelExplorer"}).
	 * @return The loaded skill.
	 * @throws IllegalArgumentException
	 *         if the skill resource is not found.
	 */
	public Skill getSkill(String name) {
		return _skills.computeIfAbsent(name, this::loadSkill);
	}

	/**
	 * Registers the ordered list of skills for an agent identified by name.
	 *
	 * @param agentName
	 *        The agent name as declared in the agent configuration.
	 * @param skillNames
	 *        Skill names available to this agent.
	 */
	public void registerAgentSkills(String agentName, List<String> skillNames) {
		_namedAgentSkills.put(agentName, skillNames);
	}

	/**
	 * Returns the skills configured for the given agent name.
	 *
	 * @param agentName
	 *        The agent name as declared in the agent configuration.
	 * @return Ordered list of skills, or empty list if none configured.
	 */
	public List<Skill> getSkillsFor(String agentName) {
		List<String> names = _namedAgentSkills.getOrDefault(agentName, Collections.emptyList());
		List<Skill> result = new ArrayList<>(names.size());
		for (String name : names) {
			result.add(getSkill(name));
		}
		return result;
	}

	/**
	 * Builds the full system prompt for an agent by injecting a catalog of available skills into the
	 * given base prompt.
	 *
	 * <p>
	 * If the base prompt contains the {@link #SKILLS_PLACEHOLDER} marker, the skill catalog
	 * replaces the marker at that position. Otherwise, the catalog is appended at the end. If the
	 * agent has no registered skills, the marker is removed (or nothing is appended).
	 * </p>
	 *
	 * @param agentName
	 *        The agent name as declared in the agent configuration.
	 * @param basePrompt
	 *        The base system prompt (typically from XML config).
	 * @return The complete system prompt.
	 */
	public String buildSystemPrompt(String agentName, String basePrompt) {
		List<Skill> skills = getSkillsFor(agentName);
		String skillSection = buildSkillSection(skills);

		if (basePrompt.contains(SKILLS_PLACEHOLDER)) {
			return basePrompt.replace(SKILLS_PLACEHOLDER, skillSection);
		}

		if (skills.isEmpty()) {
			return basePrompt;
		}
		return basePrompt + "\n\n---\n\n" + skillSection;
	}

	/**
	 * Registers skills from XML configuration entries.
	 *
	 * <p>
	 * Each {@link SkillConfig} is converted to a {@link Skill} and stored in the registry. This
	 * makes skills available before any agent requests them, fixing the empty-options problem in the
	 * configuration UI.
	 * </p>
	 *
	 * @param skillConfigs
	 *        The skill configurations keyed by name.
	 */
	public void registerSkillsFromConfig(Map<String, SkillConfig> skillConfigs) {
		for (Map.Entry<String, SkillConfig> entry : skillConfigs.entrySet()) {
			SkillConfig sc = entry.getValue();
			_skills.put(sc.getName(), new Skill(
				sc.getName(),
				sc.getDescription(),
				sc.getMode(),
				sc.getContent()));
		}
	}

	/**
	 * Returns all registered skill names.
	 */
	public List<String> allSkillNames() {
		return new ArrayList<>(_skills.keySet());
	}

	/**
	 * Placeholder marker that can be placed in a system prompt to control where the skill catalog
	 * is injected. If the marker is not present, the catalog is appended at the end.
	 *
	 * <p>
	 * Note: Uses angle brackets ({@code <<...>>}) instead of curly braces because
	 * <i>LangChain4j</i> interprets {@code &#123;&#123;...&#125;&#125;} as template variables.
	 * </p>
	 */
	public static final String SKILLS_PLACEHOLDER = "<<AVAILABLE_SKILLS>>";

	private String buildSkillSection(List<Skill> skills) {
		if (skills.isEmpty()) {
			return "";
		}

		List<Skill> autoSkills = skills.stream()
			.filter(s -> s.mode() == Skill.Mode.AUTO)
			.toList();
		List<Skill> onDemandSkills = skills.stream()
			.filter(s -> s.mode() == Skill.Mode.ON_DEMAND)
			.toList();

		StringBuilder sb = new StringBuilder();

		// Auto skills: inject full content directly
		for (Skill skill : autoSkills) {
			sb.append("### ").append(skill.description()).append("\n\n");
			sb.append(skill.content()).append("\n\n");
		}

		// On-demand skills: catalog with loadSkill instructions
		if (!onDemandSkills.isEmpty()) {
			sb.append("### Available Skills (IMPORTANT)\n\n");
			sb.append("You have specialized skills that improve your work. ");
			sb.append(
				"Load a skill BEFORE performing the related action by calling `loadSkill(\"skill-id\")`.\n\n");
			for (Skill skill : onDemandSkills) {
				sb.append("- `").append(skill.id()).append("`: ").append(skill.description()).append("\n");
			}
		}

		return sb.toString();
	}

	private static final String FRONTMATTER_DELIMITER = "---";

	private Skill loadSkill(String name) {
		String resourcePath = RESOURCE_BASE + name + ".md";
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IllegalArgumentException("Skill resource not found: " + resourcePath);
			}
			String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
			return parseSkill(name, raw);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load skill: " + resourcePath, e);
		}
	}

	private Skill parseSkill(String id, String raw) {
		if (!raw.startsWith(FRONTMATTER_DELIMITER)) {
			return new Skill(id, id, Skill.Mode.ON_DEMAND, raw);
		}

		String closingDelimiter = "\n" + FRONTMATTER_DELIMITER + "\n";
		int endIndex = raw.indexOf(closingDelimiter, FRONTMATTER_DELIMITER.length());
		if (endIndex < 0) {
			closingDelimiter = "\n" + FRONTMATTER_DELIMITER;
			endIndex = raw.indexOf(closingDelimiter, FRONTMATTER_DELIMITER.length());
		}
		if (endIndex < 0) {
			return new Skill(id, id, Skill.Mode.ON_DEMAND, raw);
		}

		String frontmatter = raw.substring(FRONTMATTER_DELIMITER.length(), endIndex).trim();
		String content = raw.substring(endIndex + closingDelimiter.length()).trim();

		String description = id;
		Skill.Mode mode = Skill.Mode.ON_DEMAND;

		for (String line : frontmatter.split("\n")) {
			int colonIndex = line.indexOf(':');
			if (colonIndex < 0) {
				continue;
			}
			String key = line.substring(0, colonIndex).trim();
			String value = line.substring(colonIndex + 1).trim();
			if ("description".equals(key)) {
				description = value;
			} else if ("mode".equals(key)) {
				mode = Skill.Mode.parse(value);
			}
		}
		return new Skill(id, description, mode, content);
	}
}
