/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.Logger;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Sender;
import com.top_logic.event.bus.Service;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * The ModelTrackingService can be used to send {@link MonitorEvent}s to inform recievers about
 * changes on the global business model.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public final class ModelTrackingService extends ManagedClass {

    /** the services the messages are send on */
    private final Service trackingService = new Service("track", "model");

    // the sender for LogEvent
    private final Sender eventSender  = new Sender(trackingService);
    
    ModelTrackingService() {
    	//singleton
    }
    
    /**
     * Returns the {@link Service} the messages are sent on.
     * 
     * @return not <code>null</code>
     */
    public static Service getTrackingService() {
    	return Module.INSTANCE.getImplementationInstance().trackingService;
    }
    
    /**
     * This method sends a new MonitorEvent on the bus.
     * 
     * @param aMessage    the object that has been changed (e.g. a project), 
     *                    must not be <code>null</code>
     * @param aSource     the "parent" object that is influenced by the change
     *                    (e.g. the parent of a project or a holder of a RiskItem).
     *                    If aSource is <code>null</code>, aMessage is used as source.
     * @param anEventType an event type as specified in {@link MonitorEvent} (created, modified ...)
     */
	private void send(TLObject aMessage, TLObject aSource, String anEventType) {
        
        if (aMessage == null) {
            Logger.warn("Unable to send track events with null message", ModelTrackingService.class);
            return;
        }
        
        if (aSource == null) {
            Logger.debug("Source is null, using message as source", ModelTrackingService.class);
            aSource = aMessage;
        }
        
		MonitorEvent theEvent =
			new MonitorEvent(eventSender, aMessage, aSource, TLContext.currentUser().getUser(), anEventType);
        try {
            Logger.debug("Send tracking event " + anEventType + " for (" + aSource + "," + aMessage + ")", ModelTrackingService.class);
            eventSender.send(theEvent);
        } catch (Exception ex) {
            Logger.error("Problem sending event " + anEventType +" for " + "Trigger: '" + aMessage + "' Source: '" + aSource, ex, ModelTrackingService.class);
        }
    }
    
    /**
     * This method sends a MonitorEvent of type {@link MonitorEvent#CREATED}
     * 
     * @param aCreatedObject the object that was created
     * @param aSource        the "parent" object that is influenced by the change.
     *                       (e.g. the parent of a project or a holder of a RiskItem)
     */
	public static void sendCreateEvent(TLObject aCreatedObject, TLObject aSource) {
        Module.INSTANCE.getImplementationInstance().send(aCreatedObject, aSource, MonitorEvent.CREATED);
    }
    
    /**
     * This method sends a MonitorEvent of type {@link MonitorEvent#MODIFIED}
     * 
     * @param aModifiedObject the object that has been modified
     * @param aSource         the "parent" object that is influenced by the change.
     *                        (e.g. the parent of a project or a holder of a RiskItem)
     */
	public static void sendModifyEvent(TLObject aModifiedObject, TLObject aSource) {
    	Module.INSTANCE.getImplementationInstance().send(aModifiedObject, aSource, MonitorEvent.MODIFIED);
    }
    
    /**
     * This method sends a MonitorEvent of type {@link MonitorEvent#DELETED}
     * 
     * @param aDeletedObject the object that had been deleted
     * @param aSource        the "parent" object that is influenced by the change.
     *                       (e.g. the parent of a project or a holder of a RiskItem)
     */
	public static void sendDeleteEvent(TLObject aDeletedObject, TLObject aSource) {
    	Module.INSTANCE.getImplementationInstance().send(aDeletedObject, aSource, MonitorEvent.DELETED);
    }
    
    /**
     * This method sends a new MonitorEvent on the bus.
     * 
     * @param aMessage    the object that has been changed (e.g. a project), 
     *                    must not be <code>null</code>
     * @param aSource     the "parent" object that is influenced by the change
     *                    (e.g. the parent of a project or a holder of a RiskItem).
     *                    If aSource is <code>null</code>, aMessage is used as source.
     * @param anEventType an event type as specified in {@link MonitorEvent} (created, modified ...)
     */
	public static void sendEvent(TLObject aMessage, TLObject aSource, String anEventType) {
    	Module.INSTANCE.getImplementationInstance().send(aMessage, aSource, anEventType);
    }

	public static final class Module extends BasicRuntimeModule<ModelTrackingService> {

		public static final Module INSTANCE = new Module();

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return Collections.singletonList(Bus.Module.class);
		}

		@Override
		public Class<ModelTrackingService> getImplementation() {
			return ModelTrackingService.class;
		}

		@Override
		protected ModelTrackingService newImplementationInstance() throws ModuleException {
			return new ModelTrackingService();
		}
	}
}

