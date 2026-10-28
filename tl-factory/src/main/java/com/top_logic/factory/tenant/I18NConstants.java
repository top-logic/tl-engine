/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.tenant;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en "{0}" is already a member of the tenant "{1}"; a user is a member of one tenant only.
	 */
	public static ResKey2 ERROR_MEMBER_OF_OTHER_TENANT__USER_TENANT;

	/**
	 * @en You may not create a tenant.
	 */
	public static ResKey ERROR_NO_TENANT_CREATION;

	/**
	 * @en "{0}" is no member of the tenant "{1}".
	 */
	public static ResKey2 ERROR_NOT_A_MEMBER__USER_TENANT;

	/**
	 * @en The tenant "{0}" needs an administrator: make another member an administrator first.
	 */
	public static ResKey1 ERROR_LAST_ADMINISTRATOR__TENANT;

	/**
	 * @en You cannot remove yourself from the tenant.
	 */
	public static ResKey ERROR_REMOVE_SELF;

	/**
	 * @en Created tenant "{0}".
	 */
	public static ResKey1 CREATED_TENANT__TENANT;

	/**
	 * @en Created member "{0}" of tenant "{1}".
	 */
	public static ResKey2 CREATED_MEMBER__USER_TENANT;

	/**
	 * @en Changed the role of "{0}" in tenant "{1}".
	 */
	public static ResKey2 CHANGED_ROLE__USER_TENANT;

	/**
	 * @en Removed member "{0}" from tenant "{1}" and deleted the account.
	 */
	public static ResKey2 REMOVED_MEMBER__USER_TENANT;

	static {
		initConstants(I18NConstants.class);
	}
}
