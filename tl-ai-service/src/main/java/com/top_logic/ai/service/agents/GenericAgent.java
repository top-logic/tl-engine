/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Generic agent interface used by {@link AgentRegistry} delegation tools to invoke any configured
 * agent by name at runtime.
 */
public interface GenericAgent {

	/**
	 * Invokes the agent with the given message.
	 *
	 * @param message
	 *        The message to send to the agent.
	 * @return The agent's response.
	 */
	@UserMessage("{{message}}")
	@Agent("Generic agent invoked via runtime delegation")
	String invoke(@V("message") String message);
}
