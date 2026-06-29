/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Structured outcome of a subagent run, handed to both result-delivery paths
 * (turn injection and {@code wait_for_subagent}) and read by token-attribution UI.
 *
 * @param finalText      the subagent's final assistant text (or an error summary if failed)
 * @param tokenUsage     tokens the subagent consumed (attributed to this subagent, never
 *                       folded into the parent's budget)
 * @param durationMs     wall-clock run duration in milliseconds
 * @param toolCallCount  number of tool calls the subagent made
 * @param stopReason     why the subagent loop stopped
 */
public record SubagentResult(
		String finalText,
		TokenUsage tokenUsage,
		long durationMs,
		int toolCallCount,
		StopReason stopReason) {
	/** */
	public SubagentResult {
		if (finalText == null) {
			finalText = "";
		}
		if (tokenUsage == null) {
			tokenUsage = TokenUsage.EMPTY;
		}
	}
}
