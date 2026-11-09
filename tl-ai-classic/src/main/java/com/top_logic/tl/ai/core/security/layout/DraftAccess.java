/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Cause;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Fallback;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.tl.ai.core.security.ToolAccessExplanation;

/**
 * What one role on its own may use under the policy being edited, and why.
 *
 * <p>
 * The answer the matrix overlay and the cell tooltips show. It is computed from the <em>draft</em>,
 * not from the running service: it says what the policy on the screen would do, which is the question
 * of somebody editing it. The effective-permissions tab answers for the running service and for
 * accounts that hold several roles.
 * </p>
 */
/*
 * Issue #404. Every verdict is the enforcement's own: RoleAccessPolicy and RunAuthorization for the
 * three run sections, RoleToolAccess for the capabilities, each built over the draft. What this class
 * adds is only the two facts a single cell cannot show - that the run refuses an agent the cell grants,
 * because the agent is infrastructure or because its own model or server is forbidden to the role, and
 * that a model or server is reachable because an agent the role may run uses it.
 */
public final class DraftAccess {

	/**
	 * What the overlay puts on a cell beyond the state the cell shows.
	 */
	public enum Mark {

		/** Nothing: the cell says what the run does. */
		NONE,

		/**
		 * The run refuses what the cell or the section default grants, for a reason no cell of this
		 * matrix shows.
		 */
		REFUSED,

		/**
		 * Reachable although no cell grants it, because an agent the role may run uses it itself.
		 */
		AGENTS_OWN;
	}

	/**
	 * The verdict for one name of one run section.
	 *
	 * @param decision
	 *        What the run decides.
	 * @param mark
	 *        What the overlay adds to the cell.
	 */
	public record RunVerdict(Decision decision, Mark mark) {

		/** Whether a run of the role may use the name. */
		public boolean allowed() {
			return decision instanceof Decision.Granted;
		}
	}

	/**
	 * The verdict for one capability group.
	 *
	 * @param explanation
	 *        What the policy decides for the role, and which rule decided.
	 * @param background
	 *        Whether an unattended run may reach the group at all.
	 */
	public record CapabilityVerdict(ToolAccessExplanation explanation, boolean background) {

		/** Whether a run of the role may use the group in the given run mode. */
		public boolean allowed(boolean unattended) {
			return explanation.allowed() && (!unattended || background);
		}
	}

	private final PolicyDraft _draft;

	private final AgentFacts _facts;

	private final Function<Dimension, Collection<String>> _knownNames;

	private final RoleAccessPolicy _agents;

	private final RoleAccessPolicy _models;

	private final RoleAccessPolicy _mcpServers;

	private final RoleToolAccess _tools;

	private final Map<String, RoleAccess> _byRole = new HashMap<>();

	/**
	 * Creates a {@link DraftAccess}.
	 *
	 * @param draft
	 *        The policy to evaluate. Read once: a later edit of the draft needs a new instance.
	 * @param facts
	 *        What the application says about its agents.
	 * @param knownNames
	 *        The names of one run dimension the application has.
	 */
	public DraftAccess(PolicyDraft draft, AgentFacts facts, Function<Dimension, Collection<String>> knownNames) {
		_draft = draft;
		_facts = facts;
		_knownNames = knownNames;
		_agents = RoleAccessPolicy.agents(draft.config());
		_models = RoleAccessPolicy.models(draft.config());
		_mcpServers = RoleAccessPolicy.mcpServers(draft.config());
		_tools = AiToolAccessService.evaluate(draft.config());
	}

	/** What the given section admits from a role it does not name. */
	public Fallback fallback(Dimension dimension) {
		switch (dimension) {
			case MODEL:
				return _models.fallback();
			case MCP_SERVER:
				return _mcpServers.fallback();
			default:
				return _agents.fallback();
		}
	}

	/** The draft this answers for. */
	public PolicyDraft draft() {
		return _draft;
	}

	/**
	 * The verdict for one name of one run section, for a user holding the given role alone.
	 */
	public RunVerdict run(String roleName, Dimension dimension, String name) {
		return role(roleName).run(dimension, name);
	}

	/**
	 * The verdict for one capability group, for a user holding the given role alone.
	 */
	public CapabilityVerdict capability(String roleName, String group) {
		return new CapabilityVerdict(_tools.explainCapability(List.of(roleName), group),
			_tools.capabilityAllowedInBackground(group));
	}

	/**
	 * The agents a user holding the given role alone can run and that use the given model or server
	 * themselves.
	 */
	public List<String> agentsUsing(String roleName, Dimension dimension, String name) {
		List<String> result = new ArrayList<>();
		for (String agentName : runnableAgents(roleName)) {
			boolean uses = dimension == Dimension.MODEL
				? name.equals(_facts.assignedModel(agentName))
				: dimension == Dimension.MCP_SERVER && _facts.declaredServers(agentName).contains(name);
			if (uses) {
				result.add(agentName);
			}
		}
		return result;
	}

	/**
	 * The agents a user holding the given role alone can actually run: permitted, not infrastructure,
	 * and not stopped by a refusal of their own model or servers.
	 */
	public List<String> runnableAgents(String roleName) {
		return role(roleName).runnableAgents();
	}

	private RoleAccess role(String roleName) {
		return _byRole.computeIfAbsent(roleName, RoleAccess::new);
	}

	/** The run verdicts of one role, computed once for all three sections. */
	private final class RoleAccess {

		private final Set<String> _roles;

		private final RunAuthorization _authorization;

		private final Map<String, RunVerdict> _agentVerdicts = new LinkedHashMap<>();

		private final List<String> _runnable = new ArrayList<>();

		private final Set<String> _ownModels = new TreeSet<>();

		private final Set<String> _ownServers = new TreeSet<>();

		RoleAccess(String roleName) {
			_roles = Set.of(roleName);
			// No principal: a role on its own, so no administrator bypass either.
			_authorization = new RunAuthorization(_agents, _models, _mcpServers, null, _roles);
			for (String agentName : _knownNames.apply(Dimension.AGENT)) {
				RunVerdict verdict = agentVerdict(agentName);
				_agentVerdicts.put(agentName, verdict);
				if (verdict.allowed()) {
					_runnable.add(agentName);
					String model = _facts.assignedModel(agentName);
					if (model != null) {
						_ownModels.add(model);
					}
					_ownServers.addAll(_facts.declaredServers(agentName));
				}
			}
		}

		private RunVerdict agentVerdict(String agentName) {
			if (_facts.isInfrastructure(agentName)) {
				return new RunVerdict(new Decision.Denied(Dimension.AGENT, agentName, null,
					Cause.INFRASTRUCTURE_AGENT), Mark.REFUSED);
			}
			Decision decision = _agents.check(_roles, agentName, Set.of());
			if (decision instanceof Decision.Denied) {
				return new RunVerdict(decision, Mark.NONE);
			}
			Decision.Denied own = _authorization.agentsOwnRefusal(_facts.assignedModel(agentName),
				_facts.declaredServers(agentName));
			return own == null ? new RunVerdict(decision, Mark.NONE) : new RunVerdict(own, Mark.REFUSED);
		}

		RunVerdict run(Dimension dimension, String name) {
			switch (dimension) {
				case MODEL:
					return nameVerdict(_models.check(_roles, name, _ownModels));
				case MCP_SERVER:
					return nameVerdict(_mcpServers.check(_roles, name, _ownServers));
				default:
					RunVerdict known = _agentVerdicts.get(name);
					// A name the application does not have cannot run; what the cell says is all there is.
					return known != null ? known : new RunVerdict(_agents.check(_roles, name, Set.of()), Mark.NONE);
			}
		}

		private RunVerdict nameVerdict(Decision decision) {
			boolean agentsOwn = decision instanceof Decision.Granted granted
				&& RoleAccessPolicy.AGENT_ROLE.equals(granted.byRole());
			return new RunVerdict(decision, agentsOwn ? Mark.AGENTS_OWN : Mark.NONE);
		}

		List<String> runnableAgents() {
			return _runnable;
		}
	}

}
