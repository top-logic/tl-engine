/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.basic.util.ResKey;
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
 * Queries both service configuration (via {@link TopLogicAgentDefinitions}) and the knowledge base,
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
		KnowledgeBase kb = knowledgeBase();
		if (kb == null) {
			return List.of();
		}
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
		KnowledgeBase kb = knowledgeBase();
		if (kb == null) {
			return List.of();
		}
		Collection<KnowledgeObject> kos = kb.getAllKnowledgeObjects(SKILL_TABLE);
		return WrapperFactory.getWrappersForKOs(Skill.class, kos);
	}

	private Agent findKBAgentByName(String name) {
		KnowledgeBase kb = knowledgeBase();
		if (kb == null) {
			return null;
		}
		List<Agent> agents =
			WrapperFactory.getWrappersByAttribute(AGENT_TABLE, kb, AgentBase.NAME_ATTR, name);
		return agents.isEmpty() ? null : agents.get(0);
	}

	private Skill findKBSkillByName(String name) {
		KnowledgeBase kb = knowledgeBase();
		if (kb == null) {
			return null;
		}
		List<Skill> skills =
			WrapperFactory.getWrappersByAttribute(SKILL_TABLE, kb, "name", name);
		return skills.isEmpty() ? null : skills.get(0);
	}

	/**
	 * The application's {@link KnowledgeBase}, or <code>null</code> if the persistency layer is not
	 * running.
	 *
	 * <p>
	 * A deployment always has one; a unit test that only exercises the configured half of the layered
	 * lookup does not. Answering <code>null</code> rather than throwing makes the KB half behave like
	 * the configuration half, which is already guarded by {@code isConfigServiceActive()}: a source
	 * that is not there contributes no definitions.
	 * </p>
	 */
	private static KnowledgeBase knowledgeBase() {
		return PersistencyLayer.Module.INSTANCE.isActive() ? PersistencyLayer.getKnowledgeBase() : null;
	}

	private AgentDefinition toAgentDefinition(Agent agent, AgentSource source) {
		List<String> skillNames = agent.getSkills() != null
			? new ArrayList<>(agent.getSkills()) : List.of();

		List<String> delegationNames = agent.getDelegations() != null
			? new ArrayList<>(agent.getDelegations()) : List.of();

		return new AgentDefinition(
			agent.getName(),
			literal(agent.getDisplayName()),
			agent.getDescription(),
			summaryOf(agent),
			agent.getSystemPrompt(),
			new ArrayList<>(agent.getTools()),
			new ArrayList<>(agent.getMcpServers()),
			skillNames,
			delegationNames,
			source);
	}

	/**
	 * The one line an agent record says about itself to the person choosing it.
	 *
	 * <p>
	 * Whoever wrote the record wrote it in their own language, so it is carried as literal text rather
	 * than as a key into a bundle nobody maintains for it. An empty attribute says nothing, which is a
	 * different thing from saying an empty sentence.
	 * </p>
	 */
	private static ResKey summaryOf(Agent agent) {
		return literal(agent.getSummary());
	}

	/**
	 * The given attribute value as a resource, or <code>null</code> if it says nothing.
	 *
	 * <p>
	 * Literal text rather than a key into a bundle: whoever typed it into the record typed it in their
	 * own language, and there is no bundle anybody maintains for a record.
	 * </p>
	 */
	private static ResKey literal(String value) {
		return value == null || value.isBlank() ? null : ResKey.text(value);
	}

	private SkillDefinition toSkillDefinition(
			com.top_logic.tl.ai.core.model.Skill skill, AgentSource source) {
		return new SkillDefinition(
			skill.getName(),
			skill.getDescription(),
			skill.getContent(),
			List.copyOf(skill.getTools()),
			source);
	}

	private boolean hasConfigAgent(String name) {
		return isConfigServiceActive() && getConfigService().hasConfigAgent(name);
	}

	private boolean hasConfigSkill(String name) {
		return isConfigServiceActive() && getConfigService().hasConfigSkill(name);
	}

	private boolean isConfigServiceActive() {
		return TopLogicAgentDefinitions.Module.INSTANCE.isActive();
	}

	private TopLogicAgentDefinitions getConfigService() {
		return TopLogicAgentDefinitions.getInstance();
	}
}
