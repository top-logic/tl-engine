/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the React view self-service (forgot-password) flow.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Forgot password?
	 */
	public static ResKey FORGOT_PASSWORD_COMMAND;

	/**
	 * @en If an account with that name exists, a verification code has been sent to its email
	 *     address.
	 */
	public static ResKey RESET_CODE_SENT;

	/**
	 * @en Please enter your user name.
	 */
	public static ResKey RESET_MISSING_USERNAME;

	/**
	 * @en The verification code is not valid or has expired. Please request a new one.
	 */
	public static ResKey RESET_CODE_INVALID;

	/**
	 * @en The entered passwords do not match.
	 */
	public static ResKey PASSWORD_MISMATCH;

	/**
	 * @en Please enter a new password.
	 */
	public static ResKey PASSWORD_EMPTY;

	/**
	 * @en Your password has been changed. You can now log in with the new password.
	 */
	public static ResKey RESET_PASSWORD_SUCCESS;

	/**
	 * @en The password could not be reset.
	 */
	public static ResKey RESET_PASSWORD_FAILED;

	/**
	 * @en Reset password of "{0}".
	 */
	public static ResKey1 RESET_PASSWORD__USER;

	static {
		initConstants(I18NConstants.class);
	}
}
