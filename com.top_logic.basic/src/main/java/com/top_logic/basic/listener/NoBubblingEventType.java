/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

/**
 * {@link EventType} which lets the change never bubble.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class NoBubblingEventType<L extends PropertyListener, S, V> extends EventType<L, S, V> {

	/**
	 * Creates a new {@link NoBubblingEventType}.
	 * 
	 * @param name
	 *        see {@link EventType#EventType(String)}
	 */
	public NoBubblingEventType(String name) {
		super(name);
	}

	@Override
	public final Bubble dispatch(L listener, S sender, V oldValue, V newValue) {
		internalDispatch(listener, sender, oldValue, newValue);
		return Bubble.CANCEL_BUBBLE;
	}

	@Override
	public boolean canBubble() {
		return false;
	}

	/**
	 * Dispatches the property change to the concrete listener interface.
	 * 
	 * <p>
	 * The property change is not propagated to ancestors.
	 * </p>
	 * 
	 * @param listener
	 *        The concrete {@link PropertyListener} that handles the change.
	 * @param sender
	 *        The sender of the property change.
	 * @param oldValue
	 *        The old value of the property
	 * @param newValue
	 *        The new value of the property.
	 * 
	 * @see NoBubblingEventType#dispatch(PropertyListener, Object, Object, Object)
	 */
	protected abstract void internalDispatch(L listener, S sender, V oldValue, V newValue);

}

