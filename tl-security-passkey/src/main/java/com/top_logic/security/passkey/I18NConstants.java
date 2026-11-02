/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the passkeys of the accounts.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Passkeys are not allowed for the account "{0}".
	 */
	public static ResKey1 ERROR_PASSKEYS_NOT_ALLOWED__ACCOUNT;

	/**
	 * @en Passkeys are not set up for this installation. Please contact your administrator.
	 */
	public static ResKey ERROR_NOT_SET_UP;

	/**
	 * @en The passkey could not be registered: {0}
	 */
	public static ResKey1 ERROR_REGISTRATION_FAILED__REASON;

	/**
	 * @en The passkey needs a name.
	 */
	public static ResKey ERROR_LABEL_MISSING;

	/**
	 * @en The passkey is already registered.
	 */
	public static ResKey ERROR_ALREADY_REGISTERED;

	/**
	 * @en The login with the passkey failed: {0}
	 */
	public static ResKey1 ERROR_AUTHENTICATION_FAILED__REASON;

	/**
	 * @en The request for the passkey has expired or was already answered. Please try again.
	 */
	public static ResKey ERROR_REQUEST_EXPIRED;

	/**
	 * @en The data of the passkey request could not be processed.
	 */
	public static ResKey ERROR_INVALID_CEREMONY_DATA;

	/**
	 * @en The passkey was used on the address {0}, for which passkeys are not set up in this
	 *     application. Open the application at its configured address or contact your
	 *     administrator.
	 */
	public static ResKey1 ERROR_WRONG_ORIGIN__ORIGIN;

	/**
	 * @en This passkey is not registered with this application. It may not have been set up
	 *     completely, may have been deleted, or may belong to another installation. Choose another
	 *     passkey or log in with your password.
	 */
	public static ResKey ERROR_UNKNOWN_PASSKEY;

	/**
	 * @en This passkey does not belong to the account "{0}". Choose a passkey of this account.
	 */
	public static ResKey1 ERROR_OTHER_ACCOUNT__ACCOUNT;

	/**
	 * @en The authenticator did not verify you (PIN, fingerprint, face), which passkeys require
	 *     here. Allow the verification and try again.
	 */
	public static ResKey ERROR_USER_NOT_VERIFIED;

	/**
	 * @en The usage counter of the passkey did not increase, which points to a copied
	 *     authenticator. The login is refused; please contact your administrator.
	 */
	public static ResKey ERROR_SIGNATURE_COUNTER;

	static {
		initConstants(I18NConstants.class);
	}
}
