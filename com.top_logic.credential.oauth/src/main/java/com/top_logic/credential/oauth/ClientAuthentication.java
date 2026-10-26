/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.net.URI;

import com.nimbusds.oauth2.sdk.id.ClientID;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Authentication of an OAuth 2.0 client at the authorization server (RFC 6749 §2.3).
 *
 * <p>
 * Creates the Nimbus client authentication that adds the client credentials to a request to the
 * authorization server, e.g. a token request. Implementations are {@link ClientSecretAuthentication}, {@link PrivateKeyJwtAuthentication} and
 * {@link ClientAssertionAuthentication}.
 * </p>
 *
 * @see OAuthClientConfig#getClientAuthentication()
 */
public interface ClientAuthentication {

	/**
	 * Creates the authentication of the client with the given identifier for one request.
	 *
	 * <p>
	 * Called for each request, so that a rotated secret takes effect and a signed assertion is
	 * fresh.
	 * </p>
	 *
	 * @param clientId
	 *        The identifier of the client at the authorization server.
	 * @param tokenEndpoint
	 *        The token endpoint of the authorization server, the audience of a signed assertion.
	 * @return The authentication to add to the request.
	 *
	 * @throws I18NRuntimeException
	 *         If the credentials are not available. The message never contains a secret.
	 */
	com.nimbusds.oauth2.sdk.auth.ClientAuthentication createAuthentication(ClientID clientId, URI tokenEndpoint);

	/**
	 * Whether the client credentials are available now, so that
	 * {@link #createAuthentication(ClientID, URI)} can succeed.
	 *
	 * <p>
	 * The check is local and cheap and never reveals a secret.
	 * </p>
	 *
	 * @see CredentialProvider#isAvailable()
	 * @see SecretSource#isAvailable()
	 */
	default boolean isAvailable() {
		return true;
	}

}
