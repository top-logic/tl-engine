/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.WaitForMaintenanceModeAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Default implementation for usage of {@link WaitForMaintenanceModeActionOp}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WaitForMaintenanceModeActionOp extends AbstractApplicationActionOp<WaitForMaintenanceModeAction> {

	/**
	 * Creates a new {@link WaitForMaintenanceModeActionOp} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link WaitForMaintenanceModeActionOp}.
	 */
	public WaitForMaintenanceModeActionOp(InstantiationContext context, WaitForMaintenanceModeAction config) {
		super(context, config);
		switch (config.getState()) {
			case MaintenanceWindowManager.IN_MAINTENANCE_MODE:
			case MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE:
			case MaintenanceWindowManager.DEFAULT_MODE:
				// legal state.
				break;
			default:
				context.error("Illegal state: " + config.getState());
		}
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		long maxSleep = config.getMaxSleep();
		if (maxSleep <= 0) {
			MaintenanceWindowManager mwm = MaintenanceWindowManager.getInstance();
			int maintenanceModeState = mwm.getMaintenanceModeState();
			if (maintenanceModeState != config.getState()) {
				throw ApplicationAssertions.fail(config, "Expected maintenance mode state not reached. Expected: "
					+ config.getState() + ", Actual: " + maintenanceModeState);
			}
		}

		int maintenanceModeState = waitForState(maxSleep);

		if (maintenanceModeState != config.getState()) {
			ApplicationAssertions.fail(config, "Expected maintenance mode state not reached. Expected: "
				+ config.getState() + ", Actual: " + maintenanceModeState);
		}

		return argument;
	}

	private int waitForState(long remainingTime) throws InterruptedException {
		long sleepTime = 10;
		while (true) {
			MaintenanceWindowManager mwm = MaintenanceWindowManager.getInstance();
			int currentState = mwm.getMaintenanceModeState();
			if (currentState == config.getState()) {
				// expected state reached. Stop now.
				return currentState;
			}
			if (remainingTime < 0) {
				return currentState;
			}
			remainingTime -= sleepTime;
			Thread.sleep(sleepTime);
		}
	}

}
