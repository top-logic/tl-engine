/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.explain;

import java.util.List;
import java.util.Objects;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Explanation of the access an account has to an object, see
 * {@link EffectiveAccess#explain(Person, TLObject)}.
 *
 * @param person
 *        The account the explanation is about.
 * @param object
 *        The object the explanation is about.
 * @param administrator
 *        Whether the account is an administrator, who may perform every operation.
 * @param restrictedUser
 *        Whether the account is a {@link Person#isRestrictedUser() restricted user}, who must not
 *        perform any operation that modifies or deletes, see
 *        {@link OperationDecision#restricted()}.
 * @param withoutSecurity
 *        Whether the type of the object is marked
 *        {@link ModelAccessRights#isWithoutSecurity(TLClass) without security}, so that every
 *        account may perform every operation on it.
 * @param internal
 *        Whether the type of the object is marked {@link ModelAccessRights#isInternal(TLClass)
 *        internal}: it is not meant to be accessed on behalf of an account.
 * @param decidingObject
 *        The object whose roles decide: the end of the chain of access parents of the object, or
 *        the object itself when its type decides for itself. <code>null</code> when the chain of
 *        access parents leads nowhere or runs in a cycle.
 * @param roleParents
 *        The role parents of the {@link #decidingObject()}, collected transitively, whose roles the
 *        account holds on the deciding object as well.
 * @param operations
 *        The decisions for each operation that applies to the object.
 * @param heldRoles
 *        The roles the account holds on the {@link #decidingObject()}, as the access manager
 *        reports them.
 * @param sourcesAvailable
 *        Whether the {@link HeldRole#sources() sources} of the held roles could be determined. They
 *        are taken from the security storage of the access manager; an access manager without such
 *        a storage, or a storage being rebuilt, delivers decisions and held roles only.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record AccessExplanation(Person person, TLObject object, boolean administrator, boolean restrictedUser,
		boolean withoutSecurity,
		boolean internal, TLObject decidingObject, List<BoundObject> roleParents, List<OperationDecision> operations,
		List<HeldRole> heldRoles, boolean sourcesAvailable) {

	/**
	 * Whether the object delegates the decision to an access parent, see {@link #decidingObject()}.
	 */
	public boolean isDelegating() {
		return !Objects.equals(decidingObject, object);
	}

	/**
	 * Whether the given entry gives the account a role inherited from a role parent, not a role on
	 * the {@link #decidingObject()} itself.
	 */
	public boolean isInherited(RoleAssignment assignment) {
		return !Objects.equals(assignment.object(), decidingObject);
	}

	/**
	 * The decision for the given operation, <code>null</code> when the operation does not apply to
	 * the object.
	 */
	public OperationDecision getDecision(BoundCommandGroup operation) {
		for (OperationDecision decision : operations) {
			if (decision.operation().equals(operation)) {
				return decision;
			}
		}
		return null;
	}

}
