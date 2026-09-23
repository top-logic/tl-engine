/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.admin} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Please enter a login name for the new account.
	 */
	public static ResKey ERROR_MISSING_LOGIN;

	/**
	 * @en An account with the login name "{0}" already exists.
	 */
	public static ResKey1 ERROR_ACCOUNT_EXISTS__LOGIN;

	/**
	 * @en No account selected to set the password for.
	 */
	public static ResKey ERROR_MISSING_ACCOUNT;

	/**
	 * @en Please enter a new password.
	 */
	public static ResKey ERROR_MISSING_PASSWORD;

	/**
	 * @en Changing the password is not supported by the configured authentication device.
	 */
	public static ResKey ERROR_PASSWORD_CHANGE_UNSUPPORTED;

	/**
	 * @en Scope
	 */
	public static ResKey MATRIX_SCOPE_COLUMN;

	/**
	 * @en Command group
	 */
	public static ResKey MATRIX_GROUP_COLUMN;

	/**
	 * @en Type
	 */
	public static ResKey COVERAGE_COLUMN_TYPE;

	/**
	 * @en Module
	 */
	public static ResKey COVERAGE_COLUMN_MODULE;

	/**
	 * @en Status
	 */
	public static ResKey COVERAGE_COLUMN_STATUS;

	/**
	 * @en Read roles
	 */
	public static ResKey COVERAGE_COLUMN_READ_ROLES;

	/**
	 * @en Role rules
	 */
	public static ResKey COVERAGE_COLUMN_ROLE_RULES;

	/**
	 * @en Security parents
	 */
	public static ResKey COVERAGE_COLUMN_SECURITY_PARENTS;

	/**
	 * @en Findings
	 */
	public static ResKey COVERAGE_COLUMN_FINDINGS;

	/**
	 * @en Security parent
	 */
	public static ResKey COVERAGE_RULE_KIND_SECURITY_PARENT;

	/**
	 * @en Role rule
	 */
	public static ResKey COVERAGE_RULE_KIND_ROLE_RULE;

	/**
	 * @en The rule "{0}" is defined by the base configuration. It can be overridden by a rule of
	 *     the same id, but not removed.
	 */
	public static ResKey1 ERROR_BASE_RULE_NOT_REMOVABLE__ID;

	/**
	 * @en Please select the type to edit the access definition of.
	 */
	public static ResKey ERROR_NO_TYPE_SELECTED;

	/**
	 * @en The analysis proposes no security parent rule for the selected type.
	 */
	public static ResKey ERROR_NO_PROPOSED_RULE;

	/**
	 * @en There is no rule to work on.
	 */
	public static ResKey ERROR_NO_RULE_SELECTED;

	/**
	 * @en There is no security parent rule with the id "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_SECURITY_PARENT_RULE__ID;

	/**
	 * @en There is no role rule with the id "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_ROLE_RULE__ID;

	/**
	 * @en Please enter an id naming the rule.
	 */
	public static ResKey ERROR_MISSING_RULE_ID;

	/**
	 * @en Please select the type the rule applies to.
	 */
	public static ResKey ERROR_MISSING_RULE_TYPE;

	/**
	 * @en Please enter at least one step leading to the security parent.
	 */
	public static ResKey ERROR_MISSING_RULE_PATH;

	/**
	 * @en Please select at least one role the rule grants.
	 */
	public static ResKey ERROR_MISSING_RULE_ROLE;

	/**
	 * @en There are no access rights to store.
	 */
	public static ResKey ERROR_NO_ACCESS_RIGHTS_SELECTED;

	/**
	 * @en Please select the model element the access rights apply to.
	 */
	public static ResKey ERROR_MISSING_ACCESS_RIGHTS_NAME;

	/**
	 * @en Please select the operation each rule applies to.
	 */
	public static ResKey ERROR_MISSING_GRANT_OPERATION;

	/**
	 * @en The access rights cannot be stored: {0}
	 */
	public static ResKey1 ERROR_ACCESS_RIGHTS_INVALID__ERRORS;

	/**
	 * @en No role source: neither a role rule nor a security parent rule applies to the type, so
	 *     no user can hold a role on its objects and every access is denied.
	 */
	public static ResKey COVERAGE_PROBLEM_NO_ROLE_SOURCE;

	/**
	 * @en No role source: neither a role rule nor a security parent rule applies to the type, so
	 *     its objects inherit the roles of the security root only, the global default security
	 *     parent being active.
	 */
	public static ResKey COVERAGE_PROBLEM_NO_ROLE_SOURCE_ROOT_FALLBACK;

	/**
	 * @en No read grant: no role is granted the read operation on the type, so its objects are
	 *     inaccessible to every user.
	 */
	public static ResKey COVERAGE_PROBLEM_NO_READ_GRANT;

	/**
	 * @en Dead grant: the operation "{0}" is granted to the roles {1}, but no rule delivers any of
	 *     them on the type or on its security parents, so the grant never takes effect.
	 */
	public static ResKey2 COVERAGE_PROBLEM_DEAD_GRANT__OPERATION_ROLES;

	/**
	 * @en Accept the proposal: the object containing an object of the type through the composition
	 *     {0} becomes its security parent ("Accept proposal").
	 */
	public static ResKey1 COVERAGE_SOLUTION_ACCEPT_PROPOSAL__REFERENCE;

	/**
	 * @en Choose the container: the type is contained through several compositions, {0}; a security
	 *     parent rule must name the one to navigate ("Security parent…").
	 */
	public static ResKey1 COVERAGE_SOLUTION_CHOOSE_PARENT__REFERENCES;

	/**
	 * @en Define a security parent rule, so that the objects inherit the roles of the object the
	 *     rule leads to ("Security parent…").
	 */
	public static ResKey COVERAGE_SOLUTION_SECURITY_PARENT_RULE;

	/**
	 * @en Define a role rule assigning users a role on the objects directly ("Role rule…").
	 */
	public static ResKey COVERAGE_SOLUTION_ROLE_RULE;

	/**
	 * @en Mark the type internal, if the application code alone uses it and no user needs access
	 *     to its objects ("Mark internal").
	 */
	public static ResKey COVERAGE_SOLUTION_MARK_INTERNAL;

	/**
	 * @en Grant the read operation to a role on the type or on its module ("Access rights…",
	 *     "Module access rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_READ_GRANT;

	/**
	 * @en Add a rule delivering one of the granted roles on the type or on its security parent
	 *     ("Role rule…", "Security parent…").
	 */
	public static ResKey COVERAGE_SOLUTION_DELIVER_ROLE;

	/**
	 * @en Grant the operation to a role that is delivered on the type instead, or drop the grant
	 *     ("Access rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_CHANGE_GRANT;

	/**
	 * @en The type is marked as internal: it is used by the application code alone, so it is exempt
	 *     from the check and no user gets access to its objects.
	 */
	public static ResKey COVERAGE_EXEMPT_INTERNAL;

	/**
	 * @en The type is excluded from access control: every user may access its objects, so it is
	 *     exempt from the check.
	 */
	public static ResKey COVERAGE_EXEMPT_WITHOUT_SECURITY;

	/**
	 * @en The access definition could not be written to the file "{0}".
	 */
	public static ResKey1 ERROR_WRITING_ACCESS_DEFINITION__FILE;

	/**
	 * @en The access definition could not be read from the file "{0}".
	 */
	public static ResKey1 ERROR_READING_ACCESS_DEFINITION__FILE;

	static {
		initConstants(I18NConstants.class);
	}
}
