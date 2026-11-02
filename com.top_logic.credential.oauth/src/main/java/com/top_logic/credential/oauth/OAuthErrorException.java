/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import com.nimbusds.oauth2.sdk.ErrorObject;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Error response of an OAuth 2.0 authorization server (RFC 6749 §5.2).
 *
 * <p>
 * Carries the {@link ErrorObject} of the response, so that a caller can react on a specific error,
 * e.g. by comparing it with the constants of {@link com.nimbusds.oauth2.sdk.OAuth2Error}.
 * </p>
 */
public class OAuthErrorException extends I18NRuntimeException {

	private final ErrorObject _errorObject;

	/**
	 * Creates a {@link OAuthErrorException}.
	 *
	 * @param message
	 *        The message of the exception.
	 * @param errorObject
	 *        See {@link #getErrorObject()}.
	 */
	public OAuthErrorException(ResKey message, ErrorObject errorObject) {
		super(message);
		_errorObject = errorObject;
	}

	/**
	 * The error of the response with its HTTP status, error code and description.
	 */
	public ErrorObject getErrorObject() {
		return _errorObject;
	}

	/**
	 * The HTTP status of the response.
	 */
	public int getStatus() {
		return _errorObject.getHTTPStatusCode();
	}

	/**
	 * The error code of the response, e.g. <code>invalid_client</code>, or <code>null</code> if the
	 * response contains no error code.
	 */
	public String getError() {
		return _errorObject.getCode();
	}

}
