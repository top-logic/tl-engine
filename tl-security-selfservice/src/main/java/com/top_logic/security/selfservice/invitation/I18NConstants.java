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

	/** @en Invitation token updated. */
	public static ResKey UPDATED_INVITATION_TOKEN;

	/** @en Token mismatch counter updated. */
	public static ResKey UPDATED_MISMATCH_COUNTER;

	/** @en Deleted invitation. */
	public static ResKey DELETED_INVITATION;

	/** @en A token was sent to {0}. Please check your inbox and enter the sent token here. */
	public static ResKey1 MESSAGE_TOKEN_SENT__MAIL;

	/** @en The token is expired. Please request a new token. */
	public static ResKey ERROR_TOKEN_EXPIRED;

	/** @en The token is incorrect. */
	public static ResKey ERROR_TOKEN_MISMATCH;

	/** @en Check token */
	public static ResKey CHECK_INVITATION_TOKEN_TITLE;

	/** @en Request token again */
	public static ResKey REQUEST_TOKEN;

	/** @en The token cannot be requested at this time. Please wait {0} seconds and try again. */
	public static ResKey1 REQUEST_TOKEN_NOT_ALLOWED__TIMEOUT;

	/** @en The token has been entered incorrectly too many times. Please request a new token. */
	public static ResKey ERROR_TOKEN_MISMATCH_TOO_MANY_TIMES;

	/** @en Welcome to the {0} app. */
	public static ResKey1 MESSAGE_WELCOME_TO_APPLICATION__APPLICATION;

	/** @en Token */
	public static ResKey TOKEN_FIELD_LABEL;

	/** Prefix for elements displayed in {@link CreateAccountDialog}. */
	public static ResPrefix CREATE_ACCOUNT_DIALOG;

	static {
		initConstants(I18NConstants.class);
	}
}
