/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCost;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.TieredCost;

/**
 * What one model request cost: its token counts times the model's prices.
 *
 * <p>
 * The prices are per million tokens, in US dollars, as the models.dev catalog states them (see
 * {@link ModelCost}). Each kind of token has its own price:
 * </p>
 *
 * <ul>
 * <li><b>Input</b> - {@link TokenUsage#inputTokens()}, the prompt without its cached part.</li>
 * <li><b>Cache read</b> and <b>cache write</b> - the cached part of the prompt. A price list that
 * gives no cache price charges these tokens at the input price: they were sent, and charging them
 * at nothing would make a run look cheaper than its bill.</li>
 * <li><b>Reasoning</b> - {@link TokenUsage#thinkingTokens()}. Every provider that reports them
 * reports them as a share of {@link TokenUsage#outputTokens()}, so that share is taken out of the
 * output and charged at the reasoning price; without a reasoning price it is charged at the output
 * price, which is what the provider does.</li>
 * <li><b>Output</b> - the rest of the response.</li>
 * </ul>
 *
 * <p>
 * <b>Tiered prices.</b> Some models cost more per token when one request carries a long prompt. The
 * catalog states that as a second price list for prompts over {@link #TIER_THRESHOLD} tokens, and
 * it applies to the whole request, not to the tokens above the threshold. That is why a cost is
 * computed per request: summed over a run, the prompts no longer say which request crossed it.
 * </p>
 *
 * @see RunCosts
 */
public final class CostCalculator {

	/**
	 * How many prompt tokens a request may carry before the {@link ModelCost#tieredCost() tiered
	 * prices} apply - the {@code context_over_200k} entry of the catalog.
	 */
	public static final int TIER_THRESHOLD = 200_000;

	private static final double PER_MILLION = 1_000_000d;

	private CostCalculator() {
		// Static utility.
	}

	/**
	 * What the given request cost under the given prices.
	 *
	 * @param price
	 *        The model's prices; {@code null} for a model without a price, which costs nothing.
	 * @param usage
	 *        What the request used; {@code null} counts as nothing.
	 * @return The cost in US dollars, split by kind of token; never {@code null}.
	 */
	public static CostBreakdown costOf(ModelCost price, TokenUsage usage) {
		if (price == null || usage == null) {
			return CostBreakdown.ZERO;
		}
		TieredCost tier = price.tieredCost();
		boolean tiered = tier != null && usage.promptTokens() > TIER_THRESHOLD;

		double input = tiered ? tier.input() : price.input();
		double output = tiered ? tier.output() : price.output();
		double cacheRead = orElse(tiered ? tier.cacheRead() : price.cacheRead(), input);
		double cacheWrite = orElse(tiered ? tier.cacheWrite() : price.cacheWrite(), input);
		// The tier names no reasoning price; the tier's output price is what the response costs.
		double reasoning = tiered ? output : orElse(price.reasoning(), output);

		int reasoningTokens = Math.max(0, Math.min(usage.thinkingTokens(), usage.outputTokens()));
		int outputTokens = usage.outputTokens() - reasoningTokens;

		return new CostBreakdown(
			cost(usage.inputTokens(), input),
			cost(outputTokens, output),
			cost(reasoningTokens, reasoning),
			cost(usage.cacheReadTokens(), cacheRead),
			cost(usage.cacheWriteTokens(), cacheWrite));
	}

	private static double cost(int tokens, double pricePerMillion) {
		return tokens <= 0 ? 0 : tokens * pricePerMillion / PER_MILLION;
	}

	private static double orElse(Double price, double fallback) {
		return price != null ? price.doubleValue() : fallback;
	}
}
