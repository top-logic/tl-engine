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
 * <li>The baseline grants apply to every user.</li>
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

		if (matchesAny(_policy.getBaseline(), tool, group, true)) {
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
	 * <b>An absent declaration admits nothing.</b> The allowlist is the compensating control for the
	 * approval prompt an unattended run cannot show, so "the administrator said nothing" must not be
	 * the state that allows everything - a policy that enables enforcement and forgets the allowlist
	 * would otherwise hand unattended runs the user's full grant set, auto-approved.
	 * {@link AiToolAccessService} refuses such a policy at boot; this method is the same rule at the
	 * decision point, for the policies that never pass through the service.
	 * </p>
	 *
	 * @param tool
	 *        The tool being checked.
	 * @return Whether the background allowlist admits the tool.
	 */
	public boolean allowedInBackground(ToolIdentity tool) {
		BackgroundAllowlist allowlist = _policy.getBackgroundAllowlist();
		if (allowlist == null) {
			return false;
		}
		if (allowlist.isAll()) {
			// The explicit opt-out: unattended runs are bounded only by the user's own grants.
			return true;
		}
		return matchesAny(allowlist.getGrants(), tool, groupOf(tool), true);
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
