/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.memory.CompactionResult;
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

	/** Called when context compaction starts. */
	default void onCompactionStart(int tokensBefore) {
		// no-op
	}

	/** Called when context compaction completes. */
	default void onCompactionComplete(CompactionResult result) {
		// no-op
	}

	/**
	 * Called when a tool call requires user approval.
	 *
	 * <p>Fired before the {@link ApprovalCallback} blocks. The UI can use this to
	 * show a pending approval indicator.</p>
	 *
	 * @param toolCall the tool call awaiting approval
	 * @param request  the approval request details
	 */
	default void onApprovalRequired(ToolCall toolCall, ApprovalRequest request) {
		// no-op
	}

	/**
	 * Called when a tool call is denied by the system or security check.
	 *
	 * @param toolCall the denied tool call
	 * @param reason   the denial reason (same text fed to the LLM)
	 */
	default void onPermissionDenied(ToolCall toolCall, String reason) {
		// no-op
	}

	/**
	 * Called when a KB transaction begins (first write tool in a turn).
	 */
	default void onTransactionBegin() {
		// no-op
	}

	/**
	 * Called when a KB transaction commits successfully.
	 *
	 * @param revisionNumber the committed revision number, or -1 if no changes were made
	 */
	default void onTransactionCommit(long revisionNumber) {
		// no-op
	}

	/**
	 * Called when a KB transaction is rolled back.
	 *
	 * @param reason the reason for rollback (typically an error message)
	 */
	default void onTransactionRollback(String reason) {
		// no-op
	}

	/**
	 * Called when the global edit lock is acquired.
	 */
	default void onLockAcquired() {
		// no-op
	}

	/**
	 * Called when the global edit lock cannot be acquired.
	 *
	 * @param reason the lock conflict details (who holds the lock)
	 */
	default void onLockFailed(String reason) {
		// no-op
	}
}
