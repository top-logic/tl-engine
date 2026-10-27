/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

/**
 * Why the {@link WorkspaceProxyHandler} refuses a request before its route handles it.
 *
 * <p>
 * The route {@link ProxyRoute#sendError(ProxyExchange, ProxyError) answers} it in the format of its
 * protocol. The {@link #message() message} is a fixed text, never something of the request.
 * </p>
 */
public enum ProxyError {

	/**
	 * The request reaches no route, or the proxy is switched off.
	 */
	NOT_FOUND(404, "Not found."),

	/**
	 * The request comes from an address the proxy does not accept.
	 */
	SOURCE_NOT_ALLOWED(403, "Source not allowed."),

	/**
	 * The request carries no valid token.
	 */
	INVALID_TOKEN(401, "Invalid token."),

	/**
	 * The token of the request is bound to another address.
	 */
	FOREIGN_SOURCE(403, "Token used from another source."),

	/**
	 * The token of the request is over its rate limit.
	 */
	RATE_LIMITED(ProxyError.SC_TOO_MANY_REQUESTS, "Too many requests."),

	;

	/**
	 * Status of an answer to a request over the rate limit.
	 */
	public static final int SC_TOO_MANY_REQUESTS = 429;

	private final int _status;

	private final String _message;

	ProxyError(int status, String message) {
		_status = status;
		_message = message;
	}

	/**
	 * The HTTP status of the answer.
	 */
	public int status() {
		return _status;
	}

	/**
	 * The text of the answer.
	 */
	public String message() {
		return _message;
	}

}
