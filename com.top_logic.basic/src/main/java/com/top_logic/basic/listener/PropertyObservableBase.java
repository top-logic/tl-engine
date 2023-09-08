/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.InlineSet;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType.Bubble;


/**
 * Base class implementing {@link PropertyObservable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class PropertyObservableBase implements PropertyObservable {
	
	/**
	 * Map of the {@link EventType} to the registered {@link PropertyListener} for that type.
	 * 
	 * <p>
	 * The value for an {@link EventType} is never empty.
	 * <p>
	 */
	@Inspectable
	private Map<EventType<?, ?, ?>, List<?>> _listeners = emptyMap();

	/**
	 * Whether global listeners are registered.
	 */
	private boolean _globalListeners = false;

	/**
	 * Inline set containing the {@link NamedConstant event types} for which an iteration is
	 * currently runnning.
	 */
	private Object _iterating = InlineSet.newInlineSet();

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		if (type == GLOBAL_LISTENER_TYPE) {
			boolean added = internalAddListener(type, listener);
			_globalListeners = true;
			return added;
		} else {
			return internalAddListener(type, listener);
		}
	}

	private <L extends PropertyListener, S, V> boolean internalAddListener(EventType<L, S, V> type, L listener) {
		List<L> listeners = get(type);
		boolean isNew = !listeners.contains(listener);
		if (isNew) {
			if (listeners == emptyList()) {
				listeners = copy(listeners, type);
			} else {
				listeners = copyWhenIterating(listeners, type);
			}

			boolean first = listeners.isEmpty();
			listeners.add(listener);
			if (first) {
				firstListenerAdded(type);
			}
		}
		return isNew;
	}

	private <L extends PropertyListener, S, V> List<L> copyWhenIterating(List<L> listeners, EventType<L, S, V> type) {
		if (currentlyIterating(type)) {
			List<L> copy = copy(listeners, type);
			_iterating = InlineSet.remove(_iterating, type);
			return copy;
		} else {
			return listeners;
		}
	}

	private boolean currentlyIterating(EventType<?, ?, ?> type) {
		return InlineSet.contains(_iterating, type);
	}

	private <L extends PropertyListener, S, V> List<L> copy(List<L> currentListeners, EventType<L, S, V> type) {
		List<L> before = currentListeners;
		List<L> copy = new ArrayList<>(Math.max(3, before.size() + 1));
		copy.addAll(currentListeners);
		put(type, copy);
		return copy;
	}

	private <L extends PropertyListener, S, V> boolean internalRemoveListener(EventType<L, S, V> type, L listener) {
		List<L> listeners = get(type);
		boolean isContained = listeners.contains(listener);
		if (isContained) {
			if (listeners.size() == 1) {
				// Only the given listener is contained.
				_listeners.remove(type);

				lastListenerRemoved(type);
			} else {
				listeners = copyWhenIterating(listeners, type);
				listeners.remove(listener);
			}
		}
		return isContained;
	}

	/**
	 * Hook that is called, if the first listener of a certain type is added.
	 * 
	 * <p>
	 * Note: The catch-all listener type is not considered specially. A
	 * {@link PropertyObservable#GLOBAL_LISTENER_TYPE} may already be registered, when this method
	 * is called for any other listener type.
	 * </p>
	 * 
	 * @param type
	 *        The added listener type.
	 */
	protected void firstListenerAdded(EventType<?, ?, ?> type) {
		// Hook for subclasses.
	}

	/**
	 * Hook that is called, if the last listener of a certain type is removed.
	 * 
	 * <p>
	 * Note: The catch-all listener type is not considered specially. A
	 * {@link PropertyObservable#GLOBAL_LISTENER_TYPE} may still be present, when this method is
	 * called for any other listener type.
	 * </p>
	 * 
	 * @param type
	 *        The added listener type.
	 */
	protected void lastListenerRemoved(EventType<?, ?, ?> type) {
		// Hook for subclasses.
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		if (type == GLOBAL_LISTENER_TYPE) {
			boolean removed = internalRemoveListener(type, listener);
			if (removed) {
				_globalListeners = hasListeners(type);
			}
			return removed;
		} else {
			return internalRemoveListener(type, listener);
		}
	}

	@SuppressWarnings("unchecked")
	private <L extends PropertyListener, S, V> List<L> get(EventType<L, S, V> eventType) {
		List<L> listeners = (List<L>) _listeners.get(eventType);
		if (listeners == null) {
			listeners = emptyList();
		}
		return listeners;
	}

	private <L extends PropertyListener, S, V> void put(EventType<L, S, V> type, List<L> currentListeners) {
		if (_listeners == emptyMap()) {
			_listeners = new HashMap<>();
		}
		_listeners.put(type, currentListeners);
	}

	private static <L extends PropertyListener> List<L> emptyList() {
		return Collections.emptyList();
	}

	private static Map<EventType<?, ?, ?>, List<?>> emptyMap() {
		return Collections.emptyMap();
	}

	/**
	 * Informs all registered listeners about the given event.
	 * 
	 * @param type
	 *        The event that occurred.
	 * @param sender
	 *        The sender of this event.
	 * @param oldValue
	 *        Old property value.
	 * @param newValue
	 *        New property value.
	 */
	protected <L extends PropertyListener, S, V> Bubble notifyListeners(EventType<L, ? super S, V> type, S sender,
			V oldValue, V newValue) {
		Bubble bubble = Bubble.BUBBLE;
		bubble = internalNotifyListeners(bubble, type, type, sender, oldValue, newValue);
		bubble = notifyGlobalListeners(bubble, type, sender, oldValue, newValue);
		return bubble;
	}

	private Bubble notifyGlobalListeners(Bubble bubble, EventType<?, ?, ?> type, Object sender, Object oldValue,
			Object newValue) {
		if (!_globalListeners) {
			// shortcut if no global listeners exist.
			return bubble;
		}
		
		return internalNotifyListeners(bubble, GLOBAL_LISTENER_TYPE, type, sender, oldValue, newValue);
	}

	private <L extends PropertyListener, S, V> Bubble internalNotifyListeners(Bubble bubble,
			EventType<L, ? super S, V> listenerType, EventType<?, ?, ?> eventType, S sender, V oldValue, V newValue) {
		boolean startedIterating = !currentlyIterating(listenerType);
		if (startedIterating) {
			_iterating = InlineSet.add(EventType.class, _iterating, listenerType);
		}
		try {
			// Note: The listeners member variable has to be copied to the stack, since it may be
			// updated when listeners deregister as result of being notified.
			List<L> currentListeners = get(listenerType);
			for (int n = 0, cnt = currentListeners.size(); n < cnt; n++) {
				L listener = currentListeners.get(n);
				Bubble localBubble = listenerType.delegate(eventType, listener, sender, oldValue, newValue);
				if (localBubble == Bubble.CANCEL_BUBBLE) {
					bubble = Bubble.CANCEL_BUBBLE;
				}
			}
			return bubble;
		} finally {
			if (startedIterating) {
				_iterating = InlineSet.remove(_iterating, listenerType);
			}
		}
	}

	/**
	 * Whether any listener is registered.
	 */
	protected boolean hasListeners() {
		return !_listeners.isEmpty();
	}

	/**
	 * Whether a listener for the given type is registered.
	 * <p>
	 * This does not include the global listeners. If a check for the registration of global
	 * listeners is needed call this method with {@link PropertyObservable#GLOBAL_LISTENER_TYPE}.
	 * </p>
	 */
	protected boolean hasListeners(EventType<?, ?, ?> eventType) {
		return _listeners.containsKey(eventType);
	}


}
