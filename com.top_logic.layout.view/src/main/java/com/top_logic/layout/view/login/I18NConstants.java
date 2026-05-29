/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the React view login UI.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Please enter user name and password.
	 */
	public static ResKey LOGIN_MISSING_CREDENTIALS;

	/**
	 * @en Login failed. Please check your credentials.
	 */
	public static ResKey LOGIN_FAILED;

	/**
	 * @en Your password has expired. Please choose a new password.
	 */
	public static ResKey PASSWORD_CHANGE_REQUIRED;

	/**
	 * @en The entered passwords do not match.
	 */
	public static ResKey PASSWORD_MISMATCH;

	/**
	 * @en Please enter a new password.
	 */
	public static ResKey PASSWORD_EMPTY;

	/**
	 * @en Changed password of "{0}".
	 */
	public static ResKey1 CHANGED_PASSWORD__USER;

	/**
	 * @en No account selected to expire the password for.
	 */
	public static ResKey EXPIRE_PASSWORD_NO_ACCOUNT;

	/**
	 * @en Cannot expire the password of this account.
	 */
	public static ResKey EXPIRE_PASSWORD_NOT_SUPPORTED;

	/**
	 * @en The password of "{0}" has been expired.
	 */
	public static ResKey1 EXPIRE_PASSWORD_DONE__USER;

	static {
		initConstants(I18NConstants.class);
	}
}
