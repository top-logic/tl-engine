/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;

/**
 * Authenticator expecting <i>BasicAuth</i> authorisation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class BasicAuthenticator implements Authenticator {

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		if (!AuthorizationUtil.authorizationSent(req)) {
			throw authenticationFailure(I18NConstants.AUTH_FAILED_MISSING_AUTHENTICATION_DATA);
		}
		String authenticationHeader = AuthorizationUtil.getAuthorizationHeader(req);
		if (!AuthorizationUtil.isBasicAuthentiation(authenticationHeader)) {
			throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_NO_BASIC_AUTH);
		}
		LoginCredentials sentCredentials = AuthorizationUtil.retrieveBasicAuthCredentials(authenticationHeader,
			BasicAuthenticator::createCredentials);
		return authenticateUser(sentCredentials);
	}

	/**
	 * Checks authentication and (optionally) retrieves the system account to use for processing
	 * requests.
	 */
	protected abstract Person authenticateUser(LoginCredentials sentCredentials) throws AuthenticationFailure;

	/**
	 * Creates an {@link AuthenticationFailure} for a failed authentication.
	 */
	protected final AuthenticationFailure authenticationFailure(ResKey message) {
		AuthenticationFailure authenticationFailure = new AuthenticationFailure(message);
		authenticationFailure.setResponseEnhancer((response, failure, path) -> {
			AuthorizationUtil.setBasicAuthAuthenticationRequestHeader(response);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, failure.getMessage());
		});
		return authenticationFailure;
	}

	/**
	 * Creates {@link LoginCredentials} for the given user name and password.
	 */
	public static LoginCredentials createCredentials(String user, char[] pwd) {
		LoginCredentials result = TypedConfiguration.newConfigItem(LoginCredentials.class);
		result.setUser(user);
		result.setPassword(new String(pwd));
		return result;
	}

}

