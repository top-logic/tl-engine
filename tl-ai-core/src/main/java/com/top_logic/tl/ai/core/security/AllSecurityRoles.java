/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.func.Function0;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * The security roles an administrator can pick from when writing a {@link ToolAccessPolicy}.
 *
 * <p>
 * Every role of the application, plus every role name the policy in effect already uses. The second
 * half matters because a policy may legitimately name a role that does not exist <em>yet</em> - one an
 * aspect that is currently switched off creates at boot, or one an administrator wrote ahead of the
 * role itself. Offering only the existing roles would let opening the editor and saving it drop such an
 * entry silently, which is the failure mode this layer is written to avoid. A name that is in neither
 * half is what {@code AiToolAccessService} rejects at boot.
 * </p>
 *
 * <p>
 * Roles are listed whatever their scope, because {@link AiToolAccessService#heldRoleNames} reads the
 * roles a person holds on the security root and a module-scoped role can be granted there too -
 * {@code SecurityStructure.user}, which every account holds, is exactly such a role.
 * </p>
 *
 * @see AiToolAccessService#unknownRoles(ToolAccessPolicy, java.util.function.Predicate)
 */
public class AllSecurityRoles extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return union(existingRoleNames(), AiToolAccessService.policy());
	}

	/** The names of the roles the application has, empty while no role store runs. */
	private static Collection<String> existingRoleNames() {
		if (!PersistencyLayer.Module.INSTANCE.isActive()) {
			return List.of();
		}
		List<String> names = new ArrayList<>();
		for (BoundedRole role : BoundedRole.getAll()) {
			names.add(role.getName());
		}
		return names;
	}

	/**
	 * The roles to offer: the ones that exist, and the ones the policy already names.
	 *
	 * @param existingRoles
	 *        The role names the application knows.
	 * @param policy
	 *        The policy in effect, may be <code>null</code> if the service is not running.
	 * @return The option list, alphabetically and without duplicates.
	 */
	static List<String> union(Collection<String> existingRoles, ToolAccessPolicy policy) {
		Set<String> roles = new TreeSet<>();
		for (String name : existingRoles) {
			if (name != null && !name.isBlank()) {
				roles.add(name);
			}
		}
		if (policy != null) {
			roles.addAll(policy.getToolAccess().keySet());
		}
		return new ArrayList<>(roles);
	}

}
