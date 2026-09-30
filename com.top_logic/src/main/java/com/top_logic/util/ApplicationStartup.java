/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.cluster.ClusterManager.NodeState;
import com.top_logic.basic.Logger;

/**
 * Notification about the completion of the application startup on this node.
 *
 * <p>
 * The {@link AbstractStartStopListener} marks the startup as {@link #begin() in progress}, before it
 * starts the module system, and {@link #complete() completes} it, when this node has become
 * {@link NodeState#RUNNING}. A service that must not become active while the application is still
 * booting registers an action with {@link #whenStarted(Runnable)}. The action runs immediately, if
 * no startup is in progress, e.g. when the service is restarted in a running application, or when a
 * test starts services without booting an application.
 * </p>
 */
public final class ApplicationStartup {

	private static final ApplicationStartup INSTANCE = new ApplicationStartup();

	/**
	 * Whether the application startup is in progress.
	 *
	 * <p>
	 * Guarded by <code>this</code>.
	 * </p>
	 */
	private boolean _inProgress;

	/**
	 * Actions waiting for the completion of the startup.
	 *
	 * <p>
	 * Guarded by <code>this</code>.
	 * </p>
	 */
	private final List<Runnable> _pending = new ArrayList<>();

	private ApplicationStartup() {
		// Singleton.
	}

	/**
	 * The {@link ApplicationStartup} of this application.
	 */
	public static ApplicationStartup getInstance() {
		return INSTANCE;
	}

	/**
	 * Whether the application has started, i.e. no application startup is in progress.
	 */
	public synchronized boolean isStarted() {
		return !_inProgress;
	}

	/**
	 * Runs the given action as soon as the application has started.
	 *
	 * <p>
	 * If the application {@link #isStarted() has started}, the action runs immediately in the
	 * calling thread. Otherwise, it runs once in the thread that {@link #complete() completes} the
	 * startup, unless it is {@link #cancel(Runnable) cancelled} before.
	 * </p>
	 *
	 * @param action
	 *        The action to run.
	 */
	public void whenStarted(Runnable action) {
		synchronized (this) {
			if (_inProgress) {
				_pending.add(action);
				return;
			}
		}
		action.run();
	}

	/**
	 * Removes an action registered with {@link #whenStarted(Runnable)} that has not run yet.
	 *
	 * @param action
	 *        The action to remove. Nothing happens, if the action is not waiting.
	 */
	public synchronized void cancel(Runnable action) {
		_pending.remove(action);
	}

	/**
	 * Marks the application startup as in progress.
	 *
	 * <p>
	 * Actions registered with {@link #whenStarted(Runnable)} wait from now on until the startup is
	 * {@link #complete() completed}.
	 * </p>
	 */
	synchronized void begin() {
		_inProgress = true;
	}

	/**
	 * Marks the application startup as complete and runs all actions waiting for it.
	 */
	void complete() {
		List<Runnable> actions;
		synchronized (this) {
			_inProgress = false;
			actions = new ArrayList<>(_pending);
			_pending.clear();
		}
		for (Runnable action : actions) {
			try {
				action.run();
			} catch (RuntimeException ex) {
				Logger.error("Action after application startup failed: " + action, ex, ApplicationStartup.class);
			}
		}
	}

}
