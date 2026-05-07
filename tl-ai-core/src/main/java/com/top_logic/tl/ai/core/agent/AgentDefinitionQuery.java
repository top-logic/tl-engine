/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.Skill;
import com.top_logic.tl.ai.core.model.impl.AgentBase;

/**
 * Unified resolution layer for agent and skill definitions.
 *
 * <p>
 * Queries both service configuration (via {@link AgentDefinitionService}) and the knowledge base,
 * merging results with the "KB wins by name" rule. All public methods return source-agnostic
 * {@link AgentDefinition} and {@link SkillDefinition} records.
 * </p>
 */
public class AgentDefinitionQuery {

	/** Meta table name for agent objects. */
	public static final String AGENT_TABLE = "AIAgent";

	/** Meta table name for skill objects. */
	public static final String SKILL_TABLE = "AISkill";

	private static final AgentDefinitionQuery INSTANCE = new AgentDefinitionQuery();

	/** Singleton accessor. */
	public static AgentDefinitionQuery getInstance() {
		return INSTANCE;
	}

	private AgentDefinitionQuery() {
		// Singleton
	}

	/**
	 * Finds an agent definition by name using layered lookup (KB first, then config).
	 *
	 * @return the merged {@link AgentDefinition}, or {@code null} if not found in either source
	 */
	public AgentDefinition findAgentByName(String name) {
		Agent kbAgent = findKBAgentByName(name);
		if (kbAgent != null) {
			AgentSource source = hasConfigAgent(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			return toAgentDefinition(kbAgent, source);
		}
		if (isConfigServiceActive()) {
			return getConfigService().getConfigAgent(name);
		}
		return null;
	}

	/**
	 * All agent definitions merged from config and KB, with KB overriding config by name.
	 */
	public Collection<AgentDefinition> getAllAgents() {
		Map<String, AgentDefinition> result = new LinkedHashMap<>();

		if (isConfigServiceActive()) {
			result.putAll(getConfigService().getAllConfigAgents());
		}

		for (Agent kbAgent : getAllKBAgents()) {
			String name = kbAgent.getName();
			AgentSource source = result.containsKey(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			result.put(name, toAgentDefinition(kbAgent, source));
		}

		return result.values();
	}

	/**
	 * Finds a skill definition by name using layered lookup (KB first, then config).
	 *
	 * @return the merged {@link SkillDefinition}, or {@code null} if not found in either source
	 */
	public SkillDefinition findSkillByName(String name) {
		Skill kbSkill = findKBSkillByName(name);
		if (kbSkill != null) {
			AgentSource source = hasConfigSkill(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			return toSkillDefinition(kbSkill, source);
		}
		if (isConfigServiceActive()) {
			return getConfigService().getConfigSkill(name);
		}
		return null;
	}

	/**
	 * All skill definitions merged from config and KB, with KB overriding config by name.
	 */
	public Collection<SkillDefinition> getAllSkills() {
		Map<String, SkillDefinition> result = new LinkedHashMap<>();

		if (isConfigServiceActive()) {
			result.putAll(getConfigService().getAllConfigSkills());
		}

		for (Skill kbSkill : getAllKBSkills()) {
			String name = kbSkill.getName();
			AgentSource source = result.containsKey(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			result.put(name, toSkillDefinition(kbSkill, source));
		}

		return result.values();
	}

	/**
	 * Determines the {@link AgentSource} for the agent with the given name.
	 *
	 * @return the source, or {@code null} if no agent with that name exists
	 */
	public AgentSource getAgentSource(String name) {
		boolean inKB = findKBAgentByName(name) != null;
		boolean inConfig = hasConfigAgent(name);

		if (inKB && inConfig) {
			return AgentSource.KB_OVERRIDE;
		}
		if (inKB) {
			return AgentSource.KB;
		}
		if (inConfig) {
			return AgentSource.CONFIG;
		}
		return null;
	}

	/**
	 * All raw KB agent wrapper objects.
	 *
	 * <p>
	 * Package-private — needed by migration tooling.
	 * </p>
	 */
	Collection<Agent> getAllKBAgents() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Collection<KnowledgeObject> kos = kb.getAllKnowledgeObjects(AGENT_TABLE);
		return WrapperFactory.getWrappersForKOs(Agent.class, kos);
	}

	/**
	 * All raw KB skill wrapper objects.
	 *
	 * <p>
	 * Package-private — needed by migration tooling.
	 * </p>
	 */
	Collection<Skill> getAllKBSkills() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Collection<KnowledgeObject> kos = kb.getAllKnowledgeObjects(SKILL_TABLE);
		return WrapperFactory.getWrappersForKOs(Skill.class, kos);
	}

	private Agent findKBAgentByName(String name) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		List<Agent> agents =
			WrapperFactory.getWrappersByAttribute(AGENT_TABLE, kb, AgentBase.NAME_ATTR, name);
		return agents.isEmpty() ? null : agents.get(0);
	}

	private Skill findKBSkillByName(String name) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		List<Skill> skills =
			WrapperFactory.getWrappersByAttribute(SKILL_TABLE, kb, "name", name);
		return skills.isEmpty() ? null : skills.get(0);
	}

	private AgentDefinition toAgentDefinition(Agent agent, AgentSource source) {
		List<String> skillNames = agent.getSkills() != null
			? new ArrayList<>(agent.getSkills()) : List.of();

		List<String> delegationNames = agent.getDelegations() != null
			? new ArrayList<>(agent.getDelegations()) : List.of();

		return new AgentDefinition(
			agent.getName(),
			agent.getDescription(),
			agent.getSystemPrompt(),
			new ArrayList<>(agent.getTools()),
			new ArrayList<>(agent.getMcpServers()),
			skillNames,
			delegationNames,
			source);
	}

	private SkillDefinition toSkillDefinition(Skill skill, AgentSource source) {
		return new SkillDefinition(
			skill.getName(),
			skill.getDescription(),
			skill.getContent(),
			source);
	}

	private boolean hasConfigAgent(String name) {
		return isConfigServiceActive() && getConfigService().hasConfigAgent(name);
	}

	private boolean hasConfigSkill(String name) {
		return isConfigServiceActive() && getConfigService().hasConfigSkill(name);
	}

	private boolean isConfigServiceActive() {
		return AgentDefinitionService.Module.INSTANCE.isActive();
	}

	private AgentDefinitionService getConfigService() {
		return AgentDefinitionService.getInstance();
	}
}
