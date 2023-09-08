/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Collection;
import java.util.Properties;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RuntimeModuleProxy<M extends ManagedClass> extends RuntimeModule<M> {

	private RuntimeModule<? extends M> concreteInstance;
	private final Class<M> implementation;

	protected RuntimeModuleProxy(Class<M> implementation) {
		this.implementation = implementation;
	}

	@Override
	public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
		if (concreteInstance != null) {
			return concreteInstance.getDependencies();
		} else {
			return getImplementationModule().getDependencies();
		}
	}

	private RuntimeModule<? extends M> getImplementationModule() {
		IterableConfiguration config = Configuration.getConfiguration(implementation);
		
		Properties properties = config.getProperties();
		try {
			return getImplementationModuleInstance(properties);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	private RuntimeModule<? extends M> getImplementationModuleInstance(Properties properties) throws ConfigurationException {
		String concreteClassName = getConcreteImplementationClass(properties);
		String moduleClassName = concreteClassName + "$Module";
		try {
			Class<?> moduleClass = Class.forName(moduleClassName);
			Object singleton = ConfigUtil.getSingleton(moduleClass);
			
			//check whether the singleton is a RuntimeModule
			if (!RuntimeModule.class.isInstance(singleton)) {
				throw new ConfigurationException("Configuration for '" + implementation.getName() + "' not correct. Class with name '"
						+ moduleClass.getName() + "' must be an instance of '" + RuntimeModule.class.getName() + "'");
			}
			RuntimeModule<?> runtimeModule = (RuntimeModule<?>) singleton;
			
			// check whether the runtimeModule is a RuntimeModule<M>
			if (!implementation.isAssignableFrom(runtimeModule.getImplementation())) {
				throw new ConfigurationException("The configured Module '" + moduleClassName + "' does not represent the class '"
						+ implementation.getName() + "'");
			}
			
			return (RuntimeModule<? extends M>) runtimeModule;
		} catch (ClassNotFoundException ex) {
			throw new ConfigurationException("There is module class with name '" + moduleClassName + "'", ex);
		}
	}

	private String getConcreteImplementationClass(Properties properties) throws ConfigurationException {
		String concreteClassName = properties.getProperty(getClassPropertyName());
		if (concreteClassName == null) {
			throw new ConfigurationException("Configuration for '" + implementation.getName() + "' not correct. No value for property '"
					+ getClassPropertyName() + "' set.");
		}
		return concreteClassName;
	}

	protected String getClassPropertyName() {
		return ConfigUtil.DEFAULT_CLASS_PROPERTY;
	}
	

	@Override
	protected M newImplementationInstance(IterableConfiguration config) throws ModuleException, ConfigurationException {
		if (concreteInstance == null) {
			throw new IllegalStateException("This proxy currently has no implementation to dispatch to");
		}
		
		return concreteInstance.newImplementationInstance(config);
	}

	@Override
	void startUp() throws ModuleException {
		if (concreteInstance != null) {
			assert isActive() : "Proxy is not active but has implementation.";
		} else {
			concreteInstance = getImplementationModule();
		}
		super.startUp();

	}

	@Override
	void shutDown() {
		super.shutDown();
		concreteInstance = null;
	}
	
	@Override
	public Class<M> getImplementation() {
		return implementation;
	}

}
