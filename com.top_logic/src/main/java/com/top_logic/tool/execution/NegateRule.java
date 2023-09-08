/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The NegateRule can be used to reverse the return value of {@link #isExecutable(LayoutComponent, Object, Map)}
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class NegateRule implements ExecutabilityRule {

    private ExecutabilityRule innerRule;
    private boolean isHidden;

    /**
     * Create a new NegateRule
     * @param aRule    the rule to invert
     * @param isHidden switch for the visibility to return in non executable state
     */
    public NegateRule(ExecutabilityRule aRule, boolean isHidden) {
        this.innerRule = aRule;
        this.isHidden  = isHidden;
    }

    /**
     * @see com.top_logic.tool.execution.ExecutabilityRule#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
     */
    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ExecutableState theState = this.innerRule.isExecutable(aComponent, model, someValues);
        if (theState.isExecutable()) {
            if (this.isHidden) {
                return ExecutableState.NOT_EXEC_HIDDEN;
            }
            else {
                return ExecutableState.NOT_EXEC_DISABLED;
            }
        }
        else {
            return ExecutableState.EXECUTABLE;
        }
    }

}

