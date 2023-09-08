/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ListenerRegistry} which is not thread safe and stores the {@link Listener}s directly.
 * 
 * @see ListenerRegistryFactory
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
class SimpleListenerRegistry<T> implements ListenerRegistry<T> {

	/** This has to be a {@link List}, as it has to keep the insertion order. */
	private final List<Listener<? super T>> _listeners = new ArrayList<>();

	/** Use the {@link ListenerRegistryFactory} instead. */
	SimpleListenerRegistry() {
		// Reduce visibility
	}

	@Override
	public void register(Listener<? super T> newListener) {
		_listeners.add(newListener);
	}

	@Override
	public void unregister(Listener<? super T> oldListener) {
		_listeners.remove(oldListener);
	}

	@Override
	public void notify(T notification) {
		for (Listener<? super T> listener : _listeners) {
			listener.notify(notification);
		}
	}

}
