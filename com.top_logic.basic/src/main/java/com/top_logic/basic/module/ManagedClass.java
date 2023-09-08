/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.format.ClassFormat;

/**
 * Base class for services.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("TopLogic service")
public abstract class ManagedClass {

	/**
	 * Configuration for services.
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface ServiceConfiguration<M extends ManagedClass> extends PolymorphicConfiguration<M> {
		/**
		 * Returns the configured dependencies and whether the dependency is
		 * enabled or not.
		 * 
		 * Dependencies are configured in the {@link ServiceConfiguration}
		 * whereas it is used in its module. The reason is that the dependencies
		 * depend on the special implementation class.
		 */
		@MapBinding(keyFormat=ClassFormat.class)
		Map<Class<?>, Boolean> getDependencies();
	}

	private boolean _started;

	/**
	 * Create a managed class
	 * 
	 * @param config
	 *        the configuration of this {@link ManagedClass}, i.e. if
	 *        <code>A extends ManagedClass</code> then the given configuration
	 *        is derived by {@link Configuration#getConfiguration(Class)} with
	 *        <code>A.class</code> as argument.
	 */
	protected ManagedClass(IterableConfiguration config) {
		// Constructor template.
	}

	/**
	 * Creates a managed class.
	 * 
	 * @since 5.7.3
	 * 
	 * @param context
	 *        the context which can be used to instantiate inner configurations.
	 * @param config
	 *        the configuration for the service.
	 */
	protected ManagedClass(InstantiationContext context, ServiceConfiguration<?> config) {
		// Constructor template.
	}
	
	/**
	 * Creates a {@link ManagedClass}.
	 * 
	 * @param config
	 *        The configuration of this {@link ManagedClass}.
	 * @throws ConfigurationException
	 *         If the given configuration is invalid.
	 */
	protected ManagedClass(Properties config) throws ConfigurationException {
		// Constructor template.
	}
	
	protected ManagedClass() {
		// Constructor template.
	}

	/**
	 * Stops this {@link RuntimeModule}.
	 */
	protected void shutDown() {
		// nothing to do here
	}
	
	/**
	 * Is called after creating the new {@link ManagedClass}. The corresponding
	 * {@link BasicRuntimeModule} is active and usable.
	 * 
	 * @since 5.7.3
	 */
	protected void startUp() {
		// nothing to do here
	}

	/**
	 * Whether the service has finished starting.
	 * 
	 * <p>
	 * Note: This is different than {@link BasicRuntimeModule#isActive()}, since the latter is
	 * already true when the service begins starting up.
	 * </p>
	 */
	public boolean isStarted() {
		return _started;
	}

	void doStart() {
		startUp();
		_started = true;
	}

	void doStop() {
		_started = false;
		shutDown();
	}

}
