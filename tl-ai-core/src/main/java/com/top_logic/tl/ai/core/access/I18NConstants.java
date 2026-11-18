/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.service.openapi.server.authentication.conf.EndpointCredential;

/**
 * Messages about personal access tokens: to the person creating or revoking one, and to a client
 * whose token is refused.
 */
public class I18NConstants extends I18NConstantsBase {

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
	 * The personal access token is not known: it was never created, it was revoked, or it lacks the
	 * permission for the agent endpoints.
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

	/**
	 * {@link EndpointCredential#credential() Credential} of a personal access token.
	 *
	 * @en A personal access token
	 * @de Ein persönliches Zugriffstoken
	 */
	public static ResKey CREDENTIAL_PERSONAL_ACCESS_TOKEN;

	/**
	 * {@link EndpointCredential#headerValue() Header value} of a personal access token.
	 *
	 * @en Bearer {@literal <personal access token>}
	 * @de Bearer {@literal <persönliches Zugriffstoken>}
	 */
	public static ResKey CREDENTIAL_PERSONAL_ACCESS_TOKEN_VALUE;

	/**
	 * {@link EndpointCredential#obtain() Where} a person creates a personal access token.
	 *
	 * @en Every person creates their own personal access token in the application at {0}: Settings
	 *     → Access tokens → New token, with the permission "Agent interfaces (MCP/A2A)". The token is
	 *     shown only once.
	 * @de Jede Person erstellt ihr eigenes persönliches Zugriffstoken in der Anwendung unter {0}:
	 *     Einstellungen → Zugriffstokens → Neues Token, mit der Berechtigung
	 *     "Agenten-Schnittstellen (MCP/A2A)". Das Token wird nur einmal angezeigt.
	 */
	public static ResKey1 CREDENTIAL_PERSONAL_ACCESS_TOKEN_OBTAIN__URL;

	/**
	 * Label of the access token permission {@link AccessTokenAuthenticator#AGENT_ENDPOINTS_PERMISSION}.
	 *
	 * @en Agent interfaces (MCP/A2A)
	 * @de Agenten-Schnittstellen (MCP/A2A)
	 */
	public static ResKey PERMISSION_AGENT_ENDPOINTS;

	static {
		initConstants(I18NConstants.class);
	}
}
