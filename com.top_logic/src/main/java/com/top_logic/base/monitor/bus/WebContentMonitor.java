
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.util.List;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.IReceiver;


/**
 * The Document monitor observes all Changes of Documents within the System. 
 *
 * This Monitor is a EventBuffer, so instead of writing all events into 
 * a logfile, it caches a certain number of events for later access.
 * 
 * The WebContent KO that gave name to this class is actually part
 * of the CMS-module, but is not as such needed by this class.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class WebContentMonitor extends DefaultMonitor implements IReceiver {

    private int eventBufferSize;                     
    
    /** The event buffer to store the events. */
    private EventBuffer eventBuffer;      

	/**
	 * Configuration for {@link WebContentMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends DefaultMonitor.Config {
		/**
		 * Namespace for the service.
		 */
		@Override
		@StringDefault(Bus.CHANGES)
		String getServiceNamespace();

		/**
		 * Name of the service.
		 */
		@Override
		@StringDefault(Bus.WEBCONTENT)
		String getServiceName();
	}
	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link WebContentMonitor}.
	 */
	public WebContentMonitor(InstantiationContext context, Config config) {
		super(context, config);

		this.eventBufferSize = config.getNumberEventsToStore();
	}

    private EventBuffer getBuffer () {
        if(this.eventBuffer == null) {
            this.eventBuffer = new EventBuffer(this.eventBufferSize);            
        }
        return this.eventBuffer;
    }      

    /**
     * Returns the last DocumentEvents (events are create, modify 
     * or delete) as a List of DocumentEvents.
     * <blockquote>   
     * Note:     The DocumentMonitor only holds only a limited number
     *           of DocumentEvents (configurable in TopLogic XML),
     * </blockquote>   
     *
     * @param    maxreturns    The maximum number of returns.
     * @return   A list of DocumentEvents.
     */
    public List getLatestWebContentEvents (int maxreturns) {
        return (this.getBuffer().getLatestEvents (maxreturns));
    }

    /**
     * Method returns all document events buffered in this instance.
     *
     * The size of this list depends on the specified size in the 
     * configuration.
     *
     * @return    All buffered DocumentEvents as list.
     */
    public List getAllWebContentEvents () {
        return (this.getBuffer().getAllEvents ());
    }

    /**
     * Receive the events and processes this to the writer.
     */
    @Override
    protected void process(MonitorEvent anEvent) {
    	this.getBuffer().addEvent(anEvent);
    }
    
	/**
	 * {@link BasicRuntimeModule} for {@link WebContentMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<WebContentMonitor> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<WebContentMonitor> getImplementation() {
			return WebContentMonitor.class;
		}
	}
}
