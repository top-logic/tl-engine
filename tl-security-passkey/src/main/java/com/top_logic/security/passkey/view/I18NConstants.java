/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the passkey elements of the React UI.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Passkey {0}
	 */
	public static ResKey1 PASSKEY_LABEL__DATE;

	/**
	 * @en The passkey request was cancelled or has timed out.
	 */
	public static ResKey ERROR_CANCELLED;

	/**
	 * @en This browser does not support passkeys.
	 */
	public static ResKey ERROR_NOT_SUPPORTED;

	/**
	 * @en Passkeys require a secure connection. Open the application via HTTPS (or on localhost).
	 */
	public static ResKey ERROR_INSECURE_CONTEXT;

	/**
	 * @en The browser or authenticator cannot handle this passkey request: {0}
	 */
	public static ResKey1 ERROR_REQUEST_NOT_SUPPORTED__MESSAGE;

	/**
	 * @en This authenticator already holds a passkey for the account.
	 */
	public static ResKey ERROR_ALREADY_ON_AUTHENTICATOR;

	/**
	 * @en Passkeys cannot be used on this address. Please contact your administrator.
	 */
	public static ResKey ERROR_WRONG_DOMAIN;

	/**
	 * @en The browser refused the passkey request ({0}).
	 */
	public static ResKey1 ERROR_BROWSER__NAME;

	/**
	 * @en The passkey request failed in the browser ({0}): {1}
	 */
	public static ResKey2 ERROR_BROWSER__NAME_MESSAGE;

	/**
	 * @en A passkey can only be created here for the account "{0}" when it sets up its second
	 *     factor during the login.
	 */
	public static ResKey1 ERROR_NOT_ENROLLING__ACCOUNT;

	/**
	 * @en A passkey can only be created for the account that is logged in.
	 */
	public static ResKey ERROR_NOT_CURRENT_USER;

	/**
	 * @en The passkey is the last second factor of the account "{0}", which is required to use one.
	 *     Set up another passkey or another second factor before deleting it.
	 */
	public static ResKey1 ERROR_LAST_SECOND_FACTOR__ACCOUNT;

	static {
		initConstants(I18NConstants.class);
	}
}
