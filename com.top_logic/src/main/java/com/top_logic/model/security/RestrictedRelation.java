/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link AccessParentFunction} delegating only some operations through another relation, see
 * {@link OperationRestriction}.
 *
 * <p>
 * The operations not delegated are decided by the grants of the delegating type itself.
 * </p>
 *
 * @param relation
 *        The relation leading to the access parent.
 * @param operations
 *        The operations on the delegating object that are delegated, not empty. Discover is
 *        delegated in addition.
 */
public record RestrictedRelation(AccessParentFunction relation, Set<BoundCommandGroup> operations)
		implements AccessParentFunction {

	@Override
	public TLObject resolve(TLObject object) {
		return relation.resolve(object);
	}

	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return relation.getParentTypes(type);
	}

	@Override
	public AccessParentFunction getRelation() {
		return relation.getRelation();
	}

	@Override
	public boolean delegates(BoundCommandGroup operation) {
		return operations.contains(operation)
			|| (SimpleBoundCommandGroup.DISCOVER.equals(operation) && !operations.isEmpty());
	}

	@Override
	public Set<BoundCommandGroup> getOperations() {
		return operations;
	}

	@Override
	public ResKey getLabel() {
		String ids = operations.stream().map(BoundCommandGroup::getID).sorted().collect(Collectors.joining(", "));
		return I18NConstants.ACCESS_PARENT_RESTRICTED__RELATION_OPERATIONS.fill(relation.getLabel(), ids);
	}

}
