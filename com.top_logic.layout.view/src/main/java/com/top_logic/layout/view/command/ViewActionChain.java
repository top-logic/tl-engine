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
		Deque<Runnable> compensations = new ArrayDeque<>();
		runFrom(context, actions, 0, input, compensations,
			value -> settled(onComplete, value),
			() -> {
				runCompensations(compensations);
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
		runFrom(context, actions, 0, input, compensations,
			continuation::resume,
			() -> {
				runCompensations(compensations);
				continuation.abort();
			});
	}

	private static void runFrom(ReactContext context, List<ViewAction> actions, int index, Object input,
			Deque<Runnable> compensations, Consumer<Object> onComplete, Runnable onAbort) {
		if (index >= actions.size()) {
			onComplete.accept(input);
			return;
		}
		ViewAction action = actions.get(index);
		Continuation continuation = new Continuation() {
			private boolean _spent;

			@Override
			public void resume(Object value) {
				spend();
				runFrom(context, actions, index + 1, value, compensations, onComplete, onAbort);
			}

			@Override
			public void abort() {
				spend();
				onAbort.run();
			}

			@Override
			public void onAbort(Runnable compensation) {
				if (_spent) {
					throw new IllegalStateException("onAbort() after the action already continued.");
				}
				compensations.push(compensation);
			}

			private void spend() {
				if (_spent) {
					throw new IllegalStateException("Continuation already used (resume/abort called twice).");
				}
				_spent = true;
			}
		};
		try {
			action.execute(context, input, continuation);
		} catch (RuntimeException failure) {
			runCompensations(compensations);
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
}
