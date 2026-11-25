/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * The candidate <code>&lt;self/&gt;</code> of an {@link AnyOfRelation}: the delegating object
 * decides by the grants of its type and the roles held on it, beside the access parents of the
 * other candidates.
 *
 * <p>
 * The relation leads to no access parent and delegates no operation. It only marks the
 * {@link AnyOfRelation} it is a candidate of as {@link #includesSelf() including} the object
 * itself. The roles held on the object are checked directly, not by a decision on the object, which
 * is the decision being computed.
 * </p>
 *
 * @see SelfAccessParent
 */
public final class SelfRelation implements AccessParentFunction {

	/**
	 * The single instance.
	 */
	public static final SelfRelation INSTANCE = new SelfRelation();

	private SelfRelation() {
		// Singleton.
	}

	/**
	 * @return Always <code>null</code>: the object is no access parent of itself.
	 */
	@Override
	public TLObject resolve(TLObject object) {
		return null;
	}

	/**
	 * @return Always <code>false</code>: the object decides itself.
	 */
	@Override
	public boolean delegates(BoundCommandGroup operation) {
		return false;
	}

	/**
	 * @return No operation: the object decides every operation itself.
	 */
	@Override
	public Set<BoundCommandGroup> getOperations() {
		return Set.of();
	}

	/**
	 * @return Always <code>true</code>.
	 */
	@Override
	public boolean includesSelf() {
		return true;
	}

	/**
	 * @return No type: the relation leads to no access parent.
	 */
	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return Set.of();
	}

	@Override
	public ResKey getLabel() {
		return I18NConstants.ACCESS_PARENT_SELF;
	}

}
