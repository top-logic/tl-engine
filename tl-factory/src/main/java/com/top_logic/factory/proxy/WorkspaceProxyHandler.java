/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;

/**
 * The workspace proxy, independent of the server it runs in: the {@link WorkspaceProxyListener} on
 * a port of its own or the {@link WorkspaceProxyServlet} in the web application.
 *
 * <p>
 * A request goes to the {@link ProxyRoute route} whose {@link ProxyRoute#getPath() path} is the
 * longest prefix of its {@link ProxyExchange#path() path}, followed by a slash or its end; a request
 * reaching no route is answered with 404, as is every request while the proxy is switched off.
 * Before the route sees it, the checks common to all routes are made, in this order:
 * </p>
 *
 * <ol>
 * <li>A request from a client address the {@link WorkspaceProxy} does not accept is refused with
 * {@link ProxyError#SOURCE_NOT_ALLOWED} - the address is the one of the connection
 * ({@link ProxyExchange#remoteAddress()}), never a header such as {@code X-Forwarded-For}.</li>
 * <li>The request must carry a workspace token in {@link ProxyForwarding#AUTHORIZATION}: as
 * {@link #BEARER_PREFIX bearer token}, or as password of {@link #BASIC_PREFIX basic authentication},
 * whose user name is ignored. The {@link WorkspaceProxy} {@link WorkspaceProxy#admit(String, String)
 * decides} about it: an invalid token is refused with {@link ProxyError#INVALID_TOKEN}, a token used
 * from another address than its first request with {@link ProxyError#FOREIGN_SOURCE}, a token over
 * its rate limit with {@link ProxyError#RATE_LIMITED} and {@link #RETRY_AFTER}.</li>
 * </ol>
 *
 * <p>
 * A token is valid for every route; the route gets the {@link ProxyAdmission admission} naming the
 * workspace the token was issued for. The route {@link ProxyRoute#sendError(ProxyExchange, ProxyError)
 * answers} a refusal in the format of its protocol.
 * </p>
 */
public class WorkspaceProxyHandler {

	/**
	 * Prefix of the value of {@link ProxyForwarding#AUTHORIZATION} before a bearer token.
	 */
	public static final String BEARER_PREFIX = "Bearer ";

	/**
	 * Prefix of the value of {@link ProxyForwarding#AUTHORIZATION} before the Base64 encoded user
	 * name and password of basic authentication.
	 */
	public static final String BASIC_PREFIX = "Basic ";

	/**
	 * Header of a 429 answer naming the seconds after which the next request is admitted.
	 */
	public static final String RETRY_AFTER = "Retry-After";

	private static final char BASIC_SEPARATOR = ':';

	private static final String PATH_SEPARATOR = "/";

	private final Supplier<? extends WorkspaceProxy> _proxy;

	/**
	 * Creates a {@link WorkspaceProxyHandler}.
	 *
	 * @param proxy
	 *        Supplier of the {@link WorkspaceProxy} asked for each request; it may supply
	 *        {@code null} when there is none, which answers requests with 404.
	 */
	public WorkspaceProxyHandler(Supplier<? extends WorkspaceProxy> proxy) {
		_proxy = proxy;
	}

	/**
	 * Answers a request.
	 */
	public void handle(ProxyExchange exchange) throws IOException {
		WorkspaceProxy proxy = _proxy.get();
		String path = exchange.path();
		ProxyRoute route = proxy == null || path == null ? null : route(proxy.routes(), path);
		if (route == null) {
			ProxyForwarding.sendText(exchange, ProxyError.NOT_FOUND.status(), ProxyError.NOT_FOUND.message());
			return;
		}
		if (!proxy.isEnabled()) {
			route.sendError(exchange, ProxyError.NOT_FOUND);
			return;
		}
		String source = exchange.remoteAddress();
		if (!proxy.isAllowedSource(source)) {
			Logger.info("Workspace proxy: refused a request from " + source + ".", WorkspaceProxyHandler.class);
			route.sendError(exchange, ProxyError.SOURCE_NOT_ALLOWED);
			return;
		}
		ProxyAdmission admission = proxy.admit(token(exchange), source);
		switch (admission.status()) {
			case ADMITTED:
				break;
			case INVALID_TOKEN:
				route.sendError(exchange, ProxyError.INVALID_TOKEN);
				return;
			case FOREIGN_SOURCE:
				route.sendError(exchange, ProxyError.FOREIGN_SOURCE);
				return;
			case RATE_LIMITED:
				long seconds = Math.max(1, (admission.retryAfter().toMillis() + 999) / 1000);
				exchange.addHeader(RETRY_AFTER, Long.toString(seconds));
				route.sendError(exchange, ProxyError.RATE_LIMITED);
				return;
		}
		route.handle(exchange, path.substring(route.getPath().length()), admission);
	}

	/**
	 * The route of the given path: the one whose path is the longest prefix of it, followed by a
	 * slash or the end of the path.
	 *
	 * @return The route, {@code null} if there is none.
	 */
	static ProxyRoute route(List<? extends ProxyRoute> routes, String path) {
		ProxyRoute result = null;
		for (ProxyRoute route : routes) {
			String prefix = route.getPath();
			boolean matches = path.equals(prefix) || path.startsWith(prefix + PATH_SEPARATOR);
			if (matches && (result == null || prefix.length() > result.getPath().length())) {
				result = route;
			}
		}
		return result;
	}

	/**
	 * The token of the request, from a bearer token or the password of basic authentication.
	 *
	 * @return The token, {@code null} if the request carries none.
	 */
	static String token(ProxyExchange exchange) {
		List<String> values = exchange.headers(ProxyForwarding.AUTHORIZATION);
		if (values.isEmpty()) {
			return null;
		}
		String authorization = values.get(0);
		if (authorization == null) {
			return null;
		}
		if (hasPrefix(authorization, BEARER_PREFIX)) {
			return authorization.substring(BEARER_PREFIX.length()).strip();
		}
		if (hasPrefix(authorization, BASIC_PREFIX)) {
			return basicPassword(authorization.substring(BASIC_PREFIX.length()).strip());
		}
		return null;
	}

	private static boolean hasPrefix(String value, String prefix) {
		return value.regionMatches(true, 0, prefix, 0, prefix.length());
	}

	private static String basicPassword(String encoded) {
		String credentials;
		try {
			credentials = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException ex) {
			return null;
		}
		int separator = credentials.indexOf(BASIC_SEPARATOR);
		return separator < 0 ? null : credentials.substring(separator + 1);
	}

}
