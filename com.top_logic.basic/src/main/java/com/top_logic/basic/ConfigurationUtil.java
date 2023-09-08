/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.awt.Color;

import com.top_logic.basic.config.ConfigUtil;

/**
 * The ConfigurationUtil contains useful static methods to read 
 * one or two attributes from the configuration.
 *  
 * Note, don't use this class to read many attributes from the 
 * configuration (performance problems). In this case try to 
 * something like that: 
 * Configuration.getConfiguration(ConfigurationUtil.class).getProperties();
 * Boolean.valueOf(theProperties.getProperty("attributeName")).booleanValue()
 * ...
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ConfigurationUtil {

    /** 
     * This method returns the double value from the configuration with the given name
     * and key or the given default double.
     * 
     * @param  aConfigurationName A section name from the configuration.
     * @param  aKey               A entry name in the section.
     * @param  aDefault           A default int if no value is definded in the
     *                            configuration.
     * @return A double.
     * 
     * @deprecated Use {@link Configuration#getDouble(String, double)}.
     */
    @Deprecated
	public static double getDoubleValue(String aConfigurationName, String aKey, double aDefault) {
        double theDouble = aDefault;
        
        String theValue  = Configuration.getConfigurationByName(aConfigurationName).getValue(aKey);
        try {
            if (!StringServices.isEmpty(theValue)) {
                theDouble = Double.parseDouble(theValue.trim());            
            }
        } catch (Exception e) {
            Logger.warn("Could not get double value for key ('" + aKey + "') from configuration('" + aConfigurationName + "')", e, ConfigurationUtil.class);
        }
        
        return theDouble;
    }
    
    /** 
     * This method returns the int value from the configuration with the given name
     * and key or the given default int.
     * 
     * @param  aConfigurationName A section name from the configuration.
     * @param  aKey               A entry name in the section.
     * @param  aDefault           A default int if no value is definded in the
     *                            configuration.
     * @return A int.
     * 
     * @deprecated Use {@link Configuration#getInteger(String, int)}.
     */
    @Deprecated
	public static int getIntValue(String aConfigurationName, String aKey, int aDefault) {
        int    theInt   = aDefault;
        
        String theValue = Configuration.getConfigurationByName(aConfigurationName).getValue(aKey);
        try {
            if (!StringServices.isEmpty(theValue)) {
                theInt = Integer.parseInt(theValue.trim());            
            }
        } catch (Exception e) {
            Logger.warn("Could not get integer value for key ('" + aKey + "') from configuration('" + aConfigurationName + "')", e, ConfigurationUtil.class);
        }
        
        return theInt;
    }
    
    /**
     * This method returns the color from the configuration with the given name
     * and key or the given default color.
     * 
     * @param  aConfigurationName A section name from the configuration.
     * @param  aKey               A entry name in the section.
     * @param  aDefault           A default color if no color is definded in the
     *                            configuration.
     * @return A {@link Color}.
     */
    public static Color getColor(String aConfigurationName, String aKey, Color aDefault) {
        Color  theResult = aDefault;
        
        String theValue  = Configuration.getConfigurationByName(aConfigurationName).getValue(aKey);
        try {
            if (!StringServices.isEmpty(theValue)) {
                theResult = Color.decode(theValue);
            }
        } catch (Exception e) {
            Logger.warn("Could not get color for key ('" + aKey + "') from configuration('" + aConfigurationName + "')", e, ConfigurationUtil.class);
        }
        
        return theResult;
    }
    
    /**
     * This method returns the boolean from the configuration with the given name
     * and key or the given default boolean.
     * 
     * @param  aConfigurationName A section name from the configuration.
     * @param  aKey               A entry name in the section.
     * @param  aDefault           A default boolean if no boolean is defined in the
     *                            configuration.
     * @return A boolean.
     * 
     * @deprecated Use {@link Configuration#getBoolean(String, boolean)}.
     */
    @Deprecated
	public static boolean getBoolean(String aConfigurationName, String aKey, boolean aDefault) {
        String  theValue = Configuration.getConfigurationByName(aConfigurationName).getValue(aKey);
        return getBoolean(theValue, aDefault);
    }

    /**
     * Return the boolean configuration value for the given class and key. 
     * 
     * @deprecated Use {@link Configuration#getBoolean(String, boolean)}.
     */
    @Deprecated
	public static boolean getBoolean(Class aConfiguredClass, String aKey, boolean aDefault) {
    	String  theValue = Configuration.getConfiguration(aConfiguredClass).getValue(aKey);
    	return getBoolean(theValue, aDefault);
    }

    /**
	 * Convert the given string to boolean.
	 * 
	 * <p>
	 * The string <code>"true"</code> returns <code>true</code>, the string <code>"false"</code>
	 * returns <code>false</code> by ignoring case. Any other value (including <code>null</code>)
	 * returns the given default.
	 * </p>
	 * 
	 * @deprecated Use {@link ConfigUtil#getBooleanValue(String, CharSequence)}.
	 */
	@Deprecated
	public static boolean getBoolean(String booleanValue, boolean aDefault) {
	    
		if (StringServices.isEmpty(booleanValue)) {
            return aDefault;
        }
        
        if (booleanValue.equalsIgnoreCase("true")) {
            return true;
        } 
        if (booleanValue.equalsIgnoreCase("false")) {
            return false;
        } 
        return aDefault;
	}
    
}

