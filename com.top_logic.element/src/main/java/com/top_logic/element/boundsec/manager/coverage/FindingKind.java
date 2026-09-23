/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Classification of a {@link CoverageFinding}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum FindingKind {

	/**
	 * Neither a {@link RoleRule} nor a {@link NavigationRule security parent rule} applies to the
	 * type, so no user can ever hold a role on its objects.
	 *
	 * <p>
	 * Only rules count as a role source. A role that is assigned on a single object is data, not a
	 * definition: it says nothing about the objects that are created next, so a type whose access
	 * depends on such an assignment alone is still reported.
	 * </p>
	 *
	 * @see CoverageFinding#isRootFallbackActive()
	 */
	NO_ROLE_SOURCE,

	/**
	 * No role is granted the read operation on the type, so its objects are inaccessible to every
	 * user. A type that the application's code alone uses is declared internal instead.
	 *
	 * @see SimpleBoundCommandGroup#READ
	 * @see ModelAccessRights#getAllowedRoles(com.top_logic.model.TLClass,
	 *      com.top_logic.tool.boundsec.BoundCommandGroup)
	 */
	NO_READ_GRANT,

	/**
	 * An operation is granted to a role that cannot be delivered on the type, neither on the type
	 * itself nor on any of its security parents. The grant therefore never takes effect.
	 *
	 * <p>
	 * A role is delivered by a rule that computes it, and equally by a {@link BoundedRole role
	 * assignment} that names an object of the type explicitly. An application that assigns a role
	 * by hand instead of computing it therefore produces no finding.
	 * </p>
	 *
	 * @see CoverageFinding#getOperation()
	 * @see CoverageFinding#getRoles()
	 * @see ElementAccessManager#canHaveRole(com.top_logic.model.TLClass,
	 *      com.top_logic.tool.boundsec.wrap.BoundedRole)
	 */
	DEAD_GRANT,

	/**
	 * The type has no role source, but it is the target of exactly one composition. Navigating that
	 * composition backwards yields the container of an object, which is proposed as its security
	 * parent.
	 *
	 * @see CoverageFinding#getSuggestedRule()
	 */
	SUGGESTED_PARENT,

	/**
	 * The type has no role source and is the target of more than one composition, so the container
	 * to use as security parent cannot be derived.
	 *
	 * @see CoverageFinding#getContainerReferences()
	 */
	AMBIGUOUS_PARENT;

}
