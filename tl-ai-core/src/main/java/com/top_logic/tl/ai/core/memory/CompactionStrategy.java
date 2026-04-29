/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.util.List;

import com.top_logic.tl.ai.core.message.Message;

/**
 * Strategy for compacting conversation history to fit within a token budget.
 *
 * <p>Implementations decide how to reduce the message list: by dropping old messages
 * ({@link TruncatingStrategy}) or by summarizing them via an LLM call
 * ({@link SummarizingStrategy}).</p>
 *
 * @see TruncatingStrategy
 * @see SummarizingStrategy
 */
public interface CompactionStrategy {

	/**
	 * Compact the message list to fit within the target token budget.
	 *
	 * @param messages     the full message list (oldest first)
	 * @param targetTokens the token budget the result should fit within
	 * @param estimator    estimates token cost of messages
	 * @return the compaction result with replacement messages and metadata
	 */
	CompactionResult compact(List<Message> messages, int targetTokens, TokenEstimator estimator);
}
