/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * What is wrong with a policy, said in the language of whoever is editing it.
 *
 * <p>
 * Two lists, and the difference between them is the whole point. A <em>refusal</em> is something that
 * would stop the next application start, so an apply carrying one is rejected: nothing this editor can
 * save may take the application down. A <em>warning</em> is a policy that starts and does not do what
 * its author probably meant - those are shown permanently and never block anything.
 * </p>
 */
/*
 * Proposal E. The refusals are the boot checks of AiToolAccessService, asked at authoring time instead of
 * hours later at the next start; SecDemoPolicyEditorTest pins that the two agree, which is the property
 * that matters and the one that could silently rot.
 *
 * The messages are resources rather than the English sentences the service logs. The service writes for
 * an operator reading a log file, in the language every log line of the application is in; this writes
 * for the administrator who is looking at the form right now.
 *
 * The warnings are the "coalesced foot-gun" pattern of the concept's research: one effective sentence per
 * combination that surprises, rather than one innocuous row per capability. They are computed from the
 * whole policy, because every one of them is about two settings at once.
 */
public class PolicyChecks {

	private PolicyChecks() {
		// Static utility.
	}

	/**
	 * Everything about the given policy that would refuse the next application start.
	 *
	 * @param policy
	 *        The policy to check.
	 * @param roleExists
	 *        Whether a security role of the given name exists.
	 * @return One message per problem, in the order an administrator would fix them; empty for a policy
	 *         that starts.
	 *
	 * @see AiToolAccessService#refusals(ToolAccessPolicy, Predicate)
	 */
	public static List<ResKey> refusals(ToolAccessPolicy policy, Predicate<String> roleExists) {
		List<ResKey> result = new ArrayList<>();
		for (String roleName : AiToolAccessService.unknownRoles(policy, roleExists)) {
			result.add(I18NConstants.REFUSE_UNKNOWN_ROLE__ROLE.fill(roleName));
		}
		Baseline baseline = policy.getBaseline();
		if (baseline != null) {
			collectEntryProblems(baseline.getGrants(),
				I18NConstants.WHERE_BASELINE, result);
		}
		BackgroundAllowlist allowlist = policy.getBackgroundAllowlist();
		if (allowlist != null) {
			collectEntryProblems(allowlist.getGrants(),
				I18NConstants.WHERE_BACKGROUND_ALLOWLIST, result);
		}
		for (RoleAccess role : new TreeMap<>(policy.getToolAccess()).values()) {
			collectEntryProblems(role.getGrants(),
				I18NConstants.WHERE_ROLE_GRANTS__ROLE.fill(role.getName()), result);
			collectEntryProblems(role.getDenies(),
				I18NConstants.WHERE_ROLE_DENIES__ROLE.fill(role.getName()), result);
		}
		for (Dimension dimension : Dimension.values()) {
			for (String name : AiToolAccessService.unknownNames(policy, dimension,
				AiToolAccessService.knownNames(dimension))) {
				result.add(I18NConstants.REFUSE_UNKNOWN_NAME__NAME.fill(name));
			}
		}
		if (AiToolAccessService.needsBackgroundAllowlist(policy)) {
			result.add(I18NConstants.REFUSE_BACKGROUND_ALLOWLIST_UNDECLARED);
		}
		return result;
	}

	/**
	 * The entries of one list that do not name exactly one thing.
	 *
	 * @param where
	 *        Names the list, so that the message can say which of several the entry is in.
	 */
	private static void collectEntryProblems(List<Grant> entries, ResKey where, List<ResKey> result) {
		for (int n = 0, size = entries.size(); n < size; n++) {
			Grant entry = entries.get(n);
			boolean namesGroup = entry.getGroup() != null && !entry.getGroup().isBlank();
			boolean namesTools = !entry.getTools().isEmpty();
			if (namesGroup == namesTools) {
				Integer position = Integer.valueOf(n + 1);
				result.add(namesGroup
					? I18NConstants.REFUSE_ENTRY_NAMES_BOTH__WHERE_POSITION.fill(where, position)
					: I18NConstants.REFUSE_ENTRY_NAMES_NOTHING__WHERE_POSITION.fill(where, position));
			}
		}
	}

	/**
	 * What the given policy does that its author probably did not mean.
	 *
	 * @param policy
	 *        The policy to check.
	 * @param rolesWithModelRights
	 *        The roles some Read, Write or Create grant of the model access rights names. Never
	 *        <code>null</code>; empty when there is nothing to compare against, which then produces no
	 *        warning rather than one about every role.
	 * @return One message per finding, in a stable order; empty for a policy with no surprises.
	 */
	public static List<ResKey> warnings(ToolAccessPolicy policy, Set<String> rolesWithModelRights) {
		List<ResKey> result = new ArrayList<>();
		for (Map.Entry<String, RoleAccess> configured : new TreeMap<>(policy.getToolAccess()).entrySet()) {
			warnAboutRole(configured.getKey(), configured.getValue(), rolesWithModelRights, result);
		}
		warnAboutBackgroundLimit(policy, result);
		return result;
	}

	private static void warnAboutRole(String roleName, RoleAccess role,
			Set<String> rolesWithModelRights, List<ResKey> result) {
		Set<String> granted = grantedGroups(role);

		if (granted.contains(ToolGroups.SCRIPT) && granted.contains(ToolGroups.DATA_WRITE)) {
			result.add(I18NConstants.WARN_SCRIPT_WITH_DATA_WRITE__ROLE.fill(roleName));
		} else if (granted.contains(ToolGroups.SCRIPT)) {
			result.add(I18NConstants.WARN_SCRIPT_WITHOUT_DATA_WRITE__ROLE.fill(roleName));
		}
		if (granted.contains(ToolGroups.EGRESS)
			&& (granted.contains(ToolGroups.DATA_READ) || granted.contains(ToolGroups.DATA_WRITE))) {
			result.add(I18NConstants.WARN_DATA_AND_EGRESS__ROLE.fill(roleName));
		}
		if (granted.contains(ToolGroups.AGENT_AUTHORING)) {
			result.add(I18NConstants.WARN_AGENT_AUTHORING__ROLE.fill(roleName));
		}

		Set<String> dataGroups = new TreeSet<>(granted);
		dataGroups.retainAll(Set.of(ToolGroups.DATA_READ, ToolGroups.DATA_WRITE));
		if (!dataGroups.isEmpty() && !rolesWithModelRights.isEmpty()
			&& !rolesWithModelRights.contains(roleName)) {
			result.add(I18NConstants.WARN_DATA_ROLE_WITHOUT_MODEL_RIGHTS__ROLE_GROUPS
				.fill(roleName, String.join(", ", dataGroups)));
		}

		boolean saysAnything = !role.getGrants().isEmpty() || !role.getDenies().isEmpty();
		if (saysAnything && SecurityRoleKind.of(roleName) == SecurityRoleKind.INTERNAL) {
			result.add(I18NConstants.WARN_INTERNAL_ROLE__ROLE.fill(roleName));
		}
		if (!role.getGrants().isEmpty() && SecurityRoleKind.EVERY_ACCOUNT.equals(roleName)) {
			result.add(I18NConstants.WARN_EVERY_ACCOUNT_ROLE__ROLE.fill(roleName));
		}
	}

	private static void warnAboutBackgroundLimit(ToolAccessPolicy policy, List<ResKey> result) {
		BackgroundAllowlist allowlist = policy.getBackgroundAllowlist();
		if (allowlist == null) {
			// Already a refusal; saying it twice would only make the refusal harder to find.
			return;
		}
		if (allowlist.isAll()) {
			result.add(I18NConstants.WARN_BACKGROUND_ALL);
			return;
		}
		Set<String> groups = new TreeSet<>();
		for (Grant entry : allowlist.getGrants()) {
			if (entry.getGroup() != null && !entry.getGroup().isBlank()) {
				groups.add(entry.getGroup());
			}
		}
		if (groups.contains(ToolGroups.SCRIPT)) {
			result.add(groups.contains(ToolGroups.DATA_WRITE)
				? I18NConstants.WARN_BACKGROUND_SCRIPT_AND_DATA_WRITE
				: I18NConstants.WARN_BACKGROUND_SCRIPT);
		}
	}

	/** The capability groups the given role entry grants, ignoring the ones it names tools for. */
	private static Set<String> grantedGroups(RoleAccess role) {
		Set<String> result = new TreeSet<>();
		for (Grant entry : role.getGrants()) {
			if (entry.getGroup() != null && !entry.getGroup().isBlank()) {
				result.add(entry.getGroup());
			}
		}
		return result;
	}

}
