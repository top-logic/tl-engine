/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for toplogic services.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLServiceUtils {

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

}
