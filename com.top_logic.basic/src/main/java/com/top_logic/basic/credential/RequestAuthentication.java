/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.net.http.HttpRequest;

/**
 * An outgoing HTTP request, as seen by a {@link CredentialProvider} that adds its credentials to
 * it.
 *
 * <p>
 * A consumer adapts the request object of its HTTP client library to this interface, e.g.
 * <code>request::setHeader</code> for an Apache HTTP client request or
 * <code>connection::setRequestProperty</code> for a {@link java.net.HttpURLConnection}.
 * </p>
 *
 * @see #of(HttpRequest.Builder)
 */
@FunctionalInterface
public interface RequestAuthentication {

	/**
	 * Sets the header with the given name, replacing a value set before.
	 *
	 * @param name
	 *        The header name.
	 * @param value
	 *        The header value.
	 */
	void setHeader(String name, String value);

	/**
	 * Adapts a request of the {@link java.net.http.HttpClient} to a {@link RequestAuthentication}.
	 *
	 * @param builder
	 *        The builder of the request to authenticate.
	 * @return A {@link RequestAuthentication} setting headers on the given builder.
	 */
	static RequestAuthentication of(HttpRequest.Builder builder) {
		return builder::setHeader;
	}

}
