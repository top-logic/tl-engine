
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.top_logic.basic.Logger;


/**
 * Central gateway for establishing the communication between different other
 * gateways. This one will publish every send information to the connected
 * gateways. If there is no communication between them, this instance will
 * automatically test the connection every minute.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ServerGateway extends Gateway {

    /** The seconds to wait for the next ping. */
    private static final int WAIT_TIME = 59;

    /** Only instance of this class. */
    private static ServerGateway soleInstance;

    /** The time, the system has wait ,without sending a message. */
    int currentWait;

    /** Simple attribute for synchronization. */
	Object lock = new Object();

    /**
     * Constructs a ServerGateway and binds it to the RMI naming. Moreover the
     * ping thread will be started.
     *
     * @exception    RemoteException    If creation fails.
     */
    public ServerGateway () throws RemoteException {
        this.bind ();

        new PingThread ().start ();
    }

    /**
     * This method is used to exchange the messages between the different
     * gateways.
     * 
     * @param        aGateway           The sending gateway.
     * @param        anEvent            The send message.
     * @exception    RemoteException    If communication fails.
     */
    @Override
	public void receive (IGateway aGateway, 
                         BusEvent anEvent) throws RemoteException {
        if (DEBUG) {
            System.out.println (this.getClass ().getName () + ".receive()...");
        }

        new NotifyThread (aGateway, anEvent).start ();
    }

    /**
     * Appends the given gateway to the list of known gateways. Every new
     * message will be send to all gateways.
     *
     * @param        aGateway           The gateway to be appended.
     * @return       <code>true</code> if gateway has been registered.
     * @exception    RemoteException    If communication fails.
     */ 
    @Override
	public boolean addGateway (IGateway aGateway) throws RemoteException {
        Collection theList = this.getGateways ();

        if (DEBUG) {
            System.out.println (this.getClass ().getName () + 
                                ".addGateway(" + aGateway + ") called!");
        }

        if (!theList.contains (aGateway)) {
            boolean theResult = theList.add (aGateway);

            try {
                aGateway.addGateway (this);

                return (theResult);
            } 
            catch (RemoteException ex) {
                // System.err.println ("Unable to add this to gateway " + 
                //                     aGateway + ", reason is: " + ex);
                Logger.warn ("Unable to add this to gateway " + 
                                    aGateway, ex, this);

                return (false);
            }
        }
        else {
            return (true);
        }
    }

    /**
     * Removes the given gateway from the list of known gateways.
     *
     * @param        aGateway           The gateway to be removed.
     * @return       <code>true</code> if gateway has been removed from list.
     * @exception    RemoteException    If communication fails.
     */ 
    @Override
	public boolean removeGateway (IGateway aGateway) throws RemoteException {
        boolean theResult = this.getGateways ().remove (aGateway);

        if (DEBUG) {
            System.out.println (this.getClass ().getName () + 
                                ".removeGateway(" + aGateway + ") called!");
        }

        try {
            aGateway.removeGateway (this);
        } 
        catch (RemoteException ex) {
            // System.err.println ("Unable to remove this from gateway " + 
            //                     aGateway + ", reason is: " + ex);
            Logger.warn ("Unable to remove this from gateway " + 
                                aGateway, ex, this);
        }

        return (theResult);
    }

    /**
     * Returns the list of connected gateways. This method is synchronized
     * with the other ones, who ping the other gateways.
     *
     * @return       The collection of the names of the connected gateways.
     * @exception    RemoteException    If communication fails.
     */
    @Override
	public Collection getConnectedGateways () throws RemoteException {
        synchronized (this.lock) {
            Collection theColl = super.getConnectedGateways ();

            this.currentWait = 0;

            return (theColl);
        }
    }

    /**
     * Bind this instance to the local registry.
     */
    public void bind () {
        this.bind (GATEWAY_NAME);
    }

    /**
     * Doing nothing, because this is the server.
     *
     * @return    <code>false</code> because this is the server.
     */
    @Override
	protected boolean lookup () {
        return (false);
        
    }

    /**
     * Bind this instance to the local registry.
     *
     * @param    aName    The name to bind this instance.
     */
    protected void bind (String aName) {
        try {
            LocateRegistry.createRegistry (this.getRegistryPort ());

            Naming.rebind (aName, this);
        } 
        catch (Exception ex) {
            // System.err.println ("Unable to establish a server process for " +
            //                     "a gateway, reason was: " + ex);
            Logger.warn ("Unable to establish a server process for " +
                                "a gateway", ex, this);

            ex.printStackTrace ();
        }
    }

    private class PingThread extends Thread {

    
        public PingThread() {
            super ("ServerGateway$PingThread");
            setDaemon(true);
        }
    
        // Overwritten methods from Thread

        /**
         * Wait for one second and check, if the idle time is larger than the
         * maximum idle time. If this is the case, the thread will check every
         * connection.
         */
        @Override
		public void run () {
            while (true) {
                try {
                    sleep (1000);

                    synchronized (ServerGateway.this.lock) {
                        if (ServerGateway.this.currentWait > WAIT_TIME) {
                            this.log ("checking connections...");

                            ServerGateway.this.checkConnectedGateways ();
                            ServerGateway.this.currentWait = 0;

                            this.log ("finished checking connections!");
                        }
                        else {
                            ServerGateway.this.currentWait++;
                        }
                    }
                }
                catch (Exception ex) {
                    this.log (ex);
                }
            }
        }

        // Private methods

        /**
         * Log a message to std-out.
         *
         * @param    anObject    Describing the message to be logged.
         */
        private void log (Object anObject) {
            if (DEBUG) {
                System.out.println ("***** PingThread: " + anObject);
            }
        }
    }

    /**
     * Special thread for notifying the different listeners.
     *
     * @author    Michael Gänsler
     * @version   0.1
     */
    private class NotifyThread extends Thread {

        // Attributes

        /** The gateway sending the message. */
        private IGateway source;

        /** The send event containing the message. */
        private BusEvent event;

        // Constructors

        /**
         * Creates an instance of this class.
         *
         * @param    aGateway    The sending gateway.
         * @param    anEvent     The send event.
         */
        public NotifyThread (IGateway aGateway, BusEvent anEvent) {
            super ("ServerGateway$NotifyThread");
            setDaemon(true);
            this.source = aGateway;
            this.event  = anEvent;
        }

        // Overwritten methods from Thread

        /**
         * Iterates through the list of listeners and notifies them about the
         * changes. If one of them is not connected anymore, it will be removed
         * from the listeners list.
         */
        @Override
		public void run () {
            synchronized (ServerGateway.this.lock) {
                IGateway   theGate;
                Collection theList = ServerGateway.this.getGateways ();
                Collection theColl = new Vector ();
                Iterator   theIt   = theList.iterator ();

                while (theIt.hasNext ()) {
                    theGate = (IGateway) theIt.next ();

                    if (!theGate.equals (this.source)) {
                        try {
                            theGate.receive (this.source, this.event);
                        } 
                        catch (RemoteException ex) {
                            theColl.add (theGate);
                        }
                    }
                }

                for (theIt = theColl.iterator (); theIt.hasNext (); ) {
                    theGate = (IGateway) theIt.next ();

                    System.out.println ("Removing gateway: " + theGate);

                    theList.remove (theGate);
                }

                ServerGateway.this.currentWait = 0;
            }
        }
    }

    /**
     * Returns the sole instance of this class.
     * The instance is constructed if necessary (lazy initialization).
     * 
     * @return    The sole instance of ServerGateway.
     */
	public synchronized static IGateway getInstance() {
        if (soleInstance == null) {
            try {
                soleInstance = new ServerGateway ();
            } 
            catch (RemoteException ex) {
                ex.printStackTrace ();
            }
        }

        return soleInstance;
    }
}
