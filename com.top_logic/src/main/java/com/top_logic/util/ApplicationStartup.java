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
 * The application has started, when the {@link AbstractStartStopListener} {@link #complete()
 * completes} the startup, i.e. when this node has become {@link NodeState#RUNNING}. A service that
 * must not become active before registers an action with {@link #whenStarted(Runnable)}: The action
 * runs, when the startup completes, or immediately, if the application has already started, e.g.
 * when the service is restarted in a running application.
 * </p>
 *
 * <p>
 * Without an application boot, e.g. when a test or a tool starts services, the startup never
 * completes, and actions registered with {@link #whenStarted(Runnable)} do not run. When the
 * application boots again in the same JVM, the {@link AbstractStartStopListener} resets the startup
 * with {@link #begin()}, before it starts the module system.
 * </p>
 */
public final class ApplicationStartup {

	private static final ApplicationStartup INSTANCE = new ApplicationStartup();

	/**
	 * Whether the application startup has completed.
	 *
	 * <p>
	 * Guarded by <code>this</code>.
	 * </p>
	 */
	private boolean _started;

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
	 * Whether the application startup has {@link #complete() completed}, and no further startup has
	 * {@link #begin() begun} since.
	 */
	public synchronized boolean isStarted() {
		return _started;
	}

	/**
	 * Runs the given action as soon as the application has started.
	 *
	 * <p>
	 * If the application {@link #isStarted() has started}, the action runs immediately in the
	 * calling thread. Otherwise, it runs once in the thread that {@link #complete() completes} the
	 * startup, unless it is {@link #cancel(Runnable) cancelled} before. As long as the startup does
	 * not complete, the action does not run.
	 * </p>
	 *
	 * @param action
	 *        The action to run.
	 */
	public void whenStarted(Runnable action) {
		synchronized (this) {
			if (!_started) {
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
	 * Marks the application as not started, when the application boots.
	 *
	 * <p>
	 * Actions registered with {@link #whenStarted(Runnable)} wait from now on until the startup is
	 * {@link #complete() completed}.
	 * </p>
	 */
	synchronized void begin() {
		_started = false;
	}

	/**
	 * Marks the application startup as complete and runs all actions waiting for it.
	 */
	void complete() {
		List<Runnable> actions;
		synchronized (this) {
			_started = true;
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
