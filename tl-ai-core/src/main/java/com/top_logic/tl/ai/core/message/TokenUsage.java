/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

/**
 * Token usage from an LLM response.
 *
 * <p>Carried on {@link AssistantMessage} for budget tracking and compaction decisions.</p>
 *
 * @param inputTokens      tokens in the request (prompt)
 * @param outputTokens     tokens in the response (completion)
 * @param thinkingTokens   tokens used for extended thinking (0 if not supported)
 * @param cacheReadTokens  tokens read from provider cache
 * @param cacheWriteTokens tokens written to provider cache
 */
public record TokenUsage(
		int inputTokens,
		int outputTokens,
		int thinkingTokens,
		int cacheReadTokens,
		int cacheWriteTokens) {

	/** Empty usage with all zeros. */
	public static final TokenUsage EMPTY = new TokenUsage(0, 0, 0, 0, 0);

	/**
	 * Total tokens consumed (input + output + thinking).
	 */
	public int totalTokens() {
		return inputTokens + outputTokens + thinkingTokens;
	}

	/**
	 * Add two usages together (for cumulative tracking).
	 */
	public TokenUsage add(TokenUsage other) {
		return new TokenUsage(
			inputTokens + other.inputTokens,
			outputTokens + other.outputTokens,
			thinkingTokens + other.thinkingTokens,
			cacheReadTokens + other.cacheReadTokens,
			cacheWriteTokens + other.cacheWriteTokens);
	}
}
