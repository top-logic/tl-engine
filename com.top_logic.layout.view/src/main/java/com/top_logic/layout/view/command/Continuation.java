/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.control.button.ButtonTone;

/**
 * Control handed to a {@link ViewAction} to continue (or cancel) the surrounding action chain.
 *
 * <p>
 * A synchronous action {@link #resume(Object) resumes} immediately. An interruptible action (e.g. a
 * confirmation guard) may hold the {@link Continuation} and {@link #resume(Object) resume} later
 * (e.g. from a dialog's confirm handler) or {@link #abort() abort} (e.g. on cancel). An action that
 * has an undoable side effect can register an {@link #onAbort(Runnable) compensation} that runs if
 * the chain is later aborted or fails.
 * </p>
 */
public interface Continuation {

	/**
	 * Continues the chain; {@code value} becomes the input of the next action.
	 *
	 * <p>
	 * Must be called at most once per action, and is mutually exclusive with {@link #abort()}.
	 * </p>
	 */
	void resume(Object value);

	/**
	 * Cancels the chain: no further actions run, and the compensations registered via
	 * {@link #onAbort(Runnable)} by already-executed actions run in reverse (last-in-first-out)
	 * order.
	 *
	 * <p>
	 * Must be called at most once per action, and is mutually exclusive with
	 * {@link #resume(Object)}.
	 * </p>
	 */
	void abort();

	/**
	 * Registers a compensating action for the step that is executing now.
	 *
	 * <p>
	 * The compensation runs (newest first) if the chain is later {@link #abort() aborted} or fails
	 * with an exception. It must be registered before the action {@link #resume(Object) resumes}.
	 * </p>
	 *
	 * @param compensation
	 *        The cleanup/rollback to run on abort or failure.
	 */
	void onAbort(Runnable compensation);

	/**
	 * The tone of what follows the step that is executing now.
	 *
	 * <p>
	 * {@link ButtonTone#DANGER} when an action that runs after this step - in this chain, or after
	 * the action this chain is nested in - {@link ViewAction#getTone() is destructive}. A step that
	 * asks the user before the chain goes on (a {@link ConfirmAction}) draws its affirmative answer
	 * in this tone: that answer lets the destruction happen. The tone of the step itself and of the
	 * steps before it does not count.
	 * </p>
	 *
	 * @return {@link ButtonTone#DEFAULT} unless the continuation knows the rest of its chain.
	 */
	default ButtonTone tone() {
		return ButtonTone.DEFAULT;
	}
}
