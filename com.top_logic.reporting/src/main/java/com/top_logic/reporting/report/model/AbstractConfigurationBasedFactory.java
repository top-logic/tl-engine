/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigurationResolver;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public abstract class AbstractConfigurationBasedFactory {

    /**
     * Return a {@link PolymorphicConfiguration} for a {@link Class} which can be 
     * instantiated from that {@link PolymorphicConfiguration}
     */
	protected final <T> PolymorphicConfiguration<T> createConfiguration(Class<? extends T> anImplementationClass)
			throws ConfigurationException {
        DefaultConfigurationResolver resolver = new DefaultConfigurationResolver();
		ConfigurationDescriptor theDesc = resolver.getConfigurationDescriptor(anImplementationClass);
    
        ConfigBuilder theBuild = TypedConfiguration.createConfigBuilder(theDesc);
        theBuild.initValue(theDesc.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME), anImplementationClass);
        
		Protocol log = new LogProtocol(AbstractConfigurationBasedFactory.class);
		InstantiationContext instantiationContext = new SimpleInstantiationContext(log);
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<T> result = (PolymorphicConfiguration) theBuild.createConfig(instantiationContext);
		log.checkErrors();
		return result;
    }
    
	protected final <T> T createImplementation(PolymorphicConfiguration<T> aConfiguration) {
		final InstantiationContext context = getInstantiationContext();
		T result = context.getInstance(aConfiguration);
		try {
			context.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Unable to instantiate configuration.", ex);
		}
		return result;
    }
    
    protected final InstantiationContext getInstantiationContext() {
		return new DefaultInstantiationContext(AbstractConfigurationBasedFactory.class);
    }
}

