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
	 * Returns the roles of which the user must hold at least one <em>in addition</em> to the
	 * object-level rights to perform the given command group on the given attribute. Relevant for
	 * READ and WRITE command groups to implement attribute-level access restrictions.
	 *
	 * <p>
	 * The returned roles further restrict access on top of the {@link #getAllowedRoles(TLClass,
	 * BoundCommandGroup) object-level rights}: the user may perform the command group on the
	 * attribute only if he is allowed to perform it on the object <em>and</em> holds one of the
	 * returned roles.
	 * </p>
	 *
	 * <p>
	 * An empty set means there is no additional attribute-level restriction: the user may perform
	 * the command group on the attribute whenever he has the corresponding rights on the object.
	 * </p>
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
	 * Checks whether the current person can read the given instance.
	 * 
	 * @implSpec Just calls {@link #isAllowed(TLObject, BoundCommandGroup)} with
	 *           {@link SimpleBoundCommandGroup#READ}.
	 */
	default boolean isReadAllowed(TLObject instance) {
		return isAllowed(instance, SimpleBoundCommandGroup.READ);
	}

	/**
	 * Checks whether the given person can perform the given command group on the given instance.
	 * Works for both built-in and custom operations.
	 */
	boolean isAllowed(Person person, TLObject instance, BoundCommandGroup commandGroup);

	/**
	 * Checks whether the given person can read the given instance.
	 * 
	 * @implSpec Just calls {@link #isAllowed(Person, TLObject, BoundCommandGroup)} with
	 *           {@link SimpleBoundCommandGroup#READ}.
	 */
	default boolean isReadAllowed(Person person, TLObject instance) {
		return isAllowed(person, instance, SimpleBoundCommandGroup.READ);
	}

	/**
	 * Checks whether the given person can perform the given command group on the given attribute of
	 * the given instance.
	 */
	boolean isAllowed(Person person, TLObject instance, TLStructuredTypePart attribute, BoundCommandGroup commandGroup);

	/**
	 * Checks whether the given person can read the given attribute of the given instance.
	 * 
	 * @implSpec Just calls
	 *           {@link #isAllowed(Person, TLObject, TLStructuredTypePart, BoundCommandGroup)} with
	 *           {@link SimpleBoundCommandGroup#READ}.
	 */
	default boolean isReadAllowed(Person person, TLObject instance, TLStructuredTypePart attribute) {
		return isAllowed(person, instance, attribute, SimpleBoundCommandGroup.READ);
	}

	/**
	 * Checks whether the current person can read the given attribute of the given instance.
	 * 
	 * @implSpec Just calls {@link #isReadAllowed(TLObject, TLStructuredTypePart)} with
	 *           {@link TLContext#currentUser()}.
	 */
	default boolean isReadAllowed(TLObject instance, TLStructuredTypePart attribute) {
		return isReadAllowed(TLContext.currentUser(), instance, attribute);
	}

	/**
	 * Checks whether the given person can create a new child object in the given composition
	 * attribute of the given parent instance.
	 *
	 * <p>
	 * Both conditions of object creation must hold (see the model-based access rights spec, section
	 * 2.3.6): the person needs the {@link SimpleBoundCommandGroup#CREATE CREATE} right on the created
	 * type (the composition attribute's target type) in the parent context, <em>and</em> the
	 * {@link SimpleBoundCommandGroup#WRITE WRITE} right on the composition attribute of the parent.
	 * </p>
	 */
	boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute);

	/**
	 * Checks whether the given person can create an instance of the given type in the given context.
	 *
	 * <p>
	 * This is condition 1 of object creation (see the model-based access rights spec, section
	 * 2.3.6): the person must hold one of the roles granted the {@link SimpleBoundCommandGroup#CREATE
	 * CREATE} command group on the type, checked on the given context object. When no context is
	 * given (<code>null</code>), the check uses the global security root.
	 * </p>
	 */
	boolean isAllowedCreate(Person person, TLClass type, TLObject context);

	/**
	 * Returns all types that the given person can perform the given command group on (based on
	 * type-level rules; instance-level checks still required for specific objects).
	 *
	 * <p>
	 * A user that bypasses the model security (e.g. an administrator, who may act on every object)
	 * gets <em>all</em> types of the system, including types without any configured rights. A
	 * restricted user without access gets no types at all.
	 * </p>
	 */
	Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup);

	/**
	 * Determines the single {@link ModelAccessRights} instance.
	 */
	static ModelAccessRights getInstance() {
		return SecurityConfigurationService.Module.INSTANCE.getImplementationInstance();
	}
}
