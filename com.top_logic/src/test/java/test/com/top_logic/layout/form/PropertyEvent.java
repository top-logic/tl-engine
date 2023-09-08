/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.GenericPropertyListener;


/**
 * A {@link PropertyEvent} informs about a value update of an {@link Object}'s property.
 * 
 * <p>
 * A {@link PropertyEvent} encapsulates the changes of a special {@link EventType} in one object.
 * </p>
 * 
 * @see GenericPropertyListener for details of the event delivery scheme.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PropertyEvent {

	private Object sender;
    
	private Object newValue;
	
	private Object oldValue;
	
	private final EventType<?, ?, ?> type;
	
	
	/**
	 * @see #getSender()
	 * @see #getType()
	 * @see #getOldValue()
	 * @see #getNewValue()
	 */
	public PropertyEvent(Object sender, EventType<?, ?, ?> type, Object oldValue, Object newValue) {
		this.sender = sender;
		this.type = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
		
	}
	
	/**
	 * Returns the type of this event. The returned value can be used to
	 * identify what to do.
	 */
	public EventType<?, ?, ?> getType() {
		return type;
	}

	/**
     * The {@link Object}, for which the
     *     {@link #getType() corresponding} property has changed.
     */
    public Object getSender() {
        return sender;
    }
    
	/**
	 * The new value of the {@link #getType() corresponding}
	 *     property.
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * The old value before the update of the
	 *     {@link #getType() corresponding} property.
	 */
	public Object getOldValue() {
		return oldValue;
	}

}
