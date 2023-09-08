/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.oauth;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Request;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.service.openapi.client.authentication.I18NConstants;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;
import com.top_logic.service.openapi.common.authentication.oauth.ClientSecret;
import com.top_logic.service.openapi.common.authentication.oauth.TokenStorage;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link SecurityEnhancer} that fetches a token from some token server and adds it to the actual
 * request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TokenBasedEnhancer implements SecurityEnhancer {

	private static final TokenStorage<TokenCacheKey, AccessToken> TOKENS = new TokenStorage<>();

	@Override
	public void enhanceUrl(UriBuilder urlBuilder) {
		// Untouched
	}

	@Override
	public void enhanceRequest(HttpClient client, ClassicHttpRequest request) {
		ClientSecret secret = getClientSecret();
		URI uri = getTokenRequestURI();

		AccessToken accessToken = TOKENS.getValidToken(newTokenCacheKey(uri, secret));
		if (accessToken == null) {
			ClientSecretPost clientAuth = newClientAuthentication(secret);
			HTTPResponse response = sendTokenRequest(uri, clientAuth);
			TokenResponse tokenResponse = parseTokenResponse(response);
			if (tokenResponse.indicatesSuccess()) {
				AccessTokenResponse successResponse = tokenResponse.toSuccessResponse();
				accessToken = successResponse.getTokens().getAccessToken();
				long lifetime = accessToken.getLifetime();
				if (lifetime == 0) {
					// unspecified lifetime. Don't cache.
				} else {
					Date expiryDate = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(lifetime));
					TOKENS.storeToken(newTokenCacheKey(uri, secret), accessToken, expiryDate);
				}
			} else {
				TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
				ErrorObject errorObject = errorResponse.getErrorObject();
				throw new TopLogicException(I18NConstants.ERROR_NO_TOKEN_GENERATED__STATUS_CODE__CODE__DESCRIPTION
					.fill(errorObject.getHTTPStatusCode(), errorObject.getCode(), errorObject.getDescription()));
			}
		}
		request.setHeader(AuthorizationUtil.AUTHORIZATION_HEADER_NAME, accessToken.toAuthorizationHeader());
	}

	/**
	 * URI of the token server.
	 * 
	 * @return The URI of the token server.
	 */
	protected abstract URI getTokenRequestURI();

	/**
	 * Secret that is used to authenticate this client to the token server.
	 */
	protected abstract ClientSecret getClientSecret();

	private TokenResponse parseTokenResponse(HTTPResponse response) {
		try {
			return TokenResponse.parse(response);
		} catch (ParseException ex) {
			throw new TopLogicException(I18NConstants.ERROR_PARSING_TOKEN_RESPONSE, ex);
		}
	}

	private HTTPResponse sendTokenRequest(URI uri, ClientSecretPost clientAuth) {
		Request tokenRequest = new TokenRequest(uri, clientAuth, new ClientCredentialsGrant());
		try {
			return tokenRequest.toHTTPRequest().send();
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SENDING_TOKEN_REQUEST, ex);
		}
	}

	private ClientSecretPost newClientAuthentication(ClientSecret clientSecret) {
		ClientSecretPost clientAuth;
		if (clientSecret != null) {
			clientAuth = getClientAuth(clientSecret);
		} else {
			clientAuth = null;
		}
		return clientAuth;
	}

	private TokenCacheKey newTokenCacheKey(URI uri, ClientSecret secret) {
		TokenCacheKey tokenKey;
		if (secret != null) {
			tokenKey = new TokenCacheKey(uri, secret.getClientId(), secret.getPassword());
		} else {
			tokenKey = new TokenCacheKey(uri);
		}
		return tokenKey;
	}

	private ClientSecretPost getClientAuth(ClientSecret secret) {
		return new ClientSecretPost(new ClientID(secret.getClientId()), new Secret(secret.getPassword()));
	}

	/**
	 * Type of the key in {@link ClientCredentialEnhancer#TOKENS}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class TokenCacheKey {

		private final URI _uri;

		private final String _clientID;

		private final String _clientSecret;

		public TokenCacheKey(URI uri) {
			this(uri, null, null);
		}

		public TokenCacheKey(URI uri, String clientID, String clientSecret) {
			_uri = Objects.requireNonNull(uri);
			_clientID = clientID;
			_clientSecret = clientSecret;
		}

		@Override
		public int hashCode() {
			return Objects.hash(_clientID, _clientSecret, _uri);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TokenCacheKey other = (TokenCacheKey) obj;
			return Objects.equals(_clientID, other._clientID) && Objects.equals(_clientSecret, other._clientSecret)
				&& Objects.equals(_uri, other._uri);
		}

	}

}

