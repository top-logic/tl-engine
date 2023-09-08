
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;


/**
 * This class provides the functionality for sending classes to get a certain 
 * service and a channel to send on when being set at the bus. Additionally, the 
 * Channel object is called to send a message to subscribed receivers.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Sender {

    /** The nameSpace of the service the sender provides. */
    public static String SPACE = "space1";
        
    /** The name of the service the sender provides. */
    public static String NAME = "name1";

    /** The service to be supplied. */
    protected Service service;
    
    /** The channel to send on. */
    private Channel channel;

    public Sender () {
        this (SPACE, NAME);
    }

    public Sender (String aNameSpace, String aName) {
        this (new Service (aNameSpace, aName));
    }

    public Sender (Service aService) {
        this.service = aService;
    }

    /**
     * Delivers the senders´ channel.
     * 
     * @return  The channel to send on.
     */
    public Channel getChannel () {
        if (channel == null) {
            this.setSender ();
        }

        return channel;
    }
    
    /**
     * Delivers an instance of Service.
     * 
     * @return  The service provided by the sender.
     */
    public Service getService () {
       if (service == null) {
           service = new Service (SPACE, NAME);
       }

       return service;
    }

    /**
     * Sends a notification to each receiver in the receiverList who has 
     * registered for a certain service (or group of channels), when the
     * corresponding BusEvent has occurred.
     *
     * @param    aBusEvent    The BusEvent to be sent to subscribed receivers.
     */
    public void send (BusEvent aBusEvent) {
        this.getChannel ().send (aBusEvent);
    }

    /**
     * Calls on the bus to add the sender with the specified service to 
     * the channel. 
     *  
     */
    private void setSender () {
       this.channel = Bus.getInstance ().addSender (this, this.getService ());
    }
}
