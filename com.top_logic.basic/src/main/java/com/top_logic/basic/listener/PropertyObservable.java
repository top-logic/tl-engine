/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;


/**
 * Interface for objects that have different modifiable properties and which offers the inform
 * listener about changes of them.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PropertyObservable {

	/**
	 * Global special {@link EventType}.
	 * 
	 * <p>
	 * {@link GenericPropertyListener} that are registered for that {@link EventType} are informed
	 * about all changes done in this {@link PropertyObservable}.
	 * </p>
	 * 
	 * <p>
	 * If the value for an {@link EventType} changes, first the special listener and then zhe global
	 * listeners are informed.
	 * </p>
	 */
	EventType<GenericPropertyListener, Object, Object> GLOBAL_LISTENER_TYPE =
		new EventType<>("globalEvent") {

			@Override
			public Bubble dispatch(GenericPropertyListener listener, Object sender, Object oldValue, Object newValue) {
				StringBuilder error = new StringBuilder();
				error.append(GenericPropertyListener.class.getName());
				error.append(" are triggered direct.");
				throw new UnsupportedOperationException(error.toString());
			}

			@Override
			EventType.Bubble delegate(EventType<?, ?, ?> eventType, GenericPropertyListener listener, Object sender,
					Object oldValue, Object newValue) {
				return listener.handlePropertyChanged(eventType, sender, oldValue, newValue);
			}

		};

	/**
	 * Adds the given listener to be informed about changed of the given type.
	 * 
	 * @param type
	 *        If the property for the given type changes, the given listener is informed.
	 * @param listener
	 *        If the property for the given type changes, the given listener is informed.
	 * @return Whether the listener was newly registered.
	 */
	<L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener);

	/**
	 * Removes the given listener from being informed about changed of the given type.
	 * 
	 * <p>
	 * If currently a change is propagated, it is possible that the given listener is although
	 * informed.
	 * </p>
	 * 
	 * @param type
	 *        If the property for the given type changes, the given listener is not longer informed.
	 * @param listener
	 *        If the property for the given type changes, the given listener is not longer informed.
	 * @return Whether the listener was removed successful.
	 */
	<L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener);

}

