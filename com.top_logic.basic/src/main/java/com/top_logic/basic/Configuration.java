/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Abstraction of configuration.
 * <p>
 * This class should be used rather than XMLProperties or any other
 * ungeneric configuration mechanism, when dealing with the main
 * configuration. (There are other valid cases where XMLProperties can/should
 * be used.)
 * </p>
 * <p>
 * Prefered usage is
 * <code>Configuration.getConfiguration (this).getValue ("myKey")</code>.
 * The result will already have been run through the AliasManager.
 * There is no need to do so again.
 * </p>
 *
 * @see com.top_logic.basic.AliasManager
 * @see com.top_logic.basic.XMLProperties
 * 
 * @deprecated Use {@link TypedConfiguration}.
 *
 * @author    Michael Eriksson
 */
@Deprecated
public abstract class Configuration {
//Notes:
//We currently do not cache in this class. Later testing might
//force a change.

//If no configuration is found for the specified class
//we recursively check the parent. A further extension could
//allow the children to override single properties (as opposed to
//the entire properties).

//The current implementation is (mostly) a wrapper around the XMLProperties.
//It is expected that future implementations will use more flexible input,
//that avoids the one giant file that is currently present. (E.g. one
//file pro class.)

//Further possible improvements are mappings between classes and section
//names, and support for some kind of module mechanism.

	protected Configuration () {
        super ();
    }

    /**
     * Get the configured value for the specified name.
     *
     * @param aName the name; must not be null
     *
     * @return the value; may be null
     *
     * #author Michael Eriksson
     */
	@Deprecated
    public String getValue (String aName) {
        // Caching?
        AliasManager amgr = AliasManager.getInstance ();
        return amgr.replace (this.getRawValue (amgr.replace(aName)));
    }

    /**
     * Equivalent to {@link #getValue(String)}
     * except that a <code>null</code> value is replaced with
     * the specified default before returning.
     *
     * @param aName         the name; must not be null
     * @param aDefaultValue the default value; may be null
     *
     * @return the value; may be null
     *
     * #author Michael Eriksson
     */
	@Deprecated
    public String getValue (String aName, String aDefaultValue) {
        String theValue = this.getValue (aName);

        if (theValue == null) {
            theValue = aDefaultValue;
        }

        return theValue;
    }

    /**
     * Convenience method for {@link ConfigUtil#getInstanceMandatory(Class, String, CharSequence)}.
     */
	@Deprecated
	public Object getInstanceMandatory(String propertyName) throws ConfigurationException {
		return ConfigUtil.getInstanceMandatory(Object.class, propertyName, getValue(propertyName));
	}

	/**
	 * Convenience method for {@link ConfigUtil#getInstanceWithClassDefault(Class, String, CharSequence, Class)}.
	 */
	@Deprecated
	public Object getInstanceWithClassDefault(String propertyName, Class defaultClass) throws ConfigurationException {
		return ConfigUtil.getInstanceWithClassDefault(Object.class, propertyName, getValue(propertyName), defaultClass);
	}
	
	/**
	 * Convenience method for {@link ConfigUtil#getInstanceWithInstanceDefault(Class, String, CharSequence, Object)}.
	 */
	@Deprecated
	public Object getInstanceWithInstanceDefault(String propertyName, Object defaultInstance) throws ConfigurationException {
		return ConfigUtil.getInstanceWithInstanceDefault(Object.class, propertyName, getValue(propertyName), defaultInstance);
	}
	
    /**
     * Convenience method for {@link ConfigUtil#getNewInstanceMandatory(Class, String, CharSequence)}.
     */
	@Deprecated
    public Object getNewInstanceMandatory(String propertyName) throws ConfigurationException {
        return ConfigUtil.getNewInstanceMandatory(Object.class, propertyName, getValue(propertyName));
    }

    /**
     * Convenience method for {@link ConfigUtil#getNewInstanceWithInstanceDefault(Class, String, CharSequence, Object)}.
     */
	@Deprecated
    public Object getNewInstanceWithInstanceDefault(String propertyName, Object defaultInstance) throws ConfigurationException {
        return ConfigUtil.getNewInstanceWithInstanceDefault(Object.class, propertyName, getValue(propertyName), defaultInstance);
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getNewInstanceWithClassDefault(Class, String, CharSequence, Class)}.
     */
	@Deprecated
    public Object getNewInstanceWithClassDefault(String propertyName, Class defaultClass) throws ConfigurationException {
        return ConfigUtil.getNewInstanceWithClassDefault(Object.class, propertyName, getValue(propertyName), defaultClass);
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getSingletonMandatory(Class, String, CharSequence)}.
     */
	@Deprecated
    public Object getSingletonMandatory(String propertyName) throws ConfigurationException {
        return ConfigUtil.getSingletonMandatory(Object.class, propertyName, getValue(propertyName));
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getSingletonWithInstanceDefault(Class, String, CharSequence, Object)}.
     */
	@Deprecated
    public Object getSingletonWithInstanceDefault(String propertyName, Object defaultInstance) throws ConfigurationException {
        return ConfigUtil.getSingletonWithInstanceDefault(Object.class, propertyName, getValue(propertyName), defaultInstance);
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getBooleanValue(String, CharSequence)}.
     */
	@Deprecated
    public boolean getBoolean(String propertyName) throws ConfigurationException {
        return ConfigUtil.getBooleanValue(propertyName, getValue(propertyName));
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getBooleanValue(String, CharSequence, boolean)}.
     */
	@Deprecated
    public boolean getBoolean(String propertyName, boolean defaultValue) throws ConfigurationException {
        return ConfigUtil.getBooleanValue(propertyName, getValue(propertyName), defaultValue);
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getIntValue(String, CharSequence)}.
     */
	@Deprecated
    public int getInteger(String propertyName) throws ConfigurationException {
        return ConfigUtil.getIntValue(propertyName, getValue(propertyName));
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getIntValue(String, CharSequence, int)}.
     */
	@Deprecated
    public int getInteger(String propertyName, int defaultValue) throws ConfigurationException {
        return ConfigUtil.getIntValue(propertyName, getValue(propertyName), defaultValue);
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getLongValue(String, CharSequence)}.
     */
	@Deprecated
    public long getLong(String propertyName) throws ConfigurationException {
    	return ConfigUtil.getLongValue(propertyName, getValue(propertyName));
    }

    /**
     * Convenience method for {@link ConfigUtil#getLongValue(String, CharSequence, long)}.
     */
	@Deprecated
    public long getLong(String propertyName, long defaultValue) throws ConfigurationException {
		return ConfigUtil.getLongValue(propertyName, getValue(propertyName), defaultValue);
	}
	
    /**
     * Convenience method for {@link ConfigUtil#getDoubleValue(String, CharSequence)}.
     */
	@Deprecated
    public double getDouble(String propertyName) throws ConfigurationException {
        return ConfigUtil.getDoubleValue(propertyName, getValue(propertyName));
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getDoubleValue(String, CharSequence, double)}.
     */
	@Deprecated
    public double getDouble(String propertyName, double defaultValue) throws ConfigurationException {
        return ConfigUtil.getDoubleValue(propertyName, getValue(propertyName), defaultValue);
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getEnumValueMandatory(String, CharSequence, Class)}.
     */
	@Deprecated
    public Enum getEnumValue(String propertyName, Class enumClass) throws ConfigurationException {
        return ConfigUtil.getEnumValueMandatory(propertyName, getValue(propertyName), enumClass);
    }
    
    /**
     * Convenience method for {@link ConfigUtil#getEnumValue(String, CharSequence, Class, Enum)}.
     */
	@Deprecated
	public Enum getEnumValue(String propertyName, Class enumClass, Enum defaultValue) throws ConfigurationException {
        return ConfigUtil.getEnumValue(propertyName, getValue(propertyName), enumClass, defaultValue);
	}

	/**
	 * {@link #getNewConfiguredInstance(Class, Class)} without a default class.
	 */
	@Deprecated
	public static Object getNewConfiguredInstance(Class baseClass) throws ConfigurationException {
		return getNewConfiguredInstance(baseClass, null);
	}
	
	/**
	 * {@link #getNewConfiguredInstance(Class, String, Class)} with {@link ConfigUtil#DEFAULT_CLASS_PROPERTY}.
	 */
	@Deprecated
    public static Object getNewConfiguredInstance(Class baseClass, Class defaultClass) throws ConfigurationException {
    	return getNewConfiguredInstance(baseClass, ConfigUtil.DEFAULT_CLASS_PROPERTY, defaultClass);
    }

	/**
	 * Create a new configured instance using a constructor with a single
	 * {@link Properties} argument.
	 * 
	 * @param baseClass
	 *        The base class that defines the configuration section name.
	 * @param classPropertyName
	 *        The property that configures the concrete class.
	 * @param defaultClass
	 *        The default class, if no configuration value for the class
	 *        property is given. If no default class is given, the class
	 *        property is mandatory.
	 * @return a new configured instance as subclass of the given base class.
	 */
	@Deprecated
    public static Object getNewConfiguredInstance(Class baseClass, String classPropertyName, Class defaultClass) throws ConfigurationException {
		Properties configuration = getConfiguration(baseClass).getProperties();
		return ConfigUtil.getConfiguredInstance(baseClass, configuration, classPropertyName, defaultClass);
	}

	/**
     * Get the value pre-AliasManager.
     *
     * @param aName the name; must not be null
     *
     * @return the value; may be null
     *
     * #author Michael Eriksson
     */
	@Deprecated
    protected abstract String getRawValue (String aName);

    /**
     * Special case that is iterable.
     * <p>
     * Note: We have deliberately not put this class "on its own"
     * to disencourage use. (It is valid but should not be done
     * unless actually needed.)
     * </p>
     *
     * #author Michael Eriksson
     */
    public static class IterableConfiguration extends Configuration {
        
        private Properties rawValues;

        public IterableConfiguration (Properties someRawValues) {
            this.rawValues = someRawValues;
        }
        
        /**
         * Forward to rawValues.
         */
        @Override
		public String toString() {
            return rawValues.toString();
        }

        @Override
		protected String getRawValue (String aName) {
            return this.rawValues.getProperty (aName);
        }

        /**
         * Get a Set of the available names.
         *
         * @return the names; never null
         *
         * #author Michael Eriksson
         */
        public Set getNames () {
            return this.rawValues.keySet ();
        }

        /**
         * Get the contents of the instance as a Properties.
         * <p>
         * As of current implementation complexyity will be n*m, with n the number
         * of properties and m the number of aliases in the system
         * </p>
         *
         * @return the properties; never null
         * #author Michael Eriksson
         */
        public Properties getProperties () {
            
            Properties   theProperties = new Properties ();
            Iterator     theEntries = this.rawValues.entrySet().iterator ();
            AliasManager amgr       = AliasManager.getInstance ();
            while (theEntries.hasNext ()) {
                Map.Entry theEntry = (Map.Entry) theEntries.next ();
                Object theName = theEntry.getKey();
                String theProp = (String) theEntry.getValue();
                       theProp = amgr.replace (theProp);

                theProperties.put(theName, theProp);
            }

            return theProperties;
        }
    }

    /**
     * Get the configuration matching the specified Class.
     *
     * @param aConfiguredClass the Class; must not be null
     *
     * @return the configuration; never null
     *
     * #author Michael Eriksson
     */
	@Deprecated
    public static IterableConfiguration getConfiguration(Class<?> aConfiguredClass) {
        if (aConfiguredClass == null) {
            throw new NullPointerException("The class to be configured must not be null.");
        }

        final Properties theProperties = getProperties (aConfiguredClass);

        return new IterableConfiguration (theProperties);
    }

    /**
     * Get the Properties matching the specified Class.
     *
     * @param aConfiguredClass the Class; must not be null
     *
     * @return the Properties; never null
     *
     * #author Michael Eriksson
     * 
     * @throws ConfigurationError If no configuration is set up.
     */
	@Deprecated
    private static Properties getProperties (Class aConfiguredClass) throws ConfigurationError {
        //Note: This method is an "implementation detail". Do _not_
        //make it none-private.
        Properties theProperties =
            getXMLProperties ().getProperties (aConfiguredClass);

        return theProperties;
    }

    /**
     * Get the configuration matching the specified name.
     * <p>
     * This method must only be used when there is no relevant
     * Class or Object that can be used. One example would be
     * the automatic setup of several instances of the same
     * class with different properties (provided as a constructor
     * argument). Cf. with e.g the DataSourceAdaptor/DataAccessService
     * setup in <i>TopLogic</i> 4.1. It should never be used when you
     * want give your configuration a fancy name or when accessing
     * the configuration of another class.
     * </p>
     *
     * @param aName the name of the properties; must not be null
     *
     * @return the configuration; never null
     *
     * #author Michael Eriksson
     */
	@Deprecated
    public static IterableConfiguration getConfigurationByName (String aName) {
        if (aName == null) {
            throw new NullPointerException ("aName");
        }

        final Properties theProperties =
            getXMLProperties ().getProperties (aName);
        return new IterableConfiguration (theProperties);
    }

    /**
     * Get a Configuration adapting the specified properties.
     * <p>
     * This method is intended for transitional use only.
     * It should ease the switch from XMLProperties.
     * </p>
     *
     * @param aProperties the Properties; must not be null
     *
     * @return the configuration; never null
     *
     * @deprecated Use one of the other methods instead
     *
     * #author Michael Eriksson
     */
    /*
    public static IterableConfiguration getPropertiesConfiguration
                                                (Properties aProperties) {
        if (aProperties == null) {
            throw new NullPointerException ("aProperties");
        }

        return new IterableConfiguration (aProperties);
    }
    */
    
    /**
     * This utility method has as only purpose to
     * reduce the number of deprecation warnings in
     * Configuration classes. It must never be used
     * anywhere else.
     *
     * @return XMLProperties.getInstance ()
     *
     *
     * #author Michael Eriksson
     * 
     * @throws ConfigurationError If no configuration is set up.
     */
	@Deprecated
    protected static XMLProperties getXMLProperties() throws ConfigurationError {
        return XMLProperties.getInstance();
    }

}
