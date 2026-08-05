/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.tl.ai.core.mcp.McpServerRegistry;
import com.top_logic.tl.ai.core.security.ToolGroupResources;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * What one agent can actually do for the principal of one run.
 *
 * <p>
 * The tool schemas sent to the provider are already filtered per user
 * ({@link ToolPermissionFilter}), so the model never sees a tool it may not call. What it cannot see
 * is <em>why</em> something is absent: "you are not permitted", "that skill is not loaded yet" and
 * "that does not exist" all look alike. This class answers the first of the three, in the places
 * where the difference is visible to the model - the skill catalog in the system prompt, the tool set
 * a skill hands over when it is loaded, and the delegates an agent is offered.
 * </p>
 *
 * <p>
 * <b>Capabilities, not tools.</b> A missing capability is named by its
 * {@link com.top_logic.tl.ai.tool.api.ToolGroups group}, because that is the unit an administrator
 * grants: {@code data.write} is one line a user can take to their administrator, where the tools
 * behind it are not. The set is computed from the tools <em>this agent</em> can reach - its own
 * {@code <tools>} plus the tools of its skills - rather than from every group in the deployment: a
 * capability no tool of this agent needs is noise in its prompt.
 * </p>
 *
 * <p>
 * <b>A delegate is judged by everything it can reach.</b> Whether a sub-agent is worth offering is
 * the same question one level deeper: {@link #unusableReason(AgentDefinition)} walks the delegate's
 * own tools, its skills' tools, its MCP tools and its own delegates, and applies the very rule the
 * skill filter applies to what it finds. The walk is over the {@link AgentGraph} and bounded by the
 * agents it has already seen, so a delegation cycle terminates.
 * </p>
 *
 * <p>
 * The same predicate refuses a spawn that could only come back empty-handed - see
 * {@link com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner#detachedSpawnRefusal}. There it is
 * asked against the context the <em>child</em> will get rather than this turn's, because a detached
 * subagent is an unattended run and loses whatever the {@code <background-allowlist>} withholds.
 * </p>
 *
 * <p>
 * <b>Authorization only, not the execution-mode gate.</b> Everything here asks
 * {@link SecurityContext#hasToolPermission(Tool)}, which is bound once per run and cannot change
 * while it lasts. The {@link ExecutionMode} half of {@link ToolPermissionFilter} is deliberately not
 * consulted: maintenance mode can open and close mid-run, and a tool withheld from the session
 * catalog because of it would stay missing after it opened. That half stays where it belongs, in
 * the per-turn schema filter.
 * </p>
 *
 * <p>
 * <b>This is UX, not a control.</b> Nothing here decides whether a tool may run - the invocation
 * gate in {@link AgentLoop} does, and it is unaffected by everything below. See
 * {@code doc/security/agent-security-architecture.md} section 2.2: all gates are deterministic Java
 * checks on the call path, never prompt instructions.
 * </p>
 */
public class AgentCapabilities {

	/**
	 * The definitions a delegate's reachable tool set is walked over.
	 *
	 * <p>
	 * An indirection rather than a direct {@link AgentDefinitionQuery} call, so that the recursive
	 * predicate can be exercised on a hand-built graph - a delegation cycle, an agent with no tools of
	 * its own - without booting the services that hold the real one.
	 * </p>
	 */
	public interface AgentGraph {

		/** The graph of the running deployment. */
		AgentGraph DEPLOYED = new AgentGraph() {

			@Override
			public AgentDefinition agent(String name) {
				return AgentDefinitionQuery.getInstance().findAgentByName(name);
			}

			@Override
			public SkillDefinition skill(String name) {
				return AgentDefinitionQuery.getInstance().findSkillByName(name);
			}

			@Override
			public List<Tool> mcpTools(String serverName) {
				if (!McpServerRegistry.Module.INSTANCE.isActive()) {
					return null;
				}
				return McpServerRegistry.getInstance().getTools(serverName);
			}
		};

		/** The named agent, or <code>null</code> if there is none. */
		AgentDefinition agent(String name);

		/** The named skill, or <code>null</code> if there is none. */
		SkillDefinition skill(String name);

		/**
		 * The tools of the named MCP server, or <code>null</code> if the server is unknown or not
		 * connected.
		 */
		List<Tool> mcpTools(String serverName);
	}

	/**
	 * The sentence that closes every message which names a capability, or deliberately names none.
	 *
	 * <p>
	 * The same instruction the {@link #notice(Collection, Collection) capability notice} gives for the
	 * general case, repeated where the model is actually holding a refusal: a message that explains
	 * itself and then leaves the model free to add a cause of its own has not fixed the guessing. This
	 * is prompt text, not a control - the gate is unaffected by whether the model obeys it.
	 * </p>
	 */
	static final String NO_GUESSING =
		"Do not attribute this to a capability unless this message names one.";

	private final Function<String, Tool> _toolResolver;

	private final SecurityContext _security;

	private final AgentGraph _graph;

	/**
	 * Creates an {@link AgentCapabilities} view for one run over the
	 * {@link AgentGraph#DEPLOYED deployed} agent graph.
	 *
	 * @param toolResolver
	 *        Resolves a qualified tool name to the registered {@link Tool}, or <code>null</code> if
	 *        no tool of that name exists.
	 * @param security
	 *        The run's security context.
	 */
	public AgentCapabilities(Function<String, Tool> toolResolver, SecurityContext security) {
		this(toolResolver, security, AgentGraph.DEPLOYED);
	}

	/**
	 * Creates an {@link AgentCapabilities} view for one run.
	 *
	 * @param toolResolver
	 *        Resolves a qualified tool name to the registered {@link Tool}, or <code>null</code> if
	 *        no tool of that name exists.
	 * @param security
	 *        The run's security context.
	 * @param graph
	 *        The definitions a delegate's reachable tool set is walked over.
	 */
	public AgentCapabilities(Function<String, Tool> toolResolver, SecurityContext security,
			AgentGraph graph) {
		_toolResolver = toolResolver;
		_security = security;
		_graph = graph;
	}

	/**
	 * The capability view of a run against the service-level tool registry.
	 *
	 * @param security
	 *        The run's security context.
	 */
	public static AgentCapabilities forRun(SecurityContext security) {
		return new AgentCapabilities(name -> ToolRegistryService.getInstance().getTool(name), security);
	}

	/**
	 * The definitions this view walks a delegate's reachable tool set over.
	 */
	public AgentGraph graph() {
		return _graph;
	}

	/**
	 * Whether the run authorizes tools at all.
	 *
	 * <p>
	 * Everything this class produces is empty or permissive when it does not, so that an application
	 * which has configured no {@code <tool-access>} policy keeps exactly its previous behaviour -
	 * including a byte-identical system prompt.
	 * </p>
	 */
	public boolean enforced() {
		return _security.securityMode().enforcesToolAccess();
	}

	/**
	 * Whether the run's principal may use the given tool.
	 */
	public boolean permits(Tool tool) {
		return !enforced() || _security.hasToolPermission(tool);
	}

	/**
	 * {@link #permits(Tool)} as a predicate, for the places that filter a tool stream.
	 */
	public Predicate<Tool> permitted() {
		return this::permits;
	}

	/**
	 * Why the given skill can do nothing for the run's principal, or <code>null</code> if it can.
	 *
	 * <p>
	 * A skill is kept unless <em>every</em> tool it declares is both resolvable and denied. Three
	 * cases deliberately keep it:
	 * </p>
	 * <ul>
	 * <li>A skill with no tools at all is pure instruction text - "all zero of its tools are denied"
	 * is vacuously true and must not delete it.</li>
	 * <li>A partially denied skill still applies to the tools the principal may use; only the denied
	 * ones are withheld when it is loaded.</li>
	 * <li>A skill none of whose tool names the registry resolves is kept, because a typo in a skill's
	 * tool list is indistinguishable from a denial here and must not remove the skill. An
	 * unresolvable name is skipped rather than counted as denied.</li>
	 * </ul>
	 *
	 * @param skill
	 *        The skill to judge.
	 * @return The deciding reason, suitable for a log line, or <code>null</code> if the skill is
	 *         usable.
	 */
	public String unusableReason(SkillDefinition skill) {
		if (!enforced()) {
			return null;
		}
		return unusableReason(resolve(skill.tools()), "known tools");
	}

	/**
	 * Whether the given skill lost its tools to the background ceiling rather than to the principal's
	 * roles.
	 *
	 * <p>
	 * An unattended run has no one to answer an approval prompt, so it is additionally capped by the
	 * policy's {@code <background-allowlist>} - see
	 * {@link SecurityContext#withheldByBackgroundCeiling(Tool)}. A skill the ceiling removed must not be
	 * explained by naming the user's roles: the same user in the chat would have it, and an
	 * administrator asked to grant a role they already granted finds nothing to do.
	 * </p>
	 *
	 * <p>
	 * True as soon as <em>one</em> of the skill's tools would have been permitted in an attended run:
	 * that tool alone would have kept the skill, so the run being unattended is what decided.
	 * </p>
	 *
	 * <p>
	 * The skill-level form of {@link SecurityContext#withheldByBackgroundCeiling(Tool)}, and a
	 * different question from {@link #lostToBackgroundCeiling(Collection)}: this one decides
	 * <em>whether</em> the ceiling is the explanation for one skill, by asking per tool; that one
	 * reports <em>which</em> of a set of capability names the ceiling withheld, for a message that
	 * lists them. Deciding by capability name would misjudge a skill whose tools declare no group at
	 * all, which is why this walks the tools instead.
	 * </p>
	 *
	 * @param skill
	 *        The skill to attribute, already known to be {@link #unusableReason(SkillDefinition)
	 *        unusable}.
	 */
	public boolean withheldByBackgroundCeiling(SkillDefinition skill) {
		if (!enforced()) {
			return false;
		}
		for (Tool tool : resolve(skill.tools())) {
			if (_security.withheldByBackgroundCeiling(tool)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Why the given delegate can do nothing for the run's principal, or <code>null</code> if it can.
	 *
	 * <p>
	 * The recursive form of {@link #unusableReason(SkillDefinition)}, over everything the delegate can
	 * reach ({@link #reachableTools(AgentDefinition)}), and with the same three cases deliberately
	 * keeping it:
	 * </p>
	 * <ul>
	 * <li>A delegate that reaches no tool at all is kept. A delegation-only agent - one that carries no
	 * tools of its own and exists to route work onwards - must not fall to "all zero of its tools are
	 * denied", which is vacuously true.</li>
	 * <li>A partly reachable delegate is kept and the parent is told what it cannot do, rather than
	 * discovering it by sending work that comes back refused. See
	 * {@link #unreachableCapabilities(AgentDefinition)} and
	 * {@code doc/security/agent-security-architecture.md} section 3.2.7.</li>
	 * <li>A delegate none of whose tool names resolve is kept, because a typo in an agent's tool list
	 * is indistinguishable from a denial here.</li>
	 * </ul>
	 *
	 * <p>
	 * Dropping a delegate takes nothing away from the principal: a child run inherits the parent's
	 * security context unchanged, so a delegate whose every tool is denied could only have started,
	 * filtered its own schemas to nothing and failed.
	 * </p>
	 *
	 * @param delegate
	 *        The delegate to judge. <code>null</code> - a delegation naming an agent that does not
	 *        exist - is usable, for the same reason an unresolvable tool name is: it is not a denial.
	 * @return The deciding reason, suitable for a log line, or <code>null</code> if the delegate is
	 *         usable.
	 */
	public String unusableReason(AgentDefinition delegate) {
		if (!enforced() || delegate == null) {
			return null;
		}
		return unusableReason(reachableTools(delegate), "reachable tools");
	}

	/**
	 * The capabilities the given delegate needs for some of its tools and the run's principal does not
	 * hold.
	 *
	 * <p>
	 * What makes a <em>partly</em> reachable delegate legible: it is still offered, and this names what
	 * it cannot be asked for. Empty for a delegate the principal can use in full, so the notice only
	 * ever appears where there is something to say.
	 * </p>
	 *
	 * <p>
	 * A delegate that is {@link #unusableReason(AgentDefinition) dropped entirely} has every one of its
	 * capabilities in here, which is what the drop's log line reports. The prompt never asks: a dropped
	 * delegate has no line to annotate.
	 * </p>
	 *
	 * @param delegate
	 *        The delegate to inspect, or <code>null</code>.
	 * @return The lacking capability names, in a stable order; empty when the run does not enforce tool
	 *         access.
	 */
	public Set<String> unreachableCapabilities(AgentDefinition delegate) {
		if (!enforced() || delegate == null) {
			return new TreeSet<>();
		}
		return lackingOf(reachableTools(delegate));
	}

	/**
	 * Every tool the given delegate can reach: its own, those of the skills it declares, those of its
	 * MCP servers, and - transitively - those of the agents it may itself delegate to.
	 *
	 * <p>
	 * Skills contribute the tools they <em>declare</em>, not the subset a load would hand over: a
	 * skill whose every tool is denied is dropped from the child's catalog, and its tools being denied
	 * is exactly what the answer here has to reflect.
	 * </p>
	 *
	 * <p>
	 * The walk visits each agent once. A delegation cycle (A delegates to B delegates to A) therefore
	 * terminates, and the revisit contributes nothing - there is nothing it could add that the first
	 * visit did not.
	 * </p>
	 *
	 * @param delegate
	 *        The agent to start from.
	 * @return The reachable tools that the registry resolves, by first occurrence. Names that resolve
	 *         to nothing are not in it; an unresolvable name is not a denial.
	 */
	public Collection<Tool> reachableTools(AgentDefinition delegate) {
		Map<String, Tool> reachable = new LinkedHashMap<>();
		collectReachable(delegate, new HashSet<>(), reachable);
		return reachable.values();
	}

	private void collectReachable(AgentDefinition agent, Set<String> visited, Map<String, Tool> reachable) {
		if (agent == null || !visited.add(agent.name())) {
			return;
		}
		addResolved(agent.tools(), reachable);
		for (String skillName : agent.skills()) {
			SkillDefinition skill = _graph.skill(skillName);
			if (skill != null) {
				addResolved(skill.tools(), reachable);
			}
		}
		for (String serverName : agent.mcpServers()) {
			// Not in the service-level registry, so these arrive as tools rather than as names.
			List<Tool> mcpTools = _graph.mcpTools(serverName);
			if (mcpTools == null) {
				continue;
			}
			for (Tool tool : mcpTools) {
				reachable.putIfAbsent(tool.name(), tool);
			}
		}
		for (String targetName : agent.delegations()) {
			collectReachable(_graph.agent(targetName), visited, reachable);
		}
	}

	private void addResolved(Collection<String> toolNames, Map<String, Tool> reachable) {
		for (String toolName : toolNames) {
			Tool tool = _toolResolver.apply(toolName);
			if (tool != null) {
				reachable.putIfAbsent(toolName, tool);
			}
		}
	}

	/**
	 * Why none of the given tools can serve the run's principal, or <code>null</code> if one of them
	 * can.
	 *
	 * @param tools
	 *        The tools to judge, already resolved - an unresolvable name never reaches here, so it can
	 *        neither save nor condemn what declared it.
	 * @param noun
	 *        What the tools are to the thing being judged, for the log line.
	 */
	private String unusableReason(Collection<Tool> tools, String noun) {
		Set<String> requiredCapabilities = new TreeSet<>();
		int denied = 0;
		for (Tool tool : tools) {
			if (permits(tool)) {
				return null;
			}
			denied++;
			String capability = _security.toolCapability(tool);
			requiredCapabilities.add(capability == null ? "(ungrouped)" : capability);
		}
		if (denied == 0) {
			// Nothing to filter, rather than everything denied - see the javadoc of the callers.
			return null;
		}
		return "none of its " + denied + " " + noun + " is permitted; they require " + requiredCapabilities;
	}

	/** The tools of the given names that the registry resolves, in order. */
	private List<Tool> resolve(Collection<String> toolNames) {
		List<Tool> tools = new ArrayList<>();
		for (String toolName : toolNames) {
			Tool tool = _toolResolver.apply(toolName);
			if (tool != null) {
				tools.add(tool);
			}
		}
		return tools;
	}

	/**
	 * The capabilities the run's principal does not hold, among those the given tools require.
	 *
	 * <p>
	 * Tools that resolve to nothing contribute nothing - there is no capability to name for a tool
	 * that does not exist. Neither do denied tools that are ungrouped: "you lack the capability of a
	 * tool that has none" is not actionable, and the deployment's answer to that is a
	 * {@code <tool-groups>} entry, not a sentence in a prompt.
	 * </p>
	 *
	 * @param toolNames
	 *        Qualified names of the tools to inspect.
	 * @return The lacking capability names, in a stable order; empty when the run does not enforce
	 *         tool access.
	 */
	public Set<String> lacking(Collection<String> toolNames) {
		if (!enforced()) {
			return new TreeSet<>();
		}
		return lackingOf(resolve(toolNames));
	}

	/** {@link #lacking(Collection)} for tools that are already resolved. */
	private Set<String> lackingOf(Collection<Tool> tools) {
		Set<String> lacking = new TreeSet<>();
		for (Tool tool : tools) {
			if (permits(tool)) {
				continue;
			}
			String capability = _security.toolCapability(tool);
			if (capability != null) {
				lacking.add(capability);
			}
		}
		return lacking;
	}

	/**
	 * The paragraph telling the agent how to read the absence of a tool, and which capabilities its
	 * principal lacks.
	 *
	 * <p>
	 * Empty unless the run enforces tool access, so an unenforced run's prompt is unchanged. Kept to
	 * a few sentences plus one list: this is per-turn prompt text and it competes with the agent's
	 * actual instructions.
	 * </p>
	 *
	 * <p>
	 * A capability an {@link SecurityContext#isBackgroundRun() unattended} run lost to the
	 * {@code <background-allowlist>} is reported as such and separately from one the user does not
	 * hold. They call for different things - the first for an administrator to widen the allowlist, the
	 * second for a role grant - and a detached subagent that reported the first as the second would
	 * tell its user something false about their own account.
	 * </p>
	 *
	 * @param agentToolNames
	 *        Qualified names of the agent's own tools.
	 * @param skills
	 *        The skills the agent offers, whose tools the agent can reach by loading them.
	 * @return The section to append to the generated skill catalog, or the empty string.
	 */
	public String notice(Collection<String> agentToolNames, Collection<SkillDefinition> skills) {
		if (!enforced()) {
			return "";
		}
		List<String> reachable = new ArrayList<>(agentToolNames);
		for (SkillDefinition skill : skills) {
			reachable.addAll(skill.tools());
		}
		Set<String> lacking = lacking(reachable);
		boolean nothingDenied = lacking.isEmpty() && reachable.stream()
			.map(_toolResolver)
			.filter(tool -> tool != null)
			.allMatch(this::permits);

		StringBuilder notice = new StringBuilder();
		notice.append("\n### Capability Limits\n\n");
		notice.append("Your tool list is filtered for the current user. A tool that is not in it is"
			+ " absent for exactly one of three reasons, and the call that fails says which:\n\n"
			+ "- this user is not permitted to use it, which is final;\n"
			+ "- it belongs to a skill you have not loaded yet, so load that skill;\n"
			+ "- this agent does not carry it at all, which only an administrator can change.\n\n"
			+ "Never a defect, and never something to search for. Do not name a capability as the"
			+ " reason unless the message you got names one.\n\n");
		if (nothingDenied) {
			// Only when nothing at all was withheld. A denied tool that is ungrouped has no
			// capability name to report, and claiming the user holds everything would be a lie.
			notice.append("This user holds every capability the tools of this agent and its skills"
				+ " require.\n\n");
		} else if (!lacking.isEmpty()) {
			// An unattended run loses capabilities its principal does hold, to the background
			// allowlist. Reporting those as rights the user lacks would send whoever reads it to the
			// wrong configuration - and would tell the agent something false about its user.
			Set<String> unattended = lostToBackgroundCeiling(lacking);
			Set<String> notHeld = new TreeSet<>(lacking);
			notHeld.removeAll(unattended);

			if (!notHeld.isEmpty()) {
				notice.append("This user does **not** hold these capabilities, so the tools that require"
					+ " them are absent:\n\n");
				appendCapabilities(notice, notHeld);
			}
			if (!unattended.isEmpty()) {
				notice.append("This run is **unattended**: nobody is present to approve a tool call, so"
					+ " it is additionally bounded by the deployment's background allowlist. These"
					+ " capabilities are out of reach in this run even though the user holds them, and"
					+ " no retry and no other tool will get around that:\n\n");
				appendCapabilities(notice, unattended);
			}
		}
		notice.append("Loading a skill may still not hand you every tool it mentions. If a call comes"
			+ " back \"Permission denied\", that is final: say plainly which capability the user"
			+ " lacks, and continue with what you can do. Do not retry the call and do not route"
			+ " around it with another tool.\n");
		return notice.toString();
	}

	/**
	 * Which of the given capabilities this run cannot reach because it is
	 * {@link SecurityContext#isBackgroundRun() unattended}, rather than because its principal does not
	 * hold them.
	 *
	 * <p>
	 * Every message that names a missing capability has to tell the two apart, because they call for
	 * different things: the first for an administrator to widen the {@code <background-allowlist>}, the
	 * second for a role grant. {@link #notice} does it for the system prompt, and a refused
	 * {@code spawn_subagent} does it for the tool error - see
	 * {@link com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner#detachedSpawnRefusal}.
	 * </p>
	 *
	 * @param capabilities
	 *        The capabilities to split, typically the ones some tool needs and this run is denied.
	 * @return The subset withheld by the allowlist, in a stable order; empty for an attended run.
	 */
	public Set<String> lostToBackgroundCeiling(Collection<String> capabilities) {
		Set<String> unattended = new TreeSet<>(_security.capabilitiesWithheldInBackground());
		unattended.retainAll(capabilities);
		return unattended;
	}

	/**
	 * Why {@code loadSkill} cannot hand over the named skill, or <code>null</code> if this view has no
	 * honest answer.
	 *
	 * <p>
	 * The {@code loadSkill} counterpart of {@link com.top_logic.tl.ai.core.tools.ToolCatalog}'s
	 * absence reason, and deliberately the same trichotomy in the same vocabulary: a skill can be
	 * missing because the acting user may use nothing in it, because this agent never declared it, or
	 * because there is no such skill. They all looked alike - "not available for this agent" - and the
	 * model was observed filling the gap with the nearest capability it could name: asked for a tool no
	 * agent carries, it reported that the user lacked {@code script}, which was neither true nor
	 * actionable.
	 * </p>
	 *
	 * <p>
	 * A capability is named only where the application really has the skill, this agent really declares
	 * it, and the acting user really is denied its tools. An unknown id is answered with
	 * <code>null</code> so that the caller states the plain "no such skill" - a typo must not disclose
	 * which capabilities the user lacks.
	 * </p>
	 *
	 * <p>
	 * See {@code doc/security/agent-security-architecture.md} section 3.2.7.
	 * </p>
	 *
	 * @param skillId
	 *        The skill the model asked for, known not to be loadable.
	 * @param agentSkillNames
	 *        The skills this agent declares, loadable or not.
	 * @return One or two sentences the model can relay verbatim, or <code>null</code> if there is
	 *         nothing to say beyond "no such skill".
	 */
	public String skillAbsenceReason(String skillId, Collection<String> agentSkillNames) {
		SkillDefinition skill = _graph.skill(skillId);
		if (skill == null) {
			// Not a skill of this application at all: a typo, or a name from a transcript of another
			// deployment. Nothing here may be answered with a capability.
			return null;
		}
		if (!agentSkillNames.contains(skillId)) {
			return "Skill '" + skillId + "' exists in this application but is not carried by this"
				+ " agent - it is not in this agent's skill list. This is not a permission problem and"
				+ " nothing you do here can reach it: an administrator would have to add it to this"
				+ " agent.";
		}
		if (unusableReason(skill) == null) {
			// Declared, usable, and still not loadable - a definition without content, which is logged
			// as a configuration warning where the skill map is built. No capability is involved.
			return null;
		}
		return withheldSkillReason(skillId, skill);
	}

	/**
	 * The reason a declared skill was withheld from this run, naming the capabilities behind it.
	 *
	 * <p>
	 * Two cases, because their remedies are different: the acting user's roles are short of a
	 * capability, which an administrator grants, or the run is unattended and the policy's
	 * {@code <background-allowlist>} is the ceiling, which no role grant changes.
	 * </p>
	 */
	private String withheldSkillReason(String skillId, SkillDefinition skill) {
		Set<String> lacking = lacking(skill.tools());
		StringBuilder reason = new StringBuilder();
		if (withheldByBackgroundCeiling(skill)) {
			reason.append("Skill '").append(skillId).append("' is withheld because this run is"
				+ " unattended: it has no user who could approve a tool call, so only the capabilities"
				+ " on the background allowlist are reachable, and every tool of this skill needs one"
				+ " that is not");
			reason.append(capabilityList(lacking)).append(". This is not about the acting user's roles"
				+ " - the same request in the chat would have this skill - and only a"
				+ " <background-allowlist> entry changes it.");
		} else {
			reason.append("Skill '").append(skillId).append("' is carried by this agent but withheld"
				+ " from the current user: every tool in it requires a capability this user does not"
				+ " hold");
			reason.append(capabilityList(lacking)).append(". Loading it would hand you no tools, so it"
				+ " is not offered; that is final, and only an administrator granting one of those"
				+ " capabilities changes it.");
		}
		reason.append(" ").append(NO_GUESSING);
		return reason.toString();
	}

	/**
	 * The capabilities of a withheld skill, described, or nothing at all for tools that have none.
	 *
	 * <p>
	 * An ungrouped tool has no capability to name and no grant that would reach it - the deployment's
	 * answer is a {@code <tool-groups>} entry - so the sentence ends without a list rather than with an
	 * empty one. {@link #NO_GUESSING} then forbids the model to supply a name of its own.
	 * </p>
	 *
	 * <p>
	 * Inline in a sentence rather than as the bulleted list the notice uses, so each group description
	 * gives up its full stop: the sentence carries on afterwards, and the message is meant to be
	 * relayed to a user as prose.
	 * </p>
	 */
	private static String capabilityList(Set<String> lacking) {
		if (lacking.isEmpty()) {
			return "";
		}
		StringBuilder list = new StringBuilder(": ");
		boolean first = true;
		for (String capability : lacking) {
			if (!first) {
				list.append("; ");
			}
			first = false;
			String described = describeCapability(capability);
			list.append(described.endsWith(".")
				? described.substring(0, described.length() - 1)
				: described);
		}
		return list.toString();
	}

	/**
	 * How the prompt names one capability the principal lacks.
	 *
	 * <p>
	 * With the group's own one-sentence description wherever there is one: named without saying what it
	 * covers, a capability invites the model to guess - {@code agent.authoring} was read out to a user
	 * as being about the data model.
	 * </p>
	 */
	public static String describeCapability(String capability) {
		String description = ToolGroupResources.englishDescription(capability);
		return description == null ? "`" + capability + "`" : "`" + capability + "` - " + description;
	}

	/** Appends one bullet per capability, each with its description where the deployment has one. */
	private static void appendCapabilities(StringBuilder notice, Collection<String> capabilities) {
		for (String capability : capabilities) {
			notice.append("- ").append(describeCapability(capability)).append("\n");
		}
		notice.append("\n");
	}
}
