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
 * The AboutToEnterMaintenanceWindowExecutable is executable when the system is about to
 * enter a maintenance window (only users within specified groups are allowed to log in).
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class AboutToEnterMaintenanceWindowExecutable implements ExecutabilityRule {

    public static final AboutToEnterMaintenanceWindowExecutable INSTANCE = new AboutToEnterMaintenanceWindowExecutable();

    public static final ExecutabilityRule NEGATED_INSTANCE = new NegateRule(INSTANCE, true);

    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
        if (MaintenanceWindowManager.getInstance().getMaintenanceModeState() == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
            return ExecutableState.EXECUTABLE;
        }
        else return ExecutableState.NOT_EXEC_HIDDEN;
    }

}
