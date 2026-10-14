/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;

/**
 * A persistent {@link TLObject} that is a {@link BoundObject}.
 * 
 * <p>
 * The roles of a user on such an object are the {@link BoundedRole}s assigned to the user (or a
 * group of the user) on the object. Its security parents are resolved through the
 * {@link AccessManager}.
 * </p>
 */
public interface BoundWrapper extends Wrapper, BoundObject {

	@Override
	default TLID getID() {
		return KBUtils.getWrappedObjectName(this);
	}

	@Override
	default Set<BoundRole> getRoles(Person aPerson) {
		return BoundedRole.getRoles(this, aPerson == null ? null : aPerson.getRepresentativeGroup());
	}

	@Override
	default Set<BoundRole> getRoles(Group aGroup) {
		return BoundedRole.getRoles(this, aGroup);
	}

	@Override
	default Set<BoundRole> getLocalAndGlobalRoles(Person aPerson) {
		return BoundedRole.getLocalAndGlobalRoles(this, aPerson);
	}

	@Override
	default Set<BoundRole> getLocalAndGlobalAndGroupRoles(Person aPerson) {
		return BoundedRole.getLocalAndGlobalAndGroupRoles(this, aPerson);
	}

	@Override
	default boolean hasAnyRole(Person aPerson) {
		return BoundedRole.hasLocalOrGlobalOrGroupRole(this, aPerson);
	}

	@Override
	default Set<BoundRole> getRoles() {
		return BoundedRole.getRoles(this, null);
	}

	@Override
	default Collection<? extends BoundObject> getSecurityChildren() {
		return Collections.emptyList();
	}

	@Override
	default BoundObject getSecurityParent() {
		throw new UnsupportedOperationException("Call getSecurityParents()");
	}

	/**
	 * The security parents configured for the type of this object.
	 * 
	 * <p>
	 * The security-parent rules of the {@link AccessManager} define the parent set. An object
	 * whose type has no security-parent rules has no security parent. The global security root
	 * is a parent only if a rule names it explicitly (the "singleton" security-parent path
	 * element).
	 * </p>
	 */
	@Override
	default Collection<? extends BoundObject> getSecurityParents() {
		return AccessManager.getInstance().getSecurityParents(this);
	}

}
