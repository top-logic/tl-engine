/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.util.Map;
import java.util.NoSuchElementException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.structured.util.NumberHandler.NumberHandlerConfig;
import com.top_logic.layout.provider.LabelProviderService;

/**
 * This class handles the {@link com.top_logic.element.structured.util.NumberHandler}s. This
 * class gets information from the xml config file and creates a map with all
 * known {@link com.top_logic.element.structured.util.NumberHandler}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
// Note: A configured number handler uses the meta label provider to add a dynamic context to the generated ID.
@ServiceDependencies(LabelProviderService.Module.class)
public class NumberHandlerFactory extends ManagedClass {
    
	/**
	 * Configuration of a {@link NumberHandlerFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<NumberHandlerFactory> {
		
		/**
		 * The configuration of the {@link NumberHandler}s.
		 */
		@Key(NumberHandlerConfig.NAME_ATTRIBUTE)
		@Subtypes({
			@Subtype(tag = "handler", type = ConfiguredNumberHandler.Config.class)
		})
		@EntryTag("custom-handler")
		Map<String, NumberHandlerConfig<? extends NumberHandler>> getHandlers();

	}

	/** The map of known number handlers. */
	private final Map<String, NumberHandler> _handlersByName;

	/**
	 * Creates a new {@link NumberHandlerFactory} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link NumberHandlerFactory}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public NumberHandlerFactory(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_handlersByName = TypedConfiguration.getInstanceMap(context, config.getHandlers());
	}

	/**
	 * This method returns a specific {@link NumberHandler} by the given key.
	 * 
	 * @param handlerName
	 *        A key.
	 * @return Returns a {@link NumberHandler}, never <code>null</code>.
	 * 
	 * @throws NoSuchElementException
	 *         if there is no {@link NumberHandler} configured for the given
	 *         name.
	 */
    public synchronized NumberHandler getNumberHandler(String handlerName) {
		NumberHandler numberHandler = _handlersByName.get(handlerName);
        if (numberHandler == null) {
        	throw new NoSuchElementException("No number handler for the given name: " + handlerName);
        }
        
        return numberHandler;
    }
    
    /**
     * This method returns the single instance of the
     * {@link NumberHandlerFactory}.
     * 
     * @return Returns a {@link NumberHandlerFactory}.
     */
    public static NumberHandlerFactory getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

	public static final class Module extends TypedRuntimeModule<NumberHandlerFactory> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<NumberHandlerFactory> getImplementation() {
			return NumberHandlerFactory.class;
		}

	}

}
