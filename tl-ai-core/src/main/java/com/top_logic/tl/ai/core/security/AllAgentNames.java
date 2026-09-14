/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.Logger;
import com.top_logic.basic.func.Function0;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentRoleAccess;

/**
 * The agents an administrator can pick from when writing a {@link ToolAccessPolicy}.
 *
 * <p>
 * Every agent the application knows - the ones its configuration declares and the ones authored in the
 * running application - plus every agent name the policy in effect already stores.
 * </p>
 *
 * <p>
 * That second half is not a nicety, and {@link AllToolNames} carries it for the same reason: an agent
 * an administrator wrote the policy for before creating it is absent from the first half, and offering
 * only what exists would let an open-and-save of the editor drop such an entry, quietly widening a
 * policy that was correct.
 * </p>
 *
 * @see AllModelNames
 * @see AllMcpServerNames
 */
public class AllAgentNames extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return union(existingAgentNames(), AiToolAccessService.policy());
	}

	/**
	 * The names of the agents the application knows, empty while nothing can answer.
	 *
	 * <p>
	 * Guarded, because this is asked while an editor is open and the knowledge base query behind it can
	 * fail on an application whose model is not (or no longer) loaded. An option list that throws hides
	 * the whole form; one that is short is merely a short list.
	 * </p>
	 */
	static Collection<String> existingAgentNames() {
		List<String> names = new ArrayList<>();
		try {
			for (AgentDefinition agent : AgentDefinitionQuery.getInstance().getAllAgents()) {
				names.add(agent.name());
			}
		} catch (RuntimeException ex) {
			Logger.warn("Listing the agents of this application failed; offering the configured ones"
				+ " only.", ex, AllAgentNames.class);
			if (TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
				names.addAll(TopLogicAgentDefinitions.getInstance().getAllConfigAgents().keySet());
			}
		}
		return names;
	}

	/**
	 * The agents to offer: the ones that exist, and the ones the policy already names.
	 *
	 * @param existingAgents
	 *        The names of the agents the application knows.
	 * @param policy
	 *        The policy in effect, may be <code>null</code> if the service is not running.
	 * @return The option list, alphabetically and without duplicates.
	 */
	static List<String> union(Collection<String> existingAgents, ToolAccessPolicy policy) {
		Set<String> names = new TreeSet<>();
		for (String name : existingAgents) {
			if (name != null && !name.isBlank()) {
				names.add(name);
			}
		}
		if (policy != null) {
			for (AgentRoleAccess role : policy.getAgentAccess().getRoles().values()) {
				addNames(names, role.getGrants());
				addNames(names, role.getDenies());
			}
		}
		return new ArrayList<>(names);
	}

	private static void addNames(Set<String> names, Collection<AgentGrant> entries) {
		for (AgentGrant entry : entries) {
			names.addAll(entry.getAgents());
		}
	}

}
