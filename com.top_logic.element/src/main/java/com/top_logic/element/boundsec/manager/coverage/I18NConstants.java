/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @see com.top_logic.layout.ResPrefix
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The type "{0}" has neither a role rule nor a role parent rule, and no composition holds
	 *     its objects. No user can hold a role on them, therefore every access is denied. If the
	 *     type is used by the application's code only, declare it internal in the access rights
	 *     configuration.
	 */
	public static ResKey1 NO_ROLE_SOURCE__TYPE;

	/**
	 * @en No role is granted an operation on the type "{0}" or on one of its attributes. Its objects
	 *     are inaccessible to every user. If the type is used by the application's code only, declare it internal in
	 *     the access rights configuration.
	 */
	public static ResKey1 NO_READ_GRANT__TYPE;

	/**
	 * @en The operation "{1}" is granted on the type "{0}" to the roles "{2}". No rule can deliver
	 *     these roles on the type or on one of its role parents, therefore the grant never takes
	 *     effect.
	 */
	public static ResKey3 DEAD_GRANT__TYPE_OPERATION_ROLES;

	/**
	 * @en The operation "{1}" is granted on the type "{0}" to the roles "{2}". An object of the type
	 *     is created in a context: on the security root, in a container holding it in a
	 *     composition, or in an object it references, an object of one of the types "{3}". No rule
	 *     can deliver these roles on such a context or on one of its role parents, therefore the
	 *     grant never takes effect.
	 */
	public static ResKey4 DEAD_CREATE_GRANT__TYPE_OPERATION_ROLES_CONTEXTS;

	/**
	 * @en The type "{0}" has an access parent, so the rules "{1}" applying to it have no effect.
	 *     Remove the rules, or remove the access parent.
	 */
	public static ResKey2 SHADOWED_RULES__TYPE_RULES;

	/**
	 * @en The operation "{1}" is granted on the type "{0}" to the roles "{2}", but the access parent
	 *     of the type decides it alone, therefore the grant has no effect. Add "Own decision" as a
	 *     candidate of "Any of" to the access parent, or revoke the grant.
	 */
	public static ResKey3 IGNORED_GRANT__TYPE_OPERATION_ROLES;

	/**
	 * @en The operation "{1}" is declared for the type "{0}", but no role holds it there: it is
	 *     granted to no role, neither directly nor through an operation implying it. Every check
	 *     of the operation on an object of the type is refused.
	 */
	public static ResKey2 UNGRANTED_OPERATION__TYPE_OPERATION;

	/**
	 * @en The operation "{1}" does not apply to the type "{0}", but "{2}" check it on an object of
	 *     the type. The check is refused for every user. Declare the operation for the type, or
	 *     check another operation.
	 */
	public static ResKey3 INAPPLICABLE_OPERATION__TYPE_OPERATION_USERS;

	/**
	 * @en The operation "{0}" checked by "{1}" is no known command group. Register the command
	 *     group, or check another operation.
	 */
	public static ResKey2 UNKNOWN_OPERATION__OPERATION_USERS;

	/**
	 * @en The operation "{1}" is granted on the type "{0}" to the roles "{2}", but no view,
	 *     command, form or privileged script checks it - nor an operation it implies - on objects
	 *     of the type. The grant has no effect: remove it, or add the check that should use it.
	 */
	public static ResKey3 UNCHECKED_GRANT__TYPE_OPERATION_ROLES;

	/**
	 * @en The rules "{1}" of the type "{0}" grant roles on historic versions that are covered through
	 *     the references "{2}". No object of the type can be covered by them: the type shares no
	 *     instances with the target type of such a reference and is no composition part of such a
	 *     type. The rules never grant a role.
	 */
	public static ResKey3 ANCHOR_NEVER_COVERS__TYPE_RULES_REFERENCES;

	/**
	 * @en The rules "{1}" of the type "{0}" use the historic reference "{2}" as anchor. The reference
	 *     is stored in a column without an index, so each search for the anchors of a version scans
	 *     the whole table. Store the reference in a link table.
	 */
	public static ResKey3 ANCHOR_WITHOUT_INDEX__TYPE_RULES_REFERENCE;

	/**
	 * @en The role "{1}" on the type "{0}" depends on itself through the rules "{2}". A role is only
	 *     held where it can be derived without relying on itself, so the cycle alone never grants
	 *     the role.
	 */
	public static ResKey3 ROLE_CYCLE__TYPE_ROLE_RULES;

	/**
	 * @en The rules "{1}" of the type "{0}" navigate a historic reference backwards from an object
	 *     that is always a current object. Such a step leads nowhere, so the rules never grant a
	 *     role.
	 */
	public static ResKey2 ANCHOR_STEP_FROM_CURRENT__TYPE_RULES;

	/**
	 * @en All rules of the type "{0}" grant roles on historic versions only. They switch off the
	 *     default for composition parts, but nobody holds a role on a current object of the type.
	 *     Add a rule for the current objects.
	 */
	public static ResKey1 NO_CURRENT_ROLE_SOURCE__TYPE;

	/**
	 * @en The rule "{1}" of the type "{0}" inherits a role from the rule "{2}", whose result is not
	 *     kept in the security storage. The rule is therefore not stored either and evaluated at
	 *     each access check.
	 */
	public static ResKey3 RULE_NOT_STORED__TYPE_RULE_SOURCE;

	/**
	 * @en The rules "{1}" of the type "{0}" are evaluated on historic versions and navigate the
	 *     reference "{2}" backwards, which may hold current objects and historic versions. What such
	 *     a step finds from a historic version is not defined for the access check. Use a historic
	 *     reference instead.
	 */
	public static ResKey3 MIXED_BACKWARDS_STEP__TYPE_RULES_REFERENCE;

	static {
		initConstants(I18NConstants.class);
	}
}
