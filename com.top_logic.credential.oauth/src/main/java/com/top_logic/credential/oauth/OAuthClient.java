/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.net.URI;
import java.util.Map.Entry;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * An OAuth 2.0 client at an authorization server: creates the token requests of the Nimbus OAuth
 * 2.0 SDK with the client's identity, credentials, scope and parameters, and sends them.
 *
 * <p>
 * The building block of credential providers that obtain tokens with any grant type. A provider
 * requests a token for its grant and keeps it in a {@link TokenCache}.
 * </p>
 *
 * @see #requestToken(AuthorizationGrant)
 * @see #newTokenCache()
 */
@Label("OAuth client")
public class OAuthClient {

	private final OAuthClientConfig _config;

	private final OAuthServer _server;

	private final ClientAuthentication _authentication;

	private final ClientID _clientId;

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
		_clientId = new ClientID(config.getClientId());
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
		return _clientId.getValue();
	}

	/**
	 * Whether this client authenticates at the authorization server, i.e. is a confidential
	 * client.
	 */
	public boolean hasAuthentication() {
		return _authentication != null;
	}

	/**
	 * Creates a {@link TokenCache} with the renewal settings of the configuration.
	 */
	public TokenCache newTokenCache() {
		return new TokenCache(_config.getRenewalMargin(), _config.getDefaultTokenLifetime());
	}

	/**
	 * The configured scope, <code>null</code> if none is configured.
	 */
	public Scope getScope() {
		String scope = _config.getScope();
		return StringServices.isEmpty(scope) ? null : Scope.parse(scope);
	}

	/**
	 * The authentication of this client for a request, or <code>null</code> for a public client
	 * without {@link OAuthClientConfig#getClientAuthentication()}, which sends only its
	 * identifier.
	 *
	 * @throws I18NRuntimeException
	 *         If the client credentials are not available.
	 */
	public com.nimbusds.oauth2.sdk.auth.ClientAuthentication createAuthentication() {
		if (_authentication == null) {
			return null;
		}
		return _authentication.createAuthentication(_clientId, _server.getTokenEndpoint());
	}

	/**
	 * Creates the HTTP request to the token endpoint for the given grant, with the configured
	 * scope and parameters and the client authentication.
	 *
	 * @param grant
	 *        The authorization grant.
	 * @return The request, ready to send.
	 *
	 * @throws I18NRuntimeException
	 *         If the token endpoint cannot be determined or the client credentials are not
	 *         available.
	 */
	public HTTPRequest newTokenRequest(AuthorizationGrant grant) {
		URI endpoint = _server.getTokenEndpoint();
		com.nimbusds.oauth2.sdk.auth.ClientAuthentication authentication = createAuthentication();
		TokenRequest.Builder builder = authentication == null
			? new TokenRequest.Builder(endpoint, _clientId, grant)
			: new TokenRequest.Builder(endpoint, authentication, grant);
		builder.scope(getScope());
		for (Entry<String, String> parameter : _config.getParameters().entrySet()) {
			builder.customParameter(parameter.getKey(), parameter.getValue());
		}
		return builder.build().toHTTPRequest();
	}

	/**
	 * Requests a token for the given grant.
	 *
	 * @param grant
	 *        The authorization grant.
	 * @return The issued token.
	 *
	 * @throws OAuthErrorException
	 *         If the authorization server answered with an error.
	 * @throws I18NRuntimeException
	 *         If the server cannot be reached, its response is malformed or the client credentials
	 *         are not available.
	 *
	 * @see OAuthServer#requestToken(HTTPRequest)
	 */
	public IssuedToken requestToken(AuthorizationGrant grant) {
		return _server.requestToken(newTokenRequest(grant));
	}

}
