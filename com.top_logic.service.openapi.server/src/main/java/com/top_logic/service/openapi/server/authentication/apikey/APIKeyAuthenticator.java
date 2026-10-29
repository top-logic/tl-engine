/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.apikey;

import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.util.TLContext;

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
	 * The accepted API keys with the technical user name for request processing.
	 */
	private List<AllowedKey> _allowedKeys;

	/**
	 * An API key accepted by an {@link APIKeyAuthenticator}.
	 */
	public static final class AllowedKey {

		private final SecretSource _key;

		private final String _userName;

		/**
		 * Creates an {@link AllowedKey}.
		 *
		 * @param key
		 *        The source of the accepted key, read at each check.
		 * @param userName
		 *        The login name of the technical user to process the request with, or the empty
		 *        string (not <code>null</code>), if request processing should happen in system
		 *        context.
		 */
		public AllowedKey(SecretSource key, String userName) {
			_key = Objects.requireNonNull(key);
			_userName = Objects.requireNonNull(userName);
		}

		/**
		 * The source of the accepted key.
		 */
		public SecretSource getKey() {
			return _key;
		}

		/**
		 * The login name of the technical user, the empty string for system context.
		 */
		public String getUserName() {
			return _userName;
		}

	}

	/**
	 * Creates a new {@link APIKeyAuthenticator}.
	 * 
	 * @param location
	 *        Where the secret is found.
	 * @param parameterName
	 *        Name of the parameter holding the secret.
	 * @param allowedKeys
	 *        The API keys that are allowed to be authenticated with the login name of the system
	 *        user to process the request with.
	 */
	public APIKeyAuthenticator(APIKeyPosition location, String parameterName, List<AllowedKey> allowedKeys) {
		_location = location;
		_allowedKeys = allowedKeys;
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

	/**
	 * The account name assigned to the given API key, <code>null</code> if the key is not
	 * configured.
	 *
	 * <p>
	 * The configured keys are read from their sources and compared with the given key in constant
	 * time, so that a configured key cannot be guessed from response times.
	 * </p>
	 */
	private String accountFor(String apikey) {
		String result = null;
		for (AllowedKey allowed : _allowedKeys) {
			try {
				// No early exit, so that the time taken does not reveal which key matched.
				if (allowed.getKey().matches(apikey)) {
					result = allowed.getUserName();
				}
			} catch (I18NRuntimeException ex) {
				Logger.error("API key for parameter '" + _parameterName + "' is not available.", ex,
					APIKeyAuthenticator.class);
			}
		}
		return result;
	}

	private Person checkKey(String apikey) throws AuthenticationFailure {
		String userName = StringServices.isEmpty(apikey) ? null : accountFor(apikey);
		if (userName == null) {
			throw new AuthenticationFailure(
				I18NConstants.AUTH_FAILED_INVALID_API_KEY__PARAMETER.fill(_parameterName));
		} else if (userName.isEmpty()) {
			// No technical user.
			return null;
		} else {
			return TLContext.inSystemContext(APIKeyAuthenticator.class, () -> {
				Person result = Person.byName(userName);
				if (result == null) {
					throw new AuthenticationFailure(
						I18NConstants.ERROR_REQUEST_USER_DOES_NOT_EXIST__NAME.fill(userName));
				}
				return result;
			});
		}
	}

}

