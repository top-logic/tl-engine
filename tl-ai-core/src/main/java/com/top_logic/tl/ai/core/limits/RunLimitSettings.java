/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.Collection;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.limits.BudgetConfig.RoleBudget;

/**
 * Reads the run limits: the {@code <capacity>} of {@link AgentRuntimeService} and the
 * {@code <budget>} of {@link AgentPolicyService}.
 *
 * <p>
 * Every accessor answers without the service, the way
 * {@link com.top_logic.tl.ai.core.agent.environment.UserMemorySettings} does: a unit test, a
 * migration or a boot step that runs before the service is up gets the shipped default rather than
 * an exception.
 * </p>
 *
 * @see CapacityConfig
 * @see BudgetConfig
 */
public class RunLimitSettings {

	/**
	 * What {@link #chatQueue()} answers for an installation that wants no waiting room at all.
	 *
	 * @see CapacityConfig#getChatQueue()
	 */
	public static final int NO_QUEUE = -1;

	/**
	 * The shipped defaults, as an instance of the configuration interface.
	 *
	 * <p>
	 * Built once from the interface's own {@code @...Default} annotations, so the answer without a
	 * running service is exactly the answer with an unconfigured one - there is no second copy of
	 * the defaults in this class to drift away from the first.
	 * </p>
	 */
	private static final RunLimitsConfig DEFAULTS = TypedConfiguration.newConfigItem(RunLimitsConfig.class);

	private RunLimitSettings() {
		// Static utility.
	}

	/**
	 * The section a test put in place of the configured one, or {@code null}.
	 *
	 * @see #override(RunLimitsConfig)
	 */
	private static volatile RunLimitsConfig _override;

	/** The configured capacity, or the shipped defaults when the service is not running. */
	private static CapacityConfig capacity() {
		RunLimitsConfig overridden = _override;
		if (overridden != null) {
			return overridden;
		}
		if (!AgentRuntimeService.Module.INSTANCE.isActive()) {
			return DEFAULTS;
		}
		CapacityConfig configured = AgentRuntimeService.getInstance().getCapacity();
		return configured == null ? DEFAULTS : configured;
	}

	/** The configured budget, or the shipped defaults when the service is not running. */
	private static BudgetConfig budget() {
		RunLimitsConfig overridden = _override;
		if (overridden != null) {
			return overridden;
		}
		if (!AgentPolicyService.Module.INSTANCE.isActive()) {
			return DEFAULTS;
		}
		BudgetConfig configured = AgentPolicyService.getInstance().getBudget();
		return configured == null ? DEFAULTS : configured;
	}

	/**
	 * Answers every question below from the given section until the handle is closed.
	 *
	 * <p>
	 * <b>A test seam.</b> What an installation allows is a property of the configuration of
	 * {@link AgentRuntimeService} and {@link AgentPolicyService}, and a unit test that wants to
	 * exercise a limit would otherwise have to boot them. Nothing in production calls this - the
	 * administrator's switches are the {@code <capacity>} and {@code <budget>} sections, and there is
	 * no second way to set them.
	 * </p>
	 *
	 * @param config
	 *        What to answer from; {@code null} restores the configured section.
	 */
	public static Overridden override(RunLimitsConfig config) {
		RunLimitsConfig before = _override;
		_override = config;
		return () -> _override = before;
	}

	/**
	 * A fresh, unconfigured section, to be filled by a test before it is handed to
	 * {@link #override(RunLimitsConfig)}.
	 */
	public static RunLimitsConfig newConfig() {
		return TypedConfiguration.newConfigItem(RunLimitsConfig.class);
	}

	/**
	 * Handle returned by {@link RunLimitSettings#override(RunLimitsConfig)}; closing it restores
	 * what was in place before.
	 */
	@FunctionalInterface
	public interface Overridden extends AutoCloseable {

		@java.lang.Override
		void close();
	}

	/**
	 * How many chat runs this installation carries at the same time; {@code 0}, the default, for as
	 * many as arrive.
	 *
	 * @see CapacityConfig#getChatWorkers()
	 */
	public static int chatWorkers() {
		return Math.max(0, capacity().getChatWorkers());
	}

	/**
	 * How many chat runs may wait for a free worker: {@code 0}, the default, for a queue of no fixed
	 * length, a positive number for a queue of that length, {@link #NO_QUEUE} for no queue at all.
	 *
	 * <p>
	 * Only of consequence when {@link #chatWorkers()} names a ceiling; without one nothing ever
	 * waits.
	 * </p>
	 *
	 * @see CapacityConfig#getChatQueue()
	 */
	public static int chatQueue() {
		int configured = capacity().getChatQueue();
		return configured < 0 ? NO_QUEUE : configured;
	}

	/**
	 * How many runs one account may have going at the same time; {@code 0} for no cap.
	 *
	 * @see CapacityConfig#getConcurrentRunsPerAccount()
	 */
	public static int concurrentRunsPerAccount() {
		return Math.max(0, capacity().getConcurrentRunsPerAccount());
	}

	/**
	 * How far back the token budget counts, in hours.
	 *
	 * @see BudgetConfig#getBudgetWindowHours()
	 */
	public static int budgetWindowHours() {
		int configured = budget().getBudgetWindowHours();
		return configured > 0 ? configured : 24;
	}

	/**
	 * How many tokens an account holding the given roles may spend in the window; {@code 0} for
	 * unlimited.
	 *
	 * <p>
	 * The largest of the {@link BudgetConfig#getRoleBudgets() role budgets} the account's roles
	 * name, and the section's own {@link BudgetConfig#getTokenBudget() budget} when they name
	 * none. A role grants headroom and never takes it away, so a {@code 0} on a role - which reads
	 * as "unlimited" there too - lifts the limit for its holders.
	 * </p>
	 *
	 * @param heldRoleNames
	 *        The roles the account holds on the security root; may be {@code null} or empty.
	 */
	public static long tokenBudget(Collection<String> heldRoleNames) {
		BudgetConfig config = budget();
		long fallback = Math.max(0, config.getTokenBudget());
		if (heldRoleNames == null || heldRoleNames.isEmpty() || config.getRoleBudgets().isEmpty()) {
			return fallback;
		}
		boolean matched = false;
		long granted = 0;
		for (String role : heldRoleNames) {
			RoleBudget budget = config.getRoleBudgets().get(role);
			if (budget == null) {
				continue;
			}
			matched = true;
			long tokens = Math.max(0, budget.getTokens());
			if (tokens == 0) {
				// Unlimited beats every finite grant, and beats the section's own budget.
				return 0;
			}
			granted = Math.max(granted, tokens);
		}
		return matched ? granted : fallback;
	}
}
