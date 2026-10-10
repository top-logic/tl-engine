/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.limits.BudgetConfig.RoleBudget;

/**
 * What an installation allows by default, and how a role budget changes it.
 *
 * <p>
 * The defaults are the contract of Gitea #343 that an upgrade must not break: <b>nothing is limited
 * until an administrator limits it</b>. This product is deployed into applications with a thousand
 * users, and every ceiling it could pick for them - how many runs at once, how many per account, how
 * many tokens - would refuse work that the installation allowed the day before the upgrade.
 * </p>
 */
public class TestRunLimitSettings extends TestCase {

	/** Reading the configuration asks {@code AgentRuntimeService} whether it runs, which needs the modules. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestRunLimitSettings.class));
	}

	/** Nothing is limited unless an administrator says so. */
	public void testTheShippedDefaults() {
		assertEquals("The installation carries as many chat runs as arrive, as it did before #343.",
			0, RunLimitSettings.chatWorkers());
		assertEquals("The per-account concurrency cap is off until it is configured.",
			0, RunLimitSettings.concurrentRunsPerAccount());
		assertEquals("The token budget is off until it is configured.",
			0, RunLimitSettings.tokenBudget(Set.of()));
		assertEquals("A window is only meaningful with a budget, and 24h is the shipped one.",
			24, RunLimitSettings.budgetWindowHours());
		assertEquals("Without a ceiling nothing waits, so the shipped queue has no fixed length.",
			0, RunLimitSettings.chatQueue());
	}

	/** A configured size is used as given. */
	public void testAConfiguredPoolSize() {
		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(pool(3, 0))) {
			assertEquals(3, RunLimitSettings.chatWorkers());
		}
		assertEquals("The override is undone when the handle closes.",
			0, RunLimitSettings.chatWorkers());
	}

	/** A ceiling queues by default: the user waits for an answer rather than being sent away. */
	public void testACeilingQueuesUnlessTheQueueIsGivenALength() {
		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(pool(4, 0))) {
			assertEquals(4, RunLimitSettings.chatWorkers());
			assertEquals("A queue of no fixed length is what a ceiling gets unless one is asked for.",
				0, RunLimitSettings.chatQueue());
		}
		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(pool(4, 10))) {
			assertEquals(10, RunLimitSettings.chatQueue());
		}
	}

	/**
	 * An installation that would rather refuse than let a user wait says so with a negative length.
	 */
	public void testARefusingInstallationSaysSo() {
		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(pool(4, -1))) {
			assertEquals(RunLimitSettings.NO_QUEUE, RunLimitSettings.chatQueue());
		}
		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(pool(4, -7))) {
			assertEquals("Any negative length reads as the one thing it can mean.",
				RunLimitSettings.NO_QUEUE, RunLimitSettings.chatQueue());
		}
	}

	/** Without a role budget, everyone gets the section's own budget. */
	public void testTheSectionsBudgetAppliesToEveryone() {
		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(budget(1000, List.of()))) {
			assertEquals(1000, RunLimitSettings.tokenBudget(Set.of()));
			assertEquals(1000, RunLimitSettings.tokenBudget(Set.of("tl.user", "reader")));
		}
	}

	/** A role the account holds grants its own figure instead. */
	public void testARoleBudgetReplacesTheDefault() {
		try (RunLimitSettings.Overridden overridden =
			RunLimitSettings.override(budget(1000, List.of(role("power", 5000))))) {
			assertEquals("An account without the role keeps the section's budget.",
				1000, RunLimitSettings.tokenBudget(Set.of("tl.user")));
			assertEquals("An account with the role gets the role's budget.",
				5000, RunLimitSettings.tokenBudget(Set.of("tl.user", "power")));
		}
	}

	/**
	 * Holding one more role never makes an account worse off.
	 *
	 * <p>
	 * The largest figure wins, and a role budget of zero - which reads as unlimited there exactly as
	 * it does in the section - beats every finite one. That is the rule that makes a role a grant of
	 * headroom rather than a second place a limit can come from.
	 * </p>
	 */
	public void testTheLargestRoleBudgetWins() {
		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(
			budget(1000, List.of(role("small", 2000), role("large", 9000), role("free", 0))))) {
			assertEquals(9000, RunLimitSettings.tokenBudget(Set.of("small", "large")));
			assertEquals("Unlimited beats a finite grant.",
				0, RunLimitSettings.tokenBudget(Set.of("small", "free")));
			assertEquals(2000, RunLimitSettings.tokenBudget(Set.of("small")));
		}
	}

	private static RunLimitsConfig pool(int workers, int queue) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.CHAT_WORKERS),
			Integer.valueOf(workers));
		config.update(config.descriptor().getProperty(RunLimitsConfig.CHAT_QUEUE),
			Integer.valueOf(queue));
		return config;
	}

	private static RunLimitsConfig budget(long tokens, List<RoleBudget> roles) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.TOKEN_BUDGET), Long.valueOf(tokens));
		for (RoleBudget role : roles) {
			config.getRoleBudgets().put(role.getName(), role);
		}
		return config;
	}

	private static RoleBudget role(String name, long tokens) {
		RoleBudget budget = TypedConfiguration.newConfigItem(RoleBudget.class);
		budget.update(budget.descriptor().getProperty(RoleBudget.NAME_ATTRIBUTE), name);
		budget.update(budget.descriptor().getProperty(RoleBudget.TOKENS), Long.valueOf(tokens));
		return budget;
	}
}
