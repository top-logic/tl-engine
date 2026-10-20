/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link ModelSecurity} backed by the engine's {@link ModelAccessRights}.
 *
 * <p>
 * Adds <b>no policy of its own</b>: every decision is the engine's, so an application configures
 * model access once - in its {@code <security-config>} - and the agent sees exactly the same rights
 * the interactive UI grants that user.
 * </p>
 *
 * <p>
 * Binds the run's principal once at construction rather than reading
 * {@code TLContext.currentUser()} per check: a concurrent tool batch runs on worker threads that
 * re-establish the sub-session, and a design that re-reads thread state per check is one
 * thread-local bug away from checking the wrong user.
 * </p>
 *
 * <p>
 * Constructed for every run. {@link ModelSecurity#UNCHECKED} is a test seam and no production path
 * uses it.
 * </p>
 */
public class ModelAccessRightsSecurity implements ModelSecurity {

	private final Person _person;

	private final ModelAccessRights _rights;

	/**
	 * Creates a {@link ModelAccessRightsSecurity} for the given run principal, checking against the
	 * engine's configured model access rights.
	 *
	 * @param person
	 *        The authenticated user the agent run acts as. A run with no principal is refused up front
	 *        (see {@code TopLogicSecurityContext.forRun}), so this is non-<code>null</code> in
	 *        production; a <code>null</code> principal is not a hole either, since the engine grants
	 *        no rights without a user.
	 * @throws IllegalStateException
	 *         If the engine's model security service is not running. Failing here is deliberate: a
	 *         missing service must stop the run rather than silently grant everything.
	 */
	public ModelAccessRightsSecurity(Person person) {
		this(person, activeRights());
	}

	/**
	 * Creates a {@link ModelAccessRightsSecurity} checking against the given rights.
	 *
	 * @param person
	 *        The authenticated user the agent run acts as, passed to {@code rights} unchanged.
	 * @param rights
	 *        The access rights to consult. Never <code>null</code>.
	 */
	public ModelAccessRightsSecurity(Person person, ModelAccessRights rights) {
		_person = person;
		_rights = Objects.requireNonNull(rights, "Model access rights must not be null.");
	}

	private static ModelAccessRights activeRights() {
		if (!SecurityConfigurationService.Module.INSTANCE.isActive()) {
			throw new IllegalStateException(
				"Model access rights are checked for every agent run, but the engine's model security"
					+ " service is not running. Re-activate it and write a <security-config>; there is"
					+ " no unchecked mode to fall back to.");
		}
		return ModelAccessRights.getInstance();
	}

	@Override
	public boolean canRead(TLObject obj) {
		return _rights.isAllowed(_person, obj, SimpleBoundCommandGroup.READ);
	}

	@Override
	public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
		return _rights.isAllowed(_person, obj, attribute, SimpleBoundCommandGroup.READ);
	}

	@Override
	public boolean canWrite(TLObject obj) {
		return _rights.isAllowed(_person, obj, SimpleBoundCommandGroup.WRITE);
	}

	@Override
	public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
		return _rights.isAllowed(_person, obj, attribute, SimpleBoundCommandGroup.WRITE);
	}

	@Override
	public boolean canDelete(TLObject obj) {
		return _rights.isAllowed(_person, obj, SimpleBoundCommandGroup.DELETE);
	}

	@Override
	public boolean canCreate(TLClass type, TLObject context) {
		return _rights.isAllowedCreate(_person, type, context);
	}

	@Override
	public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type) {
		return _rights.isAllowedCreate(_person, parent, compositionAttribute, type);
	}

	@Override
	public boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
		return _rights.isAllowedInitial(_person, draft, attribute, SimpleBoundCommandGroup.WRITE);
	}

	@Override
	public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
		return _rights.isAllowed(_person, obj, operation);
	}

	@Override
	public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
		// Filtered element-wise rather than through SearchExpression.filterSecurity: that helper
		// returns Object and preserves the input collection's shape, which would need an unchecked
		// cast here for no gain - the per-object check it performs is the one below.
		List<T> result = new ArrayList<>(objects.size());
		for (T obj : objects) {
			if (_rights.isReadAllowed(_person, obj)) {
				result.add(obj);
			}
		}
		return result;
	}

	@Override
	public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
		return _rights.getAccessibleTypes(_person, operation);
	}

	/**
	 * The principal every check of this instance is made for.
	 */
	public Person person() {
		return _person;
	}
}
