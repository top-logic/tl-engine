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
	 * @see CoverageFinding#isRootFallbackActive()
	 */
	NO_ROLE_SOURCE,

	/**
	 * No role is granted the read operation on the type, so its objects are invisible for every
	 * user that does not bypass the access check.
	 *
	 * @see SimpleBoundCommandGroup#READ
	 * @see ModelAccessRights#getAllowedRoles(com.top_logic.model.TLClass,
	 *      com.top_logic.tool.boundsec.BoundCommandGroup)
	 */
	NO_READ_GRANT,

	/**
	 * An operation is granted to a role that no rule can deliver on the type, neither on the type
	 * itself nor on any of its security parents. The grant therefore never takes effect.
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
