/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;

/**
 * What one agent run may <em>be</em>: which agent it runs, which model it runs on, which MCP servers
 * it reaches - decided against the roles of the run's principal.
 *
 * <p>
 * The sibling of {@link ToolAuthorization}, which decides what a run may <em>do</em>. Both are
 * resolved once, when the run's security context is built, and carried on it; neither re-reads thread
 * state per check, for the reason {@link ToolAuthorization} gives.
 * </p>
 *
 * <p>
 * Every method answers about one name the caller asked for. A name the agent itself uses - the model
 * it is assigned, a server it declares - is not a choice and is permitted, which is why the callers
 * pass it in: a client that echoes the agent's own model back is not choosing a model.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.agent.TopLogicSecurityContext
 * @see AiToolAccessService#runAuthorizationFor(Person)
 */
public class RunAuthorization {

	/**
	 * Permits every agent, model and MCP server without consulting a policy.
	 *
	 * <p>
	 * <b>A test seam.</b> No production run carries it:
	 * {@link AiToolAccessService#runAuthorizationFor(Person)} always evaluates the running service's
	 * policy and refuses if that service is down. An application that authored no policy gets a real
	 * authorization over an empty one - which permits every agent, the agent's own model and the
	 * agent's own servers, and that is a decision rather than the absence of one. It waives the
	 * persona rule of {@link #agentRefusal(String, boolean)} too, so that the internal entry points
	 * built on it - {@code AgentFactory.runAgent} with an unrestricted context, the test setups -
	 * keep reaching every agent the application has.
	 * </p>
	 */
	public static final RunAuthorization UNCHECKED =
		new RunAuthorization(null, null, null, null, Set.of()).withProductChosenAgent();

	private final RoleAccessPolicy _agents;

	private final RoleAccessPolicy _models;

	private final RoleAccessPolicy _mcpServers;

	private final Person _principal;

	private final Set<String> _heldRoleNames;

	/**
	 * Whether the agent of this run is the product's choice rather than the user's.
	 *
	 * @see #withProductChosenAgent()
	 */
	private final boolean _productChosenAgent;

	/**
	 * Creates a {@link RunAuthorization} for one run.
	 *
	 * @param agents
	 *        Which agents a role may run, or <code>null</code> to check nothing.
	 * @param models
	 *        Which models a role may choose, or <code>null</code> to check nothing.
	 * @param mcpServers
	 *        Which MCP servers a role may attach, or <code>null</code> to check nothing.
	 * @param principal
	 *        The user the run acts as, or <code>null</code> for a run with no user identity, who then
	 *        holds no role.
	 * @param heldRoleNames
	 *        The names of the roles {@code principal} holds on the security root, resolved once by the
	 *        caller.
	 */
	public RunAuthorization(RoleAccessPolicy agents, RoleAccessPolicy models, RoleAccessPolicy mcpServers,
			Person principal, Collection<String> heldRoleNames) {
		this(agents, models, mcpServers, principal, heldRoleNames, false);
	}

	private RunAuthorization(RoleAccessPolicy agents, RoleAccessPolicy models,
			RoleAccessPolicy mcpServers, Person principal, Collection<String> heldRoleNames,
			boolean productChosenAgent) {
		_agents = agents;
		_models = models;
		_mcpServers = mcpServers;
		_principal = principal;
		_heldRoleNames = heldRoleNames == null ? Set.of() : Set.copyOf(heldRoleNames);
		_productChosenAgent = productChosenAgent;
	}

	/**
	 * The same authorization with the agent dimension left undecided.
	 *
	 * <p>
	 * For a run whose agent the <em>product</em> chose rather than the user: the chat's prompt
	 * improver is one, and every agent the chat hides from its picker is another. {@code
	 * <agent-access>} decides which persona <em>answers</em> the user, and such an agent never
	 * answers - it rewrites a draft, or does some other piece of work on the user's behalf. Gating it
	 * would make an administrator grant an agent nobody can discover, and the feature would break for
	 * every restricted role with no way to find out why.
	 * </p>
	 *
	 * <p>
	 * The model and the MCP servers stay decided, and so does every tool the run then calls - this
	 * widens the agent dimension and nothing else. Such a run may also name an agent marked
	 * {@code infrastructure}, which is the other half of "the product chose it" - see
	 * {@link #withProductChosenAgent()}.
	 * </p>
	 */
	public RunAuthorization withoutAgentCheck() {
		if (_agents == null && _productChosenAgent) {
			return this;
		}
		return new RunAuthorization(null, _models, _mcpServers, _principal, _heldRoleNames, true);
	}

	/**
	 * The same authorization for a run whose agent the product named, keeping the allowlist.
	 *
	 * <p>
	 * The half of {@link #withoutAgentCheck()} that answers the {@link #agentRefusal(String, boolean)
	 * persona question} alone: an {@code infrastructure} agent - one the application runs <em>for</em>
	 * a user rather than letting them talk to it - is refused while a user's own run names it, and
	 * permitted here. Which agents the account may run is still decided by {@code <agent-access>}, so
	 * this widens strictly less than {@link #withoutAgentCheck()} does.
	 * </p>
	 *
	 * <p>
	 * Carried by {@link #UNCHECKED} and by nothing else in this class. A production entry point states
	 * it through {@code SecurityContext.withoutAgentChoice()}, which is the one seam that says in code
	 * that the user did not pick this agent.
	 * </p>
	 */
	public RunAuthorization withProductChosenAgent() {
		if (_productChosenAgent) {
			return this;
		}
		return new RunAuthorization(_agents, _models, _mcpServers, _principal, _heldRoleNames, true);
	}

	/** The roles the run's principal holds on the security root. */
	public Set<String> heldRoleNames() {
		return _heldRoleNames;
	}

	/**
	 * The account this run acts as, or <code>null</code> when it names none.
	 *
	 * <p>
	 * Who a run is <em>for</em>, which is a different question from what it may do. Read by the
	 * parts that attribute a run rather than authorize it - the concurrency and budget ceilings of
	 * {@code RunLimits} - so that they do not each resolve the account their own way and disagree
	 * with the policy about whose run it is.
	 * </p>
	 *
	 * <p>
	 * <code>null</code> for {@link #UNCHECKED}, and therefore for every test context: a run that
	 * cannot name a principal is not attributed to one.
	 * </p>
	 */
	public Person principal() {
		return _principal;
	}

	/**
	 * Why the run's principal may not run the named agent, recording the refusal in the audit log.
	 *
	 * <p>
	 * Two questions, and the first one no role answers: an <em>infrastructure</em> agent is one the
	 * application runs for a user rather than one a user talks to, so naming it as a conversation
	 * partner is refused whatever {@code <agent-access>} grants - including under
	 * {@code default="all"}. The product's own call path says that it is not a user's choice by
	 * carrying {@link #withProductChosenAgent()}, which is what
	 * {@code SecurityContext.withoutAgentChoice()} hands it.
	 * </p>
	 *
	 * @param agentName
	 *        The agent the run wants to be.
	 * @param infrastructure
	 *        Whether that agent is marked {@code AgentDefinition#infrastructure()}. Pass
	 *        <code>false</code> for a name no definition could be read for - a name nothing knows is
	 *        refused as unknown elsewhere, and must not be refused as infrastructure here.
	 * @return The refusal, or <code>null</code> if the agent is permitted.
	 */
	public Decision.Denied agentRefusal(String agentName, boolean infrastructure) {
		return audit(silentAgentRefusal(agentName, infrastructure));
	}

	/**
	 * Why the run's principal may not choose the named model, recording the refusal in the audit log.
	 *
	 * @param modelName
	 *        The model the request asks for.
	 * @param assignedModel
	 *        The model the agent of this request is assigned, which is not a choice and is therefore
	 *        always permitted; may be <code>null</code>.
	 * @return The refusal, or <code>null</code> if the model is permitted.
	 */
	public Decision.Denied modelRefusal(String modelName, String assignedModel) {
		return audit(decide(_models, modelName, assignedModel == null ? Set.of() : Set.of(assignedModel)));
	}

	/**
	 * Why the run's principal may not attach the named MCP server, recording the refusal in the audit
	 * log.
	 *
	 * @param serverName
	 *        The server the request asks for.
	 * @param agentServers
	 *        The servers the agent of this request declares itself, which are not a choice and are
	 *        therefore permitted unless a role forbids them by name; may be <code>null</code>.
	 * @return The refusal, or <code>null</code> if the server is permitted.
	 */
	public Decision.Denied mcpServerRefusal(String serverName, Collection<String> agentServers) {
		return audit(decide(_mcpServers, serverName, agentServers == null ? Set.of() : agentServers));
	}

	/**
	 * Why the agent's <em>own</em> model may not be used, recording the refusal in the audit log.
	 *
	 * <p>
	 * Asked for every run, not only for one that names a model: what an agent is assigned is not a
	 * choice, and is therefore permitted - but a {@code <deny>} still forbids it, and until this was
	 * asked a refusal could be escaped by saying nothing. A client that names the agent's own model
	 * was refused while the identical silent request ran.
	 * </p>
	 *
	 * @param assignedModel
	 *        The model the agent of this run is assigned; may be <code>null</code>.
	 * @return The refusal, or <code>null</code> if the run may use it.
	 */
	public Decision.Denied assignedModelRefusal(String assignedModel) {
		return audit(agentsOwn(decide(_models, assignedModel, singleton(assignedModel))));
	}

	/**
	 * Why a server the agent <em>declares</em> may not be reached, recording the refusal in the audit
	 * log.
	 *
	 * @param serverName
	 *        One of the servers the agent of this run declares.
	 * @param declaredServers
	 *        All of them.
	 * @return The refusal, or <code>null</code> if the run may reach it.
	 * @see #assignedModelRefusal(String)
	 */
	public Decision.Denied declaredServerRefusal(String serverName, Collection<String> declaredServers) {
		return audit(agentsOwn(decide(_mcpServers, serverName,
			declaredServers == null ? Set.of() : declaredServers)));
	}

	/**
	 * Whether a run of an agent with the given model and servers is possible at all for this
	 * principal, without logging a refusal.
	 *
	 * <p>
	 * For a menu: an agent whose own model or own server a role forbids cannot run for this account,
	 * so offering it would offer a turn that is refused whatever the user then picks.
	 * </p>
	 *
	 * @param assignedModel
	 *        The model that agent is assigned; may be <code>null</code>.
	 * @param declaredServers
	 *        The servers it declares; may be <code>null</code>.
	 */
	public boolean allowsAgentsOwn(String assignedModel, Collection<String> declaredServers) {
		return agentsOwnRefusal(assignedModel, declaredServers) == null;
	}

	/**
	 * Why a run of an agent with the given model and servers is impossible for this principal, without
	 * logging a refusal.
	 *
	 * <p>
	 * The same question {@link #allowsAgentsOwn} answers, for a caller that has to <em>say</em> why -
	 * the effective-access view names the model or the server that stops the agent, rather than
	 * leaving an administrator to find it. Silent on purpose: enumerating what an account may run is
	 * not a refused call, and one audit line per row would drown the ones that are.
	 * </p>
	 *
	 * @param assignedModel
	 *        The model that agent is assigned; may be <code>null</code>.
	 * @param declaredServers
	 *        The servers it declares; may be <code>null</code>.
	 * @return The first refusal found, or <code>null</code> if the agent can run.
	 */
	public Decision.Denied agentsOwnRefusal(String assignedModel, Collection<String> declaredServers) {
		Decision.Denied model = decide(_models, assignedModel, singleton(assignedModel));
		if (model != null) {
			return agentsOwn(model);
		}
		if (declaredServers != null) {
			for (String serverName : declaredServers) {
				Decision.Denied server = decide(_mcpServers, serverName, declaredServers);
				if (server != null) {
					return agentsOwn(server);
				}
			}
		}
		return null;
	}

	/**
	 * The same refusal, said as what it is: not a choice of the user's that was turned down, but two
	 * settings of the application that contradict each other.
	 */
	private static Decision.Denied agentsOwn(Decision.Denied denied) {
		return denied == null ? null
			: new Decision.Denied(denied.dimension(), denied.name(), denied.byRole(),
				RoleAccessPolicy.Cause.AGENTS_OWN_DENIED);
	}

	private static Set<String> singleton(String name) {
		return name == null ? Set.of() : Set.of(name);
	}

	/**
	 * Whether the run's principal may run the named agent, without logging a refusal.
	 *
	 * @param infrastructure
	 *        Whether that agent is marked {@code AgentDefinition#infrastructure()}.
	 * @see #agentRefusal(String, boolean)
	 * @see #silentAgentRefusal(String, boolean)
	 */
	public boolean allowsAgent(String agentName, boolean infrastructure) {
		return silentAgentRefusal(agentName, infrastructure) == null;
	}

	/**
	 * Why the run's principal may not run the named agent, without logging a refusal.
	 *
	 * <p>
	 * The same question {@link #allowsAgent} answers, for a caller that has to <em>say</em> why - a
	 * delegate filter tells the model that this agent is out of reach, and saying so by naming
	 * capabilities that are not the reason sends the user to their administrator for a grant that
	 * changes nothing (#517). Silent on purpose, like {@link #agentsOwnRefusal}: a filter asked once
	 * per agent per turn is not a refused call, and one audit line per row would drown the ones that
	 * are. The run that is actually attempted is audited by {@link #agentRefusal}.
	 * </p>
	 *
	 * @param agentName
	 *        The agent in question.
	 * @param infrastructure
	 *        Whether that agent is marked {@code AgentDefinition#infrastructure()}.
	 * @return The refusal, or <code>null</code> if the agent is permitted.
	 */
	public Decision.Denied silentAgentRefusal(String agentName, boolean infrastructure) {
		if (infrastructure && !_productChosenAgent) {
			return new Decision.Denied(RoleAccessPolicy.Dimension.AGENT, agentName, null,
				RoleAccessPolicy.Cause.INFRASTRUCTURE_AGENT);
		}
		return decide(_agents, agentName, Set.of());
	}

	/**
	 * Which of the given agents the run's principal may run.
	 *
	 * @param candidates
	 *        The agents the application has.
	 * @return The permitted names, in the order the candidates were given.
	 */
	public List<String> allowedAgents(Collection<String> candidates) {
		return allowed(_agents, candidates, Set.of());
	}

	/**
	 * Which of the given models the run's principal may choose.
	 *
	 * @param candidates
	 *        The models the application has configured.
	 * @param assignedModels
	 *        The models the agents this user may run are assigned.
	 * @return The permitted names, in the order the candidates were given.
	 */
	public List<String> allowedModels(Collection<String> candidates, Collection<String> assignedModels) {
		return allowed(_models, candidates, assignedModels);
	}

	/**
	 * Which of the given MCP servers the run's principal may attach.
	 *
	 * @param candidates
	 *        The servers the application has configured.
	 * @param agentServers
	 *        The servers the agents this user may run declare themselves.
	 * @return The permitted names, in the order the candidates were given.
	 */
	public List<String> allowedMcpServers(Collection<String> candidates, Collection<String> agentServers) {
		return allowed(_mcpServers, candidates, agentServers);
	}

	private List<String> allowed(RoleAccessPolicy access, Collection<String> candidates,
			Collection<String> agentsOwn) {
		if (access == null || isAdmin()) {
			return List.copyOf(candidates);
		}
		return access.allowed(_heldRoleNames, candidates, agentsOwn);
	}

	/**
	 * The refusal for the given name, or <code>null</code> if it is permitted. No side effects.
	 *
	 * <p>
	 * The administrator bypass mirrors {@link ToolAuthorization}: an administrator already bypasses the
	 * engine's model rights and the tool layer, and a third layer that disagreed would produce an
	 * unexplainable half-denial. There is no background ceiling here - which agent a run is and what it
	 * runs on is decided before it starts, and an unattended run inherits the acting account's.
	 * </p>
	 */
	private Decision.Denied decide(RoleAccessPolicy access, String name, Collection<String> agentsOwn) {
		if (access == null || name == null || name.isBlank()) {
			return null;
		}
		if (isAdmin()) {
			return null;
		}
		return access.check(_heldRoleNames, name, agentsOwn) instanceof Decision.Denied denied
			? denied
			: null;
	}

	private boolean isAdmin() {
		return _principal != null && _principal.isAdmin();
	}

	/**
	 * Records the refusal where an operator can find it, then returns it unchanged.
	 *
	 * <p>
	 * The same shape as {@code ToolAuthorization.audit}, so that one log format answers "why was this
	 * refused?" for both layers.
	 * </p>
	 */
	private Decision.Denied audit(Decision.Denied denied) {
		if (denied != null) {
			Logger.warn("Agent run refused: " + denied.dimension().label() + "='" + denied.name() + "'"
				+ " user=" + (_principal == null ? "(none)" : _principal.getName())
				+ " roles=" + _heldRoleNames
				+ " reason=" + denied.message(), RunAuthorization.class);
		}
		return denied;
	}
}
