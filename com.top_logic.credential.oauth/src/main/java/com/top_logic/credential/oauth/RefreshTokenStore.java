/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Persistent storage of the refresh token of an OAuth 2.0 client, so that a restarted application
 * obtains access tokens without a new login of the user.
 *
 * <p>
 * A store may hold the tokens of several clients. A token is identified by the authorization
 * server (its issuer, or its token URL if no issuer is configured) and the client identifier.
 * </p>
 *
 * @see DeviceLoginProvider
 * @see FileRefreshTokenStore
 */
public interface RefreshTokenStore {

	/**
	 * The stored refresh token of the given client.
	 *
	 * @param server
	 *        The identifier of the authorization server.
	 * @param clientId
	 *        The identifier of the client at the server.
	 * @return The refresh token, or <code>null</code> if none is stored.
	 *
	 * @throws I18NRuntimeException
	 *         If the store cannot be read. The message never contains the token.
	 */
	String load(String server, String clientId);

	/**
	 * Stores the refresh token of the given client, replacing the token stored before.
	 *
	 * @param server
	 *        The identifier of the authorization server.
	 * @param clientId
	 *        The identifier of the client at the server.
	 * @param refreshToken
	 *        The refresh token.
	 *
	 * @throws I18NRuntimeException
	 *         If the store cannot be written. The message never contains the token.
	 */
	void store(String server, String clientId, String refreshToken);

	/**
	 * Removes the stored refresh token of the given client.
	 *
	 * @param server
	 *        The identifier of the authorization server.
	 * @param clientId
	 *        The identifier of the client at the server.
	 *
	 * @throws I18NRuntimeException
	 *         If the store cannot be written.
	 */
	void clear(String server, String clientId);

}
