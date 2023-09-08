/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of listeners with copy on write strategy during notification.
 * 
 * <p>
 * Listeners can be {@link #add(Object) added} to and {@link #remove(Object) removed} from this
 * container. Currently registered listeners are informed by calling
 * {@link #notifyListeners(NotificationCallback, Object)}.
 * </p>
 * 
 * <p>
 * Notification of listeners is inexpensive, since no copy of the listener list is required due to a
 * copy-on-write strategy during notification.
 * </p>
 * 
 * @param <L>
 *        The listener type.
 * @param <E>
 *        Type of event data to pass to the listener notification.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListenerContainer<L, E> {

	/**
	 * Strategy for informing a listener about an event.
	 * 
	 * @param <L>
	 *        The listener type.
	 * @param <E>
	 *        Type of event data to pass to the listener notification.
	 */
	public interface NotificationCallback<L, E> {
		/**
		 * Informs the given listener about an event.
		 * 
		 * @param listener
		 *        The listener to inform.
		 * @param event
		 *        The event to send to the given listener.
		 */
		void notifyListener(L listener, E event);
	}

	private List<L> _listeners = new ArrayList<>();

	private boolean _iterating;

	/**
	 * Drops all registered listeners.
	 */
	public void clear() {
		_listeners.clear();
		_iterating = false;
	}

	/**
	 * Adds the given listener to this container.
	 */
	public void add(L listener) {
		copyWhenIterating();
		_listeners.add(listener);
	}

	/**
	 * Removes the given listener from this container.
	 */
	public void remove(L listener) {
		copyWhenIterating();
		_listeners.remove(listener);
	}

	private void copyWhenIterating() {
		if (_iterating) {
			_listeners = new ArrayList<>(_listeners);

			// No need to copy again, if multiple modifications happen during a single notification
			// run.
			_iterating = false;
		}
	}

	/**
	 * Informs all currently registered listeners.
	 * 
	 * @param operation
	 *        The operation to invoke for each registered listener.
	 * @param event
	 *        The event data to pass to the listener notification operation.
	 */
	public void notifyListeners(NotificationCallback<L, E> operation, E event) {
		boolean before = startIterate();
		try {
			for (L listener : _listeners) {
				operation.notifyListener(listener, event);
			}
		} finally {
			stopIterate(before);
		}
	}

	private void stopIterate(boolean before) {
		// Note: If the iterating flag was reset in a copy-on-write operation, there is no need
		// to re-enable it even if it was active in a surrounding iteration step of the current
		// (potentially nested) notification run.
		if (_iterating) {
			_iterating = before;
		}
	}

	private boolean startIterate() {
		boolean before = _iterating;
		_iterating = true;
		return before;
	}

	/**
	 * Whether no listeners are currently registered.
	 * 
	 * <p>
	 * Use this information before actually invoking
	 * {@link #notifyListeners(NotificationCallback, Object)} to avoid constructing an event object
	 * when no listeners are there to receive the event.
	 * </p>
	 */
	public boolean idle() {
		return _listeners.isEmpty();
	}

}
