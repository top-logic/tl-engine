/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.util.List;

/**
 * What the {@link WorkspaceProxyHandler} asks before it passes a request to a route: whether the
 * proxy is switched on, which routes it has, whether the request comes from an accepted address,
 * and whether the token of the caller admits it.
 *
 * @see WorkspaceProxyService
 */
public interface WorkspaceProxy {

	/**
	 * Whether the proxy accepts requests at all.
	 *
	 * <p>
	 * A switched off proxy answers every request with 404, as if it did not exist.
	 * </p>
	 */
	boolean isEnabled();

	/**
	 * The routes of the proxy; a request goes to the one whose {@link ProxyRoute#getPath() path}
	 * is the longest prefix of its own.
	 */
	List<? extends ProxyRoute> routes();

	/**
	 * Whether a request from the given client address is accepted.
	 *
	 * @param remoteAddress
	 *        The address of the client the server reports, never a header of the request.
	 */
	boolean isAllowedSource(String remoteAddress);

	/**
	 * Decides about the token of a request from an {@link #isAllowedSource(String) accepted
	 * address}.
	 *
	 * <p>
	 * The token must be valid: issued for a workspace that still exists, neither revoked nor
	 * expired. It is bound to the address of its first admitted request, and it is limited in its
	 * requests per minute. A token is valid for every route of the proxy.
	 * </p>
	 *
	 * <p>
	 * Must not leave a transaction or context open: the request is streamed after it returns.
	 * </p>
	 *
	 * @param token
	 *        What the caller presented, not checked for anything yet.
	 * @param remoteAddress
	 *        The address of the client the server reports, never a header of the request.
	 */
	ProxyAdmission admit(String token, String remoteAddress);

}
