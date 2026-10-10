/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.limits.BudgetAllowance.MoneyBudget;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * What a run's remaining budget counts, and who shares it.
 */
public class TestBudgetAllowance extends TestCase {

	/** Reading the configuration asks {@code AgentRuntimeService} whether it runs, which needs the modules. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestBudgetAllowance.class));
	}

	/** Nothing is limited until a run start says otherwise. */
	public void testAContextIsUnlimitedByDefault() {
		assertSame(BudgetAllowance.UNLIMITED, ToolContext.unrestricted().budgetAllowance());
		assertFalse(BudgetAllowance.UNLIMITED.isLimited());
		assertFalse(BudgetAllowance.UNLIMITED.exhausted());
	}

	/** An unlimited allowance records nothing and never runs out. */
	public void testUnlimitedStaysUnlimited() {
		BudgetAllowance.UNLIMITED.record(new TokenUsage(1_000_000, 0, 0, 0, 0), null);
		assertEquals(0, BudgetAllowance.UNLIMITED.spentTokens());
		assertFalse(BudgetAllowance.UNLIMITED.exhausted());
	}

	/** Everything the provider read and wrote counts, cached prompt included. */
	public void testEveryComponentOfTheUsageCounts() {
		BudgetAllowance allowance = BudgetAllowance.of(1000);
		allowance.record(new TokenUsage(10, 50, 30, 40, 50), null);
		assertEquals("10 input + 50 output (30 of them thinking) + 40 cache read + 50 cache write.",
			150, allowance.spentTokens());
		assertFalse(allowance.exhausted());
	}

	/** Reaching the headroom exactly is reaching it. */
	public void testExhaustedAtTheHeadroom() {
		BudgetAllowance allowance = BudgetAllowance.of(100);
		allowance.record(new TokenUsage(99, 0, 0, 0, 0), null);
		assertFalse(allowance.exhausted());
		allowance.record(new TokenUsage(1, 0, 0, 0, 0), null);
		assertTrue(allowance.exhausted());
	}

	/** A run admitted with nothing left is exhausted before it records anything. */
	public void testNoHeadroomIsExhaustedFromTheStart() {
		assertTrue(BudgetAllowance.of(0).exhausted());
		assertTrue("A negative remainder is not unlimited.", BudgetAllowance.of(-5).exhausted());
		assertTrue(BudgetAllowance.of(-5).isLimited());
	}

	/** A missing usage is nothing, not a failure. */
	public void testNullUsageCountsAsNothing() {
		BudgetAllowance allowance = BudgetAllowance.of(10);
		allowance.record(null, null);
		assertEquals(0, allowance.spentTokens());
	}

	/** The money headroom is the budget minus what was spent before the run; a turn's cost uses it up. */
	public void testMoneyIsExhaustedAtTheHeadroom() {
		BudgetAllowance allowance = BudgetAllowance.of(BudgetAllowance.UNLIMITED_TOKENS,
			new MoneyBudget(15, 12.40, "EUR"));
		assertTrue(allowance.isMoneyLimited());
		assertFalse("No token budget was configured.", allowance.isTokenLimited());

		allowance.record(new TokenUsage(1_000_000, 0, 0, 0, 0), new Money(2.50, "EUR"));
		assertFalse("12.40 + 2.50 is below 15.", allowance.exhausted());
		allowance.record(new TokenUsage(10, 0, 0, 0, 0), new Money(0.10, "EUR"));
		assertTrue("12.40 + 2.60 reaches 15.", allowance.moneyExhausted());
		assertTrue(allowance.exhausted());
		assertFalse("The money ran out, not the tokens.", allowance.tokensExhausted());
	}

	/** Tokens and money are bounded together: whichever runs out first ends the run. */
	public void testEitherBudgetExhaustsTheAllowance() {
		BudgetAllowance allowance = BudgetAllowance.of(100, new MoneyBudget(1, 0, "USD"));
		allowance.record(new TokenUsage(100, 0, 0, 0, 0), new Money(0.01, "USD"));
		assertTrue(allowance.tokensExhausted());
		assertFalse(allowance.moneyExhausted());
		assertTrue(allowance.exhausted());
	}

	/** A cost in another currency than the budget's is not counted against it. */
	public void testACostInAnotherCurrencyIsNotCounted() {
		BudgetAllowance allowance = BudgetAllowance.of(BudgetAllowance.UNLIMITED_TOKENS,
			new MoneyBudget(1, 0, "EUR"));
		allowance.record(TokenUsage.EMPTY, new Money(5, "USD"));
		assertEquals(0.0, allowance.spentMoney());
	}

	/** Without any limit the general factory answers the shared unlimited allowance. */
	public void testNoLimitIsUnlimited() {
		assertSame(BudgetAllowance.UNLIMITED, BudgetAllowance.of(BudgetAllowance.UNLIMITED_TOKENS, null));
	}

	/**
	 * A delegate spends its parent's budget, not one of its own.
	 *
	 * <p>
	 * The rule that keeps delegation from being a way around the ceiling: were the child given its
	 * own allowance, an agent that delegates three times would have four times the budget of one
	 * that does the same work itself.
	 * </p>
	 */
	public void testADelegateSharesTheParentsAllowance() {
		BudgetAllowance allowance = BudgetAllowance.of(100);
		ToolContext parent = ToolContext.unrestricted().withBudgetAllowance(allowance);
		ToolContext child = parent.forDelegation(TransactionScope.NOOP);
		ToolContext grandchild = child.forDelegation(TransactionScope.NOOP);

		assertSame(allowance, child.budgetAllowance());
		assertSame(allowance, grandchild.budgetAllowance());

		child.budgetAllowance().record(new TokenUsage(60, 0, 0, 0, 0), null);
		grandchild.budgetAllowance().record(new TokenUsage(40, 0, 0, 0, 0), null);
		assertTrue("What the children spent is what the parent has left.",
			parent.budgetAllowance().exhausted());
	}

	/** A detached subagent shares it too: it is still part of the run that spawned it. */
	public void testADetachedSubagentSharesTheParentsAllowance() {
		BudgetAllowance allowance = BudgetAllowance.of(100);
		ToolContext parent = ToolContext.unrestricted().withBudgetAllowance(allowance);
		ToolContext child = parent.forDetachedSubagent(TransactionScope.NOOP,
			new com.top_logic.tl.ai.tool.api.CancellationToken());
		assertSame(allowance, child.budgetAllowance());
	}

	/** Every other copy of a context carries it too. */
	public void testTheCopiesCarryIt() {
		BudgetAllowance allowance = BudgetAllowance.of(100);
		ToolContext context = ToolContext.unrestricted().withBudgetAllowance(allowance);
		assertSame(allowance, context.withModel("some-model").budgetAllowance());
		assertSame(allowance, context.withToolCallId("call-1").budgetAllowance());
		assertSame(allowance,
			context.withProgressReporter(com.top_logic.tl.ai.tool.api.ProgressReporter.NOOP).budgetAllowance());
	}
}
