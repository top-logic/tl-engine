/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
	 * @param tokensBefore the authoritative token count for {@code messages}, used for the
	 *                     "already under budget" check and reported back on the result.
	 *                     Prefer a caller's own accurate figure (e.g.
	 *                     {@link ConversationMemory#estimateTokens()}, which uses the real
	 *                     API-reported prompt size once a baseline exists) over
	 *                     {@code estimator.estimateTokens(messages)}: the estimator only sees
	 *                     the message list, never the system prompt or tool schemas that are
	 *                     also part of the real request and count toward the actual budget.
	 * @return the compaction result with replacement messages and metadata
	 */
	CompactionResult compact(List<Message> messages, int targetTokens, TokenEstimator estimator, int tokensBefore);

	/**
	 * Convenience overload for callers with no more accurate figure than the estimator
	 * itself can provide (e.g. ad-hoc/manual use, tests exercising a strategy in isolation).
	 * Production compaction goes through {@link ConversationMemory}, which always supplies
	 * its own accurate {@code tokensBefore}.
	 */
	default CompactionResult compact(List<Message> messages, int targetTokens, TokenEstimator estimator) {
		return compact(messages, targetTokens, estimator, estimator.estimateTokens(messages));
	}
}
