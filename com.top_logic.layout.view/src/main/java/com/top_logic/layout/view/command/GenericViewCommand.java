/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that executes a configured chain of {@link ViewAction}s.
 *
 * <p>
 * Each action's return value becomes the input of the next action. The first action receives the
 * command's input (typically {@code null} for toolbar buttons). This allows composing complex
 * behaviors from simple, reusable building blocks.
 * </p>
 *
 * <p>
 * Example: Create a transient object and open a dialog with it:
 * </p>
 *
 * <pre>
 * &lt;generic-command label="New"&gt;
 *   &lt;execute-script function="x -&gt; new(`my.module:MyType`, transient: true)"/&gt;
 *   &lt;open-dialog dialog-view="demo/create.view.xml" bind-input-to="model"/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
public class GenericViewCommand implements ViewCommand {

	/**
	 * Configuration for {@link GenericViewCommand}.
	 */
	@TagName("generic-command")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(GenericViewCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * The chain of actions to execute sequentially.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<ViewAction>> getActions();
	}

	private final List<ViewAction> _actions;

	/**
	 * Creates a new {@link GenericViewCommand}.
	 */
	@CalledByReflection
	public GenericViewCommand(InstantiationContext context, Config config) {
		_actions = config.getActions().stream()
			.map(c -> context.getInstance(c))
			.filter(a -> a != null)
			.toList();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		runFrom(context, 0, input, new ArrayDeque<>());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Runs the action at {@code index} and, via its {@link Continuation}, the remaining actions.
	 *
	 * <p>
	 * Synchronous actions resume inline, so the chain completes within this call. An interruptible
	 * action may resume later (the chain continues then) or abort (the remaining actions are skipped
	 * and the {@code compensations} registered so far run, newest first). A thrown failure triggers
	 * the same compensation unwind before propagating.
	 * </p>
	 *
	 * @param compensations
	 *        Shared rollback stack accumulated by {@link Continuation#onAbort(Runnable)} across the
	 *        whole chain.
	 */
	private void runFrom(ReactContext context, int index, Object input, Deque<Runnable> compensations) {
		if (index >= _actions.size()) {
			return;
		}
		ViewAction action = _actions.get(index);
		Continuation continuation = new Continuation() {
			private boolean _spent;

			@Override
			public void resume(Object value) {
				spend();
				runFrom(context, index + 1, value, compensations);
			}

			@Override
			public void abort() {
				spend();
				runCompensations(compensations);
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
