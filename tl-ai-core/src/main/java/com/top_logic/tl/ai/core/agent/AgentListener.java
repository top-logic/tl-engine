/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.ProviderException;

/**
 * Receives progress events from an {@link AgentLoop} run.
 *
 * <p>All methods have default no-op implementations. Override only what you need.
 * Methods are called on the agent loop thread — no concurrency concerns.</p>
 *
 * @see AgentLoop
 */
public interface AgentListener {

	/** Called at the start of each LLM call iteration. */
	default void onTurnStart(int turnNumber) {
		// no-op
	}

	/** Called when a turn completes (after LLM response and all tool executions for that turn). */
	default void onTurnComplete(int turnNumber, TokenUsage usage) {
		// no-op
	}

	/** Called for each text token streamed from the LLM. */
	default void onTextDelta(String text) {
		// no-op
	}

	/** Called before a tool is executed. */
	default void onToolCallStart(ToolCall toolCall) {
		// no-op
	}

	/** Called after a tool executes successfully. */
	default void onToolCallComplete(ToolCall toolCall, ToolResultMessage result) {
		// no-op
	}

	/** Called when a tool execution fails (error is fed back to LLM). */
	default void onToolError(ToolCall toolCall, String error) {
		// no-op
	}

	/** Called on an unrecoverable provider error (loop will stop). */
	default void onError(ProviderException error) {
		// no-op
	}

	/** Called for each thinking token streamed from the LLM (extended thinking). */
	default void onThinkingDelta(String text) {
		// no-op
	}

	// TODO: onCompactionStart/Complete (context compaction)
	// TODO: onPermissionCheck (security context)
}
