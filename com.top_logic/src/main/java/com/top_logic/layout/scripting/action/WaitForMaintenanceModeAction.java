/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.runtime.action.WaitForMaintenanceModeActionOp;

/**
 * {@link AwaitAction} until {@link MaintenanceWindowManager} has reached certain
 * {@link MaintenanceWindowManager#getMaintenanceModeState() state}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WaitForMaintenanceModeAction extends AwaitAction {

	@Override
	Class<? extends WaitForMaintenanceModeActionOp> getImplementationClass();

	/**
	 * The {@link MaintenanceWindowManager#getMaintenanceModeState() maintenance mode state} to wait
	 * for.
	 * 
	 * @see MaintenanceWindowManager#DEFAULT_MODE
	 * @see MaintenanceWindowManager#ABOUT_TO_ENTER_MAINTENANCE_MODE
	 * @see MaintenanceWindowManager#IN_MAINTENANCE_MODE
	 */
	@Mandatory
	int getState();

}

