/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * TL-Script functions describing the HTTP request that is currently processed, as the server sees
 * it.
 *
 * <p>
 * The values are the ones of the {@link HttpServletRequest} of the current
 * {@link InteractionContext}. When the application runs behind a reverse proxy and the servlet
 * container applies the proxy's forwarding headers (<code>Forwarded</code>,
 * <code>X-Forwarded-*</code>), the values describe the request the client sent to the proxy.
 * </p>
 *
 * <p>
 * A script that is not evaluated on behalf of an HTTP request, e.g. in a scheduler task or during
 * the application startup, gets <code>null</code> from each of these functions.
 * </p>
 */
@ScriptPrefix("request")
public class RequestFunctions extends TLScriptFunctions {

	/**
	 * The protocol scheme of the current HTTP request, e.g. <code>http</code> or
	 * <code>https</code>.
	 *
	 * @return The scheme of the current request, or <code>null</code> if the script is not
	 *         evaluated on behalf of an HTTP request.
	 */
	@Label("Scheme of the current request")
	@SideEffectFree
	public static String scheme() {
		HttpServletRequest request = currentRequest();
		return request == null ? null : request.getScheme();
	}

	/**
	 * The host name the current HTTP request was sent to.
	 *
	 * <p>
	 * This is the host part of the <code>Host</code> header, or the name of the server that
	 * received the request, if there is no such header.
	 * </p>
	 *
	 * @return The server name of the current request, or <code>null</code> if the script is not
	 *         evaluated on behalf of an HTTP request.
	 */
	@Label("Server name of the current request")
	@SideEffectFree
	public static String serverName() {
		HttpServletRequest request = currentRequest();
		return request == null ? null : request.getServerName();
	}

	/**
	 * The port number the current HTTP request was sent to.
	 *
	 * <p>
	 * This is the port of the <code>Host</code> header, or the default port of the request's scheme
	 * if the header names no port.
	 * </p>
	 *
	 * @return The server port of the current request, or <code>null</code> if the script is not
	 *         evaluated on behalf of an HTTP request.
	 */
	@Label("Server port of the current request")
	@SideEffectFree
	public static Double serverPort() {
		HttpServletRequest request = currentRequest();
		return request == null ? null : SearchExpression.toNumber(request.getServerPort());
	}

	/**
	 * The IP address of the client that sent the current HTTP request.
	 *
	 * @return The remote address of the current request, or <code>null</code> if the script is not
	 *         evaluated on behalf of an HTTP request.
	 */
	@Label("Client address of the current request")
	@SideEffectFree
	public static String remoteAddr() {
		HttpServletRequest request = currentRequest();
		return request == null ? null : request.getRemoteAddr();
	}

	/**
	 * The URL the client used for the current HTTP request.
	 *
	 * <p>
	 * The URL consists of the scheme, the server name, the server port (if it is not the default
	 * port of the scheme) and the path of the request. It does not contain the query string.
	 * </p>
	 *
	 * @return The URL of the current request, or <code>null</code> if the script is not evaluated
	 *         on behalf of an HTTP request.
	 */
	@Label("URL of the current request")
	@SideEffectFree
	public static String url() {
		HttpServletRequest request = currentRequest();
		return request == null ? null : request.getRequestURL().toString();
	}

	/**
	 * The {@link HttpServletRequest} of the current {@link InteractionContext}.
	 *
	 * @return The current request, or <code>null</code> if the current thread has no interaction,
	 *         or its interaction is not bound to an HTTP request (a system interaction).
	 */
	private static HttpServletRequest currentRequest() {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		return interaction == null ? null : interaction.asRequest();
	}

}
