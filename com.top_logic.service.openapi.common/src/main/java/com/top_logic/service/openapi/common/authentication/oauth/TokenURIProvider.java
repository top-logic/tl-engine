/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.net.URI;

import com.top_logic.basic.credential.oauth.OAuthServerConfig;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Provider of {@link URI}s to use for authentication of requests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TokenURIProvider {

	/**
	 * Endpoint {@link URI} to check that a given token is valid.
	 * 
	 * @return May be <code>null</code> when no such endpoint {@link URI} exists.
	 */
	URI getIntrospectionEndpointURI();

	/**
	 * Endpoint {@link URI} to receive a valid token from.
	 * 
	 * @return May be <code>null</code> when no such endpoint {@link URI} exists.
	 */
	URI getTokenEndpointURI();

	/**
	 * Configures the token endpoint of the given authorization server configuration.
	 *
	 * <p>
	 * The default implementation sets the {@link OAuthServerConfig#getTokenUrl() token URL} to
	 * the {@link #getTokenEndpointURI() token endpoint URI}.
	 * </p>
	 *
	 * @param server
	 *        The configuration of the authorization server to request tokens from.
	 *
	 * @throws I18NRuntimeException
	 *         If this provider knows no token endpoint.
	 */
	default void initTokenEndpoint(OAuthServerConfig server) {
		URI tokenEndpoint = getTokenEndpointURI();
		if (tokenEndpoint == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_NO_TOKEN_ENDPOINT__PROVIDER.fill(toString()));
		}
		server.setTokenUrl(tokenEndpoint.toString());
	}

}

