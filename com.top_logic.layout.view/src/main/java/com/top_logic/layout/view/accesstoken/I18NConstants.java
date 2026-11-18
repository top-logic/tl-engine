/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.accesstoken;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Messages to a person creating or revoking an access token.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Something that is not an access token was to be revoked.
	 *
	 * @en This is not an access token.
	 * @de Dies ist kein Zugriffstoken.
	 */
	public static ResKey ERROR_NOT_A_TOKEN;

	/**
	 * Somebody tried to revoke a token of another account without being an administrator.
	 *
	 * @en You can only revoke your own access tokens.
	 * @de Sie können nur Ihre eigenen Zugriffstokens widerrufen.
	 */
	public static ResKey ERROR_TOKEN_NOT_OWNED;

	/**
	 * A token was to be created without a logged-in account to create it for.
	 *
	 * @en An access token belongs to an account. Log in to create one.
	 * @de Ein Zugriffstoken gehört zu einem Konto. Melden Sie sich an, um eines zu erstellen.
	 */
	public static ResKey ERROR_NO_ACCOUNT;

	/**
	 * A token with a permission that binds to the session was to be created outside of a session
	 * of the user interface.
	 *
	 * @en A token with this permission acts in the session it is created in. Create it in the
	 *     application, not in a background task.
	 * @de Ein Token mit dieser Berechtigung wirkt in der Sitzung, in der es erstellt wird. Erstellen
	 *     Sie es in der Anwendung, nicht in einer Hintergrundaufgabe.
	 */
	public static ResKey ERROR_NO_SESSION;

	static {
		initConstants(I18NConstants.class);
	}
}
