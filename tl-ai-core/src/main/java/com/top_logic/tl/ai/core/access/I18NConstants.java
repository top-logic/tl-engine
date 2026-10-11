/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * Messages about personal access tokens: to the person creating or revoking one, and to a client
 * whose token is refused.
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
	 * A request to an agent endpoint carried no bearer token.
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

	/**
	 * An endpoint serves nobody because no authentication is configured for it.
	 *
	 * @en The endpoint {0} is not available: this application configures no authentication for it,
	 *     and the endpoint serves no request it cannot attribute to an account. Configure an
	 *     authentication on the service that offers it.
	 * @de Der Endpunkt {0} ist nicht verfügbar: Diese Anwendung konfiguriert keine Authentifizierung
	 *     dafür, und der Endpunkt bedient keine Anfrage, die er keinem Konto zuordnen kann.
	 *     Konfigurieren Sie eine Authentifizierung an dem Dienst, der ihn anbietet.
	 */
	public static ResKey1 ERROR_NO_AUTHENTICATION_CONFIGURED__ENDPOINT;

	/**
	 * One configured authentication could not be built, so it authenticates nobody.
	 *
	 * @en The authentication "{0}" of the endpoint {1} could not be created and therefore
	 *     authenticates nobody. Check its configuration and its secret.
	 * @de Die Authentifizierung "{0}" des Endpunkts {1} konnte nicht erzeugt werden und
	 *     authentifiziert daher niemanden. Prüfen Sie deren Konfiguration und deren Secret.
	 */
	public static ResKey2 ERROR_AUTHENTICATION_UNAVAILABLE__DOMAIN_ENDPOINT;

	static {
		initConstants(I18NConstants.class);
	}
}
