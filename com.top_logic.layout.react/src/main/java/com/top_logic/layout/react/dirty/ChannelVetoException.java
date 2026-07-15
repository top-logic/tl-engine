/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.dirty;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when a tab switch or channel value change is blocked because dependent forms have unsaved
 * changes.
 *
 * <p>
 * Carries the list of all dirty {@link StateHandler}s and a {@link Runnable} continuation that
 * retries the blocked action. The caller (typically
 * {@link com.top_logic.layout.react.control.ReactCommandInvoker}) catches this exception and opens
 * a confirmation dialog. After the user saves or discards, the continuation is executed to retry the
 * original action.
 * </p>
 */
public class ChannelVetoException extends RuntimeException {

	private final List<StateHandler> _dirtyHandlers;

	private final Runnable _continuation;

	private Runnable _rollback;

	private static final Runnable NOOP = () -> { };

	/**
	 * Creates a new {@link ChannelVetoException}.
	 *
	 * @param dirtyHandlers
	 *        The dirty state handlers that caused the veto. Must not be empty.
	 * @param continuation
	 *        A {@link Runnable} that retries the blocked action after dirty state is resolved.
	 */
	public ChannelVetoException(List<StateHandler> dirtyHandlers, Runnable continuation) {
		super("Channel change vetoed by " + dirtyHandlers.size() + " dirty handler(s).");
		_dirtyHandlers = Collections.unmodifiableList(dirtyHandlers);
		_continuation = continuation;
	}

	/**
	 * The dirty state handlers that blocked the channel change.
	 */
	public List<StateHandler> getDirtyHandlers() {
		return _dirtyHandlers;
	}

	/**
	 * Retries the blocked action.
	 *
	 * <p>
	 * Must only be called after all dirty handlers have been saved or discarded, so that the retry
	 * does not trigger another veto.
	 * </p>
	 */
	public Runnable getContinuation() {
		return _continuation;
	}

	/**
	 * Optional rollback action that reverts optimistic UI changes made before the veto was thrown.
	 *
	 * <p>
	 * Called when the user cancels the confirmation dialog. For example, a table that has already
	 * updated its visual selection before the channel veto was raised uses this to restore the
	 * previous selection.
	 * </p>
	 *
	 * @return The rollback action, or {@code null} if no rollback is needed.
	 */
	public Runnable getRollback() {
		return _rollback;
	}

	/**
	 * Adds a rollback action. Multiple rollbacks are chained and executed in the order they were
	 * added.
	 *
	 * @param rollback
	 *        An action to revert optimistic UI changes on cancel.
	 */
	public void addRollback(Runnable rollback) {
		if (_rollback == null) {
			_rollback = rollback;
		} else {
			Runnable previous = _rollback;
			_rollback = () -> {
				previous.run();
				rollback.run();
			};
		}
	}
}
