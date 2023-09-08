
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.rmi.Remote;
import java.rmi.RemoteException;


/** 
 * This interface describes the main functionality of a receiver who calls
 * on the bus to subscribe to certain services (channels) he wants to listen to. 
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */ 
public interface IReceiver extends Remote {

    /**
     * This method is called by Receiver objects to be connected to specified 
     * services (channels) by the bus. It has to be overloaded to allow for 
     * subscription to different groups of channels.
     * 
     * @param  aBusEvent  The BusEvent object that gives info about the event
     *                    and its source.
     */
    public void receive (BusEvent aBusEvent) throws RemoteException;

    /**
     *  Calls on the bus to add this receiver to listen to the specified
     *  service.
     */  
    public void subscribe ();

    /**
     *  Calls on the bus to stop this receiver listening to the specified
     *  service.
     */  
    public void unsubscribe ();

}
