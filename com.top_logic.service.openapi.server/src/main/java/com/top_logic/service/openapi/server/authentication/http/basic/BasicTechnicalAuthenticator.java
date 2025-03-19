/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import java.util.Collection;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;

/**
 * {@link BasicAuthenticator} that authenticates a technical user.
 */
public class BasicTechnicalAuthenticator extends BasicAuthenticator {

	private final Collection<LoginCredentials> _allowedUsers;

	/**
	 * Creates a new {@link BasicAuthenticator}.
	 */
	public BasicTechnicalAuthenticator(Collection<LoginCredentials> allowedUsers) {
		_allowedUsers = allowedUsers;
	}

	@Override
	protected Person authenticateUser(LoginCredentials sentCredentials) throws AuthenticationFailure {
		if (!_allowedUsers.contains(sentCredentials)) {
			throw authenticationFailure(I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA);
		}

		// Allowed users are technical user and *not* accounts from the application.
		return null;
	}

}
