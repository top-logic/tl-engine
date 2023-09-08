/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.objectproducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ImplementationClassesProvider;
import com.top_logic.reporting.report.model.AbstractConfigurationBasedFactory;

/**
 * This factory produces {@link ObjectProducer}.
 * 
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class ObjectProducerFactory extends AbstractConfigurationBasedFactory implements ImplementationClassesProvider {

    public static final ObjectProducerFactory INSTANCE = new ObjectProducerFactory();
    
    public  static final String DEFAULT_PRODUCER   = "default";
    private static final String DEFAULT_PRODUCER_2 = "Wrapper";
    private static final String DEFAULT_PRODUCER_3 = WrapperObjectProducer.class.getName();
    
    /** the cache of producer classes */
    private HashMap producers;
    
    /**
     * Default C'Tor
     */
    private ObjectProducerFactory() {
        this.producers            = new HashMap();
        this.producers.put(DEFAULT_PRODUCER,   WrapperObjectProducer.class);
        this.producers.put(DEFAULT_PRODUCER_2, WrapperObjectProducer.class);
        this.producers.put(DEFAULT_PRODUCER_3, WrapperObjectProducer.class);
        
    }
    
    public ObjectProducer getObjectProducer(ObjectProducerConfiguration aConfig) {
		return this.createImplementation(aConfig);
    }
    
    @Override
	public Set getImplementationClasses(ConfigurationDescriptor aDescriptor) {
        return new HashSet(this.producers.values());
    }
    
	public ObjectProducerConfiguration createObjectProducerConfiguration(
			Class<? extends ObjectProducer> anImplemetationClass) {
        
        assert ObjectProducer.class.isAssignableFrom(anImplemetationClass);
        try {
            return (ObjectProducerConfiguration) this.createConfiguration(anImplemetationClass);
        } catch (ConfigurationException c) {
            throw new ConfigurationError("invalid producer configuration", c);
        }
    }
    
    public static ObjectProducerFactory getInstance() {
        return INSTANCE;
    }
}
