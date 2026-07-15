/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.ReactContext;

/**
 * Lightweight functional building block for composable view commands.
 *
 * <p>
 * Unlike {@link ViewCommand}, a {@link ViewAction} has no configuration for label, image,
 * executability, or other presentation concerns. It is purely functional: takes an input, produces
 * an output. Actions are chained in a {@link GenericViewCommand} where each action's return value
 * becomes the next action's input.
 * </p>
 *
 * <p>
 * The chain always drives actions through {@link #execute(ReactContext, Object, Continuation)}. A
 * regular action only implements the synchronous {@link #execute(ReactContext, Object)} and is
 * adapted automatically. An action that needs to <em>suspend</em> the chain (e.g. to await a
 * confirmation dialog) extends {@link InterruptibleViewAction} and implements the
 * {@link Continuation}-based form instead.
 * </p>
 */
public interface ViewAction {

	/**
	 * Executes this action synchronously.
	 *
	 * @param context
	 *        The React context.
	 * @param input
	 *        The input value, typically the output of the previous action in the chain, or
	 *        {@code null} for the first action.
	 * @return The result to pass to the next action, or the final result of the chain.
	 */
	Object execute(ReactContext context, Object input);

	/**
	 * Executes this action within the chain, continuing via the given {@link Continuation}.
	 *
	 * <p>
	 * The default adapts the synchronous {@link #execute(ReactContext, Object)} - compute a value
	 * and {@link Continuation#resume(Object) resume} immediately. Actions that may suspend or abort
	 * the chain override this (see {@link InterruptibleViewAction}) and call
	 * {@link Continuation#resume(Object)} / {@link Continuation#abort()} themselves, possibly later.
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param input
	 *        The input value (output of the previous action, or {@code null} for the first).
	 * @param continuation
	 *        Used to continue (or cancel) the chain.
	 */
	default void execute(ReactContext context, Object input, Continuation continuation) {
		continuation.resume(execute(context, input));
	}
}
