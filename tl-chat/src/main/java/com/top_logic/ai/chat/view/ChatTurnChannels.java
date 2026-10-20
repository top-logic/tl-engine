/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentFactory.RunOverrides;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RunAuthorization;

/**
 * What the four turn-setting channels of {@code <chat-conversation/>} carry, and whether the acting
 * account may be given it.
 *
 * <p>
 * The sibling of {@link ChatSessionIds}, for the values that are <em>not</em> model objects. An agent
 * is one only when an installation authored it in the knowledge base; the agents this product ships,
 * every model and every MCP server are configuration, and the one thing all of them have is a name -
 * which is what {@code /chat/send}, {@link AgentFactory} and the security policy are written in. So
 * these channels carry names, and this class is what turns whatever a host wrote onto a channel into
 * one. A {@code tl.ai.core:Agent} record is read for its {@code name} as a convenience, so a list of
 * authored agents feeds the {@code agent} channel directly.
 * </p>
 *
 * <p>
 * The refusals mirror {@code ChatServlet}: the same {@link AgentFactory#runRefusal} the servlet
 * pre-checks a send with decides here too, so a channel cannot put the composer into a state that
 * the next send would be answered 403 for. A pre-check is a courtesy - when it cannot be made, the
 * value is let through and the run refuses where it starts.
 * </p>
 *
 * @see ChatConversationControl
 */
public final class ChatTurnChannels {

	private ChatTurnChannels() {
		// Static utility.
	}

	/**
	 * The single name on a channel: a string, the {@code name} of a {@link TLObject} record, or the
	 * only element of a one-item collection; {@code null} when the channel says nothing.
	 *
	 * <p>
	 * {@code null} and a blank string are the same answer - "nothing said" - because there is no
	 * agent, model or effort level whose name is empty.
	 * </p>
	 */
	public static String name(Object channelValue) {
		Object value = single(channelValue);
		if (value == null) {
			return null;
		}
		if (value instanceof TLObject object) {
			return blankToNull(nameOf(object));
		}
		return blankToNull(String.valueOf(value));
	}

	/**
	 * The names on a channel that holds several: a collection, or one name on its own.
	 *
	 * @return {@code null} when the channel says nothing; an empty list is an answer of its own - no
	 *         server at all.
	 */
	public static List<String> names(Object channelValue) {
		if (channelValue == null) {
			return null;
		}
		if (channelValue instanceof Collection<?> many) {
			List<String> names = new ArrayList<>(many.size());
			for (Object element : many) {
				String name = name(element);
				if (name != null) {
					names.add(name);
				}
			}
			return names;
		}
		String single = name(channelValue);
		return single == null ? null : List.of(single);
	}

	/**
	 * Whether {@code owner} may hold this conversation with the named agent.
	 *
	 * <p>
	 * Two questions, the way {@code ChatServlet} asks them: whether this application has such an
	 * agent at all - which is not a policy question, and which the servlet answers 404 - and whether
	 * a role lets this account run it. The second is more than the allowlist: an agent whose own model
	 * or whose own server a role denies cannot run for this account either, which is the rule the
	 * agent picker is filtered by.
	 * </p>
	 */
	public static boolean allowsAgent(Person owner, String agentName) {
		return exists(agentName) && allows(owner, agentName, RunOverrides.NONE);
	}

	/**
	 * Whether {@code owner} may run {@code agentName} on the named model.
	 *
	 * @param agentName
	 *        The agent the composer is set to; {@code null} leaves the agent undecided and asks about
	 *        the model alone.
	 */
	public static boolean allowsModel(Person owner, String agentName, String model) {
		return allows(owner, agentName, new RunOverrides(model, null, null));
	}

	/**
	 * Whether {@code owner} may attach the named MCP servers to a run of {@code agentName}.
	 *
	 * @param servers
	 *        The servers asked for; an empty list attaches none and is always permitted.
	 */
	public static boolean allowsServers(Person owner, String agentName, List<String> servers) {
		if (servers == null || servers.isEmpty()) {
			return true;
		}
		return allows(owner, agentName, new RunOverrides(null, servers, null));
	}

	/**
	 * Whether a run of {@code agentName} with these overrides would start for {@code owner}.
	 *
	 * <p>
	 * Permits while nothing can be asked - no account, the security service down, or the question
	 * itself failing - exactly as {@code ChatServlet.policyRefusal} does: a pre-check that cannot be
	 * made must not stop a request the run would accept.
	 * </p>
	 */
	private static boolean allows(Person owner, String agentName, RunOverrides overrides) {
		if (owner == null || !AiToolAccessService.Module.INSTANCE.isActive()) {
			return true;
		}
		try {
			RunAuthorization access = AiToolAccessService.effectiveRunAccessFor(owner);
			String agent = agentName == null || agentName.isBlank()
				? AgentFactory.defaultAgentFor(access)
				: agentName.trim();
			if (agent == null) {
				// An account that may run no agent at all. Nothing it could be given is legal.
				return false;
			}
			Decision.Denied denied = AgentFactory.runRefusal(access, agent, overrides);
			if (denied != null) {
				Logger.debug("Channel value refused for '" + agent + "': " + denied.name(),
					ChatTurnChannels.class);
				return false;
			}
			return true;
		} catch (RuntimeException ex) {
			Logger.debug("Could not check what this account may be given; letting it through.", ex,
				ChatTurnChannels.class);
			return true;
		}
	}

	/**
	 * Whether this application has an agent of that name, assuming it has while it cannot say.
	 *
	 * <p>
	 * Mirrors {@code ChatServlet}: a name no agent has is not forbidden, it is wrong, and the run
	 * would die in the background thread rather than refuse. The composer must not be put there.
	 * </p>
	 */
	private static boolean exists(String agentName) {
		if (agentName == null || agentName.isBlank()) {
			return false;
		}
		try {
			boolean known = AgentDefinitionQuery.getInstance().findAgentByName(agentName.trim()) != null;
			if (!known) {
				Logger.debug("No agent '" + agentName + "' to follow a channel to.", ChatTurnChannels.class);
			}
			return known;
		} catch (RuntimeException ex) {
			// Cannot enumerate, so cannot judge: leave it to the run, as the servlet does.
			Logger.debug("Could not check whether an agent '" + agentName + "' exists.", ex,
				ChatTurnChannels.class);
			return true;
		}
	}

	/** The {@code name} attribute of a record, or {@code null} when it has none. */
	private static String nameOf(TLObject object) {
		TLStructuredType type = object.tType();
		if (type == null) {
			return null;
		}
		TLStructuredTypePart part = type.getPart("name");
		if (part == null) {
			return null;
		}
		Object value = object.tValue(part);
		return value == null ? null : String.valueOf(value);
	}

	/** A one-item collection is the single value a multi-select list writes for one row. */
	private static Object single(Object channelValue) {
		if (channelValue instanceof Collection<?> many) {
			return many.size() == 1 ? many.iterator().next() : null;
		}
		return channelValue;
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
