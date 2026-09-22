/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The type "{0}" has neither a role rule nor a security parent rule. No user can hold a
	 *     role on its objects, therefore every access is denied.
	 */
	public static ResKey1 NO_ROLE_SOURCE__TYPE;

	/**
	 * @en The type "{0}" has neither a role rule nor a security parent rule. Its objects only
	 *     inherit the roles of the security root, because the global default security parent is
	 *     active.
	 */
	public static ResKey1 NO_ROLE_SOURCE_ROOT_FALLBACK__TYPE;

	/**
	 * @en No role is granted the read operation on the type "{0}". Its objects are invisible for
	 *     every user that does not bypass the access check.
	 */
	public static ResKey1 NO_READ_GRANT__TYPE;

	/**
	 * @en The operation "{1}" is granted on the type "{0}" to the roles "{2}". No rule can deliver
	 *     these roles on the type or on one of its security parents, therefore the grant never
	 *     takes effect.
	 */
	public static ResKey3 DEAD_GRANT__TYPE_OPERATION_ROLES;

	/**
	 * @en The type "{0}" has no role source and is contained in exactly one composition, "{1}". A
	 *     security parent rule navigating that composition backwards is proposed.
	 */
	public static ResKey2 SUGGESTED_PARENT__TYPE_REFERENCE;

	/**
	 * @en The type "{0}" has no role source and is contained in more than one composition: "{1}".
	 *     The container to use as security parent must be chosen explicitly.
	 */
	public static ResKey2 AMBIGUOUS_PARENT__TYPE_REFERENCES;

	static {
		initConstants(I18NConstants.class);
	}
}
