/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.autoconf.InAppServiceConfigStore;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for toplogic services.
 * 
 * <p>
 * Besides starting and restarting services, this class provides the operations for inspecting and
 * changing the configuration a service is started with. A configuration stored in the application
 * is kept in the autoconf folder of the top level module (see {@link InAppServiceConfigStore}) and
 * becomes effective when the service is started the next time. Until then, its module is
 * {@link BasicRuntimeModule#isConfigurationChanged() marked as changed}.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLServiceUtils {

	/**
	 * A configuration stored in the application replaces the service configuration of the
	 * underlying configuration layers, so that a value removed in an editor does not reappear from
	 * the base configuration.
	 */
	private static final boolean OVERRIDE_BASE_CONFIG = true;

	/**
	 * Reload the {@link ApplicationConfig}.
	 */
	public static void reloadConfigurations() throws IOError {
		try {
			XMLProperties.Module.INSTANCE.config().resolve();

			ApplicationConfig.getInstance().reload();
		} catch (IOException exception) {
			Logger.error("XMLProperties could not be resolved.", exception, TLServiceUtils.class);

			throw new IOError(exception);
		}
	}

	/**
	 * Restarts the given service and wraps the thrown exception into a {@link TopLogicException}.
	 */
	public static void restartService(BasicRuntimeModule<?> module) {
		try {
			ModuleUtil.INSTANCE.restart(module, null);
		} catch (RestartException exception) {
			throw new TopLogicException(errorRestartMessage(module), exception);
		}
	}

	/**
	 * {@link ResKey} when restarting module fails.
	 */
	public static ResKey errorRestartMessage(BasicRuntimeModule<?> module) {
		return I18NConstants.SERVICE_RESTART_ERROR.fill(getServiceName(module));
	}

	/**
	 * Starts the given service and wraps the thrown exception into a {@link TopLogicException}.
	 */
	public static  void startService(BasicRuntimeModule<?> module) {
		try {
			ModuleUtil.INSTANCE.startUp(module);
		} catch (IllegalArgumentException | ModuleException exception) {
			throw new TopLogicException(errorStartMessage(module), exception);
		}
	}

	/**
	 * {@link ResKey} when starting module fails.
	 */
	public static ResKey errorStartMessage(BasicRuntimeModule<?> module) {
		return I18NConstants.SERVICE_START_ERROR.fill(getServiceName(module));
	}

	private static String getServiceName(BasicRuntimeModule<?> module) {
		return ModuleUtil.INSTANCE.getModuleName(module);
	}

	/**
	 * Whether the service of the given module has a configuration.
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether the application configuration contains a configuration for the service.
	 */
	public static boolean hasServiceConfiguration(BasicRuntimeModule<?> module) {
		try {
			return ApplicationConfig.getInstance().getServiceConfiguration(module.getImplementation()) != null;
		} catch (ConfigurationException exception) {
			return false;
		}
	}

	/**
	 * An editable copy of the current configuration of the service of the given module.
	 *
	 * @param module
	 *        The module of the service.
	 * @return A copy of the configuration that can be modified and passed to
	 *         {@link #storeServiceConfiguration(BasicRuntimeModule, ServiceConfiguration)}, or
	 *         <code>null</code>, if the service has no configuration.
	 */
	public static ServiceConfiguration<? extends ManagedClass> getServiceConfiguration(BasicRuntimeModule<?> module) {
		try {
			ServiceConfiguration<? extends ManagedClass> config =
				ApplicationConfig.getInstance().getServiceConfiguration(module.getImplementation());
			if (config == null) {
				return null;
			}
			return ConfigCopier.copy(config);
		} catch (ConfigurationException exception) {
			return null;
		}
	}

	/**
	 * Whether the configuration of the service of the given module may be changed in the
	 * application.
	 *
	 * @param module
	 *        The module of the service.
	 * @return <code>false</code>, if the implementation class of the service is annotated with
	 *         {@link InApp} with value <code>false</code>, <code>true</code> otherwise.
	 */
	public static boolean isInAppEditable(BasicRuntimeModule<?> module) {
		InApp annotation = module.getImplementation().getAnnotation(InApp.class);
		return annotation == null || annotation.value();
	}

	/**
	 * Whether a configuration of the service of the given module is stored in the application.
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether the file {@link InAppServiceConfigStore#fileFor(BasicRuntimeModule)} exists.
	 *
	 * @see #resetServiceConfiguration(BasicRuntimeModule)
	 */
	public static boolean hasInAppConfiguration(BasicRuntimeModule<?> module) {
		return InAppServiceConfigStore.fileFor(module).exists();
	}

	/**
	 * Stores the given configuration of the service of the given module in the application.
	 *
	 * <p>
	 * The stored configuration replaces the configuration of the underlying configuration layers.
	 * After storing, the application configuration is reloaded and the module is
	 * {@link BasicRuntimeModule#markConfigurationChanged() marked as changed}. The configuration
	 * becomes effective with the next start of the service.
	 * </p>
	 *
	 * @param module
	 *        The module of the service.
	 * @param config
	 *        The configuration to store, see {@link #getServiceConfiguration(BasicRuntimeModule)}.
	 * @throws TopLogicException
	 *         When the configuration violates its constraints, or cannot be stored.
	 */
	public static void storeServiceConfiguration(BasicRuntimeModule<?> module, ServiceConfiguration<?> config) {
		checkConstraints(config);

		File file;
		try {
			file = InAppServiceConfigStore.store(module, config, OVERRIDE_BASE_CONFIG);
		} catch (Exception exception) {
			throw new TopLogicException(I18NConstants.SERVICE_CONFIG_SAVE_ERROR, exception);
		}
		Logger.info(file.getAbsolutePath() + " successfully written.", TLServiceUtils.class);

		reloadConfigurations();
		module.markConfigurationChanged();
	}

	/**
	 * Removes the configuration of the service of the given module stored in the application.
	 *
	 * <p>
	 * When a stored configuration has been removed, the application configuration is reloaded and
	 * the module is {@link BasicRuntimeModule#markConfigurationChanged() marked as changed}. The
	 * configuration of the underlying configuration layers becomes effective with the next start of
	 * the service.
	 * </p>
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether a stored configuration has been removed. <code>false</code>, if there was no
	 *         stored configuration, or it could not be deleted.
	 *
	 * @see #hasInAppConfiguration(BasicRuntimeModule)
	 */
	public static boolean resetServiceConfiguration(BasicRuntimeModule<?> module) {
		File file = InAppServiceConfigStore.fileFor(module);
		if (!file.exists()) {
			return false;
		}
		if (!file.delete()) {
			Logger.error("In-App service configuration file " + file.getAbsolutePath() + " could not be deleted.",
				TLServiceUtils.class);
			return false;
		}

		reloadConfigurations();
		module.markConfigurationChanged();
		return true;
	}

	/**
	 * Checks the constraints of the given service configuration.
	 *
	 * @param config
	 *        The configuration to check.
	 * @throws TopLogicException
	 *         When a constraint is violated, holding the messages of all violations.
	 */
	public static void checkConstraints(ServiceConfiguration<?> config) {
		BufferingI18NLog checkingProtocol = new BufferingI18NLog();
		new ConstraintChecker().check(checkingProtocol, config);
		ResKey[] errorKeys =
			checkingProtocol.getEntries()
				.stream()
				.filter(event -> event.getLevel() == Level.ERROR)
				.map(BufferingI18NLog.Entry::getMessage)
				.toArray(ResKey[]::new);
		if (errorKeys.length > 0) {
			throw new TopLogicException(I18NConstants.SERVICE_CONFIGURATION_INVALID__ERRORS.fill(errorKeys));
		}
	}

}
