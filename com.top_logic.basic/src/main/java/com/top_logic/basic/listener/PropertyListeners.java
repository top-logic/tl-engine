/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import com.top_logic.basic.listener.EventType.Bubble;

/**
 * {@link PropertyObservable} for classes that can not inherit from {@link PropertyObservableBase}.
 * 
 * <p>
 * This class publishes the non public methods.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PropertyListeners extends PropertyObservableBase {

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
	 * @return Whether the event shall bubble.
	 */
	public <T extends PropertyListener, S, V> Bubble notify(EventType<T, ? super S, V> type, S sender, V oldValue,
			V newValue) {
		return notifyListeners(type, sender, oldValue, newValue);
	}

	/**
	 * Whether any listener is registered.
	 */
	public boolean has() {
		return hasListeners();
	}

	/**
	 * Whether a listener for the given type is registered.
	 * <p>
	 * This does not include the global listeners. If a check for the registration of global
	 * listeners is needed call this method with {@link PropertyObservable#GLOBAL_LISTENER_TYPE}.
	 * </p>
	 */
	public boolean has(EventType<?, ?, ?> type) {
		return hasListeners(type);
	}

}

