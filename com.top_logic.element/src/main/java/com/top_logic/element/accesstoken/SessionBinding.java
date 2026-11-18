/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.accesstoken;

import jakarta.servlet.http.HttpSession;

/**
 * How the endpoint of an access token permission finds the session a token was issued from.
 *
 * <p>
 * Contributed with the permission by the module offering the endpoint, see
 * {@link AccessTokenPermissions.PermissionConfig#getSessionBinding()}; a permission with a session
 * binding acts only in that session. When a token with the permission is created in a session, the
 * session is registered under the key the token stores.
 * </p>
 */
public interface SessionBinding {

	/**
	 * Registers the given session under the given key, for as long as the session lives.
	 *
	 * @param sessionKey
	 *        The key the new token stores, see {@link AccessToken#getSessionKey()}.
	 * @param session
	 *        The session the token is created in.
	 */
	void bind(String sessionKey, HttpSession session);

	/**
	 * The address a client sends a token with the permission to.
	 *
	 * <p>
	 * Shown before the token in the invitation the owner hands to the client, so that the client
	 * gets both in one copy.
	 * </p>
	 *
	 * @param applicationURL
	 *        The public address of the application, including its context path, without a trailing
	 *        slash.
	 */
	String endpoint(String applicationURL);

}
