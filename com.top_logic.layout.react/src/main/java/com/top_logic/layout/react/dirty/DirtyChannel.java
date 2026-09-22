/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.dirty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks the dirty state of multiple {@link StateHandler}s within a scope (e.g. a tab).
 *
 * <p>
 * Each {@link StateHandler} (typically a {@code FormControl}) registers its dirty state via
 * {@link #updateState(StateHandler, boolean)}. The channel aggregates these to answer "are there any
 * dirty handlers in this scope?" without requiring the caller to traverse a control tree.
 * </p>
 *
 * <p>
 * Scopes nest, and so do their channels: a tab lies within the sidebar item displaying it, and a
 * form the user typed into sits in both. Such a form is reported to the tab's channel, which
 * forwards it to the {@link #DirtyChannel(DirtyChannel) enclosing} item's channel. Leaving the tab
 * therefore asks about the forms of that tab, and leaving the item asks about the forms of all its
 * tabs.
 * </p>
 *
 * @see StateHandler
 */
public class DirtyChannel {

	private final DirtyChannel _parent;

	private final Map<StateHandler, Boolean> _states = new LinkedHashMap<>();

	/**
	 * Creates a {@link DirtyChannel} for a scope that no dirty-tracked scope encloses.
	 */
	public DirtyChannel() {
		this(null);
	}

	/**
	 * Creates a {@link DirtyChannel} for a scope lying within the scope of another channel.
	 *
	 * @param parent
	 *        The channel of the enclosing scope, which every handler reported here is reported to as
	 *        well. May be {@code null} for a scope nothing dirty-tracked encloses.
	 */
	public DirtyChannel(DirtyChannel parent) {
		_parent = parent;
	}

	/**
	 * Updates the dirty state of a handler.
	 *
	 * <p>
	 * The state is that of the enclosing scope as much as of this one, so it reaches the channel of
	 * the enclosing scope as well.
	 * </p>
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
		if (_parent != null) {
			_parent.updateState(handler, dirty);
		}
	}

	/**
	 * Removes a handler from tracking, regardless of its dirty state.
	 *
	 * <p>
	 * Called during cleanup when a handler is disposed. A disposed handler holds nothing in the
	 * enclosing scope either, so it is dropped from the enclosing channel as well.
	 * </p>
	 *
	 * @param handler
	 *        The handler to remove.
	 */
	public void removeHandler(StateHandler handler) {
		_states.remove(handler);
		if (_parent != null) {
			_parent.removeHandler(handler);
		}
	}

	/**
	 * Whether any handler tracked by this channel is currently dirty.
	 */
	public boolean hasDirtyHandlers() {
		return !_states.isEmpty();
	}

	/**
	 * Returns all handlers of this channel that are currently dirty.
	 *
	 * @return An unmodifiable snapshot of the dirty handlers.
	 */
	public List<StateHandler> getDirtyHandlers() {
		return List.copyOf(_states.keySet());
	}
}
