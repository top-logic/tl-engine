/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.mapBasedPersistancy;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;

/**
 * Factory that creates instances of {@link com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware}
 * from a given value map.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MapBasedPersistancyFactory {

    /**
     * Paramaters for aCTor without a Map;
     */
    protected static final Class[] PARAMETERS = new Class[] {};

    /**
     * Paramaters for aCTor using a Map;
     */
    protected static final Class[] MAP_PARAMETERS = new Class[] { Map.class };
    
    /** The key for finding the class to be used as factory. */
    private static final String FACTORY_KEY = "factory";

	/**
	 * Default CTor
	 */
	protected MapBasedPersistancyFactory() {
		super();
	}

	/** Singleton pattern. */
	private static MapBasedPersistancyFactory singleton;
	
	/**
	 * Get the single instance
	 * 
	 * @return the single instance
	 */
	public static synchronized MapBasedPersistancyFactory getInstance () {
        if (singleton == null) {
            setupInstance();
        }

        return (singleton);
    }

    /**
     * Statically synhronized actual implementation of getInstance().
     */
    protected static synchronized void setupInstance() {
        if (singleton == null) {
            try {
                String theName  =
                    Configuration.getConfiguration(MapBasedPersistancyFactory.class)
                                 .getValue(FACTORY_KEY);
                Class  theClass = null;
                if (theName != null) theClass = Class.forName(theName);
                if (theClass != null) {
                	singleton = (MapBasedPersistancyFactory) theClass.newInstance();
                } else {
                	singleton = new MapBasedPersistancyFactory();
                }
            }
            catch (Exception ex) {
                Logger.info("No MapBasedPersistancyFactory configured, using default",
                            ex, MapBasedPersistancyFactory.class);
            }
        }
    }

	/** 
	 * Returns an {@link MapBasedPersistancyAware} for the given filter config Map.
     * 
     * The map MUST contain an attribute "class" indicating the class to instanciate.
     * 
     * Other atteributes in the Map may be "neg", wrapperIds, ...
	 * 
	 * @param aConfigMap	the configuration including its class name
	 * @return the {@link MapBasedPersistancyAware}, never <code>null</code>.
	 * @throws RuntimeException in case creation failed
	 */
	public MapBasedPersistancyAware getObject(Map aConfigMap) {
        
		// Get filter class
		Object theClassName = aConfigMap.get(MapBasedPersistancyAware.KEY_CLASS);
		
		// Get the filter
		MapBasedPersistancyAware theObject = null; 
		try {
		    Class theClass = Class.forName((String) theClassName);
		    try {
		    	// try with map constructor
				Constructor theConstr = theClass.getDeclaredConstructor(MAP_PARAMETERS);
				theObject = (MapBasedPersistancyAware) theConstr.newInstance(new Object[] { aConfigMap });
			} catch (NoSuchMethodException e) {
				// well, try constructor without map 
				Constructor theConstr = theClass.getDeclaredConstructor(PARAMETERS);
				theObject = (MapBasedPersistancyAware) theConstr.newInstance(new Object[] {});
				theObject.setUpFromValueMap(aConfigMap);				
			}
		}
		catch (Exception ex) {
		    throw new RuntimeException("Error creating filter instance: ", ex);
		}
		return theObject;
	}
}
