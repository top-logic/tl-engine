/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;

import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * The result of an {@link AgentLoop#run} invocation.
 *
 * @param stopReason why the loop stopped
 * @param turnsUsed  number of LLM call iterations completed
 * @param totalUsage cumulative token usage across all turns
 *
 * @see AgentLoop
 */
public record AgentResult(StopReason stopReason, int turnsUsed, TokenUsage totalUsage) {

	/** */
	public AgentResult {
		Objects.requireNonNull(stopReason, "Stop reason must not be null.");
		Objects.requireNonNull(totalUsage, "Total usage must not be null.");
		if (turnsUsed < 0) {
			throw new IllegalArgumentException("Turns used must not be negative.");
		}
	}
}
