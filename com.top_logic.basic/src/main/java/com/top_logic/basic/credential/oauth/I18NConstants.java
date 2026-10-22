/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Neither the issuer nor the token URL of the authorization server is configured.
	 */
	public static ResKey ERROR_NO_TOKEN_ENDPOINT;

	/**
	 * @en Both the issuer and the token URL of the authorization server are configured, only one of
	 *     them is allowed.
	 */
	public static ResKey ERROR_AMBIGUOUS_TOKEN_ENDPOINT;

	/**
	 * @en No issuer of the authorization server is configured, its discovery document is not
	 *     available.
	 */
	public static ResKey ERROR_NO_ISSUER;

	/**
	 * @en The discovery document of the authorization server "{0}" does not announce the endpoint
	 *     "{1}".
	 */
	public static ResKey2 ERROR_ENDPOINT_NOT_ANNOUNCED__ISSUER_FIELD;

	/**
	 * @en Fetching the discovery document "{0}" failed with HTTP status {1}.
	 */
	public static ResKey2 ERROR_DISCOVERY_FAILED__URL_STATUS;

	/**
	 * @en The discovery document names the issuer "{1}" instead of the configured issuer "{0}".
	 */
	public static ResKey2 ERROR_ISSUER_MISMATCH__EXPECTED_ACTUAL;

	/**
	 * @en The authorization server "{0}" cannot be reached.
	 */
	public static ResKey1 ERROR_SERVER_UNREACHABLE__URL;

	/**
	 * @en The URL "{0}" of the authorization server is invalid.
	 */
	public static ResKey1 ERROR_INVALID_URL__URL;

	/**
	 * @en The response of the authorization server "{0}" is malformed.
	 */
	public static ResKey1 ERROR_MALFORMED_RESPONSE__URL;

	/**
	 * @en The request to the authorization server "{0}" failed with HTTP status {1}.
	 */
	public static ResKey2 ERROR_REQUEST_FAILED__URL_STATUS;

	/**
	 * @en The authorization server "{0}" rejected the request with HTTP status {1} and error "{2}":
	 *     {3}
	 */
	public static ResKey4 ERROR_REQUEST_REJECTED__URL_STATUS_ERROR_DESCRIPTION;

	/**
	 * @en The private key of the client is not a key in PKCS#8 PEM format for the algorithm {0}.
	 */
	public static ResKey1 ERROR_INVALID_PRIVATE_KEY__ALGORITHM;

	/**
	 * @en Signing the client assertion with the algorithm {0} failed.
	 */
	public static ResKey1 ERROR_SIGNING_FAILED__ALGORITHM;

	/**
	 * @en Neither the issuer of the authorization server nor the URL of its device authorization
	 *     endpoint is configured.
	 */
	public static ResKey ERROR_NO_DEVICE_AUTHORIZATION_ENDPOINT;

	/**
	 * @en Login of "{2}" required: open {0} in a browser, confirm the code {1} and repeat the
	 *     action.
	 */
	public static ResKey3 ERROR_DEVICE_LOGIN_REQUIRED__URL_CODE_CLIENT;

	/**
	 * @en The login of "{0}" was denied. The next request starts a new login.
	 */
	public static ResKey1 DEVICE_LOGIN_DENIED__CLIENT;

	/**
	 * @en The login of "{0}" was not confirmed in time. The next request starts a new login.
	 */
	public static ResKey1 DEVICE_LOGIN_EXPIRED__CLIENT;

	/**
	 * @en The login of "{0}" failed. The next request starts a new login.
	 */
	public static ResKey1 DEVICE_LOGIN_FAILED__CLIENT;

	/**
	 * @en The authorization server no longer accepts the refresh token of "{0}", a new login is
	 *     required.
	 */
	public static ResKey1 REFRESH_TOKEN_REJECTED__CLIENT;

	/**
	 * @en The refresh token file "{0}" cannot be read.
	 */
	public static ResKey1 ERROR_REFRESH_TOKEN_UNREADABLE__PATH;

	/**
	 * @en The refresh token file "{0}" cannot be written.
	 */
	public static ResKey1 ERROR_REFRESH_TOKEN_NOT_WRITTEN__PATH;

	/**
	 * @en The refresh token file "{0}" belongs to another client or authorization server and is
	 *     ignored.
	 */
	public static ResKey1 REFRESH_TOKEN_FOREIGN__PATH;

	static {
		initConstants(I18NConstants.class);
	}
}
