/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ImplementationClassesProvider;
import com.top_logic.reporting.report.model.AbstractConfigurationBasedFactory;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;

/**
 * The PartitionFunctionFactory is a factory to create {@link PartitionFunction}s.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class PartitionFunctionFactory extends AbstractConfigurationBasedFactory implements ImplementationClassesProvider {

    /** available partitionFunctions */
	public static final String						CLASSIFICATION	= "classificationPartitionFunction";
	public static final String						DATE			= "datePartitionFunction";
	public static final String						NUMBER			= "numberPartitionFunction";
	public static final String						SAME			= "samePartitionFunction";
	public static final String						STRING			= "stringPartitionFunction";
	public static final String						PAYMENT			= "paymentPartitionFunction";

    private static PartitionFunctionFactory instance;

    /** the cache of producer classes */
	private HashMap<String, Class<?>>	partitionFunctions;

	/**
	 * Private to enforce usage of {@link #setupInstance()} / {@link #getInstance()}. 
	 */
	private PartitionFunctionFactory() {
        
	    Properties theProps = Configuration.getConfiguration(PartitionFunctionFactory.class).getProperties();
		
        this.partitionFunctions = new HashMap(theProps.size());
		
		Iterator   theIter  = theProps.keySet().iterator();
		while (theIter.hasNext()) {
            String theKey   = (String) theIter.next();
            int theIdx = theKey.indexOf(":");
            if (theKey.startsWith("function:")) {
                try {
                    String theName = theKey.substring(theIdx+1);
                    Class  theClass = ConfigUtil.getClassForNameMandatory(Object.class, theKey, theProps.getProperty(theKey));
                    
                    this.partitionFunctions.put(theName, theClass);
                } catch (ConfigurationException cfex) {
                    throw new ConfigurationError("Invalid configuration in PartitionFunctionFactory", cfex);
                }
            }
        }
	}
	
	/**
	 * Return a {@link PartitionFunctionConfiguration} that can be applied to the given 
	 * class of a {@link PartitionFunction}.
	 */
	public PartitionFunctionConfiguration createPartitionFunctionConfiguration(
			Class<? extends PartitionFunction> anImplementationClass) throws ConfigurationException {
	    
	    assert PartitionFunction.class.isAssignableFrom(anImplementationClass);
	    assert this.partitionFunctions.containsValue(anImplementationClass);
	    
	    return (PartitionFunctionConfiguration) super.createConfiguration(anImplementationClass);
	}
	
	/**
	 * Return a {@link PartitionFunction} for a given {@link PartitionFunctionConfiguration}
	 */
	public PartitionFunction getPartitionFunction(PartitionFunctionConfiguration aConfig) {
		return super.createImplementation(aConfig);
	}

	@Override
	public Set getImplementationClasses(ConfigurationDescriptor aDescriptor) {
	    return new HashSet(this.partitionFunctions.values());
	}
	
	/**
	 * Standard singleton Pattern applies.
	 */
    public static PartitionFunctionFactory getInstance() {
        if (instance == null) {
            setupInstance();
        }
        return instance;
    }

    /**
     * Set up the PartitionFunctionFactory singleton.
     */
    public static PartitionFunctionFactory setupInstance() {
	    return instance = new PartitionFunctionFactory();
	}
}
