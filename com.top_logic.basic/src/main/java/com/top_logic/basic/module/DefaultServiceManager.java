/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Arrays;
import java.util.Collection;

import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * The class {@link DefaultServiceManager} supposes that the services are
 * defined in the standard way, i.e. the properties section has a
 * {@link ConfigUtil#DEFAULT_CLASS_PROPERTY class} attribute with a
 * {@link ConfigUtil#CONFIG_CONSTRUCTOR_SIGNATURE} constructor.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultServiceManager<M extends ManagedClass> implements ServiceManager<M> {
	
	private final Class<? extends M> implementationClass;

	public DefaultServiceManager(Class<? extends M> implementationClass) {
		this.implementationClass = implementationClass;
	}

	@Override
	public M createImplementation(IterableConfiguration config) throws ModuleException, ConfigurationException {
		return ConfigUtil.getConfiguredInstance(implementationClass, config.getProperties(), null);
	}

	@Override
	public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies(IterableConfiguration config) throws ConfigurationException {
		Class<?> configuredClass = ConfigUtil.getClassForNameMandatory(Object.class, config.getProperties(), ConfigUtil.DEFAULT_CLASS_PROPERTY);
		ServiceDependencies serviceDependencies = configuredClass.getAnnotation(ServiceDependencies.class);
		if (serviceDependencies == null) {
			return BasicRuntimeModule.NO_DEPENDENCIES;
		} else {
			Class<? extends BasicRuntimeModule<?>>[] dependencies = serviceDependencies.value();
			return Arrays.asList(dependencies);
		}
	}

}

