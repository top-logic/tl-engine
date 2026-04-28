/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Collection;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.Skill;

/**
 * Queries for retrieving agent and skill definitions from the knowledge base.
 */
public class AgentDefinitionQuery {

	/** Meta table name for agent objects. */
	public static final String AGENT_TABLE = "AIAgent";

	/** Meta table name for skill objects. */
	public static final String SKILL_TABLE = "AISkill";

	private static final AgentDefinitionQuery INSTANCE = new AgentDefinitionQuery();

	/** */
	public static AgentDefinitionQuery getInstance() {
		return INSTANCE;
	}

	private AgentDefinitionQuery() {
		// Singleton
	}

	/**
	 * All {@code tl.ai.core:Agent} instances in the knowledge base.
	 */
	public Collection<Agent> getAllAgents() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Collection<KnowledgeObject> kos = kb.getAllKnowledgeObjects(AGENT_TABLE);
		return WrapperFactory.getWrappersForKOs(Agent.class, kos);
	}

	/**
	 * All {@code tl.ai.core:Skill} instances in the knowledge base.
	 */
	public Collection<Skill> getAllSkills() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Collection<KnowledgeObject> kos = kb.getAllKnowledgeObjects(SKILL_TABLE);
		return WrapperFactory.getWrappersForKOs(Skill.class, kos);
	}

	/**
	 * Finds an agent by name.
	 *
	 * @return the agent, or {@code null} if not found
	 */
	public Agent findAgentByName(String name) {
		for (Agent agent : getAllAgents()) {
			if (name.equals(agent.getName())) {
				return agent;
			}
		}
		Logger.warn("Agent not found: " + name, AgentDefinitionQuery.class);
		return null;
	}
}
