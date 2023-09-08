
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;


/**
 * Gateway for exchanging data between two VMs. The gateway can be configured
 * to subscribe to different channels. All messages send over this channels
 * will then be send via RMI to the other side of that gateway.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface IGateway extends Remote {

    /** The name of the service name for this interface. */
    public static final String GATEWAY_NAME = "BusGateway";

    /**
     * This method is used to exchange the messages between the different
     * gateways.
     * 
     * @param        aGateway           The sending gateway.
     * @param        anEvent            The send message.
     * @exception    RemoteException    If communication fails.
     */
    public void receive (IGateway aGateway, BusEvent anEvent) 
                                                        throws RemoteException;

    /**
     * Appends the given gateway to the list of known gateways. Every new
     * message will be send to all gateways.
     *
     * @param        aGateway           The gateway to be appended.
     * @return       <code>true</code> if gateway has been registered.
     * @exception    RemoteException    If communication fails.
     */ 
    public boolean addGateway (IGateway aGateway) throws RemoteException;

    /**
     * Removes the given gateway from the list of known gateways.
     *
     * @param        aGateway           The gateway to be removed.
     * @return       <code>true</code> if gateway has been removed from list.
     * @exception    RemoteException    If communication fails.
     */ 
    public boolean removeGateway (IGateway aGateway) throws RemoteException;

    /**
	 * Returns the name of the system, this instance is running on. This will be used for testing,
	 * which clients are connected to the server process and which had died in the last time.
	 *
	 * @return The name of the system.
	 * @exception RemoteException
	 *            If communication fails.
	 * @see ServerGateway
	 */
    public String getHostName () throws RemoteException;

    /**
     * Returns the list of connected gateways.
     *
     * @return       The collection of the names of the connected gateways.
     * @exception    RemoteException    If communication fails.
     */
    public Collection getConnectedGateways () throws RemoteException;
}
