/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.bus;

import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Accessor;

/**
 * The MonitorEventAccessor allows access to members of a MonitorEvent.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MonitorEventAccessor implements Accessor {

    public static final String PROPERTY_DATE           = "date";
    public static final String PROPERTY_SENDER         = "sender";
    public static final String PROPERTY_MESSAGE_OBJECT = "message";
    public static final String PROPERTY_SOURCE_OBJECT  = "source";
    public static final String PROPERTY_USER           = "user";
    public static final String PROPERTY_TYPE           = "type";
    public static final String PROPERTY_CHANNEL        = "channel";
    public static final String PROPERTY_SERVICE        = "service";
    
    /**
     * @see com.top_logic.layout.Accessor#getValue(java.lang.Object, java.lang.String)
     */
    @Override
	public Object getValue(Object aObject, String aProperty) {
        
        if (! (aObject instanceof MonitorEvent)) {
            return null;
        }
        
        MonitorEvent theEvent = (MonitorEvent) aObject; 
        
        if (PROPERTY_DATE.equals(aProperty)) {
            return theEvent.getDate();
        } 
        else if (PROPERTY_SENDER.equals(aProperty)) {
            return theEvent.getSource();            
        }
        else if (PROPERTY_MESSAGE_OBJECT.equals(aProperty)) {
            return theEvent.getMessage();          
        }
        else if (PROPERTY_SOURCE_OBJECT.equals(aProperty)) {
            return theEvent.getSourceObject();         
        }
        else if (PROPERTY_USER.equals(aProperty)) {
			Person user = theEvent.getUser();
			if (user != null) {
				return user;
			} else {
				return null;
			}
        }
        else if (PROPERTY_TYPE.equals(aProperty)) {
            return theEvent.getType();
        }
        else if (PROPERTY_CHANNEL.equals(aProperty)) {
            return ((Sender) theEvent.getSource()).getChannel();
        }
        else if (PROPERTY_SERVICE.equals(aProperty)) {
            return theEvent.getService();
        }
        return null;
    }

    @Override
	public void setValue(Object aObject, String aProperty, Object aValue) {
		// Ignore.
    }

}

