/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;

/**
 * Retrieves the client ID metadata document of a client that identifies itself by its URL.
 *
 * <p>
 * The {@link OAuthAuthorizationServer} has already checked that the URL is an <code>https</code>
 * URL with a path, and parses and checks the document itself. What an implementation decides is
 * whether and how the URL is fetched: which hosts it may reach, how long it may take and how large
 * the answer may be.
 * </p>
 *
 * @see HttpClientMetadataFetcher
 */
public interface ClientMetadataFetcher {

	/**
	 * The content of the document at {@code documentUrl}.
	 *
	 * @param documentUrl
	 *        The client ID, an <code>https</code> URL.
	 * @return The document as text, expected to be a JSON object.
	 * @throws OAuthException
	 *         <code>invalid_client</code> with the reason, if the document cannot be retrieved.
	 */
	String fetch(URI documentUrl) throws OAuthException;

}
