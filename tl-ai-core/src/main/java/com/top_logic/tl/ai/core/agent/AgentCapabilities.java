/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

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
 * "that does not exist" all look alike. This class answers the first of the three, in the two places
 * where the difference is visible to the model - the skill catalog in the system prompt and the tool
 * set a skill hands over when it is loaded.
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

	private final Function<String, Tool> _toolResolver;

	private final SecurityContext _security;

	/**
	 * Creates an {@link AgentCapabilities} view for one run.
	 *
	 * @param toolResolver
	 *        Resolves a qualified tool name to the registered {@link Tool}, or <code>null</code> if
	 *        no tool of that name exists.
	 * @param security
	 *        The run's security context.
	 */
	public AgentCapabilities(Function<String, Tool> toolResolver, SecurityContext security) {
		_toolResolver = toolResolver;
		_security = security;
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
		if (!enforced() || skill.tools().isEmpty()) {
			return null;
		}
		Set<String> requiredCapabilities = new TreeSet<>();
		int denied = 0;
		for (String toolName : skill.tools()) {
			Tool tool = _toolResolver.apply(toolName);
			if (tool == null) {
				// Unresolvable is not a denial - see the javadoc.
				continue;
			}
			if (permits(tool)) {
				return null;
			}
			denied++;
			String capability = _security.toolCapability(tool);
			requiredCapabilities.add(capability == null ? "(ungrouped)" : capability);
		}
		if (denied == 0) {
			return null;
		}
		return "none of its " + denied + " known tools is permitted; they require " + requiredCapabilities;
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
		Set<String> lacking = new TreeSet<>();
		if (!enforced()) {
			return lacking;
		}
		for (String toolName : toolNames) {
			Tool tool = _toolResolver.apply(toolName);
			if (tool == null || permits(tool)) {
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
			+ " either one this user is not permitted to use or one whose skill is not loaded yet -"
			+ " never a defect, and never something to search for.\n\n");
		if (nothingDenied) {
			// Only when nothing at all was withheld. A denied tool that is ungrouped has no
			// capability name to report, and claiming the user holds everything would be a lie.
			notice.append("This user holds every capability the tools of this agent and its skills"
				+ " require.\n\n");
		} else if (!lacking.isEmpty()) {
			notice.append("This user does **not** hold these capabilities, so the tools that require"
				+ " them are absent:\n\n");
			for (String capability : lacking) {
				notice.append("- `").append(capability).append("`");
				// Named without saying what it covers, a capability invites the model to guess -
				// "agent.authoring" was read out to a user as being about the data model.
				String description = ToolGroupResources.englishDescription(capability);
				if (description != null) {
					notice.append(" - ").append(description);
				}
				notice.append("\n");
			}
			notice.append("\n");
		}
		notice.append("Loading a skill may still not hand you every tool it mentions. If a call comes"
			+ " back \"Permission denied\", that is final: say plainly which capability the user"
			+ " lacks, and continue with what you can do. Do not retry the call and do not route"
			+ " around it with another tool.\n");
		return notice.toString();
	}
}
