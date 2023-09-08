/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig.Config.Defaults;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.vars.VariableExpander;

/**
 * Holder of the configuration of the application.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ApplicationConfig extends ManagedClass implements Reloadable {

	/**
	 * Top-level tag of an {@link ApplicationConfig.Config application configuration} file.
	 */
	public static final String ROOT_TAG = "application";

	/**
	 * Global configuration settings of a TopLogic application.
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Returns the stand alone configurations in the application
		 */
		@EntryTag("config")
		@Key(CONFIGURATION_INTERFACE_NAME)
		@Subtypes({})
		Map<Class<?>, ConfigurationItem> getConfigs();

		/**
		 * The configuration of the services in the application. The key is the
		 * full qualified class name of some service and the value the
		 * corresponding configuration.
		 */
		@EntryTag("config")
		@Key(ModuleConfiguration.SERVICE_CLASS)
		Map<Class<? extends ManagedClass>, ModuleConfiguration> getServices();

		/**
		 * The configured defaults for the configuration interfaces.
		 */
		@EntryTag("config")
		@Key(Defaults.INTERFACE)
		@Subtypes({})
		Map<Class<?>, Defaults> getDefaults();

		/**
		 * Definition of (primitive) default values for a configuration interface.
		 */
		interface Defaults extends ConfigurationItem {
			/**
			 * @see #getInterface()
			 */
			String INTERFACE = "interface";

			/**
			 * @see #getProperties()
			 */
			String PROPERTIES = "properties";

			/**
			 * The configuration interface to define default values for.
			 */
			@Name(INTERFACE)
			@Mandatory
			Class<?> getInterface();

			/**
			 * The formatted default value for a certain plain property.
			 */
			@Name(PROPERTIES)
			@DefaultContainer
			@EntryTag("property")
			@Key(StringDefaultSpec.NAME_ATTRIBUTE)
			Map<String, StringDefaultSpec> getProperties();
		}

		/**
		 * A single default value mapping.
		 */
		interface StringDefaultSpec extends NamedConfigMandatory {
			/**
			 * @see #VALUE
			 */
			String VALUE = "value";

			/**
			 * The formatted default value of the property with name {@link #getName()}.
			 */
			@Name(VALUE)
			String getValue();
		}
	}

	/**
	 * Maps the configuration interface to its application configuration
	 */
	private final ConcurrentMap<Class<?>, ConfigurationItem> _configByClass =
		new ConcurrentHashMap<>();

	/**
	 * Maps the service classes to its configuration
	 */
	private final ConcurrentMap<Class<?>, ServiceConfiguration<?>> _serviceConfigByClass =
		new ConcurrentHashMap<>();

	/**
	 * Instantiation context to delegate to.
	 */
	private volatile InstantiationContext _serviceStartupContext;

	/**
	 * Configured defaults configurations indexed by their configuration interface.
	 */
	private Map<Class<?>, Defaults> _defaults;

	/**
	 * Creates a new ApplicationConfig form the given configuration.
	 * 
	 * @param applicationConfiguration
	 *        The application configuration containing all configurations for the application
	 */
	public ApplicationConfig(InstantiationContext context, Config applicationConfiguration) {
		check(context, applicationConfiguration);
		init(context, applicationConfiguration);
	}

	private void check(InstantiationContext context, Config applicationConfiguration) {
		ConstraintChecker checker = new ConstraintChecker();
		checkItems(context, checker, applicationConfiguration.getConfigs().values());
		checkItems(context, checker, applicationConfiguration.getServices().values());
		/* Don't check the configured defaults: They are just defaults and therefore don't have to
		 * fulfill the constraints. Only their concrete usages have to fulfill the constraints. */
	}

	private void checkItems(InstantiationContext context, ConstraintChecker checker,
			Collection<? extends ConfigurationItem> items) {
		for (ConfigurationItem innerItem : items) {
			checker.check(context, innerItem);
		}
	}

	private void init(InstantiationContext context, Config applicationConfiguration) {
		_serviceStartupContext = context;
		installConfigs(applicationConfiguration);
		installServiceConfigurations(context, applicationConfiguration);
		_defaults = applicationConfiguration.getDefaults();
	}

	private void installServiceConfigurations(InstantiationContext context, Config configuration) {
		Collection<ModuleConfiguration> moduleConfigurations = configuration.getServices().values();
		for (ModuleConfiguration moduleConfiguration : moduleConfigurations) {
			Class<? extends ManagedClass> serviceClass = moduleConfiguration.getServiceClass();
			ServiceConfiguration<?> serviceConfiguration = moduleConfiguration.getInstance();
			if (serviceConfiguration != null) {
				_serviceConfigByClass.put(serviceClass, serviceConfiguration);
			} else {
				context.error("Module configuration " + moduleConfiguration + " without service configuration.");
			}
		}
	}

	private void installConfigs(Config applicationConfiguration) {
		Map<Class<?>, ConfigurationItem> allConfigs = applicationConfiguration.getConfigs();
		_configByClass.putAll(allConfigs);
	}

	/**
	 * Returnes the configuration for the given service class. The service class also serves as
	 * default implementation class.
	 * 
	 * @see #getServiceConfiguration(Class, Class)
	 */
	public <M extends ManagedClass> ServiceConfiguration<M> getServiceConfiguration(Class<M> serviceClass)
			throws ConfigurationException {
		return getServiceConfiguration(serviceClass, serviceClass);
	}
	
	/**
	 * Returns the configuration for the given service class.
	 * 
	 * @param serviceClass
	 *        A class defining a service
	 * @param defaultImplClass
	 *        A class which is taken as implementation class when a new configuration is created and
	 *        there is no default implementation class defined by the corresponding configuration
	 *        interface. In such case this class defines the actual
	 *        {@link com.top_logic.basic.module.ManagedClass.ServiceConfiguration}.
	 * @return The configuration for that service. If nothing is configured explicitly a new
	 *         instance of that configuration is created and returned. This one is shared with all
	 *         other client requested a configuration for that service class.
	 * 
	 * @throws ConfigurationException
	 *         if it is not possible to resolve a configuration interface for the given service
	 *         class or to create an instance of that configuration.
	 */
	public <M extends ManagedClass> ServiceConfiguration<M> getServiceConfiguration(Class<M> serviceClass,
			Class<? extends M> defaultImplClass) throws ConfigurationException {
		ServiceConfiguration<?> serviceConfiguration = _serviceConfigByClass.get(serviceClass);
		if (serviceConfiguration == null) {
			ServiceConfiguration<M> configItem = newServiceConfig(defaultImplClass);
			assert configItem != null : "Can not handle null configuration.";
			ServiceConfiguration<?> oldConfigItem = _serviceConfigByClass.putIfAbsent(serviceClass, configItem);
			if (oldConfigItem == null) {
				serviceConfiguration = configItem;
			} else {
				// ensure the same item is returned for each thread
				serviceConfiguration = oldConfigItem;
			}
		}

		// Map is actually type-safe because it is only filled here with correct values.
		@SuppressWarnings("unchecked")
		ServiceConfiguration<M> typedServiceConfiguration = (ServiceConfiguration<M>) serviceConfiguration;
		return typedServiceConfiguration;
	}

	/**
	 * Creates a new service configuration for the given service class.
	 * 
	 * @param implClass
	 *        If this class is a subclass of the the
	 *        {@link com.top_logic.basic.module.ManagedClass.ServiceConfiguration#getImplementationClass()
	 *        implementation class} of the new
	 *        {@link com.top_logic.basic.module.ManagedClass.ServiceConfiguration}, this class is
	 *        taken as implementation class.
	 * 
	 * @see #getServiceConfiguration(Class)
	 */
	private <M extends ManagedClass> ServiceConfiguration<M> newServiceConfig(Class<? extends M> implClass)
			throws ConfigurationException {
		Factory factory = DefaultConfigConstructorScheme.getFactory(implClass);
		Class<?> configInterface = factory.getConfigurationInterface();
		if (!ServiceConfiguration.class.isAssignableFrom(configInterface)) {
			if (PolymorphicConfiguration.class == configInterface) {
				// This can happen, if the configured service has no configuration constructor at
				// all. This is OK, if the service needs no additional configuration besides it's
				// implementation class.
				configInterface = ServiceConfiguration.class;
			} else {
				StringBuilder error = new StringBuilder();
				error.append(implClass.getName());
				error.append(" has no ");
				error.append(ServiceConfiguration.class.getName());
				error.append(" interface");
				throw new ConfigurationException(error.toString());
			}
		}
		// valid cast because it is checked before.
		@SuppressWarnings("unchecked")
		Class<? extends ServiceConfiguration<M>> serviceConfig =
			(Class<? extends ServiceConfiguration<M>>) configInterface;
		ServiceConfiguration<M> configItem = TypedConfiguration.newConfigItem(serviceConfig);
		Class<? extends M> annotatedDefaultImplementationClass = configItem.getImplementationClass();
		if (implClass == annotatedDefaultImplementationClass
			|| implClass.isAssignableFrom(annotatedDefaultImplementationClass)) {
			// default implementation class of config interface is the more specific class
		} else if (annotatedDefaultImplementationClass.isAssignableFrom(implClass)) {
			configItem.setImplementationClass(implClass);
		} else {
			StringBuilder error = new StringBuilder();
			error.append("Default implementation class ('");
			error.append(annotatedDefaultImplementationClass.getName());
			error.append("') of the config interface '");
			error.append(configInterface.getName());
			error.append("' and given implementation class '");
			error.append(implClass.getName());
			error.append("' are not compatible.");
			throw new ConfigurationException(error.toString());
		}
		return configItem;
	}

	/**
	 * Returns the Application configuration for the given configuration
	 * interface
	 * 
	 * @param <T>
	 *        the type of configuration to return
	 * @param configInterface
	 *        the configuration interface for which an instance is requested
	 */
	public <T extends ConfigurationItem> T getConfig(Class<T> configInterface) {
		ConfigurationItem configItem = _configByClass.get(configInterface);
		if (configItem == null) {
			ConfigurationItem newConfigItem = TypedConfiguration.newConfigItem(configInterface);
			assert newConfigItem != null : "Will not handle null configuration items.";
			ConfigurationItem previousValue = _configByClass.putIfAbsent(configInterface, newConfigItem);
			if (previousValue != null) {
				// ensure that all receive the same ConfigurationItem
				configItem = previousValue;
			} else {
				configItem = newConfigItem;
			}
		}
		return configInterface.cast(configItem);
	}

	/**
	 * Reloads the application configuration.
	 * 
	 * <p>
	 * The module itself is not restarted. Therefore also no dependent module is restarted.
	 * </p>
	 * 
	 * @param log
	 *        The {@link Protocol} to log errors to.
	 */
	public void reloadConfiguration(Protocol log) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		Config newConfig;
		try {
			newConfig = Module.loadConfig(context);
		} catch (ConfigurationException ex) {
			log.error("Unable to load configuration", ex);
			return;
		}

		internalShutDown();
		init(context, newConfig);
		internalStartUp();
	}

	/**
	 * Returns the singleton instance of the holder of the application
	 * configuration.
	 */
	public static ApplicationConfig getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	@Override
	protected void startUp() {
		super.startUp();
		internalStartUp();
		ReloadableManager.getInstance().addReloadable(this);
	}

	private void internalStartUp() {
		ConfigDescriptionResolver.adaptConfiguredDefaults(_defaults);
	}

	@Override
	protected void shutDown() {
		ReloadableManager.getInstance().removeReloadable(this);
		internalShutDown();
		super.shutDown();
	}

	private void internalShutDown() {
		_serviceConfigByClass.clear();
		_configByClass.clear();
	}

	/**
	 * The {@link InstantiationContext} used for service startups.
	 */
	public InstantiationContext getServiceStartupContext() {
		return _serviceStartupContext;
	}

	/**
	 * {@link BasicRuntimeModule} to access the {@link ApplicationConfig}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends BasicRuntimeModule<ApplicationConfig> {

		/**
		 * The {@link ApplicationConfig} service module singleton.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return XMLProperties.Module.DEPENDENT_ON_XML_PROPERTIES;
		}

		@Override
		public Class<ApplicationConfig> getImplementation() {
			return ApplicationConfig.class;
		}

		/**
		 * Explicitly starts up the {@link ApplicationConfig} without accessing XML configuration
		 * files.
		 *
		 * @param config
		 *        The application configuration to use.
		 */
		public void startUp(ApplicationConfig.Config config) throws ModuleException {
			if (isActive()) {
				throw new ModuleException("Must not re-start without clean shutdown in between", getImplementation());
			}
			try {
				Protocol protocol = new LogProtocol(ApplicationConfig.class);
				InstantiationContext context = new DefaultInstantiationContext(protocol);
				ApplicationConfig impl = createImplementation(context, config);
				check(protocol);

				startUp(impl);
			} catch (ConfigurationException ex) {
				throw fail(ex);
			}

			ModuleUtil.INSTANCE.markStarted(this);
		}

		@Override
		protected ApplicationConfig newImplementationInstance() throws ModuleException {
			try {
				Protocol protocol = new LogProtocol(ApplicationConfig.class);
				InstantiationContext context = new DefaultInstantiationContext(protocol);
				ConfigurationItem config = loadConfig(context);

				ApplicationConfig result = createImplementation(context, config);
				check(protocol);
				return result;
			} catch (ConfigurationException ex) {
				throw fail(ex);
			}
		}

		private void check(Protocol protocol) throws ModuleException {
			try {
				protocol.checkErrors();
			} catch (Exception ex) {
				throw fail(ex);
			}
		}

		private ApplicationConfig createImplementation(InstantiationContext context, ConfigurationItem config)
				throws ConfigurationException {
			Factory factory = DefaultConfigConstructorScheme.getFactory(ApplicationConfig.class);
			Object result = factory.createInstance(context, config);
			return (ApplicationConfig) result;
		}

		private ModuleException fail(Exception ex) {
			return new ModuleException("Problem during instantiation of application configuration.", ex,
					getImplementation());
		}

		static ApplicationConfig.Config loadConfig(InstantiationContext context) throws ConfigurationException {
			Iterable<BinaryContent> typedConfigs = XMLProperties.Module.INSTANCE.config().getTypedConfigs();
			List<BinaryContent> typedConfigList = CollectionUtil.toList(typedConfigs.iterator());
			if (typedConfigList.isEmpty()) {
				return TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
			}

			ConfigurationReader reader = new ConfigurationReader(context, configDescriptor());
			VariableExpander expander = AliasManager.getInstance().getExpander();
			ConfigurationItem config = reader.setSources(typedConfigList).setVariableExpander(expander).read();
			return (Config) config;
		}

		private static Map<String, ConfigurationDescriptor> configDescriptor() {
			ConfigurationDescriptor descr =
				TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class);
			return Collections.singletonMap(ROOT_TAG, descr);
		}

	}

	@Override
	public boolean reload() {
		BufferingProtocol protocol = new BufferingProtocol();
		reloadConfiguration(protocol);
		boolean hasErrors = protocol.hasErrors();
		if (hasErrors) {
			Logger.error("Reload failed: " + protocol.getError(), ApplicationConfig.class);
		}
		return !hasErrors;
	}

	@Override
	public String getName() {
		return "Typed application configuration";
	}

	@Override
	public String getDescription() {
		return "Holder for the configuration of the application.";
	}

	@Override
	public boolean usesXMLProperties() {
		return false;
	}

}
