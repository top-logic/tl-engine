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
 * How many agent runs this installation carries at once, and how much they may spend.
 *
 * <p>
 * The agent runs inside a shared application server on behalf of many users, against metered
 * provider APIs. With nothing configured here, one account's long analysis is indistinguishable
 * from a hundred: every run gets a thread, and no run is ever told to stop because the account it
 * acts for has spent enough. This is where an administrator puts a ceiling on both.
 * </p>
 *
 * <p>
 * Three independent limits, in the order a run meets them:
 * </p>
 * <ol>
 * <li>{@link #getChatWorkers()} - how many chat runs the installation carries at once.</li>
 * <li>{@link #getConcurrentRunsPerAccount()} - how many runs one account may have going.</li>
 * <li>{@link #getTokenBudget()} - how many tokens one account may spend in a rolling window.</li>
 * </ol>
 *
 * <p>
 * <b>All three are off until they are configured.</b> This product runs in applications with a
 * thousand users, and a figure it picked for them would be wrong for most: an installation that
 * upgrades must find exactly what it had, and the administrator who knows the machine and the
 * provider contract is the one who names the numbers. Where a ceiling is set, a run that meets it
 * waits where waiting is the better answer - see {@link #getChatQueue()} - and is refused where it
 * is not.
 * </p>
 *
 * <p>
 * Both per-account limits count the <em>principal's</em> runs. A delegated sub-agent is part of the
 * run that delegated it, not a run of its own, and is therefore neither counted against the
 * concurrency cap nor given a budget of its own - it spends its parent's.
 * </p>
 *
 * @see RunLimitSettings
 * @see RunLimits
 */
@TagName("run-limits")
@DisplayOrder({
	RunLimitsConfig.CHAT_WORKERS,
	RunLimitsConfig.CHAT_QUEUE,
	RunLimitsConfig.CONCURRENT_RUNS_PER_ACCOUNT,
	RunLimitsConfig.TOKEN_BUDGET,
	RunLimitsConfig.BUDGET_WINDOW_HOURS,
	RunLimitsConfig.ROLE_BUDGETS,
})
public interface RunLimitsConfig extends ConfigPart {

	/** Configuration property name for {@link #getChatWorkers()}. */
	String CHAT_WORKERS = "chat-workers";

	/** Configuration property name for {@link #getChatQueue()}. */
	String CHAT_QUEUE = "chat-queue";

	/** Configuration property name for {@link #getConcurrentRunsPerAccount()}. */
	String CONCURRENT_RUNS_PER_ACCOUNT = "concurrent-runs-per-account";

	/** Configuration property name for {@link #getTokenBudget()}. */
	String TOKEN_BUDGET = "token-budget";

	/** Configuration property name for {@link #getBudgetWindowHours()}. */
	String BUDGET_WINDOW_HOURS = "budget-window-hours";

	/** Configuration property name for {@link #getRoleBudgets()}. */
	String ROLE_BUDGETS = "role-budgets";

	/**
	 * How many chat runs this installation carries at the same time.
	 *
	 * <p>
	 * {@code 0}, the default, means <b>no ceiling</b>: every send gets a worker of its own, which is
	 * what this installation did before this section existed. An application serving a thousand users
	 * is not served by a figure this product guessed, so the default is "as many as arrive" and the
	 * number is the administrator's to name.
	 * </p>
	 *
	 * <p>
	 * A positive number is the bound of the thread pool the chat's background runs execute on. A run
	 * thread spends nearly all of its life waiting on a provider, so the bound is not about CPU - it
	 * is about how much memory, how many open conversations and how much provider spend the
	 * installation is willing to have in flight at once. A send that finds every worker busy
	 * {@link #getChatQueue() waits for one} rather than being turned away.
	 * </p>
	 *
	 * <p>
	 * Read once, when the pool is first used. Changing it needs a restart.
	 * </p>
	 *
	 * @see #CHAT_WORKERS
	 */
	@Name(CHAT_WORKERS)
	@Label("Chat worker threads")
	@IntDefault(0)
	int getChatWorkers();

	/**
	 * How many chat runs may wait for a free worker, once {@link #getChatWorkers()} has set a ceiling.
	 *
	 * <p>
	 * Without that ceiling this says nothing: an installation that carries every run as it arrives
	 * never has anybody waiting.
	 * </p>
	 *
	 * <p>
	 * Three readings:
	 * </p>
	 * <ul>
	 * <li>{@code 0}, the default - <b>an unbounded queue</b>. A send that finds every worker busy
	 * waits for one and its answer arrives late. That is what somebody who asked a question wants;
	 * being told to ask again is not. What waits is one small task per send, so a backlog costs the
	 * server a fraction of what carrying those runs would.</li>
	 * <li>A positive number - a queue of that length. A send that arrives at a full queue is refused
	 * with a sentence it can act on.</li>
	 * <li>{@code -1} - <b>no queue at all</b>. Every send that finds the workers busy is refused at
	 * once. For an installation that would rather say "not now" than have a user wait for an answer
	 * whose moment may have passed.</li>
	 * </ul>
	 *
	 * @see #CHAT_QUEUE
	 */
	@Name(CHAT_QUEUE)
	@Label("Chat queue length")
	@IntDefault(0)
	int getChatQueue();

	/**
	 * How many runs one account may have going at the same time.
	 *
	 * <p>
	 * {@code 0}, the default, switches the cap off. A shared installation serving many users wants a
	 * small number here - 3 to 5 is a sensible range - so that one account cannot take every worker
	 * the {@link #getChatWorkers() pool} has, or run up the provider bill on its own.
	 * </p>
	 *
	 * <p>
	 * Counted over <em>every</em> entry point - the chat, the scheduler, TL-Script - because they all
	 * start their runs through the same place. A delegated sub-agent is part of its parent run and is
	 * not counted again.
	 * </p>
	 *
	 * @see #CONCURRENT_RUNS_PER_ACCOUNT
	 */
	@Name(CONCURRENT_RUNS_PER_ACCOUNT)
	@Label("Concurrent runs per account")
	@IntDefault(0)
	int getConcurrentRunsPerAccount();

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
	 * @see RunLimitsConfig#getRoleBudgets()
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
		 * {@link RunLimitsConfig#getTokenBudget()} - which is how a role is exempted from a budget
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
