/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.time.Duration;
import java.time.Instant;

/**
 * Issues the tokens a process in a workspace presents to the LLM proxy.
 *
 * <p>
 * A token belongs to one process: it is passed to it in {@link LLMProxyService#ENV_AUTH_TOKEN} of
 * its command and revoked when the process ends.
 * </p>
 *
 * @see LLMProxyService
 */
public interface LLMTokens {

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

}
