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

	static {
		initConstants(I18NConstants.class);
	}
}
