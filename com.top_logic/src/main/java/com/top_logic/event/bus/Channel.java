
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.Computation;
import com.top_logic.util.TLContext;


/**
 * This class provides the main functionality for the setting of classes to 
 * send on or receive from a certain channel. Additionally, Channel objects 
 * instantiate BusEvent when a new sender is set and send messages to Receiver 
 * objects that listen on certain channel(s). 
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Channel {

    /** The channel´s sender. */
	private List<Sender> senderList;

    /** The channel´s receivers (collection). */
	private Collection<IReceiver> receiverList;

    /** The channel´s service (key). */
    private Service service;

    /** 
     * Creates an instance of Channel with a dedicated service, since a channel
     * should always provide a service.
     *
     * @param aService         The specified Service (key).
     */
    public Channel (Service aService) {
        this.service = aService;
    }

    /**
    * Returns a String representation of this EventObject.
    *
    * @return  A String representation of this EventObject.
    */
    @Override
	public String toString () {
        
        String sender   = senderList   == null ? "no" : senderList.size()   + " Sender(s)";
        String receiver = receiverList == null ? "no" : receiverList.size() + " Receiver(s)";
        
        return this.getClass () + " ["
            + "service: " + this.service + " " + sender + ' ' + receiver + ']';
    }

    /**
     * Returns an existing list of receivers or instanciates a new list of 
     * receivers as HashSet. 
     *
     * @return      The list of receivers.
     */
	public Collection<IReceiver> getReceiverList() {
        if (this.receiverList == null) {
			this.receiverList = new HashSet<>();
        }

        return this.receiverList;
    }

    /**
     *  Adds a receiver to the list.
     * 
     * @param aReceiver        The receiver who called on the bus.      
     */
    public void subscribe (IReceiver aReceiver) {
        this.getReceiverList ().add (aReceiver);     
    }

    /**
     *  Removes a receiver from the list.
     * 
     * @param aReceiver        The receiver who called on the bus.      
     */
    public void unsubscribe (IReceiver aReceiver) {
        this.getReceiverList ().remove (aReceiver);     
    }
    
    /**
     * Adds a sender to the channel.
     * 
     * @param aSender The Sender which called first on the bus.           
     */
    public void addSender (Sender aSender) {
        if (! this.getSenderList ().contains (aSender)) {
            this.getSenderList ().add (aSender);
        }    
    }            


    /**
     * Removes a sender from the channel.
     *
     * @param   aSender  The sender to remove.
     */
    public void removeSender (Sender aSender) {
        this.getSenderList ().remove (aSender);
    }
    
    /**
     * Check, if the given sender is attached to this channel.
     * 
     * @param    aSender    The sender to be looked up.
     * @return   <code>true</code>, if the given sender is contained in this channel
     */
    public boolean contains(Sender aSender) {
        return (this.getSenderList().contains(aSender));
    }

    /**
     * Sends a notification to each receiver in the receiverList 
     * who has registered for a certain service (or group of channels), 
     * when the corresponding busEvent has occurred.
     *
     * @param aBusEvent   The busEvent to be sent to subscribed receivers.
     */
    public void send (final BusEvent aBusEvent) {
		Iterator<IReceiver> theIt = this.getReceiverList().iterator();
        boolean   debug = Logger.isDebugEnabled (this);

        while (theIt.hasNext ()) {
			final IReceiver theRecv = theIt.next();

            if (debug) {
                Logger.debug ("Send " + aBusEvent + " to " + theRecv, this);
            }

			TLContext.inSystemContext(theRecv.getClass(), new Computation<Void>() {
            	@Override
				public Void run() {
                    try {
						theRecv.receive(aBusEvent);
                    } catch (Exception ex) {
		                Logger.error("Failed to send event '" + aBusEvent + "' to receiver '" + theRecv + "'.", ex, Channel.class);
		            }
            		return null;
            	}
            });
        }
    }

    /**
     * Delivers the service of the sender which is the source of the busEvent.
     *
     * @return  The service corresponding to the source of the busEvent.
     */
    public Service getService () {
        return service;
    }

    /**
     * Shows that this channel has neither Senders nore receivers.
     *
     * @return  true when sendesrs and recivers are bot null or empty.
     */
    public boolean isEmpty() {

        if (senderList != null 
         && senderList.size() > 0) {
            return false;   // We have senders
        }

        if (receiverList != null 
         && receiverList.size() > 0) {
            return false;   // We have Receivers
        }

        return true;    // all empty
    }

	protected List<Sender> getSenderList() {
        if (this.senderList == null) {
			this.senderList = new ArrayList<>();
        }

        return this.senderList;
    }

}
