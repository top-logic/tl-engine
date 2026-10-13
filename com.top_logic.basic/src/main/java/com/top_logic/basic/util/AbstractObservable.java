/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.listener.ListenerRegistration;
import com.top_logic.basic.listener.Registration;

/**
 * Core algorithm for registering listeners and notifying them.
 * 
 * <p>
 * Each registration of a listener is represented by a {@link ListenerRegistration} handle. A
 * notification iterates over the registrations that exist when it starts and skips those that are
 * {@link Registration#dispose() disposed} before the notification reaches them. A listener that is
 * deregistered by another listener during a notification is therefore not called anymore.
 * Registrations created during a notification are not notified in that notification.
 * </p>
 * 
 * <p>
 * The storage is never modified destructively: a new registration is appended, a disposed one is
 * only marked inactive. Once inactive entries outnumber the active ones, the storage is replaced by
 * a fresh list containing only the active entries, which leaves the list a running notification
 * iterates over untouched. Disposing a registration therefore costs amortized constant time.
 * </p>
 * 
 * @param <L>
 *        The listener interface.
 * @param <E>
 *        The event type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractObservable<L, E> {

	/**
	 * All registrations, including disposed ones that have not yet been removed.
	 */
	private List<Entry> _entries = Collections.emptyList();

	/**
	 * Number of active registrations in {@link #_entries}.
	 */
	private int _live;

	/**
	 * Number of disposed registrations still contained in {@link #_entries}.
	 */
	private int _dead;

	/**
	 * Remove the given listener.
	 * 
	 * <p>
	 * Disposes all active registrations of the given listener.
	 * </p>
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the listener was registered before removal (something changed).
	 */
	protected boolean removeListener(L listener) {
		boolean wasRemoved = false;
		List<Entry> entries = _entries;
		for (int n = 0, cnt = entries.size(); n < cnt; n++) {
			Entry entry = entries.get(n);
			if (entry.matches(listener)) {
				entry.dispose();
				wasRemoved = true;
			}
		}
		return wasRemoved;
	}

	/**
	 * Add the given listener, if it is not yet registered.
	 * 
	 * @param listener
	 *        the listener to add.
	 * @return Whether the given listener was not registered before (newly registered).
	 * 
	 * @see #register(Object)
	 */
	protected boolean addListener(L listener) {
		boolean wasAdded = !hasListener(listener);
		if (wasAdded) {
			register(listener);
		}
		return wasAdded;
	}

	/**
	 * Registers the given listener.
	 * 
	 * <p>
	 * In contrast to {@link #addListener(Object)}, each call creates a registration of its own, even
	 * if the listener is already registered. A listener registered twice is notified twice.
	 * </p>
	 * 
	 * @param listener
	 *        The listener to register, not <code>null</code>.
	 * @return The handle that ends the registration when {@link Registration#dispose() disposed}.
	 */
	protected ListenerRegistration<L> register(L listener) {
		Entry result = new Entry(Objects.requireNonNull(listener));
		if (_entries == Collections.<Entry> emptyList()) {
			_entries = new ArrayList<>();
		}
		// Note: Appending is safe even during a notification, since a notification only iterates
		// up to the size of the list at its start.
		_entries.add(result);
		_live++;
		return result;
	}

	/**
	 * Snapshot of all currently active registrations.
	 * 
	 * <p>
	 * The result is not affected by later registrations or by disposals. A caller that delivers an
	 * event later on uses {@link ListenerRegistration#isActive()} to skip registrations disposed in
	 * the meantime.
	 * </p>
	 */
	protected final List<ListenerRegistration<L>> registrations() {
		if (_live == 0) {
			return Collections.emptyList();
		}
		List<ListenerRegistration<L>> result = new ArrayList<>(_live);
		List<Entry> entries = _entries;
		for (int n = 0, cnt = entries.size(); n < cnt; n++) {
			Entry entry = entries.get(n);
			if (entry.isActive()) {
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Informs all registered listeners about the given event.
	 * 
	 * @param event
	 *        The event that occurred.
	 */
	protected void notifyListeners(E event) {
		// Note: The entries member variable has to be copied to the stack, since it is replaced
		// when disposed registrations are removed during the notification.
		List<Entry> entries = _entries;
		for (int n = 0, cnt = entries.size(); n < cnt; n++) {
			L listener = entries.get(n).getListener();
			if (listener != null) {
				sendEvent(listener, event);
			}
		}
	}

	/**
	 * Concrete implementation of sending the event (specific to the concrete listener interface).
	 * 
	 * @param listener
	 *        The listener that should be notified.
	 * @param event
	 *        The event that occurred.
	 */
	protected abstract void sendEvent(L listener, E event);

	/**
	 * Whether listeners are registered.
	 */
	protected final boolean hasListeners() {
		return _live > 0;
	}

	/**
	 * Whether the given listener is registered.
	 */
	protected final boolean hasListener(L listener) {
		List<Entry> entries = _entries;
		for (int n = 0, cnt = entries.size(); n < cnt; n++) {
			if (entries.get(n).matches(listener)) {
				return true;
			}
		}
		return false;
	}

	private void handleDisposed() {
		_live--;
		_dead++;
		if (_dead > _live) {
			compact();
		}
	}

	/**
	 * Replaces {@link #_entries} with a fresh list of the active entries.
	 * 
	 * <p>
	 * The list is not modified in place, since a running notification may iterate over it.
	 * </p>
	 */
	private void compact() {
		List<Entry> before = _entries;
		if (_live == 0) {
			_entries = Collections.emptyList();
		} else {
			List<Entry> after = new ArrayList<>(Math.max(3, _live + 1));
			for (int n = 0, cnt = before.size(); n < cnt; n++) {
				Entry entry = before.get(n);
				if (entry.isActive()) {
					after.add(entry);
				}
			}
			_entries = after;
		}
		_dead = 0;
	}

	/**
	 * A single registration of a listener in an {@link AbstractObservable}.
	 */
	private final class Entry implements ListenerRegistration<L> {

		private L _listener;

		Entry(L listener) {
			_listener = listener;
		}

		@Override
		public L getListener() {
			return _listener;
		}

		@Override
		public boolean isActive() {
			return _listener != null;
		}

		@Override
		public void dispose() {
			if (_listener == null) {
				return;
			}
			_listener = null;
			handleDisposed();
		}

		/**
		 * Whether this entry is an active registration of the given listener.
		 */
		boolean matches(Object listener) {
			return _listener != null && _listener.equals(listener);
		}

	}

}
