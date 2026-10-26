/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.util.Resources;

/**
 * Why a message cannot be sent, and the HTTP status that says which kind of "cannot" it is.
 *
 * <p>
 * Asked before a run is started - by {@link ChatServlet} for a message of the browser and by
 * {@link com.top_logic.ai.chat.layout.ChatStart} for a conversation started on the server - so that
 * a refusal reaches the user as a sentence rather than as a turn that failed.
 * </p>
 *
 * @param status
 *        The HTTP status to answer with.
 * @param reason
 *        The sentence the client shows, in the user's language.
 */
public record SendRefusal(int status, String reason) {

	/** A refusal by the agent security policy. */
	public static SendRefusal forbidden(String reason) {
		return new SendRefusal(HttpServletResponse.SC_FORBIDDEN, reason);
	}

	/**
	 * The refusal of a function the conversation's backend does not support, or <code>null</code>
	 * when it does.
	 */
	public static SendRefusal unsupported(boolean supported, ResKey reason) {
		return supported ? null : forbidden(Resources.getInstance().getString(reason));
	}

	/**
	 * Why the acting account may not make this request, in their language.
	 *
	 * <p>
	 * The friendly half of the agent security policy: {@code AgentFactory} refuses the run whatever
	 * this says, and this exists so that the refusal reaches the user as a sentence rather than as a
	 * turn that failed. Answers <code>null</code> while nothing can be asked - no account behind the
	 * request, or the policy service not running - and the run is then refused where it starts.
	 * </p>
	 */
	public static SendRefusal forRun(Person person, ChatProcessor.ChatRunOptions options) {
		String named = blankToNull(options.agent());
		if (named != null) {
			SendRefusal notAPersona = personaRefusal(named.trim());
			if (notAPersona != null) {
				return notAPersona;
			}
		}
		SendRefusal denial = policyRefusal(person, options);
		if (denial != null) {
			return denial;
		}
		if (named != null && !agentExists(named.trim())) {
			// A name no agent has. Answered here rather than accepted and left to fail in the
			// background thread, where it reached the user as a turn that stopped and the
			// administrator as an ERROR with a stack trace. Not a refusal: the request is wrong,
			// not forbidden, so it is 404 and not 403.
			return new SendRefusal(HttpServletResponse.SC_NOT_FOUND, Resources.getInstance()
				.getString(com.top_logic.tl.ai.core.security.I18NConstants.UNKNOWN_AGENT__AGENT
					.fill(named.trim())));
		}
		return null;
	}

	/**
	 * Why the named agent is nobody's conversation partner, or <code>null</code> if it is one.
	 *
	 * <p>
	 * An {@link AgentDefinition#infrastructure()} agent is one the application runs <em>for</em> the
	 * user - the prompt improver rewrites their draft. Naming it in a send is refused whatever
	 * {@code <agent-access>} grants, so this is asked before {@link #policyRefusal} and without the
	 * policy service: under {@code default="all"} that service permits it, and with the service off
	 * there is no policy to ask at all (#397). The run itself is refused again where it starts -
	 * {@code AgentFactory} asks the same question of the run's security context.
	 * </p>
	 */
	private static SendRefusal personaRefusal(String agentName) {
		AgentDefinition agentDef;
		try {
			agentDef = AgentDefinitionQuery.getInstance().findAgentByName(agentName);
		} catch (RuntimeException ex) {
			// Cannot read it, so cannot judge it: leave the request to the run, as before.
			Logger.warn("Could not check whether '" + agentName + "' is a conversation partner.", ex,
				SendRefusal.class);
			return null;
		}
		if (agentDef == null || !agentDef.infrastructure()) {
			return null;
		}
		return forbidden(Resources.getInstance().getString(
			com.top_logic.tl.ai.core.security.I18NConstants.AGENT_NOT_A_PERSONA__AGENT.fill(agentName)));
	}

	/** Whether this application has an agent of that name, assuming it has while it cannot say. */
	private static boolean agentExists(String agentName) {
		try {
			return AgentDefinitionQuery.getInstance().findAgentByName(agentName) != null;
		} catch (RuntimeException ex) {
			// Cannot enumerate, so cannot judge: leave the request to the run, as before.
			Logger.warn("Could not check whether an agent '" + agentName + "' exists.", ex,
				SendRefusal.class);
			return true;
		}
	}

	/** Why the agent security policy refuses this request, or <code>null</code> if it does not. */
	private static SendRefusal policyRefusal(Person person, ChatProcessor.ChatRunOptions options) {
		if (person == null || !AiToolAccessService.Module.INSTANCE.isActive()) {
			return null;
		}
		RunAuthorization access;
		String agentName;
		try {
			access = AiToolAccessService.effectiveRunAccessFor(person);
			agentName = options.agent() != null && !options.agent().isBlank()
				? options.agent().trim()
				: AgentFactory.defaultAgentFor(access);
		} catch (RuntimeException ex) {
			// A pre-check is a courtesy; a failure in it must not stop a request the run would accept.
			Logger.warn("Could not pre-check what this account may run.", ex, SendRefusal.class);
			return null;
		}
		if (agentName == null) {
			// A request that names no agent, from an account that may run none. Its own sentence: the
			// name of the agent they may not have says nothing they can act on.
			return forbidden(Resources.getInstance().getString(
				com.top_logic.tl.ai.core.security.I18NConstants.NO_AGENT_AVAILABLE));
		}
		List<String> mcpOverride = options.mcpOverride() ? options.mcpServers() : null;
		AgentFactory.RunOverrides overrides =
			new AgentFactory.RunOverrides(options.model(), mcpOverride, options.effort());
		Decision.Denied denied;
		try {
			denied = AgentFactory.runRefusal(access, agentName, overrides);
		} catch (RuntimeException ex) {
			Logger.warn("Could not pre-check what '" + agentName + "' may run.", ex, SendRefusal.class);
			return null;
		}
		return denied == null ? null : forbidden(Resources.getInstance().getString(denied.reason()));
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
