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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that loads skill prompt fragments from classpath resources and composes them into
 * system prompts for agents.
 *
 * <p>
 * Skills are loaded from {@code com/top_logic/ai/service/skills/<name>.md} on the classpath.
 * Each agent class is mapped to an ordered list of skill names. The
 * {@link #composeSystemMessage(Class)} method concatenates all skills for an agent in order,
 * separated by section dividers.
 * </p>
 */
public class SkillRegistry {

	private static final String RESOURCE_BASE = "com/top_logic/ai/service/skills/";

	private static final String SKILL_SEPARATOR = "\n\n---\n\n";

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
	 *        The skill name (e.g. {@code "core/zero-knowledge"}).
	 * @return The loaded skill.
	 * @throws IllegalArgumentException
	 *         if the skill resource is not found.
	 */
	public Skill getSkill(String name) {
		return _skills.computeIfAbsent(name, this::loadSkill);
	}

	/**
	 * Registers the ordered list of skills for an agent class.
	 *
	 * @param agentClass
	 *        The agent interface class.
	 * @param skillNames
	 *        Skill names in composition order.
	 */
	public void registerAgentSkills(Class<?> agentClass, String... skillNames) {
		_agentSkills.put(agentClass, Arrays.asList(skillNames));
	}

	/**
	 * Returns the ordered skills configured for the given agent class.
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
	 * Composes the system prompt for an agent by concatenating its skills in order.
	 *
	 * @param agentClass
	 *        The agent interface class.
	 * @return The composed system prompt string.
	 */
	public String composeSystemMessage(Class<?> agentClass) {
		List<Skill> skills = getSkillsFor(agentClass);
		StringBuilder sb = new StringBuilder();
		for (Skill skill : skills) {
			if (sb.length() > 0) {
				sb.append(SKILL_SEPARATOR);
			}
			sb.append(skill.content());
		}
		return sb.toString();
	}

	/**
	 * Returns all registered skill names.
	 */
	public List<String> allSkillNames() {
		return new ArrayList<>(_skills.keySet());
	}

	private Skill loadSkill(String name) {
		String resourcePath = RESOURCE_BASE + name + ".md";
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IllegalArgumentException("Skill resource not found: " + resourcePath);
			}
			String content = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
			return new Skill(name, name, SkillTier.CORE, content);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load skill: " + resourcePath, e);
		}
	}
}
