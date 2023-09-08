/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Authenticator} that always authenticates the request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AlwaysAuthenticated implements Authenticator {

	/** Singleton {@link AlwaysAuthenticated} instance. */
	public static final AlwaysAuthenticated INSTANCE = new AlwaysAuthenticated();

	/**
	 * Creates a new {@link AlwaysAuthenticated}.
	 */
	protected AlwaysAuthenticated() {
		// singleton instance
	}

	@Override
	public void authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// No authentication check.
	}

	@Override
	public Authenticator andThen(Authenticator after) {
		return after;
	}

}

