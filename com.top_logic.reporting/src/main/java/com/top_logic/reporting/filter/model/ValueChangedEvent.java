/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.filter.model;

import com.top_logic.knowledge.wrap.Wrapper;

/**
 * A {@link ValueChangedEvent} informs all registered {@link FilterModelListener}s that
 * a value of the {@link FilterModel} has changed.
 * 
 * A {@link ValueChangedEvent} contains the old value and the new value. Additional it
 * contains a method {@link #getValueIdentifier()} that identifies the changed value. 
 * E.g. the model is a {@link Wrapper} and an attribute is changed then the value 
 * identifier could be the attribute name.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ValueChangedEvent implements FilterModelEvent {

	/**
	 * This event type indicates that a value of the model has changed. 
	 */
	public static final int EVENT_TYPE_VALUE_CHANGED = 0;
	
	/** See {@link #getValueIdentifier()}. */
	private Object valueIdentifier;
	/** See {@link #oldValue}. */
	private Object oldValue;
	/** See {@link #newValue}. */
	private Object newValue;
	
	/**
	 * Creates a new ValueChangedEvent.
	 * 
	 * @param valueIdentifier
	 *            A value identifier (e.g. attribute name). Null is permitted.
	 * @param oldValue
	 *            A old value.
	 * @param newValue
	 *            A new value.
	 */
	public ValueChangedEvent(Object valueIdentifier, Object oldValue, Object newValue) {
		super();
		
		this.valueIdentifier = valueIdentifier;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public int getEventType() {
		return EVENT_TYPE_VALUE_CHANGED;
	}

	/**
	 * This method returns the valueIdentifier or <code>null</code>.
	 * 
	 * E.g. the model is a {@link Wrapper} and an attribute is changed then the
	 * value identifier could be the attribute name.
	 * 
	 * @return Returns the valueIdentifier or <code>null</code>.
	 */
	public Object getValueIdentifier() {
		return this.valueIdentifier;
	}

	/**
	 * This method returns the oldValue.
	 * 
	 * @return Returns the oldValue.
	 */
	public Object getOldValue() {
		return this.oldValue;
	}

	/**
	 * This method returns the newValue.
	 * 
	 * @return Returns the newValue.
	 */
	public Object getNewValue() {
		return this.newValue;
	}
	
}
