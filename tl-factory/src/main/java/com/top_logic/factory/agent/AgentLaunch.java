/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

/**
 * What an {@link AgentHarness} needs to start an agent process.
 *
 * @param workingDir
 *        The directory in the workspace the agent works in: the application's sources.
 * @param resumeSessionId
 *        The session ID of the conversation the process continues, {@code null} for a new
 *        conversation.
 * @param token
 *        The token the process presents to the LLM proxy; passed to the process only, in the
 *        environment of its command.
 */
public record AgentLaunch(String workingDir, String resumeSessionId, String token) {

	@Override
	public String toString() {
		// Never the token.
		return "AgentLaunch[workingDir=" + workingDir + ", resumeSessionId=" + resumeSessionId + "]";
	}

}
