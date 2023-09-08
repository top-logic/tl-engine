/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.rmi.RemoteException;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.event.bus.AbstractReceiver;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.event.bus.Service;

/**
 * The EventMonitor listens on the Bus for every event and buffers the last events.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class EventMonitor extends AbstractReceiver {

    /** default buffer size */
    private static final int DEFAULT_BUFFER_SIZE = 1000;

    /** buffer for all received events */
    private EventBuffer events;

    /** count all received events, not only the buffered */
    private long allEventsCount;

	/**
	 * Configuration for {@link EventMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractReceiver.Config {
		/**
		 * The buffer for all received events.
		 */
		@IntDefault(DEFAULT_BUFFER_SIZE)
		int getBufferSize();
		
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
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link EventMonitor}.
	 */
	public EventMonitor(InstantiationContext context, Config config) {
		super(context, config);

		int theSize = config.getBufferSize();
		events = new EventBuffer(theSize);
		subscribe();
	}

    /**
     * Singleton instance
     */
    public static EventMonitor getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
     * @see com.top_logic.event.bus.AbstractReceiver#receive(com.top_logic.event.bus.BusEvent)
     */
    @Override
	public void receive(BusEvent aBusEvent) throws RemoteException {
        events.addEvent(aBusEvent);
        this.allEventsCount++;
    }

    /**
     * This method returns all buffered events
     * 
     * @return a list of events
     */
    public List getEvents() {
        return events.getAllEvents();
    }
    
    
    /**
     * Get the count of all received events since startup 
     * 
     * @return number of all received events
     */
    public long getCountAllEvents() {
        return this.allEventsCount;
    }
    
	@Override
	protected void shutDown() {
		unsubscribe();
		events = null;
	}
    
	/**
	 * {@link BasicRuntimeModule} for {@link EventMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
    public static final class Module extends AbstractReceiverModule<EventMonitor> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<EventMonitor> getImplementation() {
			return EventMonitor.class;
		}
	}

}

