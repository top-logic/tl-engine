/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.explain;

import java.util.Set;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * The decision whether an account may perform an operation on an object.
 *
 * @param operation
 *        The operation, one that applies to the object.
 * @param checkedOperation
 *        The operation checked on the {@link #roleHolder() role holder} in place of the
 *        {@link #operation()}, see
 *        {@link ModelAccessRights#getOperationOnRoleHolder(TLObject, BoundCommandGroup)}. The
 *        operation itself for an object deciding the operation for itself.
 * @param roleHolder
 *        The object whose roles decide the operation, see
 *        {@link ModelAccessRights#getRoleHolder(TLObject, BoundCommandGroup)}. It is the
 *        {@link AccessExplanation#decidingObject() deciding object} of the explanation, unless the
 *        access parent of the object is restricted to some operations. <code>null</code> when the
 *        chain of access parents leads nowhere or runs in a cycle.
 * @param allowed
 *        The decision of the access check
 *        {@link ModelAccessRights#isAllowed(Person, TLObject, BoundCommandGroup)},
 *        made for the account.
 * @param admittingRoles
 *        The roles admitting the operation when held on the {@link #roleHolder() role holder}, see
 *        {@link ModelAccessRights#getAdmittingRoles(TLObject, BoundCommandGroup)}.
 * @param grantingRoles
 *        Those of the {@link #admittingRoles()} the account holds on the {@link #roleHolder() role
 *        holder}. An
 *        operation allowed without such a role is allowed by a bypass of the access check (an
 *        administrator, a type without security).
 * @param restricted
 *        Whether the account must not perform the operation at all, because it is a
 *        {@link Person#isRestrictedUser() restricted user} and the operation modifies or deletes
 *        (see {@link SimpleBoundCommandGroup#isAllowedCommandGroup(Person, BoundCommandGroup)}).
 *        Such an operation is denied whatever roles the account holds.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record OperationDecision(BoundCommandGroup operation, BoundCommandGroup checkedOperation,
		TLObject roleHolder, boolean allowed, Set<BoundedRole> admittingRoles, Set<BoundedRole> grantingRoles,
		boolean restricted) {
	// Pure value.
}
