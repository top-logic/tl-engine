/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link Exception} in case a request can not be authenticated.
 * 
 * @see Authenticator
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AuthenticationFailure extends I18NException {

	private ResponseEnhancer _responseEnhancer = new DefaultResponseEnhancer();

	/**
	 * Creates a new {@link AuthenticationFailure}.
	 */
	public AuthenticationFailure(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	/**
	 * Creates a new {@link AuthenticationFailure}.
	 */
	public AuthenticationFailure(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * Enhances the given response due to authentication failure encoded by this
	 * {@link AuthenticationFailure}.
	 * 
	 * @param response
	 *        The {@link HttpServletResponse} to enhance.
	 * @param requestedPath
	 *        The path which could not be accessed due to authentication failure.
	 */
	public void enhanceResponse(HttpServletResponse response, String requestedPath) throws IOException {
		_responseEnhancer.enhanceResponse(response, this, requestedPath);
	}

	/**
	 * Sets the {@link ResponseEnhancer} that is used when this {@link AuthenticationFailure} is
	 * handled.
	 */
	public void setResponseEnhancer(ResponseEnhancer responseEnhancer) {
		_responseEnhancer = Objects.requireNonNull(responseEnhancer);
	}

}

