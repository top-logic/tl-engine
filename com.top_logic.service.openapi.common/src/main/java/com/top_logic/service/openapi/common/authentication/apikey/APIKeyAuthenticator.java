/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.apikey;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.impl.AuthenticationFailure;
import com.top_logic.service.openapi.common.authentication.impl.Authenticator;

/**
 * {@link Authenticator} to authenticate using an API key.
 * 
 * @see APIKeyAuthentication
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class APIKeyAuthenticator implements Authenticator {

	private APIKeyPosition _location;

	private String _parameterName;

	private Set<String> _allowedKeys;

	/**
	 * Creates a new {@link APIKeyAuthenticator}.
	 * 
	 * @param location
	 *        Where the secret is found.
	 * @param parameterName
	 *        Name of the parameter holding the secret.
	 * @param allowedKeys
	 *        The API key that are allowed to be authenticated.
	 */
	public APIKeyAuthenticator(APIKeyPosition location, String parameterName, Set<String> allowedKeys) {
		_location = location;
		_allowedKeys = allowedKeys;
		_parameterName = Objects.requireNonNull(parameterName);
	}

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		switch (_location) {
			case COOKIE:
				for (Cookie cookie: req.getCookies()) {
					if (_parameterName.equals(cookie.getName())) {
						checkKey(cookie.getValue());
						return null;
					}
				}
				throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_NO_COOKIE__PARAMETER.fill(_parameterName));
			case HEADER:
				String header = req.getHeader(_parameterName);
				if (header == null) {
					throw new AuthenticationFailure(
						I18NConstants.AUTH_FAILED_NO_HEADER__PARAMETER.fill(_parameterName));
				}
				checkKey(header);
				break;
			case QUERY:
				String parameter = req.getParameter(_parameterName);
				if (parameter == null) {
					throw new AuthenticationFailure(
						I18NConstants.AUTH_FAILED_NO_QUERY_PARAM__PARAMETER.fill(_parameterName));
				}
				checkKey(parameter);
				break;
			default:
				throw new RuntimeException("Unkown location: " + _location);

		}
		return null;
	}

	private void checkKey(String headerValue) throws AuthenticationFailure {
		if (!_allowedKeys.contains(headerValue)) {
			throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_INVALID_API_KEY__PARAMETER.fill(_parameterName));
		}
	}

}

