/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * The class {@link ModuleSystem} manages all configured services in the system.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ModuleSystem extends ManagedClass {

	/**
	 * Set of configured active modules. This set is not dependency complete, i.e. not all dependencies
	 * of all modules may be contained.
	 */
	private final Set<BasicRuntimeModule<?>> _configuredServices;

	/**
	 * Mapping of the {@link BasicRuntimeModule} to the {@link BasicRuntimeModule} extending them.
	 */
	private final Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> _extensionMap;

	/**
	 * Configuration for {@link ModuleSystem}. Contains all services which are needed by the
	 * application.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<ModuleSystem> {

		/**
		 * List of modules known by {@link ModuleSystem}.
		 */
		@Key(ModuleConfiguration.MODULE)
		List<ModuleConfiguration> getModules();

	}

	/**
	 * Configuration of an module in the {@link ModuleSystem}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ModuleConfiguration extends ConfigurationItem {

		/** Configuration name for {@link #isEnabled()}. */
		String ENABLED = "value";

		/** Configuration name for {@link #getModule()}. */
		String MODULE = "key";

		/**
		 * The configured module.
		 */
		@Name(MODULE)
		@Mandatory
		@Label("Module")
		Class<?> getModule();

		/**
		 * Whether {@link #getModule()} should be started when the application starts.
		 */
		@Name(ENABLED)
		@Label("Enabled")
		boolean isEnabled();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for the {@link ModuleSystem}.
	 */
	public ModuleSystem(InstantiationContext context, Config config) {
		super(context, config);

		_configuredServices = createServicesMap(config);
		_extensionMap = ModuleUtil.INSTANCE.createExtensions(configuredServices());
	}

	private static Set<BasicRuntimeModule<?>> createServicesMap(Config config) {
		LogProtocol protocol = new LogProtocol(ModuleSystem.class);
		Set<BasicRuntimeModule<?>> configuredServices = new LinkedHashSet<>();

		addServices(configuredServices, config, protocol);

		protocol.checkErrors();
		return configuredServices;
	}

	private static void addServices(Set<BasicRuntimeModule<?>> configuredServices, Config config,
			LogProtocol protocol) {
		for (ModuleConfiguration moduleConf : config.getModules()) {
			if (!moduleConf.isEnabled()) {
				continue;
			}
			Class<?> configuredClass = moduleConf.getModule();
			if (!BasicRuntimeModule.class.isAssignableFrom(configuredClass)) {
				protocol.error("Configured class '" + configuredClass.getName() + "' must be a subclass of "
					+ BasicRuntimeModule.class.getName());
				continue;
			}

			@SuppressWarnings("unchecked")
			Class<? extends BasicRuntimeModule<?>> configuredService =
				(Class<? extends BasicRuntimeModule<?>>) configuredClass;
			BasicRuntimeModule<?> service;
			try {
				service = ModuleUtil.moduleByClass(configuredService);
			} catch (ConfigurationException ex) {
				protocol.error("Service class " + configuredService.getName() + " is not a singleton class.", ex);
				continue;
			}
			configuredServices.add(service);
		}
	}

	Set<BasicRuntimeModule<?>> configuredServices() {
		return _configuredServices;
	}

	Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> extensionMap() {
		return _extensionMap;
	}

	/**
	 * {@link BasicRuntimeModule} for the {@link ModuleSystem}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<ModuleSystem> {

		/** Sole instance of the {@link Module}. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ModuleSystem> getImplementation() {
			return ModuleSystem.class;
		}

	}
}
