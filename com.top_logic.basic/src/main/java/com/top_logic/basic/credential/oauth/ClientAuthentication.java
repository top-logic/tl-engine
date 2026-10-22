/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Authentication of an OAuth 2.0 client at the authorization server (RFC 6749 §2.3).
 *
 * <p>
 * Adds the client credentials to a request to the authorization server, e.g. a token request.
 * Implementations are {@link ClientSecretAuthentication}, {@link PrivateKeyJwtAuthentication} and
 * {@link ClientAssertionAuthentication}.
 * </p>
 *
 * @see OAuthClientConfig#getClientAuthentication()
 */
public interface ClientAuthentication {

	/**
	 * Adds the credentials of the client with the given identifier to the given request.
	 *
	 * @param clientId
	 *        The identifier of the client at the authorization server.
	 * @param request
	 *        The request to authenticate.
	 *
	 * @throws I18NRuntimeException
	 *         If the credentials are not available. The message never contains a secret.
	 */
	void authenticate(String clientId, OAuthRequest request);

}
