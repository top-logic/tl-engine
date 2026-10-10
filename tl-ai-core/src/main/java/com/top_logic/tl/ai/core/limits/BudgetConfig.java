/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.Map;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * How many tokens one account may spend in a rolling window.
 *
 * <p>
 * The agent runs against metered provider APIs. With nothing configured here, no run is ever told to
 * stop because the account it acts for has spent enough. <b>The budget is off until it is
 * configured</b>, for the reason {@link CapacityConfig} gives for its ceilings.
 * </p>
 *
 * <p>
 * The budget counts the <em>principal's</em> runs. A delegated sub-agent is part of the run that
 * delegated it and spends its parent's budget rather than one of its own.
 * </p>
 *
 * @see RunLimitSettings
 * @see RunLimits
 */
@TagName("budget")
@DisplayOrder({
	BudgetConfig.TOKEN_BUDGET,
	BudgetConfig.BUDGET_WINDOW_HOURS,
	BudgetConfig.ROLE_BUDGETS,
})
public interface BudgetConfig extends ConfigPart {

	/** Configuration property name for {@link #getTokenBudget()}. */
	String TOKEN_BUDGET = "token-budget";

	/** Configuration property name for {@link #getBudgetWindowHours()}. */
	String BUDGET_WINDOW_HOURS = "budget-window-hours";

	/** Configuration property name for {@link #getRoleBudgets()}. */
	String ROLE_BUDGETS = "role-budgets";

	/**
	 * How many tokens one account may spend within {@link #getBudgetWindowHours() the window}.
	 *
	 * <p>
	 * {@code 0}, the default, means unlimited. A run of an account that has already spent this much
	 * is refused before the first request is sent, and a run that reaches the ceiling while it is
	 * going stops between turns with the stop reason {@code BUDGET_EXHAUSTED}, keeping whatever it
	 * had answered so far.
	 * </p>
	 *
	 * <p>
	 * The figure counted is everything the provider was asked to read and write - prompt including
	 * its cached part, output and thinking - summed over the account's recorded runs. It therefore
	 * needs run recording to be on; an installation that records nothing has nothing to count and
	 * this limit does nothing.
	 * </p>
	 *
	 * <p>
	 * <b>What it costs.</b> The figure is read from the recorded runs at every run start, which is
	 * one pass over the execution rows. That is why it is off unless an administrator asks for it.
	 * </p>
	 *
	 * @see #TOKEN_BUDGET
	 */
	@Name(TOKEN_BUDGET)
	@Label("Token budget per account")
	@LongDefault(0)
	long getTokenBudget();

	/**
	 * How far back {@link #getTokenBudget()} counts, in hours.
	 *
	 * <p>
	 * A rolling window, not a calendar day: a run started now is measured against everything the
	 * account spent since this many hours ago. 24 hours is the default.
	 * </p>
	 *
	 * @see #BUDGET_WINDOW_HOURS
	 */
	@Name(BUDGET_WINDOW_HOURS)
	@Label("Budget window (hours)")
	@IntDefault(24)
	int getBudgetWindowHours();

	/**
	 * Token budgets that differ from {@link #getTokenBudget()} for the holders of a role.
	 *
	 * <p>
	 * An account that holds one of these roles gets the <em>largest</em> budget among them, and an
	 * account that holds none gets {@link #getTokenBudget()}. A role therefore grants headroom and
	 * never takes it away, which is the only reading under which holding one more role cannot make an
	 * account worse off.
	 * </p>
	 *
	 * <p>
	 * Deliberately this small. It is a table of numbers, not a place to express when a budget
	 * applies; a rule that needs more than "who gets how much" belongs in the roles themselves.
	 * </p>
	 *
	 * @see #ROLE_BUDGETS
	 */
	@Name(ROLE_BUDGETS)
	@Label("Token budgets by role")
	@EntryTag("role-budget")
	@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
	Map<String, RoleBudget> getRoleBudgets();

	/**
	 * What the holders of one role may spend.
	 *
	 * @see BudgetConfig#getRoleBudgets()
	 */
	@TagName("role-budget")
	interface RoleBudget extends NamedConfigMandatory {

		/** Configuration property name for {@link #getTokens()}. */
		String TOKENS = "tokens";

		/**
		 * The name of the role, as it is held on the security root.
		 *
		 * @see NamedConfigMandatory#getName()
		 */
		@Override
		@Label("Role")
		String getName();

		/**
		 * How many tokens the holders of this role may spend in the window.
		 *
		 * <p>
		 * {@code 0} means unlimited, exactly as it does for
		 * {@link BudgetConfig#getTokenBudget()} - which is how a role is exempted from a budget
		 * the installation otherwise has.
		 * </p>
		 *
		 * @see #TOKENS
		 */
		@Name(TOKENS)
		@Label("Tokens")
		@LongDefault(0)
		long getTokens();
	}
}
