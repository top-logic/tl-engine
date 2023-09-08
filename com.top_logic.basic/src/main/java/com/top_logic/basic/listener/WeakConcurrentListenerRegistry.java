/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.lang.ref.WeakReference;
import java.util.List;

import com.top_logic.basic.col.CopyOnChangeListProvider;

/**
 * A {@link ListenerRegistry} that uses {@link WeakReference} to store the {@link Listener}s and is
 * thread safe.
 * 
 * @see ListenerRegistryFactory
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
class WeakConcurrentListenerRegistry<T> implements ListenerRegistry<T> {

	/** This has to be a {@link List}, as it has to keep the insertion order. */
	private final CopyOnChangeListProvider<WeakReference<Listener<? super T>>> _listeners =
		new CopyOnChangeListProvider<>();

	/** Use the {@link ListenerRegistryFactory} instead. */
	WeakConcurrentListenerRegistry() {
		// Reduce visibility
	}

	@Override
	public void register(Listener<? super T> newListener) {
		_listeners.add(new WeakReference<>(newListener));
	}

	@Override
	public void unregister(Listener<? super T> oldListener) {
		for (WeakReference<Listener<? super T>> listenerRef : _listeners.get()) {
			Listener<? super T> currentListener = listenerRef.get();
			if (oldListener.equals(currentListener)) {
				_listeners.remove(listenerRef);
				return;
			}
			if (currentListener == null) {
				_listeners.remove(listenerRef);
			}
		}
	}

	@Override
	public void notify(T notification) {
		for (WeakReference<Listener<? super T>> listenerRef : _listeners.get()) {
			Listener<? super T> listener = listenerRef.get();
			if (listener == null) {
				_listeners.remove(listenerRef);
				continue;
			}
			listener.notify(notification);
		}
	}

}
