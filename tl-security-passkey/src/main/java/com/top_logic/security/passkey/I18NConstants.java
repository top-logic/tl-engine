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
	 * @en The passkey could not be registered.
	 */
	public static ResKey ERROR_REGISTRATION_FAILED;

	/**
	 * @en The passkey needs a name.
	 */
	public static ResKey ERROR_LABEL_MISSING;

	/**
	 * @en The passkey is already registered.
	 */
	public static ResKey ERROR_ALREADY_REGISTERED;

	/**
	 * @en The login with the passkey failed.
	 */
	public static ResKey ERROR_AUTHENTICATION_FAILED;

	/**
	 * @en The request for the passkey has expired or was already answered. Please try again.
	 */
	public static ResKey ERROR_REQUEST_EXPIRED;

	/**
	 * @en The relying party of the passkeys cannot be determined: neither is it configured nor is
	 *     there a request to derive it from.
	 */
	public static ResKey ERROR_NO_RELYING_PARTY;

	/**
	 * @en The data of the passkey request could not be processed.
	 */
	public static ResKey ERROR_INVALID_CEREMONY_DATA;

	static {
		initConstants(I18NConstants.class);
	}
}
