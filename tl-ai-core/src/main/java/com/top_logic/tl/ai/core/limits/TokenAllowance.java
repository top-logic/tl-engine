/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.concurrent.atomic.AtomicLong;

import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * What is left of a principal's token budget when a run starts, and what the run has spent of it
 * since.
 *
 * <p>
 * One instance per <em>run tree</em>. A delegated sub-agent shares its parent's instance - see
 * {@code ToolContext.forDelegation} - because a delegate is part of the run that delegated it and
 * spends the same headroom. That is also what keeps a run from multiplying its budget by delegating.
 * </p>
 *
 * <p>
 * The loop records every assistant turn into it and asks between turns whether it is
 * {@link #exhausted()}; a run that is stops with
 * {@link com.top_logic.tl.ai.core.agent.StopReason#BUDGET_EXHAUSTED}, keeping what it had answered
 * so far.
 * </p>
 *
 * @see RunLimits
 */
public final class TokenAllowance {

	/**
	 * The allowance of a run nothing limits.
	 *
	 * <p>
	 * A shared instance, which is safe because it records nothing and is never exhausted. It is what
	 * every context answers until a run start puts a real one in its place, so a loop can ask without
	 * a null check.
	 * </p>
	 */
	public static final TokenAllowance UNLIMITED = new TokenAllowance(-1);

	private final long _headroom;

	private final AtomicLong _spent = new AtomicLong();

	private TokenAllowance(long headroom) {
		_headroom = headroom;
	}

	/**
	 * An allowance of the given size.
	 *
	 * @param headroom
	 *        How many tokens are left of the principal's budget. A value of {@code 0} or less is an
	 *        allowance that is exhausted from the start; use {@link #UNLIMITED} for no limit.
	 */
	public static TokenAllowance of(long headroom) {
		return new TokenAllowance(Math.max(0, headroom));
	}

	/** Whether this allowance bounds anything at all. */
	public boolean isLimited() {
		return _headroom >= 0;
	}

	/** How many tokens were left when the run started, or {@code -1} when it is unlimited. */
	public long headroom() {
		return _headroom;
	}

	/** How many tokens the run tree has spent since it started. */
	public long spent() {
		return _spent.get();
	}

	/**
	 * Adds what one assistant turn cost.
	 *
	 * @param usage
	 *        The turn's usage; {@code null} counts as nothing.
	 */
	public void record(TokenUsage usage) {
		if (usage == null || !isLimited()) {
			return;
		}
		_spent.addAndGet(usage.totalTokens());
	}

	/** Whether the run tree has spent its headroom. */
	public boolean exhausted() {
		return isLimited() && _spent.get() >= _headroom;
	}

	@Override
	public String toString() {
		return isLimited() ? "TokenAllowance[spent=" + spent() + " of " + _headroom + "]"
			: "TokenAllowance[unlimited]";
	}
}
