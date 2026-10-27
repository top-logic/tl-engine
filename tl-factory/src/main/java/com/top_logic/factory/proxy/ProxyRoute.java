/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;

/**
 * A service the {@link WorkspaceProxyHandler workspace proxy} gives the workspaces access to, below
 * a {@link #getPath() path} of its own.
 *
 * <p>
 * The handler passes a request to the route whose path it starts with only after the checks common
 * to all routes: the address of the client, the token and its rate limit. The route decides what it
 * forwards, adds the credentials of its upstream and answers in the format of its protocol.
 * </p>
 */
public interface ProxyRoute {

	/**
	 * Configuration of a {@link ProxyRoute}.
	 */
	interface Config<I extends ProxyRoute> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration property name of {@link #getPath()}.
		 */
		String PATH = "path";

		/**
		 * The path of the route below the root of the proxy: starts with a slash, has none at
		 * its end, e.g. {@code /llm-proxy}.
		 *
		 * @see #PATH
		 */
		@Name(PATH)
		String getPath();

	}

	/**
	 * The path of the route below the root of the proxy.
	 *
	 * @see Config#getPath()
	 */
	String getPath();

	/**
	 * The environment variables a workspace container gets for this route; no secret.
	 *
	 * @param routeUrl
	 *        The URL of this route as the workspace containers reach it.
	 */
	default Map<String, String> containerEnv(String routeUrl) {
		return Collections.emptyMap();
	}

	/**
	 * The environment variables a process in a workspace gets for this route.
	 *
	 * @param token
	 *        The token of the process, valid for all routes of the proxy.
	 */
	default Map<String, String> processEnv(String token) {
		return Collections.emptyMap();
	}

	/**
	 * Answers an admitted request.
	 *
	 * @param exchange
	 *        The request and its answer.
	 * @param path
	 *        The path of the request below {@link #getPath()}: empty or starting with a slash.
	 * @param admission
	 *        The admission of the token of the request, naming the workspace it acts for.
	 */
	void handle(ProxyExchange exchange, String path, ProxyAdmission admission) throws IOException;

	/**
	 * Answers a request the {@link WorkspaceProxyHandler} refuses before {@link #handle(ProxyExchange,
	 * String, ProxyAdmission) handling}.
	 *
	 * <p>
	 * Headers the refusal needs, e.g. the time to wait of a rate limited request, are already added.
	 * By default, the answer is the {@link ProxyError#message()} as plain text.
	 * </p>
	 */
	default void sendError(ProxyExchange exchange, ProxyError error) throws IOException {
		ProxyForwarding.sendText(exchange, error.status(), error.message());
	}

	/**
	 * Releases what the route holds, e.g. the connections to its upstream.
	 */
	default void close() {
		// Nothing to release.
	}

}
