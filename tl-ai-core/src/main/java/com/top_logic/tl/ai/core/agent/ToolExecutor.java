/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;

/**
 * Executes a tool call requested by the LLM.
 *
 * <p>Contract: always return a {@link ToolResultMessage}, never throw. Tool errors
 * must be returned as a result with {@link com.top_logic.tl.ai.core.message.ToolState.Failed}
 * state — the agent loop feeds them back to the LLM as content.</p>
 *
 * @see AgentLoop
 */
@FunctionalInterface
public interface ToolExecutor {

	/**
	 * Execute the given tool call.
	 *
	 * @param toolCall the tool call from the LLM's response
	 * @param context  execution context (security, transaction, progress)
	 * @return the result (success or failure, never {@code null})
	 */
	ToolResultMessage execute(ToolCall toolCall, ToolContext context);

	// TODO: Parallel tool execution (partition read-only vs write, execute read-only batch concurrently)
	// TODO: Permission check before execute (security context flows down)
	// TODO: Cancellation support (abort long-running tools)
}
