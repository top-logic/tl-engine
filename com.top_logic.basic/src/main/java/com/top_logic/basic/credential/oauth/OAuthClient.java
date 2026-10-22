/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import java.util.Map.Entry;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * An OAuth 2.0 client at an authorization server: creates requests to the server that carry the
 * client's identity and credentials.
 *
 * <p>
 * The building block of credential providers that obtain tokens with any grant type. A provider
 * creates a token request, adds the parameters of its grant and sends it. It keeps the token in a
 * {@link TokenCache}.
 * </p>
 *
 * @see #newTokenRequest(String)
 * @see #requestToken(OAuthRequest)
 * @see #newTokenCache()
 */
@Label("OAuth client")
public class OAuthClient {

	private final OAuthClientConfig _config;

	private final OAuthServer _server;

	private final ClientAuthentication _authentication;

	/**
	 * Creates a {@link OAuthClient}.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The client configuration.
	 */
	public OAuthClient(InstantiationContext context, OAuthClientConfig config) {
		_config = config;
		_server = new OAuthServer(context, config);
		_authentication = context.getInstance(config.getClientAuthentication());
	}

	/**
	 * The authorization server.
	 */
	public OAuthServer getServer() {
		return _server;
	}

	/**
	 * The identifier of this client at the authorization server.
	 */
	public String getClientId() {
		return _config.getClientId();
	}

	/**
	 * Creates a {@link TokenCache} with the renewal settings of the configuration.
	 */
	public TokenCache newTokenCache() {
		return new TokenCache(_config.getRenewalMargin(), _config.getDefaultTokenLifetime());
	}

	/**
	 * Creates a request to the token endpoint with the given grant type, the configured scope and
	 * parameters, and the client authentication.
	 *
	 * @param grantType
	 *        The value of the parameter {@link OAuthConstants#GRANT_TYPE}.
	 * @return The request, ready to add the parameters of the grant.
	 *
	 * @throws I18NRuntimeException
	 *         If the token endpoint cannot be determined or the client credentials are not
	 *         available.
	 */
	public OAuthRequest newTokenRequest(String grantType) {
		OAuthRequest request = newRequest(_server.getTokenEndpoint());
		request.setParameter(OAuthConstants.GRANT_TYPE, grantType);
		String scope = _config.getScope();
		if (!StringServices.isEmpty(scope)) {
			request.setParameter(OAuthConstants.SCOPE, scope);
		}
		for (Entry<String, String> parameter : _config.getParameters().entrySet()) {
			request.setParameter(parameter.getKey(), parameter.getValue());
		}
		return request;
	}

	/**
	 * Creates a request to the given endpoint of the authorization server, authenticated as this
	 * client.
	 *
	 * <p>
	 * A public client without {@link OAuthClientConfig#getClientAuthentication()} sends its
	 * identifier as parameter {@link OAuthConstants#CLIENT_ID}.
	 * </p>
	 *
	 * @param endpoint
	 *        The URL of the endpoint.
	 * @return The request.
	 */
	public OAuthRequest newRequest(String endpoint) {
		OAuthRequest request = new OAuthRequest(endpoint, _server.getTokenEndpoint());
		if (_authentication == null) {
			request.setParameter(OAuthConstants.CLIENT_ID, getClientId());
		} else {
			_authentication.authenticate(getClientId(), request);
		}
		return request;
	}

	/**
	 * Sends the given token request.
	 *
	 * @see OAuthServer#requestToken(OAuthRequest)
	 */
	public AccessToken requestToken(OAuthRequest request) {
		return _server.requestToken(request);
	}

}
