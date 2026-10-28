/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources.translate;

import java.net.HttpURLConnection;

/**
 * Adds credentials to the {@link HttpURLConnection}s a {@link Translator} opens to its translation
 * API.
 *
 * <p>
 * Called for each request, before it is sent, so that an implementation may deliver a rotated
 * secret or a renewed token without the translator being created anew. When the API rejects the
 * credentials (HTTP status 401), the translator calls {@link #invalidate()} and sends the request
 * once more.
 * </p>
 *
 * @see #header(String, String)
 */
@FunctionalInterface
public interface ConnectionAuthentication {

	/**
	 * {@link ConnectionAuthentication} that sends no credentials.
	 */
	ConnectionAuthentication NONE = connection -> {
		// No credentials.
	};

	/**
	 * Adds the current credentials to the given connection.
	 *
	 * @param connection
	 *        The connection that is not yet connected.
	 */
	void authenticate(HttpURLConnection connection);

	/**
	 * Informs this instance that the API rejected the credentials last added.
	 *
	 * <p>
	 * An implementation caching its credentials drops them, so that the next call to
	 * {@link #authenticate(HttpURLConnection)} adds fresh ones.
	 * </p>
	 */
	default void invalidate() {
		// No cached credentials.
	}

	/**
	 * {@link ConnectionAuthentication} sending a static value in a request header.
	 *
	 * @param name
	 *        The name of the header.
	 * @param value
	 *        The value of the header.
	 */
	static ConnectionAuthentication header(String name, String value) {
		return connection -> connection.setRequestProperty(name, value);
	}

}
