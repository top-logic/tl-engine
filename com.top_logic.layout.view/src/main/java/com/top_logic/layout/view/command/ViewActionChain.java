/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;

/**
 * Runs a chain of {@link ViewAction}s, each action's output becoming the next action's input.
 *
 * <p>
 * The chain is driven by {@link Continuation}: a synchronous action resumes inline (the chain
 * completes within the call), while an interruptible action may resume later (e.g. after a dialog)
 * or abort (skipping the remaining actions and running, newest first, the compensations registered
 * via {@link Continuation#onAbort(Runnable)}). A thrown {@link RuntimeException} runs the same
 * compensation unwind before propagating.
 * </p>
 *
 * <p>
 * An action that is itself composed of actions - a branch such as {@link IfAction} or
 * {@link SwitchAction} - runs the actions it contains as a nested chain of the chain it is part of,
 * see {@link #nest(ReactContext, List, Object, Continuation)}.
 * </p>
 */
public class ViewActionChain {

	/**
	 * Runs {@code actions} starting with {@code input}.
	 *
	 * @param onComplete
	 *        Invoked exactly once when the chain settles normally - with the result of the last
	 *        action when the chain ran past it, or with {@code null} when it was aborted. It is
	 *        <em>not</em> invoked when an action throws (the exception propagates after
	 *        compensations run). May be {@code null}.
	 */
	public static void run(ReactContext context, List<ViewAction> actions, Object input,
			Consumer<Object> onComplete) {
		run(context, actions, input, onComplete, null);
	}

	/**
	 * Runs {@code actions} starting with {@code input}, telling the caller when the chain has come
	 * to an end whichever way it ended.
	 *
	 * @param onComplete
	 *        Invoked when the chain settles normally, see
	 *        {@link #run(ReactContext, List, Object, Consumer)}.
	 * @param onSettled
	 *        Invoked exactly once as soon as the chain cannot continue any more: it completed, it
	 *        was aborted, or an action threw. Invoked before the call returns for a chain that
	 *        settles within it, and from whatever interaction hands the chain back otherwise - a
	 *        caller that has to tell the two apart marks the call and inspects the mark after it
	 *        returned. May be {@code null}.
	 */
	public static void run(ReactContext context, List<ViewAction> actions, Object input,
			Consumer<Object> onComplete, Runnable onSettled) {
		Settlement settlement = new Settlement(onSettled);
		Deque<Runnable> compensations = new ArrayDeque<>();
		runFrom(context, actions, 0, input, compensations, settlement,
			value -> {
				settlement.settle();
				settled(onComplete, value);
			},
			() -> {
				runCompensations(compensations);
				settlement.settle();
				settled(onComplete, null);
			});
	}

	/**
	 * Runs {@code actions} as a nested chain of the action that is executing right now, handing the
	 * result of the nested chain's last action to the enclosing chain.
	 *
	 * <p>
	 * The nested chain starts with {@code input} and the enclosing chain continues with the result
	 * of its last action - with {@code input} itself when there is no action to run, so an empty
	 * branch passes the chain's value through. An abort inside the nested chain aborts the
	 * enclosing chain, and the compensations registered inside the nested chain take their place in
	 * the enclosing chain's unwind: whenever the enclosing chain is aborted or fails, they run -
	 * newest first - before the compensations of the actions that precede the nesting one.
	 * </p>
	 *
	 * @param continuation
	 *        The continuation of the action that contains the nested chain.
	 */
	public static void nest(ReactContext context, List<ViewAction> actions, Object input,
			Continuation continuation) {
		Deque<Runnable> compensations = new ArrayDeque<>();
		continuation.onAbort(() -> runCompensations(compensations));
		runFrom(context, actions, 0, input, compensations, settlementOf(continuation),
			continuation::resume,
			() -> {
				runCompensations(compensations);
				continuation.abort();
			});
	}

	/**
	 * The settlement of the chain the given continuation belongs to, so that a chain nested in it
	 * reports a failure to the caller that started the outermost chain.
	 */
	private static Settlement settlementOf(Continuation continuation) {
		if (continuation instanceof ChainContinuation chained) {
			return chained.settlement();
		}
		return new Settlement(null);
	}

	private static void runFrom(ReactContext context, List<ViewAction> actions, int index, Object input,
			Deque<Runnable> compensations, Settlement settlement, Consumer<Object> onComplete, Runnable onAbort) {
		if (index >= actions.size()) {
			onComplete.accept(input);
			return;
		}
		ViewAction action = actions.get(index);
		ChainContinuation continuation = new ChainContinuation(settlement, compensations,
			value -> runFrom(context, actions, index + 1, value, compensations, settlement, onComplete, onAbort),
			onAbort);
		try {
			action.execute(context, input, continuation);
		} catch (RuntimeException failure) {
			runCompensations(compensations);
			settlement.settle();
			throw failure;
		}
	}

	private static void settled(Consumer<Object> onComplete, Object value) {
		if (onComplete != null) {
			onComplete.accept(value);
		}
	}

	/**
	 * Runs (and removes) all registered compensations, newest first. Draining makes this safe to
	 * invoke from nested {@code catch} blocks during exception unwinding.
	 */
	private static void runCompensations(Deque<Runnable> compensations) {
		while (!compensations.isEmpty()) {
			compensations.pop().run();
		}
	}

	/**
	 * The {@link Continuation} of an action running in a chain.
	 *
	 * <p>
	 * Carries the {@link Settlement} of the chain it drives, so that a chain nested into it reaches
	 * the same settlement and the outermost caller hears of an end reached deep inside.
	 * </p>
	 */
	private static final class ChainContinuation implements Continuation {

		private final Settlement _settlement;

		private final Deque<Runnable> _compensations;

		private final Consumer<Object> _onResume;

		private final Runnable _onAbort;

		private boolean _spent;

		ChainContinuation(Settlement settlement, Deque<Runnable> compensations, Consumer<Object> onResume,
				Runnable onAbort) {
			_settlement = settlement;
			_compensations = compensations;
			_onResume = onResume;
			_onAbort = onAbort;
		}

		/**
		 * The settlement of the chain this continuation drives.
		 */
		Settlement settlement() {
			return _settlement;
		}

		@Override
		public void resume(Object value) {
			spend();
			_onResume.accept(value);
		}

		@Override
		public void abort() {
			spend();
			_onAbort.run();
		}

		@Override
		public void onAbort(Runnable compensation) {
			if (_spent) {
				throw new IllegalStateException("onAbort() after the action already continued.");
			}
			_compensations.push(compensation);
		}

		private void spend() {
			if (_spent) {
				throw new IllegalStateException("Continuation already used (resume/abort called twice).");
			}
			_spent = true;
		}
	}

	/**
	 * Tells the caller of a chain, once, that the chain has come to an end.
	 *
	 * <p>
	 * Every outcome passes through here - completion, abort and the failure unwind, which runs at
	 * each level of the chain it propagates through - so the notification is guarded to happen only
	 * the first time. The guard holds across interactions, since the end may be reached by a thread
	 * other than the one that started the chain.
	 * </p>
	 */
	private static final class Settlement {

		private final Runnable _onSettled;

		private boolean _settled;

		Settlement(Runnable onSettled) {
			_onSettled = onSettled;
		}

		/**
		 * Reports the chain settled, unless that was already reported.
		 */
		void settle() {
			synchronized (this) {
				if (_settled) {
					return;
				}
				_settled = true;
			}
			if (_onSettled != null) {
				_onSettled.run();
			}
		}
	}
}
