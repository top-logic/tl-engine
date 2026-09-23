/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.layout.view.ViewContext;

/**
 * The commands of a region of the view that have not finished: they ran up to an action that holds
 * their chain and continue when it hands the chain back.
 *
 * <p>
 * A command whose chain settles within the interaction that started it never appears here. One that
 * returns while an action still holds it - a job running in the background, a question the user has
 * not answered - counts as suspended until that action settles the chain, by any outcome.
 * </p>
 *
 * <p>
 * Established by the element that owns the region as a scope of its {@link ViewContext}, so
 * everything built below it shares it: a dialog keeps itself open while a command it started is
 * still running, and the command's own cancel button says so instead of doing nothing.
 * </p>
 *
 * @see ViewContext#getScope(Class)
 */
public class SuspendedCommands {

	private int _count;

	private final List<Runnable> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Whether a command of this region is waiting for an action to hand its chain back.
	 */
	public synchronized boolean hasSuspended() {
		return _count > 0;
	}

	/**
	 * Records that one more command of this region has been suspended.
	 *
	 * @see #settle()
	 */
	public void suspend() {
		boolean changed;
		synchronized (this) {
			changed = _count++ == 0;
		}
		if (changed) {
			notifyListeners();
		}
	}

	/**
	 * Records that one of the {@link #suspend() suspended} commands has settled.
	 *
	 * @throws IllegalStateException
	 *         If no command of this region is suspended.
	 */
	public void settle() {
		boolean changed;
		synchronized (this) {
			if (_count == 0) {
				throw new IllegalStateException("No suspended command to settle.");
			}
			changed = --_count == 0;
		}
		if (changed) {
			notifyListeners();
		}
	}

	/**
	 * Begins reporting changes of {@link #hasSuspended()}.
	 *
	 * @param listener
	 *        Run whenever this region goes from having no suspended command to having one, and
	 *        back.
	 * @return Stops the reporting again; never {@code null}.
	 */
	public Runnable observe(Runnable listener) {
		_listeners.add(listener);
		return () -> _listeners.remove(listener);
	}

	private void notifyListeners() {
		for (Runnable listener : _listeners) {
			listener.run();
		}
	}
}
