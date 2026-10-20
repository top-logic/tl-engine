/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.model.ModelService;

/**
 * Per-object model access checks for the principal of an agent run.
 *
 * <p>
 * The typed replacement for the {@code Object}-typed, caller-less
 * {@code SecurityContext.hasReadAccess}/{@code hasWriteAccess} stubs. Tools reach it through the
 * execution context they already receive, so no new plumbing is needed at the call sites - see
 * {@code ViewDocumentTool.viewDocument} for the shape of such a check.
 * </p>
 *
 * <p>
 * {@link ModelAccessRightsSecurity} - the implementation every production run carries - delegates to
 * the engine's {@code ModelAccessRights} and adds <b>no policy of its own</b>: two places deciding "may this be
 * read" is one place too many. The principal is bound once when the run's context is built rather
 * than read from the thread on each check - a concurrent tool batch runs on worker threads that
 * re-establish the sub-session, and a design that re-reads thread state per check is one thread-local
 * bug away from checking the wrong user.
 * </p>
 *
 * <p>
 * There is no mode that turns this off. An application that writes no {@code <security-config>} does
 * not get an unchecked agent - the engine's rights are deny-by-default, so it gets an agent that can
 * read nothing. See {@code doc/security/role-model-worked-example.md}.
 * </p>
 *
 * @see ModelAccessRightsSecurity
 */
public interface ModelSecurity {

	/**
	 * Grants everything and checks nothing.
	 *
	 * <p>
	 * <b>A test seam.</b> The engine's rights need a booted {@code SecurityConfigurationService},
	 * which a unit test about how a context behaves does not have. No production run carries it:
	 * {@code TopLogicSecurityContext.forRun(Person, boolean)} always constructs a
	 * {@link ModelAccessRightsSecurity}.
	 * </p>
	 */
	ModelSecurity UNCHECKED = new ModelSecurity() {

		@Override
		public boolean canRead(TLObject obj) {
			return true;
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return true;
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return true;
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return true;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type) {
			return true;
		}

		@Override
		public boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return true;
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return List.copyOf(objects);
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			// Every type, not the empty set: an empty result means "no types are accessible" (that is
			// what the engine returns for a fully restricted user), so returning it from the
			// grants-everything implementation would hide the whole model from callers that use this
			// to scope model enumeration.
			return TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel());
		}
	};

	/**
	 * Whether the run's principal may read the given object.
	 */
	boolean canRead(TLObject obj);

	/**
	 * Whether the run's principal may read the given attribute of the given object.
	 */
	boolean canRead(TLObject obj, TLStructuredTypePart attribute);

	/**
	 * Whether the run's principal may modify the given object.
	 */
	boolean canWrite(TLObject obj);

	/**
	 * Whether the run's principal may modify the given attribute of the given object.
	 */
	boolean canWrite(TLObject obj, TLStructuredTypePart attribute);

	/**
	 * Whether the run's principal may delete the given object.
	 */
	boolean canDelete(TLObject obj);

	/**
	 * Whether the run's principal may create an instance of the given type in the given context.
	 *
	 * @param context
	 *        The object the new instance is created in, or <code>null</code> for a top-level
	 *        creation, which is checked against the global security root.
	 */
	boolean canCreate(TLClass type, TLObject context);

	/**
	 * Whether the run's principal may create a new child of the given type in the given composition
	 * of the given parent.
	 *
	 * @param type
	 *        The type of the new child, which may be a specialization of the composition's target
	 *        type; <code>null</code> for the target type itself.
	 */
	boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type);

	/**
	 * Whether the run's principal may set the initial value of the given attribute of an object
	 * that is being created.
	 *
	 * <p>
	 * {@link #canWrite(TLObject, TLStructuredTypePart)} answers <code>true</code> for every object
	 * not yet committed, because the roles that decide it are derived at commit time. This is the
	 * question the application's create dialogs ask instead: an attribute grant restricts an initial
	 * value where it can be decided before the object exists, i.e. on the object its access parent
	 * leads to.
	 * </p>
	 *
	 * @param draft
	 *        The object being created, in the current transaction.
	 */
	boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute);

	/**
	 * Whether the run's principal may perform a custom business operation on the given object.
	 *
	 * <p>
	 * The permission for an operation such as "approve" is semantically distinct from the data the
	 * operation touches, so it is checked as its own command group rather than decomposed into
	 * reads and writes.
	 * </p>
	 */
	boolean canPerform(TLObject obj, BoundCommandGroup operation);

	/**
	 * The subset of the given objects the run's principal may read.
	 *
	 * <p>
	 * Tools must apply this <b>before</b> paginating and before reporting a total count: an
	 * unfiltered count discloses cardinality the user may not see.
	 * </p>
	 */
	<T extends TLObject> List<T> filterReadable(Collection<T> objects);

	/**
	 * The types the run's principal may perform the given operation on.
	 *
	 * <p>
	 * Used to restrict model enumeration and the model digest injected into the system prompt.
	 * </p>
	 */
	Set<TLClass> accessibleTypes(BoundCommandGroup operation);

	/**
	 * Asserts that the run's principal may read the given object.
	 *
	 * @throws ToolSecurityException
	 *         If the read is not permitted.
	 */
	default void requireRead(TLObject obj) {
		if (!canRead(obj)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.READ, typeName(obj));
		}
	}

	/**
	 * Asserts that the run's principal may modify the given object.
	 *
	 * @throws ToolSecurityException
	 *         If the write is not permitted.
	 */
	default void requireWrite(TLObject obj) {
		if (!canWrite(obj)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.WRITE, typeName(obj));
		}
	}

	/**
	 * The qualified type name of the given object, or <code>null</code> if it has no type.
	 */
	private static String typeName(TLObject obj) {
		if (obj == null || obj.tType() == null) {
			return null;
		}
		return obj.tType().toString();
	}
}
