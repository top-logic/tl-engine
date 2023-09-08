/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link ListenerRegistry} that uses {@link WeakReference} to store the {@link Listener}s but is
 * <b>not</b> thread safe.
 * 
 * @see ListenerRegistryFactory
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
class WeakListenerRegistry<T> implements ListenerRegistry<T> {

	/** This has to be a {@link List}, as it has to keep the insertion order. */
	private final List<WeakReference<Listener<? super T>>> _listeners =
		new ArrayList<>();

	/** Use the {@link ListenerRegistryFactory} instead. */
	WeakListenerRegistry() {
		// Reduce visibility
	}

	@Override
	public void register(Listener<? super T> newListener) {
		_listeners.add(new WeakReference<>(newListener));
	}

	@Override
	public void unregister(Listener<? super T> oldListener) {
		Iterator<WeakReference<Listener<? super T>>> iterator = _listeners.iterator();
		while (iterator.hasNext()) {
			WeakReference<Listener<? super T>> listenerRef = iterator.next();
			Listener<? super T> currentListener = listenerRef.get();
			if (oldListener.equals(currentListener)) {
				_listeners.remove(listenerRef);
				return;
			}
			if (currentListener == null) {
				iterator.remove();
			}
		}
	}

	@Override
	public void notify(T notification) {
		Iterator<WeakReference<Listener<? super T>>> iterator = _listeners.iterator();
		while (iterator.hasNext()) {
			WeakReference<Listener<? super T>> listenerRef = iterator.next();
			Listener<? super T> listener = listenerRef.get();
			if (listener == null) {
				iterator.remove();
				continue;
			}
			listener.notify(notification);
		}
	}

}
