/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * LLM-based orchestrator: infers what the user wants, plans steps, calls zero, one, or
 * multiple specialists in sequence, and synthesizes a final answer when it has enough.
 *
 * @see OrchestratorAgent#chat(ChatMemory, String)
 */
public interface OrchestratorRouter {

	/**
	 * Chat method for orchestrating AI responses with specialist delegation.
	 *
	 * @param userMessage
	 *        The user's message
	 * @return The orchestrated response after delegating to appropriate specialists
	 */
	@UserMessage("""
		User message: {{userMessage}}

		Think: what does the user want, what steps are needed, do any need a specialist? Call zero, one, or several specialist tools as needed. After each result, ask yourself: is it enough? When you have enough, synthesize and respond to the user.
		""")
	@Agent("Orchestrator: plans steps, calls Model Explorer / Model Creator / TLScript specialist as needed (0..N), synthesizes answer")
	String chat(@V("userMessage") String userMessage);
}
