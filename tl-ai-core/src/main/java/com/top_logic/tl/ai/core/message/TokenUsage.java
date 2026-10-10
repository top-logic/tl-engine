/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

/**
 * Token usage from an LLM response.
 *
 * <p>Carried on {@link AssistantMessage} for budget tracking and compaction decisions.</p>
 *
 * <p><b>Convention:</b> {@code inputTokens} counts only the <em>non-cached</em> part of the
 * prompt; cached tokens are reported exclusively in {@code cacheReadTokens} /
 * {@code cacheWriteTokens}. Providers whose API reports a cache-inclusive prompt count
 * (<code>OpenAI</code>-style {@code prompt_tokens}) must subtract the cached share when constructing
 * this record. Use {@link #promptTokens()} for the full prompt size.</p>
 *
 * <p><b>Convention:</b> {@code thinkingTokens} is the share of {@code outputTokens} the model spent
 * on reasoning, not a count beside it - which is how every provider that reports it states it
 * (<code>OpenAI</code>'s {@code output_tokens_details.reasoning_tokens}). A provider that reports
 * reasoning separately must add it to {@code outputTokens} when constructing this record.</p>
 *
 * @param inputTokens      non-cached tokens in the request (prompt)
 * @param outputTokens     tokens in the response (completion), reasoning included
 * @param thinkingTokens   the share of {@code outputTokens} spent on reasoning (0 if not reported)
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
	 * Creates a {@link TokenUsage} from a provider whose prompt-token count already
	 * <em>includes</em> the cached share (e.g. <code>OpenAI</code>-style {@code prompt_tokens}).
	 *
	 * <p>Centralizes the cache-exclusive-{@code inputTokens} convention documented on this
	 * record, so providers construct usage from the raw API fields without each
	 * re-implementing the subtraction (and risking silently reintroducing a cache-inclusive
	 * {@code inputTokens} that would corrupt compaction's token estimate).</p>
	 *
	 * @param cacheInclusivePromptTokens the provider's raw prompt-token count, including cache
	 * @param cacheReadTokens            tokens read from cache, out of the total above
	 * @param outputTokens               tokens in the response
	 * @param thinkingTokens             the share of {@code outputTokens} spent on reasoning, or 0
	 */
	public static TokenUsage fromCacheInclusivePrompt(int cacheInclusivePromptTokens,
			int cacheReadTokens, int outputTokens, int thinkingTokens) {
		int inputTokens = Math.max(0, cacheInclusivePromptTokens - cacheReadTokens);
		return new TokenUsage(inputTokens, outputTokens, thinkingTokens, cacheReadTokens, 0);
	}

	/**
	 * Total prompt size in tokens, including cached parts.
	 *
	 * <p>This is the number to compare against a model's context window — with prompt
	 * caching active, {@link #inputTokens()} alone can be a tiny fraction of the real
	 * prompt.</p>
	 */
	public int promptTokens() {
		return inputTokens + cacheReadTokens + cacheWriteTokens;
	}

	/**
	 * Total tokens consumed: the full prompt plus the output.
	 *
	 * <p>Thinking is not added: it is a part of {@link #outputTokens()} already (#730).</p>
	 */
	public int totalTokens() {
		return promptTokens() + outputTokens;
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
