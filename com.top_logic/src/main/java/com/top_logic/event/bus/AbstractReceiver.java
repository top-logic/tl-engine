
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.bus;

import java.rmi.RemoteException;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;

/** 
 * This class provides the functionality for receiving classes to get a certain
 * service and to be added to the bus. The interface' subscribe()-method is 
 * called by Receiver objects to be connected to specified services by the bus.  
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@ServiceDependencies({ Bus.Module.class, ApplicationConfig.Module.class })
public abstract class AbstractReceiver extends ManagedClass implements IReceiver {

    /** 
     * The service we want to receive Messages for 
     *
     * This is not actually used as the Service used for 
     * subscribing at the Bus counts.
     */
    private Service _service;

	/**
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<AbstractReceiver> {
		/**
		 * Namespace for the service.
		 */
		@StringDefault(Service.WILDCARD)
		String getServiceNamespace();

		/**
		 * Name of the service.
		 */
		@StringDefault(Service.WILDCARD)
		String getServiceName();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link AbstractReceiver}.
	 */
	public AbstractReceiver(InstantiationContext context, Config config) {
		super(context, config);

		_service = new Service(config.getServiceNamespace(), config.getServiceName());
	}
    
    /**
     * Supply som reasonalbale String fo looging/debugging
     */
    @Override
	public String toString() {
        return getClass() + " for " + _service;
    }

    /**
     *  Calls on the bus to add the receiver to listen to the specified
     *  service.
     */  
    @Override
	public void subscribe () {
        Bus.getInstance ().subscribe (this, this.getService ());
    }

    /**
     *  Calls on the bus to add the receiver to listen to the specified
     *  service.
     */  
    @Override
	public void unsubscribe () {
        Bus.getInstance ().unsubscribe (this, this.getService ());
    }

    /**
     * Returns a Service object or initializes a new one.
     *
     * @return    The concrete service demanded by the receiver.
     */
    protected Service getService () {
        return _service;
    }

    /**
     * Is invoked when a new channel is created at the bus, i.e. when a new 
     * sender publishes.
     * 
     * @param  aBusEvent   The event object providing information about the 
     *                     event and its source.
     */
    @Override
	public abstract void receive (BusEvent aBusEvent) throws RemoteException;

    @Override
    protected void shutDown() {
    	unsubscribe();
		super.shutDown();
    }

	/**
	 * Superclass for modules for implementations which extends {@link AbstractReceiver}. It defines
	 * the dependencies needed by {@link AbstractReceiver}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	protected static abstract class AbstractReceiverModule<M extends ManagedClass> extends TypedRuntimeModule<M> {

    }

}


    
    
