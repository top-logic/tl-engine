/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Issues the tokens a process in a workspace presents to the workspace proxy.
 *
 * <p>
 * A token belongs to one process: it is passed to it in the {@link #processEnv(String) environment}
 * of its command and revoked when the process ends. A process of the control application itself,
 * e.g. a git command run in the workspace, gets one by {@link #issueProcessToken(String, Duration)}.
 * </p>
 *
 * @see WorkspaceProxyService
 */
public interface ProxyTokens {

	/**
	 * Environment variable of a process in a workspace - never of the container - holding its
	 * token, for every route; part of the {@link #processEnv(String) environment of the process}.
	 */
	String ENV_PROXY_TOKEN = "TL_FACTORY_PROXY_TOKEN";

	/**
	 * Issues a token for a process in the given workspace.
	 *
	 * @param workspaceId
	 *        The workspace the process runs in.
	 * @param lifetime
	 *        How long the token is valid at most.
	 * @return The token, {@code null} if the proxy is switched off.
	 */
	String issue(String workspaceId, Duration lifetime);

	/**
	 * When the given token stops being valid.
	 *
	 * @return The end of its lifetime, {@code null} if it is not valid.
	 */
	Instant expiresAt(String token);

	/**
	 * Revokes a token: the next request presenting it is refused.
	 */
	void revoke(String token);

	/**
	 * The environment variables a process with the given token gets for the routes of the proxy,
	 * e.g. the token for the LLM API.
	 *
	 * <p>
	 * They are passed to that process only, in the environment of its command, never to the
	 * container of the workspace.
	 * </p>
	 *
	 * @param token
	 *        A token {@link #issue(String, Duration) issued} for the process.
	 */
	Map<String, String> processEnv(String token);

	/**
	 * Issues a token for one command in the given workspace, e.g. a git command of the control
	 * application, together with the {@link #processEnv(String) environment} the command gets.
	 *
	 * <p>
	 * The token is revoked when the result is {@link ProcessToken#close() closed}, after the
	 * command has ended:
	 * </p>
	 *
	 * <pre>
	 * try (ProcessToken token = tokens.issueProcessToken(workspaceId, lifetime)) {
	 * 	provider.exec(workspaceId, command.putEnv(token.env()), output);
	 * }
	 * </pre>
	 *
	 * @param workspaceId
	 *        The workspace the command runs in.
	 * @param lifetime
	 *        How long the token is valid at most, the longest time the command may take.
	 * @return The token with its environment, {@code null} if the proxy is switched off.
	 */
	default ProcessToken issueProcessToken(String workspaceId, Duration lifetime) {
		String token = issue(workspaceId, lifetime);
		return token == null ? null : new ProcessToken(this, token, processEnv(token));
	}

}
