/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.security;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.util.ComputationEx2;
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
 * Installed for the duration of a {@link ModelAccessRights#uncheckedSecurity(ComputationEx2) scope},
 * which is the only way to reach this implementation. Whoever opens such a scope declares that the
 * code inside it must not be subject to access rights - typically because the security configuration
 * cannot exist yet, see {@link ModelAccessRights#uncheckedSecurity(ComputationEx2)}.
 * </p>
 *
 * @implNote The scope is held in a {@link ThreadLocal}: the code that must run without a check is
 *           known per thread, and a request being served concurrently must not lose its access
 *           checks because another thread opened a scope.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class UncheckedAccessRights implements ModelAccessRights {

	/** Singleton {@link UncheckedAccessRights} instance. */
	private static final UncheckedAccessRights INSTANCE = new UncheckedAccessRights();

	private static final ThreadLocal<UncheckedAccessRights> ACTIVE = new ThreadLocal<>();

	private UncheckedAccessRights() {
		// Singleton constructor.
	}

	/**
	 * The {@link UncheckedAccessRights} installed for the current thread, or <code>null</code>, if
	 * the current thread does not run in an
	 * {@link ModelAccessRights#uncheckedSecurity(ComputationEx2) unchecked scope}.
	 */
	static ModelAccessRights active() {
		return ACTIVE.get();
	}

	/**
	 * Runs the given job with {@link UncheckedAccessRights} installed for the current thread.
	 *
	 * @see ModelAccessRights#uncheckedSecurity(ComputationEx2)
	 */
	static <T, E1 extends Throwable, E2 extends Throwable> T execute(ComputationEx2<T, E1, E2> job) throws E1, E2 {
		if (ACTIVE.get() != null) {
			// A surrounding scope is already open; there is nothing to install and nothing to restore.
			return job.run();
		}
		ACTIVE.set(INSTANCE);
		try {
			return job.run();
		} finally {
			ACTIVE.remove();
		}
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
	public boolean isAllowedCreate(Person person, TLObject parent, TLStructuredTypePart compositionAttribute) {
		return true;
	}

	@Override
	public boolean isAllowedCreate(Person person, TLClass type, TLObject context) {
		return true;
	}

	/**
	 * @implNote Without the security configuration, no roles are known. In contrast to the
	 *           {@link #isAllowed(Person, TLObject, BoundCommandGroup) checks}, the empty result must
	 *           not be mistaken for a granted access - a caller deciding access by roles cannot be
	 *           served in an unchecked scope.
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
	 *           application model is not available either, since it is still being built when an
	 *           unchecked scope is typically open.
	 */
	@Override
	public Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup) {
		return Collections.emptySet();
	}

}
