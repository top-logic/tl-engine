/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;
import java.util.Set;

/**
 * A client known to the {@link OAuthAuthorizationServer}, registered dynamically or described by
 * its client ID metadata document.
 *
 * @param clientId
 *        The identifier the client presents: a generated one for a dynamically registered client,
 *        the URL of its metadata document otherwise.
 * @param clientName
 *        The name the client gives itself, or <code>null</code>.
 * @param redirectUris
 *        The redirect URIs the client registered, each checked by
 *        {@link RedirectUris#checkRegistered(URI)}.
 */
public record OAuthClientInfo(String clientId, String clientName, Set<URI> redirectUris) {

	/**
	 * The name to show for the client: its {@link #clientName()}, or else its {@link #clientId()}.
	 */
	public String displayName() {
		return clientName == null || clientName.isBlank() ? clientId : clientName;
	}

}
