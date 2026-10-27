/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
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
	 * Neither a {@link RoleRule} nor a {@link NavigationRule role parent rule} applies to the type,
	 * and its objects are held in no composition either, so no user can ever hold a role on them and
	 * no container decides for them.
	 * 
	 * <p>
	 * Only rules count as a role source. A role that is assigned on a single object is data, not a
	 * definition: it says nothing about the objects that are created next, so a type whose access
	 * depends on such an assignment alone is still reported.
	 * </p>
	 */
	NO_ROLE_SOURCE,

	/**
	 * No role is granted an operation on the type or on one of its attributes, so its objects are
	 * inaccessible to every user. A type that the application's code alone uses is declared internal instead.
	 * 
	 * @see SimpleBoundCommandGroup#READ
	 * @see ModelAccessRights#getAllowedRoles(com.top_logic.model.TLClass,
	 *      com.top_logic.tool.boundsec.BoundCommandGroup)
	 */
	NO_READ_GRANT,

	/**
	 * An operation is granted to a role that cannot be delivered on the type, neither on the type
	 * itself nor on any of its role parents. The grant therefore never takes effect.
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
	 * The type is configured with an access parent, but role rules or role parent rules apply to it
	 * as well. Those rules have no effect, since the access parent decides alone.
	 * 
	 * @see CoverageFinding#getRuleIds()
	 */
	SHADOWED_RULES,

	/**
	 * An operation is declared for the type, but no role holds it there: it is granted to no
	 * role, neither directly nor through an operation implying it. Every place checking it on
	 * an object of the type refuses every user.
	 *
	 * <p>
	 * The {@link com.top_logic.model.security.ApplicableOperations#BUILT_IN built-in operations}
	 * apply to every type without a declaration and are not reported.
	 * </p>
	 *
	 * @see CoverageFinding#getOperation()
	 */
	UNGRANTED_OPERATION,

	/**
	 * A view, a command, a form or a privileged script checks an operation on an object of the
	 * type, but the operation does not apply to the type. The check refuses every user.
	 *
	 * <p>
	 * A place checking the security root is reported for the type of the security root.
	 * </p>
	 *
	 * @see CoverageFinding#getOperation()
	 * @see CoverageFinding#getUses()
	 */
	INAPPLICABLE_OPERATION,

	/**
	 * A view, a command, a form or a privileged script checks an operation that is no registered
	 * command group.
	 *
	 * @see CoverageFinding#getOperationName()
	 * @see CoverageFinding#getUses()
	 */
	UNKNOWN_OPERATION;

}
