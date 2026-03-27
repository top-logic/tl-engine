/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.view.form.StateHandler;

/**
 * Thrown when a {@link ViewChannel} value change is blocked by one or more
 * {@link ViewChannel.VetoListener}s because dependent forms have unsaved changes.
 *
 * <p>
 * Carries the list of all dirty {@link StateHandler}s and a {@link Runnable} continuation that
 * retries the blocked value change. The caller (typically
 * {@link com.top_logic.layout.react.control.ReactCommandInvoker}) catches this exception and opens
 * a confirmation dialog. After the user saves or discards, the continuation is executed to retry the
 * original action.
 * </p>
 */
public class ChannelVetoException extends RuntimeException {

	private final List<StateHandler> _dirtyHandlers;

	private final Runnable _continuation;

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
}
