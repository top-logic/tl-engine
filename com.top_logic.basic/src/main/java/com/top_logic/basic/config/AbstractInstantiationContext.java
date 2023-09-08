/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;

/**
 * {@link InstantiationContext} that creates fresh instances for each
 * configuration regardless of a shared instance declaration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractInstantiationContext extends InstantiationContextImpl {

	/**
	 * Creates a new {@link AbstractInstantiationContext} using the given
	 * {@link Protocol} for error reporting.
	 */
	public AbstractInstantiationContext(Log protocol) {
		super(protocol);
	}

	@Override
	public final <T> T getInstance(InstantiationContext self, PolymorphicConfiguration<T> configuration) {
		if (configuration == null) {
			return null;
		}
		
		Class<? extends T> implementationClass = configuration.getImplementationClass();
		if (implementationClass == null) {
			throw new IllegalArgumentException("Polymorphic configuration '" + configuration.descriptor().getConfigurationInterface() + "' without a value for the implementation class property cannot be instantiated.");
		}
		
		return getInstance(self, configuration, implementationClass);
	}

	/**
	 * Wraps {@link ConfigurationException} from
	 * {@link #lookupOrCreate(InstantiationContext, PolymorphicConfiguration, Class)} into a protocol error.
	 * 
	 * @param self
	 *        The outer {@link InstantiationContext} instance to pass to configuration constructors.
	 * 
	 * @see #lookupOrCreate(InstantiationContext, PolymorphicConfiguration, Class)
	 */
	protected final <T> T getInstance(InstantiationContext self, PolymorphicConfiguration<T> configuration,
			Class<?> implementationClass) {
		try {
			return lookupOrCreate(self, configuration, implementationClass);
		} catch (ConfigurationException ex) {
			// TODO decide whether error() or throw fatal(...) is correct depending on the mandatory property of the property (problem: this information is not available here).
			self.error("Configured class instantiation failed: " + implementationClass, ex);
			return null;
		}
	}

	/**
	 * Performs the actual creation.
	 * 
	 * @param self
	 *        The outer {@link InstantiationContext} instance to pass to configuration constructors.
	 * 
	 * @see #getInstance(PolymorphicConfiguration)
	 */
	protected abstract <T> T lookupOrCreate(InstantiationContext self, PolymorphicConfiguration<T> configuration, Class<?> implementationClass)
			throws ConfigurationException;
	
}
