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

	/** @en A code was sent to {0}. Please check your inbox and enter the sent code here. */
	public static ResKey1 MESSAGE_CODE_SENT__MAIL;

	/** @en The code is expired. Please request a new code. */
	public static ResKey ERROR_CODE_EXPIRED;

	/** @en The code is incorrect. */
	public static ResKey ERROR_CODE_MISMATCH;

	/** @en Check code */
	public static ResKey CHECK_INVITATION_CODE_TITLE;

	/** @en Request code again */
	public static ResKey REQUEST_CODE;

	/** @en The code cannot be requested at this time. Please wait {0} seconds and try again. */
	public static ResKey1 REQUEST_CODE_NOT_ALLOWED__TIMEOUT;

	/** @en The code has been entered incorrectly too many times. Please request a new code. */
	public static ResKey ERROR_CODE_MISMATCH_TOO_MANY_TIMES;

	/** @en Welcome to the {0} app. */
	public static ResKey1 MESSAGE_WELCOME_TO_APPLICATION__APPLICATION;

	/** @en Code */
	public static ResKey CODE_FIELD_LABEL;

	/** Prefix for elements displayed in {@link CreateAccountDialog}. */
	public static ResPrefix CREATE_ACCOUNT_DIALOG;

	static {
		initConstants(I18NConstants.class);
	}
}
