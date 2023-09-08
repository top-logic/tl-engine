/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;

/**
 * {@link AttachedPropertyListener} that reacts on the life-cycle of an {@link AbstractControlBase}
 * and registers or deregisters a {@link GenericPropertyListener} at a {@link PropertyObservable}
 * depending on the {@link AbstractControlBase#isAttached() attached} state of a control.
 * 
 * <p>
 * This can be used to connect a listener registration with the life-cycle of a
 * {@link AbstractControlBase}.
 * </p>
 * 
 * @see AbstractControlBase#ATTACHED_PROPERTY
 * @see AbstractControlBase#addListener(EventType, PropertyListener)
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AttachListener implements AttachedPropertyListener {

	/**
	 * The {@link PropertyObservable} to register {@link #_listener} to on an
	 * {@link AbstractControlBase#ATTACHED_PROPERTY attached event}.
	 */
	protected final PropertyObservable _observable;

	/**
	 * The {@link PropertyListener} to register on {@link #_observable} on an
	 * {@link AbstractControlBase#ATTACHED_PROPERTY attached event}.
	 */
	protected final PropertyListener _listener;

	/**
	 * {@link EventType} of {@link #_listener}.
	 */
	protected final EventType<PropertyListener, ?, ?> _eventType;

	/**
	 * Creates a new {@link AttachListener}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends PropertyListener> AttachListener(PropertyObservable observable, EventType<T, ?, ?> eventType,
			T listener) {
		_observable = observable;
		_eventType = (EventType<PropertyListener, ?, ?>) eventType;
		_listener = listener;
	}

	@Override
	public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
		if (newValue) {
			_observable.addListener(_eventType, _listener);
			updateObservedState(sender);
		} else {
			_observable.removeListener(_eventType, _listener);
		}
	}

	/**
	 * Updates the sender in reaction of adding listener to {@link #_observable}.
	 * 
	 * <p>
	 * The {@link AbstractControlBase} to which this listener is registered needs to bring its state
	 * in sync with the {@link #_observable}, because the control does not listening to the
	 * {@link #_observable} if it is not attached. Therefore after starting listening, the stae must
	 * be updated.
	 * </p>
	 * 
	 * @param sender
	 *        The sender this {@link AttachListener} is registered to.
	 */
	protected abstract void updateObservedState(AbstractControlBase sender);
}
