/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.keystorages.KeyStorageCheckerRegistry;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;


/**
 * Configurable static facade for {@link TypeKeyProvider}. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies(KeyStorageCheckerRegistry.Module.class)
public final class TypeKeyRegistry extends ManagedClass {

	public static final String PROVIDER_CLASS_CONFIGNAME = "provider";
	
	/**
	 * Configuration for the {@link TypeKeyRegistry}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ManagedClass.ServiceConfiguration<TypeKeyRegistry> {

		/**
		 * Configuration of the {@link TypeKeyProvider} to use.
		 */
		@Name(PROVIDER_CLASS_CONFIGNAME)
		@Mandatory
		PolymorphicConfiguration<TypeKeyProvider> getProvider();
	}

	private final TypeKeyProvider _provider;
	

	/**
	 * Creates a new {@link TypeKeyRegistry} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TypeKeyRegistry}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public TypeKeyRegistry(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_provider = context.getInstance(config.getProvider());
	}

	public static TypeKeyProvider.Key lookupTypeKey(Object obj) {
		return getInstance().getTypeProvider().lookupTypeKey(obj);
	}

	/**
	 * The singleton {@link TypeKeyRegistry} instance.
	 */
	public static TypeKeyRegistry getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

    /**
	 * The configured {@link TypeKeyProvider} implementation.
	 */
	public final TypeKeyProvider getTypeProvider() {
		return _provider;
	}

    public static class Module extends TypedRuntimeModule<TypeKeyRegistry>{
    	
    	/** Singleton {@link TypeKeyRegistry.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<TypeKeyRegistry> getImplementation() {
			return TypeKeyRegistry.class;
		}
    	
    }

}
