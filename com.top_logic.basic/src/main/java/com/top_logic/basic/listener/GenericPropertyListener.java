/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import com.top_logic.basic.listener.EventType.Bubble;

/**
 * {@link GenericPropertyListener} are {@link PropertyListener} react on each event type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GenericPropertyListener extends PropertyListener {

	/** {@link GenericPropertyListener} that ignores all events. */
	GenericPropertyListener IGNORE_EVENTS = new GenericPropertyListener() {

		@Override
		public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue) {
			// ignore all events
			return Bubble.BUBBLE;
		}

	};

	/**
	 * Handles the given property change.
	 * 
	 * @param type
	 *        The type of the property that changed.
	 * @param sender
	 *        The sender whose property changed.
	 * @param oldValue
	 *        The old property value.
	 * @param newValue
	 *        The new property value.
	 * @return Whether the event shall bubble.
	 */
	Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue);

}
