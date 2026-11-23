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
 * {@link #delegates(BoundCommandGroup) delegating} the operation abstains.
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
	 * @return The access parents of all candidates, one candidate after the other.
	 */
	@Override
	public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
		return () -> candidates.stream()
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

	private static boolean isContainerCandidate(AccessParentFunction candidate) {
		return candidate.getRelation() instanceof ContainerRelation;
	}

	@Override
	public boolean delegates(BoundCommandGroup operation) {
		return candidates.stream().anyMatch(candidate -> candidate.delegates(operation));
	}

	/**
	 * @return <code>null</code> when a candidate delegates all operations, the operations any
	 *         candidate delegates otherwise.
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
		return result;
	}

	/**
	 * @return Whether one of the candidates allows the operation, each candidate deciding the way
	 *         it combines its own access parents.
	 */
	@Override
	public boolean allows(TLObject object, BoundCommandGroup operation, ParentCheck check) {
		for (AccessParentFunction candidate : candidates) {
			if (candidate.allows(object, operation, check)) {
				return true;
			}
		}
		return false;
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
