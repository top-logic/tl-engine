/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.thread.ThreadContext;

/**
 * <p>
 * The class {@link ConfiguredRuntimeModule} is a {@link RuntimeModule} for a
 * {@link ManagedClass} which concrete implementation class may be configured,
 * i.e. in the configuration is a subclass of the {@link ManagedClass}
 * configured which is taken as
 * {@link BasicRuntimeModule#getImplementationInstance()}.
 * </p>
 * 
 * <p>
 * Moreover it allows the user to add additional dependencies via configuration.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredRuntimeModule<M extends ManagedClass> extends RuntimeModule<M> {

	private static final String DEPENDENCY_PREFIX = "dependency:";
	private static final String DEPENDENCY_ACTIVE_VALUE = "enabled";

	private static final Class<?>[] CONFIGURATION_CONSTRUCTOR_SIGNATURE = new Class<?>[] { IterableConfiguration.class };

	@Override
	protected M newImplementationInstance(final IterableConfiguration config) throws ModuleException,
			ConfigurationException {
		return createInstance(config);
	}

	/**
	 * Creates the instance from the given configuration returned by
	 * {@link RuntimeModule#newImplementationInstance(IterableConfiguration)}.
	 * 
	 * If the module declares dependency to
	 * {@link com.top_logic.basic.thread.ThreadContextManager.Module} then a {@link ThreadContext}
	 * is available during the execution.
	 * 
	 * @param config
	 *        the configuration to build the instance from
	 * 
	 * @return the sole instance later returned by {@link #getImplementationInstance()}
	 * 
	 * @throws ConfigurationException
	 *         iff the configuration is invalid to create a correct instance from
	 */
	protected M createInstance(final IterableConfiguration config) throws ConfigurationException {
		Class<?>[] constructSignature;
		Object[] constructArguments;
		final Properties configAsProperties = config.getProperties();
		if (hasConfigurationConstructor()) {
			constructSignature = CONFIGURATION_CONSTRUCTOR_SIGNATURE;
			constructArguments = new Object[] {config};
		} else {
			constructSignature = ConfigUtil.CONFIG_CONSTRUCTOR_SIGNATURE;
			constructArguments = new Object[] {configAsProperties};
		}
		return ConfigUtil.getNewInstance(getImplementation(), configAsProperties, getClassPropertyName(),
				getDefaultImplementationClass(), constructSignature, constructArguments);
	}

	/**
	 * Determines whether the implementation class has a constructor with
	 * signature {@link #CONFIGURATION_CONSTRUCTOR_SIGNATURE}.
	 * 
	 * If method returns <code>true</code> it is expected there is a public
	 * constructor with that signature; if it returns <code>false</code> it is
	 * expected there is a public constructor with signature
	 * {@link ConfigUtil#CONFIG_CONSTRUCTOR_SIGNATURE}.
	 */
	protected boolean hasConfigurationConstructor() {
		return false;
	}

	protected Class<? extends M> getDefaultImplementationClass() {
		return null;
	}

	protected String getClassPropertyName() {
		return ConfigUtil.DEFAULT_CLASS_PROPERTY;
	}

	@Override
	public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() throws ConfigurationError {
		final IterableConfiguration config = getServiceConfiguration();
		String concreteClassName = config.getValue(getClassPropertyName());
		ArrayList<Class<? extends BasicRuntimeModule<?>>> dependencies = new ArrayList<>();
		addConfiguredDependencies(dependencies);
		if (concreteClassName == null && getDefaultImplementationClass() != null) {
			Class<?> concreteImplementationClass = getDefaultImplementationClass();
			assert getImplementation().isAssignableFrom(concreteImplementationClass);
			dependencies = addDependencies(dependencies, concreteImplementationClass);
		} else {
			dependencies = addDependencies(dependencies, concreteClassName, this);
		}
		return dependencies;
	}
	
	/**
	 * Adds configured dependencies to the dependencies.
	 * 
	 * <b>Note:</b> Uses unchecked cast. The module classes are configured but
	 * only {@link BasicRuntimeModule} are allowed.
	 * 
	 * @param dependencies
	 *        the dependencies to add configured dependencies
	 */
	@SuppressWarnings("unchecked")
	private void addConfiguredDependencies(Collection<Class<? extends BasicRuntimeModule<?>>> dependencies)
			throws ConfigurationError {
		final Set<Entry<Object, Object>> entries = getServiceConfiguration().getProperties().entrySet();
		for (Entry<Object, Object> entry : entries) {
			assert entry.getKey() instanceof String;
			final String key = (String) entry.getKey();
			if (key.startsWith(DEPENDENCY_PREFIX)) {
				final String value = String.valueOf(entry.getValue());
				if (DEPENDENCY_ACTIVE_VALUE.equals(value)) {
					final String module = key.substring(DEPENDENCY_PREFIX.length());
					
					final Class<?> moduleClass;
					try {
						moduleClass = ConfigUtil.lookupClassForName(BasicRuntimeModule.class, module);
					} catch (ConfigurationException ex) {
						throw new ConfigurationError("Unable to resolve dependency '" + module + "' as as dependency '"
								+ getImplementation().getName(), ex);
					}
					dependencies.add((Class<? extends BasicRuntimeModule<?>>) moduleClass);
				}
			}
		}
	}

	/**
	 * Enhances the given dependencies with all dependencies annotated on the
	 * class with the given <code>className</code> and all super classes.
	 * 
	 * @param <T>
	 *        the type of data structure of the given dependencies
	 * @param dependencies
	 *        the dependencies to enhance
	 * @param className
	 *        the name of the class to resolve dependencies from
	 * @param module
	 *        the module to resolve dependencies for
	 * 
	 * @return the given <code>dependencies</code>
	 * 
	 * @throws ConfigurationError
	 *         if some failure occurs
	 */
	public static <T extends Collection<Class<? extends BasicRuntimeModule<?>>>> T addDependencies(T dependencies,
			String className, BasicRuntimeModule<?> module) throws ConfigurationError {
		if (className == null) {
			throw new ConfigurationError("No concrete class name given as implementation instance of module '" + module
					+ "'");
		}
		Class<?> concreteImplementationClass;
		try {
			concreteImplementationClass = ConfigUtil.lookupClassForName(Object.class, className);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Unable to instantiate '" + className + "' as concreat class for '"
					+ module.getImplementation().getName() + "' as implementation instance of module '" + module + "'",
					ex);
		}
		return addDependencies(dependencies, concreteImplementationClass);
	}

	/**
	 * Adds all annotated dependencies on the given implementation class and all
	 * super classes to the given dependencies.
	 * 
	 * @param <T>
	 *        the type of the data structure of the given dependencies
	 * @param dependencies
	 *        the dependencies to enhance
	 * @param implementationClass
	 *        the class to resolve dependencies from
	 * 
	 * @return the given dependencies
	 */
	private static <T extends Collection<Class<? extends BasicRuntimeModule<?>>>> T addDependencies(T dependencies,
			Class<?> implementationClass) {
		while (implementationClass != null) {
			ServiceDependencies dependentServices = implementationClass.getAnnotation(ServiceDependencies.class);
			if (dependentServices != null) {
				dependencies.addAll(Arrays.asList(dependentServices.value()));
			}
			implementationClass = implementationClass.getSuperclass();
		}
		return dependencies;
	}

}
