/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The relation leading from an object to its access parent: the object whose access definition
 * decides access to it.
 * <p>
 * An object with an access parent has no grants and no roles of its own for the operations the
 * relation {@link #delegates(BoundCommandGroup) delegates}. Whether a user may perform such an
 * operation on it is whether the user may perform the corresponding operation on its access parent,
 * and that parent may delegate further.
 * </p>
 *
 * <p>
 * A relation may lead to several access parents, see {@link #resolveAll(TLObject, BoundCommandGroup)}.
 * The operation is then decided by {@link #allows(TLObject, BoundCommandGroup, ParentCheck)}, by
 * default allowed as soon as it is allowed on one of them.
 * </p>
 *
 * @see ModelAccessRights#getAccessParent(TLClass)
 * @see AccessParentDefinition
 */
public interface AccessParentFunction {

	/**
	 * The access parent of the given object.
	 *
	 * <p>
	 * For a relation leading to several access parents, the first of them. The access decision
	 * follows {@link #resolveAll(TLObject, BoundCommandGroup)} instead.
	 * </p>
	 *
	 * @param object
	 *        The object delegating its access decision.
	 * @return The object deciding, <code>null</code> when the relation leads nowhere. An object
	 *         without access parent is not accessible.
	 */
	TLObject resolve(TLObject object);

	/**
	 * The access parents deciding the given operation on the given object.
	 *
	 * @param object
	 *        The object delegating its access decision.
	 * @param operation
	 *        The operation on the delegating object.
	 * @return The access parents, without <code>null</code>, possibly computed lazily while
	 *         iterating, and possibly containing an object more than once. Empty when the relation
	 *         does not {@link #delegates(BoundCommandGroup) delegate} the operation or leads
	 *         nowhere. By default the {@link #resolve(TLObject) single access parent}.
	 */
	default Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
		if (!delegates(operation)) {
			return List.of();
		}
		TLObject parent = resolve(object);
		return parent == null ? List.of() : List.of(parent);
	}

	/**
	 * The access parents deciding the given operation on the given transient object, which is
	 * being created.
	 *
	 * <p>
	 * A transient object knows the {@link TLObject#tContainer() container} it is created in, but not
	 * necessarily the composition that will hold it, and nothing refers to it yet.
	 * </p>
	 *
	 * @param draft
	 *        The transient object delegating its access decision.
	 * @param operation
	 *        The operation on the delegating object.
	 * @return The access parents as {@link #resolveAll(TLObject, BoundCommandGroup)} returns them,
	 *         which is the default.
	 */
	default Iterable<TLObject> resolveDraft(TLObject draft, BoundCommandGroup operation) {
		return resolveAll(draft, operation);
	}

	/**
	 * Whether the creation of an object is checked as a write of the context the object is created
	 * in.
	 *
	 * @return By default, whether the relation {@link #delegates(BoundCommandGroup) delegates}
	 *         Create.
	 */
	default boolean delegatesCreation() {
		return delegates(SimpleBoundCommandGroup.CREATE);
	}

	/**
	 * Whether the given operation on the given object is allowed, decided by its access parents.
	 *
	 * <p>
	 * By default, the operation is allowed as soon as the given check allows it on one of the
	 * {@link #resolveAll(TLObject, BoundCommandGroup) access parents}; the parents after the first
	 * one allowing it are not checked. A relation combining its access parents differently
	 * overrides this method and {@link #isDisjunctive()}. Every implementation keeps the contract:
	 * </p>
	 * <ol>
	 * <li>{@link #resolveAll(TLObject, BoundCommandGroup)} yields at least every object this method
	 * may pass to the check.</li>
	 * <li>This method returns <code>true</code> only when the check returned <code>true</code> for
	 * at least one of these objects.</li>
	 * <li>The decision is monotonic: a check returning <code>true</code> for more objects never
	 * turns an allowed operation into a denied one. The access check stores allowed decisions
	 * computed while a check in a cycle of access parents answered <code>false</code>
	 * provisionally.</li>
	 * </ol>
	 *
	 * @param object
	 *        The object delegating its access decision.
	 * @param operation
	 *        The operation on the delegating object, passed to the check unchanged.
	 * @param check
	 *        The check of the operation on one access parent.
	 * @return Whether the operation is allowed, <code>false</code> without access parents.
	 */
	default boolean allows(TLObject object, BoundCommandGroup operation, ParentCheck check) {
		for (TLObject parent : resolveAll(object, operation)) {
			if (check.test(parent, operation)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether {@link #allows(TLObject, BoundCommandGroup, ParentCheck)} allows an operation as soon
	 * as it is allowed on one of the {@link #resolveAll(TLObject, BoundCommandGroup) access
	 * parents}.
	 *
	 * <p>
	 * Only then the users holding a role on one of the access parents are exactly the users the
	 * access parents admit; otherwise they are a superset.
	 * </p>
	 *
	 * @return <code>true</code> by default.
	 */
	default boolean isDisjunctive() {
		return true;
	}

	/**
	 * The types an access parent of an object of the given type may have.
	 *
	 * @param type
	 *        The delegating type.
	 * @return The possible types of the access parent, <code>null</code> when they are not known
	 *         statically. A type whose parent types are unknown counts as accessible in a check
	 *         that depends on no concrete object.
	 */
	Set<TLClass> getParentTypes(TLClass type);

	/**
	 * Short description of the relation for the coverage display.
	 */
	ResKey getLabel();

	/**
	 * Whether the access parent decides the given operation on the delegating object.
	 *
	 * @param operation
	 *        The operation on the delegating object.
	 * @return Whether the operation is delegated, <code>true</code> for every operation unless the
	 *         relation is {@link RestrictedRelation restricted}.
	 */
	default boolean delegates(BoundCommandGroup operation) {
		return true;
	}

	/**
	 * Whether the given access parent decides the given operation.
	 *
	 * @param accessParent
	 *        The access parent of a type, <code>null</code> when the type decides for itself.
	 * @param operation
	 *        The operation on an object of the type.
	 * @return Whether the access parent {@link #delegates(BoundCommandGroup) delegates} the
	 *         operation, <code>false</code> for no access parent.
	 */
	static boolean isDelegated(AccessParentFunction accessParent, BoundCommandGroup operation) {
		return accessParent != null && accessParent.delegates(operation);
	}

	/**
	 * The operations on the delegating object the access parent decides, besides Discover, when it
	 * is restricted to a list of them.
	 *
	 * @return The delegated operations, <code>null</code> when the relation is not restricted to a
	 *         list: it then delegates every operation, unless it tells otherwise by
	 *         {@link #delegatesAll()} - an {@link AnyOfRelation} without a candidate leading to the
	 *         container does not delegate Create.
	 *
	 * @see #delegates(BoundCommandGroup)
	 * @see #delegatesAll()
	 */
	default Set<BoundCommandGroup> getOperations() {
		return null;
	}

	/**
	 * Whether the access parent decides every operation on the delegating object.
	 *
	 * <p>
	 * A type whose access parent decides every operation has neither grants nor roles of its own.
	 * Otherwise, the grants of the type decide the operations not
	 * {@link #delegates(BoundCommandGroup) delegated}.
	 * </p>
	 *
	 * @return By default, whether the relation is not restricted to a list of
	 *         {@link #getOperations() operations}. A relation leaving an operation to the type
	 *         without listing its operations, like an {@link AnyOfRelation} not delegating Create,
	 *         overrides this method.
	 */
	default boolean delegatesAll() {
		return getOperations() == null;
	}

	/**
	 * Whether the given access parent decides every operation, see {@link #delegatesAll()}.
	 *
	 * @param accessParent
	 *        The access parent of a type, <code>null</code> when the type decides for itself.
	 * @return <code>false</code> for no access parent.
	 */
	static boolean delegatesAll(AccessParentFunction accessParent) {
		return accessParent != null && accessParent.delegatesAll();
	}

	/**
	 * The relation leading to the access parent, without a restriction of the delegated
	 * operations.
	 *
	 * @return This relation, unless it is {@link RestrictedRelation restricted}.
	 */
	default AccessParentFunction getRelation() {
		return this;
	}

	/**
	 * The label of the type owning the given reference, its name where it has no label, for a
	 * {@link #getLabel() label} naming the reference.
	 */
	static ResKey ownerLabel(TLReference reference) {
		TLType owner = reference.getOwner();
		return ResKey.fallback(TLModelNamingConvention.getTypeLabelKey(owner), ResKey.text(owner.getName()));
	}

	/**
	 * The label of the given reference, its name where it has no label, for a
	 * {@link #getLabel() label} naming the reference.
	 */
	static ResKey referenceLabel(TLReference reference) {
		return TLModelNamingConvention.getTypePartLabelKey(reference);
	}

}
