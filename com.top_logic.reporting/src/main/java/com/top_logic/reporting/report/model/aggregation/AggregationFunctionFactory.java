/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigurationResolver;
import com.top_logic.basic.config.ImplementationClassesProvider;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.model.AbstractConfigurationBasedFactory;

/**
 * A Factory to create {@link AbstractAggregationFunction}s for Attributes on some {@link Wrapper}.
 * 
 * Please compare with the (
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class AggregationFunctionFactory extends AbstractConfigurationBasedFactory implements ImplementationClassesProvider {

	@Deprecated
	public final static String					AGGREGATION_AVG_FUNCTION		= "avg";
	@Deprecated
	public final static String					AGGREGATION_COUNT_FUNCTION		= "count";
	@Deprecated
	public final static String					AGGREGATION_COUNT_U_FUNCTION	= "countUnique";
	@Deprecated
	public final static String					AGGREGATION_LOWER_FUNCTION		= "lower";
	@Deprecated
	public final static String					AGGREGATION_MAX_FUNCTION		= "max";
	@Deprecated
	public final static String					AGGREGATION_MEDIAN_FUNCTION		= "median";
	@Deprecated
	public final static String					AGGREGATION_MIN_FUNCTION		= "min";
	@Deprecated
	public final static String					AGGREGATION_SUM_FUNCTION		= "sum";
	@Deprecated
	public final static String					AGGREGATION_UPPER_FUNCTION		= "upper";

	private static AggregationFunctionFactory	instance;
	@Deprecated
	private HashMap/*<String,Constructor>*/		constructors;
	
	private List classes;
	
	@Deprecated
	/** List of all Keys derived from constructors, in natural order */
	private List/*<String>*/                    functionList;

	/**
	 * Singleton access only, use {@link #getInstance()}
	 * public modifier for reflection
	 */
	public AggregationFunctionFactory() {
	    
	    Properties theProps = Configuration.getConfiguration(AggregationFunctionFactory.class).getProperties();
	    
	    Iterator theIter = theProps.keySet().iterator();

	    this.classes      = new ArrayList();
	    this.constructors = new HashMap();
	    
	    while (theIter.hasNext()) {
            String theKey   = (String) theIter.next();
            int theIdx = theKey.indexOf(":");
            if (theKey.startsWith("function:")) {
                try {
                    String theName = theKey.substring(theIdx+1);
	                Class  theClass = ConfigUtil.getClassForNameMandatory(Object.class, theKey, theProps.getProperty(theKey));
	                this.classes.add(theClass);
	                this.constructors.put(theName, theClass);
                } catch (ConfigurationException cfex) {
                    throw new ConfigurationError("Invalid configuration in AggregationFunctionFactory", cfex);
                }
            }
	    }
	}

	/**
     * Return a {@link AggregationFunctionConfiguration} that can be applied to the given 
     * class of a {@link AggregationFunction}.
     */
	public AggregationFunctionConfiguration createAggregationFunctionConfiguration(
			Class<? extends AbstractAggregationFunction> anImplementationClass) throws ConfigurationException {
        
        assert AggregationFunction.class.isAssignableFrom(anImplementationClass);
        assert this.classes.contains(anImplementationClass);
        
        return (AggregationFunctionConfiguration) super.createConfiguration(anImplementationClass);
    }
	
	public AggregationFunction getAggregationFunction(AggregationFunctionConfiguration aConfig) {
		return super.createImplementation(aConfig);
    }
    
	@Override
	public Set getImplementationClasses(ConfigurationDescriptor aDescriptor) {
	    return new HashSet(this.classes);
	}
	
	/**
	 * @Deprecated use {@link #getImplementationClasses(ConfigurationDescriptor)} 
	 */
	public List getFunctionList() {
	    if (this.functionList == null) {
	        this.functionList = new ArrayList(this.constructors.keySet());
	        Collections.sort(this.functionList);
	    }
	    return this.functionList;
	}
	
	
	/**
	 * @deprecated use {@link #getAggregationFunction(AggregationFunctionConfiguration)}
	 */
	@Deprecated
	public AggregationFunction getFunction(String aFunctionName, String anAttributePath) {

	    Class       theClass       = (Class) this.constructors.get(aFunctionName);

		if (theClass == null) {
			throw new ReportingException(this.getClass(), "Given funtion name '" + aFunctionName + "' is not known");
		}

		try {
		    DefaultConfigurationResolver resolver = new DefaultConfigurationResolver();
			ConfigurationDescriptor theConfDesc = resolver.getConfigurationDescriptor(theClass);
			if (AggregationFunctionConfiguration.class.isAssignableFrom(theConfDesc.getConfigurationInterface())) {
				ConfigBuilder theBuilder = TypedConfiguration.createConfigBuilder(theConfDesc);

				theBuilder.initValue(
					theConfDesc.getProperty(AggregationFunctionConfiguration.IMPLEMENTATION_CLASS_NAME), theClass);
				theBuilder.initValue(
					theConfDesc.getProperty(AggregationFunctionConfiguration.ATTRIBUTE_PATH_NAME), anAttributePath);
				Protocol log = new LogProtocol(AggregationFunctionFactory.class);
				ConfigurationItem theItem = theBuilder.createConfig(new SimpleInstantiationContext(log));
				log.checkErrors();
		        
		        return this.getAggregationFunction((AggregationFunctionConfiguration) theItem);
		    }
		    
		}
		catch (ConfigurationException ex) {
			throw new ReportingException(this.getClass(), "Unable to instanciate aggregation function: " + aFunctionName, ex);
		}
		return null;
	}

	public static synchronized AggregationFunctionFactory getInstance() {
        if (instance == null) {
            Properties theProps = Configuration.getConfiguration(AggregationFunctionFactory.class).getProperties();
            try {
                instance = ConfigUtil.getNewInstanceWithClassDefault(AggregationFunctionFactory.class, "class", theProps.getProperty("class"), AggregationFunctionFactory.class);
            } catch (ConfigurationException ex) {
                throw new ConfigurationError("Cannot instancicate a singleton of AggregationFunctionFactory", ex);
            }
        }
        return instance;
    }

}
