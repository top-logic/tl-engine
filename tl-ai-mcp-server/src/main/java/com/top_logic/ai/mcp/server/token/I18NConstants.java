/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.token;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Messages about personal access tokens: to the person creating or revoking one, and to an MCP
 * client whose token is refused.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * A token was to be created without a name.
	 *
	 * @en Give the token a name, for example the client you are going to use it in.
	 * @de Geben Sie dem Token einen Namen, zum Beispiel den des Clients, in dem Sie es verwenden.
	 */
	public static ResKey ERROR_TOKEN_NAME_MISSING;

	/**
	 * A token was to be created with a validity of zero or fewer days.
	 *
	 * @en A token is valid for at least one day. Leave the field empty for a token that does not
	 *     expire.
	 * @de Ein Token ist mindestens einen Tag gültig. Lassen Sie das Feld leer für ein Token, das
	 *     nicht abläuft.
	 */
	public static ResKey ERROR_TOKEN_VALIDITY_NOT_POSITIVE;

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
	 * A request to the MCP endpoint carried no bearer token.
	 *
	 * @en No personal access token: send it as "Authorization: Bearer <token>".
	 * @de Kein persönliches Zugriffstoken: Senden Sie es als "Authorization: Bearer <token>".
	 */
	public static ResKey ERROR_NO_BEARER_TOKEN;

	/**
	 * The bearer value does not have the format of a personal access token.
	 *
	 * @en The bearer token is not a personal access token of this application.
	 * @de Das Bearer-Token ist kein persönliches Zugriffstoken dieser Anwendung.
	 */
	public static ResKey ERROR_NOT_AN_ACCESS_TOKEN;

	/**
	 * The personal access token is not known: it was never created, or it was revoked.
	 *
	 * @en The personal access token is not valid. It may have been revoked; create a new one in the
	 *     application.
	 * @de Das persönliche Zugriffstoken ist nicht gültig. Möglicherweise wurde es widerrufen;
	 *     erstellen Sie in der Anwendung ein neues.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_UNKNOWN;

	/**
	 * The personal access token is past its expiry.
	 *
	 * @en The personal access token has expired. Create a new one in the application.
	 * @de Das persönliche Zugriffstoken ist abgelaufen. Erstellen Sie in der Anwendung ein neues.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_EXPIRED;

	static {
		initConstants(I18NConstants.class);
	}
}
