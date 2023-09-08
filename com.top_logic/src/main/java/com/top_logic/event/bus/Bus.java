/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;

/**
 * This class provides the main functionality for other classes to send on or 
 * receive from the bus´ channels.  
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Bus extends ManagedClass {

    /** The Hashtable for the bus´ channels (key = Service, value: channel). */
	private Map<Service, Channel> map;

    /**
     * The Hashtable for groups of channels and collection of receivers
     * (key = Service (.. *), value = collectionOfReceivers).
     */
	private Map<Service, Collection<IReceiver>> starMap;

	/**the namespace of some channels.*/
	public static final String CHANGES        =   "changes";

	/** the name of a channel.*/
	public static final String DOCUMENT       =   "document";

	/** the name of a channel.*/
	public static final String USER           =   "user";

	/** the name of a channel.*/
	public static final String WEBCONTENT     =   "webcontent";

	/** the name of a channel.*/
	public static final String CACHE          =   "cache";

    /**
     * The Default constructor is declared private to prevent that more than 
     * one instance of Bus is created.
     */
    Bus () {
        super();
    }

    /**
     *  Returns the unique instance of the class.
     *  Every class can now get this instance through the following message:
     *  Bus theBus = Bus.getInstance ();
     *
     * @return             The unique instance of the class.
     */
    public static Bus getInstance () {
    	return Module.INSTANCE.getImplementationInstance();
    }

    /**
     * Adds receivers of a specified channel or group of channels to the bus.
     *
     * @param  aService  The service required by the receiver.
     * @param  aReceiver The receiver calling on the bus.
     */
    public void subscribe (IReceiver aReceiver, Service aService) {

        // Checks if the receiver wants to listen to a group of channels.
        if (this.containsStar (aService)) {
            // Delivers the collection of receivers to the specified key
            // (service with *) and adds the receiver to it.
            this.addtoStarMap (aService, aReceiver);
            // Delivers the channels with services matching aService (the 
            // service with *) and adds the receivers as WildCardListeners.
            this.addReceiverToMatchingChannels (aService, aReceiver); 

        }
        // Delivers the channel of the one specified service and adds the
        // receiver to it.
        else 
            this.getChannel (aService).subscribe (aReceiver);

    }

    /**
     * Removes receivers of a specified channel or group of channels from the bus.
     *
     * @param  aService  The service required by the receiver.
     * @param  aReceiver The receiver calling on the bus.
     */
    public void unsubscribe (IReceiver aReceiver, Service aService) {
        // Checks if the receiver can listen to a group of channels.
        if (this.containsStar (aService)) {
            // Removes the receiver from the collection of receivers for the specified key
            // (service with *).
            this.removeFromStarMap (aService, aReceiver);
            // Removes the receivers as WildCardListeners from the channels with 
            // services matching aService (the service with *).
            this.removeReceiverFromMatchingChannels (aService, aReceiver); 
        }
        // Delivers the channel of the one specified service and adds the
        // receiver to it.
        else 
            this.getChannel (aService).unsubscribe (aReceiver);

    }

    /**
     * Sets the sender with its specified service to a channel at the bus.
     * The sender has exactly one service and thus, gets one channel to send on.
     *  
     * @param    aSender                  The sender calling on the bus.
     * @return                            The channel object the sender is 
     *                                    to send on.
     * @throws   IllegalStateException    If the sender of this
     *                                    channel has already been set.
     */
    public Channel addSender (Sender aSender) throws IllegalStateException {
        return (this.addSender (aSender, aSender.getService ()));
    }

    /**
     * Remove the sender from the channel specified by its service.
     *  
     * Nothing will hapen in case the Sender was never added.
     * 
     * @param    aSender                  The sender calling on the bus.
     */
    public void removeSender (Sender aSender) {
        this.removeSender (aSender, aSender.getService ());
    }

    /**
     * Sets the sender with its specified service to a channel at the bus.
     * The sender has exactly one service and thus, gets one channel to send on.
     *  
     * @param    aSender                  The sender calling on the bus.
     * @param    aService                 The service provided by the sender.
     * @return                            The channel object the sender is 
     *                                    to send on.
     * @throws   IllegalStateException    If the sender of this
     *                                    channel has already been set.
     */
    public Channel addSender (Sender aSender, Service aService) 
                                                throws IllegalStateException {
        Channel theChannel = this.getChannel (aService);

        theChannel.addSender (aSender);

        return (theChannel);
    }

    /**
     * Remove the sender from the given channel.
     * 
     * Nothing will hapen in case the Sender was never added.
     *  
     * @param    aSender                  The sender calling on the bus.
     * @param    aService                 The service provided by the sender.
     * 
     */
    public void removeSender (Sender aSender, Service aService) {
		Map<Service, Channel> channelMap = this.getMap();
		Channel theChannel = channelMap.get(aService);

        if (theChannel != null) { // No channel to remove the Sender ?
            theChannel.removeSender(aSender);
            if (theChannel.isEmpty()) {
               channelMap.remove(aService); // Release this Channel
            }
        }
    }

    /**
     *  Returns an existing map or creates a new instance of HashMap,
     *  if no map exists.
     *
     * @return   The map containing the service-channel pairs.
     */
	public Map<Service, Channel> getMap() {
        if (map == null) {
			map = new HashMap<>();
        }

        return map;
    }

    /**
     * Overwrites the get(Object key) method in interface Map (cast). Returns
     * the data object (channel) of the object key (service) or null, if the
     * key was not found in the map. In the latter case, a new instance of
     * Channel(Service) is created and added to the map.
     *
     * @return   The channel with the specified service.
     */
    private Channel getChannel (Service aService) {
        // casts the return value from Object to Channel
		Map<Service, Channel> channelMap = this.getMap();
		Channel theChannel = channelMap.get(aService);
        
        if (theChannel == null) {
            theChannel = this.createChannel (aService);
            channelMap.put (aService, theChannel);
        }

        return theChannel;
    }     

    /**
     * Returns an existing starMap or creates a new HashMap,
     * if no map exists.
     *
     * @return    The map containing the service( .. *) - collectionOfReceivers
     *            pairs.
     */
	public Map<Service, Collection<IReceiver>> getStarMap() {
        if (starMap == null) {
			starMap = new HashMap<>();
        }

        return starMap;
    }

    /**
     * Checks if the service required by the receiver contains a star as
     * either name or nameSpace, indicating that the receiver wants to listen
     * to a group of channels contained in the starMap. 
     *
     * @return boolean
     */
    private boolean containsStar (Service aService) {
        return (aService.getNameSpace().equals (Service.WILDCARD)) ||
               (aService.getName()     .equals (Service.WILDCARD));
    }

    /**
     * Adds a Receiver of a service that matches a key of the starmap (that
     * is, services connected to groups of channels) to the key´s value
     * (collection of receivers).
     *  
     * @param   aService     A service from the starmap.
     * @param   aReceiver    The receiver that is to be added to the starmap.
     * @return  result of the add operation to the collection of receivers of the specified key.
     */
    private boolean addtoStarMap (Service aService, IReceiver aReceiver) {
        // casts the return value from Object to Collection
		Collection<IReceiver> theRecvCollection = this.getStarMap().
        get (aService);

        if (theRecvCollection == null) {
			theRecvCollection = new HashSet<>();
            this.getStarMap ().put (aService, theRecvCollection);
        }

        return theRecvCollection.add (aReceiver);
    }

    /**
     * Removes a Receiver of a service that matches a key of the starmap (that
     * is, services connected to groups of channels) from the key´s value
     * (collection of receivers).
     *  
     * @param   aService     A service from the starmap.
     * @param   aReceiver    The receiver that is to be added to the starmap.
     * @return  result of the remove operation from the collection of receivers of the specified key.
     */
    private boolean removeFromStarMap (Service aService, IReceiver aReceiver) {
        // casts the return value from Object to Collection
		Collection<IReceiver> theRecvCollection = this.getStarMap().
        get (aService);

        if (theRecvCollection == null) {//nothing changes:
            return false;
        }

        return theRecvCollection.remove (aReceiver);
    }

    /**
     * Creates a new Channel with a dedicated service. Asserts that the service
     * has no star (as each channel must have one specified service) and adds
     * WildCardListeners to the channel.
     *
     * @param  aService    The service of the sender on this channel.
     * @return theChannel  The new channel.
     */
    private Channel createChannel (Service aService) {
        if (this.containsStar (aService)) {
            throw new IllegalStateException ("Each channel must be created " +
                "with a specified service");
        }
        Channel theChannel = new Channel (aService);
        this.addWildCardListenersToChannel (theChannel);

        return theChannel;
    }

    /**
     * Sends an iterator across the keySet (services) of the StarMap and adds 
     * WildcardListeners (receivers) to each matching element.
     *
     * @param aChannel  The channel that has been created.
     */
    private void addWildCardListenersToChannel (Channel aChannel) {
		Iterator<Service> theIt = this.getStarMap().keySet().iterator();

        while (theIt.hasNext ()) {
            this.addWildCardListenersToChannel (aChannel,
				theIt.next());
        }
    }
    
    /**
     * Checks if the channel´s service matches a key in the starMap. If true, 
     * the corresponding collection of receivers is delivered from the starMap 
     * and added to the channel´s receiverList.
     * 
     * @param  aChannel  The channel that has been created.
     * @param  aService  The service of the channel.
     */
    private void addWildCardListenersToChannel (Channel aChannel,
        Service aService) {
        if (this.compareServices (aChannel.getService (), aService)) {
			Collection<IReceiver> theColl = this.getStarMap().get(aService);
            aChannel.getReceiverList ().addAll (theColl);
        }
    }
    
    /**
     * Checks if the service a receiver asks for (aSource) is equal to a key in
     * the starMap (aDest) with (a1, *).equals (a1,b1) !(a1, b1).equals (a1, *).
     *
     * @param  aSource  The service asked for by a receiver.
     * @param  aDest    A service in the keySet of StarMap.
     * @return true, if aSource contains two stars and if aSource matches aDest
     *               in either namespace or name or both. 
     */
    private boolean compareServices (Service aSource, Service aDest) {
        return this.compareOne (aSource.getNameSpace (), aDest.getNameSpace ()) &&
               this.compareOne (aSource.getName (), aDest.getName ());
    }  

    private boolean compareOne (String aSource, String aDest) {
        return (aSource.equals (aDest) || Service.WILDCARD.equals (aDest));
    }

    /**
     * Delivers the channels with services matching aService (the service 
     * with *) and adds the receivers as WildCardListeners.  
     * 
     * @param aReceiver  The receiver who called on the bus to listen to a
     *                   group of channels.
     * @param aService   The service the receiver asked for.   
     */
    private void addReceiverToMatchingChannels (Service aService,
        IReceiver aReceiver) {
        // Sends an iterator across the map´s data elements.                                           
		Iterator<Channel> theIt = this.getMap().values().iterator();
        // Adds the receivers of certain services (key) to their channels
        // (value).
        while (theIt.hasNext ()) {
            this.addReceiverToMatchingChannels (aService, aReceiver,
				theIt.next());
        } 

    }

    /**
     * Checks if the service the receiver asks for matches keys in the map.
     * If true, the corresponding data objects (channel) are returned from
     * the map and the receiver added to each.
     *
     * @param  aService  The service asked for by the receiver.
     * @param  aReceiver The receiver calling on the bus.
     * @param  aChannel  The channel the receiver is to be added to.
     */
    private void addReceiverToMatchingChannels (Service aService, IReceiver 
        aReceiver, Channel aChannel) {
        // Checks if the service asked for matches the service of the 
        // channel from the map.
        if (this.compareServices (aChannel.getService (), aService)) {
            aChannel.subscribe (aReceiver);
        }

    }

    /**
     * Delivers the channels with services matching aService (the service 
     * with *) and removes the receivers as WildCardListeners.  
     * 
     * @param aReceiver  The receiver who called on the bus to listen to a
     *                   group of channels.
     * @param aService   The service the receiver asked for.   
     */
    private void removeReceiverFromMatchingChannels (Service aService,
        IReceiver aReceiver) {
        // Sends an iterator across the map´s data elements.                                           
		Iterator<Channel> theIt = this.getMap().values().iterator();
        // Removes the receivers of certain services (key) to their channels
        // (value).
        while (theIt.hasNext ()) {
            this.removeReceiverFromMatchingChannels (aService, aReceiver,
				theIt.next());
        } 

    }

    /**
     * Checks if the service the receiver asks for matches keys in the map.
     * If true, the corresponding data objects (channel) are returned from
     * the map and the receiver removed from each.
     *
     * @param  aService  The service asked for by the receiver.
     * @param  aReceiver The receiver calling on the bus.
     * @param  aChannel  The channel the receiver is to be added to.
     */
    private void removeReceiverFromMatchingChannels (Service aService, 
                                                     IReceiver aReceiver, 
                                                     Channel aChannel) {
        // Checks if the service asked for matches the service of the 
        // channel from the map.
        if (this.compareServices (aChannel.getService (), aService)) {
            aChannel.unsubscribe (aReceiver);
        }
    } 

    public static final class Module extends BasicRuntimeModule<Bus> {

		public static final Module INSTANCE = new Module();

		private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES 
			= NO_DEPENDENCIES;

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return DEPENDENCIES;
		}

		@Override
		public Class<Bus> getImplementation() {
			return Bus.class;
		}

		@Override
		protected Bus newImplementationInstance() throws ModuleException {
			return new Bus();
		}
	}

}
