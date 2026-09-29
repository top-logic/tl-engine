/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

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
 */
public class ViewActionChain {

	/**
	 * Runs {@code actions} starting with {@code input}.
	 *
	 * @param onComplete
	 *        Invoked exactly once when the chain settles normally - either it ran past the last
	 *        action or it was aborted. It is <em>not</em> invoked when an action throws (the
	 *        exception propagates after compensations run). May be {@code null}.
	 */
	public static void run(ReactContext context, List<ViewAction> actions, Object input, Runnable onComplete) {
		runFrom(context, actions, 0, input, new ArrayDeque<>(), onComplete);
	}

	private static void runFrom(ReactContext context, List<ViewAction> actions, int index, Object input,
			Deque<Runnable> compensations, Runnable onComplete) {
		if (index >= actions.size()) {
			if (onComplete != null) {
				onComplete.run();
			}
			return;
		}
		ViewAction action = actions.get(index);
		Continuation continuation = new Continuation() {
			private boolean _spent;

			@Override
			public void resume(Object value) {
				spend();
				runFrom(context, actions, index + 1, value, compensations, onComplete);
			}

			@Override
			public void abort() {
				spend();
				runCompensations(compensations);
				if (onComplete != null) {
					onComplete.run();
				}
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
