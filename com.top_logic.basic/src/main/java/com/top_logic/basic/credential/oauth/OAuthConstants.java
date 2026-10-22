/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

/**
 * Wire names of the OAuth 2.0 protocol (RFC 6749, RFC 7521, RFC 7523), of JSON web tokens (RFC
 * 7515, RFC 7519) and of the authorization server discovery (OIDC Discovery, RFC 8414).
 */
public final class OAuthConstants {

	/**
	 * Request parameter naming the grant type of a token request.
	 */
	public static final String GRANT_TYPE = "grant_type";

	/**
	 * Value of {@link #GRANT_TYPE} for the client credentials grant (RFC 6749 §4.4).
	 */
	public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

	/**
	 * Request parameter with the client identifier.
	 */
	public static final String CLIENT_ID = "client_id";

	/**
	 * Request parameter with the client secret (RFC 6749 §2.3.1).
	 */
	public static final String CLIENT_SECRET = "client_secret";

	/**
	 * Request parameter with the requested scope.
	 */
	public static final String SCOPE = "scope";

	/**
	 * Request parameter with an assertion authenticating the client (RFC 7521 §4.2).
	 */
	public static final String CLIENT_ASSERTION = "client_assertion";

	/**
	 * Request parameter with the format of the {@link #CLIENT_ASSERTION} (RFC 7521 §4.2).
	 */
	public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";

	/**
	 * Value of {@link #CLIENT_ASSERTION_TYPE} for a JSON web token (RFC 7523 §2.2).
	 */
	public static final String CLIENT_ASSERTION_TYPE_JWT_BEARER =
		"urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

	/**
	 * Field of a token response with the access token.
	 */
	public static final String ACCESS_TOKEN = "access_token";

	/**
	 * Field of a token response with the type of the access token.
	 */
	public static final String TOKEN_TYPE = "token_type";

	/**
	 * Field of a token response with the lifetime of the access token in seconds.
	 */
	public static final String EXPIRES_IN = "expires_in";

	/**
	 * Field of a token response with a refresh token.
	 */
	public static final String REFRESH_TOKEN = "refresh_token";

	/**
	 * Field of an error response with the error code (RFC 6749 §5.2).
	 */
	public static final String ERROR = "error";

	/**
	 * Field of an error response with a human-readable description of the error.
	 */
	public static final String ERROR_DESCRIPTION = "error_description";

	/**
	 * Path of the discovery document relative to the issuer.
	 */
	public static final String WELL_KNOWN_OPENID_CONFIGURATION = "/.well-known/openid-configuration";

	/**
	 * Field of the discovery document with the issuer identifier.
	 */
	public static final String ISSUER = "issuer";

	/**
	 * Field of the discovery document with the URL of the token endpoint.
	 */
	public static final String TOKEN_ENDPOINT = "token_endpoint";

	/**
	 * Claim of a JSON web token naming its issuer (RFC 7519 §4.1.1).
	 */
	public static final String CLAIM_ISSUER = "iss";

	/**
	 * Claim of a JSON web token naming its subject (RFC 7519 §4.1.2).
	 */
	public static final String CLAIM_SUBJECT = "sub";

	/**
	 * Claim of a JSON web token naming its audience (RFC 7519 §4.1.3).
	 */
	public static final String CLAIM_AUDIENCE = "aud";

	/**
	 * Claim of a JSON web token with its expiration time in seconds since the epoch (RFC 7519
	 * §4.1.4).
	 */
	public static final String CLAIM_EXPIRATION = "exp";

	/**
	 * Claim of a JSON web token with its creation time in seconds since the epoch (RFC 7519
	 * §4.1.6).
	 */
	public static final String CLAIM_ISSUED_AT = "iat";

	/**
	 * Claim of a JSON web token with its unique identifier (RFC 7519 §4.1.7).
	 */
	public static final String CLAIM_JWT_ID = "jti";

	/**
	 * Header of a JSON web signature naming the signature algorithm (RFC 7515 §4.1.1).
	 */
	public static final String HEADER_ALGORITHM = "alg";

	/**
	 * Header of a JSON web signature identifying the signing key (RFC 7515 §4.1.4).
	 */
	public static final String HEADER_KEY_ID = "kid";

	/**
	 * Header of a JSON web signature with the media type of the token (RFC 7515 §4.1.9).
	 */
	public static final String HEADER_TYPE = "typ";

	/**
	 * Value of {@link #HEADER_TYPE} for a JSON web token.
	 */
	public static final String TYPE_JWT = "JWT";

	/**
	 * HTTP header naming the media type of the request body.
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * HTTP header naming the accepted media types of the response.
	 */
	public static final String ACCEPT = "Accept";

	/**
	 * Media type of a form-encoded request body.
	 */
	public static final String FORM_URLENCODED = "application/x-www-form-urlencoded";

	/**
	 * Media type of a JSON response.
	 */
	public static final String APPLICATION_JSON = "application/json";

	private OAuthConstants() {
		// Constants only.
	}

}
