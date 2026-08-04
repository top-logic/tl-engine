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
			notice.append("This user does **not** hold these capabilities, so the tools that require"
				+ " them are absent:\n\n");
			for (String capability : lacking) {
				notice.append("- ").append(describeCapability(capability)).append("\n");
			}
			notice.append("\n");
		}
		notice.append("Loading a skill may still not hand you every tool it mentions. If a call comes"
			+ " back \"Permission denied\", that is final: say plainly which capability the user"
			+ " lacks, and continue with what you can do. Do not retry the call and do not route"
			+ " around it with another tool.\n");
		return notice.toString();
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
	static String describeCapability(String capability) {
		String description = ToolGroupResources.englishDescription(capability);
		return description == null ? "`" + capability + "`" : "`" + capability + "` - " + description;
	}
}
