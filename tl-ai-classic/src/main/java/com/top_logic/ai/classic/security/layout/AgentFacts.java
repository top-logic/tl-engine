/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.classic.security.layout;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.ai.core.agent.AgentDefinition;
import com.top_logic.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.security.ModelSecurity;

/**
 * What the application says about an agent that decides whether a role can run it: the model it is
 * assigned, the MCP servers it declares, and whether it is infrastructure.
 *
 * <p>
 * An agent a role may run can still not run for that role: when a role forbids the agent's own model
 * or one of its own servers, or when the application runs the agent for users rather than letting
 * them converse with it. The views that answer "what may this role use" have to ask these three
 * questions, and ask them the same way.
 * </p>
 */
public interface AgentFacts {

	/** The model the named agent is assigned, or <code>null</code> if nothing can say. */
	String assignedModel(String agentName);

	/** The MCP servers the named agent declares, empty if there is no such agent. */
	Collection<String> declaredServers(String agentName);

	/** Whether the named agent is one the application runs for a user rather than a persona. */
	boolean isInfrastructure(String agentName);

	/**
	 * The facts of the running application, as its configuration states them.
	 *
	 * <p>
	 * For a view about roles, which have no model rights: every stored agent counts.
	 * </p>
	 */
	static AgentFacts live() {
		return Live.CONFIGURATION;
	}

	/**
	 * The facts of the running application as the given account knows them.
	 *
	 * <p>
	 * For a view about one account: where a stored agent the account may not read has the name of a
	 * configured agent, the account runs the configured one, so its facts count.
	 * </p>
	 *
	 * @param reader
	 *        The model rights of the account.
	 */
	static AgentFacts liveFor(ModelSecurity reader) {
		return new Live(reader);
	}

	/**
	 * The facts of the running application, read from the agent definitions and the model assignment
	 * of the agent runtime.
	 */
	final class Live implements AgentFacts {

		static final Live CONFIGURATION = new Live(ModelSecurity.UNCHECKED);

		private final ModelSecurity _reader;

		private Live(ModelSecurity reader) {
			_reader = reader;
		}

		@Override
		public String assignedModel(String agentName) {
			AgentRuntimeService runtime = AgentRuntimeService.getInstance();
			if (runtime == null) {
				return null;
			}
			try {
				return runtime.resolveModelAssignment(agentName).model();
			} catch (RuntimeException ex) {
				Logger.debug("No model assignment for '" + agentName + "'.", AgentFacts.class);
				return null;
			}
		}

		@Override
		public Collection<String> declaredServers(String agentName) {
			AgentDefinition agent = AgentDefinitionQuery.getInstance().findAgentByName(agentName, _reader);
			return agent == null ? List.of() : agent.mcpServers();
		}

		@Override
		public boolean isInfrastructure(String agentName) {
			AgentDefinition agent = AgentDefinitionQuery.getInstance().findAgentByName(agentName, _reader);
			return agent != null && agent.infrastructure();
		}
	}

}
