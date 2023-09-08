/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.basic.config.I18NConstants.*;
import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static java.util.Collections.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;

/**
 * Utilities for parsing configuration values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigUtil {

	/**
	 * The default class property for
	 * {@link #getConfiguredInstance(Class, Properties, String, Class)}.
	 */
	public static final String DEFAULT_CLASS_PROPERTY = "class";

	/**
	 * {@link Constructor} signature for instantiating legacy configured instances.
	 */
	public static final Class<?>[] CONFIG_CONSTRUCTOR_SIGNATURE = {Properties.class};

	/** 
	 * Method name of singleton lookup methods.  
	 */
	static final String GET_INSTANCE_METHOD_NAME = "getInstance";
	
	/** 
	 * Singleton instance field name. 
	 */
	static final String SINGLETON_FIELD_NAME     = "INSTANCE";

	/**
	 * Empty method signature for for reflective method lookup.  
	 */
	static final Class<?> [] NO_PARAM_SIGNATURE  = new Class[0];
	
	/**
	 * Empty argument list for reflective method invocation. 
	 */
	static final Object[] NO_ARGUMENTS        = new Object[0];

	/**
	 * Combination of {@link Modifier}s that denote that a declaration is
	 * {@link Modifier#PUBLIC} and {@link Modifier#STATIC}.
	 */
	static final int PUBLIC_STATIC = Modifier.PUBLIC | Modifier.STATIC;

	/**
	 * Looks up a configured class by name.
	 * 
	 * @param expectedType
	 *        a supertype of the expected class
	 * 
	 * @throws ConfigurationException
	 *         if no class name is given, or the configured class is not found.
	 */
	public static <T> Class<? extends T> getClassForNameMandatory(Class<T> expectedType, Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getClassForNameMandatory(expectedType, propertyName, value(configuration, propertyName));
	}
	
	/**
	 * Looks up a configured class by name.
	 * 
	 * @param expectedType
	 *        a supertype of the expected class
	 * 
	 * @throws ConfigurationException
	 *         if no class name is given, or the configured class is not found.
	 */
	public static <T> Class<? extends T> getClassForNameMandatory(Class<T> expectedType, String propertyName, CharSequence className) throws ConfigurationException {
		checkNotEmpty(propertyName, className);
		return lookupClassForName(expectedType, propertyName, className);
	}
	
	/**
	 * Looks up a configured class by name.
	 * 
	 * <p>
	 * If no class is given, the default class is returned.
	 * </p>
	 * @param expectedType 
	 *        a supertype of the expected class
	 * @param defaultClass
	 *        The class to return, if no class name is given. If the default is
	 *        <code>null</code>, the result may also be <code>null</code>.
	 * 
	 * @return The configured class, or the given default class, if no
	 *         configuration is givne. May only be <code>null</code>, if the
	 *         default class is <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the configured class is not found.
	 */
	public static <T> Class<? extends T> getClassForName(Class<T> expectedType, Map<?, ?> configuration, String className, Class<? extends T> defaultClass) throws ConfigurationException {
		return getClassForName(expectedType, className, value(configuration, className), defaultClass);
	}
	
	/**
	 * Looks up a configured class by name.
	 * 
	 * <p>
	 * If no class is given, the default class is returned.
	 * </p>
	 * @param expectedType 
	 *        a supertype of the expected class
	 * @param defaultClass
	 *        The class to return, if no class name is given. If the default is
	 *        <code>null</code>, the result may also be <code>null</code>.
	 * 
	 * @return The configured class, or the given default class, if no
	 *         configuration is givne. May only be <code>null</code>, if the
	 *         default class is <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the configured class is not found.
	 */
	public static <T> Class<? extends T> getClassForName(Class<T> expectedType, String propertyName, CharSequence className, Class<? extends T> defaultClass) throws ConfigurationException {
		if (StringServices.isEmpty(className)) {
			return defaultClass;
		} 
		
		return lookupClassForName(expectedType, propertyName, className);
	}
	
	/**
	 * Instantiates the configured class expecting a default constructor (public
	 * no-argument).
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param configuration The properties from which the value is read for the given property name.
	 * @param propertyName The name of the configuration property to use.
	 * 
	 * @return a new instance of the configured class.
	 * 
	 * @throws ConfigurationException
	 *         if no class is configured, or instantiation fails.
	 */
	public static <T> T getNewInstanceMandatory(Class<T> expectedType, Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getNewInstanceMandatory(expectedType, propertyName, value(configuration, propertyName));
	}

	/**
	 * Instantiates the configured class expecting a default constructor (public
	 * no-argument).
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * 
	 * @return a new instance of the configured class.
	 * 
	 * @throws ConfigurationException
	 *         if no class is configured, or instantiation fails.
	 */
	public static <T> T getNewInstanceMandatory(Class<T> expectedType, String propertyName, CharSequence className) throws ConfigurationException {
		return getNewInstanceWithClassDefault(expectedType, propertyName, className, null);
	}

	/**
	 * Instantiates the configured class expecting a default constructor (public no-argument).
	 * 
	 * @param type
	 *        Is not allowed to be null.
	 * 
	 * @return a new instance of the configured class.
	 * 
	 * @throws ConfigurationException
	 *         if no class is configured, or instantiation fails.
	 */
	public static <T> T getNewInstanceMandatory(String propertyName, Class<T> type) throws ConfigurationException {
		return newInstance(propertyName, type);
	}

	/**
	 * Instantiates the configured class expecting a default constructor (public
	 * no-argument).
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate, if no configuration is given. May be
	 *        <code>null</code>. In that case, a configuration is required.
	 * 
	 * @return a new instance of the configured class, or the given default
	 *         class, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no default class and no configuration
	 *         is given.
	 */
	public static <T> T getNewInstanceWithClassDefault(Class<T> expectedType, Map<?, ?> configuration, String propertyName, Class<? extends T> defaultClass) throws ConfigurationException {
		return getNewInstanceWithClassDefault(expectedType, propertyName, value(configuration, propertyName), defaultClass);
	}

	/**
	 * Instantiates the configured class expecting a default constructor (public
	 * no-argument).
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate, if no configuration is given. May be
	 *        <code>null</code>. In that case, a configuration is required.
	 * 
	 * @return a new instance of the configured class, or the given default
	 *         class, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no default class and no configuration
	 *         is given.
	 */
	public static <T> T getNewInstanceWithClassDefault(Class<T> expectedType, String propertyName, CharSequence className, Class<? extends T> defaultClass) throws ConfigurationException {
		Class<? extends T> resultClass = lookupClassForName(expectedType, propertyName, className, defaultClass);
		return newInstance(propertyName, resultClass);
	}

	/**
	 * Instantiates the configured class expecting a default constructor (public no-argument).
	 * 
	 * @param type
	 *        Is allowed to be null, if the defaultClass is not null.
	 * @param defaultClass
	 *        The class to instantiate, if the parameter "type" is null. May be <code>null</code>.
	 *        In that case, the parameter "type" is not allowed to be null.
	 * 
	 * @return a new instance of the configured class, or the given default class, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no default class and no configuration is given.
	 */
	public static <T> T getNewInstanceWithClassDefault(String propertyName, Class<T> type,
			Class<? extends T> defaultClass) throws ConfigurationException {
		Class<? extends T> typeNonNull = (type == null) ? defaultClass : type;
		return newInstance(propertyName, typeNonNull);
	}

	/**
	 * Instantiates the configured class expecting a default constructor (public
	 * no-argument).
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param defaultInstance
	 *        The value to return, if no configuration is given. May be
	 *        <code>null</code>. In that case, the result is also
	 *        <code>null</code>, if no configuration is given.
	 * 
	 * @return a new instance of the configured class, or the given default
	 *         value, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails.
	 */
	public static <T> T getNewInstanceWithInstanceDefault(Class<T> expectedType, Map<?, ?> configuration, String propertyName, T defaultInstance) throws ConfigurationException {
		return getNewInstanceWithInstanceDefault(expectedType, propertyName, value(configuration, propertyName), defaultInstance);
	}
	
	/**
	 * Instantiates the configured class expecting a default constructor (public
	 * no-argument).
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param defaultInstance
	 *        The value to return, if no configuration is given. May be
	 *        <code>null</code>. In that case, the result is also
	 *        <code>null</code>, if no configuration is given.
	 * 
	 * @return a new instance of the configured class, or the given default
	 *         value, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails.
	 */
	public static <T> T getNewInstanceWithInstanceDefault(Class<T> expectedType, String propertyName, CharSequence className, T defaultInstance) throws ConfigurationException {
	    if (StringServices.isEmpty(className)) {
	        return defaultInstance;
	    } 

	    Class<? extends T> resultClass = lookupClassForName(expectedType, propertyName, className);
		return newInstance(propertyName, resultClass);
	}

	/**
	 * Instantiates the configured class expecting a default constructor (public no-argument).
	 * 
	 * @param type
	 *        Is allowed to be null, if the defaultInstance is not null.
	 * @param defaultInstance
	 *        The result, if the parameter "type" is null. May be <code>null</code>. In that case,
	 *        the parameter "type" is not allowed to be null.
	 * 
	 * @return a new instance of the configured class, or the given default value, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails.
	 */
	public static <T> T getNewInstanceWithInstanceDefault(String propertyName, Class<T> type, T defaultInstance)
			throws ConfigurationException {
		if (type == null) {
			return defaultInstance;
		}
		return newInstance(propertyName, type);
	}

	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new
	 * instance using the default constructor (public no-argument), if the
	 * configured class is not a singleton class.
	 * 
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * 
	 * @return an instance of the configured class, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration is given.
	 */
	public static <T> T getInstanceMandatory(Class<T> expectedType, Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getInstanceMandatory(expectedType, propertyName, value(configuration, propertyName));
	}
	
	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new
	 * instance using the default constructor (public no-argument), if the
	 * configured class is not a singleton class.
	 * 
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * 
	 * @return an instance of the configured class, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration is given.
	 */
	public static <T> T getInstanceMandatory(Class<T> expectedType, String propertyName, CharSequence className) throws ConfigurationException {
		return getInstanceWithClassDefault(expectedType, propertyName, className, null);
	}

	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new instance using the
	 * default constructor (public no-argument), if the configured class is not a singleton class.
	 * 
	 * @param type
	 *        Is not allowed to be null.
	 * 
	 * @return an instance of the configured class, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration is given.
	 */
	public static <T> T getInstanceMandatory(String propertyName, Class<T> type) throws ConfigurationException {
		return lookupInstance(type, propertyName, type);
	}

	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new
	 * instance using the default constructor (public no-argument), if the
	 * configured class is not a singleton class.
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate or look up the singleton from, if no
	 *        configuration is given. May be <code>null</code>. In that case, a
	 *        configuration is required.
	 * 
	 * @return an instance of the configured class, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no default class and no configuration
	 *         is given.
	 */
	public static <T> T getInstanceWithClassDefault(Class<T> expectedType, Map<?, ?> configuration, String propertyName, Class<? extends T> defaultClass) throws ConfigurationException {
		return getInstanceWithClassDefault(expectedType, propertyName, value(configuration, propertyName), defaultClass);
	}
	
	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new
	 * instance using the default constructor (public no-argument), if the
	 * configured class is not a singleton class.
	 * @param expectedClass 
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate or look up the singleton from, if no
	 *        configuration is given. May be <code>null</code>. In that case, a
	 *        configuration is required.
	 * 
	 * @return an instance of the configured class, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no default class and no configuration
	 *         is given.
	 */
	public static <T> T getInstanceWithClassDefault(Class<T> expectedClass, String propertyName, CharSequence className, Class<? extends T> defaultClass) throws ConfigurationException {
		Class<? extends T> resultClass = lookupClassForName(expectedClass, propertyName, className, defaultClass);
		
		return lookupInstance(expectedClass, propertyName, resultClass);
	}

	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new instance using the
	 * default constructor (public no-argument), if the configured class is not a singleton class.
	 * 
	 * @param type
	 *        Is allowed to be null, if the defaultClass is not null.
	 * @param defaultClass
	 *        The class to instantiate or look up the singleton from, if the parameter "type" is
	 *        null. May be <code>null</code>. In that case, the parameter "type" is not allowed to
	 *        be null.
	 * 
	 * @return an instance of the configured class, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no default class and no configuration is given.
	 */
	public static <T> T getInstanceWithClassDefault(String propertyName, Class<T> type, Class<? extends T> defaultClass)
			throws ConfigurationException {
		Class<? extends T> typeNonNull = (type == null) ? defaultClass : type;
		return lookupInstance(typeNonNull, propertyName, typeNonNull);
	}

	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new
	 * instance using the default constructor (public no-argument), if the
	 * configured class is not a singleton class.
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * @param defaultInstance
	 *        The value to return, if no configuration is given. May be
	 *        <code>null</code>. In that case, the result is also
	 *        <code>null</code>, if no configuration is given.
	 * 
	 * @return an instance of the configured class, or the given default value,
	 *         if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails.
	 */
	public static <T> T getInstanceWithInstanceDefault(Class<T> expectedType, Map<?, ?> configuration, String propertyName, T defaultInstance) throws ConfigurationException {
		return getInstanceWithInstanceDefault(expectedType, propertyName, value(configuration, propertyName), defaultInstance);
	}

	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new
	 * instance using the default constructor (public no-argument), if the
	 * configured class is not a singleton class.
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param defaultInstance
	 *        The value to return, if no configuration is given. May be
	 *        <code>null</code>. In that case, the result is also
	 *        <code>null</code>, if no configuration is given.
	 * 
	 * @return an instance of the configured class, or the given default value,
	 *         if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails.
	 */
	public static <T> T getInstanceWithInstanceDefault(Class<T> expectedType, String propertyName, CharSequence className, T defaultInstance) throws ConfigurationException {
		if (StringServices.isEmpty(className)) {
			return defaultInstance;
		} 
		
		Class<? extends T> resultClass = lookupClassForName(expectedType, propertyName, className);
		
		return lookupInstance(expectedType, propertyName, resultClass);
	}

	/**
	 * Looks up the configured class as singleton, or tries to instantiate a new instance using the
	 * default constructor (public no-argument), if the configured class is not a singleton class.
	 * 
	 * @param type
	 *        Is allowed to be null, if the defaultInstance is not null.
	 * @param defaultInstance
	 *        The result, if the parameter "type" is null. May be <code>null</code>. In that case,
	 *        the parameter "type" is not allowed to be null.
	 * 
	 * @return an instance of the configured class, or the given default value, if no configuration
	 *         is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails.
	 */
	public static <T> T getInstanceWithInstanceDefault(String propertyName, Class<T> type, T defaultInstance)
			throws ConfigurationException {
		if (type == null) {
			return defaultInstance;
		}
		return lookupInstance(type, propertyName, type);
	}
	
	/**
	 * Instantiates the configured class, expecting a public constructor with
	 * the given signature, or the default constructor (public no-argument).
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate, if if no configuration is given. May be
	 *        <code>null</code>. In that case, a configuration is required.
	 * 
	 * @return a new instance of the configured class, or the given default
	 *         class, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration and no default class
	 *         is given.
	 */
	public static <T> T getNewInstance(Class<T> expectedType, Map<?, ?> configuration, String propertyName, Class<? extends T> defaultClass, Class<?>[] signature, Object... arguments) throws ConfigurationException {
		return getNewInstance(expectedType, propertyName, value(configuration, propertyName), defaultClass, signature, arguments);
	}

	/**
	 * Instantiates the configured class, expecting the default constructor (public no-argument).
	 * 
	 * <p>
	 * Use {@link #getNewInstanceMandatory(String, Class)} instead, if the class has already been
	 * resolved. The "defaultClass" parameter is not used in that case, as it is only a default for
	 * the case that the class name is empty.
	 * </p>
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate, if if no configuration is given. May be <code>null</code>.
	 *        In that case, a configuration is required.
	 * 
	 * @return a new instance of the configured class, or the given default class, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration and no default class is given.
	 */
	public static <T> T getNewInstance(Class<T> expectedType, String propertyName, CharSequence className,
			Class<? extends T> defaultClass) throws ConfigurationException {
		Class<? extends T> resultClass = lookupClassForName(expectedType, propertyName, className, defaultClass);

		return newInstance(propertyName, resultClass);
	}
	/**
	 * Instantiates the configured class, expecting a public constructor with
	 * the given signature, or the default constructor (public no-argument).
	 * 
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate, if if no configuration is given. May be
	 *        <code>null</code>. In that case, a configuration is required.
	 * 
	 * @return a new instance of the configured class, or the given default
	 *         class, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration and no default class
	 *         is given.
	 */
	public static <T> T getNewInstance(Class<T> expectedType, String propertyName, CharSequence className, Class<? extends T> defaultClass, Class<?>[] signature, Object... arguments) throws ConfigurationException {
		Class<? extends T> resultClass = lookupClassForName(expectedType, propertyName, className, defaultClass);
		
		return newInstanceWithFallback(propertyName, resultClass, signature, arguments);
	}

	/**
	 * Instantiates the configured class, expecting a public constructor with the given signature,
	 * or the default constructor (public no-argument).
	 * 
	 * @param type
	 *        Is not allowed to be null.
	 * 
	 * @return a new instance of the configured class, or the given default class, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration and no default class is given.
	 */
	public static <T> T getNewInstance(String propertyName, Class<T> type, Class<?>[] signature, Object... arguments)
			throws ConfigurationException {
		return newInstanceWithFallback(propertyName, type, signature, arguments);
	}

	private static <T> T newInstanceWithFallback(String propertyName, Class<? extends T> resultClass,
			Class<?>[] signature, Object... arguments) throws ConfigurationException {
		try {
			return newInstance(propertyName, resultClass, signature, arguments);
		} catch (NoSuchMethodException e) {
			return newInstance(propertyName, resultClass);
		}
	}

	/**
	 * Instantiates the configured class, expecting a public constructor with
	 * the given signature, or the default constructor (public no-argument).
	 * 
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * @param defaultClass
	 *        The class to instantiate, if if no configuration is given. May be
	 *        <code>null</code>. In that case, a configuration is required.
	 * 
	 * @return a new instance of the configured class, or the given default
	 *         class, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration and no default class
	 *         is given.
	 */
	public static <T> T getInstance(Class<T> expectedType, String propertyName, CharSequence className, Class<? extends T> defaultClass, Class<?>[] signature, Object... arguments) throws ConfigurationException {
		Class<? extends T> configuredClass = lookupClassForName(expectedType, propertyName, className, defaultClass);
		
		return getInstance(expectedType, propertyName, configuredClass, signature, arguments);
	}

	/**
	 * Instantiates the configured class, expecting a public constructor with the given signature,
	 * or the default constructor (public no-argument).
	 * 
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * @param configuredClass
	 *        The class to instantiate.
	 * 
	 * @return a new instance of the configured class, or the given default class, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If instantiation fails, or no configuration and no default class is given.
	 */
	public static <T> T getInstance(Class<T> expectedType, String propertyName, Class<?> configuredClass,
			Class<?>[] signature, Object... arguments) throws ConfigurationException {
		try {
			return newInstance(propertyName, dynamicCastType(expectedType, propertyName, configuredClass), signature,
				arguments);
		} catch (NoSuchMethodException e) {
			return lookupInstance(expectedType, propertyName, configuredClass);
		}
	}
	
	/**
	 * Looks up the configured singleton class.
	 * @param expectedType
	 *        a supertype of the class of the expected instance
	 * 
	 * @return the configured singleton instance, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if no configuration is given, or the configured class is not a
	 *         singleton class.
	 */
	public static <T> T getSingletonMandatory(Class<T> expectedType, Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getSingletonMandatory(expectedType, propertyName, value(configuration, propertyName));
	}

	/**
	 * Looks up the configured singleton class.
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * 
	 * @return the configured singleton instance, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if no configuration is given, or the configured class is not a
	 *         singleton class.
	 */
	public static <T> T getSingletonMandatory(Class<T> expectedType, String propertyName, CharSequence className) throws ConfigurationException {
		return getSingletonWithClassDefault(expectedType, propertyName, className, null);
	}

	/**
	 * Looks up the configured singleton class.
	 * 
	 * @param singletonClass
	 *        Is not allowed to be null.
	 * 
	 * @return the configured singleton instance, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if no configuration is given, or the configured class is not a singleton class.
	 */
	public static <T> T getSingletonMandatory(String propertyName, Class<T> singletonClass)
			throws ConfigurationException {
		return lookupSingleton(singletonClass, propertyName, singletonClass, null);
	}

	/**
	 * Looks up the configured singleton class, or the default singleton class,
	 * if no configuration is given.
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * 
	 * @return the configured singleton instance, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the configured class is not a singleton class.
	 */
	public static <T> T getSingletonWithClassDefault(Class<T> expectedType, Map<?, ?> configuration, String propertyName, Class<? extends T> defaultClass) throws ConfigurationException {
		return getSingletonWithClassDefault(expectedType, propertyName, value(configuration, propertyName), defaultClass);
	}

	/**
	 * Looks up the configured singleton class, or the default singleton class, if no configuration
	 * is given.
	 * 
	 * <p>
	 * Use {@link #getSingletonMandatory(String, Class)} instead, if the class has already been
	 * resolved. The "defaultClass" parameter is not used in that case, as it is only a default for
	 * the case that the class name is empty.
	 * </p>
	 * 
	 * @param expectedClass
	 *        a supertype of the class of the expected instance
	 * 
	 * @return the configured singleton instance, never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         if the configured class is not a singleton class.
	 */
	public static <T> T getSingletonWithClassDefault(Class<T> expectedClass, String propertyName, CharSequence className, Class<? extends T> defaultClass) throws ConfigurationException {
		Class<? extends T> singletonClass = lookupClassForName(expectedClass, propertyName, className, defaultClass);
		return lookupSingleton(expectedClass, propertyName, singletonClass, defaultClass);
    }
	
	/**
	 * Looks up the configured singleton class, or the given default instance.
	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * 
	 * @return the configured singleton instance, only <code>null</code>, if
	 *         the default value is <code>null</code> and no configuration is
	 *         given.
	 * 
	 * @throws ConfigurationException
	 *         if the configured class is not a singleton class.
	 */
	public static <T> T getSingletonWithInstanceDefault(Class<T> expectedType, Map<?, ?> configuration, String propertyName, T defaultInstance) throws ConfigurationException {
		return getSingletonWithInstanceDefault(expectedType, propertyName, value(configuration, propertyName), defaultInstance);
	}

	/**
	 * Looks up the configured singleton class, or the given default instance.

	 * @param expectedType 
	 *        a supertype of the class of the expected instance
	 * 
	 * @return the configured singleton instance, only <code>null</code>, if
	 *         the default value is <code>null</code> and no configuration is
	 *         given.
	 * 
	 * @throws ConfigurationException
	 *         if the configured class is not a singleton class.
	 */
	public static <T> T getSingletonWithInstanceDefault(Class<T> expectedType, String propertyName, CharSequence className, T defaultInstance) throws ConfigurationException {
		if (StringServices.isEmpty(className)) {
			return defaultInstance;
		}
	
		Class<? extends T> singletonClass = lookupClassForName(expectedType, propertyName, className);
		return lookupSingleton(expectedType, propertyName, singletonClass, null);
	}
	
	// Primitive value parsers.

	// Double
	
	/**
	 * Parses a configured double value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Double getDouble(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getDouble(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured double value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Double getDouble(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Double.valueOf(getDoubleValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured double value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         double value.
	 */
	public static double getDoubleValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getDoubleValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured double value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         double value.
	 */
	public static double getDoubleValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseDoubleValue(propertyName, propertyValue);
	}

	/**
	 * Parses a configured double value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a double value.
	 */
	public static double getDoubleValue(Map<?, ?> configuration, String propertyName, double defaultValue) throws ConfigurationException {
		return getDoubleValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured double value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a double value.
	 */
	public static double getDoubleValue(String propertyName, CharSequence propertyValue, double defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseDoubleValue(propertyName, propertyValue);
	}
	
	private static double parseDoubleValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		try {
			return Double.parseDouble(propertyValue.toString());
		} catch (NumberFormatException ex) {
			throw new ConfigurationException(ERROR_DOUBLE_VALUE_EXPECTED, propertyName, propertyValue, ex);
		}
	}
	
	// Integer

	/**
	 * Parses a configured integer value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Integer getInteger(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getInteger(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured integer value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Integer getInteger(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Integer.valueOf(getIntValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured integer value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not an
	 *         integer value.
	 */
	public static int getIntValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getIntValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured integer value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not an
	 *         integer value.
	 */
	public static int getIntValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseIntValue(propertyName, propertyValue);
	}

	/**
	 * Parses a configured integer value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a integer value.
	 */
	public static int getIntValue(Map<?, ?> configuration, String propertyName, int defaultValue) throws ConfigurationException {
		return getIntValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured integer value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a integer value.
	 */
	public static int getIntValue(String propertyName, CharSequence propertyValue, int defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseIntValue(propertyName, propertyValue);
	}
	
	private static int parseIntValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		try {
			return Integer.parseInt(propertyValue.toString());
		} catch (NumberFormatException ex) {
			throw new ConfigurationException(ERROR_INTEGER_EXPECTED, propertyName, propertyValue, ex);
		}
	}
	
	// Long

	/**
	 * Parses a configured long value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Long getLong(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getLong(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured long value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Long getLong(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Long.valueOf(getLongValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured long value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         long value.
	 */
	public static long getLongValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getLongValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured long value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         long value.
	 */
	public static long getLongValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseLong(propertyName, propertyValue);
	}
	
	/**
	 * Parses a configured long value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a long value.
	 */
	public static long getLongValue(Map<?, ?> configuration, String propertyName, long defaultValue) throws ConfigurationException {
		return getLongValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured long value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a long value.
	 */
	public static long getLongValue(String propertyName, CharSequence propertyValue, long defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseLong(propertyName, propertyValue);
	}

	private static long parseLong(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		long result;
		try {
			result = Long.parseLong(propertyValue.toString());
		} catch (NumberFormatException ex) {
			throw new ConfigurationException(ERROR_LONG_EXPECTED, propertyName, propertyValue, ex);
		}
		return result;
	}
	
	// Float

	/**
	 * Parses a configured float value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Float getFloat(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getFloat(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured float value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Float getFloat(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Float.valueOf(getFloatValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured float value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         float value.
	 */
	public static float getFloatValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getFloatValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured float value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         float value.
	 */
	public static float getFloatValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseFloatValue(propertyName, propertyValue);
	}
	
	/**
	 * Parses a configured float value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a float value.
	 */
	public static float getFloatValue(Map<?, ?> configuration, String propertyName, float defaultValue) throws ConfigurationException {
		return getFloatValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured float value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a float value.
	 */
	public static float getFloatValue(String propertyName, CharSequence propertyValue, float defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseFloatValue(propertyName, propertyValue);
	}

	private static float parseFloatValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		try {
			return Float.parseFloat(propertyValue.toString());
		} catch (NumberFormatException ex) {
			throw new ConfigurationException(ERROR_FLOAT_EXPECTED, propertyName, propertyValue, ex);
		}
	}

	// Byte

	/**
	 * Parses a configured byte value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Byte getByte(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getByte(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured byte value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Byte getByte(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Byte.valueOf(getByteValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured byte value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         byte value.
	 */
	public static byte getByteValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getByteValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured byte value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         byte value.
	 */
	public static byte getByteValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseByteValue(propertyName, propertyValue);
	}
	
	/**
	 * Parses a configured byte value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a byte value.
	 */
	public static byte getByteValue(Map<?, ?> configuration, String propertyName, byte defaultValue) throws ConfigurationException {
		return getByteValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured byte value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a byte value.
	 */
	public static byte getByteValue(String propertyName, CharSequence propertyValue, byte defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseByteValue(propertyName, propertyValue);
	}

	private static byte parseByteValue(String propertyName,
			CharSequence propertyValue) throws ConfigurationException {
		try {
			return Byte.parseByte(propertyValue.toString());
		} catch (NumberFormatException ex) {
			throw new ConfigurationException(ERROR_BYTE_EXPECTED, propertyName, propertyValue, ex);
		}
	}

	// Short

	/**
	 * Parses a configured short value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Short getShort(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getShort(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured short value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Short getShort(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Short.valueOf(getShortValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured short value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         short value.
	 */
	public static short getShortValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getShortValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured short value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         short value.
	 */
	public static short getShortValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseShortValue(propertyName, propertyValue);
	}
	
	/**
	 * Parses a configured short value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a short value.
	 */
	public static short getShortValue(Map<?, ?> configuration, String propertyName, short defaultValue) throws ConfigurationException {
		return getShortValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured short value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a short value.
	 */
	public static short getShortValue(String propertyName, CharSequence propertyValue, short defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseShortValue(propertyName, propertyValue);
	}

	private static short parseShortValue(String propertyName,
			CharSequence propertyValue) throws ConfigurationException {
		try {
			return Short.parseShort(propertyValue.toString());
		} catch (NumberFormatException ex) {
			throw new ConfigurationException(ERROR_SHORT_EXPECTED, propertyName, propertyValue, ex);
		}
	}

	// Char

	/**
	 * Parses a configured character value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Character getCharacter(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getCharacter(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured character value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Character getCharacter(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Character.valueOf(getCharValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured character value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         character value.
	 */
	public static char getCharValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getCharValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured character value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not a
	 *         character value.
	 */
	public static char getCharValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseCharValue(propertyName, propertyValue);
	}

	/**
	 * Parses a configured character value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a character value.
	 */
	public static char getCharValue(Map<?, ?> configuration, String propertyName, char defaultValue) throws ConfigurationException {
		return getCharValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured character value.
	 * 
	 * @return the configured value, or the given default value, if there is no
	 *         configuration.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not a character value.
	 */
	public static char getCharValue(String propertyName, CharSequence propertyValue, char defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseCharValue(propertyName, propertyValue);
	}
	
	private static char parseCharValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (propertyValue.length() > 1) {
			throw new ConfigurationException(ERROR_CHARACTER_EXPECTED, propertyName, propertyValue);
		}
		return propertyValue.charAt(0);
	}

	// Boolean
	
	/**
	 * Parses a configured string value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Boolean getBoolean(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getBoolean(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured string value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static Boolean getBoolean(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return Boolean.valueOf(parseBooleanValue(propertyName, propertyValue));
	}

	/**
	 * Parses a configured boolean value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not
	 *         "true" or "false".
	 */
	public static boolean getBooleanValue(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getBooleanValue(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured boolean value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not
	 *         "true" or "false".
	 */
	public static boolean getBooleanValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return parseBooleanValue(propertyName, propertyValue);
	}
	
	/**
	 * Parses a configured boolean value.
	 * 
	 * @return the configured value, or the given default value, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not "true" or "false".
	 */
	public static boolean getBooleanValue(Map<?, ?> configuration, String propertyName, boolean defaultValue) throws ConfigurationException {
		return getBooleanValue(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured boolean value.
	 * 
	 * @return the configured value, or the given default value, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not "true" or "false".
	 */
	public static boolean getBooleanValue(String propertyName, CharSequence propertyValue, boolean defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return parseBooleanValue(propertyName, propertyValue);
	}

	private static boolean parseBooleanValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		// Note: equals() is not defined on CharSequence in general.
		if (equalsCharSequence(propertyValue, "true")) {
			return true;
		}
		if (equalsCharSequence(propertyValue, "false")) {
			return false;
		}
		throw new ConfigurationException(ERROR_BOOLEAN_EXPECTED, propertyName, propertyValue);
	}

	// String
	
	/**
	 * Parses a configured string value.
	 * 
	 * @return the configured value, or <code>null</code>, if no
	 *         configuration is given.
	 */
	public static String getString(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getString(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured string value.
	 * 
	 * @return the configured value, or <code>null</code>, if no configuration is given.
	 * @throws ConfigurationException
	 *         Declared for consistency with other methods.
	 */
	public static String getString(@SuppressWarnings("unused") String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return null;
		}
		return propertyValue.toString();
	}

	/**
	 * Parses a configured string value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not
	 *         "true" or "false".
	 */
	public static String getStringMandatory(Map<?, ?> configuration, String propertyName) throws ConfigurationException {
		return getStringMandatory(propertyName, value(configuration, propertyName));
	}

	/**
	 * Parses a configured string value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not
	 *         "true" or "false".
	 */
	public static String getStringMandatory(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		checkNotEmpty(propertyName, propertyValue);
		return propertyValue.toString();
	}
	
	/**
	 * Parses a configured string value.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration is given, or the configured value is not
	 *         "true" or "false".
	 */
	public static String getString(Map<?, ?> configuration, String propertyName, String defaultValue) throws ConfigurationException {
		return getString(propertyName, value(configuration, propertyName), defaultValue);
	}

	/**
	 * Parses a configured string value.
	 * 
	 * @param propertyName
	 *        Only for consistency with other methods.
	 * 
	 * @return the configured value, or the given default value, if no configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If the configured value is not "true" or "false".
	 */
	public static String getString(String propertyName, CharSequence propertyValue, String defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return defaultValue;
		}
		return propertyValue.toString();
	}

	/**
	 * Parses a configured {@link Enum} value configured with its
	 * {@link Enum#name()} or, if it is {@link ExternallyNamed}, by its
	 * {@link ExternallyNamed#getExternalName() external name}.
	 * 
	 * @return the configured value.
	 * 
	 * @throws ConfigurationException
	 *         If no configuration was given, or there is no enum
	 *         constant with the configured name.
	 */
	public static <T extends Enum<T>> T getEnumValueMandatory(String propertyName, CharSequence enumName, Class<T> enumClass) throws ConfigurationException {
		checkNotEmpty(propertyName, enumName);
		
		return internalGetEnumValue(propertyName, enumName, enumClass);
	}

	/**
	 * Parses a configured {@link Enum} value configured with its
	 * {@link Enum#name()} or, if it is {@link ExternallyNamed}, by its
	 * {@link ExternallyNamed#getExternalName() external name}.
	 * 
	 * @return the configured value, or the given default value, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If there is no enum constant with the configured name.
	 */
	public static <T extends Enum<T>> T getEnumValue(Map<?, ?> configuration, String propertyName, Class<T> enumClass, T defaultValue) throws ConfigurationException {
		return getEnumValue(propertyName, value(configuration, propertyName), enumClass, defaultValue);
	}
	
	/**
	 * Parses a configured {@link Enum} value configured with its
	 * {@link Enum#name()} or, if it is {@link ExternallyNamed}, by its
	 * {@link ExternallyNamed#getExternalName() external name}.
	 * 
	 * @return the configured value, or the given default value, if no
	 *         configuration is given.
	 * 
	 * @throws ConfigurationException
	 *         If there is no enum constant with the configured name.
	 */
	public static <T extends Enum<T>> T getEnumValue(String propertyName, CharSequence enumName, Class<T> enumClass, T defaultValue) throws ConfigurationException {
		if (StringServices.isEmpty(enumName)) {
			return defaultValue;
		}
		return internalGetEnumValue(propertyName, enumName, enumClass);
	}
	
	private static <T extends Enum<T>> T internalGetEnumValue(String propertyName, CharSequence enumName,
			Class<T> enumClass) throws ConfigurationException {
		T result = ConfigUtil.getEnumConstant(enumClass, enumName.toString());
		if (result == null) {
			throw new ConfigurationException(ERROR_INVALID_VALUE__VALUE_OPTIONS.fill(enumName, enumOptions(enumClass)),
				propertyName,
				enumName);
		}
		return result;
	}

	private static <T extends Enum<?>> String enumOptions(Class<? extends T> enumClass) {
		final Enum<?>[] enumConstants = enumClass.getEnumConstants();
		StringBuilder options = new StringBuilder();
		if (ExternallyNamed.class.isAssignableFrom(enumClass)) {
			for (Enum<?> enumConstant : enumConstants) {
				if (options.length() > 0) {
					options.append(", ");
				}
				options.append(((ExternallyNamed) enumConstant).getExternalName());
			}
		} else {
			for (Enum<?> enumConstant : enumConstants) {
				if (options.length() > 0) {
					options.append(", ");
				}
				options.append(enumConstant.name());
			}
		}
		String enumOptions = options.toString();
		return enumOptions;
	}
	
	// Implementation helpers. 
	
	/**
	 * Lookup the given configured class.
	 * @param expectedType 
	 *        a supertype of the expected class
	 * @param defaultClass
	 *        the class to use, if no configuration is given. If the default
	 *        class is <code>null</code>, a configuration is required.
	 * 
	 * @return the configured class or the given default class, if no
	 *         configuration was given. Never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If the configured class does not exist, or cannot be loaded, or
	 *         no configuration and not default class is given.
	 */
	private static <T> Class<? extends T> lookupClassForName(Class<T> expectedType, String propertyName, CharSequence className, Class<? extends T> defaultClass) throws ConfigurationException {
		Class<? extends T> resultClass;
		if (defaultClass == null) {
			resultClass = getClassForNameMandatory(expectedType, propertyName, className);
		} else {
			resultClass = getClassForName(expectedType, propertyName, className, defaultClass);
		}
		return resultClass;
	}
	
	/**
	 * Resolves the given class as singleton or new instance.
	 * 
	 * @param clazz
	 *        The class to instantiate.
	 * @return The resolved instance.
	 * @throws ConfigurationException
	 *         If instance resolution fails.
	 */
	public static <T> T getInstance(Class<? extends T> clazz) throws ConfigurationException {
		return lookupInstance(clazz, null, clazz);
	}

	/**
	 * Looks up the given class as singleton or instantiates the class using the default
	 * constructor, if the class is not a singleton class.
	 * 
	 * @param expectedType
	 *        Type that the result must be compatible with.
	 */
	static <T> T lookupInstance(Class<? extends T> expectedType, String propertyName, Class<?> configuredClass)
			throws ConfigurationException {
		try {
			return lookupSingletonOrFail(expectedType, propertyName, configuredClass);
		} catch (NoSuchFieldException e) {
			return newInstance(propertyName, dynamicCastType(expectedType, propertyName, configuredClass));
		}
	}
	
	private static <T> T lookupSingleton(Class<? extends T> expectedType, String propertyName,
			Class<? extends T> singletonClass, Class<?> defaultClass) throws ConfigurationException {
		try {
			return lookupSingletonOrFail(expectedType, propertyName, singletonClass);
		} catch (NoSuchFieldException ex) {
			if (singletonClass == defaultClass) {
				throw new AssertionError("Default class is not a singleton class.");
			} else {
				throw new ConfigurationException(ERROR_NOT_A_SINGLETON_CLASS, propertyName, singletonClass.getName(),
					ex);
			}
		}
	}

	/**
	 * Lookup the singleton instance for the given singleton class.
	 * 
	 * @param <T>
	 *        The expected type.
	 * @param singletonClass
	 *        The singleton class.
	 * @return The singleton instance.
	 * @throws ConfigurationException
	 *         If the given class is not a singleton class, or the found
	 *         singleton instance is not of the expected type.
	 */
	public static <T> T getSingleton(Class<T> singletonClass) throws ConfigurationException {
		try {
			return lookupSingletonOrFail(singletonClass, null, singletonClass);
		} catch (NoSuchFieldException ex) {
			throw new ConfigurationException(ERROR_NOT_A_SINGLETON_CLASS, null, null, ex);
		}
	}
	
	static <T> T lookupSingletonOrFail(Class<? extends T> expectedType, String propertyName,
			Class<?> singletonClass) throws ConfigurationException, NoSuchFieldException {
		try {
			return lookupGetInstanceMethod(expectedType, propertyName, singletonClass);
		} catch (NoSuchMethodException e) {
			return lookupInstanceField(expectedType, propertyName, singletonClass);
		}
	}

	private static <T> T lookupGetInstanceMethod(Class<T> expectedType, String propertyName, Class<?> singletonClass) throws NoSuchMethodException, ConfigurationException {
		try {
			Method getInstanceMethod = singletonClass.getMethod(GET_INSTANCE_METHOD_NAME, NO_PARAM_SIGNATURE);
			
			// Ignore non public static methods.
			if ((getInstanceMethod.getModifiers() & PUBLIC_STATIC) != PUBLIC_STATIC) {
				throw new NoSuchMethodException(GET_INSTANCE_METHOD_NAME);
			}
			
			Object result = getInstanceMethod.invoke(null, NO_ARGUMENTS);
			if (result == null) {
				throw new ConfigurationException(ERROR_FACTORY_METHOD_PROVIDED_NULL__NAME.fill(GET_INSTANCE_METHOD_NAME
					+ "()"), propertyName, singletonClass.getName());
			}
			
			return cast(expectedType, result);
	    } catch (IllegalArgumentException ex) {
	        throw new UnreachableAssertion("No argument was given.", ex);
	    } catch (SecurityException ex) {
			throw new ConfigurationException(ERROR_ACCESS_DENIED__TARGET.fill(GET_INSTANCE_METHOD_NAME
				+ "()"), propertyName, singletonClass.getName(), ex);
	    } catch (IllegalAccessException ex) {
			throw new ConfigurationException(ERROR_ACCESS_DENIED__TARGET.fill(GET_INSTANCE_METHOD_NAME
				+ "()"), propertyName, singletonClass.getName(), ex);
	    } catch (InvocationTargetException ex) {
			throw new ConfigurationException(ERROR_SINGLETON_LOOKUP_FAILED, propertyName, singletonClass.getName(), ex);
		} catch (ClassCastException e) {
			throw new ConfigurationException(ERROR_FACTORY_METHOD_PROVIDED_INCOMPATIBLE_VALUE__NAME_TYPE.fill(
				GET_INSTANCE_METHOD_NAME + "()", expectedType.getName()), propertyName, singletonClass.getName(), e);
		}
	}
	
	private static <T> T lookupInstanceField(Class<T> expectedType, String propertyName, Class<?> singletonClass) throws ConfigurationException, NoSuchFieldException {
		try { // try SINGLETON_FIELD_NAME then
		    Field instanceField = singletonClass.getField(SINGLETON_FIELD_NAME);

		    // Check, whether field is declared on given class.
		    if (instanceField.getDeclaringClass() != singletonClass) {
		    	throw new NoSuchFieldException("Not a singleton class, '" + SINGLETON_FIELD_NAME + "' field is declared in super class.");
		    }
		    
		    // Ignore non public static fields. 
		    if ((instanceField.getModifiers() & PUBLIC_STATIC) != PUBLIC_STATIC) {
		    	throw new NoSuchFieldException(SINGLETON_FIELD_NAME);
		    }
		    
			Object result = instanceField.get(null);
			if (result == null) {
				throw new ConfigurationException(ERROR_SINGLETON_REFERENCE_IS_NULL__FIELD.fill(SINGLETON_FIELD_NAME),
					propertyName, singletonClass.getName());
			}
			return cast(expectedType, result);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException(ERROR_ACCESS_DENIED__TARGET.fill(SINGLETON_FIELD_NAME),
				propertyName, singletonClass.getName(), e);
		} catch (SecurityException e) {
			throw new ConfigurationException(ERROR_ACCESS_DENIED__TARGET.fill(SINGLETON_FIELD_NAME),
				propertyName, singletonClass.getName(), e);
		} catch (ClassCastException e) {
			throw new ConfigurationException(ERROR_SINGLETON_REFERENCE_HAS_INCOMPATIBLE_TYPE__FIELD_TYPE.fill(
				SINGLETON_FIELD_NAME, expectedType.getName()), propertyName, singletonClass.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T cast(Class<T> expectedType, Object result) throws ClassCastException {
		if (expectedType == Object.class) {
			return (T) result;
		}
		return expectedType.cast(result);
	}
	
	/**
	 * Lookup the given configured class.
	 * 
	 * @param expectedType
	 *        A super class of the expected class
	 * @param propertyName
	 *        The configuration property.
	 * @param className
	 *        The configured class name. Must not be <code>null</code> or
	 *        empty.
	 * @return The configured {@link Class} object.
	 * 
	 * @throws ConfigurationException
	 *         If the class does not exist or cannot be loaded.
	 */
	private static <T> Class<? extends T> lookupClassForName(Class<T> expectedType, String propertyName, CharSequence className) throws ConfigurationException {
		try {
			return lookup(expectedType, propertyName, className);
		} catch (ClassNotFoundException ex) {
			throw new ConfigurationException(ERROR_CLASS_DOES_NOT_EXIST__NAME.fill(className), propertyName, className,
				ex);
		}
	}

	private static <T> Class<? extends T> lookup(Class<T> expectedSuperType, String propertyName, CharSequence className)
			throws ClassNotFoundException, ConfigurationException {
		Class<?> configuredClass = Class.forName(className.toString());
		return dynamicCastType(expectedSuperType, propertyName, configuredClass);
	}

	/**
	 * Ensure that the given configured class is a subtype of the given expected type.
	 * 
	 * @param <T>
	 *        The static expected type.
	 * @param expectedType
	 *        Dynamic representative of the expected type.
	 * @param configuredClass
	 *        the class to check.
	 * @return The configured class with more precise type.
	 * @throws ConfigurationException
	 *         If the given configured class is not a subtype of the expected type.
	 */
	public static <T> Class<? extends T> dynamicCastType(Class<T> expectedType, String propertName,
			Class<?> configuredClass)
			throws ConfigurationException {
		if (!expectedType.isAssignableFrom(configuredClass)) {
			throw new ConfigurationException(ERROR_CONFIGURED_CLASS_IS_NOT_SUBLASS_OF_EXPECTED_TYPE__CLASS_TYPE.fill(
				configuredClass.getName(), expectedType.getName()), propertName, configuredClass.getName());
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends T> result = (Class<? extends T>) configuredClass;
		
		return result;
	}
	
	/**
	 * Lookup the given configured class.
	 * 
	 * @param expectedSuperType
	 *        A super class of the expected class
	 * @param className
	 *        The configured class name. Must not be <code>null</code> or
	 *        empty.
	 * @return The configured {@link Class} object.
	 * 
	 * @throws ConfigurationException
	 *         If the class does not exist or cannot be loaded.
	 */
	public static <T> Class<? extends T> lookupClassForName(Class<T> expectedSuperType, String className) throws ConfigurationException {
		try {
			return lookup(expectedSuperType, null, className);
		} catch(ClassNotFoundException ex) {
			throw new ConfigurationException(ERROR_CLASS_DOES_NOT_EXIST__NAME.fill(className), null, null, ex);
		}
	}

	/* package protected */static <T> T newInstance(String propertyName, Class<T> aClass) throws ConfigurationException {
		try {
	        return aClass.newInstance();
	    } catch (InstantiationException e) {
			throw new ConfigurationException(ERROR_WHILE_INSTANTIATING_CLASS, propertyName, aClass.getName(), e);
	    } catch (IllegalAccessException e) {
			throw new ConfigurationException(ERROR_ACCESSING_CLASS, propertyName, aClass.getName(), e);
	    }
	}

	
	private static <T> T newInstance(String propertyName, Class<T> resultClass, Class<?>[] signature, Object... arguments) throws ConfigurationException, NoSuchMethodException {
		try {
			int argumentLength = (arguments == null) ? 0 : arguments.length;
			if (signature.length != argumentLength) {
				throw new ConfigurationException(ERROR_SIGNATURE_MISSMATCH.fill(Arrays.toString(signature),
					Arrays.toString(arguments)), propertyName, resultClass.getName());
			}
			return resultClass.getConstructor(signature).newInstance(arguments);
		} catch (SecurityException e) {
			throw new ConfigurationException(ERROR_ACCESSING_CLASS_CONSTRUCTOR, propertyName, resultClass.getName(), e);
		} catch (InstantiationException e) {
			throw new ConfigurationException(ERROR_WHILE_INSTANTIATING_CLASS, propertyName, resultClass.getName(), e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException(ERROR_ACCESSING_CLASS, propertyName, resultClass.getName(), e);
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException(ERROR_DURING_INSTANTIATION, propertyName, resultClass.getName(), e);
		} catch (InvocationTargetException e) {
			throw new ConfigurationException(ERROR_DURING_INSTANTIATION, propertyName, resultClass.getName(), e);
		}
	}

	/**
	 * Test the given class for existence of a public constructor that can be
	 * invoked by reflection.
	 * 
	 * @param testedClass
	 *        The class to test.
	 * @param signature
	 *        The requested constructor signature.
	 * @return Whether the given class can be constructed by reflection using
	 *         the given constructor signature.
	 * @throws SecurityException
	 *         See {@link Class#getConstructor(Class...)}
	 */
	public static boolean hasPublicConstructor(Class<?> testedClass, Class<?>[] signature) throws SecurityException {
		int classModifiers = testedClass.getModifiers();
		if (! Modifier.isPublic(classModifiers)) {
			return false;
		}
		if (Modifier.isInterface(classModifiers)) {
			return false;
		}
		if (Modifier.isAbstract(classModifiers)) {
			return false;
		}
		try {
			Constructor<?> constructor = testedClass.getConstructor(signature);
			if (! Modifier.isPublic(constructor.getModifiers())) {
				return false;
			}
			return true;
		} catch (NoSuchMethodException ex) {
			return false;
		}
	}

	/**
	 * Checks, whether the given class can be constructed through
	 * {@link #getConfiguredInstance(Class, Properties, String, Class)}.
	 * 
	 * @param testedClass
	 *        The class to test.
	 * @return Whether the given class is a configurable one.
	 * @throws SecurityException
	 *         See {@link Class#getConstructor(Class...)}
	 */
	public static boolean hasConfigConstructor(Class<?> testedClass) throws SecurityException {
		return hasPublicConstructor(testedClass, CONFIG_CONSTRUCTOR_SIGNATURE);
	}

	/**
	 * Checks, whether the given class can be constructed through
	 * {@link #getNewInstanceMandatory(Class, Map, String)}.
	 * 
	 * @param testedClass
	 *        The class to test.
	 * @return Whether the given class can be constructed by reflection using the default
	 *         constructor.
	 * @throws SecurityException
	 *         See {@link Class#getConstructor(Class...)}
	 */
	public static boolean hasDefaultConstructor(Class<?> testedClass) throws SecurityException {
		return hasPublicConstructor(testedClass, NO_PARAM_SIGNATURE);
	}
	
	private static final void checkNotEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			throw new ConfigurationException(PROPERTY_IS_MANDATORY_BUT_VALUE_WAS_EMPTY, propertyName, propertyValue);
	    }
	}

	private static String value(Map<?, ?> configuration, String propertyName) {
		return (String) configuration.get(propertyName);
	}

	/**
	 * Creates a instance of a class with {@link Properties} constructor.
	 * 
	 * @param <T>
	 *        The expected type.
	 * @param expectedType
	 *        The dynamic representative of the expected type.
	 * @param configuration
	 *        The configuration of the class to instantiate.
	 * @param defaultClass
	 *        The default class to instantiate, if the given configuration does not contain a class
	 *        name.
	 * @return A new instance of the configured class.
	 * @throws ConfigurationException
	 *         If instantiating fails.
	 *         
	 * @see #getConfiguredInstance(Class, Properties, String, Class)
	 */
	public static <T> T getConfiguredInstance(Class<T> expectedType, Properties configuration, Class<? extends T> defaultClass) throws ConfigurationException {
		return getConfiguredInstance(expectedType, configuration, DEFAULT_CLASS_PROPERTY, defaultClass);
	}

	/**
	 * Creates a instance of a class with {@link Properties} constructor.
	 * 
	 * @param <T>
	 *        The expected type.
	 * @param expectedType
	 *        The dynamic representative of the expected type.
	 * @param configuration
	 *        The configuration of the class to instantiate.
	 * @param classPropertyName
	 *        The name of the property holding the class name of the class to instantiate.
	 * @param defaultClass
	 *        The default class to instantiate, if the given configuration does not contain a class
	 *        name.
	 * @return A new instance of the configured class.
	 * @throws ConfigurationException
	 *         If instantiating fails.
	 */
	public static <T> T getConfiguredInstance(Class<T> expectedType, Properties configuration, String classPropertyName, Class<? extends T> defaultClass) throws ConfigurationException {
		assert defaultClass == null || expectedType.isAssignableFrom(defaultClass) : "Default class must be a subtype of base class.";
		
		return getNewInstance(expectedType, configuration, classPropertyName, defaultClass, CONFIG_CONSTRUCTOR_SIGNATURE, configuration);
	}

	/**
	 * Creates a instance of a class with {@link Properties} constructor.
	 * 
	 * @param <T>
	 *        The expected type.
	 * @param configuredClass
	 *        The concrete class to instantiate.
	 * @param configuration
	 *        The configuration for the new instance.
	 * @return A new instance of the configured class.
	 * @throws ConfigurationException
	 *         If instantiating fails.
	 */
	public static <T> T newConfiguredInstance(Class<T> configuredClass, Properties configuration)
			throws ConfigurationException {
		return 
			newInstanceWithFallback(null, configuredClass, 
				CONFIG_CONSTRUCTOR_SIGNATURE, 
				new Object[] { configuration });
	}

	/**
	 * Creates a instance of a class with {@link Properties} constructor.
	 * 
	 * @param <T>
	 *        The expected type.
	 * @param configuredClass
	 *        The concrete class to instantiate.
	 * @param signature
	 *        The constructor signature.
	 * @param arguments
	 *        The arguments to call the construtor with.
	 * @return A new instance of the configured class.
	 * @throws ConfigurationException
	 *         If instantiating fails.
	 */
	public static <T> T newInstance(Class<T> configuredClass, Class<?>[] signature, Object... arguments)
			throws ConfigurationException {
		return newInstanceWithFallback(null, configuredClass, signature, arguments);
	}

	/**
	 * Creates a instance of a class with default constructor constructor.
	 * 
	 * @param <T>
	 *        The expected type.
	 * @param configuredClass
	 *        The concrete class to instantiate.
	 * @return A new instance of the configured class.
	 * @throws ConfigurationException
	 *         If instantiating fails.
	 */
	public static <T> T newInstance(Class<T> configuredClass) throws ConfigurationException {
		return newInstance(null, configuredClass);
	}

	/**
	 * Returns the constant of the given {@link Enum} class with the given name.
	 * If the given class implements {@link ExternallyNamed} the constant with
	 * the same {@link ExternallyNamed#getExternalName()} is returned.
	 * 
	 * @param enumType
	 *        the enum class to check
	 * @param externalName
	 *        the external name to resolve
	 * 
	 * @return the enum with the given external name (if it is
	 *         {@link ExternallyNamed}) or the given name
	 * 
	 * @throws ConfigurationException
	 *         iff no enum constant is found.
	 */
	public static <T extends Enum<?>> T getEnum(Class<? extends T> enumType, String externalName)
			throws ConfigurationException {
		T result = getEnumConstant(enumType, externalName);
		if (result != null) {
			return result;
		}
		throw new ConfigurationException(ERROR_INVALID_VALUE__VALUE_OPTIONS.fill(externalName, enumOptions(enumType)),
			null, externalName);
	}

	/**
	 * Returns the constant of the given {@link Enum} class with the given name.
	 * If the given class implements {@link ExternallyNamed} the constant with
	 * the same {@link ExternallyNamed#getExternalName()} is returned.
	 * 
	 * @param enumType
	 *        the enum class to check
	 * @param externalName
	 *        the external name to resolve
	 * 
	 * @return the enum with the given external name (if it is
	 *         {@link ExternallyNamed}) or the given name, or <code>null</code> if none was found
	 * 
	 */	
	private static <T extends Enum<?>> T getEnumConstant(Class<? extends T> enumType, String externalName) {
		T result = null;
		final T[] enumConstants = enumType.getEnumConstants();
		if (ExternallyNamed.class.isAssignableFrom(enumType)) {
			for (T constant : enumConstants) {
				final String name = ((ExternallyNamed) constant).getExternalName();
				if (externalName.equals(name)) {
					result = constant;
					break;
				}
			}
		} else {
			for (T constant : enumConstants) {
				if (externalName.equals(constant.name())) {
					result = constant;
					break;
				}
			}
		}
		return result;
	}


	/**
	 * Returns the constant in the given {@link Enum} type with the given index
	 * 
	 * @param enumType
	 *        concrete enum type
	 * @param index
	 *        the index of the desired constant
	 * 
	 * @throws ConfigurationException
	 *         if there is no constant withe the given index
	 */
	public static <T extends Enum<T>> T getEnum(Class<? extends T> enumType, int index) throws ConfigurationException {
		final T[] enumConstants = enumType.getEnumConstants();
		if (index < 0 || index >= enumConstants.length) {
			throw new ConfigurationException(ERROR_NO_SUCH_ENUM_CONSTANT__ENUM_INDEX.fill(enumType.getName(), index),
				null, Integer.toString(index));
		}
		return enumConstants[index];
	}

	/**
	 * Returns the inner {@link ConfigurationItem}s stored in the given property.
	 * <p>
	 * Retrieves the {@link ConfigurationItem} from {@link ConfiguredInstance}s.
	 * </p>
	 * 
	 * @param parent
	 *        If it is null, the empty list is returned.
	 * @param property
	 *        Is not allowed to be null.
	 * @return Might be unmodifiable. Is never null. Does not contain null.
	 */
	public static Collection<ConfigurationItem> getChildConfigs(ConfigurationItem parent, PropertyDescriptor property) {
		if (parent == null) {
			return emptyList();
		}
		switch (property.kind()) {
			case PLAIN:
			case COMPLEX: {
				// Nothing to do, as there is no ConfigItem here.
				return emptyList();
			}
			case ITEM: {
				Object value = getValueIfPresent(parent, property);
				if (value == null) {
					return emptyList();
				}
				return singletonList(toConfig(property, value));
			}
			case ARRAY: {
				List<?> values = PropertyDescriptorImpl.arrayAsList(getValueIfPresent(parent, property));
				return getCollectionConfigs(property, values);
			}
			case LIST: {
				List<?> values = nonNull((List<?>) getValueIfPresent(parent, property));
				return getCollectionConfigs(property, values);
			}
			case MAP: {
				Map<?, ?> values = nonNull((Map<?, ?>) getValueIfPresent(parent, property));
				List<ConfigurationItem> result = new ArrayList<>();
				for (Object entry : values.values()) {
					if (entry == null) {
						continue;
					}
					result.add(toConfig(property, entry));
				}
				return result;
			}
			case DERIVED:
			case REF: {
				/* Nothing to do here. These ConfigItems belong somewhere else. */
				return emptyList();
			}
			default: {
				throw new UnreachableAssertion("Unhandled property kind: " + property.kind());
			}
		}
	}

	private static Collection<ConfigurationItem> getCollectionConfigs(PropertyDescriptor property, List<?> values) {
		List<ConfigurationItem> result = new ArrayList<>();
		for (Object entry : values) {
			if (entry == null) {
				continue;
			}
			result.add(toConfig(property, entry));
		}
		return result;
	}

	/**
	 * Returns the value stored under the given property in the given config. If the property is
	 * mandatory but not set, null is returned.
	 * 
	 * @param config
	 *        Is not allowed to be null.
	 * @param property
	 *        Is not allowed to be null.
	 * @return Might be null.
	 */
	public static Object getValueIfPresent(ConfigurationItem config, PropertyDescriptor property) {
		if (property.isMandatory() && !config.valueSet(property)) {
			return null;
		}
		return config.value(property);
	}

	private static ConfigurationItem toConfig(PropertyDescriptor property, Object untypedValue) {
		if (untypedValue instanceof ConfigurationItem) {
			return (ConfigurationItem) untypedValue;
		}
		/* This is the clean way. The hack above enables reuse of this code in the ConfigBuilder. It
		 * sometimes stores ConfigItems where the ConfigAccess would expect ConfiguredInstances. */
		return property.getConfigurationAccess().getConfig(untypedValue);
	}

}
