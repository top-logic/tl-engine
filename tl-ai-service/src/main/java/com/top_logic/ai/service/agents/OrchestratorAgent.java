/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.memory.ChatMemory;

/**
 * Single entrypoint for chat: the LLM plans steps and delegates to specialists as needed (zero, one, or several).
 * <p>
 * The chat UI sends user messages here. The orchestrator thinks: what does the user want, what steps are needed,
 * do any need a specialist? It may call zero tools (e.g. ask for clarification), one, or multiple in sequence
 * (e.g. Explorer for model context, then TLScript for how to use it). After each tool result it reasons: is it
 * enough? When satisfied, it synthesizes and returns the final answer. See {@link OrchestratorRouter} for the
 * agent interface and {@link AgentDelegationTools} for the delegate tools.
 * </p>
 * <p>
 * The caller must set the run's trace (e.g. {@link com.top_logic.ai.service.tools.ToolCallTracking#setTracker})
 * before calling {@link #chat(ChatMemory, String)} so agent and tool calls are recorded.
 * </p>
 */
public final class OrchestratorAgent {

	/**
	 * Handles a user message: LLM plans steps, calls zero/one/multiple specialist tools as needed, synthesizes result.
	 *
	 * @param chatMemory
	 *        Conversation memory that persists across messages in this session. The orchestrator's
	 *        system prompt is always retained; older user/assistant turns are evicted when
	 *        the window is full.
	 * @param userMessage
	 *        Raw user input (may include legacy prefixes like /explore-model or /tlscript).
	 * @return The delegated specialist's answer, or a short fallback.
	 */
	public static String chat(ChatMemory chatMemory, String userMessage) {
		if (userMessage == null || userMessage.trim().isEmpty()) {
			return "Please ask a question about the model, TLScript, or model creation.";
		}
		String trimmed = userMessage.trim();

		AgentRegistry agentRegistry = AgentRegistry.getInstance();
		if (agentRegistry == null) {
			return "AgentRegistry is not available. Please ensure the service is started.";
		}

		OrchestratorRouter router = agentRegistry.buildAgent(
			"orchestrator", OrchestratorRouter.class, chatMemory);

		return ThreadContextManager.inSystemInteraction(OrchestratorAgent.class,
			() -> router.chat(trimmed));
	}

	private OrchestratorAgent() {
		// utility
	}
}
