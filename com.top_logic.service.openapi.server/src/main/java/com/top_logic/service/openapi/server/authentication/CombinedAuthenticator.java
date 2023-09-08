/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Authenticator} executing multiple {@link Authenticator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CombinedAuthenticator implements Authenticator {

	private final Authenticator _first;

	private final Authenticator _second;

	/**
	 * Creates a new {@link CombinedAuthenticator}.
	 */
	public CombinedAuthenticator(Authenticator first, Authenticator second) {
		_first = first;
		_second = second;
	}

	@Override
	public void authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		_first.authenticate(req, resp);
		_second.authenticate(req, resp);
	}

}

