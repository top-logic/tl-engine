/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;

/**
 * Per-thread scope tracking {@link ViewChannel} listener notifications in progress, letting
 * controls defer work until the outermost notification has unwound.
 *
 * <p>
 * Work is deferred that must not run while the listeners of a notification are still being called,
 * typically a write to a channel from inside a listener: written from there, the new value would
 * reach the remaining listeners before the value they are being told about, so that they end up
 * with the outdated one (e.g. a form resetting its edit-mode channel after a refused transition).
 * Such work is {@link #afterNotification(Runnable) deferred} and runs once the outermost
 * notification has completed - safely outside any listener iteration.
 * </p>
 *
 * <p>
 * Channel implementations enter the scope around their listener notification loop (see
 * {@link DefaultViewChannel} and {@link DerivedViewChannel}), so deferral engages automatically for
 * every channel write, no matter which control or command triggered it. Notifications run
 * synchronously on the writing thread, so the scope is thread-confined.
 * </p>
 */
public final class ChannelNotificationScope {

	private static final ThreadLocal<ChannelNotificationScope> SCOPE =
		ThreadLocal.withInitial(ChannelNotificationScope::new);

	private int _depth;

	private final List<Runnable> _pending = new ArrayList<>();

	private ChannelNotificationScope() {
		// Only created through the thread-local accessor.
	}

	/**
	 * The {@link ChannelNotificationScope} of the current thread, creating it on first access.
	 *
	 * @return The current thread's scope, never {@code null}.
	 */
	public static ChannelNotificationScope current() {
		return SCOPE.get();
	}

	/**
	 * Runs the given action once the outermost channel notification has completed, or immediately
	 * when no notification is in progress.
	 *
	 * @param action
	 *        The action to run (e.g. writing a channel).
	 */
	public void afterNotification(Runnable action) {
		if (_depth == 0) {
			action.run();
		} else {
			_pending.add(action);
		}
	}

	/**
	 * Enters a listener notification; called by channel implementations before iterating their
	 * listeners.
	 */
	void enter() {
		_depth++;
	}

	/**
	 * Exits a listener notification; called by channel implementations after iterating their
	 * listeners. When the outermost notification exits, all
	 * {@link #afterNotification(Runnable) deferred} actions run.
	 *
	 * <p>
	 * Every deferred action runs, even if an earlier one fails: the actions are independent, and a
	 * skipped action would leave the state it was meant to settle (e.g. a channel that is to mirror
	 * a control's state) inconsistent for the rest of the session. A failing action is
	 * logged rather than rethrown, because this method runs in the {@code finally} block of the
	 * notification: an exception thrown here would replace the one a listener may have raised,
	 * which is the failure the caller has to see.
	 * </p>
	 */
	void exit() {
		_depth--;
		if (_depth > 0) {
			return;
		}
		try {
			if (!_pending.isEmpty()) {
				List<Runnable> actions = new ArrayList<>(_pending);
				_pending.clear();
				for (Runnable action : actions) {
					try {
						action.run();
					} catch (RuntimeException ex) {
						Logger.error("Deferred action after channel notification failed: " + action, ex,
							ChannelNotificationScope.class);
					}
				}
			}
		} finally {
			// Drop the thread-local entry so pooled threads do not retain the scope.
			SCOPE.remove();
		}
	}

}
