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
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * What a run's remaining budget counts, and who shares it.
 */
public class TestTokenAllowance extends TestCase {

	/** Reading the configuration asks {@code LLMService} whether it runs, which needs the modules. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestTokenAllowance.class));
	}

	/** Nothing is limited until a run start says otherwise. */
	public void testAContextIsUnlimitedByDefault() {
		assertSame(TokenAllowance.UNLIMITED, ToolContext.unrestricted().tokenAllowance());
		assertFalse(TokenAllowance.UNLIMITED.isLimited());
		assertFalse(TokenAllowance.UNLIMITED.exhausted());
	}

	/** An unlimited allowance records nothing and never runs out. */
	public void testUnlimitedStaysUnlimited() {
		TokenAllowance.UNLIMITED.record(new TokenUsage(1_000_000, 0, 0, 0, 0));
		assertEquals(0, TokenAllowance.UNLIMITED.spent());
		assertFalse(TokenAllowance.UNLIMITED.exhausted());
	}

	/** Everything the provider read and wrote counts, cached prompt included. */
	public void testEveryComponentOfTheUsageCounts() {
		TokenAllowance allowance = TokenAllowance.of(1000);
		allowance.record(new TokenUsage(10, 20, 30, 40, 50));
		assertEquals("10 input + 20 output + 30 thinking + 40 cache read + 50 cache write.",
			150, allowance.spent());
		assertFalse(allowance.exhausted());
	}

	/** Reaching the headroom exactly is reaching it. */
	public void testExhaustedAtTheHeadroom() {
		TokenAllowance allowance = TokenAllowance.of(100);
		allowance.record(new TokenUsage(99, 0, 0, 0, 0));
		assertFalse(allowance.exhausted());
		allowance.record(new TokenUsage(1, 0, 0, 0, 0));
		assertTrue(allowance.exhausted());
	}

	/** A run admitted with nothing left is exhausted before it records anything. */
	public void testNoHeadroomIsExhaustedFromTheStart() {
		assertTrue(TokenAllowance.of(0).exhausted());
		assertTrue("A negative remainder is not unlimited.", TokenAllowance.of(-5).exhausted());
		assertTrue(TokenAllowance.of(-5).isLimited());
	}

	/** A missing usage is nothing, not a failure. */
	public void testNullUsageCountsAsNothing() {
		TokenAllowance allowance = TokenAllowance.of(10);
		allowance.record(null);
		assertEquals(0, allowance.spent());
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
		TokenAllowance allowance = TokenAllowance.of(100);
		ToolContext parent = ToolContext.unrestricted().withTokenAllowance(allowance);
		ToolContext child = parent.forDelegation(TransactionScope.NOOP);
		ToolContext grandchild = child.forDelegation(TransactionScope.NOOP);

		assertSame(allowance, child.tokenAllowance());
		assertSame(allowance, grandchild.tokenAllowance());

		child.tokenAllowance().record(new TokenUsage(60, 0, 0, 0, 0));
		grandchild.tokenAllowance().record(new TokenUsage(40, 0, 0, 0, 0));
		assertTrue("What the children spent is what the parent has left.",
			parent.tokenAllowance().exhausted());
	}

	/** A detached subagent shares it too: it is still part of the run that spawned it. */
	public void testADetachedSubagentSharesTheParentsAllowance() {
		TokenAllowance allowance = TokenAllowance.of(100);
		ToolContext parent = ToolContext.unrestricted().withTokenAllowance(allowance);
		ToolContext child = parent.forDetachedSubagent(TransactionScope.NOOP,
			new com.top_logic.tl.ai.tool.api.CancellationToken());
		assertSame(allowance, child.tokenAllowance());
	}

	/** Every other copy of a context carries it too. */
	public void testTheCopiesCarryIt() {
		TokenAllowance allowance = TokenAllowance.of(100);
		ToolContext context = ToolContext.unrestricted().withTokenAllowance(allowance);
		assertSame(allowance, context.withModel("some-model").tokenAllowance());
		assertSame(allowance, context.withToolCallId("call-1").tokenAllowance());
		assertSame(allowance,
			context.withProgressReporter(com.top_logic.tl.ai.tool.api.ProgressReporter.NOOP).tokenAllowance());
	}
}
