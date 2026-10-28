/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.model.annotate.security.OperationDeclaration;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The operations that apply to the objects of a type (or to a module singleton), together with the
 * operations implying them.
 *
 * <p>
 * The {@link #BUILT_IN built-in operations} apply to every type. Any other operation applies only
 * where an {@link OperationDeclaration} declares it: on the type itself, on one of its
 * generalizations, or on its module (for a singleton, also on the singleton itself).
 * </p>
 *
 * <p>
 * An operation may be {@link OperationDeclaration#getImpliedBy() implied} by other operations:
 * whoever holds an implying operation on an object also holds the implied one. Implications are
 * transitive.
 * </p>
 *
 * @see ModelAccessRights#getOperations(com.top_logic.model.TLClass)
 * @see ModelAccessRights#getObjectOperations(com.top_logic.model.TLObject)
 */
public final class ApplicableOperations {

	/**
	 * The operations applying to every type without a declaration.
	 */
	public static final Set<BoundCommandGroup> BUILT_IN = Collections.unmodifiableSet(new LinkedHashSet<>(
		List.of(
			SimpleBoundCommandGroup.READ,
			SimpleBoundCommandGroup.WRITE,
			SimpleBoundCommandGroup.CREATE,
			SimpleBoundCommandGroup.DELETE,
			SimpleBoundCommandGroup.DISCOVER,
			SimpleBoundCommandGroup.EXPORT)));

	/**
	 * The operations of a type without any declaration: the {@link #BUILT_IN built-in operations}
	 * alone, none of them implied by another one.
	 */
	public static final ApplicableOperations BUILT_IN_ONLY = new ApplicableOperations(Collections.emptyMap());

	/**
	 * All applying operations, each mapped to the operations declared to imply it directly.
	 */
	private final Map<BoundCommandGroup, Set<BoundCommandGroup>> _impliedBy;

	/**
	 * All applying operations, each mapped to the operations implying it directly or transitively.
	 */
	private final Map<BoundCommandGroup, Set<BoundCommandGroup>> _implying;

	/**
	 * Creates {@link ApplicableOperations}.
	 *
	 * @param declared
	 *        The declared operations, each mapped to the operations declared to imply it directly.
	 *        The {@link #BUILT_IN built-in operations} are added implicitly.
	 */
	public ApplicableOperations(Map<BoundCommandGroup, Set<BoundCommandGroup>> declared) {
		Map<BoundCommandGroup, Set<BoundCommandGroup>> impliedBy = new HashMap<>();
		for (BoundCommandGroup builtIn : BUILT_IN) {
			impliedBy.put(builtIn, Collections.emptySet());
		}
		for (Map.Entry<BoundCommandGroup, Set<BoundCommandGroup>> entry : declared.entrySet()) {
			Set<BoundCommandGroup> direct = new HashSet<>(entry.getValue());
			Set<BoundCommandGroup> before = impliedBy.get(entry.getKey());
			if (before != null) {
				direct.addAll(before);
			}
			impliedBy.put(entry.getKey(), Collections.unmodifiableSet(direct));
		}
		_impliedBy = Collections.unmodifiableMap(impliedBy);

		Map<BoundCommandGroup, Set<BoundCommandGroup>> implying = new HashMap<>();
		for (BoundCommandGroup operation : impliedBy.keySet()) {
			implying.put(operation, Collections.unmodifiableSet(closure(operation)));
		}
		_implying = Collections.unmodifiableMap(implying);
	}

	/**
	 * The operations implying the given one, directly or transitively.
	 */
	private Set<BoundCommandGroup> closure(BoundCommandGroup operation) {
		Set<BoundCommandGroup> result = new HashSet<>();
		Deque<BoundCommandGroup> todo = new ArrayDeque<>(_impliedBy.getOrDefault(operation, Collections.emptySet()));
		while (!todo.isEmpty()) {
			BoundCommandGroup next = todo.pop();
			if (result.add(next)) {
				todo.addAll(_impliedBy.getOrDefault(next, Collections.emptySet()));
			}
		}
		return result;
	}

	/**
	 * Whether the given operation is one of the {@link #BUILT_IN built-in operations}.
	 */
	public static boolean isBuiltIn(BoundCommandGroup operation) {
		return BUILT_IN.contains(operation);
	}

	/**
	 * All operations applying, the {@link #BUILT_IN built-in operations} included.
	 */
	public Set<BoundCommandGroup> getOperations() {
		return _impliedBy.keySet();
	}

	/**
	 * Whether the given operation applies.
	 */
	public boolean appliesTo(BoundCommandGroup operation) {
		return _impliedBy.containsKey(operation);
	}

	/**
	 * The operations declared to imply the given one directly.
	 *
	 * @return The empty set for an operation that does not apply or is not implied.
	 */
	public Set<BoundCommandGroup> getImpliedBy(BoundCommandGroup operation) {
		return _impliedBy.getOrDefault(operation, Collections.emptySet());
	}

	/**
	 * The operations implying the given one, directly or through a chain of implications.
	 *
	 * <p>
	 * Whoever holds one of the result on an object also holds the given operation on it.
	 * </p>
	 *
	 * @return The empty set for an operation that does not apply or is not implied. The given
	 *         operation itself is part of the result only if the implications form a cycle, which
	 *         is a configuration error.
	 */
	public Set<BoundCommandGroup> getImplying(BoundCommandGroup operation) {
		return _implying.getOrDefault(operation, Collections.emptySet());
	}

	/**
	 * Whether the given operation is implied by itself through a cycle of implications.
	 */
	public boolean isCyclic(BoundCommandGroup operation) {
		return getImplying(operation).contains(operation);
	}

	@Override
	public String toString() {
		return ApplicableOperations.class.getSimpleName() + _impliedBy;
	}

}
