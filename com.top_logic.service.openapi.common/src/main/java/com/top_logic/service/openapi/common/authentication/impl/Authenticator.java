/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.impl;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Class checking and authenticating the communication in <i>OpenAPI</i> communication.
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
	 * Builds an {@link Authenticator} that authenticates a request if and only if both
	 * authenticators succeed and do not identify different accounts.
	 * 
	 * <p>
	 * If evaluation of either authentication method throws an exception, it is relayed to the
	 * caller of the composed authenticator.
	 * </p>
	 * 
	 * @param after
	 *        The {@link Authenticator} to apply after this authenticator is applied.
	 * @throws NullPointerException
	 *         if after is null
	 */
	default Authenticator andThen(Authenticator after) {
		// Note: Even if (fallback == AlwaysAuthenticated.INSTANCE), do not skip this authenticator,
		// since this one may identify an account as context.
		return new CombinedAuthenticator(this, after);
	}

	/**
	 * Returns an {@link Authenticator} that first check authentication with this
	 * {@link Authenticator} if and only if this does not authenticate asks the authenticator
	 * <code>after</code>. Only if none of them successfully authenticates the request, an error is
	 * relayed to the caller of the composed authenticator.
	 * 
	 * @param fallback
	 *        The second {@link Authenticator} to apply if this one cannot authenticate the request.
	 * @throws NullPointerException
	 *         if after is null
	 */
	default Authenticator or(Authenticator fallback) {
		// Note: Even if (fallback == AlwaysAuthenticated.INSTANCE), do not skip this authenticator,
		// since this one may identify an account as context.
		return new DispatchingAuthenticator(this, fallback);
	}

}

