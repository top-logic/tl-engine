/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.langchain4j.service.SystemMessage;

/**
 * Registry that loads on-demand skill prompt fragments from classpath resources and makes them
 * available to agents via {@link SkillTool}.
 *
 * <p>
 * Skills are loaded from {@code com/top_logic/ai/service/skills/<name>.md} on the classpath.
 * Each agent class is mapped to an ordered list of skill names that it can load on demand.
 * </p>
 */
public class SkillRegistry {

	private static final String RESOURCE_BASE = "com/top_logic/ai/service/skills/";

	private final Map<String, Skill> _skills = new LinkedHashMap<>();

	private final Map<Class<?>, List<String>> _agentSkills = new LinkedHashMap<>();

	/**
	 * Loads and returns the skill with the given name.
	 *
	 * <p>
	 * The skill is loaded from {@code com/top_logic/ai/service/skills/<name>.md} on the
	 * classpath. Skills are cached after first load.
	 * </p>
	 *
	 * @param name
	 *        The skill name (e.g. {@code "tlscript/enum-comparison"}).
	 * @return The loaded skill.
	 * @throws IllegalArgumentException
	 *         if the skill resource is not found.
	 */
	public Skill getSkill(String name) {
		return _skills.computeIfAbsent(name, this::loadSkill);
	}

	/**
	 * Registers the ordered list of on-demand skills for an agent class.
	 *
	 * @param agentClass
	 *        The agent interface class.
	 * @param skillNames
	 *        Skill names available on demand.
	 */
	public void registerAgentSkills(Class<?> agentClass, String... skillNames) {
		_agentSkills.put(agentClass, Arrays.asList(skillNames));
	}

	/**
	 * Returns the on-demand skills configured for the given agent class.
	 *
	 * @param agentClass
	 *        The agent interface class.
	 * @return Ordered list of skills, or empty list if none configured.
	 */
	public List<Skill> getSkillsFor(Class<?> agentClass) {
		List<String> names = _agentSkills.getOrDefault(agentClass, Collections.emptyList());
		List<Skill> result = new ArrayList<>(names.size());
		for (String name : names) {
			result.add(getSkill(name));
		}
		return result;
	}

	/**
	 * Returns all registered skill names.
	 */
	public List<String> allSkillNames() {
		return new ArrayList<>(_skills.keySet());
	}

	/**
	 * Builds the full system prompt for an agent by reading its {@link SystemMessage} annotation
	 * and appending a catalog of available on-demand skills (if any are registered).
	 *
	 * <p>
	 * If the agent has no registered skills, the original {@link SystemMessage} text is returned
	 * unchanged.
	 * </p>
	 *
	 * @param agentClass
	 *        The agent interface class (must have a method annotated with {@link SystemMessage}).
	 * @return The complete system prompt.
	 */
	public String buildSystemPrompt(Class<?> agentClass) {
		String basePrompt = readSystemMessageAnnotation(agentClass);
		List<Skill> skills = getSkillsFor(agentClass);
		if (skills.isEmpty()) {
			return basePrompt;
		}
		StringBuilder sb = new StringBuilder(basePrompt);
		sb.append("\n\n---\n\n");
		sb.append("## Available Skills\n\n");
		sb.append("The following skills can be loaded on-demand using the `loadSkill` tool. ");
		sb.append("Load a skill when you need its specialized instructions for the current task.\n\n");
		for (Skill skill : skills) {
			sb.append("- `").append(skill.name()).append("`: ").append(skill.description()).append("\n");
		}
		return sb.toString();
	}

	private static String readSystemMessageAnnotation(Class<?> agentClass) {
		for (Method method : agentClass.getDeclaredMethods()) {
			SystemMessage annotation = method.getAnnotation(SystemMessage.class);
			if (annotation != null) {
				return String.join("\n", annotation.value());
			}
		}
		throw new IllegalArgumentException(
			"No @SystemMessage annotation found on " + agentClass.getSimpleName());
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

	private Skill parseSkill(String name, String raw) {
		if (!raw.startsWith(FRONTMATTER_DELIMITER)) {
			return new Skill(name, name, raw);
		}

		String closingDelimiter = "\n" + FRONTMATTER_DELIMITER + "\n";
		int endIndex = raw.indexOf(closingDelimiter, FRONTMATTER_DELIMITER.length());
		if (endIndex < 0) {
			closingDelimiter = "\n" + FRONTMATTER_DELIMITER;
			endIndex = raw.indexOf(closingDelimiter, FRONTMATTER_DELIMITER.length());
		}
		if (endIndex < 0) {
			return new Skill(name, name, raw);
		}

		String frontmatter = raw.substring(FRONTMATTER_DELIMITER.length(), endIndex).trim();
		String content = raw.substring(endIndex + closingDelimiter.length()).trim();

		String description = name;

		for (String line : frontmatter.split("\n")) {
			int colonIndex = line.indexOf(':');
			if (colonIndex < 0) {
				continue;
			}
			String key = line.substring(0, colonIndex).trim();
			String value = line.substring(colonIndex + 1).trim();
			if ("description".equals(key)) {
				description = value;
			}
		}
		return new Skill(name, description, content);
	}
}
