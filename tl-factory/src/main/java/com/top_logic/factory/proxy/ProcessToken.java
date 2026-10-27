/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A token of the workspace proxy for one command in a workspace, with the environment the command
 * gets; {@link #close() closing} it revokes the token.
 *
 * @see ProxyTokens#issueProcessToken(String, java.time.Duration)
 */
public final class ProcessToken implements AutoCloseable {

	private final ProxyTokens _tokens;

	private final String _token;

	private final Map<String, String> _env;

	/**
	 * Creates a {@link ProcessToken}.
	 *
	 * @param tokens
	 *        The issuer of the token, revoking it on {@link #close()}.
	 * @param token
	 *        The issued token.
	 * @param env
	 *        See {@link #env()}.
	 */
	public ProcessToken(ProxyTokens tokens, String token, Map<String, String> env) {
		_tokens = tokens;
		_token = token;
		_env = Collections.unmodifiableMap(new LinkedHashMap<>(env));
	}

	/**
	 * The environment variables of the command, the token among them (see
	 * {@link ProxyTokens#processEnv(String)}); passed to the command only, never to the container.
	 */
	public Map<String, String> env() {
		return _env;
	}

	/**
	 * Revokes the token: the next request presenting it is refused.
	 */
	@Override
	public void close() {
		_tokens.revoke(_token);
	}

	@Override
	public String toString() {
		// Never the values of the environment: they carry the token.
		return "ProcessToken[env=" + _env.keySet() + "]";
	}

}
