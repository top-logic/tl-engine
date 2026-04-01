/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContext;

/**
 * Central service for checking access rights based on the model's type configuration.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ModelAccessRights {

	/**
	 * Returns the roles that are permitted to perform the given command group on instances of the
	 * given type. Applies to both built-in command groups (Read, Write, Delete) and custom business
	 * operations (Approve, Cancel, etc.).
	 */
	Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup);

	/**
	 * Returns the roles that are permitted to perform the given command group on the given
	 * attribute. Relevant for READ and WRITE command groups to implement attribute-level access
	 * restrictions.
	 */
	Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute, BoundCommandGroup commandGroup);

	/**
	 * Checks whether the current person can perform the given command group on the given instance.
	 * Works for both built-in and custom operations.
	 * 
	 * @see #isAllowed(Person, TLObject, BoundCommandGroup)
	 */
	default boolean isAllowed(TLObject instance, BoundCommandGroup commandGroup) {
		return isAllowed(TLContext.currentUser(), instance, commandGroup);
	}

	/**
	 * Checks whether the given person can perform the given command group on the given instance.
	 * Works for both built-in and custom operations.
	 */
	boolean isAllowed(Person person, TLObject instance, BoundCommandGroup commandGroup);

	/**
	 * Checks whether the given person can perform the given command group on the given attribute of
	 * the given instance.
	 */
	boolean isAllowed(Person person, TLObject instance, TLStructuredTypePart attribute, BoundCommandGroup commandGroup);

	/**
	 * Checks whether the given person can create a new child object in the given composition
	 * attribute of the given parent instance.
	 *
	 * Convenience method equivalent to {@link #isAllowed(TLObject, BoundCommandGroup)} with
	 * {@value SimpleBoundCommandGroup#WRITE}.
	 */
	boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute);

	/**
	 * Returns all types that the given person can perform the given command group on (based on
	 * type-level rules; instance-level checks still required for specific objects).
	 */
	Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup);

	/**
	 * Determines the single {@link ModelAccessRights} instance.
	 */
	static ModelAccessRights getInstance() {
		return SecurityConfigurationService.Module.INSTANCE.getImplementationInstance();
	}
}
