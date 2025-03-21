/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.impl;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.Authenticator;

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
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// No authentication or person check.
		return null;
	}

	@Override
	public Authenticator andThen(Authenticator after) {
		return after;
	}

	@Override
	public Authenticator or(Authenticator after) {
		return this;
	}

}

