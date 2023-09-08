/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.function.Supplier;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;

/**
 * {@link InstantiationContext} that creates fresh instances for each
 * configuration regardless of a shared instance declaration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SimpleInstantiationContext extends AbstractInstantiationContext {

	/**
	 * An {@link InstantiationContext} for small instantiations that does not produce shared
	 * instances.
	 */
	public static InstantiationContext CREATE_ALWAYS_FAIL_IMMEDIATELY =
		new SimpleInstantiationContext(ConfigurationErrorProtocol.INSTANCE);

	/**
	 * Creates a new {@link SimpleInstantiationContext} using the given
	 * {@link Protocol} for error reporting.
	 */
	public SimpleInstantiationContext(Log protocol) {
		super(protocol);
	}

	@Override
	protected <T> T lookupOrCreate(InstantiationContext self, PolymorphicConfiguration<T> configuration, Class<?> implementationClass)
			throws ConfigurationException {
		Factory factory = DefaultConfigConstructorScheme.getFactory(implementationClass);
		@SuppressWarnings("unchecked")
		T configuredInstance = (T) factory.createInstance(self, configuration);
		return configuredInstance;
	}
	
	@Override
	public <T> void resolveReference(Object id, Class<T> scope, ReferenceResolver<T> setter) {
		throw new UnsupportedOperationException("A simple instantiation context is not able to resolve references.");
	}

	@Override
	public <T> T deferredReferenceCheck(InstantiationContext self, Supplier<T> r) {
		// Reference resolution is not supported at all.
		return r.get();
	}
}
