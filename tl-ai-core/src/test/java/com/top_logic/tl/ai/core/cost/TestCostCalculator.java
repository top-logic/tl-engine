/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCost;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.TieredCost;

/**
 * What one model request costs under a model's prices.
 *
 * <p>
 * The prices are the shape models.dev publishes: US dollars per million tokens, with optional cache,
 * reasoning and over-200k prices. Round prices make the expected amounts readable.
 * </p>
 */
public class TestCostCalculator extends TestCase {

	private static final double DELTA = 1e-12;

	/** A full price list: input 3, output 15, cache read 0.30, cache write 3.75, reasoning 10. */
	private static final ModelCost FULL = new ModelCost(3, 15, 0.30, 3.75, 10.0, null, null, null);

	/** Each kind of token is charged at its own price. */
	public void testEachKindOfTokenHasItsOwnPrice() {
		CostBreakdown cost = CostCalculator.costOf(FULL,
			new TokenUsage(1_000_000, 2_000_000, 0, 10_000_000, 400_000));

		assertEquals("1 M input at 3.", 3.0, cost.input(), DELTA);
		assertEquals("2 M output at 15.", 30.0, cost.output(), DELTA);
		assertEquals(0.0, cost.reasoning(), DELTA);
		assertEquals("10 M cache reads at 0.30.", 3.0, cost.cacheRead(), DELTA);
		assertEquals("0.4 M cache writes at 3.75.", 1.5, cost.cacheWrite(), DELTA);
		assertEquals(37.5, cost.total(), DELTA);
	}

	/** Reasoning tokens are a share of the output, taken out of it and charged at their own price. */
	public void testReasoningIsAShareOfTheOutput() {
		CostBreakdown cost = CostCalculator.costOf(FULL, new TokenUsage(0, 1_000_000, 400_000, 0, 0));

		assertEquals("The 600 000 output tokens that were not reasoning, at 15.", 9.0, cost.output(), DELTA);
		assertEquals("400 000 reasoning tokens at 10.", 4.0, cost.reasoning(), DELTA);
	}

	/** Without a reasoning price, the reasoning share costs what the rest of the output costs. */
	public void testReasoningWithoutAPriceCostsTheOutputPrice() {
		ModelCost noReasoning = new ModelCost(3, 15, null, null, null, null, null, null);
		CostBreakdown cost = CostCalculator.costOf(noReasoning, new TokenUsage(0, 1_000_000, 400_000, 0, 0));

		assertEquals(15.0, cost.output() + cost.reasoning(), DELTA);
	}

	/** Without cache prices, cached tokens cost the input price: they were sent all the same. */
	public void testCachedTokensWithoutAPriceCostTheInputPrice() {
		ModelCost noCache = new ModelCost(2, 8, null, null, null, null, null, null);
		CostBreakdown cost = CostCalculator.costOf(noCache, new TokenUsage(0, 0, 0, 1_000_000, 500_000));

		assertEquals(2.0, cost.cacheRead(), DELTA);
		assertEquals(1.0, cost.cacheWrite(), DELTA);
	}

	/**
	 * A prompt over 200 000 tokens is charged at the tier's prices, for the whole request; a prompt
	 * at the threshold is not.
	 */
	public void testTheTierAppliesAboveTheThreshold() {
		ModelCost tiered = new ModelCost(3, 15, 0.30, 3.75, null, null, null,
			new TieredCost(6, 22.5, 0.60, 7.5));

		CostBreakdown below = CostCalculator.costOf(tiered, new TokenUsage(150_000, 1_000_000, 0, 50_000, 0));
		assertEquals("200 000 prompt tokens are not over the threshold: 0.15 M at 3.", 0.45, below.input(), DELTA);
		assertEquals(15.0, below.output(), DELTA);
		assertEquals(0.015, below.cacheRead(), DELTA);

		CostBreakdown above = CostCalculator.costOf(tiered, new TokenUsage(150_000, 1_000_000, 0, 50_001, 0));
		assertEquals("One token more and every input token costs the tier's 6.", 0.9, above.input(), DELTA);
		assertEquals("The output of that request costs the tier's 22.50.", 22.5, above.output(), DELTA);
		assertEquals("Cache reads at the tier's 0.60.", 50_001 * 0.60 / 1_000_000, above.cacheRead(), DELTA);
	}

	/** The cached part of the prompt counts towards the threshold: the model read all of it. */
	public void testCachedTokensCountTowardsTheThreshold() {
		ModelCost tiered = new ModelCost(1, 1, 1.0, 1.0, null, null, null, new TieredCost(2, 2, null, null));
		CostBreakdown cost = CostCalculator.costOf(tiered, new TokenUsage(1_000, 0, 0, 250_000, 0));

		assertEquals("The tier names no cache price, so cache reads cost the tier's input price.",
			0.5, cost.cacheRead(), DELTA);
		assertEquals(0.002, cost.input(), DELTA);
	}

	/** A model without a price costs nothing. */
	public void testAnUnknownPriceCostsNothing() {
		assertEquals(CostBreakdown.ZERO,
			CostCalculator.costOf(null, new TokenUsage(1_000_000, 1_000_000, 0, 0, 0)));
	}

	/** A request without a usage costs nothing. */
	public void testNoUsageCostsNothing() {
		assertEquals(CostBreakdown.ZERO, CostCalculator.costOf(FULL, null));
	}
}
