/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link AccessParentFunction} leading to the access parents of all its candidates, see
 * {@link AnyOfAccessParent}.
 *
 * <p>
 * An operation is allowed as soon as one candidate allows it. A candidate not
 * {@link #delegates(BoundCommandGroup) delegating} the operation abstains. The candidate
 * {@link SelfRelation} lets the object itself decide beside the other candidates, see
 * {@link #includesSelf()}.
 * </p>
 *
 * @param candidates
 *        The relations leading to the access parents, none of them an {@link AnyOfRelation}.
 */
public record AnyOfRelation(List<AccessParentFunction> candidates) implements AccessParentFunction {

	/**
	 * Copies the candidates.
	 */
	public AnyOfRelation {
		candidates = List.copyOf(candidates);
	}

	/**
	 * @return The access parent of the first candidate leading anywhere.
	 */
	@Override
	public TLObject resolve(TLObject object) {
		for (AccessParentFunction candidate : candidates) {
			TLObject parent = candidate.resolve(object);
			if (parent != null) {
				return parent;
			}
		}
		return null;
	}

	/**
	 * @return The access parents of all candidates deciding the operation, one candidate after the
	 *         other, see {@link #deciding(BoundCommandGroup)}.
	 */
	@Override
	public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
		return () -> deciding(operation).stream()
			.flatMap(candidate -> StreamSupport.stream(candidate.resolveAll(object, operation).spliterator(), false))
			.iterator();
	}

	/**
	 * @return The access parents of the candidates leading to the container: nothing refers to an
	 *         object being created yet.
	 */
	@Override
	public Iterable<TLObject> resolveDraft(TLObject draft, BoundCommandGroup operation) {
		return () -> candidates.stream()
			.filter(AnyOfRelation::isContainerCandidate)
			.flatMap(candidate -> StreamSupport.stream(candidate.resolveDraft(draft, operation).spliterator(), false))
			.iterator();
	}

	/**
	 * @return Whether a candidate leading to the container delegates Create.
	 */
	@Override
	public boolean delegatesCreation() {
		return candidates.stream()
			.anyMatch(candidate -> isContainerCandidate(candidate)
				&& candidate.delegates(SimpleBoundCommandGroup.CREATE));
	}

	/**
	 * The candidates that may decide the given operation: for Create those leading to the
	 * container, all of them otherwise.
	 */
	private List<AccessParentFunction> deciding(BoundCommandGroup operation) {
		if (SimpleBoundCommandGroup.CREATE.equals(operation)) {
			return candidates.stream().filter(AnyOfRelation::isContainerCandidate).toList();
		}
		return candidates;
	}

	private static boolean isContainerCandidate(AccessParentFunction candidate) {
		return candidate.getRelation() instanceof ContainerRelation;
	}

	/**
	 * @return Whether a candidate delegates the operation; Create only when a candidate leading to
	 *         the container delegates it, see {@link #delegatesCreation()}.
	 */
	@Override
	public boolean delegates(BoundCommandGroup operation) {
		if (SimpleBoundCommandGroup.CREATE.equals(operation)) {
			return delegatesCreation();
		}
		return candidates.stream().anyMatch(candidate -> candidate.delegates(operation));
	}

	/**
	 * @return <code>null</code> when a candidate is not restricted to a list of operations, the
	 *         operations any candidate delegates otherwise, Create only as
	 *         {@link #delegates(BoundCommandGroup)} tells.
	 */
	@Override
	public Set<BoundCommandGroup> getOperations() {
		Set<BoundCommandGroup> result = new HashSet<>();
		for (AccessParentFunction candidate : candidates) {
			Set<BoundCommandGroup> operations = candidate.getOperations();
			if (operations == null) {
				return null;
			}
			result.addAll(operations);
		}
		if (!delegatesCreation()) {
			result.remove(SimpleBoundCommandGroup.CREATE);
		}
		return result;
	}

	/**
	 * @return Whether one of the candidates {@link #deciding(BoundCommandGroup) deciding} the
	 *         operation allows it, each candidate deciding the way it combines its own access
	 *         parents.
	 */
	@Override
	public boolean allows(TLObject object, BoundCommandGroup operation, ParentCheck check) {
		for (AccessParentFunction candidate : deciding(operation)) {
			if (candidate.allows(object, operation, check)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Whether a candidate is not restricted to a list of operations, a candidate leading to
	 *         the container delegates Create, see {@link #delegatesCreation()}, and the object
	 *         itself is no candidate, see {@link #includesSelf()}.
	 */
	@Override
	public boolean delegatesAll() {
		return getOperations() == null && delegatesCreation() && !includesSelf();
	}

	/**
	 * @return Whether one of the candidates leaves Discover to the operations it delegates, see
	 *         {@link AccessParentFunction#impliesDiscover()}.
	 */
	@Override
	public boolean impliesDiscover() {
		return candidates.stream().anyMatch(AccessParentFunction::impliesDiscover);
	}

	/**
	 * @return Whether one of the candidates is the object itself, see {@link SelfRelation}.
	 */
	@Override
	public boolean includesSelf() {
		return candidates.stream().anyMatch(AccessParentFunction::includesSelf);
	}

	/**
	 * @return Whether all candidates are disjunctive.
	 */
	@Override
	public boolean isDisjunctive() {
		return candidates.stream().allMatch(AccessParentFunction::isDisjunctive);
	}

	/**
	 * @return The parent types of all candidates, <code>null</code> when those of one candidate are
	 *         not known statically, as for a {@link ContainerRelation} without composition.
	 * @see #getParentTypes(Function)
	 */
	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return getParentTypes(candidate -> candidate.getParentTypes(type));
	}

	/**
	 * The union of the parent types of all candidates.
	 *
	 * @param candidateTypes
	 *        The parent types of a single candidate, <code>null</code> when they are not known
	 *        statically. A caller knowing more than a candidate itself, such as the possible
	 *        containers of a type, resolves the candidate's types here.
	 * @return The parent types of all candidates, <code>null</code> when those of one candidate are
	 *         not known statically.
	 */
	public Set<TLClass> getParentTypes(Function<? super AccessParentFunction, Set<TLClass>> candidateTypes) {
		Set<TLClass> result = new HashSet<>();
		for (AccessParentFunction candidate : candidates) {
			Set<TLClass> types = candidateTypes.apply(candidate);
			if (types == null) {
				return null;
			}
			result.addAll(types);
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * @return The labels of the candidates, separated by a semicolon to set them apart from the
	 *         comma of a {@link RestrictedRelation restricted} candidate's operations.
	 */
	@Override
	public ResKey getLabel() {
		ResKey labels = null;
		for (AccessParentFunction candidate : candidates) {
			ResKey label = candidate.getLabel();
			labels = labels == null ? label : I18NConstants.CANDIDATE_LIST__LIST_CANDIDATE.fill(labels, label);
		}
		return I18NConstants.ACCESS_PARENT_ANY_OF__CANDIDATES.fill(labels);
	}

}
