/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;

/**
 * Authenticator expecting <i>BasicAuth</i> authorisation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BasicAuthAuthenticator implements Authenticator {

	private final Collection<LoginCredentials> _allowedUsers;

	/**
	 * Creates a new {@link BasicAuthAuthenticator}.
	 */
	public BasicAuthAuthenticator(Collection<LoginCredentials> allowedUsers) {
		_allowedUsers = allowedUsers;
	}

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		if (!AuthorizationUtil.authorizationSent(req)) {
			throw requestAuthentication(I18NConstants.AUTH_FAILED_MISSING_AUTHENTICATION_DATA);
		}
		String authenticationHeader = AuthorizationUtil.getAuthorizationHeader(req);
		if (!AuthorizationUtil.isBasicAuthentiation(authenticationHeader)) {
			throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_NO_BASIC_AUTH);
		}
		LoginCredentials sentCredentials = AuthorizationUtil.retrieveBasicAuthCredentials(authenticationHeader,
			BasicAuthAuthenticator::createCredentials);
		if (!_allowedUsers.contains(sentCredentials)) {
			throw requestAuthentication(
				I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA__USER.fill(sentCredentials.getUser()));
		}
		// Allowed users are technical user and *not* accounts from the application.
		return null;
	}

	private AuthenticationFailure requestAuthentication(ResKey message) {
		AuthenticationFailure authenticationFailure = new AuthenticationFailure(message);
		authenticationFailure.setResponseEnhancer((response, failure, path) -> {
			AuthorizationUtil.setBasicAuthAuthenticationRequestHeader(response);
			Logger.info("Authentication requested for '" + path + "'.", OpenApiServer.class);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		});
		return authenticationFailure;
	}

	private static LoginCredentials createCredentials(String user, char[] pwd) {
		LoginCredentials result = TypedConfiguration.newConfigItem(LoginCredentials.class);
		result.setUser(user);
		result.setPassword(new String(pwd));
		return result;
	}

}

