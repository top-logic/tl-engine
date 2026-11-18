/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;

/**
 * An authorization request the {@link OAuthAuthorizationServer} accepted, waiting for the person's
 * decision.
 *
 * <p>
 * Created by {@link OAuthAuthorizationServer#validateAuthorizationRequest(AuthorizationRequest, com.top_logic.base.discovery.DiscoveryContext)};
 * once the person agrees, {@link OAuthAuthorizationServer#issueCode(ValidatedAuthorization, com.top_logic.knowledge.wrap.person.Person)}
 * makes the authorization code that is sent to {@link #redirectUri()}.
 * </p>
 *
 * @param client
 *        The requesting client.
 * @param redirectUri
 *        Where the answer goes, one of the client's registered redirect URIs.
 * @param codeChallenge
 *        The PKCE code challenge, method <code>S256</code>.
 * @param resource
 *        The resource (RFC 8707) the tokens are for, a resource of this application.
 * @param scope
 *        The requested scope, or <code>null</code>.
 * @param state
 *        The client's state, returned unchanged with the answer, or <code>null</code>.
 */
public record ValidatedAuthorization(OAuthClientInfo client, URI redirectUri, CodeChallenge codeChallenge,
		URI resource, Scope scope, State state) {
	// Pure value.
}
