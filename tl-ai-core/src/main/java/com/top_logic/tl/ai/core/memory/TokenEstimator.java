/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.util.List;

import com.top_logic.tl.ai.core.message.Message;

/**
 * Estimates the token cost of messages for budget tracking and compaction decisions.
 *
 * <p>Implementations are stateless — the same message always produces the same estimate.
 * Estimates need not be exact (real tokenization is expensive); they must be consistent
 * and good enough to trigger compaction at the right time.</p>
 *
 * @see ByteBasedTokenEstimator
 */
public interface TokenEstimator {

	/**
	 * Estimate the token cost of a single message.
	 */
	int estimateTokens(Message message);

	/**
	 * Estimate the total token cost of a list of messages.
	 */
	int estimateTokens(List<Message> messages);

	/**
	 * Estimate the token cost of a block of prose, such as a system prompt.
	 *
	 * <p>
	 * Prose and JSON are charged at different rates, so a caller that knows which it holds must say
	 * so rather than pass both through one method.
	 * </p>
	 *
	 * @param text
	 *        the text to estimate; {@code null} counts as nothing
	 */
	int estimateText(String text);

	/**
	 * Estimate the token cost of a JSON document, such as a tool schema.
	 *
	 * @param json
	 *        the JSON to estimate; {@code null} counts as nothing
	 */
	int estimateJson(String json);
}
