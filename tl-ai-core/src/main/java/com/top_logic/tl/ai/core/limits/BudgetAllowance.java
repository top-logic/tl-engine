/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * What is left of a principal's token and money budget when a run starts, and what the run has
 * spent of it since.
 *
 * <p>
 * One instance per <em>run tree</em>. A delegated sub-agent shares its parent's instance - see
 * {@code ToolContext.forDelegation} - because a delegate is part of the run that delegated it and
 * spends the same headroom. That is also what keeps a run from multiplying its budget by delegating.
 * </p>
 *
 * <p>
 * The loop records every assistant turn into it - its tokens and what they cost - and asks between
 * turns whether it is {@link #exhausted()}; a run that is stops with
 * {@link com.top_logic.tl.ai.core.agent.StopReason#BUDGET_EXHAUSTED}, keeping what it had answered
 * so far. Each of the two budgets may be limited or not, independently.
 * </p>
 *
 * @see RunLimits
 */
public final class BudgetAllowance {

	/** What {@link #tokenHeadroom()} answers for an allowance that bounds no tokens. */
	public static final long UNLIMITED_TOKENS = -1;

	/**
	 * The allowance of a run nothing limits.
	 *
	 * <p>
	 * A shared instance, which is safe because it records nothing and is never exhausted. It is what
	 * every context answers until a run start puts a real one in its place, so a loop can ask without
	 * a null check.
	 * </p>
	 */
	public static final BudgetAllowance UNLIMITED = new BudgetAllowance(UNLIMITED_TOKENS, null);

	private final long _tokenHeadroom;

	private final MoneyBudget _money;

	private final AtomicLong _spentTokens = new AtomicLong();

	private final DoubleAdder _spentMoney = new DoubleAdder();

	private BudgetAllowance(long tokenHeadroom, MoneyBudget money) {
		_tokenHeadroom = tokenHeadroom;
		_money = money;
	}

	/**
	 * An allowance of the given number of tokens, with no money limit.
	 *
	 * @param tokenHeadroom
	 *        How many tokens are left of the principal's budget. A value of {@code 0} or less is an
	 *        allowance that is exhausted from the start; use {@link #UNLIMITED} for no limit.
	 */
	public static BudgetAllowance of(long tokenHeadroom) {
		return new BudgetAllowance(Math.max(0, tokenHeadroom), null);
	}

	/**
	 * An allowance with the given limits.
	 *
	 * @param tokenHeadroom
	 *        How many tokens are left of the principal's budget, or {@link #UNLIMITED_TOKENS}.
	 * @param money
	 *        The principal's money budget and what of it was spent before the run, or {@code null}
	 *        for no money limit.
	 */
	public static BudgetAllowance of(long tokenHeadroom, MoneyBudget money) {
		if (tokenHeadroom == UNLIMITED_TOKENS && money == null) {
			return UNLIMITED;
		}
		return new BudgetAllowance(tokenHeadroom == UNLIMITED_TOKENS ? UNLIMITED_TOKENS : Math.max(0, tokenHeadroom),
			money);
	}

	/** Whether this allowance bounds anything at all. */
	public boolean isLimited() {
		return isTokenLimited() || isMoneyLimited();
	}

	/** Whether this allowance bounds the tokens the run tree may spend. */
	public boolean isTokenLimited() {
		return _tokenHeadroom >= 0;
	}

	/** Whether this allowance bounds the money the run tree may spend. */
	public boolean isMoneyLimited() {
		return _money != null;
	}

	/** How many tokens were left when the run started, or {@link #UNLIMITED_TOKENS}. */
	public long tokenHeadroom() {
		return _tokenHeadroom;
	}

	/** The money budget this allowance bounds, or {@code null} when it bounds no money. */
	public MoneyBudget moneyBudget() {
		return _money;
	}

	/** How many tokens the run tree has spent since it started. */
	public long spentTokens() {
		return _spentTokens.get();
	}

	/**
	 * How much money the run tree has spent since it started, in the currency of the
	 * {@link #moneyBudget() money budget}; {@code 0} when it bounds no money.
	 */
	public double spentMoney() {
		return _spentMoney.sum();
	}

	/**
	 * Adds what one assistant turn used and cost.
	 *
	 * @param usage
	 *        The turn's usage; {@code null} counts as nothing.
	 * @param cost
	 *        What the turn cost; {@code null} counts as nothing. An amount in US dollars, as a run
	 *        records it, is converted at the {@link MoneyBudget#usdRate() rate} of the money budget;
	 *        an amount in the budget's own currency counts as it is, and one in a third currency is
	 *        not counted.
	 */
	public void record(TokenUsage usage, Money cost) {
		if (usage != null && isTokenLimited()) {
			_spentTokens.addAndGet(usage.totalTokens());
		}
		if (cost != null && isMoneyLimited()) {
			if (_money.currency().equals(cost.currency())) {
				_spentMoney.add(cost.amount());
			} else if (Money.USD.equals(cost.currency())) {
				_spentMoney.add(cost.amount() * _money.usdRate());
			}
		}
	}

	/** Whether the run tree has spent either of its budgets. */
	public boolean exhausted() {
		return tokensExhausted() || moneyExhausted();
	}

	/** Whether the run tree has spent its token headroom. */
	public boolean tokensExhausted() {
		return isTokenLimited() && _spentTokens.get() >= _tokenHeadroom;
	}

	/** Whether the run tree has spent its money headroom. */
	public boolean moneyExhausted() {
		return isMoneyLimited() && spentMoney() >= _money.headroom();
	}

	@Override
	public String toString() {
		if (!isLimited()) {
			return "BudgetAllowance[unlimited]";
		}
		return "BudgetAllowance[tokens="
			+ (isTokenLimited() ? spentTokens() + " of " + _tokenHeadroom : "unlimited")
			+ ", money=" + (isMoneyLimited() ? spentMoney() + " of " + _money.headroom() + " " + _money.currency()
				: "unlimited")
			+ "]";
	}

	/**
	 * A principal's money budget for the window, and what of it the principal had spent when the
	 * run started.
	 *
	 * @param budget
	 *        What the principal may spend in the window; positive.
	 * @param spentBefore
	 *        What the principal's recorded runs in the window had cost when this run started.
	 * @param currency
	 *        The ISO 4217 code both amounts are in: the system currency.
	 * @param usdRate
	 *        How many units of {@code currency} one US dollar is worth on the day the run started;
	 *        what the run's own costs, recorded in US dollars, are converted with.
	 */
	public record MoneyBudget(double budget, double spentBefore, String currency, double usdRate) {

		/** What was left of the budget when the run started; {@code 0} or more. */
		public double headroom() {
			return Math.max(0, budget - spentBefore);
		}
	}
}
