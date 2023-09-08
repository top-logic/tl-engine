/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core algorithm for registering listeners and notifying them.
 * 
 * @param <L>
 *        The listener interface.
 * @param <E>
 *        The event type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractObservable<L, E> {

	private boolean _iterating;

	private List<L> _listeners = Collections.emptyList();

	/**
	 * Remove the given listener.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the listener was registered before removal (something changed).
	 */
	protected boolean removeListener(L listener) {
		boolean wasRemoved = hasListener(listener);
		if (wasRemoved) {
			copyWhenIterating();
			_listeners.remove(listener);
		}
		return wasRemoved;
	}

	/**
	 * Add the given listener.
	 * 
	 * @param listener
	 *        the listener to add.
	 * @return Whether the given listener was not registered before (newly registered).
	 */
	protected boolean addListener(L listener) {
		boolean wasAdded = !hasListener(listener);
		if (wasAdded) {
			if (_listeners == Collections.emptyList()) {
				copy();
			} else {
				copyWhenIterating();
			}
			_listeners.add(listener);
		}
		return wasAdded;
	}

	/**
	 * Informs all registered listeners about the given event.
	 * 
	 * @param event
	 *        The event that occurred.
	 */
	protected void notifyListeners(E event) {
		boolean startedIterating = !_iterating;

		if (startedIterating) {
			_iterating = true;
		}
		try {
			// Note: The listeners member variable has to be copied to the stack, since it may be
			// updated when listeners deregister as result of being notified.
			List<L> currentListeners = _listeners;
			for (int n = 0, cnt = currentListeners.size(); n < cnt; n++) {
				sendEvent(currentListeners.get(n), event);
			}
		} finally {
			if (startedIterating) {
				_iterating = false;
			}
		}
	}

	/**
	 * Concrete implementation of sending the evnet (specific to the concrete listener interface).
	 * 
	 * @param listener
	 *        The listener that should be notified.
	 * @param event
	 *        The event that occurred.
	 */
	protected abstract void sendEvent(L listener, E event);

	private void copyWhenIterating() {
		if (_iterating) {
			copy();
			_iterating = false;
		}
	}

	private void copy() {
		List<L> before = _listeners;
		_listeners = new ArrayList<>(Math.min(3, before.size() + 1));
		_listeners.addAll(before);
	}

	/**
	 * Whether listeners are registered.
	 */
	protected final boolean hasListeners() {
		return !_listeners.isEmpty();
	}

	/**
	 * Whether the given listener is registered.
	 */
	protected final boolean hasListener(L listener) {
		return _listeners.contains(listener);
	}

}
