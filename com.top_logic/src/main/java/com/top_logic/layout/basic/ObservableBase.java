/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Base class providing services for observable objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObservableBase {

	private Map<Class<?>, Collection<?>> lazyListeners;

	/**
	 * Removes the given listener to the internal listener storage.
	 * 
	 * @param listenerInterface
	 *        The concrete listener interface.
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given listener was previously added.
	 */
	protected final <T> boolean removeListener(Class<T> listenerInterface, T listener) {
		if (lazyListeners == null) {
			return false;
		}
		
		Collection<?> typedListeners = lazyListeners.get(listenerInterface);
		if (typedListeners == null) {
			return false;
		}
		
		return typedListeners.remove(listener);
	}

	/**
	 * Add the given listener to the internal listener storage.
	 * 
	 * @param listenerInterface
	 *        The concrete listener interface.
	 * @param listener
	 *        The listener to add.
	 * @return Whether the given listener was not already added.
	 */	
	@SuppressWarnings("unchecked")
	protected final <T> boolean addListener(Class<T> listenerInterface, T listener) {
		assert listenerInterface.isInstance(listener) : "Does not implement " + listenerInterface;
		
		if (lazyListeners == null) {
			lazyListeners = new HashMap<>();
		}
		
		final Collection<?> listeners = lazyListeners.get(listenerInterface);
		/*
		 * listeners are all of type T since we put a Collection<T> into if none
		 * already exists.
		 */
		Collection<T> typedListeners = (Collection<T>) listeners;
		if (typedListeners == null) {
			typedListeners = new HashSet<>();
			lazyListeners.put(listenerInterface, typedListeners);
		}
		
		return typedListeners.add(listener);
	}

	/**
	 * An array of listeners implementing the given listener interface for
	 * firing an event.
	 * 
	 * @param listenerInterface
	 *        The listener interface requested.
	 * @return An array of the given listener interface type containing all
	 *         listeners that were added for the given interface.
	 */
	protected final <T> List<T> getListeners(Class<T> listenerInterface) {
		Collection<T> listeners = getListenersDirect(listenerInterface);
		if (listeners == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(listeners);
	}

	/**
	 * Direct reference to the collection holding registered listeners, or <code>null</code> if no
	 * listeners of the given type are registerd.
	 * 
	 * <p>
	 * Note: The result <b>must not</b> be used to dispatch events to the listeners, because
	 * listeners may detach during event processing resulting in concurrent modification exceptsion.
	 * </p>
	 * 
	 * @param <T>
	 *        The listener type.
	 * @param listenerInterface
	 *        The dynamic type of the listener interface.
	 * @return Internal collection of registered listeners, or <code>null</code>, if no listeners of
	 *         the requested type have registered.
	 */
	protected <T> Collection<T> getListenersDirect(Class<T> listenerInterface) {
		if (lazyListeners == null) {
			return null;
		}
		
		final Collection<?> listeners = lazyListeners.get(listenerInterface);
		/*
		 * Listeners are of type T due to construction of {@link #lazyListeners}
		 * @see #addListener(Class, Object)
		 */
		@SuppressWarnings("unchecked")
		Collection<T> typedListeners = (Collection<T>) listeners;
		if (typedListeners == null || typedListeners.isEmpty()) {
			return null;
		}
		
		// Copy to prevent concurrent modifications.
		return typedListeners;
	}
	
    /**
     * Check if any such Listener is registered.
     * 
     * @param listenerInterface The listener interface requested.
     * @return true in case {@link #getListeners(Class)} will not be empty
     */
    protected final <T> boolean hasListeners(Class<T> listenerInterface) {
        if (lazyListeners == null) {
            return false;
        }
		Collection<?> listeners = lazyListeners.get(listenerInterface);
		if (listeners == null) {
			return false;
		}
		return !listeners.isEmpty();
    }

    /**
     * Check if any such Listener is registered.
     * 
     * @param listenerInterface The listener interface requested.
     * @return true in case {@link #getListeners(Class)} will contain someListener
     */
    protected final <T> boolean hasListener(Class<T> listenerInterface, T someListener) {
        if (lazyListeners == null) {
            return false;
        }
		Collection<?> listeners = lazyListeners.get(listenerInterface);
		if (listeners == null) {
			return false;
		}
		return listeners.contains(someListener);
    }

}
