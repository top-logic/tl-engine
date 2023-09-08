/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.basic.module;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;

/**
 * {@link BasicRuntimeModule} for services which are configured using the typed
 * configuration.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TypedRuntimeModule<M extends ManagedClass> extends BasicRuntimeModule<M> {

	/**
	 * Configuration of a {@link TypedRuntimeModule}
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface ModuleConfiguration extends ConfigurationItem {

		/** Name of the attribute to set {@link #getServiceClass} */
		String SERVICE_CLASS = "service-class";

		/**
		 * The class of the service, this module represents.
		 * 
		 * @see TypedRuntimeModule#getImplementation()
		 */
		@Name(SERVICE_CLASS)
		Class<? extends ManagedClass> getServiceClass();

		/**
		 * @see #getServiceClass()
		 */
		void setServiceClass(Class<? extends ManagedClass> value);

		/**
		 * Whether this module is enabled
		 */
		@BooleanDefault(true)
		boolean isEnabled();

		/**
		 * @see #isEnabled()
		 */
		void setEnabled(boolean value);

		/**
		 * The configuration for the actual service
		 */
		@Subtypes({})
		ManagedClass.ServiceConfiguration<?> getInstance();

		/**
		 * @see #getInstance()
		 */
		void setInstance(ManagedClass.ServiceConfiguration<?> value);
	}

	/**
	 * Computation that needs the {@link ApplicationConfig application configuration} and returns
	 * the {@link ServiceConfiguration} for the {@link TypedRuntimeModule#getImplementation()
	 * implementation} class.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	class ConfigurationFetch implements Computation<ServiceConfiguration<M>> {

		@Override
		public ServiceConfiguration<M> run() {
			ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
			try {
				return applicationConfig.getServiceConfiguration(getImplementation(), getDefaultImplementationClass());
			} catch (ConfigurationException ex) {
				String message = "Unable to get service configuration for '" + getImplementation().getName() + "'";
				throw new ModuleRuntimeException(message, ex);
			}
		}
	}

	private LogProtocol logProtocol() {
		return new LogProtocol(TypedRuntimeModule.class);
	}

	private Set<Class<? extends BasicRuntimeModule<?>>> createDepencencies(Protocol log) {
		return createDepencencies(log, fetchConfig(log));
	}

	private Set<Class<? extends BasicRuntimeModule<?>>> createDepencencies(Protocol log,
			ServiceConfiguration<M> serviceConfig) {
		Set<Class<? extends BasicRuntimeModule<?>>> dependencies = resolveDependencies(log, serviceConfig);
		log.checkErrors();

		assert dependencies != null;
		return dependencies;
	}

	private ServiceConfiguration<M> fetchConfig(Log log) {
		ServiceConfiguration<M> serviceConfig =
			ModuleUtil.INSTANCE.inModuleContext(ApplicationConfig.Module.INSTANCE, new ConfigurationFetch());
		assert serviceConfig != null;

		ensureCorrectImplClass(log, serviceConfig);
		return serviceConfig;
	}

	/**
	 * Checks that the {@link ServiceConfiguration#getImplementationClass() implementation class} is
	 * a subtype of {@link #getImplementation()} the service class.
	 * 
	 * <p>
	 * In case it is not set or of wrong type {@link #getDefaultImplementationClass() default
	 * implementation class} is set to the service class.
	 * </p>
	 */
	private void ensureCorrectImplClass(Log log, ServiceConfiguration<M> config) {
		Class<?> configuredClass = config.getImplementationClass();
		if (configuredClass == null) {
			log.info("No implementation class configured. Use '" + getDefaultImplementationClass()
					+ "' as implementation class.", Protocol.VERBOSE);
			config.setImplementationClass(getDefaultImplementationClass());
		} else {
			if (!(getImplementation().isAssignableFrom(configuredClass))) {
				config.setImplementationClass(getImplementation());
				StringBuilder errorMessage = new StringBuilder();
				errorMessage.append("Configured class '");
				errorMessage.append(configuredClass.getName());
				errorMessage.append("' must be an extension of '");
				errorMessage.append(getImplementation());
				errorMessage.append('\'');
				log.error(errorMessage.toString());
			}
		}
	}

	/**
	 * Resolves dependencies needed for the service.
	 */
	private Set<Class<? extends BasicRuntimeModule<?>>> resolveDependencies(Protocol protocol,
			ServiceConfiguration<M> serviceConfig) {
		Set<Class<? extends BasicRuntimeModule<?>>> dependencies = new HashSet<>();
		dependencies.add(ApplicationConfig.Module.class);
		addConfiguredDependencies(protocol, dependencies, serviceConfig);
		addProgrammaticDependencies(dependencies, implementationClass(serviceConfig));
		return dependencies;
	}

	private Class<? extends M> implementationClass(ServiceConfiguration<M> serviceConfig) {
		Class<? extends M> implementationClass = serviceConfig.getImplementationClass();
		assert implementationClass != null : "implementation class must be given";
		return implementationClass;
	}

	/**
	 * Adds all dependencies given in the configuration to the dependencies
	 */
	private void addConfiguredDependencies(Protocol protocol, Set<Class<? extends BasicRuntimeModule<?>>> dependencies,
			ServiceConfiguration<M> serviceConfig) {
		Map<Class<?>, Boolean> configuredDependencies = serviceConfig.getDependencies();
		for (Entry<Class<?>, Boolean> entry : configuredDependencies.entrySet()) {
			if (entry.getValue()) {
				Class<?> key = entry.getKey();
				try {
					dependencies.add(ModuleUtil.getModuleClass(key));
				} catch (ConfigurationException ex) {
					protocol.error("Unable to resolve module from '" + key.getName()
							+ "' as configured dependency of '" + getImplementation() + "'", ex);
					continue;
				}
			}
		}
	}

	/**
	 * Get extension.
	 */
	private Class<? extends BasicRuntimeModule<?>> getExtension(Protocol log) {
		return getExtension(log, fetchConfig(log));
	}

	private Class<? extends BasicRuntimeModule<?>> getExtension(Protocol log, ServiceConfiguration<M> serviceConfig) {
		Class<? extends M> implementationClass = implementationClass(serviceConfig);
		Class<?> serviceClass = implementationClass;
		Class<? extends BasicRuntimeModule<?>> extended = null;
		while (serviceClass != null) {
			ServiceExtensionPoint extendedServiceAnnotation = serviceClass.getAnnotation(ServiceExtensionPoint.class);
			if (extendedServiceAnnotation != null) {
				if (extended == null) {
					extended = extendedServiceAnnotation.value();
				} else {
					StringBuilder msg = new StringBuilder();
					msg.append("Duplicate ");
					msg.append(ServiceExtensionPoint.class.getSimpleName());
					msg.append(" at ");
					msg.append(implementationClass);
					msg.append(": ");
					msg.append(extended.getName());
					msg.append(", ");
					msg.append(extendedServiceAnnotation.value().getName());
					log.error(msg.toString());
				}
			}
			serviceClass = serviceClass.getSuperclass();
		}
		return extended;
	}

	/**
	 * Adds the annotated dependencies for the given service class and all super
	 * classes.
	 */
	private void addProgrammaticDependencies(Set<Class<? extends BasicRuntimeModule<?>>> dependencies,
			Class<?> serviceClass) {
		while (serviceClass != null) {
			ServiceDependencies dependentServices = serviceClass.getAnnotation(ServiceDependencies.class);
			if (dependentServices != null) {
				dependencies.addAll(Arrays.asList(dependentServices.value()));
			}
			serviceClass = serviceClass.getSuperclass();
		}

	}

	@Override
	public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
		Protocol protocol = logProtocol();
		Set<Class<? extends BasicRuntimeModule<?>>> depencencies = createDepencencies(protocol);
		protocol.checkErrors();
		return depencencies;
	}

	@Override
	public Class<? extends BasicRuntimeModule<?>> getExtendedService() {
		Protocol protocol = logProtocol();
		Class<? extends BasicRuntimeModule<?>> extension = getExtension(protocol);
		protocol.checkErrors();
		return extension;
	}

	@Override
	protected M newImplementationInstance() throws ModuleException {
		final InstantiationContext context = ApplicationConfig.getInstance().getServiceStartupContext();
		M result = createImplementation(context, fetchConfig(context));
		if (context.hasErrors()) {
			try {
				context.checkErrors();
			} catch (Exception ex) {
				throw new ModuleException("Unable to instantiate service '" + getImplementation().getName() + "'.", ex,
					getImplementation());
			}
		}
		return result;
	}

	/**
	 * Returns the implementation class of the service when no concrete class is configured.
	 * 
	 * @return This implementation returns the {@link #getImplementation() service class}.
	 */
	protected Class<? extends M> getDefaultImplementationClass() {
		return getImplementation();
	}

	/**
	 * Creates the actual implementation instance.
	 * 
	 * <p>
	 * A {@link ThreadContext} is available if needed.
	 * </p>
	 * 
	 * @param context
	 *        the context to instantiate configurations
	 * @param configuration
	 *        the configuration of
	 * 
	 * @return The instance later returned by {@link #newImplementationInstance()}
	 */
	protected M createImplementation(InstantiationContext context, ServiceConfiguration<? extends M> configuration) {
		return context.getInstance(configuration);
	}

	/**
	 * Reloads the configuration this module depends on and restarts the service.
	 * 
	 * <p>
	 * Note: The formerly fetched {@link BasicRuntimeModule#getImplementationInstance() instance} is
	 * invalid now.
	 * </p>
	 * 
	 * @param log
	 *        The {@link Protocol} to log errors to.
	 */
	public void reloadConfiguration(Protocol log) {
		ApplicationConfig.getInstance().reloadConfiguration(log);
		if (log.hasErrors()) {
			return;
		}
		try {
			ModuleUtil.INSTANCE.restart(this, null);
		} catch (RestartException ex) {
			Class<? extends ManagedClass> failedService = ex.getFailedService();
			log.error("Unable to restart " + failedService.getName(), ex);
		}
	}

}
