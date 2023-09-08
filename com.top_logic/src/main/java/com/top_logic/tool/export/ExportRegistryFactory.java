/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;


/**
 * Provider for the {@link ExportRegistry} to use.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@ServiceDependencies({
	ThreadContextManager.Module.class
})
public final class ExportRegistryFactory extends ManagedClass {

	/**
	 * Configuration of the {@link ExportRegistryFactory}.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<ExportRegistryFactory> {

		/**
		 * Configuration of the used {@link ExportRegistry}.
		 */
		@Mandatory
		PolymorphicConfiguration<ExportRegistry> getExportRegistry();

	}

	private final ExportRegistry _registry;

	/**
	 * Creates a new {@link ExportRegistryFactory} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ExportRegistryFactory}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ExportRegistryFactory(InstantiationContext context, Config config) throws ConfigurationException {
		_registry = context.getInstance(config.getExportRegistry());
	}

	public static ExportRegistry getExportRegistry() {
		return Module.INSTANCE.getImplementationInstance()._registry;
    }

	@Override
	protected void startUp() {
		super.startUp();
		try {
			_registry.startup();
		} catch (ExportFailure ex) {
			Logger.error("Unable to start ExportRegistry '" + _registry + "'.", ex, ExportRegistryFactory.class);
		}
	}

	@Override
	protected void shutDown() {
		_registry.shutdown();
		super.shutDown();
	}
    
	/**
	 * Module for service class {@link ExportRegistryFactory}.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<ExportRegistryFactory> {

		/** Singleton {@link ExportRegistryFactory.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<ExportRegistryFactory> getImplementation() {
			return ExportRegistryFactory.class;
		}

	}

}

