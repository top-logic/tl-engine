/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.explain;

import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * An entry of the {@link SecurityStorage}: a group holds a role on an object for a reason.
 *
 * @param role
 *        The role held.
 * @param object
 *        The object the role is held on. For a role inherited from a role parent, this is the role
 *        parent. <code>null</code> when the object of the entry cannot be resolved (the storage
 *        names an object that no longer exists).
 * @param group
 *        The group holding the role: the representative group of an account, or a group the
 *        account is a member of.
 * @param source
 *        Why the group holds the role.
 * @param rule
 *        The role rule delivering the role, for the source {@link RoleSource#RULE}, otherwise
 *        <code>null</code>.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record RoleAssignment(BoundedRole role, TLObject object, Group group, RoleSource source, RoleProvider rule) {

	/**
	 * The {@link RoleProvider#getConfigId() identifier} the {@link #rule()} is configured with,
	 * <code>null</code> when the role is not delivered by a rule.
	 */
	public String ruleId() {
		return rule == null ? null : rule.getConfigId();
	}

}
