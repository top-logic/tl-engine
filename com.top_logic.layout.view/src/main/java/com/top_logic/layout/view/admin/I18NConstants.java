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

	static {
		initConstants(I18NConstants.class);
	}
}
