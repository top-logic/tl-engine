/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ToolRef;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Decides whether the roles a user holds grant them a given tool.
 *
 * <p>
 * Evaluates a {@link ToolAccessPolicy} against a set of held role names. This is the role-based
 * half of the authorization; the other half - whether the user may touch the data the tool reaches
 * - is {@link ModelSecurity}. A tool call is permitted only when both allow.
 * </p>
 *
 * <p>
 * The decision, in order:
 * </p>
 * <ol>
 * <li>An explicit deny from <em>any</em> held role rejects the tool outright.</li>
 * <li>{@link #IMPLICIT_BASELINE} is granted to every user without being configured.</li>
 * <li>The configured baseline grants apply to every user.</li>
 * <li>The grants of all held roles are unioned.</li>
 * <li>Anything not reached by the above is denied.</li>
 * </ol>
 *
 * <p>
 * This class is pure policy evaluation: it neither reads the {@code KnowledgeBase} nor resolves
 * roles. The caller supplies the role names, which keeps the decision unit-testable and keeps the
 * question "which roles does this person hold on the security root" in one place.
 * </p>
 *
 * @see ToolAccessPolicy
 * @see ToolGroups
 */
public class RoleToolAccess {

	/**
	 * Resolves the group a tool declared for itself.
	 *
	 * <p>
	 * Java tools declare their group on {@code @ToolMethod}; this abstraction keeps
	 * {@link RoleToolAccess} independent of how that declaration is read, and lets tests supply a
	 * plain map. Policy-configured group membership ({@link ToolAccessPolicy#getToolGroups()}) takes
	 * precedence over the declared group.
	 * </p>
	 */
	@FunctionalInterface
	public interface DeclaredGroups {

		/** Never resolves a group; every tool relies on the policy's explicit assignment. */
		DeclaredGroups NONE = toolName -> null;

		/**
		 * The group the given tool declares for itself.
		 *
		 * @param qualifiedToolName
		 *        The qualified tool name.
		 * @return The declared group, or <code>null</code> if the tool declares none.
		 */
		String groupOf(String qualifiedToolName);
	}

	/**
	 * The outcome of a {@link RoleToolAccess#check(Collection, String) check}.
	 */
	public sealed interface Decision {

		/**
		 * The tool is granted.
		 *
		 * @param byRole
		 *        The role that granted it, or {@code "(baseline)"} for a baseline grant.
		 * @param viaGroup
		 *        The group through which it was granted, or <code>null</code> for a per-tool grant.
		 */
		record Granted(String byRole, String viaGroup) implements Decision {
			// Intentionally empty.
		}

		/**
		 * The tool is denied.
		 *
		 * @param reason
		 *        A message safe to feed back to the model: it names the missing capability, never
		 *        the data that was withheld.
		 */
		record Denied(String reason) implements Decision {
			// Intentionally empty.
		}
	}

	private static final String BASELINE_ROLE = "(baseline)";

	/**
	 * The capability every policy grants, whether or not it says so.
	 *
	 * <p>
	 * {@link ToolGroups#INTERACTION} is the one group no administrator has to configure: an agent that
	 * cannot ask a question is broken, and a forgotten {@code <baseline>} must not be able to produce
	 * that state. It therefore reaches every role - including a role that appears with an empty grant
	 * list, and a user with no role at all - and it reaches unattended runs too, so that the same
	 * omission cannot silence a background run either.
	 * </p>
	 *
	 * <p>
	 * An explicit deny still wins, because denies are evaluated first: an administrator who takes the
	 * capability away from a role has said so out loud, which is a different thing from never having
	 * mentioned it. Declaring it in {@code <baseline>} stays legal and is a no-op.
	 * </p>
	 */
	public static final String IMPLICIT_BASELINE = ToolGroups.INTERACTION;

	private final ToolAccessPolicy _policy;

	private final DeclaredGroups _declaredGroups;

	/** Policy-configured group membership: exact tool name or {@code prefix*} pattern to group. */
	private final Map<String, String> _configuredGroups;

	/**
	 * Creates a {@link RoleToolAccess} for the given policy.
	 *
	 * @param policy
	 *        The administrator-authored policy to evaluate.
	 * @param declaredGroups
	 *        Resolves the group a tool declares for itself; use {@link DeclaredGroups#NONE} when
	 *        only the policy assigns groups.
	 */
	public RoleToolAccess(ToolAccessPolicy policy, DeclaredGroups declaredGroups) {
		_policy = policy;
		_declaredGroups = declaredGroups;
		_configuredGroups = indexConfiguredGroups(policy);
	}

	private static Map<String, String> indexConfiguredGroups(ToolAccessPolicy policy) {
		Map<String, String> result = new HashMap<>();
		for (Map.Entry<String, GroupMembership> entry : policy.getToolGroups().entrySet()) {
			for (ToolRef tool : entry.getValue().getTools().values()) {
				result.put(tool.getName(), entry.getKey());
			}
		}
		return result;
	}

	/**
	 * The group the given tool belongs to.
	 *
	 * <p>
	 * The policy's explicit assignment wins over the tool's own declaration, so an administrator can
	 * reclassify a tool (typically into {@link ToolGroups#EGRESS}) without touching its code.
	 * </p>
	 *
	 * @param qualifiedToolName
	 *        The qualified tool name.
	 * @return The group name, or <code>null</code> if the tool is ungrouped - in which case only an
	 *         explicit per-tool grant can reach it.
	 */
	public String groupOf(String qualifiedToolName) {
		return groupOf(new ToolIdentity(qualifiedToolName, null,
			_declaredGroups.groupOf(qualifiedToolName)));
	}

	/**
	 * The group the given tool belongs to.
	 *
	 * <p>
	 * The policy's explicit assignment wins over the tool's own declaration, so an administrator can
	 * reclassify a tool (typically into {@link ToolGroups#EGRESS}) without touching its code.
	 * </p>
	 *
	 * @param tool
	 *        The tool to classify.
	 * @return The group name, or <code>null</code> if the tool is ungrouped - in which case only an
	 *         explicit per-tool grant can reach it.
	 */
	public String groupOf(ToolIdentity tool) {
		for (String name : tool.names()) {
			String configured = matchConfiguredGroup(name);
			if (configured != null) {
				return configured;
			}
		}
		return tool.declaredGroup();
	}

	private String matchConfiguredGroup(String toolName) {
		String exact = _configuredGroups.get(toolName);
		if (exact != null) {
			return exact;
		}
		for (Map.Entry<String, String> entry : _configuredGroups.entrySet()) {
			if (matches(entry.getKey(), toolName)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Whether the roles held by a user grant them the given tool.
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>;
	 *        may be empty, in which case only the baseline applies.
	 * @param qualifiedToolName
	 *        The qualified name of the tool being checked; its group is resolved through the
	 *        {@link DeclaredGroups} this instance was created with.
	 * @return The decision, carrying the granting role or the denial reason.
	 */
	public Decision check(Collection<String> heldRoleNames, String qualifiedToolName) {
		return check(heldRoleNames, new ToolIdentity(qualifiedToolName, null,
			_declaredGroups.groupOf(qualifiedToolName)));
	}

	/**
	 * Whether the roles held by a user grant them the given tool.
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>;
	 *        may be empty, in which case only the baseline applies.
	 * @param tool
	 *        The tool being checked.
	 * @return The decision, carrying the granting role or the denial reason.
	 */
	public Decision check(Collection<String> heldRoleNames, ToolIdentity tool) {
		String group = groupOf(tool);
		String label = tool.displayName();

		for (String roleName : heldRoleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role != null && matchesAny(role.getDenies(), tool, group, false)) {
				return new Decision.Denied("Permission denied: tool '" + label
					+ "' is explicitly denied to your role '" + roleName + "'.");
			}
		}

		if (isImplicitBaseline(group) || matchesAny(_policy.getBaseline(), tool, group, true)) {
			return new Decision.Granted(BASELINE_ROLE, group);
		}

		for (String roleName : heldRoleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role != null && matchesAny(role.getGrants(), tool, group, true)) {
				return new Decision.Granted(roleName, group);
			}
		}

		if (group == null) {
			return new Decision.Denied("Permission denied: tool '" + label
				+ "' belongs to no tool group and has no explicit grant, so it is denied to everyone."
				+ " An administrator must assign it a group or grant it by name.");
		}
		return new Decision.Denied("Permission denied: tool '" + label
			+ "' requires the '" + group + "' capability, which none of your roles grants.");
	}

	/**
	 * Whether the roles held by a user grant them a capability as such, independently of any tool.
	 *
	 * <p>
	 * For an operation that is not a tool of its own but costs a capability anyway: executing
	 * TL-Script with {@code withCommit=true} persists arbitrary writes, so it requires
	 * {@link ToolGroups#DATA_WRITE} in addition to the {@link ToolGroups#SCRIPT} the tool itself is
	 * classified as. The argument that decides is known only inside the tool, which is why this
	 * question is asked separately from {@link #check(Collection, ToolIdentity)} and answered by the
	 * same rules.
	 * </p>
	 *
	 * <p>
	 * Only {@code <grant group="...">} entries are consulted, never per-tool grants: the question is
	 * about the capability itself, and a grant of one tool says nothing about a second capability that
	 * tool can reach through an argument.
	 * </p>
	 *
	 * @param heldRoleNames
	 *        The names of the roles the user holds on the security root. Never <code>null</code>;
	 *        may be empty, in which case only the baseline applies.
	 * @param capability
	 *        The name of the required capability (tool group).
	 * @param operation
	 *        What the caller is about to do, for the denial message - e.g.
	 *        {@code "executing TL-Script with withCommit=true"}.
	 * @return The decision, carrying the granting role or the denial reason.
	 */
	public Decision checkCapability(Collection<String> heldRoleNames, String capability, String operation) {
		for (String roleName : heldRoleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role != null && grantsGroup(role.getDenies(), capability)) {
				return new Decision.Denied("Permission denied: " + operation + " requires the '"
					+ capability + "' capability, which is explicitly denied to your role '"
					+ roleName + "'.");
			}
		}

		if (grantsGroup(_policy.getBaseline(), capability)) {
			return new Decision.Granted(BASELINE_ROLE, capability);
		}

		for (String roleName : heldRoleNames) {
			RoleAccess role = _policy.getToolAccess().get(roleName);
			if (role != null && grantsGroup(role.getGrants(), capability)) {
				return new Decision.Granted(roleName, capability);
			}
		}

		return new Decision.Denied("Permission denied: " + operation + " requires the '" + capability
			+ "' capability, which none of your roles grants.");
	}

	/**
	 * Whether a capability as such is reachable in a run that has no human to answer approval
	 * prompts.
	 *
	 * <p>
	 * The counterpart of {@link #allowedInBackground(ToolIdentity)} for
	 * {@link #checkCapability(Collection, String, String)}, and applied in addition to it.
	 * </p>
	 *
	 * @param capability
	 *        The name of the required capability (tool group).
	 * @return Whether the background allowlist admits the capability.
	 */
	public boolean capabilityAllowedInBackground(String capability) {
		BackgroundAllowlist allowlist = _policy.getBackgroundAllowlist();
		if (allowlist == null) {
			// An absent declaration admits nothing - see allowedInBackground(ToolIdentity).
			return false;
		}
		if (allowlist.isAll()) {
			return true;
		}
		return grantsGroup(allowlist.getGrants(), capability);
	}

	/**
	 * Whether any entry of {@code entries} names the given group.
	 */
	private static boolean grantsGroup(Collection<Grant> entries, String group) {
		for (Grant entry : entries) {
			if (!isBlank(entry.getGroup()) && entry.getGroup().equals(group)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given tool is reachable in a run that has no human to answer approval prompts.
	 *
	 * <p>
	 * Applied <em>in addition</em> to {@link #check(Collection, ToolIdentity)}: a background run gets
	 * the intersection of the user's grants and the allowlist.
	 * </p>
	 *
	 * @param qualifiedToolName
	 *        The qualified name of the tool being checked.
	 * @return Whether the background allowlist admits the tool.
	 */
	public boolean allowedInBackground(String qualifiedToolName) {
		return allowedInBackground(new ToolIdentity(qualifiedToolName, null,
			_declaredGroups.groupOf(qualifiedToolName)));
	}

	/**
	 * Whether the given tool is reachable in a run that has no human to answer approval prompts.
	 *
	 * <p>
	 * Applied <em>in addition</em> to {@link #check(Collection, ToolIdentity)}: a background run gets
	 * the intersection of the user's grants and the allowlist.
	 * </p>
	 *
	 * <p>
	 * <b>An absent declaration admits nothing beyond {@link #IMPLICIT_BASELINE}.</b> The allowlist is
	 * the compensating control for the approval prompt an unattended run cannot show, so "the
	 * administrator said nothing" must not be the state that allows everything - a policy that enables
	 * enforcement and forgets the allowlist would otherwise hand unattended runs the user's full grant
	 * set, auto-approved. {@link AiToolAccessService} refuses such a policy at boot; this method is the
	 * same rule at the decision point, for the policies that never pass through the service.
	 * </p>
	 *
	 * @param tool
	 *        The tool being checked.
	 * @return Whether the background allowlist admits the tool.
	 */
	public boolean allowedInBackground(ToolIdentity tool) {
		String group = groupOf(tool);
		if (isImplicitBaseline(group)) {
			// The implicit grant is not a capability an allowlist rations: it exists so that no
			// omission can leave a run unable to talk to anyone, and an unattended run is exactly
			// the kind of run an omission is easy to make about.
			return true;
		}
		BackgroundAllowlist allowlist = _policy.getBackgroundAllowlist();
		if (allowlist == null) {
			return false;
		}
		if (allowlist.isAll()) {
			// The explicit opt-out: unattended runs are bounded only by the user's own grants.
			return true;
		}
		return matchesAny(allowlist.getGrants(), tool, group, true);
	}

	/**
	 * Whether the given group is granted to everyone without being configured.
	 *
	 * @param group
	 *        The group a tool belongs to, may be <code>null</code>.
	 * @return Whether {@link #IMPLICIT_BASELINE} covers it.
	 */
	private static boolean isImplicitBaseline(String group) {
		return IMPLICIT_BASELINE.equals(group);
	}

	/**
	 * Whether any entry of {@code entries} covers the given tool.
	 *
	 * @param forGrant
	 *        Whether the entries are grants. A wildcard tool pattern in a grant never reaches a
	 *        {@link ToolGroups#PRIVILEGED privileged} group; in a deny it may, since a deny can only
	 *        narrow access.
	 */
	private static boolean matchesAny(Collection<Grant> entries, ToolIdentity tool, String group,
			boolean forGrant) {
		boolean wildcardIntoPrivileged = forGrant && ToolGroups.isPrivileged(group);
		for (Grant entry : entries) {
			if (!isBlank(entry.getGroup()) && entry.getGroup().equals(group)) {
				return true;
			}
			String toolPattern = entry.getTool();
			if (isBlank(toolPattern)) {
				continue;
			}
			for (String name : tool.names()) {
				if (toolPattern.equals(name)) {
					return true;
				}
				if (!wildcardIntoPrivileged && matches(toolPattern, name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Whether {@code pattern} - an exact name or a {@code prefix*} wildcard - covers {@code name}.
	 */
	private static boolean matches(String pattern, String name) {
		if (pattern.endsWith("*")) {
			return name.startsWith(pattern.substring(0, pattern.length() - 1));
		}
		return pattern.equals(name);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
