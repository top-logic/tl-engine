/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;

/**
 * {@link BasicAuthenticator} that authenticates a technical user.
 *
 * <p>
 * The configured passwords are read from their {@link SecretSource}s for each request and compared
 * with the sent password in constant time.
 * </p>
 */
public class BasicTechnicalAuthenticator extends BasicAuthenticator {

	private final List<AllowedUser> _allowedUsers;

	/**
	 * Creates a new {@link BasicAuthenticator}.
	 *
	 * @param allowedUsers
	 *        The configured user names and passwords that are accepted. Credentials without
	 *        password accept no request.
	 */
	public BasicTechnicalAuthenticator(Collection<LoginCredentials> allowedUsers) {
		_allowedUsers = new ArrayList<>(allowedUsers.size());
		for (LoginCredentials login : allowedUsers) {
			SecretSource password = TypedConfigUtil.createInstance(login.getPassword());
			if (password != null) {
				_allowedUsers.add(new AllowedUser(login.getUser(), password));
			}
		}
	}

	@Override
	protected Person authenticateUser(String user, char[] password) throws AuthenticationFailure {
		if (password.length == 0 || !isAllowed(user, new String(password))) {
			throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA);
		}

		// Allowed users are technical user and *not* accounts from the application.
		return null;
	}

	private boolean isAllowed(String user, String password) {
		boolean allowed = false;
		for (AllowedUser allowedUser : _allowedUsers) {
			if (!allowedUser._user.equals(user)) {
				continue;
			}
			try {
				// No early exit, so that the time taken does not reveal which password matched.
				allowed |= allowedUser._password.matches(password);
			} catch (I18NRuntimeException ex) {
				Logger.error("Password of technical user '" + user + "' is not available.", ex,
					BasicTechnicalAuthenticator.class);
			}
		}
		return allowed;
	}

	private static final class AllowedUser {

		final String _user;

		final SecretSource _password;

		AllowedUser(String user, SecretSource password) {
			_user = user;
			_password = password;
		}

	}

}
