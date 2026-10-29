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
import com.top_logic.basic.module.ConfigurationBaseline;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.autoconf.InAppServiceConfigBaseline;
import com.top_logic.util.autoconf.InAppServiceConfigStore;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for toplogic services.
 * 
 * <p>
 * Besides starting and restarting services, this class provides the operations for inspecting and
 * changing the configuration a service is started with. A configuration stored in the application
 * is kept in the autoconf folder of the top level module (see {@link InAppServiceConfigStore}) and
 * becomes effective when the service is started the next time. Before the first change since the
 * start of a service, the state of its stored configuration is recorded as
 * {@link BasicRuntimeModule#getConfigurationBaseline() baseline}. As long as the stored
 * configuration differs from this baseline, the module
 * {@link BasicRuntimeModule#isConfigurationChanged() reports a changed configuration}, and
 * {@link #discardConfigurationChanges(BasicRuntimeModule)} restores the baseline.
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
	 * @return Whether the service has a configuration, see
	 *         {@link ApplicationConfig#hasServiceConfiguration(Class)}. A service whose module is
	 *         no {@link TypedRuntimeModule} is not created from a configuration and has none.
	 * @throws TopLogicException
	 *         When it cannot be determined whether the service has a configuration.
	 */
	public static boolean hasServiceConfiguration(BasicRuntimeModule<?> module) {
		if (!(module instanceof TypedRuntimeModule<?>)) {
			// The module creates the service itself, e.g. the ApplicationConfig.
			return false;
		}
		try {
			return ApplicationConfig.getInstance().hasServiceConfiguration(module.getImplementation());
		} catch (ConfigurationException exception) {
			throw errorGettingConfiguration(module, exception);
		}
	}

	/**
	 * An editable copy of the current configuration of the service of the given module.
	 *
	 * @param module
	 *        The module of the service.
	 * @return A copy of the configuration that can be modified and passed to
	 *         {@link #storeServiceConfiguration(BasicRuntimeModule, ServiceConfiguration)}, or
	 *         <code>null</code>, if the service has no configuration, see
	 *         {@link #hasServiceConfiguration(BasicRuntimeModule)}.
	 * @throws TopLogicException
	 *         When the configuration of the service cannot be loaded.
	 */
	public static ServiceConfiguration<? extends ManagedClass> getServiceConfiguration(BasicRuntimeModule<?> module) {
		if (!hasServiceConfiguration(module)) {
			return null;
		}
		try {
			return ConfigCopier.copy(ApplicationConfig.getInstance().getServiceConfiguration(module.getImplementation()));
		} catch (ConfigurationException exception) {
			throw errorGettingConfiguration(module, exception);
		}
	}

	private static TopLogicException errorGettingConfiguration(BasicRuntimeModule<?> module,
			ConfigurationException exception) {
		return new TopLogicException(
			I18NConstants.ERROR_GETTING_SERVICE_CONFIGURATION__SERVICE.fill(getServiceName(module)), exception);
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
	 * After storing, the application configuration is reloaded. The configuration becomes
	 * effective with the next start of the service, until then the module
	 * {@link BasicRuntimeModule#isConfigurationChanged() reports a changed configuration}.
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

		synchronized (module) {
			File file;
			try {
				recordBaseline(module);
				file = InAppServiceConfigStore.store(module, config, OVERRIDE_BASE_CONFIG);
			} catch (Exception exception) {
				throw new TopLogicException(I18NConstants.SERVICE_CONFIG_SAVE_ERROR, exception);
			}
			Logger.info(file.getAbsolutePath() + " successfully written.", TLServiceUtils.class);

			reloadConfigurations();
		}
	}

	/**
	 * Removes the configuration of the service of the given module stored in the application.
	 *
	 * <p>
	 * When a stored configuration has been removed, the application configuration is reloaded. The
	 * configuration of the underlying configuration layers becomes effective with the next start of
	 * the service, until then the module
	 * {@link BasicRuntimeModule#isConfigurationChanged() reports a changed configuration}, unless
	 * the service was started without a stored configuration.
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
		synchronized (module) {
			File file = InAppServiceConfigStore.fileFor(module);
			if (!file.exists()) {
				return false;
			}
			recordBaseline(module);
			if (!file.delete()) {
				Logger.error("In-App service configuration file " + file.getAbsolutePath() + " could not be deleted.",
					TLServiceUtils.class);
				return false;
			}

			reloadConfigurations();
			return true;
		}
	}

	/**
	 * Discards the changes of the stored configuration of the service of the given module made since
	 * the service was started.
	 *
	 * <p>
	 * Restores the stored configuration to the recorded
	 * {@link BasicRuntimeModule#getConfigurationBaseline() baseline}, reloads the application
	 * configuration, and drops the baseline. Afterwards, the stored configuration is the one the
	 * running service was started with.
	 * </p>
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether the restored configuration differs from the discarded one.
	 *         <code>false</code>, if it has not been changed since the start of the service.
	 * @throws TopLogicException
	 *         When the stored configuration cannot be restored.
	 */
	public static boolean discardConfigurationChanges(BasicRuntimeModule<?> module) {
		synchronized (module) {
			ConfigurationBaseline baseline = module.getConfigurationBaseline();
			if (baseline == null) {
				return false;
			}
			boolean changed = baseline.isChanged();
			// Also a stored configuration equal in content to the one in use is restored, so that the
			// stored state is exactly the one the service was started with.
			try {
				baseline.restore();
			} catch (IOException exception) {
				throw new TopLogicException(I18NConstants.SERVICE_CONFIG_RESTORE_ERROR, exception);
			}
			reloadConfigurations();
			module.discardConfigurationBaseline();
			return changed;
		}
	}

	private static void recordBaseline(BasicRuntimeModule<?> module) {
		module.recordConfigurationBaseline(() -> InAppServiceConfigBaseline.capture(module));
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
