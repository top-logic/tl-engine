/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NegateRule;

/**
 * The StrictInNormalModeExecutable is executable when the system is not in maintenance mode
 * and is not about to enter maintenance mode.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class StrictInNormalModeExecutable implements ExecutabilityRule {

    public static final StrictInNormalModeExecutable INSTANCE = new StrictInNormalModeExecutable();

    public static final ExecutabilityRule NEGATED_INSTANCE = new NegateRule(INSTANCE, true);

    private static final ExecutabilityRule EXEC_RULE = CombinedExecutabilityRule.combine(InMaintenanceWindowExecutable.NEGATED_INSTANCE, AboutToEnterMaintenanceWindowExecutable.NEGATED_INSTANCE);

    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return EXEC_RULE.isExecutable(aComponent, model, someValues);
    }

}
