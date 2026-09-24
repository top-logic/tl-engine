/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @see com.top_logic.basic.util.ResPrefix
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The type "{0}" has neither a role rule nor a role parent rule, and no composition holds
	 *     its objects. No user can hold a role on them, therefore every access is denied. If the
	 *     type is used by the application's code only, declare it internal in the access rights
	 *     configuration.
	 */
	public static ResKey1 NO_ROLE_SOURCE__TYPE;

	/**
	 * @en The type "{0}" has neither a role rule nor a role parent rule, and no composition holds
	 *     its objects. They only inherit the roles of the security root, because the global default
	 *     security parent is active.
	 */
	public static ResKey1 NO_ROLE_SOURCE_ROOT_FALLBACK__TYPE;

	/**
	 * @en No role is granted the read operation on the type "{0}". Its objects are inaccessible to
	 *     every user. If the type is used by the application's code only, declare it internal in
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
	 * @en The type "{0}" has an access parent, so the rules "{1}" applying to it have no effect.
	 *     Remove the rules, or remove the access parent.
	 */
	public static ResKey2 SHADOWED_RULES__TYPE_RULES;

	static {
		initConstants(I18NConstants.class);
	}
}
