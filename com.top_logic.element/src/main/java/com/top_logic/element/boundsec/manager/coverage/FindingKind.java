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
	 * For a type whose access parent decides some operations but not Read, no role is granted Read
	 * on the type itself.
	 * 
	 * @see SimpleBoundCommandGroup#READ
	 * @see ModelAccessRights#getAllowedRoles(com.top_logic.model.TLClass, com.top_logic.tool.boundsec.BoundCommandGroup)
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
	 * <p>
	 * The operation "Create" is checked where an object is created: in its container, or on the
	 * security root for a type no composition holds. A role granted it must be deliverable there;
	 * on the type itself it does not count.
	 * </p>
	 * 
	 * @see SimpleBoundCommandGroup#CREATE
	 * @see CoverageFinding#getOperation()
	 * @see CoverageFinding#getRoles()
	 * @see ElementAccessManager#canHaveRole(com.top_logic.model.TLClass, com.top_logic.tool.boundsec.wrap.BoundedRole)
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
	UNKNOWN_OPERATION,

	/**
	 * An operation declared for the application, not a built-in one, is granted on the type, but
	 * no view, command, form or privileged script checks it - nor an operation it implies - on
	 * objects of the type. The grant has no effect.
	 *
	 * <p>
	 * Reported only when the places checking an operation are known. A place not declaring the
	 * type it checks counts as checking every type.
	 * </p>
	 *
	 * @see CoverageFinding#getOperation()
	 * @see CoverageFinding#getRoles()
	 */
	UNCHECKED_GRANT,

	/**
	 * A rule granting roles on historic versions only through anchor references applies to the
	 * type, but no object of the type can be covered by one of these references: The type shares no
	 * instances with the target type of a reference and is no composition part of such a type. The
	 * rule never grants a role.
	 * @see CoverageFinding#getRuleIds()
	 */
	ANCHOR_NEVER_COVERS,

	/**
	 * A rule of the type uses a historic reference as anchor that is stored in a column without an
	 * index. Each search for the anchors of a version scans the whole table.
	 * @see CoverageFinding#getRuleIds()
	 */
	ANCHOR_WITHOUT_INDEX,

	/**
	 * A role on the type depends on itself through rules evaluated on historic versions. A role is
	 * only held where it can be derived without relying on itself, so the cycle alone never grants
	 * the role.
	 * @see CoverageFinding#getRuleIds()
	 */
	ROLE_CYCLE,

	/**
	 * A rule of the type navigates a historic reference backwards from an object that is always a
	 * current object. Such a step leads nowhere, so the rule never grants a role.
	 * @see CoverageFinding#getRuleIds()
	 */
	ANCHOR_STEP_FROM_CURRENT,

	/**
	 * All rules of the type grant roles on historic versions only. The rules switch off the default
	 * for composition parts, but nobody holds a role on a current object of the type.
	 */
	NO_CURRENT_ROLE_SOURCE,

	/**
	 * A rule of the type inherits a role from a rule whose result is not kept in the security
	 * storage. The rule is therefore not stored either and evaluated at each access check.
	 * @see CoverageFinding#getRuleIds()
	 */
	RULE_NOT_STORED,

	/**
	 * A rule of the type that is evaluated on historic versions navigates a reference backwards
	 * that may hold current objects and historic versions. What such a step finds from a historic
	 * version is not defined for the access check.
	 * @see CoverageFinding#getRuleIds()
	 */
	MIXED_BACKWARDS_STEP;

}
