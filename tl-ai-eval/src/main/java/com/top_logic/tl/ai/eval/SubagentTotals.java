/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

/**
 * What the background subagents of one run spent, summed over all of them.
 *
 * <p>
 * A research lead spends a fraction of what its workers spend, so a row that counted the lead alone
 * would compare the cheap half of two arms. The subagents are runs of their own, with their own
 * stream of events; their totals are taken from their jobs when the lead's run is over.
 * </p>
 *
 * @param runs
 *        How many subagents the run started.
 * @param inputTokens
 *        Their prompt tokens, including the cached share.
 * @param cacheReadTokens
 *        The cached share of {@code inputTokens}.
 * @param outputTokens
 *        Their completion tokens.
 * @param toolCalls
 *        The tool calls they made.
 */
public record SubagentTotals(int runs, int inputTokens, int cacheReadTokens, int outputTokens, int toolCalls) {

	/** For a run that started no subagent. */
	public static final SubagentTotals NONE = new SubagentTotals(0, 0, 0, 0, 0);
}
