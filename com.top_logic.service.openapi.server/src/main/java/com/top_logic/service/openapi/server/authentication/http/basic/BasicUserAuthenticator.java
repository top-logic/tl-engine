/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.util.TLContext;

/**
 * {@link Authenticator} that authenticates system users against the built-in password database.
 */
public class BasicUserAuthenticator extends BasicAuthenticator {

	@Override
	protected Person authenticateUser(LoginCredentials login) throws AuthenticationFailure {
		return TLContext.inSystemContext(BasicUserAuthenticator.class, () -> {
			Person person = Person.byName(login.getUser());
			if (person == null) {
				throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA);
			}
			
			AuthenticationDevice authDevice = person.getAuthenticationDevice();
			if (authDevice == null) {
				throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA);
			}
			
			boolean authenticated = authDevice.authentify(
				com.top_logic.base.accesscontrol.LoginCredentials.fromUserAndPassword(person,
					login.getPassword().toCharArray()));
			if (!authenticated) {
				throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA);
			}
			
			return person;
		});
	}
}
