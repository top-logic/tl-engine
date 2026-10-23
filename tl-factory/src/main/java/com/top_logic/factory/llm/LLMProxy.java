/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

/**
 * What the {@link LLMProxyServlet} asks before it forwards a request: whether the proxy is
 * switched on, whether the request comes from an accepted address, whether the token of the caller
 * is valid, and where the request goes.
 *
 * @see LLMProxyService
 */
public interface LLMProxy {

	/**
	 * Whether the proxy accepts requests at all.
	 *
	 * <p>
	 * A switched off proxy answers every request with 404, as if it did not exist.
	 * </p>
	 */
	boolean isEnabled();

	/**
	 * Whether a request from the given client address is accepted.
	 *
	 * @param remoteAddress
	 *        The address of the client the servlet container reports, never a header of the
	 *        request.
	 */
	boolean isAllowedSource(String remoteAddress);

	/**
	 * Whether the given bearer token is valid: issued for a workspace that still exists, neither
	 * revoked nor expired.
	 *
	 * <p>
	 * Must not leave a transaction or context open: the request is streamed after it returns.
	 * </p>
	 *
	 * @param token
	 *        What the caller presented, not checked for anything yet.
	 */
	boolean authenticate(String token);

	/**
	 * Where an authenticated request is forwarded to.
	 *
	 * @return The upstream, {@code null} if none is configured.
	 */
	LLMUpstream upstream();

}
