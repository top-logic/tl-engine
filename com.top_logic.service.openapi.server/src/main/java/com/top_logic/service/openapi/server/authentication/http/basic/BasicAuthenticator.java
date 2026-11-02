/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
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
			throws AuthenticationFailure {
		if (!AuthorizationUtil.authorizationSent(req)) {
			throw authenticationFailure(I18NConstants.AUTH_FAILED_MISSING_AUTHENTICATION_DATA);
		}
		String authenticationHeader = AuthorizationUtil.getAuthorizationHeader(req);
		if (!AuthorizationUtil.isBasicAuthentiation(authenticationHeader)) {
			throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_NO_BASIC_AUTH);
		}
		Entry<String, char[]> sentCredentials =
			AuthorizationUtil.retrieveBasicAuthCredentials(authenticationHeader, Map::entry);
		return authenticateUser(sentCredentials.getKey(), sentCredentials.getValue());
	}

	/**
	 * Checks authentication and (optionally) retrieves the system account to use for processing
	 * requests.
	 *
	 * @param user
	 *        The user name sent in the request.
	 * @param password
	 *        The password sent in the request.
	 */
	protected abstract Person authenticateUser(String user, char[] password) throws AuthenticationFailure;

	/**
	 * Creates an {@link AuthenticationFailure} for a failed authentication that requests the
	 * browser to provide authentication data.
	 */
	private final AuthenticationFailure authenticationFailure(ResKey message) {
		AuthenticationFailure authenticationFailure = new AuthenticationFailure(message);
		authenticationFailure.setResponseEnhancer((response, failure, path) -> {
			AuthorizationUtil.setBasicAuthAuthenticationRequestHeader(response);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, failure.getMessage());
		});
		return authenticationFailure;
	}

}
