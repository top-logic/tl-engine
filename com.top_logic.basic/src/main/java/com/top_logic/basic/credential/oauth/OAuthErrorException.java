/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Error response of an OAuth 2.0 authorization server (RFC 6749 §5.2).
 *
 * <p>
 * Gives access to the HTTP status and the error code, so that a caller can react on a specific
 * error.
 * </p>
 */
public class OAuthErrorException extends I18NRuntimeException {

	private final int _status;

	private final String _error;

	/**
	 * Creates a {@link OAuthErrorException}.
	 *
	 * @param message
	 *        The message of the exception.
	 * @param status
	 *        See {@link #getStatus()}.
	 * @param error
	 *        See {@link #getError()}.
	 */
	public OAuthErrorException(ResKey message, int status, String error) {
		super(message);
		_status = status;
		_error = error;
	}

	/**
	 * The HTTP status of the response.
	 */
	public int getStatus() {
		return _status;
	}

	/**
	 * The error code of the response (field {@link OAuthConstants#ERROR}), e.g.
	 * <code>invalid_client</code>, or <code>null</code> if the response contains no error code.
	 */
	public String getError() {
		return _error;
	}

}
