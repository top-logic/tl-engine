/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
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
	 * @en Type
	 */
	public static ResKey COVERAGE_COLUMN_TYPE;

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
	 * @en Role parents
	 */
	public static ResKey COVERAGE_COLUMN_ROLE_PARENTS;

	/**
	 * @en Access parent
	 */
	public static ResKey COVERAGE_COLUMN_ACCESS_PARENT;

	/**
	 * @en Findings
	 */
	public static ResKey COVERAGE_COLUMN_FINDINGS;

	/**
	 * @en Role parent
	 */
	public static ResKey COVERAGE_RULE_KIND_ROLE_PARENT;

	/**
	 * @en Role rule
	 */
	public static ResKey COVERAGE_RULE_KIND_ROLE_RULE;

	/**
	 * @en New
	 */
	public static ResKey COVERAGE_RULE_STATE_NEW;

	/**
	 * @en Changed
	 */
	public static ResKey COVERAGE_RULE_STATE_CHANGED;

	/**
	 * @en Deleted
	 */
	public static ResKey COVERAGE_RULE_STATE_DELETED;

	/**
	 * @en Please select the type to edit the access definition of.
	 */
	public static ResKey ERROR_NO_TYPE_SELECTED;

	/**
	 * @en Please select the module to edit the access definition of.
	 */
	public static ResKey ERROR_NO_MODULE_SELECTED;

	/**
	 * @en There is no rule to work on.
	 */
	public static ResKey ERROR_NO_RULE_SELECTED;

	/**
	 * @en There is no role parent rule with the id "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_ROLE_PARENT_RULE__ID;

	/**
	 * @en There is no role rule with the id "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_ROLE_RULE__ID;

	/**
	 * @en Please enter at least one step leading to the role parent.
	 */
	public static ResKey ERROR_MISSING_RULE_PATH;

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
	public static ResKey1 ERROR_ACCESS_RIGHTS_INVALID__PROBLEM;

	/**
	 * @en No role source: neither a role rule nor a role parent rule applies to the type, and no
	 *     composition holds its objects, so no user can hold a role on them and every access is
	 *     denied.
	 */
	public static ResKey COVERAGE_PROBLEM_NO_ROLE_SOURCE;

	/**
	 * @en No read grant: no role is granted any operation on the type or on one of its attributes,
	 *     so its objects are inaccessible to every user.
	 */
	public static ResKey COVERAGE_PROBLEM_NO_READ_GRANT;

	/**
	 * @en Dead grant: the operation "{0}" is granted to the roles {1}, but no rule delivers any of
	 *     them on the type or on its role parents, so the grant never takes effect.
	 */
	public static ResKey2 COVERAGE_PROBLEM_DEAD_GRANT__OPERATION_ROLES;

	/**
	 * @en Make the target of a to-one reference of the type its access parent, so that the objects
	 *     delegate every access decision to the object the reference points to ("Access
	 *     rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_ACCESS_PARENT;

	/**
	 * @en Define a role parent rule, so that the objects inherit the roles of the object the
	 *     rule leads to ("Role parent…").
	 */
	public static ResKey COVERAGE_SOLUTION_ROLE_PARENT_RULE;

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
	 * @en Grant the read operation to a role on the type or one of its attributes, or on its module
	 *     ("Access rights…", "Module access rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_READ_GRANT;

	/**
	 * @en Add a rule delivering one of the granted roles on the type or on its role parent
	 *     ("Role rule…", "Role parent…").
	 */
	public static ResKey COVERAGE_SOLUTION_DELIVER_ROLE;

	/**
	 * @en Grant the operation to a role that is delivered on the type instead, or drop the grant
	 *     ("Access rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_CHANGE_GRANT;

	/**
	 * @en Shadowed rules: the type has an access parent, so the rules {0} applying to it have no
	 *     effect.
	 */
	public static ResKey1 COVERAGE_PROBLEM_SHADOWED_RULES__RULES;

	/**
	 * @en Delete the shadowed rules, or restrict them to the types that decide for themselves
	 *     ("Edit rule…", "Delete rule").
	 */
	public static ResKey COVERAGE_SOLUTION_REMOVE_SHADOWED_RULES;

	/**
	 * @en Drop the access parent of the type, so that the rules and its grants decide again
	 *     ("Access rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_REMOVE_ACCESS_PARENT;

	/**
	 * @en Ungranted operation: the operation "{0}" is declared for the type, but no role holds it
	 *     there, so every check of it on an object of the type is refused.
	 */
	public static ResKey1 COVERAGE_PROBLEM_UNGRANTED_OPERATION__OPERATION;

	/**
	 * @en Grant the operation to a role on the type ("Grants" tab), or declare it implied by an
	 *     operation the roles hold ("Access rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_GRANT_OPERATION;

	/**
	 * @en Inapplicable operation: the operation "{0}" does not apply to the type, but {1} check it
	 *     on an object of the type, so the check is refused for every user.
	 */
	public static ResKey2 COVERAGE_PROBLEM_INAPPLICABLE_OPERATION__OPERATION_USERS;

	/**
	 * @en Declare the operation for the type, one of its generalizations or its module
	 *     ("Access rights…").
	 */
	public static ResKey COVERAGE_SOLUTION_DECLARE_OPERATION;

	/**
	 * @en Unknown operation: the operation "{0}" checked by {1} is no registered command group.
	 */
	public static ResKey2 COVERAGE_PROBLEM_UNKNOWN_OPERATION__OPERATION_USERS;

	/**
	 * @en Register the command group in the configuration of the command group registry.
	 */
	public static ResKey COVERAGE_SOLUTION_REGISTER_OPERATION;

	/**
	 * @en Check another operation at the places listed.
	 */
	public static ResKey COVERAGE_SOLUTION_CHECK_OTHER_OPERATION;

	/**
	 * @en Unchecked grant: the operation "{0}" is granted to the roles {1}, but no view, command,
	 *     form or privileged script checks it - nor an operation it implies - on objects of the
	 *     type, so the grant has no effect.
	 */
	public static ResKey2 COVERAGE_PROBLEM_UNCHECKED_GRANT__OPERATION_ROLES;

	/**
	 * @en Remove the grant ("Grants" tab, or the access rights configuration of the type, its
	 *     generalization or its module that grants it).
	 */
	public static ResKey COVERAGE_SOLUTION_REMOVE_UNCHECKED_GRANT;

	/**
	 * @en Add the check that should use the operation: an access control of a view unit, the
	 *     access of a command, the edit operation of a form, or the command group of a privileged
	 *     script.
	 */
	public static ResKey COVERAGE_SOLUTION_ADD_CHECK;

	/**
	 * @en Operation uses without a checked type
	 */
	public static ResKey COVERAGE_UNATTACHED_GROUP;

	/**
	 * @en The type delegates every access decision to its access parent, the object reached through
	 *     {0}: whether a user may read, write or export an object of the type is whether the user
	 *     may do the same to that object, and creating or deleting one is writing it. The type needs
	 *     no grants and no roles of its own.
	 */
	public static ResKey1 COVERAGE_DELEGATED__PARENT;

	/**
	 * @en The type delegates every access decision to its container, whichever composition holds
	 *     the object ({0}): whether a user may read, write or export an object of the type is
	 *     whether the user may do the same to its container, and creating or deleting one is
	 *     writing the container. This is the default for a composition part without a role rule
	 *     and without a role parent rule; the type needs no grants and no roles of its own.
	 */
	public static ResKey1 COVERAGE_DELEGATED_DEFAULT__CONTAINERS;

	/**
	 * @en The type delegates every access decision to its container, whichever composition holds
	 *     the object ({0}): whether a user may read, write or export an object of the type is
	 *     whether the user may do the same to its container, and creating or deleting one is
	 *     writing the container. The type needs no grants and no roles of its own.
	 */
	public static ResKey1 COVERAGE_DELEGATED_CONTAINER__CONTAINERS;

	/**
	 * @en The type is marked as internal: it is used by the application code alone, so it is exempt
	 *     from the check and no user gets access to its objects.
	 */
	public static ResKey COVERAGE_EXEMPT_INTERNAL;

	/**
	 * @en The type is internal by the mark of "{0}": it is used by the application code alone, so
	 *     it is exempt from the check and no user gets access to its objects. The mark can be dropped
	 *     there only, not for this type alone.
	 */
	public static ResKey1 COVERAGE_EXEMPT_INTERNAL_INHERITED__ORIGIN;

	/**
	 * @en The type is excluded from access control: every user may access its objects, so it is
	 *     exempt from the check.
	 */
	public static ResKey COVERAGE_EXEMPT_WITHOUT_SECURITY;

	/**
	 * @en The type is excluded from access control by the mark of "{0}": every user may access its
	 *     objects, so it is exempt from the check. The mark can be dropped there only, not for this
	 *     type alone.
	 */
	public static ResKey1 COVERAGE_EXEMPT_WITHOUT_SECURITY_INHERITED__ORIGIN;

	/**
	 * @en The access definition could not be written to the file "{0}".
	 */
	public static ResKey1 ERROR_WRITING_ACCESS_DEFINITION__FILE;

	/**
	 * @en The access definition could not be read from the file "{0}".
	 */
	public static ResKey1 ERROR_READING_ACCESS_DEFINITION__FILE;

	/**
	 * @en The type delegates every access decision to its access parent "{0}", so it has no grants
	 *     of its own. The grants of the type the access parent leads to decide.
	 */
	public static ResKey1 GRANTS_DELEGATED__PARENT;

	/**
	 * @en The type is excluded from access control or used by the application code only, so it
	 *     has no grants.
	 */
	public static ResKey GRANTS_EXEMPT;

	/**
	 * @en Checked on objects of the type by: {0}
	 */
	public static ResKey1 GRANTS_CHECKED_BY__USERS;

	/**
	 * @en Checked on objects whose type is not declared by: {0}
	 */
	public static ResKey1 GRANTS_UNDECLARED__USERS;

	/**
	 * @en No view, command or privileged script of the application checks the operation on
	 *     objects of the type.
	 */
	public static ResKey GRANTS_NOT_CHECKED;

	/**
	 * @en Held through "{0}", which implies it. Change the grant of that operation.
	 */
	public static ResKey1 GRANTS_IMPLIED__OPERATIONS;

	/**
	 * @en Held through "{0}" on the attributes {1}, which implies it. Change the grants of these
	 *     attributes.
	 */
	public static ResKey2 GRANTS_IMPLIED_BY_ATTRIBUTES__OPERATION_ATTRIBUTES;

	/**
	 * @en Not restricted: the rights on the object decide. Granting the operation to a role
	 *     restricts the attribute to the roles it is granted to; revoking it from the last of them
	 *     lifts the restriction again.
	 */
	public static ResKey GRANTS_UNRESTRICTED;

	/**
	 * @en Stored, but not yet in effect: the change takes effect with "Apply configuration".
	 */
	public static ResKey GRANTS_PENDING;

	/**
	 * @en No rule assigns this role on objects of the type, and it is assigned to none of them
	 *     directly, so its grants never take effect. Revoke them, or define a rule assigning the
	 *     role ("Coverage" tab).
	 */
	public static ResKey GRANTS_INEFFECTIVE;

	/**
	 * @en Choose an account to see the access it has.
	 */
	public static ResKey EFFECTIVE_ACCESS_NO_ACCOUNT;

	/**
	 * @en Listed are all roles the account holds on any object. Choose a type and one of its objects
	 *     to see which operations the account may perform on that object and why.
	 */
	public static ResKey EFFECTIVE_ACCESS_NO_OBJECT;

	/**
	 * @en "{0}" is an administrator and may perform every operation on every object, whatever roles
	 *     the account holds.
	 */
	public static ResKey1 EFFECTIVE_ACCESS_ADMINISTRATOR__ACCOUNT;

	/**
	 * @en "{0}" is a restricted user and must not perform any operation that modifies or deletes,
	 *     whatever roles the account holds.
	 */
	public static ResKey1 EFFECTIVE_ACCESS_RESTRICTED_USER__ACCOUNT;

	/**
	 * @en The type "{0}" is without access control: every account may perform every operation on
	 *     its objects.
	 */
	public static ResKey1 EFFECTIVE_ACCESS_WITHOUT_SECURITY__TYPE;

	/**
	 * @en The type "{0}" is marked internal: its objects are not meant to be accessed on behalf of an
	 *     account.
	 */
	public static ResKey1 EFFECTIVE_ACCESS_INTERNAL__TYPE;

	/**
	 * @en The type "{0}" delegates its access decisions to an access parent: the grants of the type
	 *     of "{1}" and the roles the account holds on "{1}" decide.
	 */
	public static ResKey2 EFFECTIVE_ACCESS_DELEGATED__TYPE_OBJECT;

	/**
	 * @en The access parents of the object lead to no object deciding its access, or run in a
	 *     cycle, so no role grants an operation on it.
	 */
	public static ResKey EFFECTIVE_ACCESS_NO_DECIDING_OBJECT;

	/**
	 * @en The security storage of the access manager is not available - the access manager keeps
	 *     none, or it is being rebuilt. The decisions and the held roles are shown without their
	 *     sources, and the role assignments of an account cannot be listed.
	 */
	public static ResKey EFFECTIVE_ACCESS_SOURCES_UNAVAILABLE;

	/**
	 * @en Allowed through: {0}.
	 */
	public static ResKey1 EFFECTIVE_ACCESS_ALLOWED_BY_ROLES__ROLES;

	/**
	 * @en Allowed, because the account is an administrator.
	 */
	public static ResKey EFFECTIVE_ACCESS_ALLOWED_AS_ADMINISTRATOR;

	/**
	 * @en Allowed, because the type is without access control.
	 */
	public static ResKey EFFECTIVE_ACCESS_ALLOWED_WITHOUT_SECURITY;

	/**
	 * @en Allowed by the access check, although the account holds none of the admitting roles.
	 */
	public static ResKey EFFECTIVE_ACCESS_ALLOWED_OTHERWISE;

	/**
	 * @en Denied, because the account is a restricted user and the operation modifies or deletes.
	 */
	public static ResKey EFFECTIVE_ACCESS_DENIED_RESTRICTED;

	/**
	 * @en Denied: no role is granted the operation on the deciding object.
	 */
	public static ResKey EFFECTIVE_ACCESS_DENIED_NOT_GRANTED;

	/**
	 * @en Denied, admitting roles not held: {0}.
	 */
	public static ResKey1 EFFECTIVE_ACCESS_DENIED_NO_ROLE__ROLES;

	/**
	 * @en Denied by the access check, although held: {0}.
	 */
	public static ResKey1 EFFECTIVE_ACCESS_DENIED_OTHERWISE__ROLES;

	/**
	 * @en Checked as "{0}" on "{1}". {2}
	 */
	public static ResKey3 EFFECTIVE_ACCESS_CHECKED_ON__OPERATION_OBJECT_REASON;

	/**
	 * @en Inherited from the role parent "{0}".
	 */
	public static ResKey1 EFFECTIVE_ACCESS_INHERITED__PARENT;

	static {
		initConstants(I18NConstants.class);
	}
}
