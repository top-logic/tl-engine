/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

/**
 * Result of a background agent execution.
 *
 * @param response
 *        The raw LLM response; may be {@code null}.
 * @param blockReason
 *        The block reason if the agent called {@code block_turn}; {@code null} otherwise.
 */
public record ExecutionResult(String response, String blockReason) {

	/**
	 * Whether the agent requested a turn block.
	 */
	public boolean isBlocked() {
		return blockReason != null;
	}
}
