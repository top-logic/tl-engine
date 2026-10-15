/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.security;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * {@link ModelAccessRights} that grants every access without a check.
 *
 * <p>
 * Delivered by {@link ModelAccessRights#getInstance()} for code with
 * {@link ThreadContext#isSecurityUnchecked() unchecked security} that runs while the
 * {@link SecurityConfigurationService} is not started.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class UncheckedAccessRights implements ModelAccessRights {

	/** Singleton {@link UncheckedAccessRights} instance. */
	static final UncheckedAccessRights INSTANCE = new UncheckedAccessRights();

	private UncheckedAccessRights() {
		// Singleton constructor.
	}

	@Override
	public boolean isAllowed(Person person, TLObject instance, BoundCommandGroup commandGroup) {
		return true;
	}

	@Override
	public boolean isAllowed(Person person, TLObject instance, TLStructuredTypePart attribute,
			BoundCommandGroup commandGroup) {
		return true;
	}

	@Override
	public boolean isAllowedInitial(Person person, TLObject draft, TLStructuredTypePart attribute,
			BoundCommandGroup commandGroup) {
		return true;
	}

	/**
	 * @implNote Without the security configuration, no grants are known.
	 */
	@Override
	public boolean hasGrant(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
		return false;
	}

	@Override
	public boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute,
			TLClass type) {
		return true;
	}

	@Override
	public boolean isAllowedCreate(Person person, TLClass type, TLObject context) {
		return true;
	}

	/**
	 * Every type is without security here: with unchecked security no object is access
	 * controlled, so a caller asking whether a type is (e.g. before deciding by its
	 * {@link #getAllowedRoles(TLClass, BoundCommandGroup) roles}, which are unknown here) learns that
	 * access is granted independent of roles - consistent with the
	 * {@link #isAllowed(Person, TLObject, BoundCommandGroup) checks}, which grant everything.
	 */
	@Override
	public boolean isWithoutSecurity(TLClass type) {
		return true;
	}

	/**
	 * @implNote Without the security configuration, no roles are known. In contrast to the
	 *           {@link #isAllowed(Person, TLObject, BoundCommandGroup) checks}, the empty result must
	 *           not be mistaken for a granted access - a caller deciding access by roles cannot be
	 *           served with unchecked security.
	 */
	@Override
	public Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup) {
		return Collections.emptySet();
	}

	/**
	 * @implNote See {@link #getAllowedRoles(TLClass, BoundCommandGroup)}.
	 */
	@Override
	public Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute, BoundCommandGroup commandGroup) {
		return Collections.emptySet();
	}

	/**
	 * @implNote The accessible types cannot be determined without the security configuration; the
	 *           application model is not available either, since it is still being built when this
	 *           implementation is typically in use.
	 */
	@Override
	public Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup) {
		return Collections.emptySet();
	}

}
