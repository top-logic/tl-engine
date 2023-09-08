/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.annotation.SharedInstance;

/**
 * Default scheme for finding configuration constructors for implementation classes.
 * 
 * <p>
 * A configuration constructor is public and has the arguments 
 * ({@link InstantiationContext}, {@link ConfigurationItem}).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultConfigConstructorScheme {

	public interface Factory {
		Class<?> getConfigurationInterface();
		boolean isSharedInstance();

		/**
		 * Creates an instance from the given configuration.
		 * 
		 * @param context
		 *        {@link InstantiationContext} used to instantiate configuration.
		 * @param config
		 *        The configuration for the new instance
		 */
		Object createInstance(InstantiationContext context, ConfigurationItem config) throws ConfigurationException;

		/**
		 * Creates an default instance.
		 * 
		 * @param context
		 *        {@link InstantiationContext} used to instantiate configuration.
		 */
		Object createDefaultInstance(InstantiationContext context) throws ConfigurationException;

		/**
		 * Creates an default instance.
		 */
		Object createDefaultInstance() throws ConfigurationException;

		/**
		 * Whether this factory can be used to create instances.
		 */
		boolean isConcrete();
	}

	private static final String FACTORY_METHOD = "newInstance";

	private static final Map<Class<?>, Factory> factoryByClass = new HashMap<>();

	/**
	 * Find the configured object factory for the given implementation class
	 * according to the {@link DefaultConfigConstructorScheme}.
	 */
	public static synchronized Factory getFactory(Class<?> implementationClass) throws ConfigurationException {
		Factory factory = factoryByClass.get(implementationClass);
		if (factory == null) {
			try {
				factory = lookupFactory(implementationClass);
			} catch (NoClassDefFoundError ex) {
				// Enhance error to be able to find the 
				throw (NoClassDefFoundError) new NoClassDefFoundError(
					"Error resolving factory for '" + implementationClass + "'.").initCause(ex);
			}
			factoryByClass.put(implementationClass, factory);
		}
		return factory;
	}
	
	private static Factory lookupFactory(Class<?> implementationClass) throws ConfigurationException {
		int classModifiers = implementationClass.getModifiers();
		if (! Modifier.isPublic(classModifiers)) {
			throw new ConfigurationException("Configured object implementation class must be public: " + implementationClass);
		}
		
		Factory factoryMethodFactory = lookupFactoryMethod(implementationClass);
		if (factoryMethodFactory != null) {
			return factoryMethodFactory;
		}

		Constructor<?> defaultConstructor = null;
		try {
			Constructor<?> uniqueConfigConstructor = null;
			Constructor<?>[] constructors = implementationClass.getConstructors();
			for (int n = 0, cnt = constructors.length; n < cnt; n++) {
				Constructor<?> constructor = constructors[n];
				
				Class<?>[] signature = constructor.getParameterTypes();
				if (signature.length == 0) {
					defaultConstructor = constructor;
					continue;
				}
				if (!isConfigSignature(signature)) {
					continue;
				}
				
				if (uniqueConfigConstructor != null) {
					throw new ConfigurationException("Configured implementation class has no unique configuration constructor: " + uniqueConfigConstructor + ", " + constructor);
				}
				
				if (! Modifier.isPublic(constructor.getModifiers())) {
					throw new ConfigurationException("Configuration constructor must be public: " + constructor);
				}
				
				uniqueConfigConstructor = constructor;
			}
			
			if (uniqueConfigConstructor != null) {
				if (Modifier.isAbstract(classModifiers)) {
					return AbstractFactory.newAbstractFactory(uniqueConfigConstructor);
				} else {
					return new ConfiguredObjectFactory(uniqueConfigConstructor,
						isConfiguredSingleton(implementationClass));
				}
			}
		} catch (SecurityException e) {
			throw new ConfigurationException("Configured implementation class not accessible: " + implementationClass, e);
		}
		
		// Check for getInstance() method.
		try {
			Method getInstanceMethod = implementationClass.getMethod(ConfigUtil.GET_INSTANCE_METHOD_NAME, ConfigUtil.NO_PARAM_SIGNATURE);
			
			// Ignore non public static methods.
			if ((getInstanceMethod.getModifiers() & ConfigUtil.PUBLIC_STATIC) == ConfigUtil.PUBLIC_STATIC) {
				
				return new GetInstanceLookup(getInstanceMethod);
			}
		} catch (SecurityException e) {
			throw new AssertionError(e);
		} catch (NoSuchMethodException e) {
			// Fall through.
		}
		
		// Check for INSTANCE field.
	    try {
			Field instanceField = implementationClass.getField(ConfigUtil.SINGLETON_FIELD_NAME);

			// Check, whether field is declared on given class.
			if (instanceField.getDeclaringClass() == implementationClass) {
				// Ignore non public static fields. 
				if ((instanceField.getModifiers() & ConfigUtil.PUBLIC_STATIC) == ConfigUtil.PUBLIC_STATIC) {
					return new InstanceFieldAccess(instanceField);
				}
			}
		} catch (SecurityException e) {
			throw new AssertionError(e);
		} catch (NoSuchFieldException e) {
			// Fall through.
		}
		
		// Check for non-configuration constructor.
		if (defaultConstructor != null) {
			if (! Modifier.isPublic(defaultConstructor.getModifiers())) {
				throw new ConfigurationException("Configured object implementation class '" + implementationClass + "' must have at least a no public default constructor.");
			}
			
			return new DefaultConstruction(defaultConstructor, isConfiguredSingleton(implementationClass));
		}
		
		String expectedConstructorSignature = 
			implementationClass.getName() + "(" + InstantiationContext.class + ", " + "? extends "
				+ ConfigurationItem.class + ")";

		if (Modifier.isAbstract(classModifiers)) {
			return AbstractFactory.newAbstractFactory(implementationClass, PolymorphicConfiguration.class);
		}
		throw new ConfigurationException("Configured implementation class '" + implementationClass + "' has no public constructor '" + expectedConstructorSignature + "', is no singleton or has no public default constructor.");
	}

	private static boolean isConfigSignature(Class<?>[] signature) {
		if (signature.length != 2) {
			return false;
		}

		Class<?> firstArgumentType = signature[0];
		if (firstArgumentType != InstantiationContext.class) {
			return false;
		}

		Class<?> secondArgumentType = signature[1];
		if (!ConfigurationItem.class.isAssignableFrom(secondArgumentType)) {
			return false;
		}
		return true;
	}

	private static Factory lookupFactoryMethod(Class<?> implementationClass) throws ConfigurationException {
		Factory factory = null;
		Method[] declaredMethods = implementationClass.getDeclaredMethods();
		Method uniqueFactoryMethod = null;
		for (Method m : declaredMethods) {
			if (!FACTORY_METHOD.equals(m.getName())) {
				continue;
			}
			int methodModifiers = m.getModifiers();
			
			if (!(Modifier.isStatic(methodModifiers) && Modifier.isPublic(methodModifiers))) {
				continue;
			}
			Class<?>[] parameterTypes = m.getParameterTypes();
			if (!isConfigSignature(parameterTypes)) {
				continue;
			}
			Class<?> returnType = m.getReturnType();
			if (!implementationClass.isAssignableFrom(returnType)) {
				throw new ConfigurationException("Implementation class '" + implementationClass.getName()
					+ "' has factory method of unexpected return type: '" + returnType.getName() + "'");
			}
			if (uniqueFactoryMethod != null) {
				throw new ConfigurationException("More than one factory method in '" + implementationClass.getName()
					+ "': '" + uniqueFactoryMethod + "' and '" + m + "'");
			}
			uniqueFactoryMethod = m;
		}

		if (uniqueFactoryMethod != null) {
			return new FactoryMethodFactory(uniqueFactoryMethod, isConfiguredSingleton(implementationClass));
		}

		return factory;
	}

	/**
	 * Concrete {@link Factory} for classes which do not use configuration.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static abstract class AbstractUnconfiguredClassFactory implements Factory {

		public AbstractUnconfiguredClassFactory() {
		}

		@Override
		public Class<?> getConfigurationInterface() {
			return PolymorphicConfiguration.class;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public Object createInstance(InstantiationContext context, ConfigurationItem config)
				throws ConfigurationException {
			return createDefaultInstance();
		}

		@Override
		public Object createDefaultInstance(InstantiationContext context) throws ConfigurationException {
			return createDefaultInstance();
		}

	}

	/**
	 * {@link DefaultConfigConstructorScheme.Factory} that creates objects
	 * through a public default constructor.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class DefaultConstruction extends AbstractUnconfiguredClassFactory {
		
		private final Constructor<?> defaultConstructor;
		private final boolean sharedInstance;

		public DefaultConstruction(Constructor<?> aDefaultConstructor, boolean isSharedInstance) {
			this.defaultConstructor = aDefaultConstructor;
			this.sharedInstance     = isSharedInstance;
		}

		@Override
		public boolean isSharedInstance() {
			return sharedInstance;
		}

		@Override
		public Object createDefaultInstance() throws ConfigurationException {
			try {
				return defaultConstructor.newInstance(ConfigUtil.NO_ARGUMENTS);
			} catch (InvocationTargetException e) {
				throw unwrap(null, defaultConstructor.getDeclaringClass(), e);
			} catch (Exception ex) {
				throw toConfigurationException(null, defaultConstructor.getDeclaringClass(), ex);
			}
		}
	}

	/**
	 * {@link DefaultConfigConstructorScheme.Factory} that looks up objects
	 * through a public static final <code>INSTANCE</code> field.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class InstanceFieldAccess extends AbstractUnconfiguredClassFactory {

		private final Field instanceField;

		public InstanceFieldAccess(Field anInstanceField) {
			this.instanceField = anInstanceField;
		}

		@Override
		public boolean isSharedInstance() {
			return true;
		}

		@Override
		public Object createDefaultInstance() throws ConfigurationException {
			try {
				return instanceField.get(null);
			} catch (Exception ex) {
				throw toConfigurationException(null, instanceField.getDeclaringClass(), ex);
			}
		}

	}

	/**
	 * {@link DefaultConfigConstructorScheme.Factory} that looks up objects
	 * through a public static <code>getInstance()</code> method.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class GetInstanceLookup extends AbstractUnconfiguredClassFactory {

		private final Method getInstanceMethod;

		public GetInstanceLookup(Method aGetInstanceMethod) {
			this.getInstanceMethod = aGetInstanceMethod;
		}

		@Override
		public boolean isSharedInstance() {
			return true;
		}
		
		@Override
		public Object createDefaultInstance() throws ConfigurationException {
			try {
				return getInstanceMethod.invoke(null, ConfigUtil.NO_ARGUMENTS);
			} catch (InvocationTargetException e) {
				throw unwrap(null, getInstanceMethod.getDeclaringClass(), e);
			} catch (Exception ex) {
				throw toConfigurationException(null, getInstanceMethod.getDeclaringClass(), ex);
			}
		}

	}

	/**
	 * {@link DefaultConfigConstructorScheme.Factory} that is used for abstract classes.
	 * <p>
	 * Abstract classes may be pre-configured, and therefore need a {@link Factory} for them. But if
	 * {@link #createInstance(InstantiationContext, ConfigurationItem)} is called, an
	 * {@link UnsupportedOperationException} is thrown.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class AbstractFactory implements Factory {

		private final Class<?> _declaringClass;

		private final Class<?> _configInterface;

		public AbstractFactory(Class<?> declaringClass, Class<?> configInterface) {
			_declaringClass = declaringClass;
			_configInterface = configInterface;
		}

		public static Factory newAbstractFactory(Class<?> declaringClass, Class<?> configInterface) {
			return new AbstractFactory(declaringClass, configInterface);
		}

		public static Factory newAbstractFactory(Constructor<?> configConstructor) {
			Class<?> configInterface = configConstructor.getParameterTypes()[1];
			Class<?> declaringClass = configConstructor.getDeclaringClass();
			return newAbstractFactory(declaringClass, configInterface);
		}

		@Override
		public Class<?> getConfigurationInterface() {
			return _configInterface;
		}

		@Override
		public boolean isSharedInstance() {
			return false;
		}

		@Override
		public boolean isConcrete() {
			return false;
		}

		@Override
		public Object createInstance(InstantiationContext context, ConfigurationItem configuration)
				throws ConfigurationException {
			throw failAbstract(configuration.location());
		}

		@Override
		public Object createDefaultInstance(InstantiationContext context) throws ConfigurationException {
			throw failAbstract(null);
		}

		@Override
		public Object createDefaultInstance() throws ConfigurationException {
			throw failAbstract(null);
		}

		private UnsupportedOperationException failAbstract(Location location) {
			StringBuilder error = new StringBuilder();
			error.append("Cannot instantiate abstract class '");
			error.append(_declaringClass.getName());
			error.append("'");
			if (location != null) {
				error.append(" at ");
				error.append(location);
			}
			error.append(".");
			return new UnsupportedOperationException(error.toString());
		}

	}

	/**
	 * Concrete {@link Factory} that handles classes which uses configurations.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static abstract class AbstractConfiguredClassFactory implements Factory {

		private final boolean _sharedInstance;

		public AbstractConfiguredClassFactory(boolean sharedInstance) {
			_sharedInstance = sharedInstance;
		}

		protected abstract Class<?> getImplementationClass();

		@Override
		public boolean isSharedInstance() {
			return _sharedInstance;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public Object createDefaultInstance() throws ConfigurationException {
			return createDefaultInstance(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
		}

		@Override
		public Object createDefaultInstance(InstantiationContext context) throws ConfigurationException {
			@SuppressWarnings("unchecked")
			Class<? extends PolymorphicConfiguration<?>> configurationInterface =
				(Class<? extends PolymorphicConfiguration<?>>) getConfigurationInterface();
			Class<?> implClass = getImplementationClass();
			ConfigurationItem config =
				createDefaultPolymorphicConfiguration(context, configurationInterface, implClass);
			return createInstance(context, config);
		}

		/**
		 * Checks that the given newly created instance is {@link ConfiguredInstance}.
		 * 
		 * @param context
		 *        The context to create the given instance.
		 * @param instance
		 *        The newly created object to check.
		 * 
		 * @return The instance to return instead
		 */
		@SuppressWarnings("unused")
		protected Object checkConfiguredInstance(InstantiationContext context, Object instance) {
			if (instance != null && !(instance instanceof ConfiguredInstance<?>)) {
				Class<? extends Object> implClass = instance.getClass();
				if (false) {
					/* TODO #23348: This should be activated, when all instances are
					 * ConfiguredInstance. */
					context.error("Object of type '" + implClass.getName() + "' uses configuration interface '"
						+ getConfigurationInterface() + "' but is not a configured instance.");
					instance = null;
				}
			}
			return instance;
		}

	}

	/**
	 * {@link DefaultConfigConstructorScheme.Factory} that looks up objects through a public static
	 * <code>newInstance({@link InstantiationContext}, {@link PolymorphicConfiguration})</code>
	 * method.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class FactoryMethodFactory extends AbstractConfiguredClassFactory {

		private final Method _factoryMethod;

		public FactoryMethodFactory(Method factoryMethod, boolean sharedInstance) {
			super(sharedInstance);
			_factoryMethod = factoryMethod;
		}

		@Override
		public Class<?> getConfigurationInterface() {
			return _factoryMethod.getParameterTypes()[1];
		}

		@Override
		protected Class<?> getImplementationClass() {
			return _factoryMethod.getDeclaringClass();
		}

		@Override
		public Object createInstance(InstantiationContext context, ConfigurationItem config)
				throws ConfigurationException {
			Object result;
			try {
				Object obj = null; // method is static so it can be null
				result = _factoryMethod.invoke(obj, context, config);
			} catch (InvocationTargetException ex) {
				throw unwrap(config, _factoryMethod.getDeclaringClass(), ex);
			} catch (Exception ex) {
				throw toConfigurationException(null, _factoryMethod.getDeclaringClass(), ex);
			}
			return checkConfiguredInstance(context, result);
		}

	}

	/**
	 * {@link DefaultConfigConstructorScheme.Factory} that creates configured
	 * objects through a public configuration constructor with the signature (
	 * {@link InstantiationContext}, {@link PolymorphicConfiguration}).
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class ConfiguredObjectFactory extends AbstractConfiguredClassFactory {

		private final Constructor<?> configConstructor;

		public ConfiguredObjectFactory(Constructor<?> aConfigConstructor, boolean isSharedInstance) {
			super(isSharedInstance);
			this.configConstructor = aConfigConstructor;
		}

		@Override
		public Class<?> getConfigurationInterface() {
			return this.configConstructor.getParameterTypes()[1];
		}
		
		@Override
		protected Class<?> getImplementationClass() {
			return configConstructor.getDeclaringClass();
		}

		@Override
		public Object createInstance(InstantiationContext context, ConfigurationItem configuration) throws ConfigurationException {
			Object result;
			try {
				result = this.configConstructor.newInstance(new Object[] { context, configuration });
			} catch (InvocationTargetException ex) {
				throw unwrap(configuration, configConstructor.getDeclaringClass(), ex);
			} catch (Exception ex) {
				throw toConfigurationException(configuration, configConstructor.getDeclaringClass(), ex);
			}
			return checkConfiguredInstance(context, result);
		}

	}

	public static ConfigurationItem createDefaultPolymorphicConfiguration(InstantiationContext context,
			Class<? extends PolymorphicConfiguration<?>> configurationInterface, Class<?> implClass) {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(configurationInterface);
		PropertyDescriptor implementationClassProperty = builder.descriptor().getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		Object defaultValue = implementationClassProperty.getDefaultValue();
		if (defaultValue != implClass) {
			builder.initValue(implementationClassProperty, implClass);
		}
		return builder.createConfig(context);
	}

	/**
	 * Whether the given implementation class can be shared at places where its is configured with
	 * equal configurations.
	 * 
	 * <p>
	 * To externally decide, whether an implementation class is a shared instance, use
	 * {@link #getFactory(Class)}.{@link Factory#isSharedInstance()}.
	 * </p>
	 */
	private static boolean isConfiguredSingleton(Class<?> implementationClass) {
		return implementationClass.getAnnotation(SharedInstance.class) != null;
	}

	static AssertionError unwrap(ConfigurationItem context, Class<?> implClass, InvocationTargetException ex) throws ConfigurationException {
		Throwable targetException = ex.getTargetException();
		if (targetException instanceof Error) {
			throw (Error) targetException;
		}
		if (targetException instanceof ConfigurationException) {
			throw (ConfigurationException) targetException;
		}
		throw toConfigurationException(context, implClass, targetException);
	}

	static ConfigurationException toConfigurationException(ConfigurationItem context, Class<?> implClass, Throwable ex) {
		return new ConfigurationException(
			I18NConstants.ERROR_INSTANTIATION_FAILED__CLASS_LOCATION.fill(
				implClass.getName(), context == null ? "<unknown location>" : context.location()), null, null, ex);
	}

}
