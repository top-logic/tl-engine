/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.basic.util.ComputationEx2;
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
	 *
	 * <p>
	 * An empty set means that no role may perform the command group. For a type
	 * {@link #isWithoutSecurity(TLClass) without security} however, the result is meaningless, since
	 * access to such a type is granted independent of roles.
	 * </p>
	 */
	Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup);

	/**
	 * Whether objects of the given type are not access controlled at all.
	 *
	 * <p>
	 * Every user may access the objects of a type without security, no matter which roles the user
	 * has on them. That cannot be expressed as a set of roles, therefore
	 * {@link #getAllowedRoles(TLClass, BoundCommandGroup)} must not be used to decide access for
	 * such a type. The access checks ({@link #isAllowed(Person, TLObject, BoundCommandGroup)} and
	 * its variants) already take the types without security into account.
	 * </p>
	 *
	 * @param type
	 *        The type of the objects to access, typically {@link TLObject#tType()} of the object in
	 *        question.
	 *
	 * @implSpec An implementation without a notion of types excluded from access control answers
	 *           <code>false</code> for every type.
	 */
	default boolean isWithoutSecurity(TLClass type) {
		return false;
	}

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
	 *
	 * <p>
	 * Within an {@link #uncheckedSecurity(Runnable) unchecked scope}, an implementation granting
	 * every access is delivered. Otherwise the {@link SecurityConfigurationService} must be started;
	 * asking for access rights without a security configuration is an error and is reported as one.
	 * </p>
	 */
	static ModelAccessRights getInstance() {
		ModelAccessRights unchecked = UncheckedAccessRights.active();
		if (unchecked != null) {
			return unchecked;
		}
		return SecurityConfigurationService.Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Runs the given job without any access check.
	 *
	 * <p>
	 * Within the job (and everything it calls in the current thread), {@link #getInstance()} delivers
	 * an implementation that grants every access. This is required where the security configuration
	 * cannot exist yet: the {@link SecurityConfigurationService} resolves the configured type and
	 * attribute names against the application model and therefore depends on the
	 * {@link com.top_logic.util.model.ModelService}. Setting up the model in turn touches the model
	 * itself - allocating the module singletons applies the default values of their attributes, and a
	 * {@code default-by-expression} default that allocates objects executes <i>TL-Script</i>. An
	 * access check at that point would ask for a service that is still starting up.
	 * </p>
	 *
	 * <p>
	 * The scope must be opened deliberately and kept as small as possible. It is not a fallback for
	 * a security configuration that failed to start: outside such a scope, a missing security
	 * configuration must fail loudly instead of silently operating an application without access
	 * control.
	 * </p>
	 *
	 * <p>
	 * Note that this switches off the <em>model based</em> access rights only. It neither affects the
	 * rights of a command, nor does it establish a
	 * {@link com.top_logic.basic.thread.ThreadContext#isSystemContext() system context}.
	 * </p>
	 *
	 * @param job
	 *        The job to run without an access check. A surrounding scope stays open afterwards.
	 * @return The result of the given job.
	 */
	static <T, E1 extends Throwable, E2 extends Throwable> T uncheckedSecurity(ComputationEx2<T, E1, E2> job)
			throws E1, E2 {
		return UncheckedAccessRights.execute(job);
	}

	/**
	 * Runs the given job without any access check.
	 *
	 * @see #uncheckedSecurity(ComputationEx2)
	 */
	static void uncheckedSecurity(Runnable job) {
		UncheckedAccessRights.execute(() -> {
			job.run();
			return null;
		});
	}
}
