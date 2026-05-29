/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.Logger;

/**
 * Shared access-control enforcement for removable UI units.
 *
 * <p>
 * All securable elements (nav-items, tabs, tiles) run the same check: resolve the referenced
 * {@link SecurityScope} and ask whether the current user may see the unit. A unit without an
 * {@link AccessControl} is always accessible; a reference to an undefined scope fails closed.
 * </p>
 */
public class AccessChecks {

	/**
	 * Whether the UI unit carrying the given {@link AccessControl} is accessible to the current
	 * user.
	 *
	 * @param accessControl
	 *        The access control of the unit, or {@code null} when the unit is not guarded.
	 * @return {@code true} when the unit may be shown.
	 */
	public static boolean isAccessible(AccessControl accessControl) {
		if (accessControl == null) {
			// No access control: access is granted.
			return true;
		}
		String scopeId = accessControl.getScope();
		SecurityScope scope = SecurityScopeService.getInstance().getScope(scopeId);
		if (scope == null) {
			Logger.error("Reference to undefined security scope '" + scopeId + "'.", AccessChecks.class);
			// Fail closed: a misconfigured reference must not expose a guarded unit.
			return false;
		}
		return scope.isVisible();
	}

	/**
	 * Whether the UI unit described by the given configuration is accessible to the current user.
	 *
	 * @param config
	 *        The configuration of the unit.
	 * @return {@code true} when the unit may be shown.
	 *
	 * @see #isAccessible(AccessControl)
	 */
	public static boolean isAccessible(WithAccessControl config) {
		return isAccessible(config.getAccessControl());
	}

}
