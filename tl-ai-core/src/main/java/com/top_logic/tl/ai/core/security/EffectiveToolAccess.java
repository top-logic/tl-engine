/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/*
 * PR #306, the query half of the "Agent-Sicherheit" administration view. No issue tracks it; the
 * PR is the record.
 *
 * Why this is a class of its own rather than more methods on ToolAuthorization: that class is bound to
 * a run - it audits every denial it answers, it is carried on the run's security context, and it
 * exists once per run. This one answers about a *hypothesis* ("what would a holder of role X reach in
 * an unattended run"), answers about every capability at once, and must not write a log line while
 * doing so. Sharing the decision order without sharing the object is the point; the order itself lives
 * in one place per layer - RoleToolAccess for the policy, and decide()/decideCapability() below for
 * the two run properties, which mirror ToolAuthorization.decide() line for line.
 */

/**
 * What one account, or one hypothetical role set, may reach under the policy in effect - and why.
 *
 * <p>
 * The read-only counterpart of {@link ToolAuthorization}: same rules, same order, no side effect and
 * no message. Every answer is a {@link ToolAccessExplanation} carrying the rule that decided and the
 * rule it overrode, which is what lets an administration view print a sentence instead of a verdict.
 * </p>
 *
 * <p>
 * Three things beyond the policy decide, and all three are properties of the <em>run</em> rather than
 * of the configuration - which is why they are composed here and not in {@link RoleToolAccess}:
 * </p>
 * <ol>
 * <li>the {@code <background-allowlist>} ceiling of a run with nobody to answer an approval prompt,
 * which binds administrators too,</li>
 * <li>the administrator bypass,</li>
 * <li>which roles the account holds on the security root.</li>
 * </ol>
 *
 * @see AiToolAccessService#effectiveAccessFor(Person, boolean)
 */
public class EffectiveToolAccess {

	/**
	 * One capability group of the policy, with the verdict for this account and the tools behind it.
	 *
	 * @param group
	 *        The technical group name.
	 * @param explanation
	 *        The decision for the group as a whole.
	 * @param tools
	 *        The registered tools this group classifies, in a stable order; empty for a group that is
	 *        known to the policy but that no registered tool belongs to.
	 */
	public record CapabilityAccess(String group, ToolAccessExplanation explanation, List<Tool> tools) {

		/** Whether this group's tools are reachable. */
		public boolean isAllowed() {
			return explanation().allowed();
		}
	}

	private final RoleToolAccess _access;

	private final Set<String> _roleNames;

	private final boolean _admin;

	private final boolean _background;

	private final ToolAuthorization.Identities _identities;

	/**
	 * Creates an {@link EffectiveToolAccess}.
	 *
	 * @param access
	 *        The policy evaluation to ask. Never <code>null</code>.
	 * @param roleNames
	 *        The roles held on the security root - the real ones of an account, or the hypothetical
	 *        ones an administrator selected. Never <code>null</code>; may be empty.
	 * @param admin
	 *        Whether the subject is an administrator and therefore bypasses this layer.
	 * @param background
	 *        Whether the run being asked about has nobody to answer an approval prompt.
	 * @param identities
	 *        Resolves the name the policy knows a {@link Tool} by; typically
	 *        {@link AiToolAccessService#identities()}.
	 */
	public EffectiveToolAccess(RoleToolAccess access, Collection<String> roleNames, boolean admin,
			boolean background, ToolAuthorization.Identities identities) {
		_access = access;
		_roleNames = Set.copyOf(roleNames);
		_admin = admin;
		_background = background;
		_identities = identities == null ? ToolAuthorization.Identities.DECLARED_ONLY : identities;
	}

	/** The roles the subject holds on the security root. */
	public Set<String> roleNames() {
		return _roleNames;
	}

	/** Whether the subject is an administrator and therefore bypasses this layer. */
	public boolean isAdmin() {
		return _admin;
	}

	/** Whether the run being asked about has nobody to answer an approval prompt. */
	public boolean isBackground() {
		return _background;
	}

	/**
	 * Whether the subject may call the given tool, and why.
	 *
	 * @param tool
	 *        The tool to decide about.
	 * @return The decision and its provenance, never <code>null</code>.
	 */
	public ToolAccessExplanation explain(Tool tool) {
		return explain(_identities.identify(tool));
	}

	/**
	 * Whether the subject may call the tool of the given qualified name, and why.
	 *
	 * @param qualifiedToolName
	 *        The name an administrator writes in the policy.
	 */
	public ToolAccessExplanation explain(String qualifiedToolName) {
		return explain(identify(qualifiedToolName));
	}

	/**
	 * Whether the subject may call the given tool, and why.
	 *
	 * <p>
	 * The same three rules {@code ToolAuthorization.decide(ToolIdentity)} applies: the policy, the
	 * administrator bypass, and the background allowlist - which binds administrators too, because its
	 * justification is the missing approval prompt and not the principal's authority.
	 * </p>
	 *
	 * @param tool
	 *        The tool to decide about.
	 * @return The decision and its provenance, never <code>null</code>.
	 */
	public ToolAccessExplanation explain(ToolIdentity tool) {
		ToolAccessExplanation attended = attended(tool);
		if (_background && !_access.allowedInBackground(tool)) {
			return capped(attended, _access.groupOf(tool));
		}
		return attended;
	}

	/** The decision an attended run of the same subject would get. */
	private ToolAccessExplanation attended(ToolIdentity tool) {
		ToolAccessExplanation policy = _access.explain(_roleNames, tool);
		if (_admin) {
			return ToolAccessExplanation.allowedBy(ToolAccessRule.adminBypass(),
				policyDenial(policy));
		}
		return policy;
	}

	/**
	 * The decision of a run the background allowlist excludes.
	 *
	 * <p>
	 * Attributed to the ceiling only when the ceiling is what took something away. A capability no
	 * role grants is denied in both modes, and blaming the allowlist for it would send an
	 * administrator to the {@code <background-allowlist>} to fix a missing {@code <grant>} - the same
	 * wrong instruction {@code ToolAuthorization.withheldByBackgroundCeiling} exists to prevent.
	 * </p>
	 *
	 * @param attended
	 *        What the same subject would get in an attended run.
	 * @param group
	 *        The capability the ceiling is about, may be <code>null</code>.
	 */
	private static ToolAccessExplanation capped(ToolAccessExplanation attended, String group) {
		if (!attended.allowed()) {
			return attended;
		}
		return ToolAccessExplanation.deniedBy(ToolAccessRule.backgroundCeiling(group),
			attended.decidedBy());
	}

	/**
	 * Whether the subject may use the given capability group, and why.
	 *
	 * <p>
	 * Asks about the group as the unit an administrator grants: "are the tools of this group
	 * reachable". That is the tool-level rule applied to a whole group, so it honours
	 * {@link RoleToolAccess#IMPLICIT_BASELINE} - which
	 * {@link RoleToolAccess#explainCapability(Collection, String)} deliberately does not, because it
	 * answers the narrower question of what an <em>operation</em> costs.
	 * </p>
	 *
	 * @param group
	 *        The technical group name.
	 * @return The decision and its provenance, never <code>null</code>.
	 */
	public ToolAccessExplanation explainCapability(String group) {
		if (RoleToolAccess.IMPLICIT_BASELINE.contains(group)) {
			// Not rationed by the allowlist and not something an administrator has to hold: see
			// RoleToolAccess.IMPLICIT_BASELINE and RoleToolAccess.allowedInBackground.
			return ToolAccessExplanation.allowedBy(ToolAccessRule.implicitBaseline(group));
		}
		ToolAccessExplanation attended = attendedCapability(group);
		if (_background && !_access.capabilityAllowedInBackground(group)) {
			return capped(attended, group);
		}
		return attended;
	}

	/** The capability decision an attended run of the same subject would get. */
	private ToolAccessExplanation attendedCapability(String group) {
		ToolAccessExplanation policy = _access.explainCapability(_roleNames, group);
		if (_admin) {
			return ToolAccessExplanation.allowedBy(ToolAccessRule.adminBypass(),
				policyDenial(policy));
		}
		return policy;
	}

	/**
	 * The rule the administrator bypass overrode, or <code>null</code> if the policy would have
	 * allowed the tool anyway.
	 *
	 * <p>
	 * Named on purpose: "allowed - administrator" reads like a statement about the policy, and it is
	 * not one. An administrator sees every tool whatever the matrix says, so a view that does not add
	 * "although no role grants it" invites the reader to conclude their policy works.
	 * </p>
	 */
	private static ToolAccessRule policyDenial(ToolAccessExplanation policy) {
		return policy.allowed() ? null : policy.decidedBy();
	}

	/**
	 * One row per capability group the policy could decide about, with the tools behind it.
	 *
	 * <p>
	 * The candidate set is {@link RoleToolAccess#knownCapabilities()} - the groups the framework ships
	 * plus the ones the policy assigns tools to - which is deliberately a list of what is
	 * <em>known</em> rather than of what is legal. A group only a tool declares is added here from the
	 * registry, because unlike the policy the registry can be enumerated.
	 * </p>
	 *
	 * @return The rows, ordered by {@link #capabilityOrder()}.
	 */
	public List<CapabilityAccess> capabilities() {
		Set<String> groups = new TreeSet<>(_access.knownCapabilities());
		for (Tool tool : registeredTools()) {
			String group = _access.groupOf(_identities.identify(tool));
			if (group != null) {
				groups.add(group);
			}
		}

		List<CapabilityAccess> result = new ArrayList<>();
		for (String group : capabilityOrder()) {
			if (groups.remove(group)) {
				result.add(row(group));
			}
		}
		// Whatever an application, an MCP server or a <tool-groups> entry contributed, alphabetically:
		// the fixed order above cannot know about it.
		for (String group : groups) {
			result.add(row(group));
		}
		return result;
	}

	private CapabilityAccess row(String group) {
		return new CapabilityAccess(group, explainCapability(group), toolsOf(group));
	}

	/**
	 * The order the capability groups are presented in: from what every agent has, through reading and
	 * writing data, to the privileged groups.
	 *
	 * <p>
	 * Alphabetical order would put {@code agent.authoring} - the group that lets an agent grant itself
	 * every other one - first, and {@code interaction} in the middle of the write groups. A fixed order
	 * is what makes two accounts comparable at a glance; groups the framework does not ship follow, in
	 * alphabetical order.
	 * </p>
	 */
	public static List<String> capabilityOrder() {
		return List.of(
			ToolGroups.INTERACTION,
			ToolGroups.INTROSPECTION,
			ToolGroups.MODEL_READ,
			ToolGroups.DATA_READ,
			ToolGroups.DATA_WRITE,
			ToolGroups.AGENT_META,
			ToolGroups.WORKSPACE,
			ToolGroups.SCRIPT,
			ToolGroups.MODEL_STRUCTURE,
			ToolGroups.AGENT_AUTHORING,
			ToolGroups.EGRESS);
	}

	/**
	 * The registered tools the policy classifies into the given group, in a stable order.
	 *
	 * @param group
	 *        The technical group name.
	 */
	public List<Tool> toolsOf(String group) {
		List<Tool> result = new ArrayList<>();
		for (Tool tool : registeredTools()) {
			if (group.equals(_access.groupOf(_identities.identify(tool)))) {
				result.add(tool);
			}
		}
		result.sort(Comparator.comparing(this::qualifiedName));
		return result;
	}

	/**
	 * The registered tools that belong to no capability group at all.
	 *
	 * <p>
	 * Their own row in an effective-permissions view, because they are denied to everyone by a
	 * mechanism no capability grant can change: an administrator has to classify them or grant them by
	 * name. An administrator still reaches them, like everything else.
	 * </p>
	 *
	 * @return The tools, in a stable order.
	 */
	public List<Tool> unclassifiedTools() {
		List<Tool> result = new ArrayList<>();
		for (Tool tool : registeredTools()) {
			if (_access.groupOf(_identities.identify(tool)) == null) {
				result.add(tool);
			}
		}
		result.sort(Comparator.comparing(this::qualifiedName));
		return result;
	}

	/** The name the policy knows the given tool by. */
	public String qualifiedName(Tool tool) {
		return _identities.identify(tool).displayName();
	}

	/**
	 * The identity of the tool of the given qualified name, resolved through the registry if it is
	 * there.
	 *
	 * <p>
	 * A name the registry does not know is ungrouped by construction, which is the fail-closed reading
	 * - and the honest answer for a tool an administrator wrote a policy line for before creating it.
	 * </p>
	 */
	private ToolIdentity identify(String qualifiedToolName) {
		Tool tool = registry() == null ? null : registry().getTool(qualifiedToolName);
		if (tool == null) {
			return ToolIdentity.of(qualifiedToolName);
		}
		return _identities.identify(tool);
	}

	/** Every registered tool, or nothing while no registry runs. */
	private Collection<Tool> registeredTools() {
		return registry() == null ? List.of() : registry().getTools();
	}

	private static ToolRegistryService registry() {
		return ToolRegistryService.Module.INSTANCE.isActive() ? ToolRegistryService.getInstance() : null;
	}

}
