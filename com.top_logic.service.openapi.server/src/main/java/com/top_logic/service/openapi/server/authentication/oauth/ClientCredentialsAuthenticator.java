/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.oauth;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Subject;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.service.openapi.common.authentication.oauth.ClientSecret;
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

	private ServerCredentials.Config<?> _config;

	private boolean _inUserContext;

	private String _usernameField;

	private String _defaultUser;

	private Map<String, String> _userNameByClientId;

	/**
	 * Creates a new {@link ClientCredentialsAuthenticator}.
	 */
	public ClientCredentialsAuthenticator(ServerCredentials.Config<?> config, ServerCredentialSecret secret,
			String defaultUser, Map<String, String> userNameByClientId) {
		_config = config;
		_secret = secret;
		_defaultUser = defaultUser;
		_userNameByClientId = userNameByClientId;
		_uriProvider = TypedConfigUtil.createInstance(config.getURIProvider());
		_inUserContext = _config.isInUserContext();
		_usernameField = _config.getUsernameField();
	}

	@Override
	protected URI getIntrospectionURI() throws AuthenticationFailure {
		URI introspectionURI = _uriProvider.getIntrospectionEndpointURI();
		if (introspectionURI == null) {
			throw new AuthenticationFailure(
				I18NConstants.ERROR_NO_INTROSPECTION_ENDPOINT__DOMAIN.fill(_config.getDomain()));
		}
		return introspectionURI;
	}

	@Override
	protected String findAccountName(TokenIntrospectionSuccessResponse introspectionResponse, HttpServletRequest req,
			HttpServletResponse resp) throws AuthenticationFailure {
		if (!_inUserContext) {
			return null;
		}

		// Lookup technical user.
		String clientID = introspectionResponse.getClientID().toString();
		String username = _userNameByClientId.getOrDefault(clientID, _defaultUser);

		if (StringServices.isEmpty(username)) {
			// No technical user was associated with the requesting client, take the user from the
			// authorization token.
			if (_usernameField != null) {
				username = introspectionResponse.getStringParameter(_usernameField);
			} else {
				username = introspectionResponse.getUsername();
				if (StringServices.isEmpty(username)) {
					Subject subject = introspectionResponse.getSubject();
					if (subject != null) {
						username = subject.getValue();
					}
				}
			}
		}

		if (StringServices.isEmpty(username)) {
			// No user available, deny request.
			String jsonString = introspectionResponse.toJSONObject().toJSONString();
			AuthenticationFailure authenticationFailure =
				new AuthenticationFailure(I18NConstants.NO_USERNAME_IN_INTROSPECTION_RESPONSE);
			authenticationFailure.setResponseEnhancer((response, failure, path) -> {
				String noUserName = "No user for API '" + path + "' which requires user context.";
				String usernameField = !StringServices.isEmpty(_usernameField) ? _usernameField : "username";
				Logger.warn(noUserName + " Fieldname: " + usernameField + ". Introspection response: " + jsonString,
					ClientCredentialsAuthenticator.class);

				AuthorizationUtil.setBearerAuthenticationRequestHeader(response, "invalid_token", noUserName);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, noUserName);
			});
			throw authenticationFailure;
		}
		return username;
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

