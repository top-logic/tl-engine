/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.conf;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.util.Resources;

/**
 * Internationalization constants for this package: the descriptions of the credentials an
 * endpoint accepts, see {@link EndpointCredential}.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * {@link EndpointCredential#credential() Credential} of HTTP basic authentication.
	 *
	 * @en HTTP basic authentication with the login name and password of an application account
	 * @de HTTP-Basic-Authentifizierung mit Anmeldename und Passwort eines Kontos der Anwendung
	 */
	public static ResKey CREDENTIAL_HTTP_BASIC;

	/**
	 * {@link EndpointCredential#headerValue() Header value} of HTTP basic authentication.
	 *
	 * @en Basic {@literal <login name:password, Base64 encoded>}
	 * @de Basic {@literal <Anmeldename:Passwort, Base64-kodiert>}
	 */
	public static ResKey CREDENTIAL_HTTP_BASIC_VALUE;

	/**
	 * {@link EndpointCredential#credential() Credential} of an HTTP authentication with another
	 * scheme than basic.
	 *
	 * @en HTTP authentication with the scheme {0}
	 * @de HTTP-Authentifizierung mit dem Verfahren {0}
	 */
	public static ResKey1 CREDENTIAL_HTTP__SCHEME;

	/**
	 * {@link EndpointCredential#headerValue() Header value} of an HTTP authentication with another
	 * scheme than basic.
	 *
	 * @en {0} {@literal <credential>}
	 * @de {0} {@literal <Zugangsdaten>}
	 */
	public static ResKey1 CREDENTIAL_HTTP_VALUE__SCHEME;

	/**
	 * {@link EndpointCredential#credential() Credential} of an OAuth bearer authentication.
	 *
	 * @en An OAuth access token issued by the authorization server of this deployment
	 * @de Ein OAuth-Zugriffstoken, ausgestellt vom Autorisierungsserver dieser Installation
	 */
	public static ResKey CREDENTIAL_OAUTH;

	/**
	 * {@link EndpointCredential#headerValue() Header value} of an OAuth bearer authentication.
	 *
	 * @en Bearer {@literal <OAuth access token>}
	 * @de Bearer {@literal <OAuth-Zugriffstoken>}
	 */
	public static ResKey CREDENTIAL_OAUTH_VALUE;

	/**
	 * {@link EndpointCredential#credential() Credential} of an API key sent in a header.
	 *
	 * @en An API key in the header {0}
	 * @de Ein API-Schlüssel im Header {0}
	 */
	public static ResKey1 CREDENTIAL_API_KEY_HEADER__NAME;

	/**
	 * {@link EndpointCredential#credential() Credential} of an API key sent as query parameter.
	 *
	 * @en An API key in the query parameter {0}
	 * @de Ein API-Schlüssel im Query-Parameter {0}
	 */
	public static ResKey1 CREDENTIAL_API_KEY_QUERY__NAME;

	/**
	 * {@link EndpointCredential#credential() Credential} of an API key sent in a cookie.
	 *
	 * @en An API key in the cookie {0}
	 * @de Ein API-Schlüssel im Cookie {0}
	 */
	public static ResKey1 CREDENTIAL_API_KEY_COOKIE__NAME;

	/**
	 * {@link EndpointCredential#headerValue() Header value} of an API key.
	 *
	 * @en {@literal <API key>}
	 * @de {@literal <API-Schlüssel>}
	 */
	public static ResKey CREDENTIAL_API_KEY_VALUE;

	/**
	 * A credential sent in a request header, see
	 * {@link EndpointCredential#describe(Resources) the description of a credential}.
	 *
	 * @en {0}, sent as header "{1}: {2}"
	 * @de {0}, gesendet als Header "{1}: {2}"
	 */
	public static ResKey3 CREDENTIAL_IN_HEADER__CREDENTIAL_HEADER_VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
