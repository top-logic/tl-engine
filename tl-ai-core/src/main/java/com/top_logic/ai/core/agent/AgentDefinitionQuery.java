/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

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
import com.top_logic.ai.core.model.Agent;
import com.top_logic.ai.core.model.Skill;
import com.top_logic.ai.core.model.impl.AgentBase;
import com.top_logic.ai.core.security.ModelSecurity;

/**
 * Unified resolution layer for agent and skill definitions.
 *
 * <p>
 * Queries both service configuration (via {@link TopLogicAgentDefinitions}) and the knowledge base,
 * merging results with the "KB wins by name" rule. All public methods return source-agnostic
 * {@link AgentDefinition} and {@link SkillDefinition} records.
 * </p>
 *
 * <p>
 * There are two kinds of lookup. The methods with a {@link ModelSecurity reader} answer for one
 * account: a stored agent or skill that the reader may not read is unknown to it, and a configured
 * definition of the same name answers in its place. Every run and every list that a person sees
 * uses them. The methods without a reader return every definition without a check. The engine uses
 * them only to check its configuration, for example the agent names of the tool access policy.
 * </p>
 *
 * <p>
 * A stored record is found by its name for every reader. Names are unique for the whole
 * application ({@link UniqueNameConstraint}), so the name of a record that a reader may not read is
 * not free for that reader either.
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
	 * Finds an agent definition by name using layered lookup (KB first, then config), without a
	 * check of the model rights.
	 *
	 * <p>
	 * Only for a check of the configuration. A run or a list for a person uses
	 * {@link #findAgentByName(String, ModelSecurity)}.
	 * </p>
	 *
	 * @return the merged {@link AgentDefinition}, or {@code null} if not found in either source
	 */
	public AgentDefinition findAgentByName(String name) {
		return findAgentByName(name, ModelSecurity.UNCHECKED);
	}

	/**
	 * Finds an agent definition by name for the given reader, using layered lookup (KB first, then
	 * config).
	 *
	 * @param name
	 *        The agent name.
	 * @param reader
	 *        The model rights of the account the answer is for, usually
	 *        {@link SecurityContext#modelSecurity()} of a run. A stored agent that the reader may not
	 *        read is unknown: the configured agent of the same name answers in its place, if there is
	 *        one.
	 * @return The merged {@link AgentDefinition}, or {@code null} if the reader knows no agent of
	 *         this name.
	 */
	public AgentDefinition findAgentByName(String name, ModelSecurity reader) {
		Agent kbAgent = findKBAgentByName(name);
		if (kbAgent != null && reader.canRead(kbAgent)) {
			AgentSource source = hasConfigAgent(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			return toAgentDefinition(kbAgent, source);
		}
		if (isConfigServiceActive()) {
			return getConfigService().getConfigAgent(name);
		}
		return null;
	}

	/**
	 * All agent definitions merged from config and KB, with KB overriding config by name, without a
	 * check of the model rights.
	 *
	 * <p>
	 * Only for a check of the configuration. A run or a list for a person uses
	 * {@link #getAllAgents(ModelSecurity)}.
	 * </p>
	 */
	public Collection<AgentDefinition> getAllAgents() {
		return getAllAgents(ModelSecurity.UNCHECKED);
	}

	/**
	 * All agent definitions the given reader knows, merged from config and KB, with KB overriding
	 * config by name.
	 *
	 * @param reader
	 *        The model rights of the account the answer is for. A stored agent that the reader may
	 *        not read is left out.
	 * @see #findAgentByName(String, ModelSecurity)
	 */
	public Collection<AgentDefinition> getAllAgents(ModelSecurity reader) {
		Map<String, AgentDefinition> result = new LinkedHashMap<>();

		if (isConfigServiceActive()) {
			result.putAll(getConfigService().getAllConfigAgents());
		}

		for (Agent kbAgent : reader.filterReadable(getAllKBAgents())) {
			String name = kbAgent.getName();
			AgentSource source = result.containsKey(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			result.put(name, toAgentDefinition(kbAgent, source));
		}

		return result.values();
	}

	/**
	 * Finds a skill definition by name using layered lookup (KB first, then config), without a
	 * check of the model rights.
	 *
	 * <p>
	 * Only for a check of the configuration. A run or a list for a person uses
	 * {@link #findSkillByName(String, ModelSecurity)}.
	 * </p>
	 *
	 * @return the merged {@link SkillDefinition}, or {@code null} if not found in either source
	 */
	public SkillDefinition findSkillByName(String name) {
		return findSkillByName(name, ModelSecurity.UNCHECKED);
	}

	/**
	 * Finds a skill definition by name for the given reader, using layered lookup (KB first, then
	 * config).
	 *
	 * @param name
	 *        The skill name.
	 * @param reader
	 *        The model rights of the account the answer is for. A stored skill that the reader may
	 *        not read is unknown: the configured skill of the same name answers in its place, if there
	 *        is one.
	 * @return The merged {@link SkillDefinition}, or {@code null} if the reader knows no skill of
	 *         this name.
	 */
	public SkillDefinition findSkillByName(String name, ModelSecurity reader) {
		Skill kbSkill = findKBSkillByName(name);
		if (kbSkill != null && reader.canRead(kbSkill)) {
			AgentSource source = hasConfigSkill(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			return toSkillDefinition(kbSkill, source);
		}
		if (isConfigServiceActive()) {
			return getConfigService().getConfigSkill(name);
		}
		return null;
	}

	/**
	 * All skill definitions merged from config and KB, with KB overriding config by name, without a
	 * check of the model rights.
	 *
	 * <p>
	 * Only for a check of the configuration. A run or a list for a person uses
	 * {@link #getAllSkills(ModelSecurity)}.
	 * </p>
	 */
	public Collection<SkillDefinition> getAllSkills() {
		return getAllSkills(ModelSecurity.UNCHECKED);
	}

	/**
	 * All skill definitions the given reader knows, merged from config and KB, with KB overriding
	 * config by name.
	 *
	 * @param reader
	 *        The model rights of the account the answer is for. A stored skill that the reader may
	 *        not read is left out.
	 * @see #findSkillByName(String, ModelSecurity)
	 */
	public Collection<SkillDefinition> getAllSkills(ModelSecurity reader) {
		Map<String, SkillDefinition> result = new LinkedHashMap<>();

		if (isConfigServiceActive()) {
			result.putAll(getConfigService().getAllConfigSkills());
		}

		for (Skill kbSkill : reader.filterReadable(getAllKBSkills())) {
			String name = kbSkill.getName();
			AgentSource source = result.containsKey(name) ? AgentSource.KB_OVERRIDE : AgentSource.KB;
			result.put(name, toSkillDefinition(kbSkill, source));
		}

		return result.values();
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
			source,
			agent.getInfrastructure());
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
			com.top_logic.ai.core.model.Skill skill, AgentSource source) {
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
