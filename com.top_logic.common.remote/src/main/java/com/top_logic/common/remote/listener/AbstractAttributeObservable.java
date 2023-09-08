/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common listener handling code for {@link AttributeObservable} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractAttributeObservable implements AttributeObservable {

	private Map<String, Listeners> _listeners = new HashMap<>();

	@Override
	public boolean addAttributeListener(String property, AttributeListener listener) {
		Listeners listeners = _listeners.get(property);
		if (listeners == null) {
			listeners = new Listeners();
			_listeners.put(property, listeners);
		} else {
			if (listeners.contains(listener)) {
				return false;
			}
		}

		listeners.add(listener);
		return true;
	}

	@Override
	public boolean removeAttributeListener(String property, AttributeListener listener) {
		Listeners listeners = _listeners.get(property);
		if (listeners == null) {
			return false;
		}

		return listeners.remove(listener);
	}

	/**
	 * Informs registered listeners about a change of the given property.
	 * 
	 * @param property
	 *        The name of the property that has changed.
	 */
	protected void handleAttributeUpdate(String property) {
		Listeners listeners = _listeners.get(property);
		if (listeners != null) {
			listeners.handleAttributeUpdate(self(), property);
		}
		Listeners genericListeners = _listeners.get(null);
		if (genericListeners != null) {
			genericListeners.handleAttributeUpdate(self(), property);
		}
	}

	/**
	 * The sender reference to include in property change events.
	 */
	protected Object self() {
		return this;
	}

	static class Listeners {

		private boolean _iterating = false;

		private List<AttributeListener> _listeners = new ArrayList<>();

		public void handleAttributeUpdate(Object sender, String property) {
			boolean before = _iterating;
			_iterating = true;
			try {
				for (AttributeListener listener : _listeners) {
					listener.handleAttributeUpdate(sender, property);
				}
			} finally {
				if (_iterating) {
					_iterating = before;
				}
			}
		}

		public boolean contains(AttributeListener listener) {
			return _listeners.contains(listener);
		}

		public boolean remove(AttributeListener listener) {
			copyWhenIterating();
			return _listeners.remove(listener);
		}

		public void add(AttributeListener listener) {
			copyWhenIterating();
			_listeners.add(listener);
		}

		private void copyWhenIterating() {
			if (_iterating) {
				_listeners = new ArrayList<>(_listeners);
				_iterating = false;
			}
		}

	}

}
