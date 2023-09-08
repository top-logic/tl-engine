/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * The class {@link MultiRuntimeModule} manages many sub services.
 * 
 * @param <SI>
 *        the type of inner services which are managed by the described service
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MultiRuntimeModule<M extends ManagedClass, SI extends ManagedClass> extends RuntimeModule<M> {

	private static final String CONFIG_PREFIX = "service.";
	private final int PREFIX_SIZE = CONFIG_PREFIX.length();

	private final ServiceManager<SI> serviceManager;
	private Map<String, SI> services;

	protected MultiRuntimeModule(ServiceManager<SI> configuredServices) {
		this.serviceManager = configuredServices;
	}

	@Override
	public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
		try {
			return createDependencies();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Error during computing dependencies", ex);
		}

	}

	private Collection<? extends Class<? extends BasicRuntimeModule<?>>> createDependencies() throws ConfigurationException {
		ServiceDependencies serviceAnnotation = getImplementation().getAnnotation(ServiceDependencies.class);
		Set<Class<? extends BasicRuntimeModule<?>>> result;
		if (serviceAnnotation == null) {
			result = new HashSet<>();
		} else {
			result = new HashSet<>(Arrays.asList(serviceAnnotation.value()));
		}

		IterableConfiguration config = getServiceConfiguration();
		Properties properties = config.getProperties();

		if (properties.containsKey(CONFIG_PREFIX)) {
			IterableConfiguration subConfig = getSubConfig(properties.getProperty(CONFIG_PREFIX));
			result.addAll(serviceManager.getDependencies(subConfig));
		} else {
			for (Entry<Object, Object> entry : properties.entrySet()) {
				final String key = (String) entry.getKey();
				if (!key.startsWith(CONFIG_PREFIX)) {
					continue;
				}
				IterableConfiguration subConfiguration = getSubConfig((String) entry.getValue());
				result.addAll(serviceManager.getDependencies(subConfiguration));
			}
		}

		return result;
	}

	@Override
	void startUp() throws ModuleException {
		try {
			this.services = createServices();
		} catch (ConfigurationException ex) {
			throw new ModuleException("Unable to start Module for '" + getImplementation().getName() + "' as creation of sub services failed", ex, this.getImplementation());
		}
		super.startUp();
	}

	/**
	 * creates the map of inner services.
	 */
	private Map<String, SI> createServices() throws ModuleException, ConfigurationException {
		IterableConfiguration config = Configuration.getConfiguration(getImplementation());
		Properties properties = config.getProperties();
		Map<String, SI> configuredServices;
		if (properties.containsKey(CONFIG_PREFIX)) {
			IterableConfiguration subConfig = getSubConfig(properties.getProperty(CONFIG_PREFIX));
			SI configuredService = serviceManager.createImplementation(subConfig);
			configuredServices = Collections.singletonMap(CONFIG_PREFIX, configuredService);
		} else {
			configuredServices = new HashMap<>();
			for (Entry<Object, Object> entry : properties.entrySet()) {
				final String key = (String) entry.getKey();
				if (!key.startsWith(CONFIG_PREFIX)) {
					continue;
				}
				IterableConfiguration subConfiguration = getSubConfig((String) entry.getValue());
				SI configuredService = serviceManager.createImplementation(subConfiguration);
				configuredServices.put(key.substring(PREFIX_SIZE), configuredService);
			}
		}

		return configuredServices;
	}

	/**
	 * returns the configuration section with the given name. If there is no
	 * such configuration section (i.e. the returned properties are empty), then
	 * it is supposed the given name is the implementation class and a simple
	 * configuration with the given service section as value of the
	 * {@link ConfigUtil#DEFAULT_CLASS_PROPERTY class} property is build.
	 * 
	 * @param serviceConfiguration
	 *        the name of the section to read config from.
	 */
	private IterableConfiguration getSubConfig(String serviceConfiguration) throws ConfigurationException {
		IterableConfiguration subConfiguration = Configuration.getConfigurationByName(serviceConfiguration);
		Properties props = subConfiguration.getProperties();
		if (!props.isEmpty()) {
			return subConfiguration;
		}
		
		// suppose serviceConfiguration is a class name
		final String configuredClass = serviceConfiguration;
		try {
			// try to initialize
			Class.forName(configuredClass);
		} catch (ClassNotFoundException ex) {
			throw new ConfigurationException(serviceConfiguration + " is neither a class nor a configuration section with entries");
		}
		props.put(ConfigUtil.DEFAULT_CLASS_PROPERTY, configuredClass);
		
		// must build new as props are copy of the internal map
		return new IterableConfiguration(props);
	}

	@Override
	void shutDown() {
		super.shutDown();
		for (SI service : services.values()) {
			service.shutDown();
		}
		this.services = null;
	}

	public Map<String, SI> getServices() {
		return services;
	}

	public SI getServiceByName(String name) {
		return services.get(name);
	}
}
