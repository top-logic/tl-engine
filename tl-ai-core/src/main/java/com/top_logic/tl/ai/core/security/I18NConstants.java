/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Cause;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;

/**
 * I18N constants for the agent security package.
 *
 * <p>
 * The refusals a user gets to see when they asked for an agent, a model or an MCP server their roles
 * do not permit. They are written for the person in front of the chat, not for an operator: they name
 * what is unavailable and stop there. The operator's version, which also names the rule, is the
 * English sentence of {@link RoleAccessPolicy.Decision.Denied#message()} in the log.
 * </p>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Refusal shown when a role of the user explicitly forbids the requested agent.
	 *
	 * @en The agent {0} is not available to you: it is excluded for your role {1}.
	 * @de Der Agent {0} steht Ihnen nicht zur Verfügung: Für Ihre Rolle {1} ist er ausgeschlossen.
	 */
	public static ResKey2 AGENT_DENIED_BY_ROLE__AGENT_ROLE;

	/**
	 * Refusal shown when nothing permits the requested agent.
	 *
	 * @en The agent {0} is not available to you. Your roles do not include it.
	 * @de Der Agent {0} steht Ihnen nicht zur Verfügung. Ihre Rollen umfassen ihn nicht.
	 */
	public static ResKey1 AGENT_NOT_GRANTED__AGENT;

	/**
	 * Refusal shown when a role of the user explicitly forbids the requested model.
	 *
	 * @en The model {0} is not available to you: it is excluded for your role {1}.
	 * @de Das Modell {0} steht Ihnen nicht zur Verfügung: Für Ihre Rolle {1} ist es ausgeschlossen.
	 */
	public static ResKey2 MODEL_DENIED_BY_ROLE__MODEL_ROLE;

	/**
	 * Refusal shown when nothing permits the requested model.
	 *
	 * @en The model {0} is not available to you. Your roles do not include it.
	 * @de Das Modell {0} steht Ihnen nicht zur Verfügung. Ihre Rollen umfassen es nicht.
	 */
	public static ResKey1 MODEL_NOT_GRANTED__MODEL;

	/**
	 * Refusal shown when the model an agent is assigned is forbidden to a role of the acting account.
	 *
	 * @en The agent runs on the model {0}, which is excluded for your role. Nothing you can choose
	 *     changes that - please report it to an administrator.
	 * @de Der Agent läuft auf dem Modell {0}, das für Ihre Rolle ausgeschlossen ist. Daran ändert
	 *     keine Auswahl etwas - bitte melden Sie es einem Administrator.
	 */
	public static ResKey1 MODEL_OF_THE_AGENT_DENIED__MODEL;

	/**
	 * Refusal shown when a server an agent declares is forbidden to a role of the acting account.
	 *
	 * @en The agent uses the MCP server {0}, which is excluded for your role. Nothing you can choose
	 *     changes that - please report it to an administrator.
	 * @de Der Agent verwendet den MCP-Server {0}, der für Ihre Rolle ausgeschlossen ist. Daran ändert
	 *     keine Auswahl etwas - bitte melden Sie es einem Administrator.
	 */
	public static ResKey1 SERVER_OF_THE_AGENT_DENIED__SERVER;

	/**
	 * Refusal shown when a request names an agent that is not a conversation partner at all.
	 *
	 * <p>
	 * Says what it is rather than what the roles do not grant: no administrator can grant this one,
	 * so pointing the user at one would send them away for nothing.
	 * </p>
	 *
	 * @en The agent {0} cannot be talked to: the application runs it for you.
	 * @de Mit dem Agenten {0} kann man nicht sprechen: Die Anwendung führt ihn für Sie aus.
	 */
	public static ResKey1 AGENT_NOT_A_PERSONA__AGENT;

	/**
	 * Refusal shown when a request names an agent this application does not have.
	 *
	 * @en There is no agent named {0}.
	 * @de Es gibt keinen Agenten namens {0}.
	 */
	public static ResKey1 UNKNOWN_AGENT__AGENT;

	/**
	 * Refusal shown when a request chooses a model although no role lets it choose one at all.
	 *
	 * @en The model {0} is not available to you. This request runs on the model its agent is assigned.
	 * @de Das Modell {0} steht Ihnen nicht zur Verfügung. Diese Anfrage läuft mit dem Modell, das
	 *     ihrem Agenten zugewiesen ist.
	 */
	public static ResKey1 MODEL_NOT_THE_AGENTS__MODEL;

	/**
	 * Refusal shown when a role of the user explicitly forbids the requested MCP server.
	 *
	 * @en The MCP server {0} is not available to you: it is excluded for your role {1}.
	 * @de Der MCP-Server {0} steht Ihnen nicht zur Verfügung: Für Ihre Rolle {1} ist er
	 *     ausgeschlossen.
	 */
	public static ResKey2 MCP_DENIED_BY_ROLE__SERVER_ROLE;

	/**
	 * Refusal shown when nothing permits the requested MCP server.
	 *
	 * @en The MCP server {0} is not available to you. Your roles do not include it.
	 * @de Der MCP-Server {0} steht Ihnen nicht zur Verfügung. Ihre Rollen umfassen ihn nicht.
	 */
	public static ResKey1 MCP_NOT_GRANTED__SERVER;

	/**
	 * Refusal shown when a request attaches a server although no role lets it attach one at all.
	 *
	 * @en The MCP server {0} is not available to you. This request reaches the servers its agent
	 *     declares.
	 * @de Der MCP-Server {0} steht Ihnen nicht zur Verfügung. Diese Anfrage erreicht die Server, die
	 *     ihr Agent angibt.
	 */
	public static ResKey1 MCP_NOT_THE_AGENTS__SERVER;

	/**
	 * Refusal shown when a request names no agent and the account may run none at all.
	 *
	 * <p>
	 * Its own sentence rather than the name of the agent the request would have fallen back to: that
	 * name is one nobody chose and nobody can act on.
	 * </p>
	 *
	 * @en No agent is available to you. Ask an administrator to permit one for your role.
	 * @de Ihnen steht kein Agent zur Verfügung. Bitten Sie einen Administrator, Ihrer Rolle einen zu
	 *     erlauben.
	 */
	public static ResKey NO_AGENT_AVAILABLE;

	/**
	 * The refusal of one decision, in the acting user's language.
	 *
	 * @param dimension
	 *        What the refused name is.
	 * @param cause
	 *        Why it was refused.
	 * @param name
	 *        The refused name.
	 * @param roleName
	 *        The role that forbids it, or <code>null</code> unless the cause is a role's refusal.
	 * @return The message to show, never <code>null</code>.
	 */
	public static ResKey denial(Dimension dimension, Cause cause, String name, String roleName) {
		switch (dimension) {
			case MODEL:
				if (cause == Cause.AGENTS_OWN_DENIED) {
					return MODEL_OF_THE_AGENT_DENIED__MODEL.fill(name);
				}
				if (cause == Cause.ROLE_DENY) {
					return MODEL_DENIED_BY_ROLE__MODEL_ROLE.fill(name, roleName);
				}
				return cause == Cause.NOT_THE_AGENTS
					? MODEL_NOT_THE_AGENTS__MODEL.fill(name)
					: MODEL_NOT_GRANTED__MODEL.fill(name);
			case MCP_SERVER:
				if (cause == Cause.AGENTS_OWN_DENIED) {
					return SERVER_OF_THE_AGENT_DENIED__SERVER.fill(name);
				}
				if (cause == Cause.ROLE_DENY) {
					return MCP_DENIED_BY_ROLE__SERVER_ROLE.fill(name, roleName);
				}
				return cause == Cause.NOT_THE_AGENTS
					? MCP_NOT_THE_AGENTS__SERVER.fill(name)
					: MCP_NOT_GRANTED__SERVER.fill(name);
			default:
				if (cause == Cause.INFRASTRUCTURE_AGENT) {
					return AGENT_NOT_A_PERSONA__AGENT.fill(name);
				}
				// An agent has no counterpart of "what the agent itself uses", so the section default
				// can only ever refuse it outright - NOT_THE_AGENTS cannot arise here.
				return cause == Cause.ROLE_DENY
					? AGENT_DENIED_BY_ROLE__AGENT_ROLE.fill(name, roleName)
					: AGENT_NOT_GRANTED__AGENT.fill(name);
		}
	}

	static {
		initConstants(I18NConstants.class);
	}
}
