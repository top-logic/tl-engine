/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.view.form.StateHandler;

/**
 * Tracks the dirty state of multiple {@link StateHandler}s within a scope (e.g. a tab).
 *
 * <p>
 * Each {@link StateHandler} (typically a {@code FormControl}) registers its dirty state via
 * {@link #updateState(StateHandler, boolean)}. The channel aggregates these to answer "are there any
 * dirty handlers in this scope?" without requiring the caller to traverse a control tree.
 * </p>
 *
 * @see StateHandler
 */
public class DirtyChannel {

	private final Map<StateHandler, Boolean> _states = new LinkedHashMap<>();

	/**
	 * Updates the dirty state of a handler.
	 *
	 * @param handler
	 *        The state handler.
	 * @param dirty
	 *        {@code true} if dirty, {@code false} if clean. A clean handler is removed from the
	 *        tracking map.
	 */
	public void updateState(StateHandler handler, boolean dirty) {
		if (dirty) {
			_states.put(handler, Boolean.TRUE);
		} else {
			_states.remove(handler);
		}
	}

	/**
	 * Removes a handler from tracking, regardless of its dirty state.
	 *
	 * <p>
	 * Called during cleanup when a handler is disposed.
	 * </p>
	 *
	 * @param handler
	 *        The handler to remove.
	 */
	public void removeHandler(StateHandler handler) {
		_states.remove(handler);
	}

	/**
	 * Whether any tracked handler is currently dirty.
	 */
	public boolean hasDirtyHandlers() {
		return !_states.isEmpty();
	}

	/**
	 * Returns all currently dirty handlers.
	 *
	 * @return An unmodifiable snapshot of the dirty handlers.
	 */
	public List<StateHandler> getDirtyHandlers() {
		return List.copyOf(_states.keySet());
	}
}
