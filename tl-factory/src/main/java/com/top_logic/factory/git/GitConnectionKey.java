/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

/**
 * Everything the {@link GitHostingProvider} of a git connection depends on: the
 * {@link GitHostingService} keeps one provider per key, so that a changed connection gets a new
 * provider.
 *
 * @param connection
 *        The identifier of the connection, by which the providers of a connection are
 *        {@link GitHostingService#forget(String) forgotten}.
 * @param providerType
 *        The {@link GitProviderType#getName() name of the type} of the server.
 * @param url
 *        The URL of the server.
 * @param owner
 *        The owner of the repositories, see {@link GitConnectionSettings#owner()}.
 * @param builtIn
 *        The name of the {@link GitHostingService.Config#getConnections() built-in connection}
 *        whose credentials authenticate the requests, {@code null} for a connection with an access
 *        token of its own.
 * @param secretName
 *        The name of the secret holding the access token of a connection that is not built in,
 *        {@code null} for a built-in connection.
 * @param createOwner
 *        See {@link GitConnectionSettings#createOwner()}.
 */
public record GitConnectionKey(String connection, String providerType, String url, String owner, String builtIn,
		String secretName, boolean createOwner) {
	// Pure data.
}
