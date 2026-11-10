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
 * Internationalization constants for the React view self-service flows (forgotten password,
 * acceptance of an invitation, registration).
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

	/**
	 * @en A verification code has been sent to the e-mail address of the invitation.
	 */
	public static ResKey INVITATION_CODE_SENT;

	/**
	 * @en Please enter your surname.
	 */
	public static ResKey SURNAME_EMPTY;

	/**
	 * @en Please enter your e-mail address.
	 */
	public static ResKey EMAIL_EMPTY;

	/**
	 * @en If there is no account for {0} yet, we have sent a verification code to this address.
	 */
	public static ResKey1 REGISTRATION_CODE_SENT__EMAIL;

	/**
	 * @en Your account has been created and awaits the approval of an administrator.
	 */
	public static ResKey REGISTRATION_AWAITS_APPROVAL;

	static {
		initConstants(I18NConstants.class);
	}
}
