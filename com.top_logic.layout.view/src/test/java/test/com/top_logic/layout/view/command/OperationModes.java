/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.base.operation.OperationModeService;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;

/**
 * Runs test code in a given {@link OperationMode}.
 *
 * <p>
 * The mode of an application is boot-time configuration, resolved by the
 * {@link OperationModeService} when it starts. {@link #inMode(OperationMode, Runnable)} sets the
 * configured mode of the started service and restarts it, and restores the mode it found
 * afterwards.
 * </p>
 */
public class OperationModes {

	/**
	 * Runs the given test code with the {@link OperationModeService} reporting the given mode.
	 *
	 * @param mode
	 *        The mode the service reports while the body runs.
	 * @param body
	 *        The test code.
	 */
	public static void inMode(OperationMode mode, Runnable body) {
		OperationModeService.Config config = serviceConfig();
		OperationMode before = config.getMode();
		restart(config, mode);
		try {
			body.run();
		} finally {
			restart(config, before);
		}
	}

	private static void restart(OperationModeService.Config config, OperationMode mode) {
		try {
			ModuleUtil.INSTANCE.restart(OperationModeService.Module.INSTANCE, () -> config.setMode(mode));
		} catch (RestartException ex) {
			throw new IllegalStateException("Cannot restart the operation mode service in mode " + mode + ".", ex);
		}
	}

	private static OperationModeService.Config serviceConfig() {
		try {
			return (OperationModeService.Config) ApplicationConfig.getInstance()
				.getServiceConfiguration(OperationModeService.class);
		} catch (ConfigurationException ex) {
			throw new IllegalStateException("No configuration of the operation mode service.", ex);
		}
	}
}
