/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-thread scope tracking {@link ViewChannel} listener notifications in progress, letting
 * controls defer work until the outermost notification has unwound.
 *
 * <p>
 * A channel notifies a <em>snapshot</em> of its listeners. A control that rebuilds its presentation
 * from inside such a notification (e.g. a responsive master-detail switching between selector and
 * detail on a selection change) must not dispose the replaced control subtree synchronously: the
 * old subtree's controls may themselves be listeners of the notifying channel, still pending in the
 * snapshot, and would then run on a torn-down control. Such disposal is
 * {@link #afterNotification(Runnable) deferred} and runs once the outermost notification has
 * completed — safely outside any listener iteration.
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
	 *        The action to run (e.g. disposing a replaced control subtree).
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
	 * listener snapshot.
	 */
	void enter() {
		_depth++;
	}

	/**
	 * Exits a listener notification; called by channel implementations after iterating their
	 * listener snapshot. When the outermost notification exits, all
	 * {@link #afterNotification(Runnable) deferred} actions run.
	 */
	void exit() {
		_depth--;
		if (_depth == 0) {
			if (!_pending.isEmpty()) {
				List<Runnable> actions = new ArrayList<>(_pending);
				_pending.clear();
				for (Runnable action : actions) {
					action.run();
				}
			}
			// Drop the thread-local entry so pooled threads do not retain the scope.
			SCOPE.remove();
		}
	}

}
