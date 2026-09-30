/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.apikey;

import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;

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

	/**
	 * Map of API key to technical user name for request processing, or the empty string, if request
	 * processing should happen in system context.
	 */
	private Map<String, String> _accountByApiKey;

	/**
	 * Creates a new {@link APIKeyAuthenticator}.
	 * 
	 * @param location
	 *        Where the secret is found.
	 * @param parameterName
	 *        Name of the parameter holding the secret.
	 * @param allowedKeys
	 *        The API keys that are allowed to be authenticated mapped to the login name of the
	 *        system user to process the request with. If no technical user should be used for a
	 *        certain API key, the user name value must be the empty string, not <code>null</code>.
	 */
	public APIKeyAuthenticator(APIKeyPosition location, String parameterName, Map<String, String> allowedKeys) {
		_location = location;
		_accountByApiKey = allowedKeys;
		_parameterName = Objects.requireNonNull(parameterName);
	}

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure {
		String apikey = switch (_location) {
			case COOKIE -> {
				for (Cookie cookie: req.getCookies()) {
					if (_parameterName.equals(cookie.getName())) {
						yield cookie.getValue();
					}
				}
				throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_NO_COOKIE__PARAMETER.fill(_parameterName));
			}
			case HEADER -> {
				String header = req.getHeader(_parameterName);
				if (header == null) {
					throw new AuthenticationFailure(
						I18NConstants.AUTH_FAILED_NO_HEADER__PARAMETER.fill(_parameterName));
				}
				yield header;
			}
			case QUERY -> {
				String parameter = req.getParameter(_parameterName);
				if (parameter == null) {
					throw new AuthenticationFailure(
						I18NConstants.AUTH_FAILED_NO_QUERY_PARAM__PARAMETER.fill(_parameterName));
				}
				yield parameter;
			}
		};

		return checkKey(apikey);
	}

	private Person checkKey(String apikey) throws AuthenticationFailure {
		String userName = _accountByApiKey.get(apikey);
		if (userName == null) {
			throw new AuthenticationFailure(
				I18NConstants.AUTH_FAILED_INVALID_API_KEY__PARAMETER.fill(_parameterName));
		} else if (userName.isEmpty()) {
			// No technical user.
			return null;
		} else {
			Person result = Person.byName(userName);
			if (result == null) {
				throw new AuthenticationFailure(
					I18NConstants.ERROR_REQUEST_USER_DOES_NOT_EXIST__NAME.fill(userName));
			}
			return result;
		}
	}

}

