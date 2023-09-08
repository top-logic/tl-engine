/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.oauth;

import java.net.URI;

import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.common.authentication.oauth.ClientSecret;
import com.top_logic.service.openapi.common.authentication.oauth.TokenURIProvider;

/**
 * {@link SecurityEnhancer} adding security informations to authenticate using OAuth2 Client
 * credentials.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClientCredentialEnhancer extends TokenBasedEnhancer {

	private ClientCredentialSecret _secret;

	private TokenURIProvider _uriProvider;

	/**
	 * Creates a new {@link ClientCredentialEnhancer}.
	 */
	public ClientCredentialEnhancer(ClientCredentialSecret secret, TokenURIProvider uriProvider) {
		_secret = secret;
		_uriProvider = uriProvider;
	}

	@Override
	protected URI getTokenRequestURI() {
		URI tokenEndpointURI = _uriProvider.getTokenEndpointURI();
		if (tokenEndpointURI == null) {
			throw new RuntimeException("No token endpoint URI received from '" + null + "'.");
		}
		return tokenEndpointURI;
	}

	@Override
	protected ClientSecret getClientSecret() {
		return _secret.getClientSecret();
	}

}
