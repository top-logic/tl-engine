/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyListener;

/**
 * The class {@link InvalidationListener} can be used to be informed when an object becomes invalid.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InvalidationListener extends PropertyListener {

	/**
	 * {@link EventType} used to notify about invalidation of some object.
	 * 
	 * @see InvalidationListener
	 */
	EventType<InvalidationListener, Object, Boolean> INVALIDATION_PROPERTY =
		new NoBubblingEventType<>("invalidated") {

			@Override
			protected void internalDispatch(InvalidationListener listener, Object sender, Boolean oldValue,
					Boolean newValue) {
				listener.notifyInvalid(sender);
			}
		};

	/**
	 * This method informs that the given object is invalid.
	 */
	void notifyInvalid(Object invalidObject);

}
