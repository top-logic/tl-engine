/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;
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
 *        delegated in addition, if so configured.
 * @param discover
 *        How Discover on the delegating object is decided, see
 *        {@link OperationRestriction#getDiscover()}.
 */
public record RestrictedRelation(AccessParentFunction relation, Set<BoundCommandGroup> operations,
		DiscoverDelegation discover) implements AccessParentFunction {

	/**
	 * Creates a {@link RestrictedRelation} delegating Discover together with the given
	 * operations.
	 *
	 * @see #RestrictedRelation(AccessParentFunction, Set, DiscoverDelegation)
	 */
	public RestrictedRelation(AccessParentFunction relation, Set<BoundCommandGroup> operations) {
		this(relation, operations, DiscoverDelegation.DELEGATED);
	}

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
	public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
		return delegates(operation) ? relation.resolveAll(object, operation) : List.of();
	}

	@Override
	public Iterable<TLObject> resolveDraft(TLObject draft, BoundCommandGroup operation) {
		return delegates(operation) ? relation.resolveDraft(draft, operation) : List.of();
	}

	@Override
	public boolean delegatesCreation() {
		return delegates(SimpleBoundCommandGroup.CREATE) && relation.delegatesCreation();
	}

	@Override
	public boolean allows(TLObject object, BoundCommandGroup operation, ParentCheck check) {
		return delegates(operation) && relation.allows(object, operation, check);
	}

	@Override
	public boolean isDisjunctive() {
		return relation.isDisjunctive();
	}

	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return relation.getParentTypes(type);
	}

	@Override
	public AccessParentFunction getRelation() {
		return relation.getRelation();
	}

	/**
	 * @return Whether the operation is listed, or is Discover and {@link #discover() delegated}
	 *         together with the listed operations, and the restricted relation delegates it at all.
	 */
	@Override
	public boolean delegates(BoundCommandGroup operation) {
		return (operations.contains(operation)
			|| (SimpleBoundCommandGroup.DISCOVER.equals(operation) && discover == DiscoverDelegation.DELEGATED))
			&& relation.delegates(operation);
	}

	/**
	 * @return Whether Discover is not delegated together with the listed operations, but
	 *         {@link DiscoverDelegation#IMPLIED implied} by them.
	 */
	@Override
	public boolean impliesDiscover() {
		return discover == DiscoverDelegation.IMPLIED;
	}

	@Override
	public Set<BoundCommandGroup> getOperations() {
		return operations;
	}

	/**
	 * The label of the relation, followed by the labels of the delegated operations, ordered by
	 * their names, and a note when Discover is not delegated together with them.
	 */
	@Override
	public ResKey getLabel() {
		ResKey labels = OperationLabels.of(
			operations.stream().map(BoundCommandGroup::getID).collect(Collectors.toSet()));
		if (discover == DiscoverDelegation.IMPLIED) {
			return I18NConstants.ACCESS_PARENT_RESTRICTED_DISCOVER_IMPLIED__RELATION_OPERATIONS.fill(relation.getLabel(),
				labels);
		}
		return I18NConstants.ACCESS_PARENT_RESTRICTED__RELATION_OPERATIONS.fill(relation.getLabel(), labels);
	}

}
