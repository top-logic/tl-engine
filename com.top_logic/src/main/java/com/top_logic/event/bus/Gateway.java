
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;


/**
 * Gateway for exchanging data between two VMs. The gateway can be configured
 * to subscribe to different channels. All messages send over this channels
 * will then be send via RMI to the other side of that gateway.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Gateway extends UnicastRemoteObject implements IGateway {

    /** Flag for debug output. */
    protected static final boolean DEBUG = false;

    /** The only instance of this class. */
    private static Gateway singleton;

    /** Returns the list of know gateways. */
    private Collection<IGateway> gateways;

    /** Returns the map of known senders. */
    private Map<Service,Sender> senderMap;

	/**
	 * Configuration for {@link GatewayReceiver}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractReceiver.Config {
		/**
		 * Namespace for the service.
		 */
		@Override
		@StringDefault(Service.WILDCARD)
		String getServiceNamespace();

		/**
		 * Name of the service.
		 */
		@Override
		@StringDefault(Service.WILDCARD)
		String getServiceName();
	}

    /**
     * Constructs a Gateway.
     * 
     * @exception    RemoteException    If communication fails.
     */
    public Gateway () throws RemoteException {
        super ();

        if (DEBUG) {
            System.out.println ("initializing gateway...");
        }

        this.lookup ();

        if (DEBUG) {
            System.out.println ("Gateway initialized!");
        }
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
        Sender theSender = this.getSender (anEvent.getService ());

        if (DEBUG) {
            System.out.println (this.getClass ().getName () + ".receive(" +
                                aGateway + ", " + anEvent + ") called!");
        }

        if (theSender != null) {
            theSender.send (new BusEvent (theSender, 
                                          anEvent.getService (),
                                          anEvent.getType (),
                                          anEvent.getMessage ()));
        }
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
        Collection<IGateway> theList = this.getGateways();

        if (DEBUG) {
            System.out.println (this.getClass ().getName () + ".addGateway(" + aGateway + ") called!");
        }

        if (!theList.contains (aGateway)) {
            return theList.add(aGateway);
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
        if (DEBUG) {
            System.out.println (this.getClass ().getName () + 
                                ".removeGateway(" + aGateway + ") called!");
        }

        return this.getGateways ().remove (aGateway);
    }

    /**
     * Returns the name of the system, this instance is running on. This will
     * be used for testing, which clients are connected to the server process
     * and which had died in the last time.
     *
     * @return    The name of the system.
     * @see       ServerGateway
     */
    @Override
	public String getHostName () throws RemoteException {
        try {
            InetAddress theAddress = InetAddress.getLocalHost ();

            return theAddress.getHostName () +
                   " [" + theAddress.getHostAddress () + "]";
        }
        catch (UnknownHostException ex) {
            return "unknown";
        }
    }

    /**
     * Returns the a list of hostnames of connected gateways.
     *
     * @return       The collection of the names of the connected gateways.
     * @exception    RemoteException    If communication fails.
     */
    @Override
	public Collection getConnectedGateways () throws RemoteException {
        IGateway             theGate;
        Collection<String>   theGateways = new ArrayList<>();
        Collection<IGateway> theList     = this.getGateways ();

        for (Iterator theIt = theList.iterator (); theIt.hasNext (); ) {
            theGate = (IGateway) theIt.next();

            try {
                theGateways.add (theGate.getHostName ());
            } 
            catch (RemoteException ex) {
                // System.err.println ("unable to reach: " + theGate + 
                //                     ", reason is: " + ex);
                Logger.warn ("unable to reach: " + theGate , ex, this);

                theIt.remove();
            }
        }

        return (theGateways);
    }
    
	/**
	  * Check the list of of connected gateways and remove unreachable ones.
	  */
	protected void checkConnectedGateways() {

		Collection theList     = this.getGateways();
		Iterator   theIt       = theList.iterator();
 
		// Get names of all known gateways.
		while (theIt.hasNext()) {
            IGateway theGate = (IGateway) theIt.next();

			try {
				theGate.getHostName();
			} catch (RemoteException ex) {
				// System.err.println ("unable to reach: " + theGate + 
				//                     ", reason is: " + ex);
				Logger.warn("unable to reach: " + theGate, ex, this);

				theIt.remove();
			}
		}
	}

    /**
     * Delivers the description of the instance of this Class.
     * 
     * @return    The description for debugging.
     */
    @Override
	public String toString () {
        return this.getClass ().getName () + " ["
                         + "gateways: " + this.gateways
                         + ", senderMap: " + this.senderMap
                         + "]";
    }

    /**
     * Send the given event to all known gateways. If a gateway is dead, it
     * will be removed from the list of known gateways afterwards. This has
     * to be done in that was, because you cannot delete an object from a
     * collection, which has an iterator.
     *
     * @param    anEvent    The event to be send.
     */
    public void send (BusEvent anEvent) {
        IGateway             theGate;
        Collection<IGateway> theOld  = new Vector<>();
        Collection<IGateway> theList = this.getGateways();

        for (Iterator theIt = theList.iterator (); theIt.hasNext ();) {
            theGate = (IGateway) theIt.next();

            try {
                theGate.receive (this, anEvent);
            } 
            catch (RemoteException ex) {
                // System.err.println ("unable to reach: " + theGate + 
                //                     ", reason is: " + ex);
                Logger.warn ("unable to reach: " + theGate, ex, this);

                theOld.add (theGate);
            }
        }

        // Remove the dead gateways.
        for (Iterator theIt = theOld.iterator (); theIt.hasNext (); ) {
            theGate = (IGateway) theIt.next ();

            theList.remove (theGate);

            System.out.println ("Removed gateway: " + theGate);
        }
    }

    /**
     * Connect this gateway to another gateway specified by the given URL.
     * This connection will be established to exchange bus events with the
     * remote VM defined by the URL of the remote Gateway.
     *
     * @param    anURL    The URL for the remote Gateway.
     * @return   The remote reference to the Server gateway.
     */
    public IGateway lookup (String anURL) {
        try {
            return (IGateway) Naming.lookup (anURL);
        } 
        catch (Exception ex) {
            // System.err.println ("unable to establish connection to gateway \"" +
            //                     anURL + "\", reason is: " + ex);
            Logger.warn ("unable to establish connection to gateway \"" +
                                anURL + "\"", ex, this);
        }

        return (null);
    }

    /**
     * Look up the server gateway and append this instance as listener to that
     * remote reference.
     *
     * @return    <code>true</code>, if appending succeeded.
     */
    protected boolean lookup () {
        IGateway theGate = this.lookup (GATEWAY_NAME);

        if (theGate != null) {
            try {
                return theGate.addGateway (this);
            } 
            catch (RemoteException ex) {
                // System.err.println ("unable to append to gateway \"" +
                //                     theGate + "\", reason is: " + ex);
                Logger.warn ("unable to append to gateway \"" +
                                    theGate + "\"", ex, this);
            }
        }

        return (false);
    }

    /**
     * Returns the sender for the given service.
     *
     * @param    aService    The service describing the sender.
     * @return   The sender for the given service.
     */
    protected Sender getSender (Service aService) {
        Map<Service,Sender> theMap = this.getSenderMap();

        if (!theMap.containsKey (aService)) {
            theMap.put(aService, new Sender (aService));
        }

        return theMap.get (aService);
    }

    /**
     * Returns the map of all senders classified by their service.
     *
     * @return    The map of supported senders.
     */
    protected Map<Service,Sender> getSenderMap () {
        if (this.senderMap == null) {
            this.senderMap = new HashMap<>();
        }

        return (this.senderMap);
    }

    /**
     * Returns the list of all known gateways.
     *
     * @return    The list of known gateways.
     */
    protected Collection<IGateway> getGateways () {
        if (this.gateways == null) {
            this.gateways = new Vector<>();
        }

        return (this.gateways);
    }

    protected int getRegistryPort () {
        return (Registry.REGISTRY_PORT);
    }

    /**
     * Receiver for events from this machine. All received messages will be
     * published to all known gateways. They will then send the messages to
     * their local application bus.
     *
     * @author    Michael Gänsler
     * @version   0.1
     */
    private class GatewayReceiver extends AbstractReceiver {

        // Constructors

		public GatewayReceiver(InstantiationContext context, Config config) {
			super(context, config);
        }

        // Overwritten methods from Receiver

        @Override
		public void receive (BusEvent anEvent) {
            Sender theSender = Gateway.this.getSender (anEvent.getService ());

            if (theSender != anEvent.getSource ()) {
                Gateway.this.send (anEvent);
            }
        }
    }

    /**
     * Returns the only instance of this class.
     *
     * @return    The only instance of this class.
     */
    public static IGateway getInstance () {
        if (Gateway.singleton == null) {
            try {
                Gateway.singleton = new Gateway ();
            }
            catch (RemoteException ex) {
                ex.printStackTrace ();
            }
        }

        return (Gateway.singleton);
    }
}
