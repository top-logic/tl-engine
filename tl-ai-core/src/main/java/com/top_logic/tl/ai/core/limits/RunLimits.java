/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.environment.RunEnvironment;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.AccountSpend;
import com.top_logic.tl.ai.core.limits.BudgetAllowance.MoneyBudget;
import com.top_logic.tl.ai.core.security.AiToolAccessService;

/**
 * The ceiling on what one account may have running, and on what it may spend.
 *
 * <p>
 * Asked where every run enters - {@code AgentFactory.runAgent}, which the chat, the scheduler,
 * {@code AgentRunner} and the TL-Script functions all arrive at. A run is
 * {@link #admit(ToolContext) admitted} or it does not happen; an admitted run holds its slot until
 * it {@link Admission#close() releases} it.
 * </p>
 *
 * <p>
 * <b>What is not counted.</b> A run that cannot name a principal (a test context, an unattended
 * boot step) is admitted unconditionally: there is nobody to attribute it to, and refusing it would
 * take the framework's own runs down with the users'. A <em>delegated</em> run is admitted
 * unconditionally too, for the opposite reason - it is part of the run that delegated it, which was
 * already counted, and counting it again would let one user's ceiling depend on how an agent happens
 * to be built. It does share the parent's {@link BudgetAllowance}, so delegating is not a way around
 * the budget.
 * </p>
 *
 * @see CapacityConfig
 * @see BudgetConfig
 */
public class RunLimits {

	/**
	 * How many runs each principal has going, keyed by the account's object name.
	 *
	 * <p>
	 * In memory, per node: it counts what <em>this</em> server is carrying, which is the same thing
	 * the thread pool it shares is bounded by. A cluster-wide count would need a store, and the
	 * ticket's point - that one account cannot take a node down - is answered per node.
	 * </p>
	 *
	 * <p>
	 * An entry is never removed. Removing one when its count falls to zero would race with the next
	 * run of the same account taking it, and the map holds one small entry per account that has ever
	 * run an agent on this node - which is bounded by the number of accounts.
	 * </p>
	 */
	private static final Map<String, AtomicInteger> RUNNING = new ConcurrentHashMap<>();

	/** The reading the run start on this thread takes over, see {@link #reuse(SpendReading)}. */
	private static final ThreadLocal<SpendReading> REUSED = new ThreadLocal<>();

	private RunLimits() {
		// Static utility.
	}

	/**
	 * What one account had spent, read once and asked by every budget check of one send.
	 *
	 * <p>
	 * Reading the spend is a pass over the account's recorded runs. One send asks the budget up to
	 * three times - the pre-check of a regenerate, the pre-check of the send and the start of its
	 * run - and all of them are about the same moment, so they share one reading rather than each
	 * reading again (#835). The spend is read when a check first needs it, so a reading for an
	 * account without a budget reads nothing.
	 * </p>
	 *
	 * <p>
	 * The pre-checks are given the reading; the start of the run, on another thread, takes it over
	 * through {@link #reuse(SpendReading)}.
	 * </p>
	 */
	public static final class SpendReading {

		private final Person _principal;

		private AccountSpend _spent;

		private int _hours;

		private SpendReading(Person principal) {
			_principal = principal;
		}

		/** What the account spent in the given window, read on the first call for it. */
		synchronized AccountSpend spent(int hours) {
			if (_spent == null || _hours != hours) {
				_spent = spentSince(_principal, hours);
				_hours = hours;
			}
			return _spent;
		}

		boolean isFor(Person principal) {
			return _principal.equals(principal);
		}
	}

	/**
	 * A reading of what the given account spent, not read yet.
	 *
	 * @param principal
	 *        The acting account, or {@code null} if the request has none.
	 * @return The reading; {@code null} for {@code null}.
	 */
	public static SpendReading spendReading(Person principal) {
		return principal == null ? null : new SpendReading(principal);
	}

	/**
	 * Lets the next {@link #admit(ToolContext) run start} on this thread use the given reading
	 * instead of reading the spend again.
	 *
	 * <p>
	 * Used once, and only by a run of the account the reading was taken for; a run of another account
	 * reads its own. Closing the binding drops a reading nobody used.
	 * </p>
	 *
	 * @param reading
	 *        The reading the send's pre-check took; {@code null} binds nothing.
	 * @return The binding; close it when the run ended.
	 */
	public static Reuse reuse(SpendReading reading) {
		SpendReading before = REUSED.get();
		REUSED.set(reading);
		return () -> {
			if (before == null) {
				REUSED.remove();
			} else {
				REUSED.set(before);
			}
		};
	}

	/** The binding of {@link #reuse(SpendReading)}; closing it throws nothing. */
	public interface Reuse extends AutoCloseable {
		@Override
		void close();
	}

	/**
	 * A run that was let through: the slot it holds, and what is left of its budget.
	 *
	 * <p>
	 * Closing it is what frees the slot, so it belongs in a try-with-resources around the run.
	 * Closing twice does nothing.
	 * </p>
	 */
	public static final class Admission implements AutoCloseable {

		/** An admission that holds nothing, for a run that is not counted and not budgeted. */
		public static final Admission NONE = new Admission(BudgetAllowance.UNLIMITED, null);

		private final BudgetAllowance _allowance;

		private String _key;

		private Admission(BudgetAllowance allowance, String key) {
			_allowance = allowance;
			_key = key;
		}

		/**
		 * What is left of the principal's budgets, to be carried by the run's context.
		 *
		 * @see BudgetAllowance
		 */
		public BudgetAllowance allowance() {
			return _allowance;
		}

		@Override
		public synchronized void close() {
			if (_key != null) {
				release(_key);
				_key = null;
			}
		}
	}

	/**
	 * Lets the given run start, or refuses it.
	 *
	 * @param context
	 *        The run's execution context; its {@link ToolContext#security() security context} names
	 *        the principal and its {@link ToolContext#delegationDepth()} says whether this is a run
	 *        of its own.
	 * @return The slot the run holds and the budget it may spend; never {@code null}. Close it when
	 *         the run ends.
	 * @throws RunLimitExceededException
	 *         If the account already has as many runs going as it may, or has spent its budget.
	 */
	public static Admission admit(ToolContext context) {
		if (context == null || context.delegationDepth() > 0) {
			return Admission.NONE;
		}
		Person principal = principalOf(context.security());
		if (principal == null) {
			return Admission.NONE;
		}
		String key = keyOf(principal);
		acquireSlot(principal, key);
		try {
			return new Admission(allowanceFor(principal, heldRoles(context.security()), takeReused(principal)), key);
		} catch (RuntimeException ex) {
			release(key);
			throw ex;
		}
	}

	/** The reading bound for the given account's run on this thread, used up; else a new one. */
	private static SpendReading takeReused(Person principal) {
		SpendReading reading = REUSED.get();
		if (reading != null && reading.isFor(principal)) {
			REUSED.set(null);
			return reading;
		}
		return new SpendReading(principal);
	}

	/**
	 * Why this account may not start another run right now, without taking a slot.
	 *
	 * <p>
	 * The friendly half of {@link #admit(ToolContext)}, for an entry point that can answer the user
	 * before it has done anything - the chat's {@code /chat/send}. It is a courtesy and not the
	 * gate: between this answer and the run, another request of the same account can take the last
	 * slot, and that run is then refused where it starts.
	 * </p>
	 *
	 * <p>
	 * The budgets are not asked here, because reading them costs a pass over the recorded runs; see
	 * {@link #budgetRefusal(Person)}, which an entry point asks separately.
	 * </p>
	 *
	 * @param principal
	 *        The acting account, or {@code null} if the request has none.
	 * @return The refusal in the user's language, or {@code null} if nothing refuses.
	 */
	public static ResKey concurrencyRefusal(Person principal) {
		int cap = RunLimitSettings.concurrentRunsPerAccount();
		if (cap <= 0 || principal == null) {
			return null;
		}
		int running = runningFor(principal);
		if (running < cap) {
			return null;
		}
		Logger.info("Agent run refused for '" + principal.getName() + "': " + running
			+ " runs already in progress, the cap is " + cap + ".", RunLimits.class);
		return I18NConstants.TOO_MANY_RUNS_FOR_ACCOUNT__COUNT.fill(Integer.valueOf(cap));
	}

	/**
	 * Why this account may not start another run because it has spent a budget, without taking a
	 * slot.
	 *
	 * <p>
	 * The friendly half of the budget check in {@link #admit(ToolContext)}, for an entry point that
	 * can answer the user before it has done anything - the chat's {@code /chat/send}. A run refused
	 * where it starts has no answer under which the chat can show the refusal, so the sentence is
	 * given here, before the user's message is written down. It is a courtesy and not the gate: a run
	 * that finishes in between can spend the rest, and the next run is then refused where it starts.
	 * </p>
	 *
	 * <p>
	 * <b>What it costs.</b> Nothing while no budget is configured. With one, a pass over the account's
	 * recorded runs; an entry point that asks more than once for one send passes one
	 * {@link #spendReading(Person) reading} to {@link #budgetRefusal(Person, SpendReading)} and
	 * hands it to the run with {@link #reuse(SpendReading)}.
	 * </p>
	 *
	 * @param principal
	 *        The acting account, or {@code null} if the request has none.
	 * @return The refusal in the user's language, or {@code null} if nothing refuses.
	 */
	public static ResKey budgetRefusal(Person principal) {
		return budgetRefusal(principal, spendReading(principal));
	}

	/**
	 * {@link #budgetRefusal(Person)} on a reading the send shares with its other checks.
	 *
	 * @param principal
	 *        The acting account, or {@code null} if the request has none.
	 * @param reading
	 *        The send's reading of the account's spend, see {@link #spendReading(Person)}.
	 * @return The refusal in the user's language, or {@code null} if nothing refuses.
	 */
	public static ResKey budgetRefusal(Person principal, SpendReading reading) {
		if (principal == null || !RunLimitSettings.anyBudget()) {
			return null;
		}
		SpendReading forPrincipal = reading != null && reading.isFor(principal) ? reading : spendReading(principal);
		try {
			allowanceFor(principal, AiToolAccessService.heldRoleNames(principal), forPrincipal);
			return null;
		} catch (RunLimitExceededException ex) {
			return ex.reason();
		}
	}

	/**
	 * {@link #budgetRefusal(Person)} for an account whose roles the caller already knows.
	 *
	 * @param principal
	 *        The acting account, or {@code null} if the request has none.
	 * @param heldRoles
	 *        The roles the account holds on the security root.
	 * @return The refusal in the user's language, or {@code null} if nothing refuses.
	 */
	public static ResKey budgetRefusal(Person principal, Collection<String> heldRoles) {
		if (principal == null) {
			return null;
		}
		try {
			allowanceFor(principal, heldRoles, spendReading(principal));
			return null;
		} catch (RunLimitExceededException ex) {
			return ex.reason();
		}
	}

	/** How many runs the given account currently has going on this node. */
	public static int runningFor(Person principal) {
		if (principal == null) {
			return 0;
		}
		AtomicInteger counter = RUNNING.get(keyOf(principal));
		return counter == null ? 0 : counter.get();
	}

	/**
	 * Forgets every counted run.
	 *
	 * <p>
	 * <b>A test seam.</b> Nothing in production calls it: a slot is freed by closing the
	 * {@link Admission} that holds it.
	 * </p>
	 */
	public static void resetForTest() {
		RUNNING.clear();
	}

	private static void acquireSlot(Person principal, String key) {
		int cap = RunLimitSettings.concurrentRunsPerAccount();
		AtomicInteger counter = RUNNING.computeIfAbsent(key, ignored -> new AtomicInteger());
		int running = counter.incrementAndGet();
		// Rolled back below when it is one too many, so the count never stays above the cap.
		if (cap > 0 && running > cap) {
			release(key);
			String message = "Permission to run denied: account '" + principal.getName() + "' already has "
				+ cap + " agent runs in progress, which is this installation's limit.";
			Logger.info(message, RunLimits.class);
			throw new RunLimitExceededException(
				I18NConstants.TOO_MANY_RUNS_FOR_ACCOUNT__COUNT.fill(Integer.valueOf(cap)), message, true);
		}
	}

	private static void release(String key) {
		AtomicInteger counter = RUNNING.get(key);
		if (counter != null) {
			counter.decrementAndGet();
		}
	}

	/**
	 * What is left of this account's budgets, as the allowance the run's loop will check.
	 *
	 * @throws RunLimitExceededException
	 *         If nothing is left of either.
	 */
	private static BudgetAllowance allowanceFor(Person principal, Collection<String> heldRoles,
			SpendReading reading) {
		long tokenBudget = RunLimitSettings.tokenBudget(heldRoles);
		double moneyBudget = RunLimitSettings.moneyBudget(heldRoles);
		if (tokenBudget <= 0 && moneyBudget <= 0) {
			return BudgetAllowance.UNLIMITED;
		}
		int hours = RunLimitSettings.budgetWindowHours();
		AccountSpend spent = reading.spent(hours);

		long tokenHeadroom = BudgetAllowance.UNLIMITED_TOKENS;
		if (tokenBudget > 0) {
			tokenHeadroom = tokenBudget - spent.tokens();
			if (tokenHeadroom <= 0) {
				String message = "Permission to run denied: account '" + principal.getName() + "' spent "
					+ spent.tokens() + " tokens in the last " + hours + "h, which is its budget of " + tokenBudget
					+ ".";
				Logger.info(message, RunLimits.class);
				throw new RunLimitExceededException(
					I18NConstants.TOKEN_BUDGET_SPENT__HOURS.fill(Integer.valueOf(hours)), message, false);
			}
		}

		MoneyBudget money = null;
		if (moneyBudget > 0) {
			money = moneyBudget(principal, moneyBudget, spent);
		}
		if (money != null) {
			if (money.headroom() <= 0) {
				String message = "Permission to run denied: account '" + principal.getName() + "' spent "
					+ spent.cost() + " " + money.currency() + " in the last " + hours
					+ "h, which is its budget of " + moneyBudget + " " + money.currency() + ".";
				Logger.info(message, RunLimits.class);
				throw new RunLimitExceededException(moneySpent(principal, money, spent.cost(), hours,
					I18NConstants.MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS), message, false);
			}
		}
		return BudgetAllowance.of(tokenHeadroom, money);
	}

	/**
	 * The account's money budget against what it spent, in the system currency; {@code null} when
	 * it cannot be checked.
	 *
	 * <p>
	 * The money budget is stated in the system currency and the runs record US dollars, so it is
	 * checked only while a rate of today converts the run that starts, and the rates of their days
	 * converted every run of the window. Without them nobody is refused for a sum nobody could
	 * convert: the budget is not checked, and {@link CostConversion#warnNoRate(String)} tells the
	 * log. The token budget is checked either way.
	 * </p>
	 */
	private static MoneyBudget moneyBudget(Person principal, double budget, AccountSpend spent) {
		OptionalDouble rate = CostConversion.usdRate(null);
		if (rate.isEmpty() || !spent.complete()) {
			CostConversion.warnNoRate("the money budget is not checked (account '" + principal.getName() + "')");
			return null;
		}
		return new MoneyBudget(budget, spent.cost(), CostConversion.systemCurrency(), rate.getAsDouble());
	}

	/**
	 * What to tell the user whose run stopped because a budget ran out while it was working.
	 *
	 * <p>
	 * Names the amounts when it was the money budget: money is a figure the user can relate to their
	 * own work, unlike a token count. The figures are read again from the recorded runs, so the run
	 * that just stopped is part of them - which is why this is asked once, after the run ended,
	 * rather than carried out of the loop.
	 * </p>
	 *
	 * @param security
	 *        The security context of the run that stopped; names the account and its roles.
	 * @return The notice in the user's language; never {@code null}.
	 */
	public static ResKey budgetStopNotice(SecurityContext security) {
		int hours = RunLimitSettings.budgetWindowHours();
		ResKey tokenNotice = I18NConstants.RUN_STOPPED_BUDGET_SPENT__HOURS.fill(Integer.valueOf(hours));
		Person principal = principalOf(security);
		if (principal == null) {
			return tokenNotice;
		}
		double moneyBudget = RunLimitSettings.moneyBudget(heldRoles(security));
		if (moneyBudget <= 0) {
			return tokenNotice;
		}
		AccountSpend spent = spentSince(principal, hours);
		MoneyBudget money = moneyBudget(principal, moneyBudget, spent);
		if (money == null || spent.cost() < moneyBudget) {
			return tokenNotice;
		}
		return moneySpent(principal, money, spent.cost(), hours,
			I18NConstants.RUN_STOPPED_MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS);
	}

	/**
	 * Fills a refusal that names the spent amount and the budget, written the way the account's own
	 * language writes amounts of money.
	 */
	private static ResKey moneySpent(Person principal, MoneyBudget money, double spent, int hours,
			ResKey3 template) {
		Locale locale = RunEnvironment.localeOf(principal);
		return template.fill(
			CostConversion.format(new Money(spent, money.currency()), locale),
			CostConversion.format(new Money(money.budget(), money.currency()), locale),
			Integer.valueOf(hours));
	}

	/**
	 * What the account spent in the window, read from the recorded runs.
	 *
	 * <p>
	 * Nothing when nothing records runs: an installation without an {@link AgentExecutionStore} has
	 * no figure to enforce against, and inventing one would refuse runs for a spend nobody measured.
	 * </p>
	 */
	private static AccountSpend spentSince(Person principal, int hours) {
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			return new AccountSpend(0, 0, true);
		}
		Date since = new Date(System.currentTimeMillis() - Duration.ofHours(hours).toMillis());
		try {
			return AgentExecutionStore.getInstance().spentSince(principal, since);
		} catch (RuntimeException ex) {
			// A budget that cannot be read must not stop the installation from working. Admitting the
			// run is the direction that keeps the product usable; the failure is reported so that an
			// administrator sees a budget which is not being enforced.
			Logger.error("Could not read the agent budget of '" + principal.getName()
				+ "'; the run is admitted without it.", ex, RunLimits.class);
			return new AccountSpend(0, 0, true);
		}
	}

	/** The acting account of the given security context, or {@code null} if it names none. */
	private static Person principalOf(SecurityContext security) {
		return security == null ? null : security.runAuthorization().principal();
	}

	/** The roles the acting account holds, never {@code null}. */
	private static Set<String> heldRoles(SecurityContext security) {
		return security == null ? Set.of() : security.runAuthorization().heldRoleNames();
	}

	/** The account's stable identity, as the key the counter uses. */
	private static String keyOf(Person principal) {
		return KBUtils.getWrappedObjectName(principal).toString();
	}
}
