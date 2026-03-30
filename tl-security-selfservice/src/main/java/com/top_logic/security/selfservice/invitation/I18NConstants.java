/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No invitation link could be found for the given ID {0}. The invitation may have expired
	 *     or been withdrawn.
	 */
	public static ResKey1 ERROR_NO_INVITATION__ID;

	/** @en Create account */
	public static ResKey CREATE_LOGIN_DIALOG_TITLE;

	/** @en Invitation code updated. */
	public static ResKey UPDATED_INVITATION_CODE;

	/** @en Deleted invitation. */
	public static ResKey DELETED_INVITATION;

	/** @en No valid code available. Please request a new code. */
	public static ResKey ERROR_NO_VALID_CODE;

	/** @en The code is incorrect. */
	public static ResKey ERROR_CODE_MISMATCH;

	/** @en Check code */
	public static ResKey CHECK_INVITATION_CODE_TITLE;

	/** @en Request verification code */
	public static ResKey REQUEST_CODE;

	/** @en The code cannot be requested at this time. Please wait {0} seconds and try again. */
	public static ResKey1 REQUEST_CODE_NOT_ALLOWED__TIMEOUT;

	/** @en The code has been entered incorrectly too many times. Please request a new code. */
	public static ResKey ERROR_CODE_MISMATCH_TOO_MANY_TIMES;

	/** @en Welcome to the {0} app. */
	public static ResKey1 MESSAGE_WELCOME_TO_APPLICATION__APPLICATION;

	/**
	 * @en Please request a verification code. It will be sent to you by email. Enter the code here
	 *     to begin setting up your access to the application.
	 */
	public static ResKey MESSAGE_REQUEST_VERIFICATION_CODE;

	/**
	 * @en The verification code has been sent by email.
	 */
	public static ResKey MESSAGE_VERIFICATION_CODE_SENT;

	/** @en Code */
	public static ResKey CODE_FIELD_LABEL;

	/** Prefix for elements displayed in {@link CreateAccountDialog}. */
	public static ResPrefix CREATE_ACCOUNT_DIALOG;

	/** @en Creation of account not possible. */
	public static ResKey CREATE_ACCOUNT_NOT_POSSIBLE;

	/** @en Forgot password? */
	public static ResKey FORGOT_PASSWORD_DIALOG_TITLE;

	/**
	 * @en Please enter your username and request a reset password code. A message containing the
	 *     requested code is send to your email account. Enter the code here to update your
	 *     password.
	 */
	public static ResKey FORGOT_PASSWORD_USERNAME_MESSAGE;

	/** @en Username */
	public static ResKey FORGOT_PASSWORD_USERNAME_FIELD;

	/** @en Reset password. */
	public static ResKey RESET_PASSWORD_COMMAND;

	/**
	 * @en A code to reset your password has been sent to the email address listed in your account.
	 *     For security reasons, this code is only valid for a limited time.
	 */
	public static ResKey PASSWORD_RESET_MAIL_SENT_MESSAGE;

	/**
	 * @en Your password has been successfully changed. You will now be logged out for security
	 *     reasons.
	 */
	public static ResKey PASSWORD_RESET_SUCCESS_MESSAGE;

	/** @en Reset multi-factor authentication */
	public static ResKey RESET_MFA_COMMAND;

	/** @en Reset multi-factor authentication */
	public static ResKey RESET_MFA_DIALOG_TITLE;

	/**
	 * @en If you would like to reset your multi-factor authentication, please request a reset code.
	 *     This will be sent to you via email. For security reasons, this code is only valid for
	 *     this session and for a limited time.
	 * 
	 *     Enter the code here and then click 'Ok'. For security reasons, you will then be logged
	 *     out. After logging in again, you can set up multi-factor authentication.
	 */
	public static ResKey RESET_MFA_MESSAGE;

	/** @en Code */
	public static ResKey RESET_MFA_CODE_FIELD;

	/** @en Multi-factor authentication code reset */
	public static ResKey RESET_MFA_CODE;

	/**
	 * @en Your multi-factor authentication code has been successfully reset. You will now be logged
	 *     out.
	 */
	public static ResKey RESET_MFA_SUCCESS_MESSAGE;

	static {
		initConstants(I18NConstants.class);
	}
}
