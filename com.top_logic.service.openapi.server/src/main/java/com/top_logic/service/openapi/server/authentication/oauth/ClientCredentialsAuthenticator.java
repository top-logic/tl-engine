/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.oauth;

import java.io.IOException;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.service.openapi.common.authentication.oauth.ClientSecret;
import com.top_logic.service.openapi.common.authentication.oauth.ServerCredentials;
import com.top_logic.service.openapi.common.authentication.oauth.TokenURIProvider;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;

/**
 * {@link TokenBasedAuthenticator} with fixed token inspection endpoint.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClientCredentialsAuthenticator extends TokenBasedAuthenticator {

	private ServerCredentialSecret _secret;

	private TokenURIProvider _uriProvider;

	private ServerCredentials _config;

	/**
	 * Creates a new {@link ClientCredentialsAuthenticator}.
	 */
	public ClientCredentialsAuthenticator(ServerCredentials config, ServerCredentialSecret secret) {
		_config = config;
		_secret = secret;
		_uriProvider = TypedConfigUtil.createInstance(config.getURIProvider());
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
	protected String findAccountName(TokenIntrospectionSuccessResponse introspectionResponse, HttpServletRequest req,
			HttpServletResponse resp) throws AuthenticationFailure, IOException {
		if (!_config.isInUserContext()) {
			return null;
		}
		String userNameField = _config.getUserNameField();
		if (userNameField == null) {
			return introspectionResponse.getUsername();
		}
		return req.getHeader(userNameField);
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

