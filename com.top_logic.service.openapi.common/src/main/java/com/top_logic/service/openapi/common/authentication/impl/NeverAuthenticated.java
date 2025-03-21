/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.impl;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link Authenticator} that never authenticates the request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NeverAuthenticated implements Authenticator {

	private ResKey _externalReason;

	private int _errorCode;

	/**
	 * Creates a new {@link NeverAuthenticated} with error code
	 * {@link HttpServletResponse#SC_SERVICE_UNAVAILABLE}.
	 * 
	 * @param externalReason
	 *        Reason that is written to the response (i.e. visible for the caller of the API), why
	 *        the request is not available.
	 */
	public NeverAuthenticated(ResKey externalReason) {
		this(HttpServletResponse.SC_SERVICE_UNAVAILABLE, externalReason);
	}

	/**
	 * Creates a new {@link NeverAuthenticated}.
	 * 
	 * @param errorCode
	 *        Error code to write to the response.
	 * @param externalReason
	 *        Reason that is written to the response (i.e. visible for the caller of the API), why
	 *        the request is not available.
	 */
	public NeverAuthenticated(int errorCode, ResKey externalReason) {
		_errorCode = errorCode;
		_externalReason = externalReason;
	}

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		AuthenticationFailure failure = new AuthenticationFailure(_externalReason);
		failure.setResponseEnhancer((response, authEx, requestedPath) -> {
			String message = authEx.getMessage();
			response.sendError(_errorCode, message);
		});
		throw failure;
	}

	@Override
	public Authenticator andThen(Authenticator after) {
		return this;
	}

	@Override
	public Authenticator or(Authenticator fallback) {
		return fallback;
	}

	/**
	 * Creates a {@link NeverAuthenticated} indicating that the server is misconfigured (no secret
	 * for authentication method).
	 */
	public static Authenticator missingSecret() {
		return new NeverAuthenticated(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			I18NConstants.ERROR_MISSING_AUTHENTICATION_SECRET);
	}

}

