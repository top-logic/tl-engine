/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentDefault;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.AgentRoleAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpDefault;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpRoleAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelDefault;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelRoleAccess;

/*
 * Issue #392. One evaluator for three sections rather than three copies of the same six lines, which
 * is what the ticket asks for with "a generic RoleAccessPolicy instead of four parallel check*
 * methods". No type parameter: an agent, a model and an MCP server are each a name, and the only thing
 * that actually differs between the three is the wording of a refusal and what the section default
 * admits - both of which are carried by Dimension and Fallback.
 *
 * Why the tool dimension is NOT folded in here, although the ticket counts four: a tool is reached
 * through a capability group, through a baseline that applies to everyone, and under a background
 * ceiling that binds administrators. None of those three exists for the other dimensions, and a class
 * that had to be parameterised by all of them would be a worse home for both halves than
 * RoleToolAccess is for its own. What the two share is the vocabulary - an allowlist, a deny that
 * wins, deny-by-default - and that is shared in the documentation and in the XML idiom, which is where
 * an administrator meets it.
 */

/**
 * Decides whether the roles a user holds let them run a given agent, choose a given model, or attach
 * a given MCP server.
 *
 * <p>
 * One instance per dimension, built from the matching section of a {@link ToolAccessPolicy}. Like
 * {@link RoleToolAccess} this is pure policy evaluation: it neither reads the {@code KnowledgeBase}
 * nor resolves roles, so the decision is unit-testable and the question "which roles does this person
 * hold" stays in one place.
 * </p>
 *
 * <p>
 * The decision, in order:
 * </p>
 * <ol>
 * <li>An explicit deny from <em>any</em> held role refuses the name outright - including a name the
 * agent itself declares.</li>
 * <li>A grant of any held role permits it.</li>
 * <li>The agent's own choice permits it: the model the agent is assigned, the MCP servers it
 * declares. There is no such set for agents.</li>
 * <li>If any held role is named in this section, nothing else is permitted - the allowlist reading of
 * a role entry, which is what lets one restricted role be added to a running application without
 * touching everybody else.</li>
 * <li>Otherwise the section default decides.</li>
 * </ol>
 *
 * @see ToolAccessPolicy#getAgentAccess()
 * @see RunAuthorization
 */
public final class RoleAccessPolicy {

	/** The role name a decision reports when the section default decided. */
	public static final String DEFAULT_ROLE = "(default)";

	/** The role name a decision reports when the agent's own definition decided. */
	public static final String AGENT_ROLE = "(agent)";

	/**
	 * What a section is about.
	 */
	public enum Dimension {

		/** Which agents a user may run. */
		AGENT("agent"),

		/** Which language models a user may choose for a single request. */
		MODEL("model"),

		/** Which MCP servers a user may attach to a single request. */
		MCP_SERVER("MCP server");

		private final String _label;

		private Dimension(String label) {
			_label = label;
		}

		/** What to call one entry of this dimension in a message written in English. */
		public String label() {
			return _label;
		}
	}

	/**
	 * What a section admits from a user none of whose roles it names.
	 */
	public enum Fallback {

		/** Everything the application has. */
		ALL,

		/**
		 * Only what the agent itself uses - its assigned model, its declared servers.
		 *
		 * <p>
		 * Which is what NONE refuses too: the two differ in what a client is offered, not in what it
		 * may do.
		 * </p>
		 *
		 * @see RoleAccessPolicy#offersAgentChoice()
		 */
		BOUND,

		/** Nothing, and nothing is offered either. */
		NONE;
	}

	/**
	 * Why a name was refused.
	 */
	public enum Cause {

		/** A role of the user forbids it by name. */
		ROLE_DENY,

		/** Nothing permits it: no role of the user, and not the section default. */
		NOT_GRANTED,

		/** Only what the agent itself uses is permitted, and this is not that. */
		NOT_THE_AGENTS,

		/**
		 * The agent itself uses it and a role forbids it.
		 *
		 * <p>
		 * Nobody chose this: two settings of the application contradict each other - an agent is
		 * assigned a model, or declares a server, that a role of the acting account may never use. It
		 * therefore gets a sentence of its own, addressed to a user who can do nothing but report it.
		 * </p>
		 */
		AGENTS_OWN_DENIED,

		/**
		 * The agent is infrastructure: the application runs it, nobody converses with it.
		 *
		 * <p>
		 * Not a role's decision and not overridable by one - see
		 * {@code AgentDefinition#infrastructure()}. It gets a sentence of its own because "your roles
		 * do not include it" would send the user to an administrator who has nothing to grant.
		 * </p>
		 */
		INFRASTRUCTURE_AGENT;
	}

	/**
	 * The outcome of a {@link RoleAccessPolicy#check(Collection, String, Collection) check}.
	 */
	public sealed interface Decision {

		/**
		 * The name is permitted.
		 *
		 * @param byRole
		 *        The role that permitted it, {@link RoleAccessPolicy#DEFAULT_ROLE} for the section
		 *        default, or {@link RoleAccessPolicy#AGENT_ROLE} for the agent's own choice.
		 */
		record Granted(String byRole) implements Decision {
			// Intentionally empty.
		}

		/**
		 * The name is refused.
		 *
		 * @param dimension
		 *        What the refused name is.
		 * @param name
		 *        The refused name.
		 * @param byRole
		 *        The role that forbids it, or <code>null</code> unless {@link #cause()} is
		 *        {@link Cause#ROLE_DENY}.
		 * @param cause
		 *        Why it was refused.
		 */
		record Denied(Dimension dimension, String name, String byRole, Cause cause) implements Decision {

			/**
			 * The refusal in the acting user's language, for a client that has to show it to them.
			 */
			public ResKey reason() {
				return I18NConstants.denial(dimension(), cause(), name(), byRole());
			}

			/**
			 * The refusal in English, for a log line and for a message fed back to the model.
			 *
			 * <p>
			 * Names the setting an administrator has to change, never anything about what was
			 * withheld.
			 * </p>
			 */
			public String message() {
				String what = dimension().label() + " '" + name() + "'";
				switch (cause()) {
					case INFRASTRUCTURE_AGENT:
						return "Permission denied: " + what + " is an infrastructure agent - the"
							+ " application runs it on a user's behalf and nobody converses with it."
							+ " No role grant changes that.";
					case AGENTS_OWN_DENIED:
						return "Permission denied: the agent uses " + what + " itself, and it is"
							+ " explicitly denied to your role '" + byRole() + "'. The agent cannot run"
							+ " for this account at all.";
					case ROLE_DENY:
						return "Permission denied: " + what + " is explicitly denied to your role '"
							+ byRole() + "'.";
					case NOT_THE_AGENTS:
						return "Permission denied: " + what + " is not the one the agent uses, and none"
							+ " of your roles permits choosing another.";
					default:
						return "Permission denied: " + what + " is permitted by none of your roles.";
				}
			}
		}
	}

	/** What one role says: the names it permits, and the names it forbids. */
	private record Entry(Set<String> grants, Set<String> denies) {
		// Pure data.
	}

	private final Dimension _dimension;

	private final Fallback _fallback;

	private final Map<String, Entry> _byRole;

	private RoleAccessPolicy(Dimension dimension, Fallback fallback, Map<String, Entry> byRole) {
		_dimension = dimension;
		_fallback = fallback;
		_byRole = byRole;
	}

	/** Which agents the given policy lets a role run. */
	public static RoleAccessPolicy agents(ToolAccessPolicy policy) {
		AgentAccess section = policy.getAgentAccess();
		Map<String, Entry> byRole = new TreeMap<>();
		for (AgentRoleAccess role : section.getRoles().values()) {
			byRole.put(role.getName(), new Entry(
				names(role.getGrants(), AgentGrant::getAgents),
				names(role.getDenies(), AgentGrant::getAgents)));
		}
		return new RoleAccessPolicy(Dimension.AGENT, fallbackOf(section.getDefault()), byRole);
	}

	/** Which models the given policy lets a role choose. */
	public static RoleAccessPolicy models(ToolAccessPolicy policy) {
		ModelAccess section = policy.getModelAccess();
		Map<String, Entry> byRole = new TreeMap<>();
		for (ModelRoleAccess role : section.getRoles().values()) {
			byRole.put(role.getName(), new Entry(
				names(role.getGrants(), ModelGrant::getModels),
				names(role.getDenies(), ModelGrant::getModels)));
		}
		return new RoleAccessPolicy(Dimension.MODEL, fallbackOf(section.getDefault()), byRole);
	}

	/** Which MCP servers the given policy lets a role attach. */
	public static RoleAccessPolicy mcpServers(ToolAccessPolicy policy) {
		McpAccess section = policy.getMcpAccess();
		Map<String, Entry> byRole = new TreeMap<>();
		for (McpRoleAccess role : section.getRoles().values()) {
			byRole.put(role.getName(), new Entry(
				names(role.getGrants(), McpGrant::getServers),
				names(role.getDenies(), McpGrant::getServers)));
		}
		return new RoleAccessPolicy(Dimension.MCP_SERVER, fallbackOf(section.getDefault()), byRole);
	}

	private static Fallback fallbackOf(AgentDefault value) {
		return value == AgentDefault.NONE ? Fallback.NONE : Fallback.ALL;
	}

	private static Fallback fallbackOf(ModelDefault value) {
		switch (value) {
			case ALL:
				return Fallback.ALL;
			case NONE:
				return Fallback.NONE;
			default:
				return Fallback.BOUND;
		}
	}

	private static Fallback fallbackOf(McpDefault value) {
		switch (value) {
			case ALL:
				return Fallback.ALL;
			case NONE:
				return Fallback.NONE;
			default:
				return Fallback.BOUND;
		}
	}

	private static <G> Set<String> names(Collection<G> entries, Function<G, List<String>> namesOf) {
		Set<String> result = new LinkedHashSet<>();
		for (G entry : entries) {
			for (String name : namesOf.apply(entry)) {
				if (name != null && !name.isBlank()) {
					result.add(name.trim());
				}
			}
		}
		return result;
	}

	/** What this evaluation is about. */
	public Dimension dimension() {
		return _dimension;
	}

	/** What this section admits from a user none of whose roles it names. */
	public Fallback fallback() {
		return _fallback;
	}

	/** The roles this section names, in a stable order. */
	public Set<String> configuredRoles() {
		return new TreeSet<>(_byRole.keySet());
	}

	/** Every name this section mentions, granted or denied, in a stable order. */
	public Set<String> mentionedNames() {
		Set<String> result = new TreeSet<>();
		for (Entry entry : _byRole.values()) {
			result.addAll(entry.grants());
			result.addAll(entry.denies());
		}
		return result;
	}

	/**
	 * Whether what the agent itself uses is worth <em>offering</em> as a choice.
	 *
	 * <p>
	 * It is always <em>accepted</em> - a request that names the model its agent is assigned is not
	 * choosing anything. {@link Fallback#NONE} says that it should not be offered either, which is the
	 * only thing that tells it from {@link Fallback#BOUND}.
	 * </p>
	 */
	public boolean offersAgentChoice() {
		return _fallback != Fallback.NONE;
	}

	/**
	 * Whether the roles held by a user permit the given name.
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>; may
	 *        be empty, in which case only the section default applies.
	 * @param name
	 *        The agent, model or server name being asked about.
	 * @param agentsOwn
	 *        What the agent of this request uses itself - its assigned model, its declared servers.
	 *        Empty for the agent dimension, and for a question asked without an agent in hand.
	 * @return The decision, carrying the permitting role or the reason for the refusal.
	 */
	public Decision check(Collection<String> heldRoleNames, String name, Collection<String> agentsOwn) {
		List<String> roleNames = stableOrder(heldRoleNames);

		for (String roleName : roleNames) {
			Entry entry = _byRole.get(roleName);
			if (entry != null && entry.denies().contains(name)) {
				return new Decision.Denied(_dimension, name, roleName, Cause.ROLE_DENY);
			}
		}
		for (String roleName : roleNames) {
			Entry entry = _byRole.get(roleName);
			if (entry != null && entry.grants().contains(name)) {
				return new Decision.Granted(roleName);
			}
		}
		if (agentsOwn != null && agentsOwn.contains(name)) {
			return new Decision.Granted(AGENT_ROLE);
		}
		for (String roleName : roleNames) {
			if (_byRole.containsKey(roleName)) {
				return new Decision.Denied(_dimension, name, null, Cause.NOT_GRANTED);
			}
		}
		switch (_fallback) {
			case ALL:
				return new Decision.Granted(DEFAULT_ROLE);
			case BOUND:
				return new Decision.Denied(_dimension, name, null, Cause.NOT_THE_AGENTS);
			default:
				return new Decision.Denied(_dimension, name, null, Cause.NOT_GRANTED);
		}
	}

	/**
	 * Which of the given names the roles held by a user permit.
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root.
	 * @param candidates
	 *        The names the application has.
	 * @param agentsOwn
	 *        What the agents of this user use themselves; ignored when this section says such a choice
	 *        is not to be offered.
	 * @return The permitted names, in the order the candidates were given.
	 */
	public List<String> allowed(Collection<String> heldRoleNames, Collection<String> candidates,
			Collection<String> agentsOwn) {
		Collection<String> offered = offersAgentChoice() ? agentsOwn : Set.of();
		List<String> result = new ArrayList<>();
		for (String candidate : candidates) {
			if (check(heldRoleNames, candidate, offered) instanceof Decision.Granted) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * The held roles in a defined order, so that two roles disagreeing produce the same sentence
	 * twice.
	 *
	 * @see RoleToolAccess
	 */
	private static List<String> stableOrder(Collection<String> heldRoleNames) {
		return new ArrayList<>(new TreeSet<>(heldRoleNames));
	}
}
