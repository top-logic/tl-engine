/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.top_logic.base.bus.DocumentEvent;
import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.event.bus.IReceiver;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Listener of {@link com.top_logic.base.bus.UserEvent}s.
 * Supportes are backup on file with buffering for fast access.
 *
 *                                 and implements IReceiver
 *                                 and to use EventBuffer, EventWriter, EventReader

 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DocumentMonitor extends DefaultMonitor implements IReceiver {
    
    private int         eventBufferSize;
       
    /** The event writer to write out the events. */
    private EventWriter eventWriter;       
    
    /** The event buffer to store the events. */
    private EventBuffer eventBuffer;  

	/**
	 * Configuration for {@link DocumentMonitor}.
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
		@StringDefault(Bus.DOCUMENT)
		String getServiceName();
    }
    
	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link DocumentMonitor}.
	 */
	public DocumentMonitor(InstantiationContext context, Config config) {
		super(context, config);

		this.eventBufferSize = config.getNumberEventsToStore();

		// reader
		List theEvents = null;
		try {
			theEvents = new EventReader(this.createReader()).readAllEvents(this);
		} catch (Exception e) {
			Logger.error("Unable to read DocumentEvents", e, this);
		}
		if (theEvents != null && !theEvents.isEmpty()) {
			EventBuffer buffer = this.getBuffer();
			int size = theEvents.size();
			for (int i = 0; i < size; i++) {
				buffer.addEvent((BusEvent) theEvents.get(i));
			}
		}
	}
    
    private EventBuffer getBuffer () {
        if(this.eventBuffer == null) {
            this.eventBuffer = new EventBuffer(this.eventBufferSize);            
        }
        return this.eventBuffer;
    }    
    
    private EventWriter getWriter () {
        if(this.eventWriter == null) {
            try {
                this.eventWriter = new EventWriter(createWriter());      
            } catch (IOException e) {
                Logger.error ("Unable to initialize Writer of UserEvents, reason is: ", e, this);            
            }             
        }
        return this.eventWriter;
    }
    
    // RECEIVER    

    /**
     * Receive the events and processes this to the writer.
     *
     * @param    aBusEvent    The event to be processed.
     */
    @Override
    protected void process(MonitorEvent aBusEvent) {
        if (aBusEvent instanceof DocumentEvent) {
            //add event to cache            
            this.getBuffer().addEvent(aBusEvent);
            
            //backup all currently cached events     
            try {
                 this.getWriter().writeAllEvents (this.eventBuffer.getAllEvents());
            } catch (IOException e) {
              Logger.error ("Unable to write event buffer, reason is: ", e, this);                           
            } 
        }
    }   
    
    // DOCUMENT

    /**
     * Returns buffer size.
     */ 
    public int getMaxBufSize () {
        return this.getBuffer().getSize();
    }

    /**
	 * Returns the last DocumentEvents (events are create, modify or delete) as a List of
	 * DocumentEvents.
	 * 
	 * <p>
	 * Note: The DocumentMonitor only holds only a limited number of DocumentEvents (configurable in
	 * TopLogic XML).
	 * </p>
	 * 
	 * @param maxreturns
	 *        The maximum number of returns.
	 * @return A list of DocumentEvents.
	 */
    public List getLatestDocumentEvents (int maxreturns) {
        return (this.getLatestDocumentEvents (maxreturns, true));
    }

    /**
     * Returns the last DocumentEvents (events are create, modify 
     * or delete) as a List of DocumentEvents.
     *   
     * @param    maxreturns        The maximum number of returns.
     * @param    withDuplicates    true, if more than one event with 
     *                             the same message.
     * @return   A list of DocumentEvents.
     * <blockquote>
     * Note:    The DocumentMonitor only holds only a limited number
     *           of DocumentEvents (configurable in TopLogic XML),
     * </blockquote>
     */
    public List getLatestDocumentEvents (int maxreturns, boolean withDuplicates) {
        return (this.eventBuffer.getLatestEvents (maxreturns, withDuplicates));
    }

    /**
     * Method returns all document events buffered in this instance.
     *
     * The size of this list depends on the specified size in the 
     * configuration.
     *
     * @return    All buffered DocumentEvents as list.
     */
    public List getAllDocumentEvents () {
        return (this.eventBuffer.getAllEvents ());
    }
    
    /**
     * Method to read MonitorEvent from BufferedReader.
     * 
     * @param aReader reader to read from
     * @return event UserEvent
     */   
    @Override
	public MonitorEvent readEvent(BufferedReader aReader) throws IOException {     
		TLID theSourceID = IdentifierUtil.fromExternalForm(aReader.readLine());
        String theSourceType  = aReader.readLine ();
		TLID theMessageID = IdentifierUtil.fromExternalForm(aReader.readLine());
        String theMessageType = aReader.readLine ();
        long   theDate        = 0;        
        try {
            theDate = Long.parseLong (aReader.readLine ());
        }
        catch (NumberFormatException ex) { /* ignored */ }
        String theType = aReader.readLine ();
        String theUser = aReader.readLine ();
        if (theUser != null) {
			Wrapper messageKO = MonitorEvent.getWrapper(theMessageType, theMessageID);
			Wrapper sourceKO = MonitorEvent.getWrapper(theSourceType, theSourceID);
			Person theUserI = MonitorEvent.getUser(theUser);

            if (messageKO == null|| sourceKO  == null || theUserI  == null) {
                    Logger.warn("Unable to recreate correct Event "
                            + theMessageType + '/' + theMessageID + " "
                            + theSourceType  + '/' + theSourceID  + " "
                            + theUser, this);
                    return null;
            }

            if ("Document".equals (theMessageType)) {
                return new DocumentEvent (new Sender(),messageKO, sourceKO, theUserI, new Date (theDate), theType);
            }
            else {
                // return new ContentAssistantEvent (new Sender(),messageKO, sourceKO, theUserI, new Date (theDate), theType);
            }  
        }
        return null;           
    }
    
	/**
	 * {@link BasicRuntimeModule} for {@link DocumentMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
    public static final class Module extends AbstractReceiverModule<DocumentMonitor> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<DocumentMonitor> getImplementation() {
			return DocumentMonitor.class;
		}

	}
}
