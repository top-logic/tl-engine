/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.oauth;

import java.net.URI;

import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;

import com.top_logic.service.openapi.common.authentication.oauth.ClientSecret;
import com.top_logic.service.openapi.common.authentication.oauth.TokenURIProvider;

/**
 * {@link TokenBasedAuthenticator} with fixed token inspection endpoint.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClientCredentialsAuthenticator extends TokenBasedAuthenticator {

	private ServerCredentialSecret _secret;

	private TokenURIProvider _uriProvider;

	/**
	 * Creates a new {@link ClientCredentialsAuthenticator}.
	 */
	public ClientCredentialsAuthenticator(ServerCredentialSecret secret, TokenURIProvider uriProvider) {
		_secret = secret;
		_uriProvider = uriProvider;
	}

	@Override
	protected URI getIntrospectionURI() {
		URI introspectionURI = _uriProvider.getIntrospectionEndpointURI();
		if (introspectionURI == null) {
			throw new RuntimeException("No Introspection endpoint delivered by '" + _uriProvider + "'.");
		}
		return introspectionURI;
	}

	@Override
	protected ClientSecretPost getClientAuth() {
		ClientSecret clientSecret = _secret.getClientSecret();
		if (clientSecret != null) {
			return new ClientSecretPost(new ClientID(clientSecret.getClientId()),
				new Secret(clientSecret.getPassword()));
		} else {
			return null;
		}
	}

}

