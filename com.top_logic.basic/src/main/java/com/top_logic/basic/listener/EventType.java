/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

/**
 * The type of an property event.
 * 
 * <p>
 * This type translates the property change to a change of the concrete type.
 * </p>
 * 
 * @param <L>
 *        The type of the concrete listener interface for this type. It is used to find the concrete
 *        listener method to dispatch to.
 * @param <S>
 *        Type of the sender whose property has changed.
 * @param <V>
 *        Type of the value of the property that has changed.
 * 
 * @see PropertyListener
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class EventType<L extends PropertyListener, S, V> {

	/**
	 * Whether the event should "bubble", i.e. whether this event should also be send to listener of
	 * a parent. What exactly a "parent" is, if there is one, depends on the concrete
	 * {@link PropertyObservable}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public enum Bubble {
		/**
		 * Return type to describe that the event is potentially relevant for a ancestor of the
		 * {@link PropertyObservable}.
		 */
		BUBBLE,
		/**
		 * Return type to describe that the event must not be propagated to the ancestors of the
		 * {@link PropertyObservable}.
		 */
		CANCEL_BUBBLE,

		;

	}

	/**
	 * @see #name()
	 */
	private final String _name;

	/**
	 * Creates a new {@link EventType}.
	 * 
	 * @param name
	 *        Name of the event type. The name is not really relevant, it makes debugging easier.
	 */
	public EventType(String name) {
		_name = name;
	}

	/**
	 * Dispatches the property change to the concrete listener interface.
	 * 
	 * @param listener
	 *        The concrete {@link PropertyListener} that handles the change.
	 * @param sender
	 *        The sender of the property change.
	 * @param oldValue
	 *        The old value of the property
	 * @param newValue
	 *        The new value of the property.
	 * @return Whether the event should also be delivered to the ancestors of the
	 *         {@link PropertyObservable}.
	 */
	public abstract Bubble dispatch(L listener, S sender, V oldValue, V newValue);
	
	/**
	 * Internal method to delegate event to the given listener.
	 * 
	 * @param eventType
	 *        Actual event type. Almost ever this.
	 * @param listener
	 *        The triggered listener.
	 * @param sender
	 *        The sender.
	 * @param oldValue
	 *        Old value.
	 * @param newValue
	 *        New value.
	 * @return Whether event must bubble.
	 */
	Bubble delegate(EventType<?, ?, ?> eventType, L listener, S sender, V oldValue, V newValue) {
		return dispatch(listener, sender, oldValue, newValue);
	}

	/**
	 * Name of this {@link EventType}.
	 */
	public String name() {
		return _name;
	}

	/**
	 * Whether this event supports bubbling.
	 * 
	 * <p>
	 * Some events does not support bubbling in general, e.g. because there is no structure in which
	 * it can bubble. If the listeners for an {@link EventType} which can not bubble are informed,
	 * the event must not bubble and the return value of the notification must be ignored.
	 * </p>
	 */
	public boolean canBubble() {
		return true;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		return super.equals(obj);
	}

}

