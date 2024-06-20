/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Class checking and authenticating the communication in Open API communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Authenticator {

	/**
	 * Authenticates the request.
	 * 
	 * @param req
	 *        Request that requires authentication.
	 * @param resp
	 *        Response corresponding to the given request.
	 * @throws AuthenticationFailure
	 *         when authentication is not possible.
	 * 
	 * @return The account which was authenticated. May be <code>null</code>, in this case no
	 *         special account is authenticated.
	 */
	Person authenticate(HttpServletRequest req, HttpServletResponse resp) throws AuthenticationFailure, IOException;

	/**
	 * Returns an {@link Authenticator} that first check authentication with this
	 * {@link Authenticator} and applies the authenticator <code>after</code>. If evaluation of
	 * either method throws an exception, it is relayed to the caller of the composed authenticator.
	 * 
	 * @param after
	 *        The {@link Authenticator} to apply after this authenticator is applied.
	 * @throws NullPointerException
	 *         if after is null
	 */
	default Authenticator andThen(Authenticator after) {
		if (after == AlwaysAuthenticated.INSTANCE) {
			return this;
		}
		return new CombinedAuthenticator(this, after);
	}

}

