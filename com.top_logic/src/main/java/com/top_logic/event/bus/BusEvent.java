
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.util.EventObject;


/**
 * BusEvent is instantiated by Channel with source (sender), corresponding 
 * service and aMessage which is understood by the Receiver objects listening 
 * on this channel or group of channels.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class BusEvent extends EventObject {

    /** The service of the sender which is the source of the event. */
    private Service service;
    
    /** The type of the message to be sent to Receiver objects listening. */
    private String type;
    
    /** The message to be sent to Receiver objects listening. */
    private Object message; 

    /** 
     * Constructs a prototypical BusEvent with source = Sender by calling the 
     * superclass´constructor.
     *
     * @param  aSender   The sender (source) on which the event initially 
     *                   occurred.
     * @param  aService  The corresponding service.
     * @param  aType     The type of the emssage to be sent.
     * @param  aMessage  The message to be sent to Receiver objects subscribed 
     *                   to the service(s).
     */  
    public BusEvent (Sender aSender, Service aService, String aType, 
                                                     Object aMessage) {
       super(aSender);
       this.setService(aService);
       this.setMessage (aType, aMessage);
    }

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  A String representation of this EventObject.
     */
    @Override
	public String toString () {
        return getClass ().getName () + "[source: " + source +
                                        ", service: " + service +
                                        ", message: " + message +
                                        "]";
    }

    /**
     * Returns the service provided by the source of the busEvent (sender).
     *
     * @return  service   The source´s service.
     */
    public Service getService () {
         return service;
    }
     
     
    /**
     * Sets the service provided by the source of the busEvent (sender).
     *
     * @param  aService   The sender´s service.
     */
    public void setService (Service aService) {
         this.service = aService;
    }
     
    
    /**
     * Sets the type and content of the message that is to be sent when a 
     * busEvent occurs.
     *
     * @param  aType    The type of the message corresponding to the busEvent.
     * @param  aMessage The message to be sent in the specified case.
     */
    public void setMessage (String aType, Object aMessage) {
        this.type = aType;
        this.message = aMessage;
    }
    
    /**
     * Returns the type of the message that is to be sent.
     *
     * @return  type   The message type corresponding to the busEvent.
     */
    public String getType () {
          return type;
    }

        
    
    /**
     * Returns the message that is to be sent when the busEvent occurrs.
     *
     * @return  message   The message corresponding to the busEvent.
     */
    public Object getMessage () {
          return message;
    }
  
      
    
}
