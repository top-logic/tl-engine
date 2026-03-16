/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

/**
 * Thread-local storage for {@code block_turn} tool invocations.
 *
 * <p>
 * When a background agent calls the {@code block_turn} tool, the reason is stored here. After
 * agent execution, the executor checks this to determine if the turn should be blocked.
 * </p>
 */
public class BlockTurnResult {

	private static final ThreadLocal<String> BLOCK_REASON = new ThreadLocal<>();

	/**
	 * Records a block request with the given reason.
	 *
	 * @param reason
	 *        The reason for blocking.
	 */
	public static void block(String reason) {
		BLOCK_REASON.set(reason);
	}

	/**
	 * Returns and clears the block reason, or {@code null} if no block was requested.
	 */
	public static String consumeBlockReason() {
		String reason = BLOCK_REASON.get();
		BLOCK_REASON.remove();
		return reason;
	}

	/**
	 * Clears any pending block.
	 */
	public static void clear() {
		BLOCK_REASON.remove();
	}
}
