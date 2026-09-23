/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
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
