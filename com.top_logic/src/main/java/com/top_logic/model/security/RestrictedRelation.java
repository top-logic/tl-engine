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

	/**
	 * Checks the restriction.
	 *
	 * @throws IllegalArgumentException
	 *         If no operation is given: a relation without operations to delegate is no access
	 *         parent, and one delegating every operation is the relation itself.
	 */
	public RestrictedRelation {
		if (operations.isEmpty()) {
			throw new IllegalArgumentException("An access parent restricted to no operations decides nothing.");
		}
	}

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

	/**
	 * The label of the relation, followed by the labels of the delegated operations, ordered by
	 * their names.
	 */
	@Override
	public ResKey getLabel() {
		ResKey labels = AccessParentConfig.operationLabels(
			operations.stream().map(BoundCommandGroup::getID).collect(Collectors.toSet()));
		return I18NConstants.ACCESS_PARENT_RESTRICTED__RELATION_OPERATIONS.fill(relation.getLabel(), labels);
	}

}
