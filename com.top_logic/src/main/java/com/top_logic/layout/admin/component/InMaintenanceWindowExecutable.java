/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NegateRule;

/**
 * {@link ExecutabilityRule} allowing execution when the system is in a maintenance mode (only users
 * within specified groups are allowed to log in).
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class InMaintenanceWindowExecutable implements ExecutabilityRule {

    public static final InMaintenanceWindowExecutable INSTANCE = new InMaintenanceWindowExecutable();

    public static final ExecutabilityRule NEGATED_INSTANCE = new NegateRule(INSTANCE, true);

	private static final ExecutableState NOT_EXECUTABLE =
		new ExecutableState(ExecutableState.CommandVisibility.HIDDEN, I18NConstants.ERROR_NOT_IN_MAINTENANCE_MODE);

    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
        if (MaintenanceWindowManager.getInstance().getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
            return ExecutableState.EXECUTABLE;
        }
        return NOT_EXECUTABLE;
    }

}
