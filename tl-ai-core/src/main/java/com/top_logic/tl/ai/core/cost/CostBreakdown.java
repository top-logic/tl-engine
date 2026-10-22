/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

/**
 * What one model request cost, in US dollars, split by the kind of token it was charged for.
 *
 * @param input
 *        The prompt tokens that were not read from the provider's cache.
 * @param output
 *        The response tokens, without the reasoning share.
 * @param reasoning
 *        The share of the response the model spent on reasoning.
 * @param cacheRead
 *        The prompt tokens read from the provider's cache.
 * @param cacheWrite
 *        The prompt tokens written to the provider's cache.
 *
 * @see CostCalculator
 */
public record CostBreakdown(double input, double output, double reasoning, double cacheRead,
		double cacheWrite) {

	/** A request that cost nothing. */
	public static final CostBreakdown ZERO = new CostBreakdown(0, 0, 0, 0, 0);

	/** The sum of all five parts. */
	public double total() {
		return input + output + reasoning + cacheRead + cacheWrite;
	}
}
