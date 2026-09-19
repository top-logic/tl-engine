/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;

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
 * to be built. It does share the parent's {@link TokenAllowance}, so delegating is not a way around
 * the budget.
 * </p>
 *
 * @see RunLimitsConfig
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

	private RunLimits() {
		// Static utility.
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
		public static final Admission NONE = new Admission(TokenAllowance.UNLIMITED, null);

		private final TokenAllowance _allowance;

		private String _key;

		private Admission(TokenAllowance allowance, String key) {
			_allowance = allowance;
			_key = key;
		}

		/**
		 * What is left of the principal's token budget, to be carried by the run's context.
		 *
		 * @see TokenAllowance
		 */
		public TokenAllowance allowance() {
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
			return new Admission(allowanceFor(principal, heldRoles(context.security())), key);
		} catch (RuntimeException ex) {
			release(key);
			throw ex;
		}
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
	 * The token budget is deliberately <em>not</em> asked here. Reading it costs a pass over the
	 * recorded runs, and the answer is wanted once per run rather than once per request; a run whose
	 * budget is spent is refused at its start and the chat shows the same sentence there.
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
	 * What is left of this account's token budget, as the allowance the run's loop will check.
	 *
	 * @throws RunLimitExceededException
	 *         If nothing is left.
	 */
	private static TokenAllowance allowanceFor(Person principal, Collection<String> heldRoles) {
		long budget = RunLimitSettings.tokenBudget(heldRoles);
		if (budget <= 0) {
			return TokenAllowance.UNLIMITED;
		}
		int hours = RunLimitSettings.budgetWindowHours();
		long spent = spentSince(principal, hours);
		long headroom = budget - spent;
		if (headroom <= 0) {
			String message = "Permission to run denied: account '" + principal.getName() + "' spent "
				+ spent + " tokens in the last " + hours + "h, which is its budget of " + budget + ".";
			Logger.info(message, RunLimits.class);
			throw new RunLimitExceededException(
				I18NConstants.TOKEN_BUDGET_SPENT__HOURS.fill(Integer.valueOf(hours)), message, false);
		}
		return TokenAllowance.of(headroom);
	}

	/**
	 * What the account spent in the window, read from the recorded runs.
	 *
	 * <p>
	 * Zero when nothing records runs: an installation without an {@link AgentExecutionStore} has no
	 * figure to enforce against, and inventing one would refuse runs for a spend nobody measured.
	 * </p>
	 */
	private static long spentSince(Person principal, int hours) {
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			return 0;
		}
		Date since = new Date(System.currentTimeMillis() - Duration.ofHours(hours).toMillis());
		try {
			return AgentExecutionStore.getInstance().tokensSpentSince(principal, since);
		} catch (RuntimeException ex) {
			// A budget that cannot be read must not stop the installation from working. Admitting the
			// run is the direction that keeps the product usable; the failure is reported so that an
			// administrator sees a budget which is not being enforced.
			Logger.error("Could not read the agent token budget of '" + principal.getName()
				+ "'; the run is admitted without it.", ex, RunLimits.class);
			return 0;
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
