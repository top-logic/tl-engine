/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.factory.proxy.ProxyTokens;

/**
 * What an {@link AgentHarness} needs to start an agent process.
 *
 * @param workingDir
 *        The directory in the workspace the agent works in: the application's sources.
 * @param resumeSessionId
 *        The session ID of the conversation the process continues, {@code null} for a new
 *        conversation.
 * @param env
 *        The environment variables of the process, e.g. its token for the workspace proxy (see
 *        {@link ProxyTokens#processEnv(String)}); passed to the process only, in the environment
 *        of its command, never to the container.
 */
public record AgentLaunch(String workingDir, String resumeSessionId, Map<String, String> env) {

	/**
	 * Creates an {@link AgentLaunch}.
	 */
	public AgentLaunch {
		env = env == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(env));
	}

	@Override
	public String toString() {
		// Never the values of the environment: they carry the token.
		return "AgentLaunch[workingDir=" + workingDir + ", resumeSessionId=" + resumeSessionId + ", env="
			+ env.keySet() + "]";
	}

}
